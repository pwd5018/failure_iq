package com.failureiq.backend.service;

import com.failureiq.backend.dto.RunTriageContextDto;

public interface RunTriageContextService {

    RunTriageContextDto getRunTriageContext(Long runId);

    RunTriageContextDto getLatestRunTriageContext();
}
