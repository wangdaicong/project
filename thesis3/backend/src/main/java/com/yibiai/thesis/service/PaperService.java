package com.yibiai.thesis.service;

import com.yibiai.thesis.dto.OutlineRequest;
import com.yibiai.thesis.dto.PaperGenerateRequest;
import com.yibiai.thesis.entity.Paper;
import com.yibiai.thesis.repository.PaperRepository;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.navigation.PdfExplicitDestination;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
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
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSimpleField;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
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
            1. 结构完整，包含摘要、引言、正文（多章节）、结论、参考文献、致谢等部分
            2. 每个章节下设2-4个小节，小节下可设具体论点
            3. 内容专业、逻辑清晰、层次分明
            4. 符合学术论文写作规范
            5. 字数分配合理
            
            格式要求（必须严格遵守）：
            - 论文标题使用一级标题：# 论文标题
            - 摘要使用二级标题：## 摘要（摘要下面不要添加任何三级或四级标题）
            - 所有正文章节（包括绪论和结论）都必须编号为"第X章"，使用二级标题：## 第一章 绪论、## 第二章 xxx、...、## 第七章 结论
            - 参考文献使用二级标题：## 参考文献，下方注明"（采用GB/T 7714标准格式，不少于20篇）"
            - 致谢使用二级标题：## 致谢，下方注明"（约300-500字，感谢导师、老师、同学、家人等）"
            - 参考文献和致谢部分不要添加三级或四级标题
            - 只有正文章节（第一章、第二章…）才使用三级标题：### 1.1 xxx
            - 只有正文章节才使用四级标题：#### 1.1.1 xxx
            - 大纲末尾添加"---"分隔线，然后给出各部分的建议字数分配，格式如：
              摘要：约300-500字
              第一章 绪论：约800-1000字
              第二章 xxx：约1500-2000字
              ...以此类推
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
        List<String> sections = parseOutlineSections(request.getOutline());

        if (sections.size() <= 1) {
            String systemPrompt = buildPaperSystemPrompt(request);
            String userPrompt = buildPaperUserPrompt(request);
            return deepSeekService.chatStream(systemPrompt, userPrompt);
        }

        int totalWords = request.getWordCount() != null ? request.getWordCount() : 10000;
        String languageHint = buildLanguageHint(request.getLanguages());
        String lang = formatLanguagesForPrompt(request.getLanguages());
        int sectionCount = sections.size();

        StringBuilder previousContent = new StringBuilder();

        return Flux.range(0, sectionCount)
                .concatMap(idx -> {
                    String section = sections.get(idx);
                    int sectionWords = estimateSectionWords(section, sectionCount, totalWords, idx);
                    String sysPrompt = buildSectionSystemPrompt(request, section, idx, sectionCount, languageHint);
                    String usrPrompt = buildSectionUserPrompt(request, section, sectionWords, lang, previousContent.toString());

                    System.out.println("[PaperService] 开始生成第 " + (idx + 1) + "/" + sectionCount + " 节: " + section.split("\\n")[0]);

                    Flux<String> sectionFlux = deepSeekService.chatStream(sysPrompt, usrPrompt)
                            .doOnNext(chunk -> previousContent.append(chunk));

                    if (idx > 0) {
                        return Flux.just("\n\n").concatWith(sectionFlux);
                    }
                    return sectionFlux;
                });
    }

    public Mono<String> generatePaper(PaperGenerateRequest request) {
        String systemPrompt = buildPaperSystemPrompt(request);
        String userPrompt = buildPaperUserPrompt(request);
        return deepSeekService.chat(systemPrompt, userPrompt);
    }

    private List<String> parseOutlineSections(String outline) {
        if (outline == null || outline.isBlank()) {
            return List.of();
        }

        // 预处理：截断大纲末尾的"字数分配建议"等非正文内容
        String cleanedOutline = removeTrailingNotes(outline);

        List<String> sections = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        String[] lines = cleanedOutline.split("\\r?\\n");

        for (String line : lines) {
            if (isChapterHeading(line) && current.length() > 0) {
                sections.add(current.toString().trim());
                current = new StringBuilder();
            }
            current.append(line).append("\n");
        }
        if (current.length() > 0) {
            sections.add(current.toString().trim());
        }

        // 第一个section如果不是章节内容（只是论文标题），合并到第二个section
        if (sections.size() > 1 && !isChapterHeading(sections.get(0).split("\\n")[0])) {
            // 检查第一个section的首行是否为章节标题
            boolean firstIsChapter = false;
            for (String line : sections.get(0).split("\\n")) {
                if (isChapterHeading(line)) {
                    firstIsChapter = true;
                    break;
                }
            }
            if (!firstIsChapter) {
                sections.set(1, sections.get(0) + "\n" + sections.get(1));
                sections.remove(0);
            }
        }

        // 确保有摘要章节
        boolean hasAbstract = sections.stream().anyMatch(s -> {
            String lower = s.toLowerCase();
            return lower.contains("摘要") || lower.contains("abstract");
        });
        if (!hasAbstract && !sections.isEmpty()) {
            sections.add(0, "摘要");
        }

        System.out.println("[PaperService] 大纲解析为 " + sections.size() + " 个章节:");
        for (int i = 0; i < sections.size(); i++) {
            String firstLine = sections.get(i).split("\\n")[0];
            System.out.println("  [" + (i + 1) + "] " + firstLine);
        }

        return sections;
    }

    /**
     * 移除大纲末尾的非正文内容（字数分配建议、注意事项等）
     */
    private String removeTrailingNotes(String outline) {
        String[] lines = outline.split("\\r?\\n");
        int cutIndex = lines.length;

        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim().replaceAll("[#*\\s]+", "").replaceAll("[*:：]+$", "").trim();
            // 检测"字数分配"、"字数建议"、"注意事项"等非正文标记
            if (trimmed.contains("字数分配") || trimmed.contains("字数建议") ||
                trimmed.contains("注意事项") || trimmed.contains("写作建议") ||
                trimmed.matches(".*\\d+[字].*\\d+[字].*")) {
                // 往回找到这个段落的标题行（可能是 --- 分隔线或 ** 加粗标题）
                int start = i;
                while (start > 0 && lines[start - 1].trim().matches("^[-—=]{3,}$|^$")) {
                    start--;
                }
                cutIndex = start;
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cutIndex; i++) {
            sb.append(lines[i]).append("\n");
        }
        return sb.toString();
    }

    private boolean isChapterHeading(String line) {
        String trimmed = line.trim();

        // 列表项（以 * - + 数字. 开头）不是章节标题
        if (trimmed.matches("^[*\\-+]\\s.*") || trimmed.matches("^\\d+\\.\\s.*")) {
            return false;
        }

        // 去除所有markdown标记：#、*（加粗）、空格等，获取纯文本
        String plain = trimmed
                .replaceAll("^#+\\s*", "")   // 去掉 # ## ### 前缀
                .replaceAll("^\\*+", "")     // 去掉开头的 **
                .replaceAll("\\*+$", "")     // 去掉结尾的 **
                .trim();

        // 匹配章级标题
        if (plain.matches("^第[一二三四五六七八九十百\\d]+章.*") ||
            plain.matches("^摘\\s*要.*") ||
            plain.matches("^Abstract.*") ||
            plain.matches("^参考文献.*") ||
            plain.matches("^致\\s*谢.*") ||
            plain.matches("^引\\s*言.*") ||
            plain.matches("^绪\\s*论.*") ||
            plain.matches("^结\\s*论.*")) {
            return true;
        }

        return false;
    }

    private int estimateSectionWords(String section, int totalSections, int totalWords, int index) {
        String trimmed = section.trim().toLowerCase();
        if (trimmed.contains("摘要") || trimmed.contains("abstract")) {
            return Math.max(300, totalWords / 15);
        }
        if (trimmed.contains("致谢")) {
            return 500;
        }
        if (trimmed.contains("参考文献")) {
            return 800;
        }
        if (trimmed.contains("附录")) {
            return Math.max(500, totalWords / 10);
        }
        int contentSections = Math.max(1, totalSections - 3);
        return totalWords / contentSections;
    }

    private String buildSectionSystemPrompt(PaperGenerateRequest request, String section,
                                             int sectionIndex, int totalSections, String languageHint) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            你是一位专业的学术论文写作专家。你正在分章节撰写一篇学术论文。
            请根据提供的章节大纲，撰写该章节的完整内容。
            
            写作要求：
            1. 严格按照提供的章节大纲结构进行写作，大纲中的每一个小节、子节都必须完整体现，不得遗漏
            2. 语言专业、准确，符合学术论文规范
            3. 论述有理有据，逻辑严密
            4. 只输出当前章节的内容，不要输出其他章节
            5. 每个章节必须以该章的标题开头（如“## 摘要”、“## 第一章 绪论”、“## 参考文献”等），使用markdown二级标题格式
            """);

        if (section.contains("摘要") || section.contains("Abstract") || section.contains("abstract")) {
            sb.append("6. 包含中文摘要和关键词，紧接着必须包含对应的英文摘要（Abstract）和英文关键词（Keywords），英文摘要是中文摘要的直译\n");
        }

        if (section.contains("参考文献")) {
            sb.append("6. 生成真实可查的参考文献，格式使用GB/T 7714标准，至少20条\n");
        }

        if (section.contains("致谢")) {
            sb.append("6. 撰写完整的致谢内容（约300-500字），感谢导师的指导、学校老师的教诲、同学朋友的帮助、家人的支持等，语言真挚诚恳，符合学术论文致谢规范\n");
        }

        if (!languageHint.isBlank()) {
            sb.append(languageHint).append("\n");
        }

        if (Boolean.TRUE.equals(request.getIncludeCharts())) {
            sb.append("适当加入数据表格，使用markdown表格格式\n");
        }
        if (Boolean.TRUE.equals(request.getIncludeImages())) {
            sb.append("适当加入插图，使用markdown图片语法 ![图注](https://picsum.photos/seed/fig1/800/400)\n");
        }
        if (Boolean.TRUE.equals(request.getIncludeFormulas())) {
            sb.append("适当加入数学公式，使用LaTeX格式\n");
        }
        if (Boolean.TRUE.equals(request.getIncludeCode())) {
            sb.append("适当加入代码示例，使用markdown代码块\n");
        }

        sb.append("\n请直接输出该章节内容，使用markdown格式。不要输出与当前章节无关的内容。");
        return sb.toString();
    }

    private String buildSectionUserPrompt(PaperGenerateRequest request, String sectionOutline,
                                           int sectionWords, String lang, String previousContent) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("""
            论文题目：%s
            论文类型：%s
            学科领域：%s
            写作语言：%s
            
            当前章节大纲：
            %s
            
            该章节目标字数：约%d字
            """,
            request.getTitle(),
            request.getPaperType(),
            request.getSubject(),
            lang,
            sectionOutline,
            sectionWords
        ));

        if (previousContent != null && !previousContent.isBlank()) {
            String prev = previousContent;
            int max = 2000;
            if (prev.length() > max) {
                prev = prev.substring(prev.length() - max);
            }
            sb.append("\n前文末尾（用于衔接上下文，请勿重复）：\n");
            sb.append(prev);
            sb.append("\n");
        }

        if (request.getCustomRequirements() != null && !request.getCustomRequirements().isBlank()) {
            sb.append("\n特殊要求：").append(request.getCustomRequirements()).append("\n");
        }

        if (request.getReferenceContent() != null && !request.getReferenceContent().isBlank()) {
            sb.append("\n参考资料：").append(request.getReferenceContent()).append("\n");
        }

        sb.append("\n请撰写该章节的完整内容。");
        return sb.toString();
    }

    private String buildPaperSystemPrompt(PaperGenerateRequest request) {
        String languageHint = buildLanguageHint(request.getLanguages());
        StringBuilder sb = new StringBuilder();
        sb.append("""
            你是一位专业的学术论文写作专家，拥有丰富的学术写作经验。请根据提供的大纲和要求，撰写一篇高质量的学术论文。
            
            写作要求：
            1. 严格按照提供的大纲结构进行写作，大纲中的每一个章节、小节、子节都必须在论文正文中完整体现，不得遗漏任何一个章节
            2. 语言专业、准确，符合学术论文规范
            3. 论述有理有据，逻辑严密
            4. 适当引用文献，在文中标注引用位置
            5. 生成真实可查的参考文献，格式规范
            6. 包含中文摘要和关键词，紧接着必须包含对应的英文摘要（Abstract）和英文关键词（Keywords），英文摘要是中文摘要的直译
            7. 结尾包含致谢部分
            8. 必须一次性输出完整论文，从摘要到致谢，不得中途截断或省略任何章节
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

    private String sanitizeExportContent(String title, String markdownContent) {
        if (markdownContent == null || markdownContent.isBlank()) {
            return markdownContent;
        }

        String normalized = markdownContent.replace("\r\n", "\n").replace('\r', '\n');
        String trimmedStart = normalized.stripLeading();

        boolean looksLikeAiPreface = trimmedStart.startsWith("好的，作为")
                || trimmedStart.startsWith("好的，作为一名")
                || trimmedStart.contains("作为一名专业的学术论文写作专家")
                || trimmedStart.contains("我将严格遵循您提供的")
                || trimmedStart.contains("为您撰写这篇题为");
        if (looksLikeAiPreface) {
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("(?m)^(#{1,6})\\s+.+$")
                    .matcher(trimmedStart);
            if (m.find()) {
                trimmedStart = trimmedStart.substring(m.start()).stripLeading();
            }
        }

        if (title != null && !title.isBlank()) {
            String t = title.trim();
            java.util.regex.Matcher hm = java.util.regex.Pattern
                    .compile("(?m)^#{1,6}\\s+(.+)$")
                    .matcher(trimmedStart);
            if (hm.find() && hm.start() == 0) {
                String headingText = hm.group(1).trim();
                if (headingText.equals(t)) {
                    trimmedStart = trimmedStart.substring(hm.end()).stripLeading();
                }
            }
        }

        return trimmedStart;
    }

    public byte[] exportDocx(String title, String markdownContent) {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String content = sanitizeExportContent(title, markdownContent);
            buildDocxThesis(doc, title, content);

            // Ensure Word updates all fields (PAGEREF) when the document is opened
            try {
                org.apache.poi.xwpf.usermodel.XWPFSettings settings = doc.getSettings();
                // Use reflection to access getCTSettings() which may have protected access
                java.lang.reflect.Method m = settings.getClass().getDeclaredMethod("getCTSettings");
                m.setAccessible(true);
                org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSettings ctSettings =
                        (org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSettings) m.invoke(settings);
                if (!ctSettings.isSetUpdateFields()) {
                    ctSettings.addNewUpdateFields();
                }
                ctSettings.getUpdateFields().setVal(true);
                System.out.println("[EXPORT] updateFields set to true successfully");
            } catch (Exception ex) {
                System.out.println("[EXPORT] Failed to set updateFields: " + ex.getClass().getName() + ": " + ex.getMessage());
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

            String content = sanitizeExportContent(title, markdownContent);
            buildPdfThesis(document, pdf, title, content, font);

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
        String text = toPlainText(sanitizeExportContent(null, markdownContent));
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private enum BlockType {
        ZH_ABSTRACT,
        EN_ABSTRACT,
        TOC,
        REFERENCES,
        ACKNOWLEDGEMENT,
        HEADING_1,
        HEADING_2,
        HEADING_3,
        HEADING_4,
        TABLE,
        CODE_BLOCK,
        PARAGRAPH
    }

    private record Block(BlockType type, String text) {
    }

    // Font size constants (in pt): 三号=16, 四号=14, 小四=12, 五号=10.5(use 10)
    private static final int FONT_SAN_HAO = 16;   // 三号
    private static final int FONT_SI_HAO = 14;    // 四号
    private static final int FONT_XIAO_SI = 12;   // 小四号
    private static final int FONT_WU_HAO = 10;    // 五号 (approx 10.5pt, use 10 for integer API)

    private void buildDocxThesis(XWPFDocument doc, String title, String markdownContent) {
        List<Block> blocks = parseMarkdownBlocks(markdownContent);

        // Reset state for this export
        bookmarkIdCounter = 0;
        footerRelId = null;

        // Create footer part (not document-level default — only referenced by sections that need page numbers)
        createPageNumberFooter(doc);

        // 论文标题：二号黑体加粗居中
        if (title != null && !title.isBlank()) {
            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.CENTER);
            p.setSpacingBefore(24 * 20);
            p.setSpacingAfter(18 * 20);
            XWPFRun run = p.createRun();
            run.setBold(true);
            run.setFontFamily("黑体");
            run.setFontSize(22); // 二号
            run.setText(title.trim());
        }

        // Debug: write all blocks to a file with UTF-8 encoding
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream("debug_blocks.txt"), java.nio.charset.StandardCharsets.UTF_8))) {
            for (int di = 0; di < blocks.size(); di++) {
                Block db = blocks.get(di);
                pw.println("[BUILD] Block[" + di + "] type=" + db.type + " textLen=" + db.text.length() + " preview=" + db.text.substring(0, Math.min(120, db.text.length())).replace("\n", "\\n"));
            }
            pw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // === Pass 1: Render abstracts first (in order) ===
        // Track which block indices belong to abstract sections so Pass 3 can skip them
        java.util.Set<Integer> abstractBlockIndices = new java.util.HashSet<>();
        boolean zhTitleAdded = false;
        boolean enTitleAdded = false;
        boolean inZhSection = false;
        boolean inEnSection = false;
        // Collect all keywords across multiple abstract blocks
        String[] zhKeywords = new String[0];
        String[] enKeywords = new String[0];

        for (int i = 0; i < blocks.size(); i++) {
            Block b = blocks.get(i);
            if (b.type == BlockType.ZH_ABSTRACT) {
                inZhSection = true;
                inEnSection = false;
                abstractBlockIndices.add(i);
                if (!zhTitleAdded) {
                    // End title page section: no footer, no page number
                    addDocxSectionBreak(doc, false, false);
                    // 中文摘要："摘要"为三号黑体字居中加粗（摘要两字间空一格，段前24磅，段后18磅）
                    addDocxCenteredTitle(doc, "摘 要", "黑体", FONT_SAN_HAO, true, 24, 18);
                    zhTitleAdded = true;
                }
                String body = removeKeywordLines(b.text, true);
                if (!body.isEmpty()) {
                    addDocxBodyParagraphs(doc, body, "宋体", FONT_XIAO_SI, true, 2, 0, 0, 20);
                }
                String[] kw = extractKeywords(b.text, true);
                if (kw.length > 0) zhKeywords = kw;
            } else if (b.type == BlockType.EN_ABSTRACT) {
                // Before switching to English, output Chinese keywords if pending
                if (inZhSection && zhKeywords.length > 0) {
                    addDocxBlankLine(doc);
                    addDocxKeywords(doc, "关键词：", zhKeywords, true);
                    zhKeywords = new String[0]; // reset so we don't output again
                }
                inEnSection = true;
                inZhSection = false;
                abstractBlockIndices.add(i);
                if (!enTitleAdded) {
                    // End Chinese abstract section: with footer, reset page to 1 (ZH abstract starts from page 1)
                    addDocxSectionBreak(doc, true, true);
                    // 英文摘要："ABSTRACT"为三号Arial居中加黑，段前24磅，段后18磅
                    addDocxCenteredTitle(doc, "ABSTRACT", "Arial", FONT_SAN_HAO, true, 24, 18);
                    enTitleAdded = true;
                }
                String body = removeKeywordLines(b.text, false);
                if (!body.isEmpty()) {
                    addDocxBodyParagraphs(doc, body, "Times New Roman", FONT_XIAO_SI, true, 2, 0, 0, 20);
                }
                String[] kw = extractKeywords(b.text, false);
                if (kw.length > 0) enKeywords = kw;
            } else if (b.type == BlockType.TABLE && (inZhSection || inEnSection)) {
                // Table within abstract section — render it inline
                abstractBlockIndices.add(i);
                addDocxTable(doc, b.text);
            } else {
                inZhSection = false;
                inEnSection = false;
            }
        }
        // Output any remaining keywords
        if (zhKeywords.length > 0) {
            addDocxBlankLine(doc);
            // 中文关键词："关键词"小四号宋体加黑，顶格书写，其后为关键词（小四号宋体），各关键词之间用中文逗号分开
            addDocxKeywords(doc, "关键词", zhKeywords, true);
        }
        if (enKeywords.length > 0) {
            addDocxBlankLine(doc);
            // 英文关键词："Key words"小四号Times New Roman加黑，顶格书写，关键词小四号Times New Roman
            addDocxKeywords(doc, "Key words", enKeywords, false);
        }
        if (enTitleAdded) {
            // End English abstract section: with footer, NO reset (EN continues from ZH abstract page numbering)
            addDocxSectionBreak(doc, true, false);
        }

        // === Pass 2: Add TOC section break to end abstracts and start TOC with page reset ===
        addDocxSectionBreak(doc, true, true);
        addDocxCenteredTitle(doc, "目 录", "黑体", FONT_SAN_HAO, true, 24, 18);
        
        // === Pass 3: Collect TOC entries from blocks ===
        java.util.List<TocEntryWithBookmark> tocEntries = collectTocEntries(blocks);
        
        // === Pass 4: Insert TOC entries ===
        for (TocEntryWithBookmark entry : tocEntries) {
            addDocxTocEntryWithPageRef(doc, entry.title, entry.level, entry.bookmarkName);
        }
        
        // === Pass 5: Add section break after TOC to start main content with page reset ===
        addDocxSectionBreak(doc, true, true);
        
        System.out.println("[DEBUG] TOC entries added: " + tocEntries.size());
        System.out.println("[DEBUG] Total paragraphs after TOC: " + doc.getParagraphs().size());

        // === Pass 6: Render all remaining content ===
        int h1 = 0;
        int h2 = 0;
        int h3 = 0;

        for (int i = 0; i < blocks.size(); i++) {
            Block b = blocks.get(i);

            // Skip abstracts, TOC, and blocks already rendered in Pass 1
            if (b.type == BlockType.ZH_ABSTRACT || b.type == BlockType.EN_ABSTRACT || b.type == BlockType.TOC
                    || abstractBlockIndices.contains(i)) {
                continue;
            }

            switch (b.type) {
                case REFERENCES -> {
                    // "参考文献"三号黑体加粗居中（使用Heading1样式以显示在导航中）
                    addDocxHeadingWithBookmark(doc, "参考文献", 1, "_toc_references");
                    // 参考文献条目：五号宋体，顶格书写，单倍行距
                    addDocxReferenceItems(doc, b.text);
                }
                case ACKNOWLEDGEMENT -> {
                    // "致 谢"三号黑体居中（使用Heading1样式以显示在导航中）
                    addDocxHeadingWithBookmark(doc, "致 谢", 1, "_toc_acknowledgement");
                    // 内容：小四号宋体，首行缩进2字符，行距固定值20磅
                    addDocxBodyParagraphs(doc, b.text, "宋体", FONT_XIAO_SI, true, 2, 0, 0, 20);
                }
                case HEADING_1 -> {
                    // 章标题：三号黑体加粗居中
                    h1++;
                    h2 = 0;
                    h3 = 0;
                    String headingText = normalizeHeadingNumbering(b.text, 1, h1, h2, h3);
                    addDocxHeadingWithBookmark(doc, headingText, 1, "_toc_h" + h1);
                }
                case HEADING_2 -> {
                    // 二级标题：四号黑体加黑，顶左书写
                    if (h1 == 0) h1 = 1;
                    h2++;
                    h3 = 0;
                    String headingText = normalizeHeadingNumbering(b.text, 2, h1, h2, h3);
                    addDocxHeadingWithBookmark(doc, headingText, 2, "_toc_h" + h1 + "_" + h2);
                }
                case HEADING_3 -> {
                    // 三级标题：小四号黑体加黑，顶左书写
                    if (h1 == 0) h1 = 1;
                    if (h2 == 0) h2 = 1;
                    h3++;
                    String headingText = normalizeHeadingNumbering(b.text, 3, h1, h2, h3);
                    addDocxHeadingWithBookmark(doc, headingText, 3, "_toc_h" + h1 + "_" + h2 + "_" + h3);
                }
                case HEADING_4 -> addDocxHeading(doc, b.text, 4);
                case TABLE -> addDocxTable(doc, b.text);
                case CODE_BLOCK -> addDocxCodeBlock(doc, b.text);
                // 正文段落：小四号宋体，首行缩进2字符，行距固定值20磅
                case PARAGRAPH -> addDocxBodyParagraphs(doc, b.text, "宋体", FONT_XIAO_SI, true, 2, 0, 0, 20);
            }
        }
    }

    private void buildPdfThesis(Document document, PdfDocument pdf, String title, String markdownContent, PdfFont baseFont) {
        List<Block> blocks = parseMarkdownBlocks(markdownContent);

        PdfOutline rootOutline = pdf.getOutlines(false);
        PdfOutline lastH1 = null;
        PdfOutline lastH2 = null;
        PdfOutline lastH3 = null;

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
                    String zhBody = removeKeywordLines(b.text, true);
                    addPdfBody(document, baseFont, zhBody, 12);
                    String[] kw = extractKeywords(b.text, true);
                    if (kw.length > 0) {
                        addPdfKeywords(document, baseFont, "关键词：", kw, true);
                    }
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
                    String enBody = removeKeywordLines(b.text, false);
                    addPdfBody(document, enFont, enBody, 12);
                    String[] kw = extractKeywords(b.text, false);
                    if (kw.length > 0) {
                        addPdfKeywords(document, enFont, "Key words: ", kw, false);
                    }
                    addPdfBlankLine(document, enFont);
                }
                case TOC -> {
                    addPdfCenteredTitle(document, baseFont, "目 录", 16);
                    addPdfBody(document, baseFont, b.text, 12);
                    document.add(new com.itextpdf.layout.element.AreaBreak());
                }
                case REFERENCES -> {
                    addPdfHeading(document, pdf, baseFont, "参考文献", 1, rootOutline);
                    addPdfReferences(document, baseFont, b.text);
                }
                case ACKNOWLEDGEMENT -> {
                    addPdfHeading(document, pdf, baseFont, "致 谢", 1, rootOutline);
                    addPdfBody(document, baseFont, b.text, 12);
                }
                case HEADING_1 -> {
                    h1++;
                    h2 = 0;
                    h3 = 0;
                    String headingText = normalizeHeadingNumbering(b.text, 1, h1, h2, h3);
                    PdfOutline o = addPdfHeading(document, pdf, baseFont, headingText, 1, rootOutline);
                    lastH1 = o;
                    lastH2 = null;
                    lastH3 = null;
                }
                case HEADING_2 -> {
                    if (h1 == 0) {
                        h1 = 1;
                    }
                    h2++;
                    h3 = 0;
                    String headingText = normalizeHeadingNumbering(b.text, 2, h1, h2, h3);
                    PdfOutline parent = lastH1 != null ? lastH1 : rootOutline;
                    PdfOutline o = addPdfHeading(document, pdf, baseFont, headingText, 2, parent);
                    lastH2 = o;
                    lastH3 = null;
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
                    PdfOutline parent = lastH2 != null ? lastH2 : (lastH1 != null ? lastH1 : rootOutline);
                    PdfOutline o = addPdfHeading(document, pdf, baseFont, headingText, 3, parent);
                    lastH3 = o;
                }
                case HEADING_4 -> addPdfHeading(document, pdf, baseFont, b.text, 4, lastH3 != null ? lastH3 : (lastH2 != null ? lastH2 : rootOutline));
                case TABLE -> addPdfTable(document, baseFont, b.text);
                case CODE_BLOCK -> addPdfCodeBlock(document, baseFont, b.text);
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
        List<Block> rawBlocks = new java.util.ArrayList<>();

        String currentHeading = null;
        int currentLevel = 0;
        StringBuilder buf = new StringBuilder();
        boolean inCodeBlock = false;
        StringBuilder codeBuf = new StringBuilder();

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
                } else if (heading != null && (heading.equals("致谢") || heading.equals("致 谢"))) {
                    t = BlockType.ACKNOWLEDGEMENT;
                } else {
                    t = BlockType.PARAGRAPH;
                }
                // debug logged to file
                rawBlocks.add(new Block(t, text));
            }
            buf.setLength(0);
        };

        for (String line : lines) {
            String trimmed = line.trim();

            // Handle fenced code blocks
            if (trimmed.startsWith("```")) {
                if (!inCodeBlock) {
                    // flush current paragraph text before code block
                    String priorText = buf.toString().trim();
                    if (!priorText.isEmpty()) {
                        flush.accept(currentLevel, currentHeading);
                    }
                    inCodeBlock = true;
                    codeBuf.setLength(0);
                    continue;
                } else {
                    // end of code block
                    inCodeBlock = false;
                    String code = codeBuf.toString();
                    if (!code.trim().isEmpty()) {
                        rawBlocks.add(new Block(BlockType.CODE_BLOCK, code));
                    }
                    codeBuf.setLength(0);
                    continue;
                }
            }
            if (inCodeBlock) {
                codeBuf.append(line).append("\n");
                continue;
            }

            // Handle headings (#{1,4}), strip invisible chars (BOM, zero-width spaces) before matching
            String cleanedLine = trimmed.replaceAll("[\\uFEFF\\u200B\\u200C\\u200D\\u00A0]", "");
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("^\\s*(#{1,4})\\s*(.+)$").matcher(cleanedLine);
            if (m.matches()) {
                flush.accept(currentLevel, currentHeading);
                String hashes = m.group(1);
                String headingText = m.group(2).replaceAll("\\*\\*", "").trim();
                currentHeading = headingText;
                currentLevel = hashes.length();

                // debug logged to file
                // Skip HEADING block for special sections — they are handled by flush()
                boolean isSpecial = headingText.equals("摘要") || headingText.equals("中文摘要") || headingText.equals("摘 要")
                        || headingText.equalsIgnoreCase("ABSTRACT") || headingText.equals("英文摘要")
                        || headingText.equals("目录") || headingText.equals("目 录")
                        || headingText.equals("参考文献")
                        || headingText.equals("致谢") || headingText.equals("致 谢");
                // debug logged to file
                if (!isSpecial) {
                    // Detect chapter-level headings (第X章, 绪论, 结论) regardless of markdown level
                    boolean isChapter = headingText.matches("^第[一二三四五六七八九十百\\d]+章.*")
                            || headingText.matches("^绪\\s*论.*")
                            || headingText.matches("^结\\s*论.*");
                    BlockType t;
                    if (isChapter) {
                        t = BlockType.HEADING_1; // Always treat chapter headings as level 1
                    } else if (currentLevel <= 2) {
                        // ## non-chapter heading → treat as HEADING_1 (top-level)
                        t = BlockType.HEADING_1;
                    } else if (currentLevel == 3) {
                        t = BlockType.HEADING_2; // ### → section
                    } else {
                        t = BlockType.HEADING_3; // #### → subsection
                    }
                    rawBlocks.add(new Block(t, headingText));
                }
                continue;
            }
            buf.append(line).append("\n");
        }
        // flush unclosed code block if any
        if (inCodeBlock && codeBuf.length() > 0) {
            String code = codeBuf.toString();
            if (!code.trim().isEmpty()) {
                rawBlocks.add(new Block(BlockType.CODE_BLOCK, code));
            }
        }
        flush.accept(currentLevel, currentHeading);

        // Second pass: split combined ZH_ABSTRACT that contains embedded English abstract
        List<Block> splitBlocks = new java.util.ArrayList<>();
        for (Block b : rawBlocks) {
            if (b.type() == BlockType.ZH_ABSTRACT) {
                splitAbstractBlock(b, splitBlocks);
            } else {
                splitBlocks.add(b);
            }
        }

        // Third pass: extract tables and deduplicate special blocks
        List<Block> blocks = new java.util.ArrayList<>();
        boolean hasZhAbstract = false;
        boolean hasEnAbstract = false;
        boolean hasReferences = false;
        for (Block b : splitBlocks) {
            Block actual = b;
            // Deduplicate: only keep the first ZH_ABSTRACT, EN_ABSTRACT, REFERENCES
            if (b.type() == BlockType.ZH_ABSTRACT) {
                if (hasZhAbstract) { actual = new Block(BlockType.PARAGRAPH, b.text()); }
                else { hasZhAbstract = true; }
            } else if (b.type() == BlockType.EN_ABSTRACT) {
                if (hasEnAbstract) { actual = new Block(BlockType.PARAGRAPH, b.text()); }
                else { hasEnAbstract = true; }
            } else if (b.type() == BlockType.REFERENCES) {
                if (hasReferences) { actual = new Block(BlockType.PARAGRAPH, b.text()); }
                else { hasReferences = true; }
            }
            if (actual.type() == BlockType.PARAGRAPH || actual.type() == BlockType.ZH_ABSTRACT
                    || actual.type() == BlockType.EN_ABSTRACT || actual.type() == BlockType.REFERENCES
                    || actual.type() == BlockType.ACKNOWLEDGEMENT) {
                extractTablesFromParagraph(actual, blocks);
            } else {
                blocks.add(actual);
            }
        }
        return blocks;
    }

    /**
     * Split a ZH_ABSTRACT block that may contain an embedded English abstract.
     * The AI often generates both Chinese and English abstracts under a single "## 摘要" heading.
     * We detect the English abstract boundary by looking for lines starting with "Abstract" or
     * a line that is predominantly English text after the Chinese keywords line.
     */
    private void splitAbstractBlock(Block zhBlock, List<Block> out) {
        String text = zhBlock.text();
        String[] lines = text.split("\n", -1);

        // Strategy: find the split point where English abstract begins
        // The AI generates both Chinese and English abstracts under one "## 摘要" heading.
        // Typical structure:
        //   (Chinese abstract text...)
        //   关键词：xxx
        //   (optional table)
        //   (Chinese contribution text...)
        //   关键词：xxx  (possibly repeated)
        //   (optional table)
        //   Abstract (or directly English text)
        //   (English abstract text...)
        //   Keywords: xxx
        int splitIdx = -1;

        // Strategy 1: Look for standalone "Abstract" title line (most reliable)
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim().replaceAll("\\*\\*", "").trim();
            if (trimmed.equalsIgnoreCase("Abstract") || trimmed.equalsIgnoreCase("Abstract:")
                    || trimmed.equalsIgnoreCase("ABSTRACT") || trimmed.equalsIgnoreCase("ABSTRACT:")) {
                splitIdx = i;
                break;
            }
        }

        // Strategy 2: Look for "Keywords:" or "Key words:" line (English keywords marker)
        if (splitIdx == -1) {
            for (int i = 0; i < lines.length; i++) {
                String trimmed = lines[i].trim();
                if (trimmed.matches("(?i)^\\**\\s*Key\\s*words?\\s*[:：].*")) {
                    // Found English keywords line — scan backwards to find where English text starts
                    for (int j = i - 1; j >= 0; j--) {
                        String prev = lines[j].trim();
                        if (prev.isEmpty()) continue;
                        // If this line is NOT English-dominant, the English abstract starts at j+1
                        if (!isEnglishDominant(prev) && !prev.matches("(?i)^\\**\\s*Key\\s*words?\\s*[:：].*")) {
                            splitIdx = j + 1;
                            break;
                        }
                    }
                    if (splitIdx == -1) {
                        // All lines before Keywords are English — unlikely, but handle it
                        splitIdx = 0;
                    }
                    break;
                }
            }
        }

        // Strategy 3: After the LAST Chinese keywords line, find first English-dominant text
        if (splitIdx == -1) {
            int lastChineseKeywordsIdx = -1;
            for (int i = 0; i < lines.length; i++) {
                String trimmed = lines[i].trim();
                if (trimmed.startsWith("关键词") || trimmed.startsWith("关键字")) {
                    lastChineseKeywordsIdx = i;
                }
            }
            if (lastChineseKeywordsIdx >= 0) {
                for (int i = lastChineseKeywordsIdx + 1; i < lines.length; i++) {
                    String trimmed = lines[i].trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("|")) continue; // skip blank lines and table rows
                    if (trimmed.matches("^[A-Za-z].*") || isEnglishDominant(trimmed)) {
                        splitIdx = i;
                        break;
                    }
                }
            }
        }

        if (splitIdx > 0) {
            StringBuilder zhPart = new StringBuilder();
            StringBuilder enPart = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                if (i < splitIdx) {
                    zhPart.append(lines[i]).append("\n");
                } else {
                    enPart.append(lines[i]).append("\n");
                }
            }
            String zhText = zhPart.toString().trim();
            String enText = enPart.toString().trim();
            // Remove the "Abstract" title line from English text if present
            enText = enText.replaceFirst("(?i)^\\*{0,2}\\s*Abstract\\s*:?\\s*\\*{0,2}\\s*\n?", "").trim();

            if (!zhText.isEmpty()) {
                out.add(new Block(BlockType.ZH_ABSTRACT, zhText));
            }
            if (!enText.isEmpty()) {
                out.add(new Block(BlockType.EN_ABSTRACT, enText));
            }
        } else {
            // No English abstract found, keep as-is
            out.add(zhBlock);
        }
    }

    private boolean isEnglishDominant(String text) {
        if (text == null || text.isEmpty()) return false;
        int ascii = 0;
        int total = 0;
        for (char c : text.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                total++;
                if (c < 128) ascii++;
            }
        }
        return total > 0 && (double) ascii / total > 0.7;
    }

    private void extractTablesFromParagraph(Block block, List<Block> out) {
        String[] lines = block.text().split("\n", -1);
        StringBuilder paraBuf = new StringBuilder();
        // For special block types (abstracts, references, acknowledgement), ALL text chunks keep the original type
        boolean preserveType = block.type() == BlockType.ZH_ABSTRACT || block.type() == BlockType.EN_ABSTRACT
                || block.type() == BlockType.REFERENCES || block.type() == BlockType.ACKNOWLEDGEMENT;
        int i = 0;
        while (i < lines.length) {
            String line = lines[i];
            // Detect table: current line has |, next line is separator
            if (looksLikeTableRow(line) && i + 1 < lines.length && MD_TABLE_SEPARATOR.matcher(lines[i + 1]).matches()) {
                // flush prior paragraph text
                String prior = paraBuf.toString().trim();
                if (!prior.isEmpty()) {
                    out.add(new Block(block.type(), prior));
                }
                paraBuf.setLength(0);

                // collect all table rows
                StringBuilder tableBuf = new StringBuilder();
                while (i < lines.length && (looksLikeTableRow(lines[i]) || MD_TABLE_SEPARATOR.matcher(lines[i]).matches())) {
                    tableBuf.append(lines[i]).append("\n");
                    i++;
                }
                out.add(new Block(BlockType.TABLE, tableBuf.toString().trim()));
                continue;
            }
            paraBuf.append(line).append("\n");
            i++;
        }
        String remaining = paraBuf.toString().trim();
        if (!remaining.isEmpty()) {
            out.add(new Block(block.type(), remaining));
        }
    }

    private String normalizeHeadingNumbering(String heading, int level, int h1, int h2, int h3) {
        if (heading == null) {
            return "";
        }
        String t = heading.trim();
        // Already has numeric numbering like "1.1 xxx" or "1.1xxx" (with or without space)
        if (t.matches("^\\d+(\\.\\d+){0,2}\\s*\\S.*")) {
            return t;
        }
        // Already has chapter-style numbering like "第一章", "第1章", "第二章 xxx"
        if (t.matches("^第[一二三四五六七八九十百\\d]+章.*")) {
            return t;
        }
        // Don't add numbering for level-1 headings (chapter titles)
        if (level == 1) {
            return t;
        }
        return switch (level) {
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
        // 顶格书写，行距固定值20磅
        p.setSpacingBefore(0);
        p.setSpacingAfter(0);
        p.setSpacingBetween(20, LineSpacingRule.EXACT);
        p.setFirstLineIndent(0); // 顶格

        String fontFamily = chineseComma ? "宋体" : "Times New Roman";

        // "关键词："/"Key words: " 小四号加黑
        XWPFRun labelRun = p.createRun();
        labelRun.setFontFamily(fontFamily);
        labelRun.setFontSize(FONT_XIAO_SI);
        labelRun.setBold(true);
        labelRun.setText(label + (chineseComma ? "：" : ": "));

        // 关键词内容 小四号不加黑
        XWPFRun kwRun = p.createRun();
        kwRun.setFontFamily(fontFamily);
        kwRun.setFontSize(FONT_XIAO_SI);
        kwRun.setBold(false);

        String joined = joinKeywords(keywords, chineseComma);
        if (!joined.isBlank()) {
            kwRun.setText(joined);
        }
    }

    // Counter for unique bookmark IDs
    private int bookmarkIdCounter = 0;

    private void addDocxHeading(XWPFDocument doc, String heading, int level) {
        addDocxHeadingWithBookmark(doc, heading, level, null);
    }

    private void addDocxHeadingWithBookmark(XWPFDocument doc, String heading, int level, String bookmarkName) {
        XWPFParagraph p = doc.createParagraph();

        // Use built-in heading styles for TOC compatibility in both Word/WPS.
        // We still override alignment/font/spacing below to keep thesis formatting.
        String styleName = switch (level) {
            case 1 -> "Heading1";
            case 2 -> "Heading2";
            case 3 -> "Heading3";
            default -> "Heading4";
        };
        p.setStyle(styleName);

        // Set outline level via XML for navigation pane hierarchy (all levels)
        CTP ctp = p.getCTP();
        if (!ctp.isSetPPr()) {
            ctp.addNewPPr();
        }
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr pPr = ctp.getPPr();
        CTDecimalNumber outlineLvl = pPr.isSetOutlineLvl() ? pPr.getOutlineLvl() : pPr.addNewOutlineLvl();
        outlineLvl.setVal(BigInteger.valueOf(Math.max(0, Math.min(8, level - 1))));

        // 章标题(level 1)：三号黑体加粗居中，段前24磅段后18磅
        // 二级标题(level 2)：四号黑体加黑，顶左书写，段前段后适当
        // 三级标题(level 3)：小四黑体加黑，顶左书写
        if (level == 1) {
            p.setAlignment(ParagraphAlignment.CENTER);
            p.setSpacingBefore(24 * 20);
            p.setSpacingAfter(18 * 20);
        } else {
            p.setAlignment(ParagraphAlignment.LEFT);
            p.setSpacingBefore(12 * 20);
            p.setSpacingAfter(6 * 20);
        }

        // Add bookmark for TOC hyperlink if bookmarkName is provided
        if (bookmarkName != null) {
            int bmId = bookmarkIdCounter++;
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark bmStart = ctp.addNewBookmarkStart();
            bmStart.setId(BigInteger.valueOf(bmId));
            bmStart.setName(bookmarkName);
        }

        XWPFRun run = p.createRun();
        run.setBold(true);
        run.setFontFamily("黑体");
        // 章标题三号(16pt)，二级标题四号(14pt)，三级标题小四(12pt)
        run.setFontSize(level == 1 ? FONT_SAN_HAO : (level == 2 ? FONT_SI_HAO : FONT_XIAO_SI));
        run.setText(heading);

        // Close bookmark
        if (bookmarkName != null) {
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTMarkupRange bmEnd = ctp.addNewBookmarkEnd();
            bmEnd.setId(BigInteger.valueOf(bookmarkIdCounter - 1));
        }
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
            // 五号宋体，顶格书写，单倍行距
            p.setSpacingBetween(1.0, LineSpacingRule.AUTO);
            p.setSpacingBefore(0);
            p.setSpacingAfter(0);
            XWPFRun run = p.createRun();
            run.setFontFamily("宋体");
            run.setFontSize(FONT_WU_HAO);
            run.setText(t);
        }
    }

    private java.util.List<TocEntryWithBookmark> collectTocEntries(List<Block> blocks) {
        java.util.List<TocEntryWithBookmark> tocEntries = new java.util.ArrayList<>();
        int h1 = 0;
        int h2 = 0;
        int h3 = 0;

        for (Block b : blocks) {
            String tocTitle_text = null;
            int level = 0;
            String bookmarkName = null;

            switch (b.type) {
                case HEADING_1 -> {
                    h1++;
                    h2 = 0;
                    h3 = 0;
                    tocTitle_text = normalizeHeadingNumbering(b.text, 1, h1, h2, h3);
                    level = 1;
                    bookmarkName = "_toc_h" + h1;
                }
                case HEADING_2 -> {
                    if (h1 == 0) h1 = 1;
                    h2++;
                    h3 = 0;
                    tocTitle_text = normalizeHeadingNumbering(b.text, 2, h1, h2, h3);
                    level = 2;
                    bookmarkName = "_toc_h" + h1 + "_" + h2;
                }
                case HEADING_3 -> {
                    if (h1 == 0) h1 = 1;
                    if (h2 == 0) h2 = 1;
                    h3++;
                    tocTitle_text = normalizeHeadingNumbering(b.text, 3, h1, h2, h3);
                    level = 3;
                    bookmarkName = "_toc_h" + h1 + "_" + h2 + "_" + h3;
                }
                case REFERENCES -> {
                    tocTitle_text = "参考文献";
                    level = 1;
                    bookmarkName = "_toc_references";
                }
                case ACKNOWLEDGEMENT -> {
                    tocTitle_text = "致 谢";
                    level = 1;
                    bookmarkName = "_toc_acknowledgement";
                }
                default -> {}
            }

            if (tocTitle_text != null && bookmarkName != null) {
                tocEntries.add(new TocEntryWithBookmark(tocTitle_text, level, bookmarkName));
            }
        }
        
        return tocEntries;
    }

    private static class TocEntryWithBookmark {
        String title;
        int level;
        String bookmarkName;
        TocEntryWithBookmark(String title, int level, String bookmarkName) {
            this.title = title;
            this.level = level;
            this.bookmarkName = bookmarkName;
        }
    }

    private void addDocxTocEntryWithPageRef(XWPFDocument doc, String title, int level, String bookmarkName) {
        XWPFParagraph p = doc.createParagraph();
        p.setStyle("TOC" + Math.max(1, Math.min(3, level)));
        p.setSpacingBetween(1.0, LineSpacingRule.AUTO);
        p.setSpacingBefore(0);
        p.setSpacingAfter(0);

        // 目录格式规范：章目录四号宋体，一级小节小四号宋体左缩进1字，二级小节五号宋体左缩进2字
        int indentTwips = 0;
        int fontSize = 14; // 四号 = 14pt
        if (level == 2) {
            indentTwips = 420; // 左缩进1个字符（约420 twips）
            fontSize = 12; // 小四号 = 12pt
        } else if (level >= 3) {
            indentTwips = 840; // 左缩进2个字符（约840 twips）
            fontSize = 10; // 五号 = 10.5pt，这里用10
        }
        if (indentTwips > 0) {
            p.setIndentationLeft(indentTwips);
        }

        CTP ctp = p.getCTP();
        if (!ctp.isSetPPr()) ctp.addNewPPr();
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr pPr = ctp.getPPr();
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTabs tabs = pPr.isSetTabs() ? pPr.getTabs() : pPr.addNewTabs();
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTabStop tab = tabs.addNewTab();
        tab.setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STTabJc.RIGHT);
        tab.setLeader(org.openxmlformats.schemas.wordprocessingml.x2006.main.STTabTlc.DOT);
        tab.setPos(BigInteger.valueOf(8200 - indentTwips));

        XWPFRun titleRun = p.createRun();
        titleRun.setFontFamily("宋体");
        titleRun.setFontSize(fontSize);
        titleRun.setText(title);

        XWPFRun tabRun = p.createRun();
        tabRun.addTab();

        // Use PAGEREF field to reference the bookmark's actual page number
        CTP ctpForPageRef = p.getCTP();
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSimpleField pageRefField = ctpForPageRef.addNewFldSimple();
        pageRefField.setInstr(" PAGEREF " + bookmarkName + " \\h ");
        
        // Add a run inside the field with placeholder text (will be updated by Word)
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR fieldRun = pageRefField.addNewR();
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr fieldRPr = fieldRun.addNewRPr();
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts fieldFonts = fieldRPr.addNewRFonts();
        fieldFonts.setAscii("宋体");
        fieldFonts.setEastAsia("宋体");
        fieldFonts.setHAnsi("宋体");
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHpsMeasure fieldSz = fieldRPr.addNewSz();
        fieldSz.setVal(BigInteger.valueOf(fontSize * 2));
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHpsMeasure fieldSzCs = fieldRPr.addNewSzCs();
        fieldSzCs.setVal(BigInteger.valueOf(fontSize * 2));
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText fieldText = fieldRun.addNewT();
        fieldText.setStringValue("1");
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

    // Footer relation IDs — created once, referenced by sections that need page numbers
    private String footerRelId = null;

    /**
     * Create a footer part containing a centered PAGE field.
     * Does NOT set it as the document default — sections must explicitly reference it.
     */
    private void createPageNumberFooter(XWPFDocument doc) {
        try {
            // Create footer with PAGE field
            org.apache.poi.xwpf.usermodel.XWPFFooter footer =
                    doc.createFooter(org.apache.poi.wp.usermodel.HeaderFooterType.DEFAULT);
            XWPFParagraph p = footer.createParagraph();
            p.setAlignment(ParagraphAlignment.CENTER);

            CTP ctp = p.getCTP();
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR r1 = ctp.addNewR();
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr rPr = r1.addNewRPr();
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHpsMeasure sz = rPr.addNewSz();
            sz.setVal(BigInteger.valueOf(20)); // 10pt
            r1.addNewFldChar().setFldCharType(org.openxmlformats.schemas.wordprocessingml.x2006.main.STFldCharType.BEGIN);
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR r2 = ctp.addNewR();
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText instrText = r2.addNewInstrText();
            instrText.setStringValue(" PAGE ");
            instrText.setSpace(org.apache.xmlbeans.impl.xb.xmlschema.SpaceAttribute.Space.PRESERVE);
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR r3 = ctp.addNewR();
            r3.addNewFldChar().setFldCharType(org.openxmlformats.schemas.wordprocessingml.x2006.main.STFldCharType.END);

            footerRelId = doc.getRelationId(footer);

            // IMPORTANT: doc.createFooter(DEFAULT) auto-adds a footer reference to the document
            // body sectPr, which makes it apply to ALL sections including the title page.
            // Remove it so only sections that explicitly reference it will show page numbers.
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody ctBody = doc.getDocument().getBody();
            if (ctBody.isSetSectPr()) {
                org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr bodySectPr = ctBody.getSectPr();
                // Remove all footer references from body sectPr
                for (int i = bodySectPr.sizeOfFooterReferenceArray() - 1; i >= 0; i--) {
                    bodySectPr.removeFooterReference(i);
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Add a section break. The sectPr is attached to the LAST existing paragraph in the document
     * (not a new empty paragraph), to avoid creating extra blank space or duplicate footers.
     *
     * @param showPageNumber whether this section should display page numbers in footer
     * @param resetPageNumber whether to reset page numbering to 1 for this section
     */
    private void addDocxSectionBreakAtParagraph(XWPFDocument doc, int paragraphIndex) {
        // Add section break at the specified paragraph index
        java.util.List<XWPFParagraph> paragraphs = doc.getParagraphs();
        if (paragraphIndex < 0 || paragraphIndex >= paragraphs.size()) {
            return; // Invalid index
        }
        
        XWPFParagraph para = paragraphs.get(paragraphIndex);
        CTP ctp = para.getCTP();
        if (!ctp.isSetPPr()) ctp.addNewPPr();
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr sectPr = ctp.getPPr().addNewSectPr();
        sectPr.addNewType().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STSectionMark.NEXT_PAGE);
        
        // Reset page number to 1 for main content
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageNumber pgNum = sectPr.addNewPgNumType();
        pgNum.setStart(BigInteger.ONE);
        
        // Show page number with footer
        if (footerRelId != null) {
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHdrFtrRef footerRef = sectPr.addNewFooterReference();
            footerRef.setType(org.openxmlformats.schemas.wordprocessingml.x2006.main.STHdrFtr.DEFAULT);
            footerRef.setId(footerRelId);
        }
    }

    private void addDocxSectionBreak(XWPFDocument doc, boolean showPageNumber, boolean resetPageNumber) {
        java.util.List<XWPFParagraph> paragraphs = doc.getParagraphs();
        XWPFParagraph lastPara;
        if (paragraphs.isEmpty()) {
            lastPara = doc.createParagraph();
        } else {
            lastPara = paragraphs.get(paragraphs.size() - 1);
        }
        CTP ctp = lastPara.getCTP();
        if (!ctp.isSetPPr()) ctp.addNewPPr();
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr sectPr = ctp.getPPr().addNewSectPr();
        sectPr.addNewType().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STSectionMark.NEXT_PAGE);

        if (resetPageNumber) {
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageNumber pgNum = sectPr.addNewPgNumType();
            pgNum.setStart(BigInteger.ONE);
        }

        if (showPageNumber && footerRelId != null) {
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHdrFtrRef footerRef = sectPr.addNewFooterReference();
            footerRef.setType(org.openxmlformats.schemas.wordprocessingml.x2006.main.STHdrFtr.DEFAULT);
            footerRef.setId(footerRelId);
        }
    }

    private void addDocxTable(XWPFDocument doc, String tableMarkdown) {
        String[] lines = tableMarkdown.split("\n", -1);
        java.util.List<String[]> dataRows = new java.util.ArrayList<>();
        for (String line : lines) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            if (MD_TABLE_SEPARATOR.matcher(t).matches()) continue;
            dataRows.add(splitTableRow(t));
        }
        if (dataRows.isEmpty()) return;

        int cols = 0;
        for (String[] row : dataRows) {
            cols = Math.max(cols, row.length);
        }
        if (cols == 0) return;

        org.apache.poi.xwpf.usermodel.XWPFTable table = doc.createTable(dataRows.size(), cols);
        table.setWidth("100%");

        // Set table borders
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr tblPr = table.getCTTbl().getTblPr();
        if (tblPr == null) tblPr = table.getCTTbl().addNewTblPr();
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders borders = tblPr.isSetTblBorders() ? tblPr.getTblBorders() : tblPr.addNewTblBorders();
        for (var border : new org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder[]{
                borders.isSetTop() ? borders.getTop() : borders.addNewTop(),
                borders.isSetBottom() ? borders.getBottom() : borders.addNewBottom(),
                borders.isSetLeft() ? borders.getLeft() : borders.addNewLeft(),
                borders.isSetRight() ? borders.getRight() : borders.addNewRight(),
                borders.isSetInsideH() ? borders.getInsideH() : borders.addNewInsideH(),
                borders.isSetInsideV() ? borders.getInsideV() : borders.addNewInsideV()
        }) {
            border.setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
            border.setSz(BigInteger.valueOf(4));
            border.setColor("000000");
        }

        for (int r = 0; r < dataRows.size(); r++) {
            String[] cells = dataRows.get(r);
            org.apache.poi.xwpf.usermodel.XWPFTableRow row = table.getRow(r);
            for (int c = 0; c < cols; c++) {
                org.apache.poi.xwpf.usermodel.XWPFTableCell cell = row.getCell(c);
                if (cell == null) cell = row.addNewTableCell();
                String cellText = c < cells.length ? cells[c].trim() : "";
                // Clear default empty paragraph
                if (cell.getParagraphs().size() > 0) {
                    XWPFParagraph cp = cell.getParagraphs().get(0);
                    cp.setAlignment(ParagraphAlignment.CENTER);
                    cp.setSpacingBefore(0);
                    cp.setSpacingAfter(0);
                    XWPFRun run = cp.createRun();
                    run.setFontFamily("宋体");
                    run.setFontSize(10);
                    if (r == 0) run.setBold(true);
                    run.setText(cellText);
                } else {
                    cell.setText(cellText);
                }
            }
        }
        addDocxBlankLine(doc);
    }

    private void addDocxCodeBlock(XWPFDocument doc, String code) {
        String[] lines = code.split("\n", -1);
        // Remove trailing empty lines
        int end = lines.length;
        while (end > 0 && lines[end - 1].trim().isEmpty()) end--;

        for (int i = 0; i < end; i++) {
            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.LEFT);
            p.setSpacingBefore(0);
            p.setSpacingAfter(0);
            p.setSpacingBetween(14, LineSpacingRule.EXACT);

            // Set gray background shading
            CTP ctp = p.getCTP();
            if (!ctp.isSetPPr()) ctp.addNewPPr();
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd shd;
            if (ctp.getPPr().isSetShd()) {
                shd = ctp.getPPr().getShd();
            } else {
                shd = ctp.getPPr().addNewShd();
            }
            shd.setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd.CLEAR);
            shd.setFill("F0F0F0");

            XWPFRun run = p.createRun();
            run.setFontFamily("Consolas");
            run.setFontSize(9);
            run.setText(lines[i].isEmpty() ? " " : lines[i]);
        }
        addDocxBlankLine(doc);
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

    private PdfOutline addPdfHeading(Document document, PdfDocument pdf, PdfFont font, String heading, int level, PdfOutline parentOutline) {
        Paragraph p = new Paragraph(heading)
                .setFont(font)
                .setBold()
                .setFontSize(level == 1 ? 16 : (level == 2 ? 14 : 12))
                .setTextAlignment(level == 1 ? com.itextpdf.layout.properties.TextAlignment.CENTER : com.itextpdf.layout.properties.TextAlignment.LEFT)
                .setMarginTop(12)
                .setMarginBottom(6);
        document.add(p);

        PdfOutline outline = parentOutline.addOutline(heading);
        PdfPage page = pdf.getLastPage();
        if (page != null) {
            float top = page.getPageSize().getTop();
            outline.addDestination(PdfExplicitDestination.createFitH(page, top));
        }
        return outline;
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

    private void addPdfTable(Document document, PdfFont font, String tableMarkdown) {
        String[] lines = tableMarkdown.split("\n", -1);
        java.util.List<String[]> dataRows = new java.util.ArrayList<>();
        for (String line : lines) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            if (MD_TABLE_SEPARATOR.matcher(t).matches()) continue;
            dataRows.add(splitTableRow(t));
        }
        if (dataRows.isEmpty()) return;

        int cols = 0;
        for (String[] row : dataRows) {
            cols = Math.max(cols, row.length);
        }
        if (cols == 0) return;

        float[] colWidths = new float[cols];
        java.util.Arrays.fill(colWidths, 1f);
        com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(com.itextpdf.layout.properties.UnitValue.createPercentArray(colWidths))
                .useAllAvailableWidth()
                .setMarginTop(6)
                .setMarginBottom(6);

        for (int r = 0; r < dataRows.size(); r++) {
            String[] cells = dataRows.get(r);
            for (int c = 0; c < cols; c++) {
                String cellText = c < cells.length ? cells[c].trim() : "";
                com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph(cellText).setFont(font).setFontSize(10).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))
                        .setPadding(4);
                if (r == 0) {
                    cell.setBold();
                    cell.setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(240, 240, 240));
                }
                table.addCell(cell);
            }
        }
        document.add(table);
    }

    private void addPdfCodeBlock(Document document, PdfFont font, String code) {
        String[] lines = code.split("\n", -1);
        int end = lines.length;
        while (end > 0 && lines[end - 1].trim().isEmpty()) end--;

        com.itextpdf.layout.element.Div codeDiv = new com.itextpdf.layout.element.Div()
                .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(240, 240, 240))
                .setPadding(8)
                .setMarginTop(6)
                .setMarginBottom(6);

        for (int i = 0; i < end; i++) {
            Paragraph p = new Paragraph(lines[i].isEmpty() ? " " : lines[i])
                    .setFont(font)
                    .setFontSize(9)
                    .setFixedLeading(14)
                    .setMarginTop(0)
                    .setMarginBottom(0);
            codeDiv.add(p);
        }
        document.add(codeDiv);
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
                    String rest = t.replaceFirst("(?i)key\\s*words", "");
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
        // Support comma and semicolon delimiters (both Chinese and English)
        String[] parts = cleaned.split("[，,；;]+", -1);
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

    private String removeKeywordLines(String text, boolean chinese) {
        if (text == null || text.isBlank()) return "";
        String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String t = line.trim();
            if (chinese && (t.startsWith("关键词") || t.startsWith("关键字"))) continue;
            if (!chinese && (t.toLowerCase().startsWith("key words") || t.toLowerCase().startsWith("keywords"))) continue;
            sb.append(line).append("\n");
        }
        return sb.toString().trim();
    }

    private static final Pattern MD_FENCED_CODE_BLOCK = Pattern.compile("(?s)```[^\n]*\n(.*?)\n```", Pattern.MULTILINE);
    private static final Pattern MD_INLINE_CODE = Pattern.compile("`([^`]*)`");
    private static final Pattern MD_IMAGE = Pattern.compile("!\\[([^\\]]*)\\]\\([^\\)]*\\)");
    private static final Pattern MD_LINK = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)");
    private static final Pattern MD_BOLD_ITALIC = Pattern.compile("(\\*\\*|__|\\*|_)");
    private static final Pattern MD_HEADING = Pattern.compile("(?m)^\\s{0,3}#{1,6}\\s*");
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
            raw[i] = stripInlineMarkdown(raw[i].trim());
        }
        return raw;
    }

    private String stripInlineMarkdown(String text) {
        if (text == null || text.isEmpty()) return text;
        // Remove bold/italic markers: **, __, *, _
        String s = text.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        s = s.replaceAll("__(.+?)__", "$1");
        s = s.replaceAll("\\*(.+?)\\*", "$1");
        s = s.replaceAll("_(.+?)_", "$1");
        // Remove inline code backticks
        s = s.replaceAll("`([^`]*)`", "$1");
        return s.trim();
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
