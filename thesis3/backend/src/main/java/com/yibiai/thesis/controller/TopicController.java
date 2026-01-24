package com.yibiai.thesis.controller;

import com.yibiai.thesis.dto.ApiResponse;
import com.yibiai.thesis.dto.TopicSuggestRequest;
import com.yibiai.thesis.service.TopicService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/topic")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @PostMapping("/suggest")
    public Mono<ApiResponse<String>> suggestTopics(@RequestBody TopicSuggestRequest request) {
        return topicService.suggestTopics(request)
                .map(ApiResponse::success)
                .onErrorResume(e -> Mono.just(ApiResponse.error(500, "生成选题失败: " + e.getMessage())));
    }
}
