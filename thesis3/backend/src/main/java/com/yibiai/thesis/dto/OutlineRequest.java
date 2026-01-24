package com.yibiai.thesis.dto;

import lombok.Data;

import java.util.List;

@Data
public class OutlineRequest {
    private String title;
    private String paperType;
    private String subject;
    private List<String> languages;
    private Integer wordCount;
    private String customRequirements;
    private String referenceContent;
}
