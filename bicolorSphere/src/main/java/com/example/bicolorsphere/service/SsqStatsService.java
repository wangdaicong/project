package com.example.bicolorsphere.service;

import com.example.bicolorsphere.domain.SsqDraw;
import com.example.bicolorsphere.repo.SsqDrawRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Service
public class SsqStatsService {

    private final SsqDrawRepository repository;

    private static volatile String ENSEMBLE_TUNED_CACHE_KEY = null;
    private static volatile long ENSEMBLE_TUNED_CACHE_AT = 0L;
    private static volatile int[] ENSEMBLE_TUNED_CACHE_W = null;

    public SsqStatsService(SsqDrawRepository repository) {
        this.repository = repository;
    }

    public static class PredictOptions {
        private Integer minSum;
        private Integer maxSum;
        private Integer minSpan;
        private Integer maxSpan;
        private Integer minOdd;
        private Integer maxOdd;
        private int[] zoneRatio;
        private Set<Integer> danReds;
        private Set<Integer> killReds;
        private Set<Integer> danBlues;
        private Set<Integer> killBlues;
        private int maxTry = 120;

        public Map<String, Object> asMap() {
            return map(
                    "minSum", minSum,
                    "maxSum", maxSum,
                    "minSpan", minSpan,
                    "maxSpan", maxSpan,
                    "minOdd", minOdd,
                    "maxOdd", maxOdd,
                    "zoneRatio", zoneRatio == null ? null : ("" + zoneRatio[0] + ":" + zoneRatio[1] + ":" + zoneRatio[2]),
                    "danReds", danReds,
                    "killReds", killReds,
                    "danBlues", danBlues,
                    "killBlues", killBlues,
                    "maxTry", maxTry
            );
        }

        public Integer getMinSum() { return minSum; }
        public void setMinSum(Integer minSum) { this.minSum = minSum; }
        public Integer getMaxSum() { return maxSum; }
        public void setMaxSum(Integer maxSum) { this.maxSum = maxSum; }
        public Integer getMinSpan() { return minSpan; }
        public void setMinSpan(Integer minSpan) { this.minSpan = minSpan; }
        public Integer getMaxSpan() { return maxSpan; }
        public void setMaxSpan(Integer maxSpan) { this.maxSpan = maxSpan; }
        public Integer getMinOdd() { return minOdd; }
        public void setMinOdd(Integer minOdd) { this.minOdd = minOdd; }
        public Integer getMaxOdd() { return maxOdd; }
        public void setMaxOdd(Integer maxOdd) { this.maxOdd = maxOdd; }
        public int[] getZoneRatio() { return zoneRatio; }
        public void setZoneRatio(int[] zoneRatio) { this.zoneRatio = zoneRatio; }
        public Set<Integer> getDanReds() { return danReds; }
        public void setDanReds(Set<Integer> danReds) { this.danReds = danReds; }
        public Set<Integer> getKillReds() { return killReds; }
        public void setKillReds(Set<Integer> killReds) { this.killReds = killReds; }
        public Set<Integer> getDanBlues() { return danBlues; }
        public void setDanBlues(Set<Integer> danBlues) { this.danBlues = danBlues; }
        public Set<Integer> getKillBlues() { return killBlues; }
        public void setKillBlues(Set<Integer> killBlues) { this.killBlues = killBlues; }
        public int getMaxTry() { return maxTry; }
        public void setMaxTry(int maxTry) { this.maxTry = maxTry; }

        public static int[] parseZoneRatio(String s) {
            if (s == null) return null;
            String t = s.trim();
            if (t.isEmpty()) return null;
            String[] parts = t.split("[:：]");
            if (parts.length != 3) return null;
            try {
                int a = Integer.parseInt(parts[0].trim());
                int b = Integer.parseInt(parts[1].trim());
                int c = Integer.parseInt(parts[2].trim());
                if (a + b + c != 6) return null;
                return new int[]{a, b, c};
            } catch (Exception e) {
                return null;
            }
        }

        public static Set<Integer> parseNumSet(String s, int min, int max) {
            if (s == null) return null;
            String t = s.trim();
            if (t.isEmpty()) return null;
            String[] parts = t.split("[,，\\s]+");
            Set<Integer> set = new HashSet<>();
            for (String p : parts) {
                String q = p == null ? "" : p.trim();
                if (q.isEmpty()) continue;
                try {
                    int v = Integer.parseInt(q);
                    if (v >= min && v <= max) set.add(v);
                } catch (Exception ignored) {
                }
            }
            return set.isEmpty() ? null : set;
        }
    }

    public Map<String, Object> hotCold(int latestN) {
        List<SsqDraw> draws = repository.latest(latestN);
        int[] redCnt = new int[34];
        int[] blueCnt = new int[17];

        for (SsqDraw d : draws) {
            for (int r : d.getReds()) {
                if (r >= 1 && r <= 33) redCnt[r]++;
            }
            int b = d.getBlue();
            if (b >= 1 && b <= 16) blueCnt[b]++;
        }

        return map(
                "latestN", latestN,
                "red", toList(redCnt, 1, 33),
                "blue", toList(blueCnt, 1, 16)
        );
    }

    public Map<String, Object> trend(int latestN) {
        List<SsqDraw> draws = repository.latest(latestN);
        Collections.reverse(draws);

        List<String> drawNos = new ArrayList<>();
        List<String> drawDates = new ArrayList<>();
        List<int[]> reds = new ArrayList<>();
        List<Integer> blues = new ArrayList<>();

        for (SsqDraw d : draws) {
            drawNos.add(d.getDrawNo());
            drawDates.add(d.getDrawDate() == null ? "" : d.getDrawDate().toString());
            int[] r = new int[6];
            for (int i = 0; i < 6; i++) {
                r[i] = d.getReds().get(i);
            }
            reds.add(r);
            blues.add(d.getBlue());
        }

        return map(
                "latestN", latestN,
                "drawNos", drawNos,
                "drawDates", drawDates,
                "reds", reds,
                "blues", blues
        );
    }

