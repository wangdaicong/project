package com.yibiai.thesis.dto;

import lombok.Data;

@Data
public class OutlineRequest {
    private String title;
    private String paperType;
    private String subject;
    private Integer wordCount;
    private String customRequirements;
    private String referenceContent;
}
