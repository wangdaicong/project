package com.volunteer.exam.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 测试爬虫 - 测试能否爬取阳光高考网院校详情页
 */
public class TestCrawler {

    public static void main(String[] args) {
        try {
            System.out.println("========================================");
            System.out.println("测试爬取阳光高考网院校详情页");
            System.out.println("URL: https://gaokao.chsi.com.cn/sch/schoolInfoMain--schId-1.dhtml");
            System.out.println("========================================\n");
            
            // 发送请求
            Document doc = Jsoup.connect("https://gaokao.chsi.com.cn/sch/schoolInfoMain--schId-1.dhtml")
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
            
            System.out.println("✓ 成功获取页面\n");
            
            // 打印页面标题
            String title = doc.title();
            System.out.println("页面标题: " + title);
            System.out.println();
            
            // 尝试提取院校名称
            Element schoolNameElement = doc.selectFirst("h1.name");
            if (schoolNameElement == null) {
                schoolNameElement = doc.selectFirst("div.school-name");
            }
            if (schoolNameElement == null) {
                schoolNameElement = doc.selectFirst("h1");
            }
            
            if (schoolNameElement != null) {
                System.out.println("院校名称: " + schoolNameElement.text());
            } else {
                System.out.println("× 未找到院校名称元素");
            }
            System.out.println();
            
            // 尝试提取Logo
            Element logoElement = doc.selectFirst("img.logo");
            if (logoElement == null) {
                logoElement = doc.selectFirst("div.school-logo img");
            }
            if (logoElement == null) {
                logoElement = doc.selectFirst("img[src*=logo]");
            }
            
            if (logoElement != null) {
                System.out.println("Logo URL: " + logoElement.attr("src"));
            } else {
                System.out.println("× 未找到Logo元素");
            }
            System.out.println();
            
            // 尝试提取基本信息
            System.out.println("基本信息:");
            Elements infoElements = doc.select("div.info-item");
            if (infoElements.isEmpty()) {
                infoElements = doc.select("li.info");
            }
            if (infoElements.isEmpty()) {
                infoElements = doc.select("p.info");
            }
            
            if (!infoElements.isEmpty()) {
                for (Element info : infoElements) {
                    System.out.println("  - " + info.text());
                }
            } else {
                System.out.println("  × 未找到基本信息元素");
            }
            System.out.println();
            
            // 打印所有可能包含信息的div
            System.out.println("页面主要内容区域:");
            Elements contentDivs = doc.select("div[class*=content], div[class*=info], div[class*=detail]");
            for (int i = 0; i < Math.min(5, contentDivs.size()); i++) {
                Element div = contentDivs.get(i);
                System.out.println("  [" + i + "] class=" + div.className() + ", text=" + 
                        div.text().substring(0, Math.min(100, div.text().length())) + "...");
            }
            System.out.println();
            
            // 打印页面的主要HTML结构（前2000个字符）
            String html = doc.html();
            System.out.println("页面HTML结构（前2000字符）:");
            System.out.println(html.substring(0, Math.min(2000, html.length())));
            System.out.println();
            
            System.out.println("========================================");
            System.out.println("测试完成");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("× 爬取失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