    public Map<String, Object> predict(int latestN) {
        List<SsqDraw> draws = repository.latest(latestN);
        int[] redCnt = new int[34];
        int[] blueCnt = new int[17];

        for (SsqDraw d : draws) {
            for (int r : d.getReds()) {
                if (r >= 1 && r <= 33) redCnt[r]++;
            }
            int b = d.getBlue();
            if (b >= 1 && b <= 16) blueCnt[b]++;
        }

        List<Integer> redPick = topK(redCnt, 6, 1, 33);
        Collections.sort(redPick);
        int bluePick = topK(blueCnt, 1, 1, 16).get(0);

        return map(
                "strategy", "frequency_top",
                "latestN", latestN,
                "red", redPick,
                "blue", bluePick,
                "disclaimer", "预测仅供娱乐，不构成任何保证或建议。"
        );
    }

    public Map<String, Object> omission(int latestN) {
        List<SsqDraw> draws = repository.latest(latestN);
        Collections.reverse(draws);

        int[] redMiss = initMissArray(33);
        int[] blueMiss = initMissArray(16);

        for (SsqDraw d : draws) {
            boolean[] redHit = new boolean[34];
            for (int r : d.getReds()) {
                if (r >= 1 && r <= 33) {
                    redHit[r] = true;
                }
            }
            for (int i = 1; i <= 33; i++) {
                redMiss[i] = redHit[i] ? 0 : (redMiss[i] + 1);
            }

            boolean blueHit = d.getBlue() >= 1 && d.getBlue() <= 16;
            for (int i = 1; i <= 16; i++) {
                blueMiss[i] = (blueHit && d.getBlue() == i) ? 0 : (blueMiss[i] + 1);
            }
        }

        return map(
                "latestN", latestN,
                "red", toList(redMiss, 1, 33),
                "blue", toList(blueMiss, 1, 16)
        );
    }

    public Map<String, Object> predict(int latestN, String strategy, int count) {
        return predict(latestN, strategy, count, null);
    }

    public Map<String, Object> predict(int latestN, String strategy, int count, PredictOptions options) {
        String raw = strategy == null ? "frequency_top" : strategy.trim();
        String s = normalizeStrategy(raw);
        int c = Math.max(1, Math.min(20, count));

        List<SsqDraw> draws = repository.latest(latestN);
        List<Map<String, Object>> picks = new ArrayList<>();

        Map<String, Object> explain = null;
        if (isMlFamily(s)) {
            explain = buildMlExplain(draws);
        }

        for (int i = 0; i < c; i++) {
            Pick p = pickFromDraws(draws, s, i, options);
            if (p == null) {
                return map(
                        "strategy", "ml",
                        "strategyAlias", raw,
                        "latestN", latestN,
                        "count", c,
                        "options", options == null ? null : options.asMap(),
                        "explain", explain,
                        "error", "无法在 maxTry 次尝试内生成满足约束条件的号码，请适当放宽和值/跨度/区间比/奇偶比/胆杀等条件或提高 maxTry。",
                        "disclaimer", "预测仅供娱乐，不构成任何保证或建议。"
                );
            }
            List<Integer> red = new ArrayList<Integer>(p.getReds());
            Collections.sort(red);
            picks.add(map("red", red, "blue", p.getBlue()));
        }

        return map(
                "strategy", isMlFamily(s) ? "ml" : s,
                "strategyAlias", isMlFamily(s) ? raw : null,
                "latestN", latestN,
                "count", c,
                "picks", picks,
                "options", options == null ? null : options.asMap(),
                "explain", explain,
                "disclaimer", "预测仅供娱乐，不构成任何保证或建议。"
        );
    }

    public Map<String, Object> backtest(String strategy, int trainWindow, int testCount) {
        return backtest(strategy, trainWindow, testCount, null);
    }

    public Map<String, Object> backtest(String strategy, int trainWindow, int testCount, PredictOptions options) {
        String s = strategy == null ? "frequency_top" : strategy.trim();

        int train = Math.max(50, trainWindow);
        int test = Math.max(10, testCount);

        List<SsqDraw> all = repository.latest(train + test);
        Collections.reverse(all);
        if (all.size() < train + test) {
            return map(
                    "strategy", s,
                    "trainWindow", train,
                    "testCount", test,
                    "error", "数据量不足，先同步更多历史数据"
            );
        }

        int redHitTotal = 0;
        int blueHitTotal = 0;
        int bothHitTotal = 0;
        int[] redHitDist = new int[7];
        List<Map<String, Object>> samples = new ArrayList<>();

        for (int i = 0; i < test; i++) {
            int trainStart = i;
            int trainEnd = i + train;
            List<SsqDraw> trainSet = all.subList(trainStart, trainEnd);
            SsqDraw actual = all.get(trainEnd);

            Pick pick = pickFromDraws(trainSet, s, i, options);

            int redHits = 0;
            Set<Integer> actualR = new HashSet<>(actual.getReds());
            for (int r : pick.getReds()) {
                if (actualR.contains(r)) {
                    redHits++;
                }
            }
            boolean blueHit = pick.getBlue() == actual.getBlue();

            redHitTotal += redHits;
            blueHitTotal += blueHit ? 1 : 0;
            bothHitTotal += (blueHit && redHits > 0) ? 1 : 0;
            if (redHits >= 0 && redHits <= 6) {
                redHitDist[redHits] += 1;
            }

            if (i < 10) {
                samples.add(map(
                        "predictRed", pick.getReds(),
                        "predictBlue", pick.getBlue(),
                        "actualDrawNo", actual.getDrawNo(),
                        "actualRed", actual.getReds(),
                        "actualBlue", actual.getBlue(),
                        "redHits", redHits,
                        "blueHit", blueHit
                ));
            }
        }

        double avgRedHits = redHitTotal * 1.0 / test;
        double blueHitRate = blueHitTotal * 1.0 / test;

        // 一个简单的综合评分：偏向“稳”（红球平均命中 + 蓝球命中率加权 + 红球>=2命中率）
        double red2plusRate = (redHitDist[2] + redHitDist[3] + redHitDist[4] + redHitDist[5] + redHitDist[6]) * 1.0 / test;
        double score = avgRedHits + (blueHitRate * 1.2) + (red2plusRate * 0.8);

        return map(
                "strategy", s,
                "trainWindow", train,
                "testCount", test,
                "avgRedHits", avgRedHits,
                "blueHitRate", blueHitRate,
                "redHitDist", map(
                        "0", redHitDist[0],
                        "1", redHitDist[1],
                        "2", redHitDist[2],
                        "3", redHitDist[3],
                        "4", redHitDist[4],
                        "5", redHitDist[5],
                        "6", redHitDist[6]
                ),
                "red2plusRate", red2plusRate,
                "bothHitCount", bothHitTotal,
                "score", score,
                "options", options == null ? null : options.asMap(),
                "samples", samples
        );
    }

