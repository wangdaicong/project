package com.volunteer.exam.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * 批量数据导入服务
 * 用于快速导入大量高校、专业数据
 */
@Service
public class BatchDataImportService {

    private static final Logger log = LoggerFactory.getLogger(BatchDataImportService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 从CSV批量导入高校数据
     * CSV格式：name,province,city,level,type,is_985,is_211,is_double_first_class,website,introduction
     */
    @Transactional
    public int importUniversitiesFromCSV(String filePath) {
        int count = 0;
        List<Map<String, Object>> batch = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            
            // 跳过表头
            br.readLine();
            
            while ((line = br.readLine()) != null) {
                try {
                    String[] data = parseCSVLine(line);
                    
                    if (data.length < 5) {
                        log.warn("数据格式错误，跳过: {}", line);
                        continue;
                    }
                    
                    Map<String, Object> university = new HashMap<>();
                    university.put("name", data[0].trim());
                    university.put("province", data[1].trim());
                    university.put("city", data.length > 2 ? data[2].trim() : "");
                    university.put("level", data.length > 3 ? data[3].trim() : "本科");
                    university.put("type", data.length > 4 ? data[4].trim() : "综合");
                    university.put("is_985", data.length > 5 ? parseInt(data[5]) : 0);
                    university.put("is_211", data.length > 6 ? parseInt(data[6]) : 0);
                    university.put("is_double_first_class", data.length > 7 ? parseInt(data[7]) : 0);
                    university.put("website", data.length > 8 ? data[8].trim() : "");
                    university.put("introduction", data.length > 9 ? data[9].trim() : "");
                    
                    batch.add(university);
                    
                    // 每100条批量插入一次
                    if (batch.size() >= 100) {
                        count += batchInsertUniversities(batch);
                        batch.clear();
                        log.info("已导入{}所高校", count);
                    }
                    
                } catch (Exception e) {
                    log.error("解析数据失败: {}", line, e);
                }
            }
            
            // 插入剩余数据
            if (!batch.isEmpty()) {
                count += batchInsertUniversities(batch);
            }
            
            log.info("CSV导入完成，共导入{}所高校", count);
            
        } catch (Exception e) {
            log.error("导入CSV失败: {}", e.getMessage(), e);
            throw new RuntimeException("导入失败", e);
        }
        
        return count;
    }

    /**
     * 从JSON批量导入专业数据
     */
    @Transactional
    public int importMajorsFromJSON(String filePath) {
        try {
            String json = Files.readString(Paths.get(filePath));
            List<Map<String, Object>> majors = objectMapper.readValue(json, 
                new TypeReference<List<Map<String, Object>>>() {});
            
            int count = batchInsertMajors(majors);
            
            log.info("JSON导入完成，共导入{}个专业", count);
            return count;
            
        } catch (Exception e) {
            log.error("导入JSON失败: {}", e.getMessage(), e);
            throw new RuntimeException("导入失败", e);
        }
    }

    /**
     * 批量插入高校数据
     */
    private int batchInsertUniversities(List<Map<String, Object>> universities) {
        String sql = "INSERT INTO university (name, province, city, level, type, is_985, is_211, " +
                    "is_double_first_class, website, introduction, created_time, updated_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "city = VALUES(city), level = VALUES(level), type = VALUES(type), " +
                    "is_985 = VALUES(is_985), is_211 = VALUES(is_211), " +
                    "is_double_first_class = VALUES(is_double_first_class), " +
                    "website = VALUES(website), introduction = VALUES(introduction), " +
                    "updated_time = NOW()";
        
        int[] results = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, Object> u = universities.get(i);
                ps.setString(1, (String) u.get("name"));
                ps.setString(2, (String) u.get("province"));
                ps.setString(3, (String) u.get("city"));
                ps.setString(4, (String) u.get("level"));
                ps.setString(5, (String) u.get("type"));
                ps.setInt(6, (Integer) u.get("is_985"));
                ps.setInt(7, (Integer) u.get("is_211"));
                ps.setInt(8, (Integer) u.get("is_double_first_class"));
                ps.setString(9, (String) u.get("website"));
                ps.setString(10, (String) u.get("introduction"));
            }
            
