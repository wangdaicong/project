package com.yibiai.thesis.controller;

import com.yibiai.thesis.service.AigcService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/aigc")
public class AigcController {

    private final AigcService aigcService;

    public AigcController(AigcService aigcService) {
        this.aigcService = aigcService;
    }

    @PostMapping(value = "/reduce", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> reduceAigc(@RequestBody Map<String, String> request) {
        String content = request.getOrDefault("content", "");
        String language = request.getOrDefault("language", "zh");
        
        if (content.isBlank()) {
            return Flux.error(new IllegalArgumentException("内容不能为空"));
        }
        
        return aigcService.reduceAigc(content, language);
    }
}
