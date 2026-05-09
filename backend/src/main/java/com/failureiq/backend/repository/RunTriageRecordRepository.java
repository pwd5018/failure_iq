package com.failureiq.backend.repository;

import com.failureiq.backend.entity.RunTriageRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// This repository stores AI triage assistant results for reuse.
public interface RunTriageRecordRepository extends JpaRepository<RunTriageRecord, Long> {

    Optional<RunTriageRecord> findTopByTestRunIdAndRequestedProviderAndProviderModelOrderByGeneratedAtDesc(
            Long testRunId,
            String requestedProvider,
            String providerModel
    );
}
