package com.exan.app.service;

import com.exan.app.dto.leaderboard.LeaderboardItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LeaderboardService {
    private final StringRedisTemplate redis;

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.BASIC_ISO_DATE;

    public void addDailyScore(long subjectId, long userId, int score) {
        String key = dailyKey(subjectId);
        redis.opsForZSet().incrementScore(key, String.valueOf(userId), score);
        redis.expire(key, java.time.Duration.ofDays(10));
    }

    public List<LeaderboardItemVO> getDaily(long subjectId, int limit) {
        int l = Math.min(Math.max(limit, 1), 100);
        String key = dailyKey(subjectId);
        Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> tuples =
            redis.opsForZSet().reverseRangeWithScores(key, 0, l - 1);
        List<LeaderboardItemVO> res = new ArrayList<>();
        if (tuples == null) {
            return res;
        }
        int rank = 1;
        for (var t : tuples) {
            if (t.getValue() == null) {
                continue;
            }
            long uid = Long.parseLong(t.getValue());
            double score = t.getScore() == null ? 0d : t.getScore();
            res.add(new LeaderboardItemVO(uid, score, rank++));
        }
        return res;
    }

    private String dailyKey(long subjectId) {
        String day = LocalDate.now().format(DAY_FMT);
        return "lb:daily:" + subjectId + ":" + day;
    }
}
