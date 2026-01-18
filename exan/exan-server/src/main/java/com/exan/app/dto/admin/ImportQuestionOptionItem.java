package com.exan.app.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record ImportQuestionOptionItem(
    @NotBlank String key,
    @NotBlank String content
) {
}
