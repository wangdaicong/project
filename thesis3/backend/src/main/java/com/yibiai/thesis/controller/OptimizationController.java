package com.yibiai.thesis.controller;

import com.yibiai.thesis.dto.ApiResponse;
import com.yibiai.thesis.dto.OptimizationCreateRequest;
import com.yibiai.thesis.dto.OptimizationSegmentDto;
import com.yibiai.thesis.entity.OptimizationChangeLog;
import com.yibiai.thesis.entity.OptimizationSegment;
import com.yibiai.thesis.entity.OptimizationSession;
import com.yibiai.thesis.repository.OptimizationChangeLogRepository;
import com.yibiai.thesis.repository.OptimizationSegmentRepository;
import com.yibiai.thesis.repository.OptimizationSessionRepository;
import com.yibiai.thesis.service.ConcurrencyManager;
import com.yibiai.thesis.service.OptimizationService;
import com.yibiai.thesis.service.PaperService;
import com.yibiai.thesis.service.StreamManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/optimization")
public class OptimizationController {

    private final OptimizationSessionRepository sessionRepository;
    private final OptimizationSegmentRepository segmentRepository;
    private final OptimizationChangeLogRepository changeLogRepository;
    private final OptimizationService optimizationService;
    private final PaperService paperService;
    private final StreamManager streamManager;
    private final ConcurrencyManager concurrencyManager;

    public OptimizationController(
            OptimizationSessionRepository sessionRepository,
            OptimizationSegmentRepository segmentRepository,
            OptimizationChangeLogRepository changeLogRepository,
            OptimizationService optimizationService,
            PaperService paperService,
            StreamManager streamManager,
            ConcurrencyManager concurrencyManager
    ) {
        this.sessionRepository = sessionRepository;
        this.segmentRepository = segmentRepository;
        this.changeLogRepository = changeLogRepository;
        this.optimizationService = optimizationService;
        this.paperService = paperService;
        this.streamManager = streamManager;
        this.concurrencyManager = concurrencyManager;
    }

