package com.failureiq.backend.repository;

import com.failureiq.backend.entity.TestRun;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// JpaRepository gives us ready-made CRUD methods like save, findAll, and findById.
public interface TestRunRepository extends JpaRepository<TestRun, Long> {

    // This loads runs with their test case results so history calculations stay simple.
    @EntityGraph(attributePaths = "testCaseResults")
    List<TestRun> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "testCaseResults")
    Optional<TestRun> findTopByOrderByCreatedAtDesc();

    @Override
    @EntityGraph(attributePaths = "testCaseResults")
    Optional<TestRun> findById(Long id);
}