    public Map<String, Object> recommend(int trainWindow, int testCount) {
        int train = Math.max(50, trainWindow);
        int test = Math.max(10, testCount);

        List<Map<String, Object>> candidates = new ArrayList<>();

        // 基础：不带约束
        candidates.add(recommendCandidate("hybrid", train, test, null));
        candidates.add(recommendCandidate("weighted_random", train, test, null));
        candidates.add(recommendCandidate("frequency_top", train, test, null));
        candidates.add(recommendCandidate("omission_top", train, test, null));

        // 常用约束：2:2:2 + 奇数 2~4 + 适度重试
        PredictOptions z222 = new PredictOptions();
        z222.setZoneRatio(new int[]{2, 2, 2});
        z222.setMinOdd(2);
        z222.setMaxOdd(4);
        z222.setMaxTry(200);
        candidates.add(recommendCandidate("zone_balanced", train, test, z222));
        candidates.add(recommendCandidate("weighted_random", train, test, z222));

        // 稍微宽松和值/跨度（避免极端）：和值 70~140，跨度 15~28
        PredictOptions z222SumSpan = new PredictOptions();
        z222SumSpan.setZoneRatio(new int[]{2, 2, 2});
        z222SumSpan.setMinOdd(2);
        z222SumSpan.setMaxOdd(4);
        z222SumSpan.setMinSum(70);
        z222SumSpan.setMaxSum(140);
        z222SumSpan.setMinSpan(15);
        z222SumSpan.setMaxSpan(28);
        z222SumSpan.setMaxTry(260);
        candidates.add(recommendCandidate("zone_balanced", train, test, z222SumSpan));
        candidates.add(recommendCandidate("weighted_random", train, test, z222SumSpan));

        // 选出最佳 score
        Map<String, Object> best = null;
        double bestScore = -1e9;
        for (int i = 0; i < candidates.size(); i++) {
            Map<String, Object> c = candidates.get(i);
            if (c == null) continue;
            Object sObj = c.get("score");
            double sc = (sObj instanceof Number) ? ((Number) sObj).doubleValue() : -1e9;
            if (sc > bestScore) {
                bestScore = sc;
                best = c;
            }
        }

        return map(
                "trainWindow", train,
                "testCount", test,
                "best", best,
                "candidates", candidates,
                "disclaimer", "推荐基于历史回测，仅供娱乐，不构成任何保证或建议。"
        );
    }

    private Map<String, Object> recommendCandidate(String strategy, int train, int test, PredictOptions opt) {
        Map<String, Object> r = backtest(strategy, train, test, opt);
        if (r == null) return null;
        if (r.get("error") != null) {
            // 数据不足时直接返回错误（上层会一起带出去）
            return r;
        }
        // 带上标识，便于前端展示
        r.put("candidateStrategy", strategy);
        return r;
    }

    private Map<String, Object> pickOne(int latestN, String strategy, int salt, PredictOptions options) {
        List<SsqDraw> draws = repository.latest(latestN);
        Pick p = pickFromDraws(draws, strategy, salt, options);
        List<Integer> red = new ArrayList<Integer>(p.getReds());
        Collections.sort(red);
        return map("red", red, "blue", p.getBlue());
    }

    private Pick pickFromDraws(List<SsqDraw> draws, String strategy, int salt, PredictOptions options) {
        int[] redCnt = new int[34];
        int[] blueCnt = new int[17];
        int[] redMiss = initMissArray(33);
        int[] blueMiss = initMissArray(16);

        List<SsqDraw> ordered = new ArrayList<>(draws);
        Collections.reverse(ordered);

        for (SsqDraw d : ordered) {
            boolean[] redHit = new boolean[34];
            for (int r : d.getReds()) {
                if (r >= 1 && r <= 33) {
                    redCnt[r]++;
                    redHit[r] = true;
                }
            }
            for (int i = 1; i <= 33; i++) {
                redMiss[i] = redHit[i] ? 0 : (redMiss[i] + 1);
            }

            int b = d.getBlue();
            if (b >= 1 && b <= 16) {
                blueCnt[b]++;
            }
            for (int i = 1; i <= 16; i++) {
                blueMiss[i] = (b == i) ? 0 : (blueMiss[i] + 1);
            }
        }

        String s = strategy == null ? "frequency_top" : strategy;
        PredictOptions opt = options == null ? new PredictOptions() : options;
        SecureRandom rnd = new SecureRandom(("ssq-" + salt).getBytes());

        SsqDraw last = ordered.isEmpty() ? null : ordered.get(ordered.size() - 1);
        int[][] redTrans = null;
        int[] blueTransFromLast = null;
        int[] bayesRedScore = null;
        int[] bayesBlueScore = null;
        if ("markov".equalsIgnoreCase(s) || "ml".equalsIgnoreCase(s) || "ensemble".equalsIgnoreCase(s) || "ensemble_tuned".equalsIgnoreCase(s)) {
            redTrans = buildRedTransition(ordered);
            blueTransFromLast = buildBlueFromLastTransition(ordered);
        }
        if ("bayes".equalsIgnoreCase(s) || "ml".equalsIgnoreCase(s) || "ensemble".equalsIgnoreCase(s) || "ensemble_tuned".equalsIgnoreCase(s)) {
            int bucket = last == null ? 0 : featureBucket(last);
            bayesRedScore = buildBayesRedScore(ordered, bucket);
            bayesBlueScore = buildBayesBlueScore(ordered, bucket);
        }

        // 约束过滤：生成-校验，不通过则重试（避免直接 topK 造成过于固定）
        int maxTry = Math.max(50, opt.getMaxTry());
        for (int t = 0; t < maxTry; t++) {
            Pick candidate = pickRaw(redCnt, blueCnt, redMiss, blueMiss, ordered, last, redTrans, blueTransFromLast,
                    bayesRedScore, bayesBlueScore, s, salt + t, rnd, opt);
            if (candidate == null) continue;
            if (accept(candidate, opt)) return candidate;
        }

        // 若用户设置了任何约束：宁可失败也不要返回不满足约束的号码
        if (hasConstraints(opt)) {
            return null;
        }

        // 无约束兜底：返回一次不带过滤的结果，保证接口稳定
        Pick fallback = pickRaw(redCnt, blueCnt, redMiss, blueMiss, ordered, last, redTrans, blueTransFromLast,
                bayesRedScore, bayesBlueScore, s, salt, rnd, new PredictOptions());
        if (fallback != null) return fallback;
        List<Integer> red = topK(redCnt, 6, 1, 33);
        int blue = topK(blueCnt, 1, 1, 16).get(0);
        return new Pick(red, blue);
    }

