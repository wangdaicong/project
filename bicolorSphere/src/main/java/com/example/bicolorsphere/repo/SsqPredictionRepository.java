package com.example.bicolorsphere.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.*;

@Repository
public class SsqPredictionRepository {

    private final JdbcTemplate jdbcTemplate;

    public SsqPredictionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        ensureTable();
    }

    private void ensureTable() {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS ssq_prediction_record (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键'," +
                        "draw_no VARCHAR(20) NOT NULL COMMENT '期号'," +
                        "predict_reds VARCHAR(64) NOT NULL COMMENT '预测红球（两位数空格分隔）'," +
                        "predict_blue INT NOT NULL COMMENT '预测蓝球'," +
                        "actual_reds VARCHAR(64) NULL COMMENT '真实红球（两位数空格分隔）'," +
                        "actual_blue INT NULL COMMENT '真实蓝球'," +
                        "red_hit INT NULL COMMENT '红球命中个数(0-6)'," +
                        "blue_hit INT NULL COMMENT '蓝球是否命中(0/1)'," +
                        "hit_rate DECIMAL(10,6) NULL COMMENT '命中率=(红中+蓝中)/7'," +
                        "error_rate DECIMAL(10,6) NULL COMMENT '误差率=1-命中率'," +
                        "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                        "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                        "UNIQUE KEY uk_draw_pick (draw_no, predict_reds, predict_blue)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='双色球预测记录表'"
        );

        // Ensure comments exist even if table was created earlier.
        try {
            jdbcTemplate.execute("ALTER TABLE ssq_prediction_record COMMENT='双色球预测记录表'");
            jdbcTemplate.execute("ALTER TABLE ssq_prediction_record MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键'");
            jdbcTemplate.execute("ALTER TABLE ssq_prediction_record MODIFY COLUMN draw_no VARCHAR(20) NOT NULL COMMENT '期号'");
            jdbcTemplate.execute("ALTER TABLE ssq_prediction_record MODIFY COLUMN predict_reds VARCHAR(64) NOT NULL COMMENT '预测红球（两位数空格分隔）'");
            jdbcTemplate.execute("ALTER TABLE ssq_prediction_record MODIFY COLUMN predict_blue INT NOT NULL COMMENT '预测蓝球'");
            jdbcTemplate.execute("ALTER TABLE ssq_prediction_record MODIFY COLUMN actual_reds VARCHAR(64) NULL COMMENT '真实红球（两位数空格分隔）'");
            jdbcTemplate.execute("ALTER TABLE ssq_prediction_record MODIFY COLUMN actual_blue INT NULL COMMENT '真实蓝球'");
            jdbcTemplate.execute("ALTER TABLE ssq_prediction_record MODIFY COLUMN red_hit INT NULL COMMENT '红球命中个数(0-6)'");
            jdbcTemplate.execute("ALTER TABLE ssq_prediction_record MODIFY COLUMN blue_hit INT NULL COMMENT '蓝球是否命中(0/1)'");
            jdbcTemplate.execute("ALTER TABLE ssq_prediction_record MODIFY COLUMN hit_rate DECIMAL(10,6) NULL COMMENT '命中率=(红中+蓝中)/7'");
            jdbcTemplate.execute("ALTER TABLE ssq_prediction_record MODIFY COLUMN error_rate DECIMAL(10,6) NULL COMMENT '误差率=1-命中率'");
            jdbcTemplate.execute("ALTER TABLE ssq_prediction_record MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'");
            jdbcTemplate.execute("ALTER TABLE ssq_prediction_record MODIFY COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'");
        } catch (Exception ignore) {
        }
    }

    public int insertIgnore(String drawNo, String predictReds, int predictBlue) {
        return jdbcTemplate.update(
                "INSERT IGNORE INTO ssq_prediction_record(draw_no, predict_reds, predict_blue) VALUES (?,?,?)",
                drawNo,
                normalizeReds(predictReds),
                predictBlue
        );
    }

    public List<PredictionRow> listUnresolved(int limit) {
        int lim = Math.max(1, Math.min(5000, limit));
        return jdbcTemplate.query(
                "SELECT id, draw_no, predict_reds, predict_blue, actual_reds, actual_blue, red_hit, blue_hit, hit_rate, error_rate, created_at, updated_at " +
                        "FROM ssq_prediction_record WHERE actual_reds IS NULL OR actual_blue IS NULL ORDER BY draw_no ASC, id ASC LIMIT ?",
                (rs, rowNum) -> {
                    PredictionRow r = new PredictionRow();
                    r.setId(rs.getLong("id"));
                    r.setDrawNo(rs.getString("draw_no"));
                    r.setPredictReds(rs.getString("predict_reds"));
                    r.setPredictBlue(rs.getInt("predict_blue"));
                    r.setActualReds(rs.getString("actual_reds"));
                    Integer ab = (Integer) rs.getObject("actual_blue");
                    r.setActualBlue(ab);
                    Integer rh = (Integer) rs.getObject("red_hit");
                    r.setRedHit(rh);
                    Integer bh = (Integer) rs.getObject("blue_hit");
                    r.setBlueHit(bh);
                    BigDecimal hr = (BigDecimal) rs.getObject("hit_rate");
                    r.setHitRate(hr);
                    BigDecimal er = (BigDecimal) rs.getObject("error_rate");
                    r.setErrorRate(er);
                    Timestamp ca = (Timestamp) rs.getObject("created_at");
                    r.setCreatedAt(ca);
                    Timestamp ua = (Timestamp) rs.getObject("updated_at");
                    r.setUpdatedAt(ua);
                    return r;
                },
                lim
        );
    }

    public SearchResult search(String drawNo, int page, int size) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(200, size));
        int offset = p * s;

        String base = " FROM ssq_prediction_record WHERE 1=1";
        List<Object> args = new ArrayList<Object>();
        if (drawNo != null && !drawNo.trim().isEmpty()) {
            base += " AND draw_no = ?";
            args.add(drawNo.trim());
        }

        long total = 0;
        Long tv = jdbcTemplate.queryForObject("SELECT COUNT(1)" + base, Long.class, args.toArray());
        if (tv != null) total = tv;

        List<Object> pageArgs = new ArrayList<Object>(args);
        pageArgs.add(s);
        pageArgs.add(offset);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, draw_no, predict_reds, predict_blue, actual_reds, actual_blue, red_hit, blue_hit, hit_rate, error_rate, created_at, updated_at" +
                        base + " ORDER BY draw_no DESC, id DESC LIMIT ? OFFSET ?",
                pageArgs.toArray()
        );

        return new SearchResult(total, p, s, rows);
    }

    public int updateResult(long id,
                            String actualReds,
                            int actualBlue,
                            int redHit,
                            int blueHit,
                            BigDecimal hitRate,
                            BigDecimal errorRate) {
        return jdbcTemplate.update(
                "UPDATE ssq_prediction_record SET actual_reds=?, actual_blue=?, red_hit=?, blue_hit=?, hit_rate=?, error_rate=? WHERE id=?",
                normalizeReds(actualReds),
                actualBlue,
                redHit,
                blueHit,
                hitRate,
                errorRate,
                id
        );
    }

    public static String normalizeReds(String reds) {
        String t = reds == null ? "" : reds.trim();
        if (t.isEmpty()) return t;
        List<Integer> nums = tokenizeInts(t);
        Collections.sort(nums);
        List<String> out = new ArrayList<String>();
        for (Integer n : nums) {
            if (n == null) continue;
            String ss = String.valueOf(n);
            out.add(ss.length() == 1 ? "0" + ss : ss);
        }
        return String.join(" ", out);
    }

    private static List<Integer> tokenizeInts(String t) {
        List<Integer> nums = new ArrayList<Integer>();
        String[] tokens = t.split("[^0-9]+");
        for (String s : tokens) {
            if (s == null) continue;
            String ss = s.trim();
            if (ss.isEmpty()) continue;
            try {
                nums.add(Integer.parseInt(ss));
            } catch (Exception ignore) {
            }
        }
        return nums;
    }

    public static Set<Integer> parseRedsToSet(String reds) {
        Set<Integer> set = new HashSet<Integer>();
        String t = reds == null ? "" : reds.trim();
        if (t.isEmpty()) return set;
        List<Integer> nums = tokenizeInts(t);
        set.addAll(nums);
        return set;
    }

    public static BigDecimal round6(double v) {
        return new BigDecimal(v).setScale(6, RoundingMode.HALF_UP);
    }

    public static class SearchResult {
        private long total;
        private int page;
        private int size;
        private List<Map<String, Object>> rows;

        public SearchResult(long total, int page, int size, List<Map<String, Object>> rows) {
            this.total = total;
            this.page = page;
            this.size = size;
            this.rows = rows;
        }

        public long getTotal() {
            return total;
        }

        public int getPage() {
            return page;
        }

        public int getSize() {
            return size;
        }

        public List<Map<String, Object>> getRows() {
            return rows;
        }

        public Map<String, Object> asMap() {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("total", total);
            m.put("page", page);
            m.put("size", size);
            m.put("rows", rows);
            return m;
        }
    }

    public static class PredictionRow {
        private long id;
        private String drawNo;
        private String predictReds;
        private int predictBlue;
        private String actualReds;
        private Integer actualBlue;
        private Integer redHit;
        private Integer blueHit;
        private BigDecimal hitRate;
        private BigDecimal errorRate;
        private Timestamp createdAt;
        private Timestamp updatedAt;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getDrawNo() {
            return drawNo;
        }

        public void setDrawNo(String drawNo) {
            this.drawNo = drawNo;
        }

        public String getPredictReds() {
            return predictReds;
        }

        public void setPredictReds(String predictReds) {
            this.predictReds = predictReds;
        }

        public int getPredictBlue() {
            return predictBlue;
        }

        public void setPredictBlue(int predictBlue) {
            this.predictBlue = predictBlue;
        }

        public String getActualReds() {
            return actualReds;
        }

        public void setActualReds(String actualReds) {
            this.actualReds = actualReds;
        }

        public Integer getActualBlue() {
            return actualBlue;
        }

        public void setActualBlue(Integer actualBlue) {
            this.actualBlue = actualBlue;
        }

        public Integer getRedHit() {
            return redHit;
        }

        public void setRedHit(Integer redHit) {
            this.redHit = redHit;
        }

        public Integer getBlueHit() {
            return blueHit;
        }

        public void setBlueHit(Integer blueHit) {
            this.blueHit = blueHit;
        }

        public BigDecimal getHitRate() {
            return hitRate;
        }

        public void setHitRate(BigDecimal hitRate) {
            this.hitRate = hitRate;
        }

        public BigDecimal getErrorRate() {
            return errorRate;
        }

        public void setErrorRate(BigDecimal errorRate) {
            this.errorRate = errorRate;
        }

        public Timestamp getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Timestamp createdAt) {
            this.createdAt = createdAt;
        }

        public Timestamp getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Timestamp updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
