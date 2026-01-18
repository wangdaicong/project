package com.exan.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exan.app.dto.paper.PaperDetailResponse;
import com.exan.app.dto.paper.ListPapersResponse;
import com.exan.app.dto.paper.PaperItemVO;
import com.exan.domain.entity.Paper;
import com.exan.domain.entity.PaperContent;
import com.exan.domain.entity.PaperStat;
import com.exan.domain.mapper.PaperContentMapper;
import com.exan.domain.mapper.PaperMapper;
import com.exan.domain.mapper.PaperStatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaperService {
    private final PaperMapper paperMapper;
    private final PaperContentMapper paperContentMapper;
    private final PaperStatMapper paperStatMapper;

    public ListPapersResponse listPapers(long stageId, long subjectId, Integer grade, String regionCode, int limit) {
        int l = Math.min(Math.max(limit, 1), 200);

        LambdaQueryWrapper<Paper> qw = new LambdaQueryWrapper<Paper>()
            .eq(Paper::getStageId, stageId)
            .eq(Paper::getSubjectId, subjectId)
            .eq(Paper::getStatus, "ONLINE")
            .orderByDesc(Paper::getPaperDate)
            .orderByDesc(Paper::getId)
            .last("limit " + l);

        if (regionCode != null && !regionCode.isBlank()) {
            qw.eq(Paper::getRegionCode, regionCode.trim());
        }

        if (grade != null && grade > 0) {
            String zh = gradeToZh(grade);
            String kw1 = grade + "年级";
            if (zh != null) {
                String kw2 = zh + "年级";
                qw.and(w -> w.like(Paper::getName, kw1).or().like(Paper::getName, kw2));
            } else {
                qw.like(Paper::getName, kw1);
            }
        }

        List<Paper> papers = paperMapper.selectList(qw);

        List<PaperItemVO> items = papers.stream()
            .map(p -> new PaperItemVO(
                p.getId(),
                p.getStageId(),
                p.getSubjectId(),
                p.getName(),
                p.getPaperDate(),
                p.getRegionCode()
            ))
            .toList();

        return new ListPapersResponse(items);
    }

    @Transactional
    public PaperDetailResponse getPaperDetail(long id) {
        Paper p = paperMapper.selectById(id);
        if (p == null) return null;

        PaperContent c = paperContentMapper.selectOne(new LambdaQueryWrapper<PaperContent>()
            .eq(PaperContent::getPaperId, id)
            .last("limit 1"));

        String sourceUrl = c == null ? null : c.getSourceUrl();
        String contentText = c == null ? null : c.getContentText();
        String attachmentsJson = c == null ? null : c.getAttachmentsJson();

        PaperStat s = paperStatMapper.selectOne(new LambdaQueryWrapper<PaperStat>()
            .eq(PaperStat::getPaperId, id)
            .last("limit 1"));
        if (s == null) {
            s = new PaperStat();
            s.setPaperId(id);
            s.setViews(1L);
            s.setDownloads(0L);
            paperStatMapper.insert(s);
        } else {
            Long v = s.getViews() == null ? 0L : s.getViews();
            s.setViews(v + 1L);
            paperStatMapper.updateById(s);
        }

        return new PaperDetailResponse(
            p.getId(),
            p.getStageId(),
            p.getSubjectId(),
            p.getName(),
            p.getPaperDate(),
            p.getRegionCode(),
            sourceUrl,
            s.getViews(),
            s.getDownloads(),
            contentText,
            attachmentsJson
        );
    }

    @Transactional
    public long incDownload(long paperId) {
        Paper p = paperMapper.selectById(paperId);
        if (p == null) {
            throw new IllegalArgumentException("paper not found");
        }

        PaperStat s = paperStatMapper.selectOne(new LambdaQueryWrapper<PaperStat>()
            .eq(PaperStat::getPaperId, paperId)
            .last("limit 1"));
        if (s == null) {
            s = new PaperStat();
            s.setPaperId(paperId);
            s.setViews(0L);
            s.setDownloads(1L);
            paperStatMapper.insert(s);
            return 1L;
        }

        Long d = s.getDownloads() == null ? 0L : s.getDownloads();
        s.setDownloads(d + 1L);
        paperStatMapper.updateById(s);
        return s.getDownloads();
    }

    private static String gradeToZh(int grade) {
        return switch (grade) {
            case 1 -> "一";
            case 2 -> "二";
            case 3 -> "三";
            case 4 -> "四";
            case 5 -> "五";
            case 6 -> "六";
            case 7 -> "七";
            case 8 -> "八";
            case 9 -> "九";
            case 10 -> "十";
            case 11 -> "十一";
            case 12 -> "十二";
            default -> null;
        };
    }
}
