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
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSimpleField;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

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
            buildDocxThesis(doc, title, markdownContent);

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

            buildPdfThesis(document, title, markdownContent, font);

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

    public byte[] exportTxt(String markdownContent) {
        String text = toPlainText(markdownContent);
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private enum BlockType {
        ZH_ABSTRACT,
        EN_ABSTRACT,
        TOC,
        REFERENCES,
        HEADING_1,
        HEADING_2,
        HEADING_3,
        PARAGRAPH
    }

    private record Block(BlockType type, String text) {
    }

    private void buildDocxThesis(XWPFDocument doc, String title, String markdownContent) {
        List<Block> blocks = parseMarkdownBlocks(markdownContent);

        if (title != null && !title.isBlank()) {
            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = p.createRun();
            run.setBold(true);
            run.setFontFamily("黑体");
            run.setFontSize(16);
            run.setText(title.trim());
        }

        int h1 = 0;
        int h2 = 0;
        int h3 = 0;

        for (int i = 0; i < blocks.size(); i++) {
            Block b = blocks.get(i);
            switch (b.type) {
                case ZH_ABSTRACT -> {
                    addDocxCenteredTitle(doc, "摘 要", "黑体", 16, true, 24, 18);
                    addDocxBodyParagraphs(doc, b.text, "宋体", 12, true, 2, 0, 0, 20);
                    addDocxBlankLine(doc);
                    String[] kw = extractKeywords(b.text, true);
                    addDocxKeywords(doc, "关键词", kw, true);
                    addDocxBlankLine(doc);
                }
                case EN_ABSTRACT -> {
                    addDocxCenteredTitle(doc, "ABSTRACT", "Arial", 16, true, 24, 18);
                    addDocxBodyParagraphs(doc, b.text, "Times New Roman", 12, true, 2, 0, 0, 20);
                    addDocxBlankLine(doc);
                    String[] kw = extractKeywords(b.text, false);
                    addDocxKeywords(doc, "Key words", kw, false);
                    addDocxBlankLine(doc);
                }
                case TOC -> {
                    addDocxCenteredTitle(doc, "目 录", "黑体", 16, true, 24, 18);
                    addDocxTocField(doc);
                    addDocxPageBreak(doc);
                }
                case REFERENCES -> {
                    addDocxCenteredTitle(doc, "参考文献", "黑体", 16, true, 24, 18);
                    addDocxReferenceItems(doc, b.text);
                }
                case HEADING_1 -> {
                    h1++;
                    h2 = 0;
                    h3 = 0;
                    String headingText = normalizeHeadingNumbering(b.text, 1, h1, h2, h3);
                    addDocxHeading(doc, headingText, 1);
                }
                case HEADING_2 -> {
                    if (h1 == 0) {
                        h1 = 1;
                    }
                    h2++;
                    h3 = 0;
                    String headingText = normalizeHeadingNumbering(b.text, 2, h1, h2, h3);
                    addDocxHeading(doc, headingText, 2);
                }
                case HEADING_3 -> {
                    if (h1 == 0) {
                        h1 = 1;
                    }
                    if (h2 == 0) {
                        h2 = 1;
                    }
                    h3++;
                    String headingText = normalizeHeadingNumbering(b.text, 3, h1, h2, h3);
                    addDocxHeading(doc, headingText, 3);
                }
                case PARAGRAPH -> addDocxBodyParagraphs(doc, b.text, "宋体", 12, true, 2, 0, 0, 20);
            }
        }
    }

    private void buildPdfThesis(Document document, String title, String markdownContent, PdfFont baseFont) {
        List<Block> blocks = parseMarkdownBlocks(markdownContent);

        if (title != null && !title.isBlank()) {
            Paragraph p = new Paragraph(title.trim())
                    .setFont(baseFont)
                    .setBold()
                    .setFontSize(16)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setMarginTop(12)
                    .setMarginBottom(12);
            document.add(p);
        }

        int h1 = 0;
        int h2 = 0;
        int h3 = 0;

        for (Block b : blocks) {
            switch (b.type) {
                case ZH_ABSTRACT -> {
                    addPdfCenteredTitle(document, baseFont, "摘 要", 16);
                    addPdfBody(document, baseFont, b.text, 12);
                    String[] kw = extractKeywords(b.text, true);
                    addPdfKeywords(document, baseFont, "关键词", kw, true);
                    addPdfBlankLine(document, baseFont);
                }
                case EN_ABSTRACT -> {
                    PdfFont enFont;
                    try {
                        enFont = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
                    } catch (Exception ignored) {
                        enFont = baseFont;
                    }
                    addPdfCenteredTitle(document, enFont, "ABSTRACT", 16);
                    addPdfBody(document, enFont, b.text, 12);
                    String[] kw = extractKeywords(b.text, false);
                    addPdfKeywords(document, enFont, "Key words", kw, false);
                    addPdfBlankLine(document, enFont);
                }
                case TOC -> {
                    addPdfCenteredTitle(document, baseFont, "目 录", 16);
                    addPdfBody(document, baseFont, b.text, 12);
                    document.add(new com.itextpdf.layout.element.AreaBreak());
                }
                case REFERENCES -> {
                    addPdfCenteredTitle(document, baseFont, "参考文献", 16);
                    addPdfReferences(document, baseFont, b.text);
                }
                case HEADING_1 -> {
                    h1++;
                    h2 = 0;
                    h3 = 0;
                    String headingText = normalizeHeadingNumbering(b.text, 1, h1, h2, h3);
                    addPdfHeading(document, baseFont, headingText, 1);
                }
                case HEADING_2 -> {
                    if (h1 == 0) {
                        h1 = 1;
                    }
                    h2++;
                    h3 = 0;
                    String headingText = normalizeHeadingNumbering(b.text, 2, h1, h2, h3);
                    addPdfHeading(document, baseFont, headingText, 2);
                }
                case HEADING_3 -> {
                    if (h1 == 0) {
                        h1 = 1;
                    }
                    if (h2 == 0) {
                        h2 = 1;
                    }
                    h3++;
                    String headingText = normalizeHeadingNumbering(b.text, 3, h1, h2, h3);
                    addPdfHeading(document, baseFont, headingText, 3);
                }
                case PARAGRAPH -> addPdfBody(document, baseFont, b.text, 12);
            }
        }
    }

    private List<Block> parseMarkdownBlocks(String markdownContent) {
        if (markdownContent == null) {
            return List.of();
        }
        String normalized = markdownContent.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);
        List<Block> blocks = new java.util.ArrayList<>();

        String currentHeading = null;
        int currentLevel = 0;
        StringBuilder buf = new StringBuilder();

        java.util.function.BiConsumer<Integer, String> flush = (level, heading) -> {
            String text = buf.toString().trim();
            if (!text.isEmpty()) {
                BlockType t;
                if (heading != null && (heading.equals("摘要") || heading.equals("中文摘要") || heading.equals("摘 要"))) {
                    t = BlockType.ZH_ABSTRACT;
                } else if (heading != null && (heading.equalsIgnoreCase("ABSTRACT") || heading.equals("英文摘要"))) {
                    t = BlockType.EN_ABSTRACT;
                } else if (heading != null && (heading.equals("目录") || heading.equals("目 录"))) {
                    t = BlockType.TOC;
                } else if (heading != null && heading.equals("参考文献")) {
                    t = BlockType.REFERENCES;
                } else {
                    t = BlockType.PARAGRAPH;
                }
                blocks.add(new Block(t, text));
            }
            buf.setLength(0);
        };

        for (String line : lines) {
            String trimmed = line.trim();
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(#{1,3})\\s+(.+)$").matcher(trimmed);
            if (m.matches()) {
                flush.accept(currentLevel, currentHeading);
                String hashes = m.group(1);
                String headingText = m.group(2).replaceAll("\\*\\*", "").trim();
                currentHeading = headingText;
                currentLevel = hashes.length();

                BlockType t = switch (currentLevel) {
                    case 1 -> BlockType.HEADING_1;
                    case 2 -> BlockType.HEADING_2;
                    default -> BlockType.HEADING_3;
                };
                blocks.add(new Block(t, headingText));
                continue;
            }
            buf.append(line).append("\n");
        }
        flush.accept(currentLevel, currentHeading);
        return blocks;
    }

    private String normalizeHeadingNumbering(String heading, int level, int h1, int h2, int h3) {
        if (heading == null) {
            return "";
        }
        String t = heading.trim();
        if (t.matches("^\\d+(\\.\\d+){0,2}\\s+.*")) {
            return t;
        }
        return switch (level) {
            case 1 -> h1 + " " + t;
            case 2 -> h1 + "." + h2 + " " + t;
            default -> h1 + "." + h2 + "." + h3 + " " + t;
        };
    }

    private void addDocxCenteredTitle(XWPFDocument doc, String text, String font, int fontSize, boolean bold, int beforePt, int afterPt) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        p.setSpacingBefore(beforePt * 20);
        p.setSpacingAfter(afterPt * 20);
        XWPFRun run = p.createRun();
        run.setBold(bold);
        run.setFontFamily(font);
        run.setFontSize(fontSize);
        run.setText(text);
    }

    private void addDocxBodyParagraphs(XWPFDocument doc, String markdown, String font, int fontSize, boolean indent, int indentChars, int beforePt, int afterPt, int lineSpacingPtExact) {
        String text = toPlainText(markdown);
        for (String line : text.split("\\n", -1)) {
            String t = line.stripTrailing();
            if (t.isBlank()) {
                addDocxBlankLine(doc);
                continue;
            }

            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.BOTH);
            p.setSpacingBefore(beforePt * 20);
            p.setSpacingAfter(afterPt * 20);
            p.setSpacingBetween(lineSpacingPtExact, LineSpacingRule.EXACT);
            if (indent) {
                p.setFirstLineIndent(indentChars * fontSize * 20);
            }
            XWPFRun run = p.createRun();
            run.setFontFamily(font);
            run.setFontSize(fontSize);
            run.setText(t);
        }
    }

    private void addDocxKeywords(XWPFDocument doc, String label, String[] keywords, boolean chineseComma) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.LEFT);
        p.setSpacingBefore(0);
        p.setSpacingAfter(0);
        p.setSpacingBetween(20, LineSpacingRule.EXACT);
        XWPFRun labelRun = p.createRun();
        labelRun.setFontFamily(chineseComma ? "宋体" : "Times New Roman");
        labelRun.setFontSize(12);
        labelRun.setBold(true);
        labelRun.setText(label);

        XWPFRun kwRun = p.createRun();
        kwRun.setFontFamily(chineseComma ? "宋体" : "Times New Roman");
        kwRun.setFontSize(12);
        kwRun.setBold(false);

        String joined = joinKeywords(keywords, chineseComma);
        if (!joined.isBlank()) {
            kwRun.setText(" " + joined);
        }
    }

    private void addDocxHeading(XWPFDocument doc, String heading, int level) {
        XWPFParagraph p = doc.createParagraph();
        if (level == 1) {
            p.setStyle("Heading1");
        } else if (level == 2) {
            p.setStyle("Heading2");
        } else {
            p.setStyle("Heading3");
        }
        if (level == 1) {
            p.setAlignment(ParagraphAlignment.CENTER);
        } else {
            p.setAlignment(ParagraphAlignment.LEFT);
        }
        p.setSpacingBefore(0);
        p.setSpacingAfter(0);

        XWPFRun run = p.createRun();
        run.setBold(true);
        run.setFontFamily("黑体");
        run.setFontSize(level == 1 ? 16 : (level == 2 ? 14 : 12));
        run.setText(heading);
    }

    private void addDocxReferenceItems(XWPFDocument doc, String markdown) {
        String text = toPlainText(markdown);
        String[] lines = text.split("\\n", -1);
        for (String line : lines) {
            String t = line.trim();
            if (t.isBlank()) {
                continue;
            }
            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.LEFT);
            p.setSpacingBetween(1.0, LineSpacingRule.AUTO);
            p.setSpacingBefore(0);
            p.setSpacingAfter(0);
            XWPFRun run = p.createRun();
            run.setFontFamily("宋体");
            run.setFontSize(10);
            run.setText(t);
        }
    }

    private void addDocxTocField(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.LEFT);
        CTP ctp = p.getCTP();
        CTSimpleField toc = ctp.addNewFldSimple();
        toc.setInstr("TOC \\o \"1-3\" \\h \\z \\u");
    }

    private void addDocxBlankLine(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setText("");
    }

    private void addDocxPageBreak(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setPageBreak(true);
    }

    private void addPdfCenteredTitle(Document document, PdfFont font, String text, int fontSize) {
        Paragraph p = new Paragraph(text)
                .setFont(font)
                .setBold()
                .setFontSize(fontSize)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setMarginTop(24)
                .setMarginBottom(18);
        document.add(p);
    }

    private void addPdfBody(Document document, PdfFont font, String markdown, int fontSize) {
        String text = toPlainText(markdown);
        for (String line : text.split("\\n", -1)) {
            String t = line.stripTrailing();
            if (t.isBlank()) {
                document.add(new Paragraph(" ").setMargin(0));
                continue;
            }
            Paragraph p = new Paragraph(t)
                    .setFont(font)
                    .setFontSize(fontSize)
                    .setFirstLineIndent(fontSize * 2)
                    .setFixedLeading(20)
                    .setMarginTop(0)
                    .setMarginBottom(0);
            document.add(p);
        }
    }

    private void addPdfKeywords(Document document, PdfFont font, String label, String[] keywords, boolean chineseComma) {
        String joined = joinKeywords(keywords, chineseComma);
        Paragraph p = new Paragraph()
                .setFont(font)
                .setFontSize(12)
                .setFixedLeading(20)
                .setMarginTop(0)
                .setMarginBottom(0);
        p.add(new com.itextpdf.layout.element.Text(label).setBold());
        if (!joined.isBlank()) {
            p.add(new com.itextpdf.layout.element.Text(" " + joined));
        }
        document.add(p);
    }

    private void addPdfBlankLine(Document document, PdfFont font) {
        document.add(new Paragraph(" ").setFont(font).setMargin(0));
    }

    private void addPdfHeading(Document document, PdfFont font, String heading, int level) {
        Paragraph p = new Paragraph(heading)
                .setFont(font)
                .setBold()
                .setFontSize(level == 1 ? 16 : (level == 2 ? 14 : 12))
                .setTextAlignment(level == 1 ? com.itextpdf.layout.properties.TextAlignment.CENTER : com.itextpdf.layout.properties.TextAlignment.LEFT)
                .setMarginTop(12)
                .setMarginBottom(6);
        document.add(p);
    }

    private void addPdfReferences(Document document, PdfFont font, String markdown) {
        String text = toPlainText(markdown);
        for (String line : text.split("\\n", -1)) {
            String t = line.trim();
            if (t.isBlank()) {
                continue;
            }
            Paragraph p = new Paragraph(t)
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginTop(0)
                    .setMarginBottom(0);
            document.add(p);
        }
    }

    private String[] extractKeywords(String markdown, boolean chinese) {
        if (markdown == null) {
            return new String[0];
        }
        String text = markdown.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = text.split("\n");
        for (String line : lines) {
            String t = line.trim();
            if (chinese) {
                if (t.startsWith("关键词")) {
                    String rest = t.substring("关键词".length()).replaceFirst("^[:：\\s]+", "");
                    return splitKeywords(rest, true);
                }
            } else {
                if (t.toLowerCase().startsWith("key words") || t.toLowerCase().startsWith("keywords")) {
                    String rest = t.replaceFirst("(?i)key\\s*words", "").replaceFirst("(?i)keywords", "");
                    rest = rest.replaceFirst("^[:：\\s]+", "");
                    return splitKeywords(rest, false);
                }
            }
        }
        return new String[0];
    }

    private String[] splitKeywords(String raw, boolean chineseComma) {
        if (raw == null) {
            return new String[0];
        }
        String cleaned = raw.trim();
        if (cleaned.isBlank()) {
            return new String[0];
        }
        String[] parts = cleaned.split(chineseComma ? "[，,]+" : "[,，]+", -1);
        java.util.List<String> out = new java.util.ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isBlank()) {
                out.add(t);
            }
        }
        return out.toArray(new String[0]);
    }

    private String joinKeywords(String[] keywords, boolean chineseComma) {
        if (keywords == null || keywords.length == 0) {
            return "";
        }
        String sep = chineseComma ? "，" : ", ";
        String joined = String.join(sep, keywords);
        return joined.replaceAll("[，,\\s]+$", "");
    }

    private static final Pattern MD_FENCED_CODE_BLOCK = Pattern.compile("(?s)```[^\n]*\n(.*?)\n```", Pattern.MULTILINE);
    private static final Pattern MD_INLINE_CODE = Pattern.compile("`([^`]*)`");
    private static final Pattern MD_IMAGE = Pattern.compile("!\\[([^\\]]*)\\]\\([^\\)]*\\)");
    private static final Pattern MD_LINK = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)");
    private static final Pattern MD_BOLD_ITALIC = Pattern.compile("(\\*\\*|__|\\*|_)");
    private static final Pattern MD_HEADING = Pattern.compile("(?m)^\\s{0,3}#{1,6}\\s+");
    private static final Pattern MD_BLOCKQUOTE = Pattern.compile("(?m)^\\s{0,3}>\\s?");
    private static final Pattern MD_LIST_PREFIX = Pattern.compile("(?m)^\\s{0,3}([\\*\\-\\+]\\s+|\\d+\\.\\s+)");
    private static final Pattern MD_HR = Pattern.compile("(?m)^\\s{0,3}(-{3,}|\\*{3,}|_{3,})\\s*$");
    private static final Pattern MD_TABLE_SEPARATOR = Pattern.compile("(?m)^\\s*\\|?\\s*:?-{2,}:?\\s*(\\|\\s*:?-{2,}:?\\s*)+\\|?\\s*$");

    private String toPlainText(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        String text = markdown;

        text = convertMarkdownTablesToAlignedText(text);

        text = MD_FENCED_CODE_BLOCK.matcher(text).replaceAll("$1");
        text = MD_IMAGE.matcher(text).replaceAll("$1");
        text = MD_LINK.matcher(text).replaceAll("$1 ($2)");
        text = MD_INLINE_CODE.matcher(text).replaceAll("$1");

        text = MD_HEADING.matcher(text).replaceAll("");
        text = MD_BLOCKQUOTE.matcher(text).replaceAll("");
        text = MD_LIST_PREFIX.matcher(text).replaceAll("");
        text = MD_HR.matcher(text).replaceAll("");
        text = MD_TABLE_SEPARATOR.matcher(text).replaceAll("");

        text = MD_BOLD_ITALIC.matcher(text).replaceAll("");

        text = text.replace("\r\n", "\n").replace('\r', '\n');
        text = text.replaceAll("\n{3,}", "\n\n").trim();
        return text;
    }

    private String convertMarkdownTablesToAlignedText(String input) {
        String[] lines = input.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        StringBuilder out = new StringBuilder();

        int i = 0;
        while (i < lines.length) {
            String line = lines[i];

            if (looksLikeTableRow(line) && i + 1 < lines.length && MD_TABLE_SEPARATOR.matcher(lines[i + 1]).matches()) {
                int start = i;
                int end = i;
                while (end < lines.length && looksLikeTableRow(lines[end])) {
                    end++;
                }

                List<String[]> rows = new java.util.ArrayList<>();
                int maxCols = 0;
                for (int r = start; r < end; r++) {
                    if (MD_TABLE_SEPARATOR.matcher(lines[r]).matches()) {
                        continue;
                    }
                    String[] cells = splitTableRow(lines[r]);
                    rows.add(cells);
                    maxCols = Math.max(maxCols, cells.length);
                }

                int[] widths = new int[maxCols];
                for (String[] row : rows) {
                    for (int c = 0; c < row.length; c++) {
                        widths[c] = Math.max(widths[c], row[c].length());
                    }
                }

                for (String[] row : rows) {
                    for (int c = 0; c < maxCols; c++) {
                        String cell = c < row.length ? row[c] : "";
                        out.append(padRight(cell, widths[c]));
                        if (c < maxCols - 1) {
                            out.append("  ");
                        }
                    }
                    out.append("\n");
                }
                i = end;
                continue;
            }

            out.append(line).append("\n");
            i++;
        }

        return out.toString();
    }

    private boolean looksLikeTableRow(String line) {
        if (line == null) {
            return false;
        }
        String t = line.trim();
        if (t.isEmpty()) {
            return false;
        }
        return t.contains("|");
    }

    private String[] splitTableRow(String line) {
        String t = line.trim();
        if (t.startsWith("|")) {
            t = t.substring(1);
        }
        if (t.endsWith("|")) {
            t = t.substring(0, t.length() - 1);
        }
        String[] raw = t.split("\\|", -1);
        for (int i = 0; i < raw.length; i++) {
            raw[i] = raw[i].trim();
        }
        return raw;
    }

    private String padRight(String s, int width) {
        if (s == null) {
            s = "";
        }
        if (s.length() >= width) {
            return s;
        }
        return s + " ".repeat(width - s.length());
    }
}
