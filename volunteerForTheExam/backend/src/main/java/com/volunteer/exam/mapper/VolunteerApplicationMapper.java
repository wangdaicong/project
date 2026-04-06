package com.volunteer.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.exam.entity.VolunteerApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 志愿填报记录Mapper
 */
@Mapper
public interface VolunteerApplicationMapper extends BaseMapper<VolunteerApplication> {
}
