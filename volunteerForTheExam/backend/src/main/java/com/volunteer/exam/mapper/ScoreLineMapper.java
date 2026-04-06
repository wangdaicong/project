package com.volunteer.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.exam.entity.ScoreLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 历年分数线Mapper
 */
@Mapper
public interface ScoreLineMapper extends BaseMapper<ScoreLine> {
    
    /**
     * 查询某院校的历年分数线
     */
    @Select("SELECT * FROM score_line WHERE university_id = #{universityId} " +
            "AND province = #{province} " +
            "AND category = #{category} " +
            "ORDER BY year DESC")
    List<ScoreLine> selectByUniversityAndProvince(
            @Param("universityId") Long universityId,
            @Param("province") String province,
            @Param("category") String category
    );
    
    /**
     * 查询某省份某年份的所有院校分数线
     */
    @Select("SELECT * FROM score_line WHERE province = #{province} " +
            "AND year = #{year} " +
            "AND category = #{category} " +
            "ORDER BY min_score DESC")
    List<ScoreLine> selectByProvinceAndYear(
            @Param("province") String province,
            @Param("year") Integer year,
            @Param("category") String category
    );
    
    /**
     * 获取可用的年份列表
     */
    @Select("SELECT DISTINCT year FROM score_line ORDER BY year DESC")
    List<Integer> selectAvailableYears();
}
