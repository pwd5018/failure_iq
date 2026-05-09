package com.failureiq.backend.entity;

import com.failureiq.backend.dto.SummaryLength;
import com.failureiq.backend.dto.SummaryType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// This entity stores generated summaries so the app can reuse them later.
@Entity
@Table(name = "run_summary_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunSummaryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SummaryType summaryType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SummaryLength summaryLength;

    @Column(nullable = false)
    private String requestedProvider;

    private String providerModel;

    @Column(nullable = false)
    private String generatedBy;

    @Column(nullable = false)
    private boolean usedFallback;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Column(length = 500)
    private String headline;

    @Column(length = 2000)
    private String shortSummary;

    @Column(length = 8000)
    private String summaryText;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "run_summary_record_bullets", joinColumns = @JoinColumn(name = "summary_record_id"))
    @Column(name = "triage_bullet", length = 1000)
    private List<String> triageBullets = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "test_run_id", nullable = false)
    private TestRun testRun;

    @PrePersist
    public void prePersist() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}