    private static boolean hasConstraints(PredictOptions opt) {
        if (opt == null) return false;
        if (opt.getMinSum() != null || opt.getMaxSum() != null) return true;
        if (opt.getMinSpan() != null || opt.getMaxSpan() != null) return true;
        if (opt.getMinOdd() != null || opt.getMaxOdd() != null) return true;
        if (opt.getZoneRatio() != null) return true;
        if (opt.getDanReds() != null && !opt.getDanReds().isEmpty()) return true;
        if (opt.getKillReds() != null && !opt.getKillReds().isEmpty()) return true;
        if (opt.getDanBlues() != null && !opt.getDanBlues().isEmpty()) return true;
        if (opt.getKillBlues() != null && !opt.getKillBlues().isEmpty()) return true;
        return false;
    }

    private Pick pickRaw(int[] redCnt, int[] blueCnt, int[] redMiss, int[] blueMiss,
                         List<SsqDraw> ordered, SsqDraw last,
                         int[][] redTrans, int[] blueTransFromLast,
                         int[] bayesRedScore, int[] bayesBlueScore,
                         String strategy, int salt, Random rnd, PredictOptions opt) {
        String s = strategy == null ? "frequency_top" : strategy;

        // ml 家族：统一用集成策略（更稳），外部不再暴露 ensemble/ensemble_tuned
        if (isMlFamily(s)) {
            if (last == null) {
                List<Integer> red = topK(redCnt, 6, 1, 33);
                int blue = topK(blueCnt, 1, 1, 16).get(0);
                return new Pick(red, blue);
            }

            int[] bestW = getOrTuneEnsembleWeights(ordered, salt);
            int wHybrid = bestW == null || bestW.length < 4 ? 250 : bestW[0];
            int wMarkov = bestW == null || bestW.length < 4 ? 250 : bestW[1];
            int wBayes = bestW == null || bestW.length < 4 ? 200 : bestW[2];
            int wMl = bestW == null || bestW.length < 4 ? 300 : bestW[3];

            int[] redScore = buildEnsembleRedScore(redCnt, redMiss, ordered, last, redTrans, bayesRedScore, wHybrid, wMarkov, wBayes, wMl);
            int[] blueScore = buildEnsembleBlueScore(blueCnt, blueMiss, ordered, last, blueTransFromLast, bayesBlueScore, wHybrid, wMarkov, wBayes, wMl);

            List<Integer> red = weightedSampleWithoutReplacementInRange(redScore, redMiss, 6, 1, 33, rnd, opt);
            int blue = weightedSampleOneWithFilter(blueScore, blueMiss, 1, 16, rnd, opt);
            return new Pick(red, blue);
        }

        if ("zone_balanced".equalsIgnoreCase(s)) {
            // 分区均衡：一区(1-11)/二区(12-22)/三区(23-33) 各取2个
            List<Integer> red = new ArrayList<>();
            red.addAll(weightedSampleWithoutReplacementInRange(redCnt, redMiss, 2, 1, 11, rnd, opt));
            red.addAll(weightedSampleWithoutReplacementInRange(redCnt, redMiss, 2, 12, 22, rnd, opt));
            red.addAll(weightedSampleWithoutReplacementInRange(redCnt, redMiss, 2, 23, 33, rnd, opt));
            int blue = weightedSampleOneWithFilter(blueCnt, blueMiss, 1, 16, rnd, opt);
            return new Pick(red, blue);
        }

        if ("omission_top".equalsIgnoreCase(s)) {
            List<Integer> red = topK(redMiss, 6, 1, 33);
            int blue = topK(blueMiss, 1, 1, 16).get(0);
            return new Pick(red, blue);
        }
        if ("hybrid".equalsIgnoreCase(s)) {
            int[] redScore = new int[34];
            int[] blueScore = new int[17];
            for (int i = 1; i <= 33; i++) {
                redScore[i] = redCnt[i] * 2 + redMiss[i];
            }
            for (int i = 1; i <= 16; i++) {
                blueScore[i] = blueCnt[i] * 2 + blueMiss[i];
            }
            List<Integer> red = topK(redScore, 6, 1, 33);
            int blue = topK(blueScore, 1, 1, 16).get(0);
            return new Pick(red, blue);
        }
        if ("weighted_random".equalsIgnoreCase(s)) {
            SecureRandom rr = new SecureRandom(("ssq-" + salt).getBytes());
            List<Integer> red = weightedSampleWithoutReplacementInRange(redCnt, redMiss, 6, 1, 33, rr, opt);
            int blue = weightedSampleOneWithFilter(blueCnt, blueMiss, 1, 16, rr, opt);
            return new Pick(red, blue);
        }

        if ("markov".equalsIgnoreCase(s)) {
            if (last == null) {
                List<Integer> red = topK(redCnt, 6, 1, 33);
                int blue = topK(blueCnt, 1, 1, 16).get(0);
                return new Pick(red, blue);
            }
            int[] redScore = buildMarkovRedScore(last, redTrans, redCnt, redMiss);
            List<Integer> red = weightedSampleWithoutReplacementInRange(redScore, redMiss, 6, 1, 33, rnd, opt);
            int blue = buildMarkovBluePick(last, blueTransFromLast, blueCnt, blueMiss, rnd, opt);
            return new Pick(red, blue);
        }

        if ("bayes".equalsIgnoreCase(s)) {
            if (bayesRedScore == null || bayesBlueScore == null) {
                List<Integer> red = topK(redCnt, 6, 1, 33);
                int blue = topK(blueCnt, 1, 1, 16).get(0);
                return new Pick(red, blue);
            }
            List<Integer> red = weightedSampleWithoutReplacementInRange(bayesRedScore, redMiss, 6, 1, 33, rnd, opt);
            int blue = weightedSampleOneWithFilter(bayesBlueScore, blueMiss, 1, 16, rnd, opt);
            return new Pick(red, blue);
        }

        // frequency_top
        List<Integer> red = topK(redCnt, 6, 1, 33);
        int blue = topK(blueCnt, 1, 1, 16).get(0);
        return new Pick(red, blue);
    }

