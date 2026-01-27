package com.yibiai.thesis.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AigcService {

    private final DeepSeekService deepSeekService;

    public AigcService(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    public Flux<String> reduceAigc(String content, String language) {
        String langName = getLanguageName(language);
        
        String systemPrompt = String.format("""
            你是一位专业的学术写作专家，擅长将AI生成的文本改写为更自然、更人性化的学术表达。
            你的任务是对用户提供的论文内容进行改写，以降低AIGC检测率。
            
            改写要求：
            1. 保持原文的核心观点和学术含义不变
            2. 调整句式结构，使表达更加多样化
            3. 替换部分词汇为同义词或近义词
            4. 适当调整段落结构和逻辑顺序
            5. 增加一些过渡性语句，使文章更流畅
            6. 保持学术性和专业性，不要口语化
            7. 保留原文的格式（标题、段落等）
            8. 使用%s进行改写
            
            注意：
            - 不要添加任何解释性文字，直接输出改写后的内容
            - 不要改变原文的主题和核心论点
            - 保持原文的篇幅，不要大幅增减内容
            """, langName);

        String userPrompt = "请对以下论文内容进行改写，降低AIGC检测率：\n\n" + content;

        return deepSeekService.chatStream(systemPrompt, userPrompt);
    }

    private String getLanguageName(String langCode) {
        return switch (langCode) {
            case "en" -> "英语";
            case "ja" -> "日语";
            case "ko" -> "韩语";
            case "ru" -> "俄语";
            default -> "中文";
        };
    }
}
