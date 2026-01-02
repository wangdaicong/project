package com.example.bicolorsphere.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SsqAutoSyncJob {

    private final boolean enabled;
    private final int syncPages;
    private final SsqSyncService syncService;

    public SsqAutoSyncJob(
            @Value("${app.ssq.autosync.enabled:true}") boolean enabled,
            @Value("${app.ssq.autosync.syncPages:2}") int syncPages,
            SsqSyncService syncService
    ) {
        this.enabled = enabled;
        this.syncPages = syncPages;
        this.syncService = syncService;
    }

    @Scheduled(cron = "${app.ssq.autosync.cron:0 15 1 * * ?}")
    public void run() {
        if (!enabled) {
            return;
        }
        try {
            syncService.syncPages(1, Math.max(1, syncPages));
        } catch (Exception ignored) {
        }
    }
}