    private static boolean isMlFamily(String s) {
        if (s == null) return false;
        return "ml".equalsIgnoreCase(s) || "ensemble".equalsIgnoreCase(s) || "ensemble_tuned".equalsIgnoreCase(s);
    }

    private static String normalizeStrategy(String raw) {
        if (raw == null) return "frequency_top";
        String s = raw.trim();
        if (isMlFamily(s)) return "ml";
        return s;
    }

    private static Map<String, Object> buildMlExplain(List<SsqDraw> draws) {
        if (draws == null || draws.isEmpty()) return null;
        List<SsqDraw> ordered = new ArrayList<>(draws);
        Collections.reverse(ordered);

        int[] w = getOrTuneEnsembleWeights(ordered, 0);
        return map(
                "mode", "ensemble_tuned",
                "weights", map(
                        "hybrid", w == null ? null : w[0],
                        "markov", w == null ? null : w[1],
                        "bayes", w == null ? null : w[2],
                        "ml", w == null ? null : w[3]
                ),
                "cacheTtlMinutes", 10
        );
    }

    private static int[][] buildRedTransition(List<SsqDraw> ordered) {
        int[][] trans = new int[34][34];
        if (ordered == null || ordered.size() < 2) return trans;
        for (int i = 0; i + 1 < ordered.size(); i++) {
            SsqDraw prev = ordered.get(i);
            SsqDraw next = ordered.get(i + 1);
            if (prev == null || next == null) continue;
            for (Integer a : prev.getReds()) {
                if (a == null || a < 1 || a > 33) continue;
                for (Integer b : next.getReds()) {
                    if (b == null || b < 1 || b > 33) continue;
                    trans[a][b] += 1;
                }
            }
        }
        return trans;
    }

    private static int[] buildBlueFromLastTransition(List<SsqDraw> ordered) {
        int[] score = new int[17];
        if (ordered == null || ordered.size() < 2) return score;
        int lastBlue = ordered.get(ordered.size() - 1).getBlue();
        int[][] trans = new int[17][17];
        for (int i = 0; i + 1 < ordered.size(); i++) {
            int a = ordered.get(i).getBlue();
            int b = ordered.get(i + 1).getBlue();
            if (a >= 1 && a <= 16 && b >= 1 && b <= 16) {
                trans[a][b] += 1;
            }
        }
        if (lastBlue >= 1 && lastBlue <= 16) {
            System.arraycopy(trans[lastBlue], 0, score, 0, score.length);
        }
        return score;
    }

    private static int[] getOrTuneEnsembleWeights(List<SsqDraw> ordered, int salt) {
        if (ordered == null || ordered.size() < 80) return new int[]{250, 250, 200, 300};
        SsqDraw last = ordered.get(ordered.size() - 1);
        String lastNo = last == null ? "" : String.valueOf(last.getDrawNo());
        String key = lastNo + ":" + ordered.size();
        long now = System.currentTimeMillis();
        if (key.equals(ENSEMBLE_TUNED_CACHE_KEY) && ENSEMBLE_TUNED_CACHE_W != null && (now - ENSEMBLE_TUNED_CACHE_AT) < 10 * 60 * 1000L) {
            return ENSEMBLE_TUNED_CACHE_W;
        }

        int testCount = 20;
        int total = ordered.size();
        if (total <= testCount + 30) return new int[]{250, 250, 200, 300};

        // 小网格：控制组合数，保证接口响应速度
        int[] hybridCandidates = new int[]{150, 250, 350};
        int[] markovCandidates = new int[]{200, 300, 400};
        int[] bayesCandidates = new int[]{100, 200, 300};

        double bestScore = -1e18;
        int[] best = new int[]{250, 250, 200, 300};
        for (int h : hybridCandidates) {
            for (int m : markovCandidates) {
                for (int b : bayesCandidates) {
                    int ml = 1000 - h - m - b;
                    if (ml < 100 || ml > 600) continue;
                    double sc = rollingScoreEnsemble(ordered, h, m, b, ml, testCount, salt);
                    if (sc > bestScore) {
                        bestScore = sc;
                        best = new int[]{h, m, b, ml};
                    }
                }
            }
        }

        ENSEMBLE_TUNED_CACHE_KEY = key;
        ENSEMBLE_TUNED_CACHE_AT = now;
        ENSEMBLE_TUNED_CACHE_W = best;
        return best;
    }

    private static double rollingScoreEnsemble(List<SsqDraw> ordered, int wHybrid, int wMarkov, int wBayes, int wMl, int testCount, int salt) {
        int total = ordered.size();
        int start = Math.max(60, total - testCount);
        double sum = 0.0;
        for (int i = start; i < total; i++) {
            List<SsqDraw> train = ordered.subList(0, i);
            SsqDraw actual = ordered.get(i);
            if (actual == null || actual.getReds() == null || actual.getReds().size() != 6) continue;
            Pick pred = ensemblePredictOnce(train, wHybrid, wMarkov, wBayes, wMl, salt + i);
            if (pred == null) continue;

            int redHit = 0;
            Set<Integer> a = new HashSet<Integer>(actual.getReds());
            for (Integer r : pred.getReds()) {
                if (r != null && a.contains(r)) redHit++;
            }
            int blueHit = (pred.getBlue() == actual.getBlue()) ? 1 : 0;
            sum += redHit + blueHit * 1.5;
        }
        return sum;
    }

