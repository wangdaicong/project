package com.exan.api;

import com.exan.app.dto.leaderboard.LeaderboardItemVO;
import com.exan.app.service.LeaderboardService;
import com.exan.infra.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboards")
@RequiredArgsConstructor
public class LeaderboardController {
    private final LeaderboardService leaderboardService;

    @GetMapping("/daily")
    public ApiResponse<List<LeaderboardItemVO>> daily(@RequestParam long subjectId, @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(leaderboardService.getDaily(subjectId, limit));
    }
}
