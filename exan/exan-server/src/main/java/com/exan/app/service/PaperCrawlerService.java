package com.exan.app.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import com.exan.domain.entity.Paper;
import com.exan.domain.entity.PaperContent;
import com.exan.domain.mapper.PaperContentMapper;
import com.exan.domain.mapper.PaperMapper;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PaperCrawlerService {
    private final PaperMapper paperMapper;
    private final PaperContentMapper paperContentMapper;

    public static final String DEFAULT_ZXXK_ENTRY_URL = "https://sx.zxxk.com/p";

    @Transactional
    public int syncDemo(long stageId, long subjectId) {
        int n = 0;

        n += upsert(stageId, subjectId, "2026-01-10 小学数学 真题卷", LocalDate.of(2026, 1, 10), "CN");
        n += upsert(stageId, subjectId, "2026-01-05 小学数学 真题卷", LocalDate.of(2026, 1, 5), "CN");
        n += upsert(stageId, subjectId, "2025-12-28 小学数学 真题卷", LocalDate.of(2025, 12, 28), "CN");

        return n;
    }

    @Transactional
    public int syncZxxkPaperContentByTitle(long paperId, String title, String listUrl, String cookie) {
        if (paperId <= 0) {
            throw new IllegalArgumentException("paperId is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (listUrl == null || listUrl.isBlank()) {
            throw new IllegalArgumentException("listUrl is required");
        }

        Map<String, Object> fetched = fetchPaperContentFromZxxk(listUrl, title, cookie);
        String detailUrl = (String) fetched.get("detailUrl");
        String contentText = (String) fetched.get("contentText");
        String attachmentsJson = (String) fetched.get("attachmentsJson");

        PaperContent c = paperContentMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaperContent>()
            .eq(PaperContent::getPaperId, paperId)
            .last("limit 1"));

        if (c == null) {
            c = new PaperContent();
            c.setPaperId(paperId);
            c.setSourceUrl(detailUrl);
            c.setContentText(contentText);
            c.setAttachmentsJson(attachmentsJson);
            return paperContentMapper.insert(c);
        }

        c.setSourceUrl(detailUrl);
        c.setContentText(contentText);
        c.setAttachmentsJson(attachmentsJson);
        return paperContentMapper.updateById(c);
    }

    private Map<String, Object> fetchPaperContentFromZxxk(String listUrl, String title, String cookie) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
             BrowserContext ctx = (cookie == null || cookie.isBlank())
                 ? browser.newContext()
                 : browser.newContext(new Browser.NewContextOptions().setExtraHTTPHeaders(Map.of("Cookie", cookie)));
             Page page = ctx.newPage()) {
            Map<Integer, List<String>> candidatesByPage = new HashMap<>();
            page.onResponse(resp -> {
                try {
                    if (resp.status() != 200) {
                        return;
                    }
                    String u = resp.url();
                    if (u == null) {
                        return;
                    }
                    String lower = u.toLowerCase(Locale.ROOT);
                    if (lower.startsWith("https://cdn-preview.xkw.com/")) {
                        Integer p = tryParsePreviewPageNo(lower);
                        if (p == null) {
                            return;
                        }
                        addCandidate(candidatesByPage, p, u);
                        return;
                    }

                    // Sometimes signed preview urls are returned in a JSON payload rather than direct <img> src.
                    String ct = null;
                    try {
                        ct = resp.headers().get("content-type");
                    } catch (Exception ignore) {
                        ct = null;
                    }
                    if (ct != null && ct.toLowerCase(Locale.ROOT).contains("json")) {
                        String body = resp.text();
                        if (body == null || body.isBlank() || !body.contains("cdn-preview.xkw.com")) {
                            return;
                        }
                        Matcher m = Pattern.compile("https?://cdn-preview\\.xkw\\.com/[^\\\"\\'\\)\\s]+/jpg/(\\d{1,4})\\.jpg[^\\\"\\'\\)\\s]*").matcher(body);
                        while (m.find()) {
                            String url = decodeHtmlAmp(m.group(0));
                            Integer pageNo = null;
                            try {
                                pageNo = Integer.parseInt(m.group(1));
                            } catch (Exception ignore) {
                                pageNo = null;
                            }
                            if (pageNo == null || url == null || url.isBlank()) {
                                continue;
                            }
                            addCandidate(candidatesByPage, pageNo, url);
                        }
                    }
                } catch (Exception ignore) {
                }
            });
            page.navigate(listUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE).setTimeout(45000));
            page.waitForTimeout(1200);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> links = (List<Map<String, Object>>) page.evalOnSelectorAll(
                "a[href]",
                "els => els.map(e => ({ text: (e.innerText || '').trim(), href: e.href }))"
            );

            String targetUrl = null;
            String listUrlLower = listUrl == null ? "" : listUrl.toLowerCase(Locale.ROOT);
            if (listUrlLower.contains("/soft/")) {
                targetUrl = listUrl;
            }
            String normalizedTitle = normalize(title);
            if (targetUrl == null) {
                for (Map<String, Object> l : links) {
                    Object tObj = l.get("text");
                    Object hObj = l.get("href");
                    if (!(tObj instanceof String) || !(hObj instanceof String)) {
                        continue;
                    }
                    String t = (String) tObj;
                    if (t == null || t.isBlank()) {
                        continue;
                    }
                    if (normalize(t).contains(normalizedTitle)) {
                        targetUrl = (String) hObj;
                        break;
                    }
                }
            }

            if (targetUrl == null) {
                throw new RuntimeException("paper detail url not found by title");
            }

            page.navigate(targetUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE).setTimeout(45000));
            page.waitForTimeout(1500);
            String bodyText = page.innerText("body");
            if (bodyText == null) {
                bodyText = "";
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> a2 = (List<Map<String, Object>>) page.evalOnSelectorAll(
                "a[href]",
                "els => els.map(e => ({ href: e.href, text: (e.innerText || '').trim() }))"
            );

            List<Map<String, Object>> atts = new ArrayList<>();

            // Keep only meaningful file links (PDF/Office). Filter out site icons and unrelated links.
            for (Map<String, Object> l : a2) {
                Object hrefObj = l.get("href");
                if (!(hrefObj instanceof String)) {
                    continue;
                }
                String href = (String) hrefObj;
                if (href == null || href.isBlank()) {
                    continue;
                }
                String lower = href.toLowerCase(Locale.ROOT);
                if (lower.startsWith("data:")) {
                    continue;
                }
                if (lower.contains("zxxkstatic.zxxk.com") || lower.contains("static.zxxk.com")) {
                    continue;
                }
                if (lower.endsWith(".pdf") || lower.contains(".pdf?") || lower.endsWith(".doc") || lower.endsWith(".docx") || lower.contains(".doc?") || lower.contains(".docx?")) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("type", "pdf");
                    m.put("url", href);
                    Object tx = l.get("text");
                    if (tx instanceof String && !((String) tx).isBlank()) {
                        m.put("label", (String) tx);
                    }
                    atts.add(m);
                }
            }

            // Trigger lazy loading of preview thumbnails by scrolling the page.
            // Without this, some pages (e.g. 3/4) may not appear in the DOM and cannot be collected.
            try {
                page.waitForTimeout(800);
                // scroll window a bit
                for (int i = 0; i < 4; i++) {
                    page.mouse().wheel(0, 1200);
                    page.waitForTimeout(400);
                }

                // scroll all scrollable containers (thumbnail list is often a nested scrollable div)
                for (int i = 0; i < 10; i++) {
                    page.evaluate(
                        "() => {" +
                            "const nodes = Array.from(document.querySelectorAll('*'));" +
                            "for (const el of nodes) {" +
                            "  const st = window.getComputedStyle(el);" +
                            "  const oy = st.overflowY;" +
                            "  if ((oy === 'auto' || oy === 'scroll') && el.scrollHeight > el.clientHeight + 20) {" +
                            "    el.scrollTop = Math.min(el.scrollHeight, el.scrollTop + Math.max(400, Math.floor(el.clientHeight * 0.9)));" +
                            "  }" +
                            "}" +
                        "}"
                    );
                    page.waitForTimeout(450);
                }

                // force to bottom for any scrollable containers
                page.evaluate(
                    "() => {" +
                        "const nodes = Array.from(document.querySelectorAll('*'));" +
                        "for (const el of nodes) {" +
                        "  const st = window.getComputedStyle(el);" +
                        "  const oy = st.overflowY;" +
                        "  if ((oy === 'auto' || oy === 'scroll') && el.scrollHeight > el.clientHeight + 20) {" +
                        "    el.scrollTop = el.scrollHeight;" +
                        "  }" +
                        "}" +
                    "}"
                );
                page.waitForTimeout(800);
            } catch (Exception ignore) {
            }

            atts = dedupeAndSortAttachments(atts);

            String attsJson = null;
            try {
                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();

                Map<String, Object> meta = extractMeta(bodyText);
                Integer totalPages = meta.get("totalPages") instanceof Number ? ((Number) meta.get("totalPages")).intValue() : null;

                // Add candidates from DOM/HTML as a fallback (may include HTML-encoded &amp;).
                try {
                    String html = page.content();
                    if (html != null && !html.isBlank()) {
                        Matcher m = Pattern.compile("https?://cdn-preview\\.xkw\\.com/[^\\\"\\'\\)\\s]+/jpg/(\\d{1,4})\\.jpg[^\\\"\\'\\)\\s]*").matcher(html);
                        while (m.find()) {
                            String url = decodeHtmlAmp(m.group(0));
                            Integer pageNo = null;
                            try {
                                pageNo = Integer.parseInt(m.group(1));
                            } catch (Exception ignore) {
                                pageNo = null;
                            }
                            if (pageNo == null || url == null || url.isBlank()) {
                                continue;
                            }
                            addCandidate(candidatesByPage, pageNo, url);
                        }
                    }
                } catch (Exception ignore) {
                }

                List<Map<String, Object>> verified = new ArrayList<>();
                if (totalPages != null && totalPages > 0 && totalPages <= 80) {
                    for (int p = 1; p <= totalPages; p++) {
                        List<String> cs = candidatesByPage.get(p);
                        String ok = pickFirstAccessible(cs);
                        if (ok == null) {
                            continue;
                        }
                        Map<String, Object> mm = new HashMap<>();
                        mm.put("type", "image");
                        mm.put("url", ok);
                        mm.put("page", p);
                        verified.add(mm);
                    }
                } else {
                    for (Map.Entry<Integer, List<String>> e : candidatesByPage.entrySet()) {
                        if (e.getKey() == null) {
                            continue;
                        }
                        String ok = pickFirstAccessible(e.getValue());
                        if (ok == null) {
                            continue;
                        }
                        Map<String, Object> mm = new HashMap<>();
                        mm.put("type", "image");
                        mm.put("url", ok);
                        mm.put("page", e.getKey());
                        verified.add(mm);
                    }
                }

                // Replace any previously collected image urls with verified ones, keep PDFs.
                List<Map<String, Object>> keep = atts.stream()
                    .filter(a -> a != null && "pdf".equals(a.get("type")))
                    .collect(Collectors.toCollection(ArrayList::new));
                keep.addAll(verified);
                atts = dedupeAndSortAttachments(keep);

                Map<String, Object> payload = new HashMap<>();
                payload.put("meta", meta);
                payload.put("items", atts);
                attsJson = om.writeValueAsString(payload);
            } catch (Exception ignore) {
                attsJson = null;
            }

            Map<String, Object> out = new HashMap<>();
            out.put("detailUrl", targetUrl);
            // Body text on zxxk pages contains lots of navigation noise; frontend uses preview images mainly.
            out.put("contentText", null);
            out.put("attachmentsJson", attsJson);
            return out;
        }
    }

    private static void addCandidate(Map<Integer, List<String>> candidatesByPage, Integer pageNo, String url) {
        if (pageNo == null || url == null || url.isBlank()) {
            return;
        }
        String normalized = decodeHtmlAmp(url).trim();
        candidatesByPage.computeIfAbsent(pageNo, k -> new ArrayList<>()).add(normalized);
    }

    private static String decodeHtmlAmp(String url) {
        if (url == null) {
            return null;
        }
        return url.replace("&amp;", "&");
    }

    private static String pickFirstAccessible(List<String> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        // de-dup while preserving order
        List<String> uniq = candidates.stream()
            .filter(s -> s != null && !s.isBlank())
            .map(String::trim)
            .distinct()
            .toList();

        HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(6))
            .build();

        int checked = 0;
        for (String u : uniq) {
            if (checked++ >= 8) {
                break;
            }
            try {
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(u))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(8))
                    .build();
                HttpResponse<Void> resp = client.send(req, HttpResponse.BodyHandlers.discarding());
                int sc = resp.statusCode();
                if (sc >= 200 && sc < 300) {
                    return u;
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    private static Map<String, Object> extractMeta(String bodyText) {
        Map<String, Object> meta = new HashMap<>();
        if (bodyText == null || bodyText.isBlank()) {
            return meta;
        }
        meta.put("views", tryMatchInt(bodyText, "浏览[:：]?\\s*(\\d+)"));
        meta.put("downloads", tryMatchInt(bodyText, "下载[:：]?\\s*(\\d+)"));
        meta.put("owner", tryMatchStr(bodyText, "所属[:：]?\\s*([^\\s\\n]{2,50})"));

        Integer totalPages = tryFindTotalPages(bodyText);
        if (totalPages != null) {
            meta.put("totalPages", totalPages);
        }
        return meta;
    }

    private static Integer tryMatchInt(String text, String pattern) {
        try {
            Matcher m = Pattern.compile(pattern).matcher(text);
            if (!m.find()) {
                return null;
            }
            return Integer.parseInt(m.group(1));
        } catch (Exception ignore) {
            return null;
        }
    }

    private static String tryMatchStr(String text, String pattern) {
        try {
            Matcher m = Pattern.compile(pattern).matcher(text);
            if (!m.find()) {
                return null;
            }
            String s = m.group(1);
            if (s == null) {
                return null;
            }
            s = s.trim();
            return s.isEmpty() ? null : s;
        } catch (Exception ignore) {
            return null;
        }
    }

    private static Integer tryFindTotalPages(String bodyText) {
        try {
            // try to find "1/4" like marker and use the maximum right side as totalPages
            Matcher m = Pattern.compile("(\\d{1,3})\\s*/\\s*(\\d{1,3})").matcher(bodyText);
            Integer best = null;
            while (m.find()) {
                int total = Integer.parseInt(m.group(2));
                if (total <= 0 || total > 80) {
                    continue;
                }
                if (best == null || total > best) {
                    best = total;
                }
            }
            return best;
        } catch (Exception ignore) {
            return null;
        }
    }

    private static Integer tryParsePreviewPageNo(String urlLower) {
        if (urlLower == null) {
            return null;
        }
        // typical: .../jpg/1.jpg?... or .../jpg/12.jpg
        Matcher m = Pattern.compile("/jpg/(\\d{1,4})\\.jpg").matcher(urlLower);
        if (!m.find()) {
            return null;
        }
        try {
            return Integer.parseInt(m.group(1));
        } catch (Exception ignore) {
            return null;
        }
    }

    private static List<Map<String, Object>> dedupeAndSortAttachments(List<Map<String, Object>> atts) {
        if (atts == null || atts.isEmpty()) {
            return List.of();
        }
        Map<String, Map<String, Object>> byKey = new HashMap<>();
        for (Map<String, Object> a : atts) {
            if (a == null) {
                continue;
            }
            Object urlObj = a.get("url");
            if (!(urlObj instanceof String) || ((String) urlObj).isBlank()) {
                continue;
            }
            String url = ((String) urlObj).trim();
            String type = a.get("type") instanceof String ? (String) a.get("type") : "";
            Object pageObj = a.get("page");
            String key = type + "|" + url;
            if (pageObj instanceof Number) {
                key = key + "|" + ((Number) pageObj).intValue();
            }
            byKey.putIfAbsent(key, a);
        }
        List<Map<String, Object>> out = new ArrayList<>(byKey.values());
        out.sort(Comparator
            .comparing((Map<String, Object> a) -> {
                Object t = a.get("type");
                return t instanceof String ? (String) t : "";
            })
            .thenComparing(a -> {
                Object p = a.get("page");
                if (p instanceof Number) {
                    return ((Number) p).intValue();
                }
                return 999999;
            })
            .thenComparing(a -> {
                Object u = a.get("url");
                return u instanceof String ? (String) u : "";
            }));
        return out;
    }

    private static List<Map<String, Object>> ensurePreviewPages(List<Map<String, Object>> atts, int totalPages) {
        if (atts == null) {
            atts = new ArrayList<>();
        }
        if (totalPages <= 0) {
            return atts;
        }

        String templateUrl = null;
        for (Map<String, Object> a : atts) {
            if (a == null) {
                continue;
            }
            Object t = a.get("type");
            Object u = a.get("url");
            if (!("image".equals(t)) || !(u instanceof String)) {
                continue;
            }
            String url = (String) u;
            if (url.toLowerCase(Locale.ROOT).contains("/jpg/1.jpg")) {
                templateUrl = url;
                break;
            }
            if (templateUrl == null) {
                templateUrl = url;
            }
        }
        if (templateUrl == null) {
            return atts;
        }

        Map<Integer, String> byPage = new HashMap<>();
        for (Map<String, Object> a : atts) {
            if (a == null) {
                continue;
            }
            if (!("image".equals(a.get("type")))) {
                continue;
            }
            Object p = a.get("page");
            Object u = a.get("url");
            if (p instanceof Number && u instanceof String) {
                byPage.put(((Number) p).intValue(), (String) u);
            }
        }

        for (int i = 1; i <= totalPages; i++) {
            if (byPage.containsKey(i)) {
                continue;
            }
            String url = templateUrl.replaceAll("/jpg/\\d{1,4}\\.jpg", "/jpg/" + i + ".jpg");
            Map<String, Object> m = new HashMap<>();
            m.put("type", "image");
            m.put("url", url);
            m.put("page", i);
            atts.add(m);
        }

        return dedupeAndSortAttachments(atts);
    }

    private static String stripQuery(String url) {
        if (url == null) {
            return null;
        }
        int idx = url.indexOf('?');
        if (idx <= 0) {
            return url;
        }
        return url.substring(0, idx);
    }

    private static String normalize(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().replaceAll("\\s+", "");
    }

    @Transactional
    public int syncZxxkPrimaryGrade3Math(long stageId, long subjectId, int limit, String cookie) {
        return syncZxxkPrimaryGrade3MathByUrl(stageId, subjectId, limit, DEFAULT_ZXXK_ENTRY_URL, cookie);
    }

    @Transactional
    public int syncZxxkPrimaryGrade3MathByUrl(long stageId, long subjectId, int limit, String url, String cookie) {
        int n = 0;

        try {
            Document doc = fetchZxxkDocument(url, cookie);

            n += syncFromDocumentAnchors(stageId, subjectId, limit, doc);
            if (n == 0) {
                n += syncFromPlaywrightVisibleTexts(stageId, subjectId, limit, url);
            }
        } catch (Exception e) {
            throw new RuntimeException("sync zxxk failed: " + e.getMessage(), e);
        }

        return n;
    }

    private int syncFromDocumentAnchors(long stageId, long subjectId, int limit, Document doc) {
        int n = 0;
        Elements links = doc.select("a[href]");
        for (Element a : links) {
            if (n >= limit) {
                break;
            }

            String title = a.text();
            if (!matchesGrade3MathPaperWord(title)) {
                continue;
            }

            LocalDate paperDate = tryParseDate(title);
            if (paperDate == null) {
                paperDate = LocalDate.now();
            }

            n += upsert(stageId, subjectId, title, paperDate, "CN");
        }
        return n;
    }

    private int syncFromPlaywrightVisibleTexts(long stageId, long subjectId, int limit, String url) {
        int n = 0;
        List<String> titles = fetchVisibleTitlesWithPlaywright(url, limit * 3);
        for (String title : titles) {
            if (n >= limit) {
                break;
            }
            if (!matchesGrade3Math(title)) {
                continue;
            }
            LocalDate paperDate = tryParseDate(title);
            if (paperDate == null) {
                paperDate = LocalDate.now();
            }
            n += upsert(stageId, subjectId, title, paperDate, "CN");
        }
        return n;
    }

    private List<String> fetchVisibleTitlesWithPlaywright(String url, int limit) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
             Page page = browser.newPage()) {
            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE).setTimeout(45000));
            page.waitForTimeout(1500);

            // For SPA-rendered pages, titles may not be inside <a> text nodes.
            // Scan visible body text lines and pick the lines that look like paper titles.
            String body = page.innerText("body");
            if (body == null || body.isBlank()) {
                return List.of();
            }

            return java.util.Arrays.stream(body.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(this::matchesGrade3Math)
                .distinct()
                .limit(Math.max(limit, 1))
                .collect(Collectors.toList());
        }
    }

    public String debugZxxkBodyLinesByUrl(String url, int sample) {
        List<String> lines;
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
             Page page = browser.newPage()) {
            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE).setTimeout(45000));
            page.waitForTimeout(1500);
            String body = page.innerText("body");
            if (body == null) {
                body = "";
            }
            lines = java.util.Arrays.stream(body.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        }

        int sampleSize = Math.min(Math.max(sample, 0), 50);
        List<String> matchedB64 = new ArrayList<>();
        for (String line : lines) {
            if (matchedB64.size() >= sampleSize) {
                break;
            }
            if (matchesGrade3Math(line)) {
                matchedB64.add(Base64.getEncoder().encodeToString(line.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            }
        }

        return "lineCount=" + lines.size()
            + "\nmatched_grade3_math_sample_b64=" + matchedB64;
    }

    private boolean matchesGrade3Math(String title) {
        if (title == null) {
            return false;
        }
        String t = title.trim();
        if (t.isEmpty()) {
            return false;
        }
        return t.contains("三年级") && t.contains("数学");
    }

    private boolean matchesGrade3MathPaperWord(String title) {
        if (title == null) {
            return false;
        }
        String t = title.trim();
        if (t.isEmpty()) {
            return false;
        }
        if (!t.contains("三年级")) {
            return false;
        }
        if (!t.contains("数学")) {
            return false;
        }
        return t.contains("试卷") || t.contains("测试") || t.contains("期末") || t.contains("期中") || t.contains("练习");
    }

    public String debugFetchZxxkHtmlSnippet(String cookie, int maxLen) {
        return debugFetchZxxkHtmlSnippetByUrl(DEFAULT_ZXXK_ENTRY_URL, cookie, maxLen);
    }

    public String debugFetchZxxkHtmlSnippetByUrl(String url, String cookie, int maxLen) {
        try {
            Document doc = fetchZxxkDocument(url, cookie);
            String html = doc.outerHtml();
            int len = Math.min(Math.max(maxLen, 200), 3000);
            if (html.length() <= len) {
                return html;
            }
            return html.substring(0, len);
        } catch (Exception e) {
            throw new RuntimeException("debug fetch zxxk failed: " + e.getMessage(), e);
        }
    }

    public String debugZxxkStats(String cookie, int sample) {
        return debugZxxkStatsByUrl(DEFAULT_ZXXK_ENTRY_URL, cookie, sample);
    }

    public String debugZxxkStatsByUrl(String url, String cookie, int sample) {
        Document doc = fetchZxxkDocument(url, cookie);
        String title = doc.title();
        String text = doc.text();
        Elements links = doc.select("a[href]");

        List<String> scriptSrc = doc.select("script[src]")
            .stream()
            .map(e -> e.attr("src"))
            .filter(s -> s != null && !s.isBlank())
            .limit(50)
            .collect(Collectors.toList());

        String titleB64 = Base64.getEncoder().encodeToString(title.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        int containGrade3 = 0;
        int containMath = 0;
        int containPaperWord = 0;
        int containGrade3Math = 0;
        int containGrade3MathPaper = 0;

        List<String> sampleGrade3MathPaperB64 = new ArrayList<>();
        List<String> sampleGrade3MathB64 = new ArrayList<>();
        List<String> samplePaperB64 = new ArrayList<>();

        int sampleSize = Math.min(Math.max(sample, 0), 50);
        List<String> samples = new ArrayList<>();
        for (Element a : links) {
            String t = a.text();
            if (t != null) {
                t = t.trim();
            }
            if (t != null && !t.isEmpty()) {
                boolean g3 = t.contains("三年级");
                boolean math = t.contains("数学");
                boolean paper = t.contains("试卷") || t.contains("测试") || t.contains("期末") || t.contains("期中") || t.contains("练习");

                if (g3) {
                    containGrade3++;
                }
                if (math) {
                    containMath++;
                }
                if (paper) {
                    containPaperWord++;
                }
                if (g3 && math) {
                    containGrade3Math++;
                    if (sampleGrade3MathB64.size() < sampleSize) {
                        sampleGrade3MathB64.add(Base64.getEncoder().encodeToString(t.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                    }
                }
                if (g3 && math && paper) {
                    containGrade3MathPaper++;
                    if (sampleGrade3MathPaperB64.size() < sampleSize) {
                        sampleGrade3MathPaperB64.add(Base64.getEncoder().encodeToString(t.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                    }
                }
                if (paper && samplePaperB64.size() < sampleSize) {
                    samplePaperB64.add(Base64.getEncoder().encodeToString(t.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                }
            }

            if (samples.size() >= sampleSize) {
                break;
            }
            if (t == null || t.isEmpty()) {
                continue;
            }
            samples.add(t);
        }

        return "title_b64=" + titleB64
            + "\ntextContains_grade3=" + text.contains("三年级")
            + "\ntextContains_math=" + text.contains("数学")
            + "\ntextContains_paperWord=" + (text.contains("试卷") || text.contains("测试") || text.contains("期末") || text.contains("期中") || text.contains("练习"))
            + "\nanchorCount=" + links.size()
            + "\nanchorContains_grade3=" + containGrade3
            + "\nanchorContains_math=" + containMath
            + "\nanchorContains(paperWord)=" + containPaperWord
            + "\nanchorContains_grade3_math=" + containGrade3Math
            + "\nanchorContains_grade3_math_paperWord=" + containGrade3MathPaper
            + "\nscripts_src=" + scriptSrc
            + "\nsampleAnchors_b64=" + samples.stream().map(s -> Base64.getEncoder().encodeToString(s.getBytes(java.nio.charset.StandardCharsets.UTF_8))).collect(Collectors.toList())
            + "\nsampleGrade3Math_b64=" + sampleGrade3MathB64
            + "\nsamplePaperWord_b64=" + samplePaperB64
            + "\nsampleGrade3MathPaperWord_b64=" + sampleGrade3MathPaperB64;
    }

    private Document fetchZxxkDocument(String url, String cookie) {
        try {
            Connection c = connect(url, cookie)
                .userAgent("Mozilla/5.0")
                .timeout(15000);
            Document doc = c.get();
            String html = doc.outerHtml();
            if (!looksLikeAntiBot(html)) {
                return doc;
            }

            String html2 = fetchHtmlWithPlaywright(url);
            if (looksLikeAntiBot(html2)) {
                throw new RuntimeException("zxxk anti-bot page still detected after Playwright");
            }
            return Jsoup.parse(html2, url);
        } catch (Exception e) {
            throw new RuntimeException("fetch zxxk document failed: " + e.getMessage(), e);
        }
    }

    private String fetchHtmlWithPlaywright(String url) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
             Page page = browser.newPage()) {
            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE).setTimeout(30000));
            page.waitForTimeout(1500);
            return page.content();
        }
    }

    private org.jsoup.Connection connect(String url, String cookie) {
        org.jsoup.Connection c = Jsoup.connect(url);
        if (cookie != null && !cookie.isBlank()) {
            c.header("Cookie", cookie);
        }
        return c;
    }

    private boolean looksLikeAntiBot(String html) {
        if (html == null) {
            return false;
        }
        return html.contains("alicfw")
            || html.contains("alicfw_gfver")
            || html.contains("function check()")
            || html.contains("parm_0")
            || html.contains("parm_1");
    }

    private LocalDate tryParseDate(String text) {
        if (text == null) {
            return null;
        }

        Matcher m1 = Pattern.compile("(\\d{4})[-./](\\d{1,2})[-./](\\d{1,2})").matcher(text);
        if (m1.find()) {
            int y = Integer.parseInt(m1.group(1));
            int mo = Integer.parseInt(m1.group(2));
            int d = Integer.parseInt(m1.group(3));
            return LocalDate.of(y, mo, d);
        }

        Matcher m2 = Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日").matcher(text);
        if (m2.find()) {
            int y = Integer.parseInt(m2.group(1));
            int mo = Integer.parseInt(m2.group(2));
            int d = Integer.parseInt(m2.group(3));
            return LocalDate.of(y, mo, d);
        }

        Matcher m3 = Pattern.compile("(\\d{4})年(\\d{1,2})月").matcher(text);
        if (m3.find()) {
            int y = Integer.parseInt(m3.group(1));
            int mo = Integer.parseInt(m3.group(2));
            return LocalDate.of(y, mo, 1);
        }

        try {
            return LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-M-d", Locale.ROOT));
        } catch (Exception ignore) {
            return null;
        }
    }

    private int upsert(long stageId, long subjectId, String name, LocalDate paperDate, String regionCode) {
        Paper p = new Paper();
        p.setStageId(stageId);
        p.setSubjectId(subjectId);
        p.setName(name);
        p.setPaperType("FIXED");
        p.setTotalScore(100);
        p.setTimeLimitSec(3600);
        p.setVersion(1);
        p.setStatus("ONLINE");
        p.setPricingType("FREE");
        p.setPriceCent(0);
        p.setPaperDate(paperDate);
        p.setRegionCode(regionCode);

        try {
            paperMapper.insert(p);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
}
