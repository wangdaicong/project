package com.example.bicolorsphere.service;

import com.example.bicolorsphere.domain.SsqDraw;
import com.example.bicolorsphere.repo.SsqDrawRepository;
import com.example.bicolorsphere.repo.SsqPredictionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class SsqPredictionService {

    private final SsqPredictionRepository predictionRepository;
    private final SsqDrawRepository drawRepository;

    public SsqPredictionService(SsqPredictionRepository predictionRepository, SsqDrawRepository drawRepository) {
        this.predictionRepository = predictionRepository;
        this.drawRepository = drawRepository;
    }

    public Map<String, Object> savePrediction(String drawNo, String predictReds, int predictBlue) {
        int inserted = predictionRepository.insertIgnore(drawNo, predictReds, predictBlue);
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("ok", Boolean.TRUE);
        m.put("inserted", inserted);
        return m;
    }

    public Map<String, Object> reconcileUnresolved(int limit) {
        List<SsqPredictionRepository.PredictionRow> rows = predictionRepository.listUnresolved(limit);
        int scanned = rows.size();
        int updated = 0;
        int notFound = 0;

        for (SsqPredictionRepository.PredictionRow r : rows) {
            String drawNo = r.getDrawNo();
            Optional<SsqDraw> opt = drawRepository.findByDrawNo(drawNo);
            if (!opt.isPresent()) {
                notFound++;
                continue;
            }

            SsqDraw d = opt.get();
            String actualReds = joinReds(d);
            int actualBlue = d.getBlue();

            Set<Integer> pr = SsqPredictionRepository.parseRedsToSet(r.getPredictReds());
            Set<Integer> ar = SsqPredictionRepository.parseRedsToSet(actualReds);

            int redHit = 0;
            for (Integer n : pr) {
                if (ar.contains(n)) redHit++;
            }
            int blueHit = (r.getPredictBlue() == actualBlue) ? 1 : 0;

            double hitRateV = (redHit + blueHit) / 7.0;
            double errorRateV = 1.0 - hitRateV;
            BigDecimal hitRate = SsqPredictionRepository.round6(hitRateV);
            BigDecimal errorRate = SsqPredictionRepository.round6(errorRateV);

            int u = predictionRepository.updateResult(
                    r.getId(),
                    actualReds,
                    actualBlue,
                    redHit,
                    blueHit,
                    hitRate,
                    errorRate
            );
            if (u > 0) updated += u;
        }

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("ok", Boolean.TRUE);
        m.put("scanned", scanned);
        m.put("updated", updated);
        m.put("notFound", notFound);
        return m;
    }

    public Map<String, Object> search(String drawNo, int page, int size) {
        return predictionRepository.search(drawNo, page, size).asMap();
    }

    private static String joinReds(SsqDraw d) {
        if (d == null || d.getReds() == null || d.getReds().size() != 6) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int n = d.getReds().get(i);
            if (i > 0) sb.append(' ');
            if (n < 10) sb.append('0');
            sb.append(n);
        }
        return sb.toString();
    }
}
