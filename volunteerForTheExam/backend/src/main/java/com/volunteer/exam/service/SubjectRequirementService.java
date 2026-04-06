package com.volunteer.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.volunteer.exam.entity.SubjectRequirement;
import com.volunteer.exam.mapper.SubjectRequirementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 选科要求服务类
 */
@Service
public class SubjectRequirementService {

    @Autowired
    private SubjectRequirementMapper subjectRequirementMapper;

    /**
     * 根据选科组合查询可报考的专业和院校
     */
    public List<SubjectRequirement> queryBySubjects(String subjects, String province, 
                                                     Integer year, String degreeLevel, String universityType) {
        String[] subjectArray = subjects.split(",");
        
        // 获取所有专业
        QueryWrapper<SubjectRequirement> wrapper = new QueryWrapper<>();
        wrapper.eq("province", province)
               .eq("year", year);
        
        if (degreeLevel != null && !degreeLevel.isEmpty()) {
            wrapper.eq("degree_level", degreeLevel);
        }
        
        List<SubjectRequirement> allMajors = subjectRequirementMapper.selectList(wrapper);
        
        // 判断每个专业是否可报考
        for (SubjectRequirement major : allMajors) {
            boolean canApply = checkCanApply(major, subjectArray);
            major.setCanApply(canApply ? 1 : 0);
        }
        
        return allMajors;
    }
    
    /**
     * 判断选科组合是否满足专业要求
     */
    private boolean checkCanApply(SubjectRequirement major, String[] selectedSubjects) {
        String requirement = major.getSubjectRequirement();
        
        // 不限选科
        if (requirement == null || requirement.isEmpty() || "不限".equals(requirement)) {
            return true;
        }
        
        // 解析要求的科目
        String[] requiredSubjects = requirement.split("\\+");
        
        // 检查是否包含所有必选科目
        for (String required : requiredSubjects) {
            boolean found = false;
            for (String selected : selectedSubjects) {
                if (selected.trim().equals(required.trim())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 根据专业名称查询选科要求
     */
    public List<SubjectRequirement> queryByMajor(String majorName, String province, Integer year) {
        QueryWrapper<SubjectRequirement> wrapper = new QueryWrapper<>();
        wrapper.eq("province", province)
               .eq("year", year)
               .like("major_name", majorName);
        return subjectRequirementMapper.selectList(wrapper);
    }

    /**
     * 根据院校名称查询选科要求
     */
    public List<SubjectRequirement> queryByUniversity(String universityName, String province, Integer year) {
        QueryWrapper<SubjectRequirement> wrapper = new QueryWrapper<>();
        wrapper.eq("province", province)
               .eq("year", year)
               .like("university_name", universityName);
        return subjectRequirementMapper.selectList(wrapper);
    }

    /**
     * 获取所有专业门类
     */
    public List<String> getMajorCategories() {
        return subjectRequirementMapper.getMajorCategories();
    }

    /**
     * 获取指定门类下的专业列表
     */
    public List<String> getMajorsByCategory(String category) {
        return subjectRequirementMapper.getMajorsByCategory(category);
    }
}
