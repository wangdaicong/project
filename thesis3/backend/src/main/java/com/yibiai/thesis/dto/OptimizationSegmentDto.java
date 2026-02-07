package com.yibiai.thesis.dto;

import java.time.LocalDateTime;

public class OptimizationSegmentDto {
    private Long id;
    private String sessionId;
    private Integer segmentIndex;
    private String stage;
    private String originalText;
    private String polishedText;
    private String enhancedText;
    private String status;
    private Boolean isTitle;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getSegmentIndex() {
        return segmentIndex;
    }

    public void setSegmentIndex(Integer segmentIndex) {
        this.segmentIndex = segmentIndex;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getPolishedText() {
        return polishedText;
    }

    public void setPolishedText(String polishedText) {
        this.polishedText = polishedText;
    }

    public String getEnhancedText() {
        return enhancedText;
    }

    public void setEnhancedText(String enhancedText) {
        this.enhancedText = enhancedText;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsTitle() {
        return isTitle;
    }

    public void setIsTitle(Boolean title) {
        isTitle = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
