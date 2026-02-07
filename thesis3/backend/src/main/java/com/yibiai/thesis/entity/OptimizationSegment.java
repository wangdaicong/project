package com.yibiai.thesis.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "optimization_segments")
public class OptimizationSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private OptimizationSession session;

    private Integer segmentIndex;

    private String stage;

    @Column(columnDefinition = "CLOB")
    private String originalText;

    @Column(columnDefinition = "CLOB")
    private String polishedText;

    @Column(columnDefinition = "CLOB")
    private String enhancedText;

    private String status;

    private Boolean isTitle = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime completedAt;
}
