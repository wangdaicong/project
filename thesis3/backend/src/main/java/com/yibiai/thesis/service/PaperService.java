package com.yibiai.thesis.service;

import com.yibiai.thesis.dto.OutlineRequest;
import com.yibiai.thesis.dto.PaperGenerateRequest;
import com.yibiai.thesis.entity.Paper;
import com.yibiai.thesis.repository.PaperRepository;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaperService {

    private final PaperRepository paperRepository;
    private final DeepSeekService deepSeekService;

    public PaperService(PaperRepository paperRepository, DeepSeekService deepSeekService) {
        this.paperRepository = paperRepository;
        this.deepSeekService = deepSeekService;
    }

    public Mono<String> generateOutline(OutlineRequest request) {
        String languageHint = buildLanguageHint(request.getLanguages());
        String systemPrompt = """
            你是一位专业的学术论文写作专家。请根据用户提供的论文题目、类型、学科和字数要求，生成一份详细的三级论文大纲。
            %s
            大纲要求：
            1. 结构完整，包含摘要、引言、正文（多章节）、结论、参考文献等部分
            2. 每个章节下设2-4个小节，小节下可设具体论点
            3. 内容专业、逻辑清晰、层次分明
            4. 符合学术论文写作规范
            5. 字数分配合理
            请直接输出大纲内容，使用markdown格式。
            """.formatted(languageHint);

        String userPrompt = String.format("""
            论文题目：%s
            论文类型：%s
            学科领域：%s
            写作语言：%s
            目标字数：%d字
            %s
            %s
            
            请生成详细的三级论文大纲。
            """,
            request.getTitle(),
            request.getPaperType(),
            request.getSubject(),
            formatLanguagesForPrompt(request.getLanguages()),
            request.getWordCount(),
            request.getCustomRequirements() != null ? "特殊要求：" + request.getCustomRequirements() : "",
            request.getReferenceContent() != null ? "参考资料：" + request.getReferenceContent() : ""
        );

        return deepSeekService.chat(systemPrompt, userPrompt);
    }

    public Flux<String> generatePaperStream(PaperGenerateRequest request) {
        String systemPrompt = buildPaperSystemPrompt(request);
        String userPrompt = buildPaperUserPrompt(request);
        return deepSeekService.chatStream(systemPrompt, userPrompt);
    }

    public Mono<String> generatePaper(PaperGenerateRequest request) {
        String systemPrompt = buildPaperSystemPrompt(request);
        String userPrompt = buildPaperUserPrompt(request);
        return deepSeekService.chat(systemPrompt, userPrompt);
    }

    private String buildPaperSystemPrompt(PaperGenerateRequest request) {
        String languageHint = buildLanguageHint(request.getLanguages());
        StringBuilder sb = new StringBuilder();
        sb.append("""
            你是一位专业的学术论文写作专家，拥有丰富的学术写作经验。请根据提供的大纲和要求，撰写一篇高质量的学术论文。
            
            写作要求：
            1. 严格按照提供的大纲结构进行写作
            2. 语言专业、准确，符合学术论文规范
            3. 论述有理有据，逻辑严密
            4. 适当引用文献，在文中标注引用位置
            5. 生成真实可查的参考文献，格式规范
            6. 包含中英文摘要和关键词
            7. 结尾包含致谢部分
            """);

        if (!languageHint.isBlank()) {
            sb.append(languageHint).append("\n");
        }

        if (Boolean.TRUE.equals(request.getIncludeCharts())) {
            sb.append("8. 适当加入数据表格，使用markdown表格格式\n");
        }
        if (Boolean.TRUE.equals(request.getIncludeImages())) {
            sb.append("9. 适当加入插图，使用markdown图片语法 ![图1 图注](https://picsum.photos/seed/fig1/800/400) ，并在图片下方补充图注说明\n");
        }
        if (Boolean.TRUE.equals(request.getIncludeFormulas())) {
            sb.append("10. 适当加入数学公式，使用LaTeX格式\n");
        }
        if (Boolean.TRUE.equals(request.getIncludeCode())) {
            sb.append("11. 适当加入代码示例，使用markdown代码块\n");
        }

        sb.append("\n请直接输出论文内容，使用markdown格式。");
        return sb.toString();
    }

    private String buildPaperUserPrompt(PaperGenerateRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("""
            论文题目：%s
            论文类型：%s
            学科领域：%s
            写作语言：%s
            目标字数：%d字
            
            论文大纲：
            %s
            """,
            request.getTitle(),
            request.getPaperType(),
            request.getSubject(),
            formatLanguagesForPrompt(request.getLanguages()),
            request.getWordCount(),
            request.getOutline()
        ));

        if (request.getPreviousContent() != null && !request.getPreviousContent().isBlank()) {
            String prev = request.getPreviousContent();
            int max = 4000;
            if (prev.length() > max) {
                prev = prev.substring(prev.length() - max);
            }
            sb.append("\n已生成的论文内容（末尾片段，仅用于续写定位，请勿重复）：\n");
            sb.append(prev);
            sb.append("\n\n请从以上内容末尾继续撰写，避免重复，直到完整覆盖大纲剩余章节，并补齐参考文献与致谢。\n");
        }

        if (request.getCustomRequirements() != null && !request.getCustomRequirements().isBlank()) {
            sb.append("\n特殊要求：").append(request.getCustomRequirements()).append("\n");
        }

        if (request.getReferenceContent() != null && !request.getReferenceContent().isBlank()) {
            sb.append("\n参考资料内容：").append(request.getReferenceContent()).append("\n");
        }

        if (request.getPreviousContent() != null && !request.getPreviousContent().isBlank()) {
            sb.append("\n继续输出剩余内容（不要重复已写部分）。");
        } else {
            sb.append("\n请根据以上大纲，撰写完整的学术论文。");
        }

        return sb.toString();
    }

    private String buildLanguageHint(List<String> languages) {
        if (languages == null || languages.isEmpty()) {
            return "";
        }

        String formatted = formatLanguagesForPrompt(languages);
        if (formatted.isBlank()) {
            return "";
        }

        return "写作语言要求：请严格使用 " + formatted + " 进行输出。";
    }

    private String formatLanguagesForPrompt(List<String> languages) {
        if (languages == null || languages.isEmpty()) {
            return "";
        }
        return String.join("/", languages);
    }

    public Mono<String> generateReferences(String title, String subject, int count) {
        String systemPrompt = """
            你是一位学术文献专家。请根据论文题目和学科，生成真实可查的参考文献列表。
            要求：
            1. 文献格式规范，符合GB/T 7714标准
            2. 包含期刊论文、专著、学位论文等多种类型
            3. 中英文文献都要包含
            4. 文献内容与论文主题相关
            5. 尽量使用近5年的文献
            """;

        String userPrompt = String.format("""
            论文题目：%s
            学科领域：%s
            需要文献数量：%d篇
            
            请生成参考文献列表。
            """, title, subject, count);

        return deepSeekService.chat(systemPrompt, userPrompt);
    }

    public Paper savePaper(Paper paper) {
        paper.setUpdatedAt(LocalDateTime.now());
        return paperRepository.save(paper);
    }

    public Optional<Paper> findById(Long id) {
        return paperRepository.findById(id);
    }

    public List<Paper> findByUserId(Long userId) {
        return paperRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void deletePaper(Long id) {
        paperRepository.deleteById(id);
    }

    public byte[] exportDocx(String title, String markdownContent) {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (title != null && !title.isBlank()) {
                XWPFParagraph p = doc.createParagraph();
                XWPFRun run = p.createRun();
                run.setBold(true);
                run.setFontSize(16);
                run.setText(title);
            }

            String text = markdownContent == null ? "" : markdownContent;
            for (String line : text.split("\\r?\\n", -1)) {
                XWPFParagraph p = doc.createParagraph();
                XWPFRun run = p.createRun();
                run.setFontSize(12);
                run.setText(line);
            }

            doc.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Word失败：" + e.getMessage(), e);
        }
    }

    public byte[] exportPdf(String title, String markdownContent) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont font;
            try {
                font = tryLoadWindowsChineseFont();
            } catch (Exception ignored) {
                font = null;
            }

            if (font == null) {
                try {
                    font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H", EmbeddingStrategy.PREFER_EMBEDDED);
                } catch (Exception ignored) {
                    font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                }
            }
            document.setFont(font);

            if (title != null && !title.isBlank()) {
                document.add(new Paragraph(title).setFontSize(16));
            }

            String text = markdownContent == null ? "" : markdownContent;
            for (String line : text.split("\\r?\\n", -1)) {
                document.add(new Paragraph(line).setFontSize(11));
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出PDF失败：" + e.getMessage(), e);
        }
    }

    private PdfFont tryLoadWindowsChineseFont() {
        String[] candidates = new String[] {
                "C:/Windows/Fonts/msyh.ttc",
                "C:/Windows/Fonts/msyh.ttf",
                "C:/Windows/Fonts/simsun.ttc",
                "C:/Windows/Fonts/simsun.ttf"
        };

        for (String p : candidates) {
            try {
                Path path = Path.of(p);
                if (Files.exists(path)) {
                    return PdfFontFactory.createFont(path.toString(), "Identity-H", EmbeddingStrategy.PREFER_EMBEDDED);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
