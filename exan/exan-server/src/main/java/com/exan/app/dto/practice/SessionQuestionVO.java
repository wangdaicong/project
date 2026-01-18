package com.exan.app.dto.practice;

import java.util.List;

public record SessionQuestionVO(
    Long id,
    String type,
    String stem,
    Integer difficulty,
    List<QuestionOptionVO> options
) {
    public record QuestionOptionVO(String key, String content) {
    }
}
