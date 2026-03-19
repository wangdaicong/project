package com.volunteer.exam.controller;

import com.volunteer.exam.service.DataCrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据管理控制器
 * 提供手动触发数据更新的接口
 */
@Slf4j
@RestController
@RequestMapping("/api/data-manage")
@CrossOrigin
public class DataManageController {

    @Autowired
    private DataCrawlerService crawlerService;
    
    /**
     * 立即执行完整数据更新
     */
    @PostMapping("/update/full")
    public Map<String, Object> updateFull() {
        log.info("手动触发：完整数据更新");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            crawlerService.runFullUpdate();
            result.put("success", true);
            result.put("message", "完整数据更新已启动，请查看日志了解进度");
        } catch (Exception e) {
            log.error("完整数据更新失败", e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 立即更新院校排名
     */
    @PostMapping("/update/rankings")
    public Map<String, Object> updateRankings() {
        log.info("手动触发：更新院校排名");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            crawlerService.updateRankings();
            result.put("success", true);
            result.put("message", "院校排名更新已启动");
        } catch (Exception e) {
            log.error("院校排名更新失败", e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 立即更新录取分数线
     */
    @PostMapping("/update/scores")
    public Map<String, Object> updateScores() {
        log.info("手动触发：更新录取分数线");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            crawlerService.updateAdmissionScores();
            result.put("success", true);
            result.put("message", "录取分数线更新已启动");
        } catch (Exception e) {
            log.error("录取分数线更新失败", e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 验证数据质量
     */
    @GetMapping("/verify")
    public Map<String, Object> verifyData() {
        log.info("手动触发：数据质量验证");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            String verifyResult = crawlerService.verifyData();
            result.put("success", true);
            result.put("message", "数据验证完成");
            result.put("data", verifyResult);
        } catch (Exception e) {
            log.error("数据验证失败", e);
            result.put("success", false);
            result.put("message", "验证失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取定时任务配置
     */
    @GetMapping("/schedule/config")
    public Map<String, Object> getScheduleConfig() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("rankings", Map.of(
            "cron", "0 0 2 15 4 ?",
            "description", "每年4月15日凌晨2点更新院校排名"
        ));
        
        config.put("scores", Map.of(
            "cron", "0 0 3 20 7 ?",
            "description", "每年7月20日凌晨3点更新录取分数线"
        ));
        
        config.put("verify", Map.of(
            "cron", "0 0 1 * * ?",
            "description", "每天凌晨1点进行数据质量检查"
        ));
        
        config.put("fullUpdate", Map.of(
            "cron", "0 0 4 ? * MON",
            "description", "每周一凌晨4点执行完整更新（仅4月、7-8月）"
        ));
        
        return config;
    }
}
