package com.volunteer.exam.controller;

import com.volunteer.exam.common.Result;
import com.volunteer.exam.crawler.UniversityCrawler;
import com.volunteer.exam.crawler.UniversityCrawlerEnhanced;
import com.volunteer.exam.crawler.UniversityCrawlerV2;
import com.volunteer.exam.crawler.UniversityCrawlerFinal;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 院校信息爬虫控制器
 */
@RestController
@RequestMapping("/api/crawler")
@CrossOrigin
public class UniversityCrawlerController {

    @Resource
    private UniversityCrawler universityCrawler;
    
    @Resource
    private UniversityCrawlerEnhanced universityCrawlerEnhanced;
    
    @Resource
    private UniversityCrawlerV2 universityCrawlerV2;
    
    @Resource
    private UniversityCrawlerFinal universityCrawlerFinal;

    /**
     * 爬取单个院校信息
     */
    @PostMapping("/university/{schoolName}")
    public Result crawlUniversity(@PathVariable String schoolName) {
        try {
            universityCrawler.crawlAndSaveByName(schoolName);
            return Result.success("爬取成功");
        } catch (Exception e) {
            return Result.error("爬取失败: " + e.getMessage());
        }
    }

    /**
     * 批量爬取所有院校信息（原版）
     */
    @PostMapping("/university/all")
    public Result crawlAllUniversities() {
        try {
            // 异步执行，避免请求超时
            new Thread(() -> {
                universityCrawler.crawlAllUniversities();
            }).start();
            return Result.success("已开始批量爬取，请稍后查看数据库");
        } catch (Exception e) {
            return Result.error("启动爬取失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量爬取所有院校信息（增强版 - 使用百度搜索）
     */
    @PostMapping("/university/enhanced")
    public Result crawlAllUniversitiesEnhanced() {
        try {
            // 异步执行，避免请求超时
            new Thread(() -> {
                universityCrawlerEnhanced.crawlAllUniversities();
            }).start();
            return Result.success("已开始增强版批量爬取（使用百度搜索），每次处理50所院校，请稍后查看数据库");
        } catch (Exception e) {
            return Result.error("启动爬取失败: " + e.getMessage());
        }
    }
    
    /**
     * 从阳光高考网院校列表页面批量爬取（V2版本）
     */
    @PostMapping("/university/v2")
    public Result crawlFromChsiList() {
        try {
            // 异步执行，避免请求超时
            new Thread(() -> {
                universityCrawlerV2.crawlUniversityList();
            }).start();
            return Result.success("已开始从阳光高考网院校列表爬取，请查看后端日志了解进度");
        } catch (Exception e) {
            return Result.error("启动爬取失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试爬取单个院校详情页
     */
    @GetMapping("/test/{schoolId}")
    public Result testCrawlSchoolDetail(@PathVariable String schoolId) {
        try {
            String url = "https://gaokao.chsi.com.cn/sch/schoolInfoMain--schId-" + schoolId + ".dhtml";
            
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .referrer("https://gaokao.chsi.com.cn/")
                    .timeout(20000)
                    .ignoreHttpErrors(true)
                    .get();
            
            String title = doc.title();
            String bodyText = doc.body() != null ? doc.body().text() : "";
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("url", url);
            result.put("title", title);
            result.put("bodyText", bodyText.substring(0, Math.min(500, bodyText.length())));
            result.put("success", !bodyText.contains("访问过于频繁") && !bodyText.contains("验证码"));
            result.put("message", bodyText.contains("访问过于频繁") ? "遇到反爬虫限制" : 
                                 bodyText.contains("验证码") ? "需要验证码" : "成功获取页面");
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 最终版批量爬取（遍历院校ID，通过名称匹配数据库）
     */
    @PostMapping("/university/final")
    public Result crawlFinal() {
        try {
            // 异步执行，避免请求超时
            new Thread(() -> {
                universityCrawlerFinal.crawlAllUniversities();
            }).start();
            return Result.success("已开始最终版批量爬取，遍历3000个院校ID，通过名称匹配数据库，请查看后端日志了解进度");
        } catch (Exception e) {
            return Result.error("启动爬取失败: " + e.getMessage());
        }
    }
}
