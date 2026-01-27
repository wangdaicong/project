package com.yibiai.thesis.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class PaperPassService {

    private final DeepSeekService deepSeekService;

    public PaperPassService(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    public Flux<String> reduceRepetition(String content, String language) {
        String langName = getLanguageName(language);
        
        String systemPrompt = String.format("""
            你是一位专业的学术写作专家，擅长对论文进行降重改写，以降低查重系统的重复率检测结果。
            你的任务是对用户提供的论文内容进行深度改写，使其通过各类查重系统检测。
            
            降重改写要求：
            1. 彻底改变句子结构，使用不同的表达方式重新组织语句
            2. 用同义词、近义词替换原文中的关键词汇
            3. 调整语序，将主动句改为被动句或反之
            4. 拆分长句为短句，或合并短句为长句
            5. 增加过渡词和连接词，使文章更流畅
            6. 保持原文的核心观点和学术含义不变
            7. 保持学术性和专业性，不要口语化
            8. 保留原文的格式（标题、段落等）
            9. 使用%s进行改写
            
            注意：
            - 不要添加任何解释性文字，直接输出改写后的内容
            - 不要改变原文的主题和核心论点
            - 保持原文的篇幅，不要大幅增减内容
            - 确保改写后的内容与原文有足够的差异，以通过查重检测
            """, langName);

        String userPrompt = "请对以下论文内容进行深度降重改写，降低查重率：\n\n" + content;

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
