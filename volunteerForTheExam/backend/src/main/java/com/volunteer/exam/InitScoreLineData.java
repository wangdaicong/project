package com.volunteer.exam;

import com.volunteer.exam.entity.ScoreLine;
import com.volunteer.exam.mapper.ScoreLineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InitScoreLineData implements CommandLineRunner {
    
    @Autowired
    private ScoreLineMapper scoreLineMapper;
    
    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有数据
        Long count = scoreLineMapper.selectCount(null);
        if (count > 0) {
            System.out.println("分数线数据已存在，跳过初始化");
            return;
        }
        
        System.out.println("开始初始化分数线数据...");
        
        List<ScoreLine> scoreLines = new ArrayList<>();
        
        // 清华大学数据
        scoreLines.add(createScoreLine(1L, "清华大学", "北京", 2023, "本科一批", "理科", 685, 692, 705, 50, 30));
        scoreLines.add(createScoreLine(1L, "清华大学", "北京", 2023, "本科一批", "文科", 665, 672, 685, 20, 15));
        scoreLines.add(createScoreLine(1L, "清华大学", "北京", 2022, "本科一批", "理科", 680, 688, 700, 55, 28));
        scoreLines.add(createScoreLine(1L, "清华大学", "北京", 2022, "本科一批", "文科", 660, 668, 680, 22, 14));
        scoreLines.add(createScoreLine(1L, "清华大学", "北京", 2021, "本科一批", "理科", 678, 685, 698, 60, 32));
        scoreLines.add(createScoreLine(1L, "清华大学", "北京", 2021, "本科一批", "文科", 658, 665, 678, 25, 16));
        scoreLines.add(createScoreLine(1L, "清华大学", "北京", 2020, "本科一批", "理科", 675, 682, 695, 65, 30));
        scoreLines.add(createScoreLine(1L, "清华大学", "北京", 2020, "本科一批", "文科", 655, 662, 675, 28, 15));
        scoreLines.add(createScoreLine(1L, "清华大学", "北京", 2019, "本科一批", "理科", 672, 680, 693, 70, 29));
        scoreLines.add(createScoreLine(1L, "清华大学", "北京", 2019, "本科一批", "文科", 652, 660, 672, 30, 14));
        
        // 北京大学数据
        scoreLines.add(createScoreLine(2L, "北京大学", "北京", 2023, "本科一批", "理科", 683, 690, 703, 52, 35));
        scoreLines.add(createScoreLine(2L, "北京大学", "北京", 2023, "本科一批", "文科", 668, 675, 688, 18, 20));
        scoreLines.add(createScoreLine(2L, "北京大学", "北京", 2022, "本科一批", "理科", 678, 686, 698, 58, 33));
        scoreLines.add(createScoreLine(2L, "北京大学", "北京", 2022, "本科一批", "文科", 663, 670, 683, 20, 18));
        scoreLines.add(createScoreLine(2L, "北京大学", "北京", 2021, "本科一批", "理科", 676, 683, 696, 62, 36));
        scoreLines.add(createScoreLine(2L, "北京大学", "北京", 2021, "本科一批", "文科", 660, 667, 680, 23, 19));
        scoreLines.add(createScoreLine(2L, "北京大学", "北京", 2020, "本科一批", "理科", 673, 680, 693, 68, 34));
        scoreLines.add(createScoreLine(2L, "北京大学", "北京", 2020, "本科一批", "文科", 657, 664, 677, 26, 17));
        scoreLines.add(createScoreLine(2L, "北京大学", "北京", 2019, "本科一批", "理科", 670, 678, 690, 72, 32));
        scoreLines.add(createScoreLine(2L, "北京大学", "北京", 2019, "本科一批", "文科", 654, 662, 674, 29, 16));
        
        // 浙江大学数据
        scoreLines.add(createScoreLine(3L, "浙江大学", "北京", 2023, "本科一批", "理科", 675, 682, 695, 80, 25));
        scoreLines.add(createScoreLine(3L, "浙江大学", "北京", 2023, "本科一批", "文科", 655, 662, 675, 35, 12));
        scoreLines.add(createScoreLine(3L, "浙江大学", "北京", 2022, "本科一批", "理科", 670, 678, 690, 85, 24));
        scoreLines.add(createScoreLine(3L, "浙江大学", "北京", 2022, "本科一批", "文科", 650, 658, 670, 38, 11));
        scoreLines.add(createScoreLine(3L, "浙江大学", "北京", 2021, "本科一批", "理科", 668, 675, 688, 90, 26));
        scoreLines.add(createScoreLine(3L, "浙江大学", "北京", 2021, "本科一批", "文科", 648, 655, 668, 40, 13));
        scoreLines.add(createScoreLine(3L, "浙江大学", "北京", 2020, "本科一批", "理科", 665, 672, 685, 95, 25));
        scoreLines.add(createScoreLine(3L, "浙江大学", "北京", 2020, "本科一批", "文科", 645, 652, 665, 42, 12));
        scoreLines.add(createScoreLine(3L, "浙江大学", "北京", 2019, "本科一批", "理科", 662, 670, 682, 100, 24));
        scoreLines.add(createScoreLine(3L, "浙江大学", "北京", 2019, "本科一批", "文科", 642, 650, 662, 45, 11));
        
        // 批量插入
        for (ScoreLine scoreLine : scoreLines) {
            scoreLineMapper.insert(scoreLine);
        }
        
        System.out.println("分数线数据初始化完成，共插入 " + scoreLines.size() + " 条记录");
    }
    
    private ScoreLine createScoreLine(Long universityId, String universityName, String province, 
                                     Integer year, String batch, String category,
                                     Integer minScore, Integer avgScore, Integer maxScore,
                                     Integer minRank, Integer enrollmentCount) {
        ScoreLine scoreLine = new ScoreLine();
        scoreLine.setUniversityId(universityId);
        scoreLine.setUniversityName(universityName);
        scoreLine.setProvince(province);
        scoreLine.setYear(year);
        scoreLine.setBatch(batch);
        scoreLine.setCategory(category);
        scoreLine.setMinScore(minScore);
        scoreLine.setAvgScore(avgScore);
        scoreLine.setMaxScore(maxScore);
        scoreLine.setMinRank(minRank);
        scoreLine.setEnrollmentCount(enrollmentCount);
        return scoreLine;
    }
}
