package com.volunteer.exam.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * 数据爬虫服务
 * 调用Python爬虫脚本自动更新数据
 */
@Slf4j
@Service
public class DataCrawlerService {

    private static final String CRAWLER_DIR = "E:\\AiProject\\project\\volunteerForTheExam\\data-crawler";
    
    /**
     * 执行完整数据更新
     */
    public void runFullUpdate() {
        log.info("========================================");
        log.info("开始执行完整数据更新");
        log.info("========================================");
        
        try {
            // 执行阳光高考爬虫
            executePythonScript("gaokao_chsi_crawler.py");
            
            log.info("完整数据更新成功");
        } catch (Exception e) {
            log.error("完整数据更新失败", e);
        }
    }
    
    /**
     * 更新院校排名
     */
    public void updateRankings() {
        log.info("开始更新院校排名");
        
        try {
            executePythonScript("auto_crawl_rankings.py");
            log.info("院校排名更新成功");
        } catch (Exception e) {
            log.error("院校排名更新失败", e);
            // 使用备用方案
            try {
                executePythonScript("update_rankings_from_excel.py");
                log.info("使用备用排名数据更新成功");
            } catch (Exception ex) {
                log.error("备用方案也失败", ex);
            }
        }
    }
    
    /**
     * 更新录取分数线
     */
    public void updateAdmissionScores() {
        log.info("开始更新录取分数线");
        
        try {
            executePythonScript("auto_crawl_scores.py");
            log.info("录取分数线更新成功");
        } catch (Exception e) {
            log.error("录取分数线更新失败", e);
        }
    }
    
    /**
     * 验证数据质量
     */
    public String verifyData() {
        log.info("开始验证数据质量");
        
        try {
            String result = executePythonScript("verify_data.py");
            log.info("数据验证完成");
            return result;
        } catch (Exception e) {
            log.error("数据验证失败", e);
            return "验证失败: " + e.getMessage();
        }
    }
    
    /**
     * 执行Python脚本
     */
    private String executePythonScript(String scriptName) throws Exception {
        File scriptFile = new File(CRAWLER_DIR, scriptName);
        
        if (!scriptFile.exists()) {
            throw new RuntimeException("脚本文件不存在: " + scriptFile.getAbsolutePath());
        }
        
        ProcessBuilder pb = new ProcessBuilder("python", scriptFile.getAbsolutePath());
        pb.directory(new File(CRAWLER_DIR));
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[Python] {}", line);
                output.append(line).append("\n");
            }
        }
        
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new RuntimeException("脚本执行失败，退出码: " + exitCode);
        }
        
        return output.toString();
    }
}
