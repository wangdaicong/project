package com.exan.api.admin;

import com.exan.app.dto.admin.ImportQuestionsRequest;
import com.exan.app.service.QuestionAdminService;
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

import java.util.List;

@RestController
@RequestMapping("/admin/questions")
@RequiredArgsConstructor
public class AdminQuestionController {
    private final QuestionAdminService questionAdminService;

    @PostMapping("/import")
    public ApiResponse<Void> importQuestions(@RequestBody @Valid ImportQuestionsRequest req) {
        questionAdminService.importQuestions(req.items());
        return ApiResponse.ok(null);
    }

    @GetMapping("/pending")
    public ApiResponse<List<Question>> pending(@RequestParam long subjectId, @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.ok(questionAdminService.listPending(subjectId, limit));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable("id") long id) {
        questionAdminService.approve(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable("id") long id) {
        questionAdminService.reject(id);
        return ApiResponse.ok(null);
    }
}
