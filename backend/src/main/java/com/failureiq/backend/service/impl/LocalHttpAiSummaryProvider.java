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
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// This provider sends one prompt to a configurable HTTP endpoint.
// It supports a small local-development-friendly contract first:
// request body: { "model": "...", "prompt": "..." }
// response body: either { "summary": "..." } or an OpenAI-style choices array.
@Service
@RequiredArgsConstructor
public class LocalHttpAiSummaryProvider implements AiSummaryProvider {

    private final AiSummaryProperties aiSummaryProperties;
    private final SummaryPromptBuilder summaryPromptBuilder;
    private final ObjectMapper objectMapper;

    @Override
    public String getProviderName() {
        return "local-http";
    }

    @Override
    public boolean isAvailable() {
        return aiSummaryProperties.isEnabled()
                && "local-http".equalsIgnoreCase(aiSummaryProperties.getProvider())
                && aiSummaryProperties.getEndpoint() != null
                && !aiSummaryProperties.getEndpoint().isBlank();
    }

    @Override
    public String generateSummary(RunSummaryContextDto summaryContext, SummaryType summaryType, SummaryLength summaryLength) {
        try {
            String prompt = summaryPromptBuilder.buildPrompt(summaryContext, summaryType, summaryLength);

            JsonNode requestBody = objectMapper.createObjectNode()
                    .put("model", aiSummaryProperties.getModel())
                    .put("prompt", prompt);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(aiSummaryProperties.getEndpoint()))
                    .timeout(Duration.ofSeconds(aiSummaryProperties.getTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)));

            if (aiSummaryProperties.getApiKey() != null && !aiSummaryProperties.getApiKey().isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + aiSummaryProperties.getApiKey());
            }

            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(aiSummaryProperties.getTimeoutSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("AI summary request failed with status " + response.statusCode());
            }

            return extractSummaryText(response.body());
        } catch (Exception exception) {
            throw new IllegalStateException("AI summary generation failed: " + exception.getMessage(), exception);
        }
    }

    private String extractSummaryText(String responseBody) throws Exception {
        JsonNode rootNode = objectMapper.readTree(responseBody);

        if (rootNode.hasNonNull("summary")) {
            return rootNode.get("summary").asText();
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
