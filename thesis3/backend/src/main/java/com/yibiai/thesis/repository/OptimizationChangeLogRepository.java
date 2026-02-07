package com.yibiai.thesis.repository;

import com.yibiai.thesis.entity.OptimizationChangeLog;
import com.yibiai.thesis.entity.OptimizationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptimizationChangeLogRepository extends JpaRepository<OptimizationChangeLog, Long> {
    List<OptimizationChangeLog> findBySessionOrderBySegmentIndexAscCreatedAtAsc(OptimizationSession session);
}
