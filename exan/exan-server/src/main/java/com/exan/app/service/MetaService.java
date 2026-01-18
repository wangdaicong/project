package com.exan.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exan.domain.entity.EduStage;
import com.exan.domain.entity.Subject;
import com.exan.domain.mapper.EduStageMapper;
import com.exan.domain.mapper.SubjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetaService {
    private final EduStageMapper eduStageMapper;
    private final SubjectMapper subjectMapper;

    public List<EduStage> listStages() {
        return eduStageMapper.selectList(new LambdaQueryWrapper<EduStage>()
            .eq(EduStage::getStatus, 1)
            .orderByAsc(EduStage::getSort));
    }

    public List<Subject> listSubjects(long stageId) {
        return subjectMapper.selectList(new LambdaQueryWrapper<Subject>()
            .eq(Subject::getStageId, stageId)
            .eq(Subject::getStatus, 1)
            .orderByAsc(Subject::getSort));
    }
}
