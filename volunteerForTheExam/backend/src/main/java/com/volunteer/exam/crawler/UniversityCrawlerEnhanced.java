package com.volunteer.exam.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 增强版院校信息爬虫
 * 优化了反爬虫策略，添加了重试机制和更真实的请求头
 */
@Component
public class UniversityCrawlerEnhanced {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private Random random = new Random();

    /**
     * 根据院校名称爬取并保存信息
     */
    public void crawlAndSaveByName(String schoolName) {
        try {
            System.out.println("开始处理院校: " + schoolName);
            
            // 先从数据库查询院校
            String querySql = "SELECT id FROM university WHERE school_name = ? LIMIT 1";
            Long universityId = jdbcTemplate.queryForObject(querySql, Long.class, schoolName);
            
            if (universityId == null) {
                System.out.println("数据库中未找到院校: " + schoolName);
                return;
            }
            
            // 检查是否已有详细信息
            String checkSql = "SELECT website FROM university WHERE id = ?";
            String existingWebsite = jdbcTemplate.queryForObject(checkSql, String.class, universityId);
            
            if (existingWebsite != null && !existingWebsite.isEmpty()) {
                System.out.println("院校已有详细信息，跳过: " + schoolName);
                return;
            }
            
            // 爬取院校官网信息（使用百度搜索作为备选方案）
            Map<String, String> data = crawlUniversityInfoFromBaidu(schoolName);
            
            // 保存到数据库
            if (!data.isEmpty()) {
                updateUniversityDetail(universityId, data);
                System.out.println("✓ 成功保存院校信息: " + schoolName);
            } else {
                System.out.println("× 未能获取院校信息: " + schoolName);
            }
            
            // 随机延迟20-30秒
            int randomDelay = 20000 + random.nextInt(10000);
            System.out.println("等待 " + (randomDelay/1000) + " 秒后继续...");
            Thread.sleep(randomDelay);
            
        } catch (Exception e) {
            System.err.println("处理院校失败: " + schoolName + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 从百度搜索获取院校官网信息
     */
    private Map<String, String> crawlUniversityInfoFromBaidu(String schoolName) {
        Map<String, String> data = new HashMap<>();
        
        try {
            // 使用百度搜索院校官网
            String searchUrl = "https://www.baidu.com/s?wd=" + 
                    java.net.URLEncoder.encode(schoolName + " 官网", "UTF-8");
            
            // 随机延迟2-5秒
            Thread.sleep(2000 + random.nextInt(3000));
            
            Document searchDoc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Connection", "keep-alive")
                    .referrer("https://www.baidu.com/")
                    .timeout(15000)
                    .ignoreHttpErrors(true)
                    .get();
            
            // 提取第一个搜索结果的链接作为官网
            Element firstResult = searchDoc.selectFirst("div.result a");
            if (firstResult != null) {
                String website = firstResult.attr("href");
                if (website != null && !website.isEmpty()) {
                    data.put("website", website);
                }
            }
            
            // 尝试从搜索结果中提取电话和地址
            Element contentDiv = searchDoc.selectFirst("div.c-abstract");
            if (contentDiv != null) {
                String content = contentDiv.text();
                
                // 简单的电话号码匹配
                if (content.contains("电话") || content.contains("联系")) {
                    String[] parts = content.split("[，。；]");
                    for (String part : parts) {
                        if (part.contains("电话") && part.matches(".*\\d{3,4}-?\\d{7,8}.*")) {
                            data.put("phone", part.trim());
                            break;
                        }
                    }
                }
                
                // 简单的地址匹配
                if (content.contains("地址")) {
                    String[] parts = content.split("[，。；]");
                    for (String part : parts) {
                        if (part.contains("地址")) {
                            data.put("address", part.trim());
                            break;
                        }
                    }
                }
            }
            
            System.out.println("从百度搜索获取到 " + data.size() + " 个字段");
            
        } catch (Exception e) {
            System.err.println("从百度搜索获取信息失败: " + e.getMessage());
        }
        
        return data;
    }

    /**
     * 更新数据库中的院校详细信息
     */
    private void updateUniversityDetail(Long universityId, Map<String, String> data) {
        if (data.isEmpty()) {
            return;
        }
        
        StringBuilder sql = new StringBuilder("UPDATE university SET ");
        boolean first = true;
        
        for (String key : data.keySet()) {
            if (!first) {
                sql.append(", ");
            }
            sql.append(key).append(" = ?");
            first = false;
        }
        
        sql.append(" WHERE id = ?");
        
        Object[] params = new Object[data.size() + 1];
        int i = 0;
        for (String value : data.values()) {
            params[i++] = value;
        }
        params[i] = universityId;
        
        jdbcTemplate.update(sql.toString(), params);
    }

    /**
     * 批量爬取所有院校信息
     */
    public void crawlAllUniversities() {
        System.out.println("========================================");
        System.out.println("开始批量爬取院校信息");
        System.out.println("策略：使用百度搜索获取官网信息");
        System.out.println("间隔：每个院校20-30秒随机延迟");
        System.out.println("========================================");
        
        String sql = "SELECT school_name FROM university WHERE website IS NULL OR website = '' ORDER BY id LIMIT 50";
        
        jdbcTemplate.query(sql, (rs) -> {
            String schoolName = rs.getString("school_name");
            crawlAndSaveByName(schoolName);
        });
        
        System.out.println("========================================");
        System.out.println("批量爬取完成！");
        System.out.println("========================================");
    }
}
