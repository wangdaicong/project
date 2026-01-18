package com.exan.api;

import com.exan.app.dto.paper.PaperDetailResponse;
import com.exan.app.dto.paper.ListPapersResponse;
import com.exan.app.service.PaperService;
import com.exan.infra.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/papers")
@RequiredArgsConstructor
public class PaperController {
    private final PaperService paperService;

    @GetMapping
    public ApiResponse<ListPapersResponse> list(
        @RequestParam long stageId,
        @RequestParam long subjectId,
        @RequestParam(required = false) Integer grade,
        @RequestParam(required = false) String regionCode,
        @RequestParam(defaultValue = "50") int limit
    ) {
        return ApiResponse.ok(paperService.listPapers(stageId, subjectId, grade, regionCode, limit));
    }

    @GetMapping("/{id}")
    public ApiResponse<PaperDetailResponse> getDetail(@PathVariable long id) {
        return ApiResponse.ok(paperService.getPaperDetail(id));
    }

    @PostMapping("/{id}/download")
    public ApiResponse<Long> incDownload(@PathVariable long id) {
        return ApiResponse.ok(paperService.incDownload(id));
    }
}
