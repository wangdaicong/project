package com.yibiai.thesis.repository;

import com.yibiai.thesis.entity.OptimizationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OptimizationSessionRepository extends JpaRepository<OptimizationSession, Long> {
    Optional<OptimizationSession> findBySessionId(String sessionId);
}
