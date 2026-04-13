package com.failureiq.automation.reporting;

import com.failureiq.automation.config.FrameworkConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// This helper sends the generated report JSON to the FailureIQ backend API.
// It uses Java's built-in HttpClient to keep the design simple.
public final class FailureIqUploader {

    private FailureIqUploader() {
    }

    public static void uploadReport(FailureIqRunReport report) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(report);

            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FrameworkConfig.getFailureIqApiUrl()))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("FailureIQ upload succeeded.");
                System.out.println("API response status: " + response.statusCode());
                System.out.println("API response body: " + response.body());
            } else {
                System.out.println("FailureIQ upload failed.");
                System.out.println("API response status: " + response.statusCode());
                System.out.println("API response body: " + response.body());
            }
        } catch (IOException | InterruptedException exception) {
            System.out.println("FailureIQ upload could not be completed.");
            System.out.println("Reason: " + exception.getMessage());

            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
