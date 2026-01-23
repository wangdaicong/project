package com.yibiai.thesis.controller;

import com.yibiai.thesis.dto.ApiResponse;
import com.yibiai.thesis.dto.OutlineRequest;
import com.yibiai.thesis.dto.PaperGenerateRequest;
import com.yibiai.thesis.entity.Paper;
import com.yibiai.thesis.service.PaperService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/paper")
public class PaperController {

    private final PaperService paperService;

    public PaperController(PaperService paperService) {
        this.paperService = paperService;
    }

    @PostMapping("/outline")
    public Mono<ApiResponse<String>> generateOutline(@RequestBody OutlineRequest request) {
        return paperService.generateOutline(request)
                .map(ApiResponse::success)
                .onErrorResume(e -> Mono.just(ApiResponse.error(e.getMessage())));
    }

    @PostMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generatePaperStream(@RequestBody PaperGenerateRequest request) {
        return paperService.generatePaperStream(request);
    }

    @PostMapping("/generate")
    public Mono<ApiResponse<String>> generatePaper(@RequestBody PaperGenerateRequest request) {
        return paperService.generatePaper(request)
                .map(ApiResponse::success)
                .onErrorResume(e -> Mono.just(ApiResponse.error(e.getMessage())));
    }

    @PostMapping("/references")
    public Mono<ApiResponse<String>> generateReferences(
            @RequestParam String title,
            @RequestParam String subject,
            @RequestParam(defaultValue = "40") int count) {
        return paperService.generateReferences(title, subject, count)
                .map(ApiResponse::success)
                .onErrorResume(e -> Mono.just(ApiResponse.error(e.getMessage())));
    }

    @PostMapping("/save")
    public ApiResponse<Paper> savePaper(@RequestBody Paper paper) {
        try {
            Paper saved = paperService.savePaper(paper);
            return ApiResponse.success("保存成功", saved);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<Paper> getPaper(@PathVariable Long id) {
        return paperService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("论文不存在"));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<Paper>> getUserPapers(@PathVariable Long userId) {
        List<Paper> papers = paperService.findByUserId(userId);
        return ApiResponse.success(papers);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePaper(@PathVariable Long id) {
        try {
            paperService.deletePaper(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
