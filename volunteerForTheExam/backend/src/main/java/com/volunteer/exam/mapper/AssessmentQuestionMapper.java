package com.volunteer.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.exam.entity.AssessmentQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专业测评问题Mapper
 */
@Mapper
public interface AssessmentQuestionMapper extends BaseMapper<AssessmentQuestion> {
}
