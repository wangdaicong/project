package com.exan.app.dto.admin;

public record CreateImportJobResponse(
    long jobId,
    int totalCount,
    int insertedCount,
    int duplicateCount,
    int failedCount
) {
}