    private static Pick ensemblePredictOnce(List<SsqDraw> ordered, int wHybrid, int wMarkov, int wBayes, int wMl, int salt) {
        if (ordered == null || ordered.size() < 30) return null;

        int[] redCnt = new int[34];
        int[] blueCnt = new int[17];
        int[] redMiss = initMissArray(33);
        int[] blueMiss = initMissArray(16);

        for (SsqDraw d : ordered) {
            boolean[] redHit = new boolean[34];
            if (d != null && d.getReds() != null) {
                for (int r : d.getReds()) {
                    if (r >= 1 && r <= 33) {
                        redCnt[r]++;
                        redHit[r] = true;
                    }
                }
            }
            for (int i = 1; i <= 33; i++) {
                redMiss[i] = redHit[i] ? 0 : (redMiss[i] + 1);
            }

            int b = d == null ? 0 : d.getBlue();
            if (b >= 1 && b <= 16) blueCnt[b]++;
            for (int i = 1; i <= 16; i++) {
                blueMiss[i] = (b == i) ? 0 : (blueMiss[i] + 1);
            }
        }

        SsqDraw last = ordered.get(ordered.size() - 1);
        int[][] redTrans = buildRedTransition(ordered);
        int[] blueTransFromLast = buildBlueFromLastTransition(ordered);
        int bucket = last == null ? 0 : featureBucket(last);
        int[] bayesRedScore = buildBayesRedScore(ordered, bucket);
        int[] bayesBlueScore = buildBayesBlueScore(ordered, bucket);

        Random rnd = new SecureRandom(("ssq-ens-tune-" + salt).getBytes());
        int[] redScore = buildEnsembleRedScore(redCnt, redMiss, ordered, last, redTrans, bayesRedScore, wHybrid, wMarkov, wBayes, wMl);
        int[] blueScore = buildEnsembleBlueScore(blueCnt, blueMiss, ordered, last, blueTransFromLast, bayesBlueScore, wHybrid, wMarkov, wBayes, wMl);
        List<Integer> red = weightedSampleWithoutReplacementInRange(redScore, redMiss, 6, 1, 33, rnd, new PredictOptions());
        int blue = weightedSampleOneWithFilter(blueScore, blueMiss, 1, 16, rnd, new PredictOptions());
        return new Pick(red, blue);
    }

    private static int[] buildEnsembleRedScore(int[] redCnt, int[] redMiss, List<SsqDraw> ordered, SsqDraw last,
                                              int[][] redTrans, int[] bayesRedScore,
                                              int wHybrid, int wMarkov, int wBayes, int wMl) {
        int[] hybridRed = new int[34];
        for (int i = 1; i <= 33; i++) hybridRed[i] = redCnt[i] * 2 + redMiss[i];
        int[] markovRed = buildMarkovRedScore(last, redTrans, redCnt, redMiss);
        int[] mlRed = buildMlRedScore(redCnt, redMiss, markovRed, ordered);
        int[] bayesRed = bayesRedScore == null ? new int[34] : bayesRedScore;

        int[] nHybrid = normalizeScore(hybridRed, 1, 33);
        int[] nMarkov = normalizeScore(markovRed, 1, 33);
        int[] nBayes = normalizeScore(bayesRed, 1, 33);
        int[] nMl = normalizeScore(mlRed, 1, 33);

        int[] out = new int[34];
        for (int v = 1; v <= 33; v++) {
            long s = 0;
            s += (long) nHybrid[v] * wHybrid;
            s += (long) nMarkov[v] * wMarkov;
            s += (long) nBayes[v] * wBayes;
            s += (long) nMl[v] * wMl;
            out[v] = (int) Math.min(Integer.MAX_VALUE, Math.max(1L, s));
        }
        return out;
    }

    private static int[] buildEnsembleBlueScore(int[] blueCnt, int[] blueMiss, List<SsqDraw> ordered, SsqDraw last,
                                               int[] blueTransFromLast, int[] bayesBlueScore,
                                               int wHybrid, int wMarkov, int wBayes, int wMl) {
        int[] hybridBlue = new int[17];
        for (int i = 1; i <= 16; i++) hybridBlue[i] = blueCnt[i] * 2 + blueMiss[i];

        int[] markovBlue = new int[17];
        for (int v = 1; v <= 16; v++) {
            long s1 = blueTransFromLast == null ? 0 : blueTransFromLast[v];
            long base = (long) blueCnt[v] * 2L + Math.min(50, blueMiss[v]);
            markovBlue[v] = (int) Math.min(Integer.MAX_VALUE, (s1 * 10L + base));
        }

        int[] mlBlue = buildMlBlueScore(blueCnt, blueMiss, blueTransFromLast, ordered);
        int[] bayesBlue = bayesBlueScore == null ? new int[17] : bayesBlueScore;

        int[] nHybrid = normalizeScore(hybridBlue, 1, 16);
        int[] nMarkov = normalizeScore(markovBlue, 1, 16);
        int[] nBayes = normalizeScore(bayesBlue, 1, 16);
        int[] nMl = normalizeScore(mlBlue, 1, 16);

        int[] out = new int[17];
        for (int v = 1; v <= 16; v++) {
            long s = 0;
            s += (long) nHybrid[v] * wHybrid;
            s += (long) nMarkov[v] * wMarkov;
            s += (long) nBayes[v] * wBayes;
            s += (long) nMl[v] * wMl;
            out[v] = (int) Math.min(Integer.MAX_VALUE, Math.max(1L, s));
        }
        return out;
    }

