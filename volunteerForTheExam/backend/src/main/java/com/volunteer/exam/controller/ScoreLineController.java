package com.volunteer.exam.controller;

import com.volunteer.exam.common.Result;
import com.volunteer.exam.entity.ScoreLine;
import com.volunteer.exam.service.ScoreLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 历年分数线控制器
 */
@RestController
@RequestMapping("/api/score-line")
public class ScoreLineController {
    
    @Autowired
    private ScoreLineService scoreLineService;
    
    /**
     * 获取某院校的历年分数线趋势
     * 
     * @param universityId 院校ID
     * @param province 省份
     * @param category 科类（理科/文科）
     * @return 分数线趋势数据
     */
    @GetMapping("/trend/{universityId}")
    public Result<Map<String, Object>> getScoreTrend(
            @PathVariable Long universityId,
            @RequestParam String province,
            @RequestParam(defaultValue = "理科") String category) {
        
        Map<String, Object> trend = scoreLineService.getUniversityScoreTrend(universityId, province, category);
        
        if (trend.isEmpty()) {
            return Result.error("暂无该院校的分数线数据");
        }
        
        return Result.success("查询成功", trend);
    }
    
    /**
     * 获取某省份某年份的分数线排名
     * 
     * @param province 省份
     * @param year 年份
     * @param category 科类
     * @return 分数线排名列表
     */
    @GetMapping("/ranking")
    public Result<List<ScoreLine>> getProvinceRanking(
            @RequestParam String province,
            @RequestParam Integer year,
            @RequestParam(defaultValue = "理科") String category) {
        
        List<ScoreLine> ranking = scoreLineService.getProvinceYearRanking(province, year, category);
        return Result.success("查询成功", ranking);
    }
    
    /**
     * 获取可用的年份列表
     * 
     * @return 年份列表
     */
    @GetMapping("/years")
    public Result<List<Integer>> getAvailableYears() {
        List<Integer> years = scoreLineService.getAvailableYears();
        return Result.success("查询成功", years);
    }
    
    /**
     * 对比多个院校的分数线趋势
     * 
     * @param universityIds 院校ID列表（逗号分隔）
     * @param province 省份
     * @param category 科类
     * @return 对比数据
     */
    @GetMapping("/compare")
    public Result<Map<String, Object>> compareUniversities(
            @RequestParam String universityIds,
            @RequestParam String province,
            @RequestParam(defaultValue = "理科") String category) {
        
        // 解析院校ID列表
        String[] ids = universityIds.split(",");
        List<Long> idList = new java.util.ArrayList<>();
        for (String id : ids) {
            try {
                idList.add(Long.parseLong(id.trim()));
            } catch (NumberFormatException e) {
                return Result.error("院校ID格式错误");
            }
        }
        
        if (idList.isEmpty() || idList.size() > 5) {
            return Result.error("请选择1-5所院校进行对比");
        }
        
        Map<String, Object> compareData = scoreLineService.compareUniversities(idList, province, category);
        return Result.success("查询成功", compareData);
    }
}
