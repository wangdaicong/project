package com.exan.app.dto.practice;

import java.util.List;

public record SessionDetailResponse(
    Long sessionId,
    Long stageId,
    Long subjectId,
    String status,
    Integer scoreGot,
    Integer scoreTotal,
    List<SessionQuestionVO> questions
) {
}
