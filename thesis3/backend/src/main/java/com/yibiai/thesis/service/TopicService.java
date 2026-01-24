package com.yibiai.thesis.service;

import com.yibiai.thesis.dto.TopicSuggestRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TopicService {

    private final DeepSeekService deepSeekService;

    public TopicService(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    public Mono<String> suggestTopics(TopicSuggestRequest request) {
        String systemPrompt = buildSystemPrompt(request);
        String userPrompt = buildUserPrompt(request);
        return deepSeekService.chat(systemPrompt, userPrompt);
    }

    private String buildSystemPrompt(TopicSuggestRequest request) {
        int count = request.getCount() != null ? request.getCount() : 10;
        return String.format("""
            你是一位资深的学术论文选题专家，拥有丰富的学术研究经验。请根据用户提供的研究方向和要求，生成%d个高质量的论文题目建议。
            
            选题要求：
            1. 题目要具有学术价值和研究意义
            2. 题目要明确、具体，避免过于宽泛
            3. 题目要符合当前学术研究热点和趋势
            4. 题目要具有可行性，适合在规定字数内完成
            5. 每个题目都要有创新点或独特视角
            
            输出格式要求：
            请严格按照以下JSON格式输出，不要添加任何其他内容：
            {
              "topics": [
                {
                  "title": "论文题目1",
                  "description": "简要说明该题目的研究意义和创新点（50字以内）",
                  "keywords": ["关键词1", "关键词2", "关键词3"]
                },
                ...
              ]
            }
            """, count);
    }

    private String buildUserPrompt(TopicSuggestRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("研究方向/关键词：").append(request.getDirection()).append("\n");
        
        if (request.getPaperType() != null && !request.getPaperType().isEmpty()) {
            sb.append("论文类型：").append(request.getPaperType()).append("\n");
        }
        
        if (request.getWordCount() != null && request.getWordCount() > 0) {
            sb.append("目标字数：").append(request.getWordCount()).append("字\n");
        }
        
        int count = request.getCount() != null ? request.getCount() : 10;
        sb.append("请生成").append(count).append("个论文题目建议。");
        
        return sb.toString();
    }
}
