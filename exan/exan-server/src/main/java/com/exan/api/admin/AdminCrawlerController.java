package com.exan.api.admin;

import com.exan.app.service.PaperCrawlerService;
import com.exan.infra.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/crawlers")
@RequiredArgsConstructor
public class AdminCrawlerController {
    private final PaperCrawlerService paperCrawlerService;

    @PostMapping("/papers/sync-demo")
    public ApiResponse<Integer> syncDemo(
        @RequestParam long stageId,
        @RequestParam long subjectId
    ) {
        return ApiResponse.ok(paperCrawlerService.syncDemo(stageId, subjectId));
    }

    @PostMapping("/papers/sync-zxxk-primary-g3-math")
    public ApiResponse<Integer> syncZxxkPrimaryG3Math(
        @RequestParam long stageId,
        @RequestParam long subjectId,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(required = false) String cookie,
        @RequestParam(required = false) String url
    ) {
        if (url == null || url.isBlank()) {
            return ApiResponse.ok(paperCrawlerService.syncZxxkPrimaryGrade3Math(stageId, subjectId, limit, cookie));
        }
        return ApiResponse.ok(paperCrawlerService.syncZxxkPrimaryGrade3MathByUrl(stageId, subjectId, limit, url, cookie));
    }

    @PostMapping("/papers/debug-zxxk-html")
    public ApiResponse<String> debugZxxkHtml(
        @RequestParam(required = false) String cookie,
        @RequestParam(defaultValue = "800") int maxLen,
        @RequestParam(required = false) String url
    ) {
        if (url == null || url.isBlank()) {
            return ApiResponse.ok(paperCrawlerService.debugFetchZxxkHtmlSnippet(cookie, maxLen));
        }
        return ApiResponse.ok(paperCrawlerService.debugFetchZxxkHtmlSnippetByUrl(url, cookie, maxLen));
    }

    @PostMapping("/papers/debug-zxxk-stats")
    public ApiResponse<String> debugZxxkStats(
        @RequestParam(required = false) String cookie,
        @RequestParam(defaultValue = "20") int sample,
        @RequestParam(required = false) String url
    ) {
        if (url == null || url.isBlank()) {
            return ApiResponse.ok(paperCrawlerService.debugZxxkStats(cookie, sample));
        }
        return ApiResponse.ok(paperCrawlerService.debugZxxkStatsByUrl(url, cookie, sample));
    }

    @PostMapping("/papers/debug-zxxk-body-lines")
    public ApiResponse<String> debugZxxkBodyLines(
        @RequestParam String url,
        @RequestParam(defaultValue = "20") int sample
    ) {
        return ApiResponse.ok(paperCrawlerService.debugZxxkBodyLinesByUrl(url, sample));
    }

    @PostMapping("/papers/sync-zxxk-content")
    public ApiResponse<Integer> syncZxxkPaperContent(
        @RequestParam long paperId,
        @RequestParam String title,
        @RequestParam String listUrl,
        @RequestParam(required = false) String cookie
    ) {
        return ApiResponse.ok(paperCrawlerService.syncZxxkPaperContentByTitle(paperId, title, listUrl, cookie));
    }
}
