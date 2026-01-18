package com.exan.app.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ImportQuestionItem(
    @NotNull Long stageId,
    @NotNull Long subjectId,
    @NotBlank String type,
    @NotBlank String stem,
    Integer difficulty,
    String analysis,
    Object answer,
    List<ImportQuestionOptionItem> options
) {
}
