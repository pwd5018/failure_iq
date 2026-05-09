package com.failureiq.backend.service;

import com.failureiq.backend.dto.TriageAssistantResponseDto;

public interface AiTriageAssistantService {

    TriageAssistantResponseDto getRunTriageAssistant(Long runId);

    TriageAssistantResponseDto regenerateRunTriageAssistant(Long runId);

    TriageAssistantResponseDto getLatestRunTriageAssistant();

    TriageAssistantResponseDto regenerateLatestRunTriageAssistant();
}
