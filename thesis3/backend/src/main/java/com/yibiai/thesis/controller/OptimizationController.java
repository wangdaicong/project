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
    private final StreamManager streamManager;
    private final ConcurrencyManager concurrencyManager;

    public OptimizationController(
            OptimizationSessionRepository sessionRepository,
            OptimizationSegmentRepository segmentRepository,
            OptimizationChangeLogRepository changeLogRepository,
            OptimizationService optimizationService,
            StreamManager streamManager,
            ConcurrencyManager concurrencyManager
    ) {
        this.sessionRepository = sessionRepository;
        this.segmentRepository = segmentRepository;
        this.changeLogRepository = changeLogRepository;
        this.optimizationService = optimizationService;
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
