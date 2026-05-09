package com.failureiq.backend.service.impl;

import com.failureiq.backend.dto.RunTriageContextDto;
import com.failureiq.backend.dto.TriageRecommendationItemDto;
import com.failureiq.backend.service.AiTriageProvider;
import org.springframework.stereotype.Service;

import java.util.List;

// This provider always works because it only uses deterministic triage facts.
@Service
public class FallbackAiTriageProvider implements AiTriageProvider {

    @Override
    public String getProviderName() {
        return "fallback";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String generateTriageText(RunTriageContextDto triageContext, List<TriageRecommendationItemDto> rankedTargets) {
        TriageRecommendationItemDto firstTarget = rankedTargets.get(0);
        StringBuilder text = new StringBuilder();

        text.append("HEADLINE: Start with ").append(firstTarget.getTitle()).append(".\n");
        text.append("OVERALL_RECOMMENDATION: ");
        text.append("This run has ").append(triageContext.getNewlyFailingCount()).append(" newly failing tests, ");
        text.append(triageContext.getStillFailingCount()).append(" still failing tests, and ");
        text.append(triageContext.getTopFailureClusters().size()).append(" major failure clusters worth grouping together. ");
        text.append("The best next step is to work through the ranked targets in order, starting with the clearest regression signals and then checking the broader shared failures.\n");
        text.append("TOP_ACTIONS:\n");
        rankedTargets.stream().limit(3).forEach(item ->
                text.append("- ").append(item.getSuggestedNextStep()).append("\n")
        );
        text.append("EVIDENCE:\n");
        text.append("- Newly failing tests are prioritized ahead of older known noise.\n");
        if (!triageContext.getTopFailureClusters().isEmpty()) {
            text.append("- The largest cluster affects ").append(triageContext.getTopFailureClusters().get(0).getTestCount()).append(" tests.\n");
        }
        if (!triageContext.getTopFlakyTests().isEmpty()) {
            text.append("- The run also includes flaky history that may affect reproduction confidence.\n");
        }

        return text.toString();
    }
}
