package com.failureiq.backend.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
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

// This entity stores generated triage assistant responses for one run.
@Entity
@Table(name = "run_triage_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunTriageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(length = 3000)
    private String overallRecommendation;

    @Column(length = 20000)
    private String recommendationsJson;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "run_triage_record_actions", joinColumns = @JoinColumn(name = "triage_record_id"))
    @Column(name = "action_text", length = 1000)
    private List<String> topActions = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "run_triage_record_evidence", joinColumns = @JoinColumn(name = "triage_record_id"))
    @Column(name = "evidence_text", length = 1000)
    private List<String> evidence = new ArrayList<>();

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
