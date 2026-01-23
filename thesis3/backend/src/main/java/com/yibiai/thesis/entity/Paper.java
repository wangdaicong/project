package com.yibiai.thesis.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "papers")
public class Paper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String title;
    private String paperType;
    private String subject;
    private Integer wordCount;

    @Column(columnDefinition = "CLOB")
    private String outline;

    @Column(columnDefinition = "CLOB")
    private String content;

    @Column(columnDefinition = "CLOB")
    private String references;

    private String status = "draft";
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
