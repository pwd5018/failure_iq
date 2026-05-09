package com.failureiq.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
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

// A TestRun represents one uploaded execution run from an automation suite.
@Entity
@Table(name = "test_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String runName;

    @Column(nullable = false)
    private String triggeredBy;

    private String browserName;

    private String browserVersion;

    private String environmentName;

    private String profileName;

    private String buildNumber;

    private String branchName;

    private String commitSha;

    private Double suiteDurationSeconds;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Tags make filtering simple without needing another entity.
    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "test_run_tags", joinColumns = @JoinColumn(name = "test_run_id"))
    @Column(name = "tag_value")
    private List<String> runTags = new ArrayList<>();

    // One test run can contain many individual test case results.
    @OneToMany(mappedBy = "testRun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TestCaseResult> testCaseResults = new ArrayList<>();

    @OneToMany(mappedBy = "testRun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RunSummaryRecord> summaryRecords = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
