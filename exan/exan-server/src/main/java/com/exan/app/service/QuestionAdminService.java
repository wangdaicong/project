package com.exan.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exan.app.dto.admin.ImportQuestionItem;
import com.exan.app.dto.admin.ImportQuestionOptionItem;
import com.exan.infra.exception.BizException;
import com.exan.infra.util.DigestUtil;
import com.exan.infra.util.TextNormalizeUtil;
import com.exan.domain.entity.Question;
import com.exan.domain.entity.QuestionOption;
import com.exan.domain.mapper.QuestionMapper;
import com.exan.domain.mapper.QuestionOptionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionAdminService {
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void importQuestions(List<ImportQuestionItem> items) {
        for (ImportQuestionItem item : items) {
            String norm = TextNormalizeUtil.normalizeForHash(item.stem());
            String hash = DigestUtil.md5Hex(norm);

            Question q = new Question();
            q.setStageId(item.stageId());
            q.setSubjectId(item.subjectId());
            q.setType(item.type().toUpperCase());
            q.setStem(item.stem());
            q.setDifficulty(item.difficulty() == null ? 3 : item.difficulty());
            q.setAnalysis(item.analysis());
            try {
                q.setAnswer(item.answer() == null ? null : objectMapper.writeValueAsString(item.answer()));
            } catch (Exception e) {
                throw new BizException(400, "answer字段不是合法JSON");
            }
            q.setStatus("PENDING");
            q.setQuestionHash(hash);

            try {
                questionMapper.insert(q);
            } catch (Exception e) {
                // 可能是 unique(subject_id, question_hash) 冲突
                continue;
            }

            if (item.options() != null && !item.options().isEmpty()) {
                List<QuestionOption> opts = new ArrayList<>();
                for (ImportQuestionOptionItem opt : item.options()) {
                    QuestionOption o = new QuestionOption();
                    o.setQuestionId(q.getId());
                    o.setOptKey(opt.key());
                    o.setContent(opt.content());
                    opts.add(o);
                }
                for (QuestionOption o : opts) {
                    questionOptionMapper.insert(o);
                }
            }
        }
    }

    public List<Question> listPending(long subjectId, int limit) {
        return questionMapper.selectList(new LambdaQueryWrapper<Question>()
            .eq(Question::getSubjectId, subjectId)
            .eq(Question::getStatus, "PENDING")
            .orderByDesc(Question::getId)
            .last("limit " + Math.min(Math.max(limit, 1), 200)));
    }

    public void approve(long questionId) {
        Question q = questionMapper.selectById(questionId);
        if (q == null) {
            throw new BizException(404, "题目不存在");
        }
        q.setStatus("ONLINE");
        questionMapper.updateById(q);
    }

    public void reject(long questionId) {
        Question q = questionMapper.selectById(questionId);
        if (q == null) {
            throw new BizException(404, "题目不存在");
        }
        q.setStatus("OFFLINE");
        questionMapper.updateById(q);
    }
}
