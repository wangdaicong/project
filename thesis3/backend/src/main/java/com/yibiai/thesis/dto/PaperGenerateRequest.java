package com.yibiai.thesis.dto;

import lombok.Data;
import java.util.List;

@Data
public class PaperGenerateRequest {
    private String title;
    private String paperType;
    private String subject;
    private Integer wordCount;
    private String outline;
    private String previousContent;
    private String customRequirements;
    private String referenceContent;
    private Boolean includeCharts = false;
    private Boolean includeImages = false;
    private Boolean includeFormulas = false;
    private Boolean includeCode = false;
    private List<String> languages;
}
