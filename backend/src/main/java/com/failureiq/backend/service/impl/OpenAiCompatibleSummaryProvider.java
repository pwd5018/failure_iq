package com.failureiq.backend.service.impl;

import com.failureiq.backend.config.AiSummaryProperties;
import com.failureiq.backend.dto.RunSummaryContextDto;
import com.failureiq.backend.dto.SummaryLength;
import com.failureiq.backend.dto.SummaryType;
import com.failureiq.backend.service.AiSummaryProvider;
import com.failureiq.backend.service.SummaryPromptBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// This provider handles any OpenAI-compatible chat completions endpoint.
// We use the same request path for Ollama, Groq, and other compatible APIs so
// provider switching is driven by configuration only.
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiCompatibleSummaryProvider implements AiSummaryProvider {

    private final AiSummaryProperties aiSummaryProperties;
    private final SummaryPromptBuilder summaryPromptBuilder;
    private final ObjectMapper objectMapper;

    @Override
    public String getProviderName() {
        return "openai-compatible";
    }

    @Override
    public boolean supportsProvider(String configuredProvider) {
        String provider = configuredProvider == null ? "" : configuredProvider.trim().toLowerCase();
        return "ollama".equals(provider)
                || "groq".equals(provider)
                || "openai-compatible".equals(provider);
    }

    @Override
    public boolean isAvailable() {
        return aiSummaryProperties.isEnabled()
                && supportsProvider(aiSummaryProperties.getNormalizedProvider())
                && aiSummaryProperties.getResolvedBaseUrl() != null
                && !aiSummaryProperties.getResolvedBaseUrl().isBlank()
                && aiSummaryProperties.getModel() != null
                && !aiSummaryProperties.getModel().isBlank();
    }

    @Override
    public String generateSummary(RunSummaryContextDto summaryContext, SummaryType summaryType, SummaryLength summaryLength) {
        try {
            String prompt = summaryPromptBuilder.buildPrompt(summaryContext, summaryType, summaryLength);
            URI completionsUri = buildChatCompletionsUri(aiSummaryProperties.getResolvedBaseUrl());

            JsonNode requestBody = objectMapper.createObjectNode()
                    .put("model", aiSummaryProperties.getModel())
                    // Lower temperature keeps the summary grounded and more consistent.
                    .put("temperature", 0.2)
                    .set("messages", objectMapper.createArrayNode()
                            .add(objectMapper.createObjectNode()
                                    .put("role", "system")
                                    .put("content", "You are a careful QA summary assistant. Stay grounded in the provided run data only."))
                            .add(objectMapper.createObjectNode()
                                    .put("role", "user")
                                    .put("content", prompt)));

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(completionsUri)
                    .timeout(Duration.ofSeconds(aiSummaryProperties.getTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)));

            if (requiresAuthorizationHeader()
                    && aiSummaryProperties.getApiKey() != null
                    && !aiSummaryProperties.getApiKey().isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + aiSummaryProperties.getApiKey());
            }

            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(aiSummaryProperties.getTimeoutSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn(
                        "AI summary request failed for provider {} with status {} at {}",
                        aiSummaryProperties.getNormalizedProvider(),
                        response.statusCode(),
                        completionsUri
                );
                throw new IllegalStateException("AI summary request failed with status " + response.statusCode());
            }

            return extractSummaryText(response.body());
        } catch (Exception exception) {
            log.warn(
                    "AI summary generation failed for provider {} using base URL {}: {}",
                    aiSummaryProperties.getNormalizedProvider(),
                    aiSummaryProperties.getResolvedBaseUrl(),
                    exception.getMessage()
            );
            throw new IllegalStateException("AI summary generation failed: " + exception.getMessage(), exception);
        }
    }

    private URI buildChatCompletionsUri(String configuredBaseUrl) {
        String trimmedBaseUrl = configuredBaseUrl.trim();

        if (trimmedBaseUrl.endsWith("/chat/completions")) {
            return URI.create(trimmedBaseUrl);
        }

        if (trimmedBaseUrl.endsWith("/")) {
            return URI.create(trimmedBaseUrl + "chat/completions");
        }

        return URI.create(trimmedBaseUrl + "/chat/completions");
    }

    private boolean requiresAuthorizationHeader() {
        return !"ollama".equals(aiSummaryProperties.getNormalizedProvider());
    }

    private String extractSummaryText(String responseBody) throws Exception {
        JsonNode rootNode = objectMapper.readTree(responseBody);

        if (rootNode.hasNonNull("summary")) {
            return rootNode.get("summary").asText();
        }

        if (rootNode.hasNonNull("response")) {
            return rootNode.get("response").asText();
        }

        JsonNode choicesNode = rootNode.path("choices");
        if (choicesNode.isArray() && !choicesNode.isEmpty()) {
            JsonNode firstChoice = choicesNode.get(0);

            if (firstChoice.path("message").hasNonNull("content")) {
                return firstChoice.path("message").get("content").asText();
            }

            if (firstChoice.hasNonNull("text")) {
                return firstChoice.get("text").asText();
            }
        }

        throw new IllegalStateException("AI provider response did not include summary text.");
    }
}
