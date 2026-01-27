package com.yibiai.thesis.controller;

import com.yibiai.thesis.service.PptService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/ppt")
public class PptController {

    private final PptService pptService;

    public PptController(PptService pptService) {
        this.pptService = pptService;
    }

    @PostMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateOutline(@RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "");
        String content = request.getOrDefault("content", "");
        
        if (title.isBlank() || content.isBlank()) {
            return Flux.error(new IllegalArgumentException("标题和内容不能为空"));
        }
        
        return pptService.generateOutline(title, content);
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportPptx(@RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "答辩PPT");
        String outline = request.getOrDefault("outline", "");
        
        if (outline.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        byte[] pptxBytes = pptService.exportPptx(title, outline);
        
        String encodedFilename = URLEncoder.encode(title + ".pptx", StandardCharsets.UTF_8)
                .replace("+", "%20");
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"))
                .body(pptxBytes);
    }
}
