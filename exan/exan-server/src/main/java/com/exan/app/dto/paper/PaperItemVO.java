package com.exan.app.dto.paper;

import java.time.LocalDate;

public record PaperItemVO(
    Long id,
    Long stageId,
    Long subjectId,
    String title,
    LocalDate paperDate,
    String regionCode
) {
}
