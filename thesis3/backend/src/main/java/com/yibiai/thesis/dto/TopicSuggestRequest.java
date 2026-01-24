package com.yibiai.thesis.dto;

import lombok.Data;

@Data
public class TopicSuggestRequest {
    private String direction;      // 论文方向/关键词
    private String paperType;      // 论文类型：毕业论文、期刊论文、职称论文等
    private Integer wordCount;     // 字数要求
    private Integer count = 10;    // 生成题目数量，默认10个
}
