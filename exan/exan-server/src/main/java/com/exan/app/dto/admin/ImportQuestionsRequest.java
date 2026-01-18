package com.exan.app.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ImportQuestionsRequest(
    @NotEmpty @Valid List<ImportQuestionItem> items
) {
}
