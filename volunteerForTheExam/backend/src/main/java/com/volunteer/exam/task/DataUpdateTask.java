package com.volunteer.exam.task;

import com.volunteer.exam.service.DataCrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 数据自动更新定时任务
 * 每年自动更新排名和分数线
 */
@Slf4j
@Component
public class DataUpdateTask {

    @Autowired
    private DataCrawlerService crawlerService;
    
    /**
     * 每年4月15日凌晨2点更新院校排名
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 15 4 ?")
    public void updateRankingsScheduled() {
        log.info("========================================");
        log.info("定时任务：更新院校排名");
        log.info("========================================");
        
        crawlerService.updateRankings();
    }
    
    /**
     * 每年7月20日凌晨3点开始更新分数线
     */
    @Scheduled(cron = "0 0 3 20 7 ?")
    public void updateScoresScheduled() {
        log.info("========================================");
        log.info("定时任务：更新录取分数线");
        log.info("========================================");
        
        crawlerService.updateAdmissionScores();
    }
    
    /**
     * 每天凌晨1点进行数据质量检查
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void verifyDataScheduled() {
        log.info("========================================");
        log.info("定时任务：数据质量检查");
        log.info("========================================");
        
        String result = crawlerService.verifyData();
        log.info("验证结果:\n{}", result);
    }
    
    /**
     * 每周一凌晨4点执行完整更新（可选）
     */
    @Scheduled(cron = "0 0 4 ? * MON")
    public void fullUpdateScheduled() {
        LocalDate now = LocalDate.now();
        
        // 只在4月或7-8月执行完整更新
        int month = now.getMonthValue();
        if (month == 4 || month == 7 || month == 8) {
            log.info("========================================");
            log.info("定时任务：完整数据更新");
            log.info("========================================");
            
            crawlerService.runFullUpdate();
        }
    }
}
