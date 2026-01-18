package com.exan.app.dto.practice;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreatePracticeSessionRequest(
    @NotNull Long stageId,
    @NotNull Long subjectId,
    @Min(1) @Max(50) Integer count
) {
}
