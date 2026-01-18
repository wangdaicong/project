package com.exan.app.dto.leaderboard;

public record LeaderboardItemVO(
    long userId,
    double score,
    int rank
) {
}
