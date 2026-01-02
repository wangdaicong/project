package com.example.bicolorsphere.service;

import com.example.bicolorsphere.domain.SsqDraw;
import com.example.bicolorsphere.repo.SsqDrawRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SsqSyncService {
    private final SsqCrawlerService crawlerService;
    private final SsqDrawRepository repository;

    public SsqSyncService(SsqCrawlerService crawlerService, SsqDrawRepository repository) {
        this.crawlerService = crawlerService;
        this.repository = repository;
    }

    public SyncResult syncPages(int fromPage, int toPage) throws IOException {
        int start = Math.max(1, fromPage);
        int end = Math.max(start, toPage);

        int fetched = 0;
        int inserted = 0;
        List<String> errors = new ArrayList<>();

        for (int p = start; p <= end; p++) {
            try {
                List<SsqDraw> page = crawlerService.fetchPage(p);
                fetched += page.size();
                for (SsqDraw draw : page) {
                    inserted += repository.upsertIgnore(draw);
                }
            } catch (Exception e) {
                errors.add("page=" + p + ": " + e.getMessage());
            }
        }

        return new SyncResult(start, end, fetched, inserted, errors);
    }

    public SyncMissingResult syncMissing(int maxPages, int stopAfterNoInsertPages) {
        int maxP = Math.max(1, Math.min(200, maxPages));
        int stopAfter = Math.max(1, Math.min(20, stopAfterNoInsertPages));

        int fetched = 0;
        int inserted = 0;
        int scannedPages = 0;
        int noInsertStreak = 0;
        List<String> errors = new ArrayList<String>();

        for (int p = 1; p <= maxP; p++) {
            scannedPages++;
            try {
                List<SsqDraw> page = crawlerService.fetchPage(p);
                fetched += page.size();
                int pageInserted = 0;
                for (SsqDraw draw : page) {
                    pageInserted += repository.upsertIgnore(draw);
                }
                inserted += pageInserted;

                if (pageInserted == 0) {
                    noInsertStreak++;
                } else {
                    noInsertStreak = 0;
                }

                if (noInsertStreak >= stopAfter) {
                    break;
                }
            } catch (Exception e) {
                errors.add("page=" + p + ": " + e.getMessage());
            }
        }

        return new SyncMissingResult(scannedPages, fetched, inserted, noInsertStreak, errors);
    }

    public static class SyncResult {
        private int fromPage;
        private int toPage;
        private int fetched;
        private int inserted;
        private List<String> errors;

        public SyncResult(int fromPage, int toPage, int fetched, int inserted, List<String> errors) {
            this.fromPage = fromPage;
            this.toPage = toPage;
            this.fetched = fetched;
            this.inserted = inserted;
            this.errors = errors;
        }

        public int getFromPage() {
            return fromPage;
        }

        public int getToPage() {
            return toPage;
        }

        public int getFetched() {
            return fetched;
        }

        public int getInserted() {
            return inserted;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    public static class SyncMissingResult {
        private int scannedPages;
        private int fetched;
        private int inserted;
        private int stopAfterNoInsertPages;
        private List<String> errors;

        public SyncMissingResult(int scannedPages, int fetched, int inserted, int stopAfterNoInsertPages, List<String> errors) {
            this.scannedPages = scannedPages;
            this.fetched = fetched;
            this.inserted = inserted;
            this.stopAfterNoInsertPages = stopAfterNoInsertPages;
            this.errors = errors;
        }

        public int getScannedPages() {
            return scannedPages;
        }

        public int getFetched() {
            return fetched;
        }

        public int getInserted() {
            return inserted;
        }

        public int getStopAfterNoInsertPages() {
            return stopAfterNoInsertPages;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
