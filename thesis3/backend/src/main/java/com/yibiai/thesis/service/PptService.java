package com.yibiai.thesis.service;

import org.apache.poi.xslf.usermodel.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;

@Service
public class PptService {

    private final DeepSeekService deepSeekService;

    public PptService(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    public Flux<String> generateOutline(String title, String content) {
        String systemPrompt = """
            你是一位专业的学术答辩PPT制作专家。根据用户提供的论文内容，生成一份专业的答辩PPT大纲。
            
            PPT大纲要求：
            1. 封面页：论文标题、作者信息、指导教师、日期
            2. 目录页：列出PPT主要章节
            3. 研究背景与意义（1-2页）
            4. 研究目标与问题（1页）
            5. 研究方法（1-2页）
            6. 研究结果与分析（2-3页）
            7. 结论与展望（1页）
            8. 致谢页
            
            输出格式要求：
            - 每一页用 "## 页X：标题" 开头
            - 每页内容用要点形式列出，每个要点一行
            - 要点简洁精炼，适合PPT展示
            - 提取论文核心内容，不要照搬原文
            - 总共生成10-15页内容
            
            直接输出PPT大纲内容，不要添加任何解释性文字。
            """;

        String userPrompt = "论文标题：" + title + "\n\n论文内容：\n" + content;

        return deepSeekService.chatStream(systemPrompt, userPrompt);
    }

    public byte[] exportPptx(String title, String outline) {
        try (XMLSlideShow ppt = new XMLSlideShow();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            String[] pages = outline.split("(?=## 页)");
            
            for (String page : pages) {
                if (page.trim().isEmpty()) continue;
                
                XSLFSlide slide = ppt.createSlide();
                
                String[] lines = page.trim().split("\n");
                String pageTitle = "";
                StringBuilder contentBuilder = new StringBuilder();
                
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("## ")) {
                        pageTitle = line.substring(3).replaceAll("^页\\d+[：:]\\s*", "");
                    } else if (line.startsWith("- ") || line.startsWith("• ")) {
                        contentBuilder.append(line.substring(2)).append("\n");
                    } else if (!line.isEmpty()) {
                        contentBuilder.append(line).append("\n");
                    }
                }
                
                XSLFTextBox titleBox = slide.createTextBox();
                titleBox.setAnchor(new Rectangle(50, 30, 620, 60));
                XSLFTextParagraph titlePara = titleBox.addNewTextParagraph();
                XSLFTextRun titleRun = titlePara.addNewTextRun();
                titleRun.setText(pageTitle.isEmpty() ? title : pageTitle);
                titleRun.setFontSize(28.0);
                titleRun.setBold(true);
                titleRun.setFontColor(new Color(0, 51, 102));
                
                if (contentBuilder.length() > 0) {
                    XSLFTextBox contentBox = slide.createTextBox();
                    contentBox.setAnchor(new Rectangle(50, 100, 620, 350));
                    
                    String[] contentLines = contentBuilder.toString().split("\n");
                    for (String contentLine : contentLines) {
                        if (contentLine.trim().isEmpty()) continue;
                        XSLFTextParagraph para = contentBox.addNewTextParagraph();
                        para.setBullet(true);
                        para.setIndentLevel(0);
                        XSLFTextRun run = para.addNewTextRun();
                        run.setText(contentLine.trim());
                        run.setFontSize(18.0);
                        run.setFontColor(new Color(51, 51, 51));
                    }
                }
            }
            
            if (ppt.getSlides().isEmpty()) {
                XSLFSlide slide = ppt.createSlide();
                XSLFTextBox titleBox = slide.createTextBox();
                titleBox.setAnchor(new Rectangle(50, 200, 620, 80));
                XSLFTextParagraph titlePara = titleBox.addNewTextParagraph();
                XSLFTextRun titleRun = titlePara.addNewTextRun();
                titleRun.setText(title);
                titleRun.setFontSize(36.0);
                titleRun.setBold(true);
                titleRun.setFontColor(new Color(0, 51, 102));
            }
            
            ppt.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出PPT失败：" + e.getMessage(), e);
        }
    }
}
