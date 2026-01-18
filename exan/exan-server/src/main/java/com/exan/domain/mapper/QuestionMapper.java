package com.exan.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exan.domain.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
