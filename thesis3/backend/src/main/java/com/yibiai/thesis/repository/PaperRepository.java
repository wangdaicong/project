package com.yibiai.thesis.repository;

import com.yibiai.thesis.entity.Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaperRepository extends JpaRepository<Paper, Long> {
    List<Paper> findByUserIdOrderByCreatedAtDesc(Long userId);
}
