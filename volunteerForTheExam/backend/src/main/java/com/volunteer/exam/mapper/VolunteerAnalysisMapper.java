package com.volunteer.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.exam.entity.VolunteerAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 志愿分析结果Mapper
 */
@Mapper
public interface VolunteerAnalysisMapper extends BaseMapper<VolunteerAnalysis> {
    
    @Select("SELECT * FROM volunteer_analysis WHERE application_id = #{applicationId}")
    VolunteerAnalysis selectByApplicationId(@Param("applicationId") Long applicationId);
}
