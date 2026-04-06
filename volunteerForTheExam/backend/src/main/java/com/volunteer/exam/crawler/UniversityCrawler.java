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

/**
 * 阳光高考网院校信息爬虫
 * 爬取院校详细信息：官网、校址、电话、微信、微博、百家号、视频号等
 */
@Component
public class UniversityCrawler {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 根据院校ID爬取详细信息
     * @param schoolId 院校ID（阳光高考网的学校ID）
     * @return 院校详细信息
     */
    public Map<String, String> crawlUniversityDetail(String schoolId) {
        Map<String, String> data = new HashMap<>();
        
        try {
            // 阳光高考网院校详情页URL
            String url = "https://gaokao.chsi.com.cn/sch/schoolInfoMain--schId-" + schoolId + ".dhtml";
            
            // 设置User-Agent，模拟浏览器访问
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            
            // 爬取Logo
            Element logoElement = doc.selectFirst("div.sch-logo img");
            if (logoElement != null) {
                data.put("logo_url", logoElement.attr("src"));
            }
            
            // 爬取基本信息
            Elements infoItems = doc.select("div.sch-info-box .info-item");
            for (Element item : infoItems) {
                String label = item.selectFirst(".label") != null ? 
                        item.selectFirst(".label").text() : "";
                String value = item.selectFirst(".value") != null ? 
                        item.selectFirst(".value").text() : "";
                
                if (label.contains("所在地")) {
                    data.put("address", value);
                } else if (label.contains("官方网址")) {
                    Element link = item.selectFirst("a");
                    if (link != null) {
                        data.put("website", link.attr("href"));
                    }
                } else if (label.contains("招生电话")) {
                    data.put("phone", value);
                } else if (label.contains("详细地址")) {
                    data.put("address", value);
                }
            }
            
            // 爬取社交媒体信息
            // 微信
            Element wechatElement = doc.selectFirst("div.wechat-info");
            if (wechatElement != null) {
                String wechatName = wechatElement.selectFirst(".name") != null ? 
                        wechatElement.selectFirst(".name").text() : "";
                String wechatId = wechatElement.selectFirst(".id") != null ? 
                        wechatElement.selectFirst(".id").text() : "";
                data.put("wechat_name", wechatName);
                data.put("wechat_id", wechatId);
            }
            
            // 微博
            Element weiboElement = doc.selectFirst("div.weibo-info");
            if (weiboElement != null) {
                String weiboName = weiboElement.selectFirst(".name") != null ? 
                        weiboElement.selectFirst(".name").text() : "";
                String weiboId = weiboElement.selectFirst(".id") != null ? 
                        weiboElement.selectFirst(".id").text() : "";
                data.put("weibo_name", weiboName);
                data.put("weibo_id", weiboId);
            }
            
            // 百家号
            Element baijiaElement = doc.selectFirst("div.baijia-info");
            if (baijiaElement != null) {
                String baijiaName = baijiaElement.selectFirst(".name") != null ? 
                        baijiaElement.selectFirst(".name").text() : "";
                String baijiaId = baijiaElement.selectFirst(".id") != null ? 
                        baijiaElement.selectFirst(".id").text() : "";
                data.put("baijia_name", baijiaName);
                data.put("baijia_id", baijiaId);
            }
            
            // 视频号
            Element videoElement = doc.selectFirst("div.video-info");
            if (videoElement != null) {
                String videoName = videoElement.selectFirst(".name") != null ? 
                        videoElement.selectFirst(".name").text() : "";
                String videoId = videoElement.selectFirst(".id") != null ? 
                        videoElement.selectFirst(".id").text() : "";
                data.put("video_name", videoName);
                data.put("video_id", videoId);
            }
            
            // 爬取院校简介
            Element introElement = doc.selectFirst("div.sch-intro");
            if (introElement != null) {
                data.put("introduction", introElement.text());
            }
            
            System.out.println("成功爬取院校信息: " + schoolId);
            
        } catch (Exception e) {
            System.err.println("爬取院校信息失败: " + schoolId + ", 错误: " + e.getMessage());
        }
        
        return data;
    }

    /**
     * 根据院校名称搜索并爬取信息
     * @param schoolName 院校名称
     */
    public void crawlAndSaveByName(String schoolName) {
        try {
            // 先从数据库查询院校
            String querySql = "SELECT id, school_name FROM university WHERE school_name = ? LIMIT 1";
            Map<String, Object> result = jdbcTemplate.queryForMap(querySql, schoolName);
            
            if (result == null) {
                System.out.println("数据库中未找到院校: " + schoolName);
                return;
            }
            
            Long universityId = (Long) result.get("id");
            
            // 检查是否已有详细信息
            String checkSql = "SELECT website FROM university WHERE id = ?";
            String existingWebsite = jdbcTemplate.queryForObject(checkSql, String.class, universityId);
            
            if (existingWebsite != null && !existingWebsite.isEmpty()) {
                System.out.println("院校已有详细信息，跳过: " + schoolName);
                return;
            }
            
            // 从阳光高考网搜索院校ID
            String searchUrl = "https://gaokao.chsi.com.cn/sch/search.do?searchType=1&keyword=" + 
                    java.net.URLEncoder.encode(schoolName, "UTF-8");
            
            // 添加随机延迟，避免请求过快
            Thread.sleep(2000 + (int)(Math.random() * 3000));
            
            Document searchDoc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .referrer("https://gaokao.chsi.com.cn/")
                    .timeout(15000)
                    .ignoreHttpErrors(true)
                    .get();
            
            // 获取第一个搜索结果的学校ID
            Element firstResult = searchDoc.selectFirst("div.yxk-result-box a");
            if (firstResult == null) {
                System.out.println("未在阳光高考网找到院校: " + schoolName);
                return;
            }
            
            String href = firstResult.attr("href");
            String schoolId = extractSchoolId(href);
            
            if (schoolId == null) {
                System.out.println("无法提取院校ID: " + schoolName);
                return;
            }
            
            // 爬取详细信息
            Map<String, String> data = crawlUniversityDetail(schoolId);
            
            // 保存到数据库
            if (!data.isEmpty()) {
                updateUniversityDetail(universityId, data);
                System.out.println("成功保存院校信息: " + schoolName);
            }
            
            // 随机延迟20-30秒，避免被识别为机器人
            int randomDelay = 20000 + (int)(Math.random() * 10000);
            Thread.sleep(randomDelay);
            
        } catch (Exception e) {
            System.err.println("处理院校失败: " + schoolName + ", 错误: " + e.getMessage());
        }
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
            end = url.length();
        }
        return url.substring(start, end);
    }

    /**
     * 更新数据库中的院校详细信息
     */
    private void updateUniversityDetail(Long universityId, Map<String, String> data) {
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
        String sql = "SELECT id, school_name FROM university WHERE website IS NULL OR website = ''";
        
        jdbcTemplate.query(sql, (rs) -> {
            String schoolName = rs.getString("school_name");
            System.out.println("正在处理: " + schoolName);
            crawlAndSaveByName(schoolName);
        });
        
        System.out.println("批量爬取完成！");
    }
}
