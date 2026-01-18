package com.exan.app.dto.paper;

import java.time.LocalDate;

public record PaperDetailResponse(
    Long id,
    Long stageId,
    Long subjectId,
    String title,
    LocalDate paperDate,
    String regionCode,
    String sourceUrl,
    Long views,
    Long downloads,
    String contentText,
    String attachmentsJson
) {
}
