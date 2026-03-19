package com.volunteer.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.volunteer.exam.common.Result;
import com.volunteer.exam.entity.University;
import com.volunteer.exam.service.UniversityService;
import com.volunteer.exam.service.RecommendService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/university")
@CrossOrigin
public class UniversityController {
    
    @Resource
    private UniversityService universityService;
    
    @Resource
    private RecommendService recommendService;

    @GetMapping("/list")
    public Result<IPage<University>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer maxScore,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String type) {
        
        IPage<University> page = universityService.queryUniversities(
                pageNum, pageSize, province, minScore, maxScore, level, type);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<University> getById(@PathVariable Long id) {
        University university = universityService.getById(id);
        return Result.success(university);
    }

    /**
     * 智能推荐 - 专业级推荐
     */
    @GetMapping("/recommend/majors")
    public Result recommendMajors(@RequestParam Integer score, @RequestParam(required = false) String province) {
        if (score == null || score < 0 || score > 750) {
            return Result.error("分数必须在0-750之间");
        }
        if (province == null || province.isEmpty()) {
            province = "全国";
        }
        return Result.success(recommendService.recommendMajors(score, province));
    }

    /**
     * 智能推荐 - 院校级推荐（兼容旧接口）
     */
    @GetMapping("/recommend")
    public Result recommend(@RequestParam Integer score, @RequestParam(required = false) String province) {
        if (score == null || score < 0 || score > 750) {
            return Result.error("分数必须在0-750之间");
        }
        if (province == null || province.isEmpty()) {
            province = "全国";
        }
        return Result.success(recommendService.recommendUniversities(score, province));
    }

    /**
     * 获取分类热门院校（首页展示）
     * 返回985前3、211前3、专科前3，共9所
     */
    @GetMapping("/hot")
    public Result getHotUniversities() {
        return Result.success(universityService.getHotUniversitiesByCategory());
    }
}
