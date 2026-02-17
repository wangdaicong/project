package com.yibiai.thesis.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "optimization_sessions")
public class OptimizationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sessionId;

    private Long userId;

    @Column(columnDefinition = "CLOB")
    private String originalText;

    private String originalFileName;

    private String processingMode;

    private String currentStage;

    private String status;

    private Double progress = 0.0;

    private Integer currentPosition = 0;

    private Integer totalSegments = 0;

    private Integer failedSegmentIndex;

    @Column(columnDefinition = "CLOB")
    private String errorMessage;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime completedAt;
}