    @PostMapping("/start")
    public ApiResponse<OptimizationSession> start(@RequestBody OptimizationCreateRequest req) {
        if (req.getOriginalText() == null || req.getOriginalText().isBlank()) {
            return ApiResponse.error(400, "originalText不能为空");
        }

        OptimizationSession s = new OptimizationSession();
        s.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        s.setOriginalText(req.getOriginalText());
        s.setOriginalFileName(req.getOriginalFileName());
        s.setProcessingMode(req.getProcessingMode() == null ? "paper_polish_enhance" : req.getProcessingMode());
        s.setCurrentStage("polish");
        s.setStatus("queued");
        s.setProgress(0.0);
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(s);

        optimizationService.runOptimization(s.getId());
        return ApiResponse.success(s);
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status(@RequestParam(required = false) String sessionId) {
        return ApiResponse.success(concurrencyManager.getStatus(sessionId));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<OptimizationSession>> listSessions() {
        return ApiResponse.success(sessionRepository.findAll());
    }

    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<Map<String, Object>> sessionDetail(@PathVariable String sessionId) {
        OptimizationSession s = sessionRepository.findBySessionId(sessionId).orElse(null);
        if (s == null) {
            return ApiResponse.error(404, "会话不存在");
        }
        List<OptimizationSegment> segs = segmentRepository.findBySessionOrderBySegmentIndexAsc(s);
        return ApiResponse.success(Map.of("session", s, "segments", segs));
    }

    @GetMapping("/sessions/{sessionId}/progress")
    public ApiResponse<OptimizationSession> progress(@PathVariable String sessionId) {
        OptimizationSession s = sessionRepository.findBySessionId(sessionId).orElse(null);
        if (s == null) {
            return ApiResponse.error(404, "会话不存在");
        }
        return ApiResponse.success(s);
    }

    @GetMapping("/sessions/{sessionId}/segments")
    public ApiResponse<List<OptimizationSegmentDto>> segments(@PathVariable String sessionId) {
        OptimizationSession s = sessionRepository.findBySessionId(sessionId).orElse(null);
        if (s == null) {
            return ApiResponse.error(404, "会话不存在");
        }
        List<OptimizationSegment> segs = segmentRepository.findBySessionOrderBySegmentIndexAsc(s);
        List<OptimizationSegmentDto> dto = segs.stream().map(seg -> {
            OptimizationSegmentDto d = new OptimizationSegmentDto();
            d.setId(seg.getId());
            d.setSessionId(s.getSessionId());
            d.setSegmentIndex(seg.getSegmentIndex());
            d.setStage(seg.getStage());
            d.setOriginalText(seg.getOriginalText());
            d.setPolishedText(seg.getPolishedText());
            d.setEnhancedText(seg.getEnhancedText());
            d.setStatus(seg.getStatus());
            d.setIsTitle(seg.getIsTitle());
            d.setCreatedAt(seg.getCreatedAt());
            d.setCompletedAt(seg.getCompletedAt());
            return d;
        }).collect(Collectors.toList());
        return ApiResponse.success(dto);
    }

    @GetMapping(value = "/sessions/{sessionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@PathVariable String sessionId) {
        return streamManager.connect(sessionId);
    }

    @GetMapping("/sessions/{sessionId}/changes")
    public ApiResponse<List<OptimizationChangeLog>> changes(@PathVariable String sessionId) {
        OptimizationSession s = sessionRepository.findBySessionId(sessionId).orElse(null);
        if (s == null) {
            return ApiResponse.error(404, "会话不存在");
        }
        return ApiResponse.success(changeLogRepository.findBySessionOrderBySegmentIndexAscCreatedAtAsc(s));
    }

    @GetMapping(value = "/sessions/{sessionId}/export", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> export(@PathVariable String sessionId) {
        OptimizationSession s = sessionRepository.findBySessionId(sessionId).orElse(null);
        if (s == null) {
            return ResponseEntity.notFound().build();
        }

        List<OptimizationSegment> segs = segmentRepository.findBySessionOrderBySegmentIndexAsc(s);
        StringBuilder out = new StringBuilder();
        for (OptimizationSegment seg : segs) {
            String part = seg.getEnhancedText();
            if (part == null || part.isBlank()) {
                part = seg.getPolishedText();
            }
            if (part == null || part.isBlank()) {
                part = seg.getOriginalText();
            }
            if (part == null) {
                part = "";
            }
            if (!out.isEmpty()) {
                out.append("\n\n");
            }
            out.append(part.trim());
        }

        String filename = "optimization_" + sessionId + ".txt";
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(out.toString());
    }

    /**
     * Export optimization result as Word (.docx) with full thesis formatting.
     *
     * Strategy:
     * 1. Build a lookup map: segment.originalText -> best processed text
     * 2. Scan session.originalText line by line
     * 3. For each line, classify as structural (heading/table/code) or body paragraph
     * 4. Structural lines: add markdown ## markers as needed, keep original text
     * 5. Body paragraphs: look up in the map and replace with processed text
     * 6. Pass the rebuilt markdown to PaperService.exportDocx (frozen, not modified)
     */
    @GetMapping(value = "/sessions/{sessionId}/export-docx")
    public ResponseEntity<byte[]> exportDocx(@PathVariable String sessionId) {
        System.out.println("[EXPORT-DOCX] === Start export for session: " + sessionId + " ===");

        OptimizationSession s = sessionRepository.findBySessionId(sessionId).orElse(null);
        if (s == null) {
            return ResponseEntity.notFound().build();
        }

        String rawText = s.getOriginalText();
        if (rawText == null || rawText.isBlank()) {
            return ResponseEntity.internalServerError().build();
        }

        // Build title->table mapping directly from original text to avoid cross-title mismatches.
        java.util.LinkedHashMap<String, String> rawTableByTitle = new java.util.LinkedHashMap<>();
        String[] rawLinesForTableMap = rawText.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        for (int i = 0; i < rawLinesForTableMap.length; i++) {
            String tt = rawLinesForTableMap[i].trim();
            if (!tt.matches("^表\\s*\\d+[-—]\\d+.*")) {
                continue;
            }
            StringBuilder tb = new StringBuilder();
            int j = i + 1;
            while (j < rawLinesForTableMap.length) {
                String rt = rawLinesForTableMap[j].trim();
                if (rt.startsWith("|")) {
                    tb.append(rt).append("\n");
                    j++;
                    continue;
                }
                if (rt.isEmpty()) {
                    j++;
                    continue;
                }
                break;
            }
            if (!tb.isEmpty()) {
                rawTableByTitle.put(normalizeTableTitle(tt), tb.toString());
            }
        }

        // --- Step 1: build segment replacement map + collect table blocks ---
        List<OptimizationSegment> segs = segmentRepository.findBySessionOrderBySegmentIndexAsc(s);
        java.util.LinkedHashMap<String, String> segMap = new java.util.LinkedHashMap<>();
        // Collect table blocks from segments: groups of consecutive | rows
        java.util.List<String> tableBlocks = new java.util.ArrayList<>();
        java.util.List<Integer> tableTitleSegIdx = new java.util.ArrayList<>();
        java.util.List<String> tableTitleTexts = new java.util.ArrayList<>();
        StringBuilder currentTable = null;
        for (int si = 0; si < segs.size(); si++) {
            OptimizationSegment seg = segs.get(si);
            String orig = seg.getOriginalText();
            if (orig == null || orig.isBlank()) continue;
            String key = orig.trim();
            if (key.matches("^表\\s*\\d+[-—]\\d+.*")) {
                tableTitleSegIdx.add(si);
                tableTitleTexts.add(key);
            }

            String val = seg.getEnhancedText();
            if (val == null || val.isBlank()) val = seg.getPolishedText();
            if (val == null || val.isBlank()) val = orig;

            String valTrimmed = val.trim();
            segMap.put(key, valTrimmed);

            // Collect markdown table rows from original text only (avoid duplication)
            java.util.List<String> tableRows = new java.util.ArrayList<>();
            if (key.startsWith("|")) {
                tableRows.add(key);
            }

            if (!tableRows.isEmpty()) {
                if (currentTable == null) currentTable = new StringBuilder();
                for (String r : tableRows) {
                    currentTable.append(r).append("\n");
                }
                continue;
            }

            // End of a table block when current segment is not table-related
            if (currentTable != null) {
                tableBlocks.add(currentTable.toString());
                currentTable = null;
            }
        }
        if (currentTable != null) {
            tableBlocks.add(currentTable.toString());
        }

        // Fallback: if no pipe-table rows found, try reconstructing table rows from segments
        // near each table title (rows often stored as tab/space-delimited text, not markdown pipes).
        if (tableBlocks.size() < tableTitleSegIdx.size()) {
            java.util.List<String> fallbackBlocks = new java.util.ArrayList<>();
            for (int ti = 0; ti < tableTitleSegIdx.size(); ti++) {
                int start = tableTitleSegIdx.get(ti);
                java.util.List<String[]> rowCells = new java.util.ArrayList<>();

                for (int j = start + 1; j < segs.size(); j++) {
                    OptimizationSegment nseg = segs.get(j);
                    String no = nseg.getOriginalText();
                    if (no == null) continue;
                    String nt = no.trim();
                    String nv = nseg.getEnhancedText();
                    if (nv == null || nv.isBlank()) nv = nseg.getPolishedText();
                    if (nv == null || nv.isBlank()) nv = no;
                    String nvt = nv.trim();

                    if (nt.matches("^表\\s*\\d+[-—]\\d+.*") || nt.matches("^第[一二三四五六七八九十百\\d]+章.*")
                            || nt.matches("^\\d+\\.\\d+.*") || nt.matches("^摘\\s*要$")
                            || nt.equalsIgnoreCase("ABSTRACT") || nt.equals("参考文献") || nt.matches("^致\\s*谢$")) {
                        break;
                    }

                    if (nvt.isBlank()) {
                        if (!rowCells.isEmpty()) break;
                        continue;
                    }

                    String[] cells;
                    if (nvt.contains("\t")) {
                        cells = nvt.split("\\t+");
                    } else if (nvt.contains("｜")) {
                        cells = nvt.split("｜");
                    } else if (nvt.contains("|")) {
                        cells = nvt.split("\\|");
                    } else {
                        cells = nvt.split("\\s{2,}");
                    }

                    java.util.List<String> cleaned = new java.util.ArrayList<>();
                    for (String c : cells) {
                        String ct = c.trim();
                        if (!ct.isEmpty()) cleaned.add(ct);
                    }

                    // likely table row: at least 2 columns and not too verbose
                    if (cleaned.size() >= 2 && nvt.length() <= 240) {
                        rowCells.add(cleaned.toArray(new String[0]));
                    } else if (!rowCells.isEmpty()) {
                        break;
                    }
                }

                if (rowCells.size() >= 2) {
                    int colCount = 0;
                    for (String[] r : rowCells) colCount = Math.max(colCount, r.length);
                    if (colCount >= 2) {
                        StringBuilder tb = new StringBuilder();
                        String[] header = rowCells.get(0);
                        tb.append("|");
                        for (int c = 0; c < colCount; c++) {
                            String hv = c < header.length ? header[c] : "";
                            tb.append(hv).append("|");
                        }
                        tb.append("\n|");
                        for (int c = 0; c < colCount; c++) tb.append("---|");
                        tb.append("\n");
                        for (int r = 1; r < rowCells.size(); r++) {
                            tb.append("|");
                            String[] rr = rowCells.get(r);
                            for (int c = 0; c < colCount; c++) {
                                String rv = c < rr.length ? rr[c] : "";
                                tb.append(rv).append("|");
                            }
                            tb.append("\n");
                        }
                        fallbackBlocks.add(tb.toString());
                    }
                }
            }

            if (!fallbackBlocks.isEmpty()) {
                tableBlocks = fallbackBlocks;
                System.out.println("[EXPORT-DOCX] Using fallback reconstructed tables: " + tableBlocks.size());
            }
        }
        System.out.println("[EXPORT-DOCX] Segment map size: " + segMap.size()
                + ", table blocks: " + tableBlocks.size());

        // --- Step 2: scan original text line by line, rebuild as markdown ---
        String[] lines = rawText.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        
        // --- Pre-scan: detect outline block (chapter-title-list pattern) ---
        // Strategy: Find a cluster of "第X章" lines that appear as an outline/TOC summary.
        // These are typically a list of chapter titles without body paragraphs between them.
        // Allow short intro lines (e.g., "本文的结构安排如下：") and empty lines in between.
        java.util.Set<Integer> outlineSkipLines = new java.util.HashSet<>();
        // Collect chapter titles from the outline block so we can re-insert them in the body
        java.util.List<String> outlineChapterTitles = new java.util.ArrayList<>();
        {
            // Step 1: Find all "第X章" lines (with or without ## prefix)
            java.util.List<Integer> chapterLines = new java.util.ArrayList<>();
            for (int pi = 0; pi < lines.length; pi++) {
                String pt = lines[pi].trim();
                // Match "第X章..." or "## 第X章..." patterns
                String stripped = pt.replaceFirst("^#{1,4}\\s+", "");
                if (stripped.matches("^第[一二三四五六七八九十百\\d]+章[：:：].*") 
                        || stripped.matches("^第[一二三四五六七八九十百\\d]+章$")) {
                    chapterLines.add(pi);
                }
            }
            
            // Step 2: Find clusters of >=3 chapter lines that are close together
            // (no long body paragraphs between them, only short text/empty/heading lines)
            if (chapterLines.size() >= 3) {
                int clusterStart = 0;
                for (int ci = 0; ci < chapterLines.size(); ci++) {
                    // Check if this chapter line is too far from previous (body text in between)
                    if (ci > clusterStart) {
                        int prevLine = chapterLines.get(ci - 1);
                        int curLine = chapterLines.get(ci);
                        boolean hasLongBody = false;
                        for (int pi = prevLine + 1; pi < curLine; pi++) {
                            String pt = lines[pi].trim();
                            // If there's a long body paragraph between chapter titles, break the cluster
                            if (pt.length() > 80 && !pt.matches("^#{1,4}\\s+.*") 
                                    && !pt.matches("^第[一二三四五六七八九十百\\d]+章.*")
                                    && !pt.matches("^\\d+\\.\\d+.*")) {
                                hasLongBody = true;
                                break;
                            }
                        }
                        if (hasLongBody) {
                            // Save previous cluster if big enough
                            int clusterSize = ci - clusterStart;
                            if (clusterSize >= 3) {
                                markOutlineCluster(lines, chapterLines, clusterStart, ci - 1, outlineSkipLines, outlineChapterTitles);
                            }
                            clusterStart = ci;
                        }
                    }
                }
                // Check final cluster
                int finalClusterSize = chapterLines.size() - clusterStart;
                if (finalClusterSize >= 3) {
                    markOutlineCluster(lines, chapterLines, clusterStart, chapterLines.size() - 1, outlineSkipLines, outlineChapterTitles);
                }
            }
            
            if (!outlineSkipLines.isEmpty()) {
                System.out.println("[EXPORT-DOCX] Detected outline block: " + outlineSkipLines.size() + " lines skipped");
            } else {
                System.out.println("[EXPORT-DOCX] No outline block detected");
            }
        }
        
        StringBuilder md = new StringBuilder();
        boolean inTocSection = false;
        String title = null;
        boolean skipFollowingRawTableRows = false;
        // Track which chapter numbers have been output, to auto-insert missing ones from outline
        java.util.Set<Integer> outputChapterNumbers = new java.util.HashSet<>();
        // Build a map: chapter number -> outline title (e.g., 1 -> "第一章：绪论")
        java.util.Map<Integer, String> outlineChapterMap = new java.util.LinkedHashMap<>();
        String[] chineseNums = {"零","一","二","三","四","五","六","七","八","九","十"};
        for (String oct : outlineChapterTitles) {
            // Extract chapter number from title like "第一章：绪论" or "第1章：绪论"
            java.util.regex.Matcher cm = java.util.regex.Pattern.compile(
                    "^第([一二三四五六七八九十百\\d]+)章").matcher(oct);
            if (cm.find()) {
                String numStr = cm.group(1);
                int num = -1;
                try { num = Integer.parseInt(numStr); } catch (NumberFormatException e) {
                    for (int ni = 0; ni < chineseNums.length; ni++) {
                        if (chineseNums[ni].equals(numStr)) { num = ni; break; }
                    }
                }
                if (num > 0) outlineChapterMap.put(num, oct);
            }
        }
        System.out.println("[EXPORT-DOCX] Outline chapter titles: " + outlineChapterMap);

        for (int li = 0; li < lines.length; li++) {
            String line = lines[li];
            String t = line.trim();

            // Skip outline block lines
            if (outlineSkipLines.contains(li)) {
                continue;
            }

            // Extract title from first non-empty line
            if (title == null && !t.isEmpty()) {
                title = t.replaceFirst("^#+\\s+", "").replaceAll("\\*\\*", "").trim();
                System.out.println("[EXPORT-DOCX] Title extracted: " + title);
                continue;
            }

            // Classify line
            boolean isSpecialHeading = t.matches("^摘\\s*要$")
                    || t.equalsIgnoreCase("ABSTRACT")
                    || t.equals("参考文献")
                    || t.matches("^致\\s*谢$");
            boolean isTocHeading = t.matches("^目\\s*录$");
            boolean isChapter = t.matches("^第[一二三四五六七八九十百\\d]+章.*");
            boolean isSubsection = t.matches("^\\d+\\.\\d+\\.\\d+.*") && t.length() <= 80;
            boolean isSection = !isSubsection && t.matches("^\\d+\\.\\d+[^.].*") && t.length() <= 80;
            boolean isTableRow = t.startsWith("|");
            boolean isCodeFence = t.startsWith("```");
            boolean isMarkdownHeading = t.matches("^#{1,4}\\s+.+");
            // Table title line: e.g. "表 3-1 ..." or "表3-1..."
            boolean isTableTitle = t.matches("^表\\s*\\d+[-—]\\d+.*");
            // Mermaid technical-route lines (e.g. "graph TD", "graphTD", "graph LR")
            boolean isMermaidStart = t.matches("^graph\\s*[A-Za-z]{2}.*");
            boolean isMermaidArrowLine = t.contains("-->");
            boolean isMermaidLine = isMermaidStart || isMermaidArrowLine;

            if (skipFollowingRawTableRows && isTableRow) {
                continue;
            }
            if (skipFollowingRawTableRows && !isTableRow) {
                skipFollowingRawTableRows = false;
            }

            // --- TOC section detection ---
            // Skip the "目录" heading and actual TOC entries (lines with tab chars, page numbers, or dot leaders)
            if (isTocHeading) {
                inTocSection = true;
                continue;
            }
            if (inTocSection) {
                // Real TOC entries typically contain: tab characters, dot leaders (……), page numbers at end
                boolean isTocEntry = t.contains("\t") 
                        || t.matches(".*\\.{3,}.*")           // dot leaders
                        || t.matches(".*…{2,}.*")             // Chinese dot leaders
                        || t.matches(".*\\d+\\s*$")           // ends with page number
                        || t.contains("点击此处") || t.contains("更新域") || t.contains("生成目录");
                if (t.isEmpty() || isTocEntry) {
                    continue;
                }
                // Non-TOC content encountered, exit TOC section
                inTocSection = false;
            }

            // --- Output line with proper markdown markers ---
            if (t.isEmpty()) {
                md.append("\n");
                continue;
            }

            // Auto-insert missing chapter title from outline block before section/subsection headings
            if ((isSection || isSubsection) && !outlineChapterMap.isEmpty()) {
                // Extract chapter number from section heading (e.g., "1.2" -> chapter 1)
                String headingForCheck = t.replaceFirst("^#{1,4}\\s+", "");
                java.util.regex.Matcher secMatcher = java.util.regex.Pattern.compile("^(\\d+)\\.").matcher(headingForCheck);
                if (secMatcher.find()) {
                    int chapterNum = Integer.parseInt(secMatcher.group(1));
                    if (!outputChapterNumbers.contains(chapterNum) && outlineChapterMap.containsKey(chapterNum)) {
                        String chapterTitle = outlineChapterMap.get(chapterNum);
                        md.append("## ").append(chapterTitle).append("\n");
                        outputChapterNumbers.add(chapterNum);
                        System.out.println("[EXPORT-DOCX] Auto-inserted missing chapter: " + chapterTitle);
                    }
                }
            }

            if (isMarkdownHeading) {
                // Clean markdown headings: remove description after period
                String cleanHeading = t;
                if (t.matches("^#{1,4}\\s+第[一二三四五六七八九十百\\d]+章[：:].+")) {
                    // Remove description after period for chapter headings
                    cleanHeading = t.replaceAll("[。.].*$", "");
                }
                md.append(cleanHeading).append("\n");
                // Track chapter number if this is a chapter heading in markdown format
                String stripped = cleanHeading.replaceFirst("^#{1,4}\\s+", "");
                java.util.regex.Matcher chMatcher = java.util.regex.Pattern.compile("^第([一二三四五六七八九十百\\d]+)章").matcher(stripped);
                if (chMatcher.find()) {
                    String numStr = chMatcher.group(1);
                    int num = -1;
                    try { num = Integer.parseInt(numStr); } catch (NumberFormatException e) {
                        for (int ni = 0; ni < chineseNums.length; ni++) {
                            if (chineseNums[ni].equals(numStr)) { num = ni; break; }
                        }
                    }
                    if (num > 0) outputChapterNumbers.add(num);
                }
            } else if (isSpecialHeading) {
                md.append("## ").append(t).append("\n");
            } else if (isChapter) {
                // Clean chapter headings: remove description after period
                String cleanChapter = t;
                if (t.matches("^第[一二三四五六七八九十百\\d]+章[：:].+")) {
                    cleanChapter = t.replaceAll("[。.].*$", "");
                }
                md.append("## ").append(cleanChapter).append("\n");
                // Track chapter number
                java.util.regex.Matcher chMatcher = java.util.regex.Pattern.compile("^第([一二三四五六七八九十百\\d]+)章").matcher(cleanChapter);
                if (chMatcher.find()) {
                    String numStr = chMatcher.group(1);
                    int num = -1;
                    try { num = Integer.parseInt(numStr); } catch (NumberFormatException e) {
                        for (int ni = 0; ni < chineseNums.length; ni++) {
                            if (chineseNums[ni].equals(numStr)) { num = ni; break; }
                        }
                    }
                    if (num > 0) outputChapterNumbers.add(num);
                }
            } else if (isSubsection) {
                md.append("#### ").append(t).append("\n");
            } else if (isSection) {
                md.append("### ").append(t).append("\n");
            } else if (isMermaidStart) {
                // Wrap mermaid block to force CODE_BLOCK parsing and dedicated rendering.
                md.append("```mermaid\n");
                md.append(t).append("\n");
                int j = li + 1;
                while (j < lines.length) {
                    String mt = lines[j].trim();
                    if (mt.isEmpty()) {
                        break;
                    }
                    // Recognize mermaid syntax: arrows, subgraph, end, nodes with brackets, semicolons
                    boolean isMermaidSyntax = mt.contains("-->") 
                            || mt.startsWith("subgraph") || mt.equals("end")
                            || mt.matches("^[A-Za-z].*\\[.*\\].*")  // node definitions like A[text]
                            || mt.matches("^[A-Za-z].*-->.*")       // arrow lines
                            || mt.endsWith(";")                      // lines ending with semicolon
                            || mt.contains("&");                     // parallel connections like G&H
                    if (isMermaidSyntax) {
                        md.append(mt).append("\n");
                        j++;
                        continue;
                    }
                    break;
                }
                md.append("```\n");
                li = j - 1;
            } else if (isTableRow || isCodeFence || isMermaidLine) {
                md.append(t).append("\n");
            } else if (isTableTitle) {
                // Output the table title, then insert the table block mapped by this title.
                md.append(t).append("\n\n");
                String mapped = rawTableByTitle.get(normalizeTableTitle(t));
                if (mapped != null && !mapped.isBlank()) {
                    md.append(mapped).append("\n");
                    skipFollowingRawTableRows = true;
                    System.out.println("[EXPORT-DOCX] Inserted raw mapped table after title: "
                            + t.substring(0, Math.min(40, t.length())));
                }
            } else {
                // Body paragraph — look up replacement
                String replacement = segMap.get(t);
                if (replacement != null && !replacement.equals(t)) {
                    md.append(replacement).append("\n");
                } else {
                    md.append(t).append("\n");
                }
            }
        }
        if (title == null || title.isBlank()) title = "export";
        String markdown = md.toString();
        System.out.println("[EXPORT-DOCX] Rebuilt markdown length: " + markdown.length());

        // Debug: write rebuilt markdown + segment dump
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream("debug_aigc_export_markdown.txt"), StandardCharsets.UTF_8))) {
            pw.println("=== Title: " + title + " ===");
            pw.println("=== Table blocks found: " + tableBlocks.size() + " ===");
            for (int tb = 0; tb < tableBlocks.size(); tb++) {
                pw.println("--- Table block #" + tb + " ---");
                pw.println(tableBlocks.get(tb));
            }
            pw.println("=== Segment dump (first 30 chars of each orig, showing | segments) ===");
            for (int si = 0; si < segs.size(); si++) {
                String so = segs.get(si).getOriginalText();
                if (so == null) continue;
                String sot = so.trim();
                if (sot.contains("|") || sot.startsWith("表")) {
                    pw.println("SEG[" + si + "] len=" + sot.length() + " : " + sot.substring(0, Math.min(80, sot.length())));
                }
            }
            pw.println("=== Table-title neighborhoods (orig/proc) ===");
            for (int ti = 0; ti < tableTitleSegIdx.size(); ti++) {
                int center = tableTitleSegIdx.get(ti);
                pw.println("--- around table title #" + ti + " at SEG[" + center + "]: " + tableTitleTexts.get(ti));
                int from = Math.max(0, center - 5);
                int to = Math.min(segs.size() - 1, center + 12);
                for (int k = from; k <= to; k++) {
                    OptimizationSegment ds = segs.get(k);
                    String o = ds.getOriginalText();
                    String p = ds.getEnhancedText();
                    if (p == null || p.isBlank()) p = ds.getPolishedText();
                    if (p == null || p.isBlank()) p = o;
                    String ot = o == null ? "" : o.trim();
                    String pt = p == null ? "" : p.trim();
                    if (ot.length() > 120) ot = ot.substring(0, 120) + "...";
                    if (pt.length() > 120) pt = pt.substring(0, 120) + "...";
                    pw.println("SEG[" + k + "] O: " + ot);
                    pw.println("SEG[" + k + "] P: " + pt);
                }
            }
            pw.println("=== MARKDOWN ===");
            pw.println(markdown);
            pw.flush();
        } catch (Exception ignored) {}

        try {
            byte[] docxBytes = paperService.exportDocx(title, markdown);
            System.out.println("[EXPORT-DOCX] Generated docx size: " + docxBytes.length + " bytes");

            // Determine filename based on whether there's an original file name
            String baseFileName;
            String suffix;
            
            if (s.getOriginalFileName() != null && !s.getOriginalFileName().isEmpty()) {
                // If uploaded from file, use original filename + _RAIGC
                String originalName = s.getOriginalFileName();
                // Remove extension if present
                int lastDot = originalName.lastIndexOf('.');
                if (lastDot > 0) {
                    baseFileName = originalName.substring(0, lastDot);
                } else {
                    baseFileName = originalName;
                }
                suffix = "_RAIGC";
                System.out.println("[EXPORT-DOCX] Using original filename: " + originalName);
            } else {
                // If generated content, use title + _降AIGC
                baseFileName = title;
                suffix = "_降AIGC";
                System.out.println("[EXPORT-DOCX] Using title as filename: " + title);
            }
            
            String safeFileName = baseFileName.replaceAll("[\\\\/:*?\"<>|]", "_");
            String fullName = safeFileName + suffix + ".docx";
            String encodedName = java.net.URLEncoder.encode(fullName, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            String headerVal = "attachment; filename=\"" + safeFileName + suffix + ".docx\"; filename*=UTF-8''" + encodedName;

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerVal)
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .body(docxBytes);
        } catch (Exception e) {
            System.out.println("[EXPORT-DOCX] !!!! Export FAILED !!!!");
            System.out.println("[EXPORT-DOCX] Error: " + e.getClass().getName() + ": " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("[EXPORT-DOCX] Cause: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
            }
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Extract paper title from the original text (first non-empty line).
     */
    private String extractTitle(String originalText) {
        if (originalText == null || originalText.isBlank()) return "export";
        for (String line : originalText.split("\n")) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            t = t.replaceFirst("^#+\\s+", "").replaceAll("\\*\\*", "").trim();
            if (!t.isEmpty() && t.length() <= 80) return t;
        }
        return "export";
    }

    private String normalizeTableTitle(String title) {
        if (title == null) return "";
        return title.trim().replaceAll("：", ":").replaceAll("\\s+", "");
    }

    @PostMapping("/sessions/{sessionId}/stop")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stop(@PathVariable String sessionId) {
        OptimizationSession s = sessionRepository.findBySessionId(sessionId).orElse(null);
        if (s == null) {
            return ResponseEntity
                    .status(404)
                    .contentType(new MediaType("application", "json", StandardCharsets.UTF_8))
                    .body(ApiResponse.error(404, "会话不存在"));
        }
        String rawStatus = s.getStatus();
        String status = rawStatus == null ? "" : rawStatus.trim().toLowerCase();
        if (!"queued".equals(status) && !"processing".equals(status)) {
            return ResponseEntity
                    .badRequest()
                    .contentType(new MediaType("application", "json", StandardCharsets.UTF_8))
                    .body(ApiResponse.error(400, "只能停止排队中或处理中的会话（当前状态：" + (rawStatus == null ? "null" : rawStatus) + "）"));
        }
        s.setStatus("stopped");
        s.setErrorMessage("用户手动停止");
        s.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(s);
        streamManager.broadcast(sessionId, Map.of("type", "stopped", "message", "用户手动停止"));
        return ResponseEntity
                .ok()
                .contentType(new MediaType("application", "json", StandardCharsets.UTF_8))
                .body(ApiResponse.success(Map.of(
                        "message", "会话已停止",
                        "status", "stopped",
                        "sessionId", sessionId
                )));
    }

    @PostMapping("/sessions/{sessionId}/retry")
    public ResponseEntity<ApiResponse<Map<String, Object>>> retry(@PathVariable String sessionId) {
        OptimizationSession s = sessionRepository.findBySessionId(sessionId).orElse(null);
        if (s == null) {
            return ResponseEntity
                    .status(404)
                    .contentType(new MediaType("application", "json", StandardCharsets.UTF_8))
                    .body(ApiResponse.error(404, "会话不存在"));
        }
        if (!"failed".equals(s.getStatus()) && !"stopped".equals(s.getStatus())) {
            return ResponseEntity
                    .badRequest()
                    .contentType(new MediaType("application", "json", StandardCharsets.UTF_8))
                    .body(ApiResponse.error(400, "仅可对失败或已停止的会话执行重试"));
        }
        String old = s.getErrorMessage() == null ? "未知错误" : s.getErrorMessage();
        s.setStatus("queued");
        s.setErrorMessage("[重试中] 上次失败原因: " + old);
        s.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(s);
        optimizationService.runOptimization(s.getId());
        return ResponseEntity
                .ok()
                .contentType(new MediaType("application", "json", StandardCharsets.UTF_8))
                .body(ApiResponse.success(Map.of(
                        "message", "已重新排队",
                        "status", "queued",
                        "sessionId", sessionId
                )));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteSession(@PathVariable String sessionId) {
        OptimizationSession s = sessionRepository.findBySessionId(sessionId).orElse(null);
        if (s == null) {
            return ResponseEntity
                    .status(404)
                    .contentType(new MediaType("application", "json", StandardCharsets.UTF_8))
                    .body(ApiResponse.error(404, "会话不存在"));
        }
        if ("queued".equals(s.getStatus()) || "processing".equals(s.getStatus())) {
            return ResponseEntity
                    .badRequest()
                    .contentType(new MediaType("application", "json", StandardCharsets.UTF_8))
                    .body(ApiResponse.error(400, "排队中或处理中会话不可删除，请先停止"));
        }

        List<OptimizationSegment> segs = segmentRepository.findBySessionOrderBySegmentIndexAsc(s);
        List<OptimizationChangeLog> logs = changeLogRepository.findBySessionOrderBySegmentIndexAscCreatedAtAsc(s);
        if (!logs.isEmpty()) {
            changeLogRepository.deleteAll(logs);
        }
        if (!segs.isEmpty()) {
            segmentRepository.deleteAll(segs);
        }
        sessionRepository.delete(s);

        return ResponseEntity
                .ok()
                .contentType(new MediaType("application", "json", StandardCharsets.UTF_8))
                .body(ApiResponse.success(Map.of(
                        "message", "会话已删除",
                        "sessionId", sessionId
                )));
    }

    @PostMapping("/sessions/{sessionId}/delete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteSessionCompat(@PathVariable String sessionId) {
        return deleteSession(sessionId);
    }

    /**
     * Mark all lines in an outline cluster (from first chapter title to last chapter title)
     * as lines to skip. Also marks short intro lines between/before chapter titles.
     */
    private void markOutlineCluster(String[] lines, java.util.List<Integer> chapterLines,
                                     int clusterStartIdx, int clusterEndIdx,
                                     java.util.Set<Integer> outlineSkipLines,
                                     java.util.List<String> outlineChapterTitles) {
        int firstLine = chapterLines.get(clusterStartIdx);
        int lastLine = chapterLines.get(clusterEndIdx);
        
        // Also look backwards from the first chapter line for a short intro line
        // (e.g., "本文的结构安排如下：")
        int scanStart = firstLine;
        for (int pi = firstLine - 1; pi >= Math.max(0, firstLine - 3); pi--) {
            String pt = lines[pi].trim();
            if (pt.isEmpty()) continue;
            if (pt.length() <= 80 && (pt.contains("结构") || pt.contains("安排") 
                    || pt.contains("如下") || pt.contains("章节") || pt.contains("论文结构"))) {
                scanStart = pi;
                break;
            }
            break; // stop at first non-matching non-empty line
        }
        
        // Mark all lines from scanStart to lastLine as skip, and collect chapter titles
        for (int pi = scanStart; pi <= lastLine; pi++) {
            String pt = lines[pi].trim();
            if (!pt.isEmpty()) {
                outlineSkipLines.add(pi);
                // Collect chapter titles (strip ## prefix)
                String stripped = pt.replaceFirst("^#{1,4}\\s+", "");
                if (stripped.matches("^第[一二三四五六七八九十百\\d]+章.*")) {
                    // Clean: remove description after period (e.g., "第一章：绪论。阐述..." → "第一章：绪论")
                    String clean = stripped.replaceAll("[。.].*$", "");
                    outlineChapterTitles.add(clean);
                }
                System.out.println("[EXPORT-DOCX] Outline skip line " + pi + ": " + pt.substring(0, Math.min(40, pt.length())));
            }
        }
    }
}
