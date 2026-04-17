package com.failureiq.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// These properties control whether AI summaries are enabled and how the app
// should talk to a local or remote text-generation endpoint.
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "failureiq.ai")
public class AiSummaryProperties {

    private boolean enabled = false;
    private String provider = "fallback";
    private String model = "local-model";
    private String baseUrl = "";
    // This older property is still supported so existing local setups do not break.
    private String endpoint = "";
    private String apiKey = "";
    private int timeoutSeconds = 20;
    private String summaryStyle = "both";

    public String getResolvedBaseUrl() {
        if (baseUrl != null && !baseUrl.isBlank()) {
            return baseUrl.trim();
        }

        return endpoint == null ? "" : endpoint.trim();
    }

    public String getNormalizedProvider() {
        return provider == null ? "fallback" : provider.trim().toLowerCase();
    }
}
