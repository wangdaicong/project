package com.yibiai.thesis.repository;

import com.yibiai.thesis.entity.OptimizationSegment;
import com.yibiai.thesis.entity.OptimizationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptimizationSegmentRepository extends JpaRepository<OptimizationSegment, Long> {
    List<OptimizationSegment> findBySessionOrderBySegmentIndexAsc(OptimizationSession session);
}
