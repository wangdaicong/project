package com.volunteer.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.exam.common.Result;
import com.volunteer.exam.entity.*;
import com.volunteer.exam.service.UniversityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 院校查询控制器
 */
@RestController
@RequestMapping("/api/university")
@CrossOrigin
public class UniversityController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private UniversityService universityService;

    /**
     * 搜索院校
     */
    @GetMapping("/search")
    public Result search(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String supervisor,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String schoolNature,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean is985,
            @RequestParam(required = false) Boolean is211,
            @RequestParam(required = false) Boolean isDoubleFirstClass
    ) {
        try {
            // 构建SQL查询
            StringBuilder sql = new StringBuilder("SELECT * FROM university WHERE 1=1");
            List<Object> params = new ArrayList<>();

            // 关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append(" AND school_name LIKE ?");
                params.add("%" + keyword.trim() + "%");
            }

            // 省份筛选
            if (province != null && !province.trim().isEmpty()) {
                sql.append(" AND location = ?");
                params.add(province.trim());
            }

            // 主管部门筛选
            if (supervisor != null && !supervisor.trim().isEmpty()) {
                sql.append(" AND supervisor = ?");
                params.add(supervisor.trim());
            }

            // 办学层次筛选
            if (level != null && !level.trim().isEmpty()) {
                sql.append(" AND level LIKE ?");
                params.add("%" + level.trim() + "%");
            }

            // 办学性质筛选
            if (schoolNature != null && !schoolNature.trim().isEmpty()) {
                sql.append(" AND school_nature LIKE ?");
                params.add("%" + schoolNature.trim() + "%");
            }

            // 类型筛选
            if (type != null && !type.trim().isEmpty()) {
                sql.append(" AND type = ?");
                params.add(type.trim());
            }

            // 985筛选
            if (is985 != null && is985) {
                sql.append(" AND is_985 = 1");
            }

            // 211筛选
            if (is211 != null && is211) {
                sql.append(" AND is_211 = 1");
            }

            // 双一流筛选
            if (isDoubleFirstClass != null && isDoubleFirstClass) {
                sql.append(" AND is_double_first_class = 1");
            }

            // 查询总数
            String countSql = "SELECT COUNT(*) FROM (" + sql.toString() + ") t";
            Integer total = jdbcTemplate.queryForObject(countSql, params.toArray(), Integer.class);

            // 分页
            sql.append(" ORDER BY id LIMIT ? OFFSET ?");
            params.add(size);
            params.add((page - 1) * size);

            // 查询数据
            List<Map<String, Object>> universities = jdbcTemplate.queryForList(sql.toString(), params.toArray());

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("data", universities);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (int) Math.ceil((double) total / size));
            result.put("success", true);

            return Result.success(result);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取院校详情
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        try {
            String sql = "SELECT * FROM university WHERE id = ?";
            Map<String, Object> university = jdbcTemplate.queryForMap(sql, id);
            return Result.success(university);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 推荐院校
     */
    @GetMapping("/recommend")
    public Result recommend(
            @RequestParam Integer score,
            @RequestParam(required = false) String province
    ) {
        try {
            String sql = "SELECT * FROM university WHERE 1=1";
            List<Object> params = new ArrayList<>();

            if (province != null && !province.trim().isEmpty() && !"全国".equals(province)) {
                sql += " AND province = ?";
                params.add(province);
            }

            sql += " ORDER BY RAND() LIMIT 20";

            List<Map<String, Object>> universities = jdbcTemplate.queryForList(sql, params.toArray());
            return Result.success(universities);

        } catch (Exception e) {
            return Result.error("推荐失败: " + e.getMessage());
        }
    }
    
    /**
     * 院校列表（使用Service）
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        try {
            Page<University> result = universityService.getUniversityList(keyword, province, type, level, page, size);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", result.getRecords());
            data.put("total", result.getTotal());
            data.put("page", result.getCurrent());
            data.put("size", result.getSize());
            data.put("totalPages", result.getPages());
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 院校详情（使用Service）
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        try {
            University university = universityService.getUniversityDetail(id);
            if (university == null) {
                return Result.error("院校不存在");
            }
            return Result.success(university);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取院校开设专业列表
     */
    @GetMapping("/{id}/majors")
    public Result getMajors(
            @PathVariable Long id,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String degreeLevel
    ) {
        try {
            Map<String, Object> result = universityService.getUniversityMajors(id, category, degreeLevel);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取院校的张雪峰式分析
     */
    @GetMapping("/zhangxuefeng/{id}")
    public Result getZhangxuefengAnalysis(@PathVariable Long id) {
        try {
            String sql = "SELECT historical_affiliation, industry_recognition, " +
                        "has_doctoral_program, has_master_program, " +
                        "employment_advantage, postgraduate_difficulty " +
                        "FROM university WHERE id = ?";
            
            Map<String, Object> analysis = jdbcTemplate.queryForMap(sql, id);
            
            return Result.success(analysis);
        } catch (Exception e) {
            return Result.error("获取张雪峰分析失败: " + e.getMessage());
        }
    }
}
