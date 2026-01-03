package com.example.bicolorsphere.repo;

import com.example.bicolorsphere.domain.SsqDraw;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class SsqDrawRepository {
    private final JdbcTemplate jdbcTemplate;

    public SsqDrawRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        ensureComments();
    }

    private void ensureComments() {
        try {
            jdbcTemplate.execute("ALTER TABLE ssq_draw COMMENT='双色球开奖记录表'");
            jdbcTemplate.execute("ALTER TABLE ssq_draw MODIFY COLUMN draw_no VARCHAR(20) NOT NULL COMMENT '期号'");
            jdbcTemplate.execute("ALTER TABLE ssq_draw MODIFY COLUMN draw_date DATE NULL COMMENT '开奖日期'");
            jdbcTemplate.execute("ALTER TABLE ssq_draw MODIFY COLUMN red1 INT NOT NULL COMMENT '红球1'");
            jdbcTemplate.execute("ALTER TABLE ssq_draw MODIFY COLUMN red2 INT NOT NULL COMMENT '红球2'");
            jdbcTemplate.execute("ALTER TABLE ssq_draw MODIFY COLUMN red3 INT NOT NULL COMMENT '红球3'");
            jdbcTemplate.execute("ALTER TABLE ssq_draw MODIFY COLUMN red4 INT NOT NULL COMMENT '红球4'");
            jdbcTemplate.execute("ALTER TABLE ssq_draw MODIFY COLUMN red5 INT NOT NULL COMMENT '红球5'");
            jdbcTemplate.execute("ALTER TABLE ssq_draw MODIFY COLUMN red6 INT NOT NULL COMMENT '红球6'");
            jdbcTemplate.execute("ALTER TABLE ssq_draw MODIFY COLUMN blue INT NOT NULL COMMENT '蓝球'");
        } catch (Exception ignore) {
        }
    }

    private static final RowMapper<SsqDraw> MAPPER = (rs, rowNum) -> {
        String drawNo = rs.getString("draw_no");
        Date date = rs.getDate("draw_date");
        LocalDate drawDate = date == null ? null : date.toLocalDate();
        List<Integer> reds = new ArrayList<>(6);
        reds.add(rs.getInt("red1"));
        reds.add(rs.getInt("red2"));
        reds.add(rs.getInt("red3"));
        reds.add(rs.getInt("red4"));
        reds.add(rs.getInt("red5"));
        reds.add(rs.getInt("red6"));
        int blue = rs.getInt("blue");
        return new SsqDraw(drawNo, drawDate, reds, blue);
    };

    public int upsertIgnore(SsqDraw draw) {
        return jdbcTemplate.update(
                "INSERT IGNORE INTO ssq_draw(draw_no, draw_date, red1, red2, red3, red4, red5, red6, blue) VALUES (?,?,?,?,?,?,?,?,?)",
                draw.getDrawNo(),
                draw.getDrawDate() == null ? null : Date.valueOf(draw.getDrawDate()),
                draw.getReds().get(0),
                draw.getReds().get(1),
                draw.getReds().get(2),
                draw.getReds().get(3),
                draw.getReds().get(4),
                draw.getReds().get(5),
                draw.getBlue()
        );
    }

    public List<SsqDraw> page(int page, int size) {
        int offset = Math.max(0, page) * Math.max(1, size);
        return jdbcTemplate.query(
                "SELECT draw_no, draw_date, red1, red2, red3, red4, red5, red6, blue FROM ssq_draw ORDER BY draw_no DESC LIMIT ? OFFSET ?",
                MAPPER,
                size,
                offset
        );
    }

    public SearchResult search(SearchFilter filter) {
        int page = Math.max(0, filter.getPage());
        int size = Math.max(1, filter.getSize());
        int offset = page * size;

        String base = " FROM ssq_draw WHERE 1=1";
        List<Object> args = new ArrayList<>();

        if (filter.getDrawNoFrom() != null && !filter.getDrawNoFrom().trim().isEmpty()) {
            base += " AND draw_no >= ?";
            args.add(filter.getDrawNoFrom());
        }
        if (filter.getDrawNoTo() != null && !filter.getDrawNoTo().trim().isEmpty()) {
            base += " AND draw_no <= ?";
            args.add(filter.getDrawNoTo());
        }
        if (filter.getDateFrom() != null) {
            base += " AND draw_date >= ?";
            args.add(Date.valueOf(filter.getDateFrom()));
        }
        if (filter.getDateTo() != null) {
            base += " AND draw_date <= ?";
            args.add(Date.valueOf(filter.getDateTo()));
        }

        if (filter.getIncludeRed() != null) {
            base += " AND (red1=? OR red2=? OR red3=? OR red4=? OR red5=? OR red6=?)";
            for (int i = 0; i < 6; i++) {
                args.add(filter.getIncludeRed());
            }
        }
        if (filter.getIncludeBlue() != null) {
            base += " AND blue=?";
            args.add(filter.getIncludeBlue());
        }

        long total = 0;
        Long tv = jdbcTemplate.queryForObject("SELECT COUNT(1)" + base, Long.class, args.toArray());
        if (tv != null) {
            total = tv;
        }

        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(size);
        pageArgs.add(offset);

        List<SsqDraw> rows = jdbcTemplate.query(
                "SELECT draw_no, draw_date, red1, red2, red3, red4, red5, red6, blue" + base + " ORDER BY draw_no DESC LIMIT ? OFFSET ?",
                MAPPER,
                pageArgs.toArray()
        );

        return new SearchResult(total, page, size, rows);
    }

    public List<SsqDraw> listForExport(SearchFilter filter, int maxRows) {
        SearchFilter f = new SearchFilter(
                filter.getDrawNoFrom(),
                filter.getDrawNoTo(),
                filter.getDateFrom(),
                filter.getDateTo(),
                filter.getIncludeRed(),
                filter.getIncludeBlue(),
                0,
                Math.max(1, Math.min(maxRows, 10000))
        );
        return search(f).getRows();
    }

    public List<SsqDraw> latest(int limit) {
        return jdbcTemplate.query(
                "SELECT draw_no, draw_date, red1, red2, red3, red4, red5, red6, blue FROM ssq_draw ORDER BY draw_no DESC LIMIT ?",
                MAPPER,
                limit
        );
    }

    public Optional<SsqDraw> findByDrawNo(String drawNo) {
        List<SsqDraw> list = jdbcTemplate.query(
                "SELECT draw_no, draw_date, red1, red2, red3, red4, red5, red6, blue FROM ssq_draw WHERE draw_no = ?",
                MAPPER,
                drawNo
        );
        return list.stream().findFirst();
    }

    public long count() {
        Long v = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM ssq_draw", Long.class);
        return v == null ? 0 : v;
    }

    public static class SearchFilter {
        private String drawNoFrom;
        private String drawNoTo;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private Integer includeRed;
        private Integer includeBlue;
        private int page;
        private int size;

        public SearchFilter(String drawNoFrom, String drawNoTo, LocalDate dateFrom, LocalDate dateTo, Integer includeRed, Integer includeBlue, int page, int size) {
            this.drawNoFrom = drawNoFrom;
            this.drawNoTo = drawNoTo;
            this.dateFrom = dateFrom;
            this.dateTo = dateTo;
            this.includeRed = includeRed;
            this.includeBlue = includeBlue;
            this.page = page;
            this.size = size;
        }

        public String getDrawNoFrom() {
            return drawNoFrom;
        }

        public String getDrawNoTo() {
            return drawNoTo;
        }

        public LocalDate getDateFrom() {
            return dateFrom;
        }

        public LocalDate getDateTo() {
            return dateTo;
        }

        public Integer getIncludeRed() {
            return includeRed;
        }

        public Integer getIncludeBlue() {
            return includeBlue;
        }

        public int getPage() {
            return page;
        }

        public int getSize() {
            return size;
        }
    }

    public static class SearchResult {
        private long total;
        private int page;
        private int size;
        private List<SsqDraw> rows;

        public SearchResult(long total, int page, int size, List<SsqDraw> rows) {
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

        public List<SsqDraw> getRows() {
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
}
