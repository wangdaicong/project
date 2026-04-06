package com.volunteer.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.exam.entity.SubjectRequirement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 选科要求Mapper
 */
@Mapper
public interface SubjectRequirementMapper extends BaseMapper<SubjectRequirement> {
    
    /**
     * 根据选科组合查询
     */
    @Select("<script>" +
            "SELECT * FROM major_subject_requirement " +
            "WHERE province = #{province} AND year = #{year} " +
            "<if test='degreeLevel != null'> AND degree_level = #{degreeLevel} </if>" +
            "AND (" +
            "  subject_requirement LIKE CONCAT('%', #{subject1}, '%') " +
            "  OR subject_requirement LIKE CONCAT('%', #{subject2}, '%') " +
            "  OR subject_requirement LIKE CONCAT('%', #{subject3}, '%') " +
            "  OR subject_requirement = '不限' " +
            ")" +
            "</script>")
    List<SubjectRequirement> queryBySubjects(
            @Param("province") String province,
            @Param("year") Integer year,
            @Param("degreeLevel") String degreeLevel,
            @Param("subject1") String subject1,
            @Param("subject2") String subject2,
            @Param("subject3") String subject3);
    
    /**
     * 获取所有专业门类
     */
    @Select("SELECT DISTINCT major_category FROM major_subject_requirement WHERE major_category IS NOT NULL ORDER BY major_category")
    List<String> getMajorCategories();
    
    /**
     * 获取指定门类下的专业
     */
    @Select("SELECT DISTINCT major_name FROM major_subject_requirement WHERE major_category = #{category} ORDER BY major_name")
    List<String> getMajorsByCategory(@Param("category") String category);
}
