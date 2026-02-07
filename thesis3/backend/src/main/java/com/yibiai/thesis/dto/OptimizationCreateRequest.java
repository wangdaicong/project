package com.yibiai.thesis.dto;

import lombok.Data;

@Data
public class OptimizationCreateRequest {
    private String originalText;
    private String processingMode = "paper_polish_enhance";
}
