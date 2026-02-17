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

            // Collect markdown table rows from BOTH original and processed text
            java.util.List<String> tableRows = new java.util.ArrayList<>();
            if (key.startsWith("|")) {
                tableRows.add(key);
            }
            for (String vl : valTrimmed.split("\\n")) {
                String vt = vl.trim();
                if (vt.startsWith("|")) {
                    tableRows.add(vt);
                }
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
        StringBuilder md = new StringBuilder();
        boolean inTocSection = false;
        String title = null;
        boolean skipFollowingRawTableRows = false;

        for (int li = 0; li < lines.length; li++) {
            String line = lines[li];
            String t = line.trim();

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
            // Mermaid technical-route lines
            boolean isMermaidStart = t.matches("^graph[A-Za-z]{2}.*");
            boolean isMermaidArrowLine = t.contains("-->");
            boolean isMermaidLine = isMermaidStart || isMermaidArrowLine;

            if (skipFollowingRawTableRows && isTableRow) {
                continue;
            }
            if (skipFollowingRawTableRows && !isTableRow) {
                skipFollowingRawTableRows = false;
            }

            // --- TOC outline detection ---
            if (isTocHeading) {
                inTocSection = true;
                continue;
            }
            if (inTocSection) {
                if (t.isEmpty() || isChapter || isSection || isSubsection || t.contains("\t")) {
                    continue;
                }
                inTocSection = false;
            }

            // --- Output line with proper markdown markers ---
            if (t.isEmpty()) {
                md.append("\n");
                continue;
            }

            if (isMarkdownHeading) {
                md.append(t).append("\n");
            } else if (isSpecialHeading) {
                md.append("## ").append(t).append("\n");
            } else if (isChapter) {
                md.append("## ").append(t).append("\n");
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
                    if (mt.contains("-->") || mt.startsWith("subgraph") || mt.equals("end")) {
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
}