    private static int[] normalizeScore(int[] score, int from, int to) {
        int[] out = new int[score == null ? 0 : score.length];
        if (score == null || score.length == 0) return out;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int v = from; v <= to; v++) {
            int s = (v >= 0 && v < score.length) ? score[v] : 0;
            if (s < min) min = s;
            if (s > max) max = s;
        }
        if (min == Integer.MAX_VALUE) return out;
        if (max <= min) {
            for (int v = from; v <= to; v++) {
                if (v >= 0 && v < out.length) out[v] = 1000;
            }
            return out;
        }
        long range = (long) max - (long) min;
        for (int v = from; v <= to; v++) {
            int s = (v >= 0 && v < score.length) ? score[v] : 0;
            long norm = ((long) s - (long) min) * 1000L / range;
            if (v >= 0 && v < out.length) out[v] = (int) Math.max(1L, norm + 1L);
        }
        return out;
    }

    private static int[] buildMarkovRedScore(SsqDraw last, int[][] redTrans, int[] redCnt, int[] redMiss) {
        int[] score = new int[34];
        if (last == null) return score;
        for (int v = 1; v <= 33; v++) {
            long s = 0;
            if (redTrans != null) {
                for (Integer a : last.getReds()) {
                    if (a == null || a < 1 || a > 33) continue;
                    s += redTrans[a][v];
                }
            }
            long base = (long) redCnt[v] * 2L + Math.min(50, redMiss[v]);
            score[v] = (int) Math.min(Integer.MAX_VALUE, (s * 10L + base));
        }
        return score;
    }

    private static int buildMarkovBluePick(SsqDraw last, int[] blueTransFromLast, int[] blueCnt, int[] blueMiss, Random rnd, PredictOptions opt) {
        int[] score = new int[17];
        for (int v = 1; v <= 16; v++) {
            long s = blueTransFromLast == null ? 0 : blueTransFromLast[v];
            long base = (long) blueCnt[v] * 2L + Math.min(50, blueMiss[v]);
            score[v] = (int) Math.min(Integer.MAX_VALUE, (s * 10L + base));
        }
        return weightedSampleOneWithFilter(score, blueMiss, 1, 16, rnd, opt);
    }

    private static int featureBucket(SsqDraw d) {
        if (d == null || d.getReds() == null || d.getReds().size() != 6) return 0;
        List<Integer> r = new ArrayList<>(d.getReds());
        Collections.sort(r);
        int sum = 0;
        int odd = 0;
        int z1 = 0, z2 = 0, z3 = 0;
        for (int i = 0; i < r.size(); i++) {
            int v = r.get(i);
            sum += v;
            if ((v & 1) == 1) odd++;
            if (v <= 11) z1++; else if (v <= 22) z2++; else z3++;
        }
        int span = r.get(r.size() - 1) - r.get(0);
        int sumB = Math.min(6, Math.max(0, sum / 30));
        int spanB = Math.min(6, Math.max(0, span / 5));
        int oddB = odd;
        int zoneB = (z1 * 100 + z2 * 10 + z3);
        return sumB * 1000 + spanB * 100 + oddB * 10 + (zoneB % 10);
    }

    private static int[] buildBayesRedScore(List<SsqDraw> ordered, int bucket) {
        int[] score = new int[34];
        if (ordered == null || ordered.size() < 2) return score;

        Map<Integer, int[]> byBucket = new HashMap<Integer, int[]>();
        for (int i = 0; i + 1 < ordered.size(); i++) {
            SsqDraw prev = ordered.get(i);
            SsqDraw next = ordered.get(i + 1);
            if (prev == null || next == null) continue;
            int b = featureBucket(prev);
            int[] cnt = byBucket.get(b);
            if (cnt == null) {
                cnt = new int[34];
                byBucket.put(b, cnt);
            }
            for (Integer r : next.getReds()) {
                if (r != null && r >= 1 && r <= 33) cnt[r]++;
            }
        }

        int[] cnt = byBucket.get(bucket);
        if (cnt == null) cnt = new int[34];
        for (int v = 1; v <= 33; v++) {
            score[v] = cnt[v] + 1;
        }
        return score;
    }

    private static int[] buildBayesBlueScore(List<SsqDraw> ordered, int bucket) {
        int[] score = new int[17];
        if (ordered == null || ordered.size() < 2) return score;

        Map<Integer, int[]> byBucket = new HashMap<Integer, int[]>();
        for (int i = 0; i + 1 < ordered.size(); i++) {
            SsqDraw prev = ordered.get(i);
            SsqDraw next = ordered.get(i + 1);
            if (prev == null || next == null) continue;
            int b = featureBucket(prev);
            int[] cnt = byBucket.get(b);
            if (cnt == null) {
                cnt = new int[17];
                byBucket.put(b, cnt);
            }
            int blue = next.getBlue();
            if (blue >= 1 && blue <= 16) cnt[blue]++;
        }

        int[] cnt = byBucket.get(bucket);
        if (cnt == null) cnt = new int[17];
        for (int v = 1; v <= 16; v++) {
            score[v] = cnt[v] + 1;
        }
        return score;
    }

    private static int[] buildMlRedScore(int[] redCnt, int[] redMiss, int[] markovRedScore, List<SsqDraw> ordered) {
        int[] recent = new int[34];
        int lookback = Math.min(30, ordered == null ? 0 : ordered.size());
        for (int i = Math.max(0, (ordered == null ? 0 : ordered.size()) - lookback); i < (ordered == null ? 0 : ordered.size()); i++) {
            SsqDraw d = ordered.get(i);
            if (d == null) continue;
            for (Integer r : d.getReds()) {
                if (r != null && r >= 1 && r <= 33) recent[r]++;
            }
        }

        int[] score = new int[34];
        for (int v = 1; v <= 33; v++) {
            long s = 0;
            s += redCnt[v] * 4L;
            s += Math.min(50, redMiss[v]) * 2L;
            s += recent[v] * 6L;
            if (markovRedScore != null) s += markovRedScore[v] * 2L;
            score[v] = (int) Math.min(Integer.MAX_VALUE, s);
        }
        return score;
    }

    private static int[] buildMlBlueScore(int[] blueCnt, int[] blueMiss, int[] blueTransFromLast, List<SsqDraw> ordered) {
        int[] recent = new int[17];
        int lookback = Math.min(40, ordered == null ? 0 : ordered.size());
        for (int i = Math.max(0, (ordered == null ? 0 : ordered.size()) - lookback); i < (ordered == null ? 0 : ordered.size()); i++) {
            SsqDraw d = ordered.get(i);
            if (d == null) continue;
            int b = d.getBlue();
            if (b >= 1 && b <= 16) recent[b]++;
        }

        int[] score = new int[17];
        for (int v = 1; v <= 16; v++) {
            long s = 0;
            s += blueCnt[v] * 4L;
            s += Math.min(50, blueMiss[v]) * 2L;
            s += recent[v] * 6L;
            if (blueTransFromLast != null) s += blueTransFromLast[v] * 20L;
            score[v] = (int) Math.min(Integer.MAX_VALUE, s);
        }
        return score;
    }

    private static boolean accept(Pick p, PredictOptions opt) {
        if (p == null) return false;
        List<Integer> reds = p.getReds() == null ? Collections.<Integer>emptyList() : p.getReds();
        if (reds.size() != 6) return false;

        // kill
        if (opt.getKillReds() != null && !opt.getKillReds().isEmpty()) {
            for (int r : reds) {
                if (opt.getKillReds().contains(r)) return false;
            }
        }
        if (opt.getKillBlues() != null && !opt.getKillBlues().isEmpty()) {
            if (opt.getKillBlues().contains(p.getBlue())) return false;
        }

        // dan: 必须包含
        if (opt.getDanReds() != null && !opt.getDanReds().isEmpty()) {
            Set<Integer> s = new HashSet<>(reds);
            if (!s.containsAll(opt.getDanReds())) return false;
        }
        if (opt.getDanBlues() != null && !opt.getDanBlues().isEmpty()) {
            if (!opt.getDanBlues().contains(p.getBlue())) return false;
        }

        // sum / span / odd
        int sum = 0;
        int odd = 0;
        int min = 99;
        int max = 0;
        int z1 = 0, z2 = 0, z3 = 0;
        for (int r : reds) {
            sum += r;
            if ((r & 1) == 1) odd++;
            if (r < min) min = r;
            if (r > max) max = r;
            if (r <= 11) z1++; else if (r <= 22) z2++; else z3++;
        }
        int span = max - min;

        if (opt.getMinSum() != null && sum < opt.getMinSum()) return false;
        if (opt.getMaxSum() != null && sum > opt.getMaxSum()) return false;
        if (opt.getMinSpan() != null && span < opt.getMinSpan()) return false;
        if (opt.getMaxSpan() != null && span > opt.getMaxSpan()) return false;
        if (opt.getMinOdd() != null && odd < opt.getMinOdd()) return false;
        if (opt.getMaxOdd() != null && odd > opt.getMaxOdd()) return false;

        if (opt.getZoneRatio() != null) {
            int[] zr = opt.getZoneRatio();
            if (zr.length == 3) {
                if (z1 != zr[0] || z2 != zr[1] || z3 != zr[2]) return false;
            }
        }
        return true;
    }

    private static List<Integer> weightedSampleWithoutReplacementInRange(int[] cnt, int[] miss, int k, int from, int to, Random rnd, PredictOptions opt) {
        List<Integer> pool = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            if (opt.getKillReds() != null && opt.getKillReds().contains(i)) continue;
            pool.add(i);
        }
        // 先把胆号塞进去（只在覆盖范围内）
        List<Integer> picked = new ArrayList<>();
        if (opt.getDanReds() != null && !opt.getDanReds().isEmpty()) {
            for (Integer d : opt.getDanReds()) {
                if (d != null && d >= from && d <= to && pool.contains(d) && picked.size() < k) {
                    picked.add(d);
                    pool.remove(d);
                }
            }
        }

        while (picked.size() < k && !pool.isEmpty()) {
            long total = 0;
            long[] w = new long[pool.size()];
            for (int i = 0; i < pool.size(); i++) {
                int v = pool.get(i);
                long wi = 1L + cnt[v] * 3L + Math.min(50, miss[v]);
                w[i] = wi;
                total += wi;
            }
            long r = (long) (rnd.nextDouble() * total);
            int idx = 0;
            for (int i = 0; i < pool.size(); i++) {
                r -= w[i];
                if (r < 0) {
                    idx = i;
                    break;
                }
            }
            picked.add(pool.remove(idx));
        }
        return picked;
    }

    private static int weightedSampleOneWithFilter(int[] cnt, int[] miss, int from, int to, Random rnd, PredictOptions opt) {
        // 若有蓝胆，优先使用
        if (opt.getDanBlues() != null && !opt.getDanBlues().isEmpty()) {
            List<Integer> candidates = new ArrayList<>(opt.getDanBlues());
            candidates.removeIf(v -> v == null || v < from || v > to || (opt.getKillBlues() != null && opt.getKillBlues().contains(v)));
            if (!candidates.isEmpty()) {
                return candidates.get((int) (rnd.nextDouble() * candidates.size()));
            }
        }

        // 过滤 kill 蓝球
        List<Integer> pool = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            if (opt.getKillBlues() != null && opt.getKillBlues().contains(i)) continue;
            pool.add(i);
        }
        if (pool.isEmpty()) return from;
        if (pool.size() == (to - from + 1)) {
            return weightedSampleOne(cnt, miss, from, to, rnd);
        }

        long total = 0;
        long[] w = new long[pool.size()];
        for (int i = 0; i < pool.size(); i++) {
            int v = pool.get(i);
            long wi = 1L + cnt[v] * 3L + Math.min(50, miss[v]);
            w[i] = wi;
            total += wi;
        }
        long r = (long) (rnd.nextDouble() * total);
        for (int i = 0; i < pool.size(); i++) {
            r -= w[i];
            if (r < 0) return pool.get(i);
        }
        return pool.get(0);
    }

    private static int[] initMissArray(int max) {
        int[] a = new int[max + 1];
        Arrays.fill(a, 0);
        return a;
    }

    private static int weightedSampleOne(int[] cnt, int[] miss, int from, int to, Random rnd) {
        long total = 0;
        long[] w = new long[to + 1];
        for (int i = from; i <= to; i++) {
            long wi = 1L + cnt[i] * 3L + Math.min(50, miss[i]);
            w[i] = wi;
            total += wi;
        }
        long r = (long) (rnd.nextDouble() * total);
        for (int i = from; i <= to; i++) {
            r -= w[i];
            if (r < 0) {
                return i;
            }
        }
        return from;
    }

    private static List<Integer> weightedSampleWithoutReplacement(int[] cnt, int[] miss, int k, int from, int to, Random rnd) {
        List<Integer> pool = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            pool.add(i);
        }
        List<Integer> picked = new ArrayList<>();
        for (int j = 0; j < k && !pool.isEmpty(); j++) {
            long total = 0;
            long[] w = new long[pool.size()];
            for (int i = 0; i < pool.size(); i++) {
                int v = pool.get(i);
                long wi = 1L + cnt[v] * 3L + Math.min(50, miss[v]);
                w[i] = wi;
                total += wi;
            }
            long r = (long) (rnd.nextDouble() * total);
            int idx = 0;
            for (int i = 0; i < pool.size(); i++) {
                r -= w[i];
                if (r < 0) {
                    idx = i;
                    break;
                }
            }
            picked.add(pool.remove(idx));
        }
        return picked;
    }

    private static class Pick {
        private List<Integer> reds;
        private int blue;

        public Pick(List<Integer> reds, int blue) {
            this.reds = reds;
            this.blue = blue;
        }

        public List<Integer> getReds() {
            return reds;
        }

        public int getBlue() {
            return blue;
        }
    }

    private static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new HashMap<String, Object>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return m;
    }

    private static List<Map<String, Object>> toList(int[] cnt, int from, int to) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            list.add(map("num", i, "count", cnt[i]));
        }
        list.sort((a, b) -> Integer.compare((int) b.get("count"), (int) a.get("count")));
        return list;
    }

    private static List<Integer> topK(int[] cnt, int k, int from, int to) {
        List<Integer> all = new ArrayList<>();
        for (int i = from; i <= to; i++) all.add(i);
        all.sort((a, b) -> Integer.compare(cnt[b], cnt[a]));
        return all.subList(0, Math.min(k, all.size()));
    }
}
