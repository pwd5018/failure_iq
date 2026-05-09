package com.failureiq.backend.dto;

// These target types tell the UI what kind of object the recommendation points to.
public enum TriageTargetType {
    CLUSTER,
    TEST,
    RUN_DIFF,
    FLAKY_TEST
}
