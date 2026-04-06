package com.volunteer.exam.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 图表数据控制器
 * 提供分数线趋势等图表数据
 */
@RestController
@RequestMapping("/api/chart")
public class ChartController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取院校历年分数线趋势
     */
    @GetMapping("/score-trend")
    public Map<String, Object> getScoreTrend(
            @RequestParam Long universityId,
            @RequestParam String province,
            @RequestParam(required = false) String category) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String sql = "SELECT year, category, min_score, avg_score, max_score, min_rank " +
                        "FROM score_line " +
                        "WHERE university_id = ? AND province = ? ";
            
            List<Object> params = new ArrayList<>();
            params.add(universityId);
            params.add(province);
            
            if (category != null && !category.isEmpty()) {
                sql += "AND category = ? ";
                params.add(category);
            }
            
            sql += "ORDER BY year ASC, category";
            
            List<Map<String, Object>> scoreData = jdbcTemplate.queryForList(sql, params.toArray());
            
            // 按科类分组
            Map<String, List<Map<String, Object>>> groupedData = new HashMap<>();
            for (Map<String, Object> item : scoreData) {
                String cat = (String) item.get("category");
                groupedData.computeIfAbsent(cat, k -> new ArrayList<>()).add(item);
            }
            
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", groupedData);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取趋势数据失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 获取专业就业率趋势
     */
    @GetMapping("/employment-trend")
    public Map<String, Object> getEmploymentTrend(@RequestParam Long majorId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String sql = "SELECT year, employment_rate, average_salary " +
                        "FROM employment_data " +
                        "WHERE major_id = ? " +
                        "ORDER BY year ASC";
            
            List<Map<String, Object>> employmentData = jdbcTemplate.queryForList(sql, majorId);
            
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", employmentData);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取就业趋势失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 获取院校分数线对比数据
     */
    @PostMapping("/score-compare")
    public Map<String, Object> getScoreCompare(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Integer> universityIds = (List<Integer>) request.get("universityIds");
            String province = (String) request.get("province");
            String category = (String) request.get("category");
            Integer year = (Integer) request.get("year");
            
            String placeholders = String.join(",", Collections.nCopies(universityIds.size(), "?"));
            String sql = "SELECT s.*, u.name as university_name " +
                        "FROM score_line s " +
                        "INNER JOIN university u ON s.university_id = u.id " +
                        "WHERE s.university_id IN (" + placeholders + ") " +
                        "AND s.province = ? AND s.category = ? AND s.year = ?";
            
            List<Object> params = new ArrayList<>(universityIds);
            params.add(province);
            params.add(category);
            params.add(year);
            
            List<Map<String, Object>> compareData = jdbcTemplate.queryForList(sql, params.toArray());
            
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", compareData);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取对比数据失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 获取热门专业统计
     */
    @GetMapping("/hot-majors")
    public Map<String, Object> getHotMajors() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String sql = "SELECT m.id, m.name, m.category, " +
                        "AVG(e.employment_rate) as avg_employment_rate, " +
                        "AVG(e.average_salary) as avg_salary " +
                        "FROM major_info m " +
                        "LEFT JOIN employment_data e ON m.id = e.major_id " +
                        "GROUP BY m.id, m.name, m.category " +
                        "ORDER BY avg_employment_rate DESC, avg_salary DESC " +
                        "LIMIT 10";
            
            List<Map<String, Object>> hotMajors = jdbcTemplate.queryForList(sql);
            
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", hotMajors);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取热门专业失败: " + e.getMessage());
        }
        
        return response;
    }
}
