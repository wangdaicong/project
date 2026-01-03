package com.example.bicolorsphere.web;

import com.example.bicolorsphere.repo.SsqDrawRepository;
import com.example.bicolorsphere.service.SsqStatsService;
import com.example.bicolorsphere.service.SsqSyncService;
import com.example.bicolorsphere.service.SsqExcelExportService;
import com.example.bicolorsphere.service.SsqPredictionService;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SsqController {

    private final SsqDrawRepository repository;
    private final SsqSyncService syncService;
    private final SsqStatsService statsService;
    private final SsqExcelExportService excelExportService;
    private final SsqPredictionService predictionService;

    public SsqController(SsqDrawRepository repository,
                         SsqSyncService syncService,
                         SsqStatsService statsService,
                         SsqExcelExportService excelExportService,
                         SsqPredictionService predictionService) {
        this.repository = repository;
        this.syncService = syncService;
        this.statsService = statsService;
        this.excelExportService = excelExportService;
        this.predictionService = predictionService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("ok", Boolean.TRUE);
        m.put("count", repository.count());
        return m;
    }

    @GetMapping("/draws")
    public Object draws(@RequestParam(defaultValue = "0") @Min(0) int page,
                        @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        return repository.page(page, size);
    }

    @GetMapping("/draws/search")
    public Object search(@RequestParam(required = false) String drawNoFrom,
                         @RequestParam(required = false) String drawNoTo,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                         @RequestParam(required = false) @Min(1) @Max(33) Integer includeRed,
                         @RequestParam(required = false) @Min(1) @Max(16) Integer includeBlue,
                         @RequestParam(defaultValue = "0") @Min(0) int page,
                         @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        return repository.search(new SsqDrawRepository.SearchFilter(
                drawNoFrom,
                drawNoTo,
                dateFrom,
                dateTo,
                includeRed,
                includeBlue,
                page,
                size
        )).asMap();
    }

    @GetMapping("/draws/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String drawNoFrom,
                                         @RequestParam(required = false) String drawNoTo,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                         @RequestParam(required = false) @Min(1) @Max(33) Integer includeRed,
                                         @RequestParam(required = false) @Min(1) @Max(16) Integer includeBlue,
                                         @RequestParam(defaultValue = "5000") @Min(1) @Max(10000) int maxRows) throws IOException {
        SsqDrawRepository.SearchFilter filter = new SsqDrawRepository.SearchFilter(drawNoFrom, drawNoTo, dateFrom, dateTo, includeRed, includeBlue, 0, maxRows);
        byte[] bytes = excelExportService.exportDraws(repository.listForExport(filter, maxRows));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ssq_draws.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @PostMapping("/sync")
    public Object sync(@RequestParam(defaultValue = "1") @Min(1) int fromPage,
                       @RequestParam(defaultValue = "5") @Min(1) int toPage) throws IOException {
        SsqSyncService.SyncResult r = syncService.syncPages(fromPage, toPage);
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("fromPage", r.getFromPage());
        m.put("toPage", r.getToPage());
        m.put("fetched", r.getFetched());
        m.put("inserted", r.getInserted());
        m.put("errors", r.getErrors());
        m.put("reconcile", predictionService.reconcileUnresolved(5000));
        return m;
    }

    @PostMapping("/sync/missing")
    public Object syncMissing(@RequestParam(defaultValue = "80") @Min(1) @Max(200) int maxPages,
                              @RequestParam(defaultValue = "3") @Min(1) @Max(20) int stopAfterNoInsertPages) {
        SsqSyncService.SyncMissingResult r = syncService.syncMissing(maxPages, stopAfterNoInsertPages);
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("scannedPages", r.getScannedPages());
        m.put("fetched", r.getFetched());
        m.put("inserted", r.getInserted());
        m.put("stopAfterNoInsertPages", r.getStopAfterNoInsertPages());
        m.put("errors", r.getErrors());
        m.put("reconcile", predictionService.reconcileUnresolved(5000));
        return m;
    }

    @PostMapping("/predictions")
    public Object savePrediction(@RequestParam String drawNo,
                                 @RequestParam String reds,
                                 @RequestParam @Min(1) @Max(16) int blue) {
        return predictionService.savePrediction(drawNo, reds, blue);
    }

    @PostMapping("/predictions/reconcile")
    public Object reconcile(@RequestParam(defaultValue = "5000") @Min(1) @Max(5000) int limit) {
        return predictionService.reconcileUnresolved(limit);
    }

    @GetMapping("/trend")
    public Object trend(@RequestParam(defaultValue = "100") @Min(10) @Max(5000) int latestN) {
        return statsService.trend(latestN);
    }

    @GetMapping("/hotcold")
    public Object hotCold(@RequestParam(defaultValue = "200") @Min(20) @Max(1000) int latestN) {
        return statsService.hotCold(latestN);
    }

    @GetMapping("/omission")
    public Object omission(@RequestParam(defaultValue = "200") @Min(20) @Max(2000) int latestN) {
        return statsService.omission(latestN);
    }

    @GetMapping("/predict")
    public Object predict(@RequestParam(defaultValue = "200") @Min(20) @Max(2000) int latestN,
                          @RequestParam(defaultValue = "frequency_top") String strategy,
                          @RequestParam(defaultValue = "1") @Min(1) @Max(20) int count,
                          @RequestParam(required = false) Integer minSum,
                          @RequestParam(required = false) Integer maxSum,
                          @RequestParam(required = false) Integer minSpan,
                          @RequestParam(required = false) Integer maxSpan,
                          @RequestParam(required = false) Integer minOdd,
                          @RequestParam(required = false) Integer maxOdd,
                          @RequestParam(required = false) String zoneRatio,
                          @RequestParam(required = false) String danReds,
                          @RequestParam(required = false) String killReds,
                          @RequestParam(required = false) String danBlues,
                          @RequestParam(required = false) String killBlues,
                          @RequestParam(required = false) Integer maxTry) {
        SsqStatsService.PredictOptions opt = buildPredictOptions(minSum, maxSum, minSpan, maxSpan, minOdd, maxOdd,
                zoneRatio, danReds, killReds, danBlues, killBlues, maxTry);
        return statsService.predict(latestN, strategy, count, opt);
    }

    @GetMapping("/backtest")
    public Object backtest(@RequestParam(defaultValue = "frequency_top") String strategy,
                           @RequestParam(defaultValue = "200") @Min(50) @Max(2000) int trainWindow,
                           @RequestParam(defaultValue = "50") @Min(10) @Max(500) int testCount,
                           @RequestParam(required = false) Integer minSum,
                           @RequestParam(required = false) Integer maxSum,
                           @RequestParam(required = false) Integer minSpan,
                           @RequestParam(required = false) Integer maxSpan,
                           @RequestParam(required = false) Integer minOdd,
                           @RequestParam(required = false) Integer maxOdd,
                           @RequestParam(required = false) String zoneRatio,
                           @RequestParam(required = false) String danReds,
                           @RequestParam(required = false) String killReds,
                           @RequestParam(required = false) String danBlues,
                           @RequestParam(required = false) String killBlues,
                           @RequestParam(required = false) Integer maxTry) {
        SsqStatsService.PredictOptions opt = buildPredictOptions(minSum, maxSum, minSpan, maxSpan, minOdd, maxOdd,
                zoneRatio, danReds, killReds, danBlues, killBlues, maxTry);
        return statsService.backtest(strategy, trainWindow, testCount, opt);
    }

    @GetMapping("/recommend")
    public Object recommend(@RequestParam(defaultValue = "200") @Min(50) @Max(2000) int trainWindow,
                            @RequestParam(defaultValue = "80") @Min(10) @Max(500) int testCount) {
        return statsService.recommend(trainWindow, testCount);
    }

    private static SsqStatsService.PredictOptions buildPredictOptions(Integer minSum,
                                                                      Integer maxSum,
                                                                      Integer minSpan,
                                                                      Integer maxSpan,
                                                                      Integer minOdd,
                                                                      Integer maxOdd,
                                                                      String zoneRatio,
                                                                      String danReds,
                                                                      String killReds,
                                                                      String danBlues,
                                                                      String killBlues,
                                                                      Integer maxTry) {
        SsqStatsService.PredictOptions opt = new SsqStatsService.PredictOptions();
        opt.setMinSum(minSum);
        opt.setMaxSum(maxSum);
        opt.setMinSpan(minSpan);
        opt.setMaxSpan(maxSpan);
        opt.setMinOdd(minOdd);
        opt.setMaxOdd(maxOdd);
        opt.setZoneRatio(SsqStatsService.PredictOptions.parseZoneRatio(zoneRatio));
        opt.setDanReds(SsqStatsService.PredictOptions.parseNumSet(danReds, 1, 33));
        opt.setKillReds(SsqStatsService.PredictOptions.parseNumSet(killReds, 1, 33));
        opt.setDanBlues(SsqStatsService.PredictOptions.parseNumSet(danBlues, 1, 16));
        opt.setKillBlues(SsqStatsService.PredictOptions.parseNumSet(killBlues, 1, 16));
        if (maxTry != null) {
            opt.setMaxTry(Math.max(10, Math.min(500, maxTry)));
        }
        return opt;
    }
}
