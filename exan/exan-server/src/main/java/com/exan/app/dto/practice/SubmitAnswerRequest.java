package com.exan.app.dto.practice;

import jakarta.validation.constraints.NotNull;

public record SubmitAnswerRequest(
    @NotNull Long questionId,
    @NotNull Object answer
) {
}
