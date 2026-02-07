package com.yibiai.thesis.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "optimization_change_logs")
public class OptimizationChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private OptimizationSession session;

    private Integer segmentIndex;

    private String stage;

    @Column(columnDefinition = "CLOB")
    private String beforeText;

    @Column(columnDefinition = "CLOB")
    private String afterText;

    @Column(columnDefinition = "CLOB")
    private String changesDetail;

    private LocalDateTime createdAt = LocalDateTime.now();
}