            @Override
            public int getBatchSize() {
                return universities.size();
            }
        });
        
        return results.length;
    }

    /**
     * 批量插入专业数据
     */
    private int batchInsertMajors(List<Map<String, Object>> majors) {
        String sql = "INSERT INTO major_info (name, code, category, degree, years, " +
                    "introduction, main_courses, employment_direction, created_time, updated_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "category = VALUES(category), degree = VALUES(degree), years = VALUES(years), " +
                    "introduction = VALUES(introduction), main_courses = VALUES(main_courses), " +
                    "employment_direction = VALUES(employment_direction), updated_time = NOW()";
        
        int[] results = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, Object> m = majors.get(i);
                ps.setString(1, (String) m.get("name"));
                ps.setString(2, (String) m.getOrDefault("code", ""));
                ps.setString(3, (String) m.getOrDefault("category", ""));
                ps.setString(4, (String) m.getOrDefault("degree", ""));
                ps.setInt(5, (Integer) m.getOrDefault("years", 4));
                ps.setString(6, (String) m.getOrDefault("introduction", ""));
                ps.setString(7, (String) m.getOrDefault("main_courses", ""));
                ps.setString(8, (String) m.getOrDefault("employment_direction", ""));
            }
            
            @Override
            public int getBatchSize() {
                return majors.size();
            }
        });
        
        return results.length;
    }

    /**
     * 解析CSV行（处理引号内的逗号）
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    /**
     * 安全解析整数
     */
    private int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 数据统计
     */
    public Map<String, Object> getDataStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 高校统计
            Integer universityCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM university", Integer.class);
            Integer university985Count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM university WHERE is_985 = 1", Integer.class);
            Integer university211Count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM university WHERE is_211 = 1", Integer.class);
            
            stats.put("universityTotal", universityCount);
            stats.put("university985", university985Count);
            stats.put("university211", university211Count);
            
            // 专业统计
            Integer majorCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM major_info", Integer.class);
            stats.put("majorTotal", majorCount);
            
            // 分数线统计
            Integer scoreLineCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM score_line", Integer.class);
            stats.put("scoreLineTotal", scoreLineCount);
            
            // 按省份统计
            List<Map<String, Object>> provinceStats = jdbcTemplate.queryForList(
                "SELECT province, COUNT(*) as count FROM university GROUP BY province ORDER BY count DESC LIMIT 10");
            stats.put("topProvinces", provinceStats);
            
        } catch (Exception e) {
            log.error("获取统计数据失败: {}", e.getMessage());
        }
        
        return stats;
    }

    /**
     * 数据验证
     */
    public Map<String, Object> validateData() {
        Map<String, Object> result = new HashMap<>();
        List<String> issues = new ArrayList<>();
        
        try {
            // 检查重复数据
            List<Map<String, Object>> duplicates = jdbcTemplate.queryForList(
                "SELECT name, province, COUNT(*) as count FROM university " +
                "GROUP BY name, province HAVING count > 1");
            
            if (!duplicates.isEmpty()) {
                issues.add("发现" + duplicates.size() + "组重复数据");
                result.put("duplicates", duplicates);
            }
            
            // 检查缺失字段
            Integer missingProvince = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM university WHERE province IS NULL OR province = ''", 
                Integer.class);
            
            if (missingProvince > 0) {
                issues.add(missingProvince + "所高校缺少省份信息");
            }
            
            result.put("valid", issues.isEmpty());
            result.put("issues", issues);
            
        } catch (Exception e) {
            log.error("数据验证失败: {}", e.getMessage());
            result.put("valid", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
