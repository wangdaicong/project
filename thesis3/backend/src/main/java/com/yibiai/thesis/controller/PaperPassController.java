package com.yibiai.thesis.controller;

import com.yibiai.thesis.service.PaperPassService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/paperpass")
public class PaperPassController {

    private final PaperPassService paperPassService;

    public PaperPassController(PaperPassService paperPassService) {
        this.paperPassService = paperPassService;
    }

    @PostMapping(value = "/reduce", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> reduceRepetition(@RequestBody Map<String, String> request) {
        String content = request.getOrDefault("content", "");
        String language = request.getOrDefault("language", "zh");
        
        if (content.isBlank()) {
            return Flux.error(new IllegalArgumentException("内容不能为空"));
        }
        
        return paperPassService.reduceRepetition(content, language);
    }
}
