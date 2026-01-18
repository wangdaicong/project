package com.exan.api.admin;

import com.exan.app.dto.admin.CreateImportJobResponse;
import com.exan.app.dto.admin.ImportQuestionsRequest;
import com.exan.app.service.ImportJobService;
import com.exan.domain.entity.ImportJob;
import com.exan.domain.entity.Question;
import com.exan.infra.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/import-jobs")
@RequiredArgsConstructor
public class AdminImportJobController {
    private final ImportJobService importJobService;

    @PostMapping("/question-json")
    public ApiResponse<CreateImportJobResponse> createQuestionJson(@RequestBody @Valid ImportQuestionsRequest req) {
        return ApiResponse.ok(importJobService.createQuestionJsonJob(req.items()));
    }

    @PostMapping("/question-json-file")
    public ApiResponse<CreateImportJobResponse> createQuestionJsonFile(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(importJobService.createQuestionJsonJobFromFile(file));
    }

    @GetMapping
    public ApiResponse<List<ImportJob>> list(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(importJobService.listJobs(limit));
    }

    @GetMapping("/{id}")
    public ApiResponse<ImportJob> detail(@PathVariable("id") long id) {
        return ApiResponse.ok(importJobService.getJob(id));
    }

    @GetMapping("/{id}/pending-questions")
    public ApiResponse<List<Question>> pendingQuestions(@PathVariable("id") long id, @RequestParam(defaultValue = "200") int limit) {
        return ApiResponse.ok(importJobService.listPendingQuestionsByJob(id, limit));
    }

    @PostMapping("/{id}/approve-all")
    public ApiResponse<Integer> approveAll(@PathVariable("id") long id) {
        return ApiResponse.ok(importJobService.approveAllPendingByJob(id));
    }

    @PostMapping("/{id}/reject-all")
    public ApiResponse<Integer> rejectAll(@PathVariable("id") long id) {
        return ApiResponse.ok(importJobService.rejectAllPendingByJob(id));
    }
}
