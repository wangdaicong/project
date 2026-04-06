package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 历年分数线实体类
 */
@Data
@TableName("score_line")
public class ScoreLine {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 院校ID
     */
    private Long universityId;
    
    /**
     * 院校名称
     */
    private String universityName;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 年份
     */
    private Integer year;
    
    /**
     * 批次（本科一批、本科二批等）
     */
    private String batch;
    
    /**
     * 科类（理科、文科）
     */
    private String category;
    
    /**
     * 最低分
     */
    private Integer minScore;
    
    /**
     * 平均分
     */
    private Integer avgScore;
    
    /**
     * 最高分
     */
    private Integer maxScore;
    
    /**
     * 最低位次
     */
    private Integer minRank;
    
    /**
     * 招生人数
     */
    private Integer enrollmentCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
