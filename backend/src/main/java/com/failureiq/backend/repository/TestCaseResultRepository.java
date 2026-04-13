package com.failureiq.backend.repository;

import com.failureiq.backend.entity.TestCaseResult;
import com.failureiq.backend.enums.TestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseResultRepository extends JpaRepository<TestCaseResult, Long> {

    long countByStatus(TestStatus status);
}
