package com.volunteer.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.exam.entity.VolunteerDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 志愿详情Mapper
 */
@Mapper
public interface VolunteerDetailMapper extends BaseMapper<VolunteerDetail> {
    
    @Select("SELECT * FROM volunteer_detail WHERE application_id = #{applicationId} ORDER BY volunteer_order ASC")
    List<VolunteerDetail> selectByApplicationId(@Param("applicationId") Long applicationId);
}
