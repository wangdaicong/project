package com.volunteer.exam.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 阳光高考网院校信息爬虫 V2
 * 直接从院校列表页面爬取数据
 */
@Component
public class UniversityCrawlerV2 {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private Random random = new Random();

    /**
     * 从阳光高考网院校列表页面爬取所有院校信息
     */
    public void crawlUniversityList() {
        System.out.println("========================================");
        System.out.println("开始从阳光高考网爬取院校列表");
        System.out.println("URL: https://gaokao.chsi.com.cn/sch/search.do");
        System.out.println("========================================");
        
        try {
            int pageNum = 1;
            int totalProcessed = 0;
            int maxPages = 100; // 最多爬取100页
            
            while (pageNum <= maxPages) {
                System.out.println("\n正在爬取第 " + pageNum + " 页...");
                
                // 构建URL
                String url = "https://gaokao.chsi.com.cn/sch/search.do?searchType=1&page=" + pageNum;
                
                // 随机延迟3-6秒
                int delay = 3000 + random.nextInt(3000);
                System.out.println("等待 " + (delay/1000) + " 秒...");
                Thread.sleep(delay);
                
                // 发送请求
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Connection", "keep-alive")
                        .header("Upgrade-Insecure-Requests", "1")
                        .referrer("https://gaokao.chsi.com.cn/")
                        .timeout(20000)
                        .ignoreHttpErrors(true)
                        .get();
                
                // 检查是否成功获取页面
                if (doc == null) {
                    System.err.println("× 获取第 " + pageNum + " 页失败");
                    break;
                }
                
                // 解析院校列表
                Elements schoolItems = doc.select("div.yxk-result-box");
                
                if (schoolItems.isEmpty()) {
                    System.out.println("第 " + pageNum + " 页没有更多数据，爬取完成");
                    break;
                }
                
                System.out.println("找到 " + schoolItems.size() + " 所院校");
                
                // 处理每所院校
                for (Element item : schoolItems) {
                    try {
                        Map<String, String> data = parseSchoolItem(item);
                        
                        if (!data.isEmpty()) {
                            String schoolName = data.get("school_name");
                            
                            // 检查数据库中是否存在该院校
                            String checkSql = "SELECT id FROM university WHERE school_name = ? LIMIT 1";
                            Long universityId = null;
                            
                            try {
                                universityId = jdbcTemplate.queryForObject(checkSql, Long.class, schoolName);
                            } catch (Exception e) {
                                // 院校不存在，跳过
                                System.out.println("  - 跳过（数据库中不存在）: " + schoolName);
                                continue;
                            }
                            
                            // 检查是否已有详细信息
                            String websiteCheckSql = "SELECT website FROM university WHERE id = ?";
                            String existingWebsite = jdbcTemplate.queryForObject(websiteCheckSql, String.class, universityId);
                            
                            if (existingWebsite != null && !existingWebsite.isEmpty() && !existingWebsite.contains("baidu")) {
                                System.out.println("  - 跳过（已有数据）: " + schoolName);
                                continue;
                            }
                            
                            // 保存数据
                            updateUniversityDetail(universityId, data);
                            totalProcessed++;
                            System.out.println("  ✓ 成功保存: " + schoolName + " (官网: " + data.getOrDefault("website", "无") + ")");
                        }
                        
                    } catch (Exception e) {
                        System.err.println("  × 处理院校失败: " + e.getMessage());
                    }
                }
                
                pageNum++;
                
                // 每处理10页休息一下
                if (pageNum % 10 == 0) {
                    System.out.println("\n已处理 " + pageNum + " 页，休息30秒...");
                    Thread.sleep(30000);
                }
            }
            
            System.out.println("\n========================================");
            System.out.println("爬取完成！");
            System.out.println("共处理 " + totalProcessed + " 所院校");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("爬取过程出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 解析单个院校信息
     */
    private Map<String, String> parseSchoolItem(Element item) {
        Map<String, String> data = new HashMap<>();
        
        try {
            // 院校名称
            Element nameElement = item.selectFirst("a.name-box");
            if (nameElement != null) {
                String schoolName = nameElement.text().trim();
                data.put("school_name", schoolName);
            }
            
            // 院校详情页链接（可以从中提取更多信息）
            Element linkElement = item.selectFirst("a.name-box");
            if (linkElement != null) {
                String href = linkElement.attr("href");
                // 从详情页链接中提取学校ID，后续可以用来爬取详细信息
                if (href.contains("schId-")) {
                    String schoolId = extractSchoolId(href);
                    data.put("school_id", schoolId);
                }
            }
            
            // 解析院校基本信息（所在地、办学性质等）
            Elements infoElements = item.select("p.info");
            for (Element info : infoElements) {
                String text = info.text();
                
                // 提取官网
                Element websiteLink = info.selectFirst("a[href]");
                if (websiteLink != null && text.contains("官网")) {
                    String website = websiteLink.attr("href");
                    if (website != null && !website.isEmpty()) {
                        data.put("website", website);
                    }
                }
                
                // 提取电话
                if (text.contains("电话") || text.contains("招生电话")) {
                    String phone = text.replaceAll(".*电话[：:：]\\s*", "").trim();
                    if (!phone.isEmpty()) {
                        data.put("phone", phone);
                    }
                }
                
                // 提取地址
                if (text.contains("地址")) {
                    String address = text.replaceAll(".*地址[：:：]\\s*", "").trim();
                    if (!address.isEmpty()) {
                        data.put("address", address);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("解析院校信息失败: " + e.getMessage());
        }
        
        return data;
    }

    /**
     * 从URL中提取学校ID
     */
    private String extractSchoolId(String url) {
        if (url == null || !url.contains("schId-")) {
            return null;
        }
        int start = url.indexOf("schId-") + 6;
        int end = url.indexOf(".", start);
        if (end == -1) {
            end = url.indexOf(",", start);
        }
        if (end == -1) {
            end = url.length();
        }
        return url.substring(start, end);
    }

    /**
     * 更新数据库中的院校详细信息
     */
    private void updateUniversityDetail(Long universityId, Map<String, String> data) {
        if (data.isEmpty()) {
            return;
        }
        
        // 移除school_name和school_id，这些不需要更新
        data.remove("school_name");
        data.remove("school_id");
        
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
}
