package com.exan.api;

import com.exan.app.dto.practice.CreatePracticeSessionRequest;
import com.exan.app.dto.practice.SessionDetailResponse;
import com.exan.app.dto.practice.SubmitAnswerRequest;
import com.exan.app.service.LeaderboardService;
import com.exan.app.service.PracticeService;
import com.exan.infra.web.ApiResponse;
import com.exan.infra.web.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
public class PracticeController {
    private final PracticeService practiceService;
    private final LeaderboardService leaderboardService;

    @PostMapping("/sessions")
    public ApiResponse<Long> create(@RequestBody @Valid CreatePracticeSessionRequest req, HttpServletRequest request) {
        long userId = UserContext.currentUserId(request);
        long id = practiceService.createPracticeSession(userId, req.stageId(), req.subjectId(), req.count() == null ? 10 : req.count());
        return ApiResponse.ok(id);
    }

    @GetMapping("/sessions/{id}")
    public ApiResponse<SessionDetailResponse> detail(@PathVariable("id") long id, HttpServletRequest request) {
        long userId = UserContext.currentUserId(request);
        return ApiResponse.ok(practiceService.getSessionDetail(userId, id));
    }

    @PostMapping("/sessions/{id}/answers")
    public ApiResponse<Void> answer(@PathVariable("id") long id, @RequestBody @Valid SubmitAnswerRequest req, HttpServletRequest request) {
        long userId = UserContext.currentUserId(request);
        practiceService.submitAnswer(userId, id, req);
        return ApiResponse.ok(null);
    }

    @PostMapping("/sessions/{id}/submit")
    public ApiResponse<SessionDetailResponse> submit(@PathVariable("id") long id, HttpServletRequest request) {
        long userId = UserContext.currentUserId(request);
        SessionDetailResponse res = practiceService.submitSession(userId, id);
        if (res.scoreGot() != null && res.scoreGot() > 0) {
            // 日榜以答题得分累计
            if (res.subjectId() != null) {
                leaderboardService.addDailyScore(res.subjectId(), userId, res.scoreGot());
            }
        }
        return ApiResponse.ok(res);
    }
}
