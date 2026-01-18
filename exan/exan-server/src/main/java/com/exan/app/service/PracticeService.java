package com.exan.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exan.app.dto.practice.SessionDetailResponse;
import com.exan.app.dto.practice.SessionQuestionVO;
import com.exan.app.dto.practice.SubmitAnswerRequest;
import com.exan.domain.entity.ExamAnswer;
import com.exan.domain.entity.ExamSession;
import com.exan.domain.entity.ExamSessionQuestion;
import com.exan.domain.entity.Question;
import com.exan.domain.entity.QuestionOption;
import com.exan.domain.entity.WrongQuestion;
import com.exan.domain.mapper.ExamAnswerMapper;
import com.exan.domain.mapper.ExamSessionMapper;
import com.exan.domain.mapper.ExamSessionQuestionMapper;
import com.exan.domain.mapper.QuestionMapper;
import com.exan.domain.mapper.QuestionOptionMapper;
import com.exan.domain.mapper.WrongQuestionMapper;
import com.exan.infra.exception.BizException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PracticeService {
    private final ExamSessionMapper examSessionMapper;
    private final ExamSessionQuestionMapper examSessionQuestionMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final ExamAnswerMapper examAnswerMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public long createPracticeSession(long userId, long stageId, long subjectId, int count) {
        int c = Math.min(Math.max(count, 1), 50);

        List<Question> questions = questionMapper.selectList(new LambdaQueryWrapper<Question>()
            .eq(Question::getStageId, stageId)
            .eq(Question::getSubjectId, subjectId)
            .eq(Question::getStatus, "ONLINE")
            .orderByAsc(Question::getId)
            .last("order by rand() limit " + c));

        if (questions.isEmpty()) {
            throw new BizException(400, "题库为空，请先在后台导入并审核上架题目");
        }

        ExamSession session = new ExamSession();
        session.setUserId(userId);
        session.setMode("PRACTICE");
        session.setStageId(stageId);
        session.setSubjectId(subjectId);
        session.setStatus("DOING");
        session.setStartedAt(LocalDateTime.now());
        session.setScoreTotal(questions.size());
        session.setScoreGot(0);
        examSessionMapper.insert(session);

        int sort = 1;
        for (Question q : questions) {
            ExamSessionQuestion sq = new ExamSessionQuestion();
            sq.setSessionId(session.getId());
            sq.setQuestionId(q.getId());
            sq.setScore(1);
            sq.setSort(sort++);
            examSessionQuestionMapper.insert(sq);
        }

        return session.getId();
    }

    public SessionDetailResponse getSessionDetail(long userId, long sessionId) {
        ExamSession session = examSessionMapper.selectById(sessionId);
        if (session == null || session.getUserId() == null || session.getUserId() != userId) {
            throw new BizException(404, "会话不存在");
        }

        List<ExamSessionQuestion> sqs = examSessionQuestionMapper.selectList(new LambdaQueryWrapper<ExamSessionQuestion>()
            .eq(ExamSessionQuestion::getSessionId, sessionId)
            .orderByAsc(ExamSessionQuestion::getSort));

        if (sqs.isEmpty()) {
            return new SessionDetailResponse(
                sessionId,
                session.getStageId(),
                session.getSubjectId(),
                session.getStatus(),
                session.getScoreGot(),
                session.getScoreTotal(),
                List.of()
            );
        }

        List<Long> qIds = sqs.stream().map(ExamSessionQuestion::getQuestionId).toList();
        List<Question> qs = questionMapper.selectBatchIds(qIds);
        Map<Long, Question> qMap = new HashMap<>();
        for (Question q : qs) {
            qMap.put(q.getId(), q);
        }

        List<QuestionOption> opts = questionOptionMapper.selectList(new LambdaQueryWrapper<QuestionOption>()
            .in(QuestionOption::getQuestionId, qIds));

        Map<Long, List<QuestionOption>> optMap = opts.stream().collect(Collectors.groupingBy(QuestionOption::getQuestionId));

        List<SessionQuestionVO> vos = new ArrayList<>();
        for (ExamSessionQuestion sq : sqs) {
            Question q = qMap.get(sq.getQuestionId());
            if (q == null) {
                continue;
            }
            List<QuestionOption> qOpts = optMap.getOrDefault(q.getId(), List.of());
            List<SessionQuestionVO.QuestionOptionVO> o = qOpts.stream()
                .sorted((a, b) -> a.getOptKey().compareToIgnoreCase(b.getOptKey()))
                .map(it -> new SessionQuestionVO.QuestionOptionVO(it.getOptKey(), it.getContent()))
                .toList();

            vos.add(new SessionQuestionVO(q.getId(), q.getType(), q.getStem(), q.getDifficulty(), o));
        }

        return new SessionDetailResponse(
            sessionId,
            session.getStageId(),
            session.getSubjectId(),
            session.getStatus(),
            session.getScoreGot(),
            session.getScoreTotal(),
            vos
        );
    }

    @Transactional
    public void submitAnswer(long userId, long sessionId, SubmitAnswerRequest req) {
        ExamSession session = examSessionMapper.selectById(sessionId);
        if (session == null || session.getUserId() == null || session.getUserId() != userId) {
            throw new BizException(404, "会话不存在");
        }
        if (!"DOING".equals(session.getStatus())) {
            throw new BizException(400, "当前会话不可答题");
        }

        ExamSessionQuestion sq = examSessionQuestionMapper.selectOne(new LambdaQueryWrapper<ExamSessionQuestion>()
            .eq(ExamSessionQuestion::getSessionId, sessionId)
            .eq(ExamSessionQuestion::getQuestionId, req.questionId()));
        if (sq == null) {
            throw new BizException(400, "题目不属于该会话");
        }

        Question q = questionMapper.selectById(req.questionId());
        if (q == null) {
            throw new BizException(404, "题目不存在");
        }

        JsonNode userAnswer;
        try {
            userAnswer = objectMapper.valueToTree(req.answer());
        } catch (Exception e) {
            throw new BizException(400, "answer格式错误");
        }

        boolean correct = false;
        if (q.getAnswer() != null && !q.getAnswer().isBlank()) {
            try {
                JsonNode std = objectMapper.readTree(q.getAnswer());
                correct = std.equals(userAnswer);
            } catch (Exception e) {
                correct = false;
            }
        }

        ExamAnswer ans = examAnswerMapper.selectOne(new LambdaQueryWrapper<ExamAnswer>()
            .eq(ExamAnswer::getSessionId, sessionId)
            .eq(ExamAnswer::getQuestionId, req.questionId()));
        if (ans == null) {
            ans = new ExamAnswer();
            ans.setSessionId(sessionId);
            ans.setQuestionId(req.questionId());
        }

        try {
            ans.setAnswerJson(objectMapper.writeValueAsString(req.answer()));
        } catch (Exception e) {
            throw new BizException(400, "answer序列化失败");
        }

        ans.setIsCorrect(correct ? 1 : 0);
        ans.setScoreGot(correct ? sq.getScore() : 0);
        ans.setAnsweredAt(LocalDateTime.now());

        if (ans.getId() == null) {
            examAnswerMapper.insert(ans);
        } else {
            examAnswerMapper.updateById(ans);
        }

        if (!correct) {
            WrongQuestion w = wrongQuestionMapper.selectOne(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId)
                .eq(WrongQuestion::getQuestionId, req.questionId()));
            if (w == null) {
                w = new WrongQuestion();
                w.setUserId(userId);
                w.setQuestionId(req.questionId());
                w.setWrongCount(1);
                w.setLastWrongAt(LocalDateTime.now());
                w.setLastSessionId(sessionId);
                wrongQuestionMapper.insert(w);
            } else {
                w.setWrongCount(w.getWrongCount() == null ? 1 : w.getWrongCount() + 1);
                w.setLastWrongAt(LocalDateTime.now());
                w.setLastSessionId(sessionId);
                wrongQuestionMapper.updateById(w);
            }
        }
    }

    @Transactional
    public SessionDetailResponse submitSession(long userId, long sessionId) {
        ExamSession session = examSessionMapper.selectById(sessionId);
        if (session == null || session.getUserId() == null || session.getUserId() != userId) {
            throw new BizException(404, "会话不存在");
        }
        if (!"DOING".equals(session.getStatus())) {
            return getSessionDetail(userId, sessionId);
        }

        List<ExamSessionQuestion> sqs = examSessionQuestionMapper.selectList(new LambdaQueryWrapper<ExamSessionQuestion>()
            .eq(ExamSessionQuestion::getSessionId, sessionId));

        int total = sqs.stream().mapToInt(it -> it.getScore() == null ? 0 : it.getScore()).sum();
        List<ExamAnswer> answers = examAnswerMapper.selectList(new LambdaQueryWrapper<ExamAnswer>()
            .eq(ExamAnswer::getSessionId, sessionId));
        int got = answers.stream().mapToInt(it -> it.getScoreGot() == null ? 0 : it.getScoreGot()).sum();

        session.setScoreTotal(total);
        session.setScoreGot(got);
        session.setStatus("FINISHED");
        session.setSubmittedAt(LocalDateTime.now());
        examSessionMapper.updateById(session);

        return getSessionDetail(userId, sessionId);
    }
}
