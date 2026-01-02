package com.example.bicolorsphere.service;

import com.example.bicolorsphere.domain.SsqDraw;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SsqCrawlerService {

    private static final String BASE_URL = "http://kaijiang.zhcw.com/zhcw/inc/ssq/ssq_wqhg.jsp?pageNum=%d";
    private static final Pattern DRAW_NO_PATTERN = Pattern.compile("\\b(\\d{7})\\b");
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern NUM_PATTERN = Pattern.compile("\\b(\\d{1,2})\\b");

    public List<SsqDraw> fetchPage(int pageNum) throws IOException {
        String url = String.format(BASE_URL, pageNum);
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(15_000)
                .get();

        Elements rows = doc.select("tr");
        List<SsqDraw> result = new ArrayList<>();

        for (Element tr : rows) {
            String text = tr.text();

            String drawNo = tryParseDrawNo(tr);
            if (drawNo == null) {
                Matcher noM = DRAW_NO_PATTERN.matcher(text);
                if (!noM.find()) {
                    continue;
                }
                drawNo = noM.group(1);
            }

            LocalDate drawDate = tryParseDate(tr);
            if (drawDate == null) {
                Matcher dateM = DATE_PATTERN.matcher(text);
                if (dateM.find()) {
                    drawDate = LocalDate.parse(dateM.group(1), DateTimeFormatter.ISO_LOCAL_DATE);
                }
            }

            List<Integer> numbers = tryParseNumbersByDom(tr);
            if (numbers.size() < 7) {
                numbers = parseNumbersFallback(text);
            }

            if (numbers.size() < 7) {
                continue;
            }

            List<Integer> reds = new ArrayList<>(numbers.subList(0, 6));
            Collections.sort(reds);
            int blue = numbers.get(6);
            if (!isValid(reds, blue)) {
                continue;
            }

            result.add(new SsqDraw(drawNo, drawDate, reds, blue));
        }

        return result;
    }

    private static String tryParseDrawNo(Element tr) {
        Elements tds = tr.select("td");
        if (tds.isEmpty()) {
            return null;
        }
        String c0 = tds.get(0).text();
        Matcher m = DRAW_NO_PATTERN.matcher(c0);
        return m.find() ? m.group(1) : null;
    }

    private static LocalDate tryParseDate(Element tr) {
        Elements tds = tr.select("td");
        for (Element td : tds) {
            String s = td.text();
            Matcher m = DATE_PATTERN.matcher(s);
            if (m.find()) {
                return LocalDate.parse(m.group(1), DateTimeFormatter.ISO_LOCAL_DATE);
            }
        }
        return null;
    }

    private static List<Integer> tryParseNumbersByDom(Element tr) {
        List<Integer> nums = new ArrayList<>();

        Elements balls = tr.select("em, span");
        for (Element e : balls) {
            String s = e.text();
            if (s == null || s.trim().isEmpty()) {
                continue;
            }
            if (!s.matches("\\d{1,2}")) {
                continue;
            }
            int v = Integer.parseInt(s);
            if (v >= 1 && v <= 33) {
                nums.add(v);
            }
        }

        if (nums.size() >= 7) {
            int blue = nums.get(6);
            if (blue > 16) {
                return new ArrayList<>();
            }
            return nums.subList(0, 7);
        }
        return nums;
    }

    private static List<Integer> parseNumbersFallback(String text) {
        List<Integer> nums = new ArrayList<>();
        Matcher numM = NUM_PATTERN.matcher(text);
        while (numM.find()) {
            int v = Integer.parseInt(numM.group(1));
            if (v >= 1 && v <= 33) {
                nums.add(v);
            }
        }
        if (nums.size() < 7) {
            return nums;
        }
        int blue = nums.get(6);
        if (blue > 16) {
            return new ArrayList<>();
        }
        return nums.subList(0, 7);
    }

    private static boolean isValid(List<Integer> reds, int blue) {
        if (reds.size() != 6) {
            return false;
        }
        for (int r : reds) {
            if (r < 1 || r > 33) {
                return false;
            }
        }
        if (blue < 1 || blue > 16) {
            return false;
        }
        return reds.stream().distinct().count() == 6;
    }
}
