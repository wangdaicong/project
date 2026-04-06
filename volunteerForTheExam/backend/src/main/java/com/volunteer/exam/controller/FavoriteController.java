package com.volunteer.exam.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 收藏功能控制器
 */
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public Map<String, Object> addFavorite(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            Long universityId = Long.parseLong(request.get("universityId").toString());
            
            // 检查是否已收藏
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_favorite WHERE user_id = ? AND university_id = ?",
                Integer.class, userId, universityId
            );
            
            if (count > 0) {
                response.put("code", 400);
                response.put("message", "已经收藏过该院校");
                return response;
            }
            
            // 添加收藏
            jdbcTemplate.update(
                "INSERT INTO user_favorite (user_id, university_id) VALUES (?, ?)",
                userId, universityId
            );
            
            response.put("code", 200);
            response.put("message", "收藏成功");
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "收藏失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 取消收藏
     */
    @PostMapping("/remove")
    public Map<String, Object> removeFavorite(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            Long universityId = Long.parseLong(request.get("universityId").toString());
            
            jdbcTemplate.update(
                "DELETE FROM user_favorite WHERE user_id = ? AND university_id = ?",
                userId, universityId
            );
            
            response.put("code", 200);
            response.put("message", "取消收藏成功");
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "取消收藏失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 获取收藏列表
     */
    @GetMapping("/list")
    public Map<String, Object> getFavoriteList(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String sql = "SELECT u.* FROM university u " +
                        "INNER JOIN user_favorite f ON u.id = f.university_id " +
                        "WHERE f.user_id = ? ORDER BY f.created_time DESC";
            
            List<Map<String, Object>> favorites = jdbcTemplate.queryForList(sql, userId);
            
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", favorites);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取收藏列表失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Map<String, Object> checkFavorite(@RequestParam Long userId, @RequestParam Long universityId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_favorite WHERE user_id = ? AND university_id = ?",
                Integer.class, userId, universityId
            );
            
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", Map.of("isFavorite", count > 0));
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "检查失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 院校对比
     */
    @PostMapping("/compare")
    public Map<String, Object> compareUniversities(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Integer> universityIds = (List<Integer>) request.get("universityIds");
            
            if (universityIds == null || universityIds.size() < 2 || universityIds.size() > 4) {
                response.put("code", 400);
                response.put("message", "请选择2-4所院校进行对比");
                return response;
            }
            
            // 查询院校基本信息
            String placeholders = String.join(",", Collections.nCopies(universityIds.size(), "?"));
            String sql = "SELECT * FROM university WHERE id IN (" + placeholders + ")";
            List<Map<String, Object>> universities = jdbcTemplate.queryForList(sql, universityIds.toArray());
            
            // 查询历年分数线
            String scoreSql = "SELECT * FROM score_line WHERE university_id IN (" + placeholders + ") " +
                            "ORDER BY university_id, year DESC";
            List<Map<String, Object>> scoreLines = jdbcTemplate.queryForList(scoreSql, universityIds.toArray());
            
            // 组装对比数据
            Map<String, Object> compareData = new HashMap<>();
            compareData.put("universities", universities);
            compareData.put("scoreLines", scoreLines);
            
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", compareData);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "对比失败: " + e.getMessage());
        }
        
        return response;
    }
}
