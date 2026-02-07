package com.yibiai.thesis.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OptimizationSessionResponse {
    private Long id;
    private String sessionId;
    private String currentStage;
    private String status;
    private Double progress;
    private Integer currentPosition;
    private Integer totalSegments;
    private String errorMessage;
    private String processingMode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}
