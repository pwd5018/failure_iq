package com.failureiq.backend.repository;

import com.failureiq.backend.entity.TestRun;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository gives us ready-made CRUD methods like save, findAll, and findById.
public interface TestRunRepository extends JpaRepository<TestRun, Long> {
}
