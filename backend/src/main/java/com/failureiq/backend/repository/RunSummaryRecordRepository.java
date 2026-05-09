package com.failureiq.backend.repository;

import com.failureiq.backend.dto.SummaryLength;
import com.failureiq.backend.dto.SummaryType;
import com.failureiq.backend.entity.RunSummaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// This repository stores generated summaries for later reuse.
public interface RunSummaryRecordRepository extends JpaRepository<RunSummaryRecord, Long> {

    Optional<RunSummaryRecord> findTopByTestRunIdAndSummaryTypeAndSummaryLengthAndRequestedProviderAndProviderModelOrderByGeneratedAtDesc(
            Long testRunId,
            SummaryType summaryType,
            SummaryLength summaryLength,
            String requestedProvider,
            String providerModel
    );

    List<RunSummaryRecord> findByTestRunIdOrderByGeneratedAtDesc(Long testRunId);
}
