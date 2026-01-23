package com.yibiai.thesis.service;

import com.yibiai.thesis.dto.OutlineRequest;
import com.yibiai.thesis.dto.PaperGenerateRequest;
import com.yibiai.thesis.entity.Paper;
import com.yibiai.thesis.repository.PaperRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        String systemPrompt = """
            你是一位专业的学术论文写作专家。请根据用户提供的论文题目、类型、学科和字数要求，生成一份详细的三级论文大纲。
            大纲要求：
            1. 结构完整，包含摘要、引言、正文（多章节）、结论、参考文献等部分
            2. 每个章节下设2-4个小节，小节下可设具体论点
            3. 内容专业、逻辑清晰、层次分明
            4. 符合学术论文写作规范
            5. 字数分配合理
            请直接输出大纲内容，使用markdown格式。
            """;

        String userPrompt = String.format("""
            论文题目：%s
            论文类型：%s
            学科领域：%s
            目标字数：%d字
            %s
            %s
            
            请生成详细的三级论文大纲。
            """,
            request.getTitle(),
            request.getPaperType(),
            request.getSubject(),
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

        if (Boolean.TRUE.equals(request.getIncludeCharts())) {
            sb.append("8. 适当加入数据表格，使用markdown表格格式\n");
        }
        if (Boolean.TRUE.equals(request.getIncludeFormulas())) {
            sb.append("9. 适当加入数学公式，使用LaTeX格式\n");
        }
        if (Boolean.TRUE.equals(request.getIncludeCode())) {
            sb.append("10. 适当加入代码示例，使用markdown代码块\n");
        }

        sb.append("\n请直接输出论文内容，使用markdown格式。");
        return sb.toString();
    }

    private String buildPaperUserPrompt(PaperGenerateRequest request) {
        return String.format("""
            论文题目：%s
            论文类型：%s
            学科领域：%s
            目标字数：%d字
            
            论文大纲：
            %s
            
            %s
            %s
            
            请根据以上大纲，撰写完整的学术论文。
            """,
            request.getTitle(),
            request.getPaperType(),
            request.getSubject(),
            request.getWordCount(),
            request.getOutline(),
            request.getCustomRequirements() != null ? "特殊要求：" + request.getCustomRequirements() : "",
            request.getReferenceContent() != null ? "参考资料内容：" + request.getReferenceContent() : ""
        );
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
}
