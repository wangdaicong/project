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
 * 最终版院校信息爬虫
 * 通过遍历阳光高考网的院校ID，爬取详情页数据
 * 通过院校名称匹配数据库，确保数据一一对应
 */
@Component
public class UniversityCrawlerFinal {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private Random random = new Random();

    /**
     * 批量爬取院校信息
     * 从schId=1开始遍历，最多爬取3000个ID
     */
    public void crawlAllUniversities() {
        System.out.println("========================================");
        System.out.println("开始批量爬取阳光高考网院校信息");
        System.out.println("策略：遍历院校ID，通过名称匹配数据库");
        System.out.println("========================================\n");
        
        int successCount = 0;
        int skipCount = 0;
        int failCount = 0;
        int notFoundCount = 0;
        
        // 从ID 1开始遍历，最多3000个
        for (int schId = 1; schId <= 3000; schId++) {
            try {
                System.out.println("\n[" + schId + "/3000] 正在处理院校ID: " + schId);
                
                // 随机延迟20-30秒
                int delay = 20000 + random.nextInt(10000);
                System.out.println("等待 " + (delay/1000) + " 秒...");
                Thread.sleep(delay);
                
                // 爬取院校详情页
                Map<String, String> data = crawlSchoolDetail(schId);
                
                if (data.isEmpty()) {
                    System.out.println("× 未能获取到有效数据");
                    failCount++;
                    continue;
                }
                
                String schoolName = data.get("school_name");
                if (schoolName == null || schoolName.trim().isEmpty()) {
                    System.out.println("× 未找到院校名称");
                    failCount++;
                    continue;
                }
                
                System.out.println("获取到院校: " + schoolName);
                
                // 在数据库中查找匹配的院校
                String querySql = "SELECT id, website FROM university WHERE school_name = ? LIMIT 1";
                
                try {
                    Map<String, Object> dbRecord = jdbcTemplate.queryForMap(querySql, schoolName);
                    Long universityId = ((Number) dbRecord.get("id")).longValue();
                    String existingWebsite = (String) dbRecord.get("website");
                    
                    // 检查是否已有官网数据
                    if (existingWebsite != null && !existingWebsite.isEmpty() && 
                        !existingWebsite.contains("baidu")) {
                        System.out.println("- 跳过（已有数据）: " + schoolName);
                        skipCount++;
                        continue;
                    }
                    
                    // 更新数据库
                    data.remove("school_name"); // 移除院校名称，不更新
                    if (!data.isEmpty()) {
                        updateUniversityDetail(universityId, data);
                        successCount++;
                        System.out.println("✓ 成功更新: " + schoolName);
                        System.out.println("  官网: " + data.getOrDefault("website", "无"));
                        System.out.println("  电话: " + data.getOrDefault("phone", "无"));
                    }
                    
                } catch (org.springframework.dao.EmptyResultDataAccessException e) {
                    System.out.println("- 数据库中未找到: " + schoolName);
                    notFoundCount++;
                }
                
                // 每处理50个院校，输出统计信息
                if (schId % 50 == 0) {
                    System.out.println("\n========== 进度统计 ==========");
                    System.out.println("已处理: " + schId + "/3000");
                    System.out.println("成功: " + successCount);
                    System.out.println("跳过: " + skipCount);
                    System.out.println("失败: " + failCount);
                    System.out.println("未匹配: " + notFoundCount);
                    System.out.println("=============================\n");
                }
                
            } catch (Exception e) {
                System.err.println("× 处理ID " + schId + " 时出错: " + e.getMessage());
                failCount++;
            }
        }
        
        System.out.println("\n========================================");
        System.out.println("爬取完成！");
        System.out.println("总计处理: 3000");
        System.out.println("成功更新: " + successCount);
        System.out.println("跳过（已有数据）: " + skipCount);
        System.out.println("失败: " + failCount);
        System.out.println("未匹配: " + notFoundCount);
        System.out.println("========================================");
    }

    /**
     * 爬取单个院校详情页
     */
    private Map<String, String> crawlSchoolDetail(int schId) {
        Map<String, String> data = new HashMap<>();
        
        try {
            String url = "https://gaokao.chsi.com.cn/sch/schoolInfoMain--schId-" + schId + ".dhtml";
            
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Connection", "keep-alive")
                    .referrer("https://gaokao.chsi.com.cn/")
                    .timeout(20000)
                    .ignoreHttpErrors(true)
                    .get();
            
            // 检查是否遇到反爬虫
            String bodyText = doc.body() != null ? doc.body().text() : "";
            if (bodyText.contains("访问过于频繁") || bodyText.contains("验证码")) {
                System.out.println("× 遇到反爬虫限制");
                return data;
            }
            
            // 提取院校名称（多种可能的选择器）
            String schoolName = null;
            Element nameElement = doc.selectFirst("h1.name");
            if (nameElement == null) nameElement = doc.selectFirst("div.school-name");
            if (nameElement == null) nameElement = doc.selectFirst("h1");
            if (nameElement == null) {
                // 尝试从title中提取
                String title = doc.title();
                if (title != null && !title.isEmpty()) {
                    schoolName = title.replace("-阳光高考", "").replace("_阳光高考", "").trim();
                }
            } else {
                schoolName = nameElement.text().trim();
            }
            
            if (schoolName != null && !schoolName.isEmpty()) {
                data.put("school_name", schoolName);
            }
            
            // 提取Logo
            Element logoElement = doc.selectFirst("img.logo, div.school-logo img, img[src*=logo]");
            if (logoElement != null) {
                String logoUrl = logoElement.attr("abs:src");
                if (logoUrl != null && !logoUrl.isEmpty()) {
                    data.put("logo_url", logoUrl);
                }
            }
            
            // 提取基本信息（官网、电话、地址等）
            Elements infoElements = doc.select("div.info-item, li.info, p.info, tr");
            for (Element info : infoElements) {
                String text = info.text();
                
                // 提取官网
                if (text.contains("官网") || text.contains("网址")) {
                    Element link = info.selectFirst("a[href]");
                    if (link != null) {
                        String website = link.attr("abs:href");
                        if (website != null && !website.isEmpty() && !website.contains("gaokao.chsi")) {
                            data.put("website", website);
                        }
                    }
                }
                
                // 提取电话
                if (text.contains("电话") || text.contains("招生电话") || text.contains("联系电话")) {
                    String phone = text.replaceAll(".*电话[：:：]\\s*", "").trim();
                    if (phone.matches(".*\\d{3,4}-?\\d{7,8}.*")) {
                        data.put("phone", phone);
                    }
                }
                
                // 提取地址
                if (text.contains("地址") || text.contains("校址")) {
                    String address = text.replaceAll(".*地址[：:：]\\s*", "").trim();
                    if (!address.isEmpty() && address.length() > 5) {
                        data.put("address", address);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("爬取失败: " + e.getMessage());
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
}
