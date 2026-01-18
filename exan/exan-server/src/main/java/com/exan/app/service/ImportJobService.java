package com.exan.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exan.app.dto.admin.CreateImportJobResponse;
import com.exan.app.dto.admin.ImportQuestionItem;
import com.exan.app.dto.admin.ImportQuestionOptionItem;
import com.exan.domain.entity.ImportJob;
import com.exan.domain.entity.ImportJobItem;
import com.exan.domain.entity.Question;
import com.exan.domain.entity.QuestionOption;
import com.exan.domain.mapper.ImportJobItemMapper;
import com.exan.domain.mapper.ImportJobMapper;
import com.exan.domain.mapper.QuestionMapper;
import com.exan.domain.mapper.QuestionOptionMapper;
import com.exan.infra.exception.BizException;
import com.exan.infra.util.DigestUtil;
import com.exan.infra.util.TextNormalizeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportJobService {
    private final ImportJobMapper importJobMapper;
    private final ImportJobItemMapper importJobItemMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final ObjectMapper objectMapper;
    private final com.exan.infra.config.ExanStorageProperties storageProperties;

    @Transactional
    public CreateImportJobResponse createQuestionJsonJob(List<ImportQuestionItem> items) {
        return createQuestionJsonJobInternal(items, null, null);
    }

    @Transactional
    public CreateImportJobResponse createQuestionJsonJobFromFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "questions.json";
        }
        if (!originalFilename.toLowerCase().endsWith(".json")) {
            throw new BizException(400, "仅支持json文件");
        }
        String text;
        try {
            text = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BizException(400, "读取文件失败");
        }

        List<ImportQuestionItem> items;
        try {
            CollectionType type = objectMapper.getTypeFactory().constructCollectionType(List.class, ImportQuestionItem.class);
            items = objectMapper.readValue(text, type);
        } catch (Exception e) {
            throw new BizException(400, "JSON解析失败：需要为题目数组");
        }

        Path storedPath = storeFile(originalFilename, text);
        return createQuestionJsonJobInternal(items, originalFilename, storedPath.toString());
    }

    private Path storeFile(String originalFilename, String content) {
        try {
            Path dir = Paths.get(storageProperties.getLocalUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String safeName = String.valueOf(System.currentTimeMillis()) + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path p = dir.resolve(safeName);
            Files.writeString(p, content, StandardCharsets.UTF_8);
            return p;
        } catch (Exception e) {
            throw new BizException(500, "保存文件失败");
        }
    }

    private CreateImportJobResponse createQuestionJsonJobInternal(List<ImportQuestionItem> items, String originalFilename, String storedFilePath) {
        if (items == null || items.isEmpty()) {
            throw new BizException(400, "items不能为空");
        }

        ImportJob job = new ImportJob();
        job.setJobType("QUESTION_JSON");
        job.setStatus("FINISHED");
        job.setTotalCount(items.size());
        job.setInsertedCount(0);
        job.setDuplicateCount(0);
        job.setFailedCount(0);
        job.setOriginalFilename(originalFilename);
        job.setStoredFilePath(storedFilePath);

        Long stageId = items.stream().map(ImportQuestionItem::stageId).distinct().count() == 1 ? items.get(0).stageId() : null;
        Long subjectId = items.stream().map(ImportQuestionItem::subjectId).distinct().count() == 1 ? items.get(0).subjectId() : null;
        job.setStageId(stageId);
        job.setSubjectId(subjectId);
        importJobMapper.insert(job);

        int inserted = 0;
        int duplicate = 0;
        int failed = 0;

        for (ImportQuestionItem item : items) {
            String norm = TextNormalizeUtil.normalizeForHash(item.stem());
            String hash = DigestUtil.md5Hex(norm);

            ImportJobItem ji = new ImportJobItem();
            ji.setJobId(job.getId());
            ji.setSubjectId(item.subjectId());
            ji.setQuestionHash(hash);

            try {
                Question q = new Question();
                q.setStageId(item.stageId());
                q.setSubjectId(item.subjectId());
                q.setType(item.type().toUpperCase());
                q.setStem(item.stem());
                q.setDifficulty(item.difficulty() == null ? 3 : item.difficulty());
                q.setAnalysis(item.analysis());
                q.setAnswer(item.answer() == null ? null : objectMapper.writeValueAsString(item.answer()));
                q.setStatus("PENDING");
                q.setQuestionHash(hash);

                questionMapper.insert(q);

                ji.setQuestionId(q.getId());
                ji.setResult("INSERTED");
                importJobItemMapper.insert(ji);

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

                inserted++;
            } catch (Exception e) {
                String msg = e.getMessage() == null ? "" : e.getMessage();
                if (msg.contains("uk_question_subject_hash") || msg.toLowerCase().contains("duplicate")) {
                    ji.setResult("DUPLICATE");
                    importJobItemMapper.insert(ji);
                    duplicate++;
                } else {
                    ji.setResult("FAILED");
                    ji.setMessage("insert failed");
                    importJobItemMapper.insert(ji);
                    failed++;
                }
            }
        }

        job.setInsertedCount(inserted);
        job.setDuplicateCount(duplicate);
        job.setFailedCount(failed);
        importJobMapper.updateById(job);

        return new CreateImportJobResponse(job.getId(), job.getTotalCount(), inserted, duplicate, failed);
    }

    public List<ImportJob> listJobs(int limit) {
        int l = Math.min(Math.max(limit, 1), 50);
        return importJobMapper.selectList(new LambdaQueryWrapper<ImportJob>()
            .orderByDesc(ImportJob::getId)
            .last("limit " + l));
    }

    public ImportJob getJob(long jobId) {
        ImportJob job = importJobMapper.selectById(jobId);
        if (job == null) {
            throw new BizException(404, "任务不存在");
        }
        return job;
    }

    public List<Question> listPendingQuestionsByJob(long jobId, int limit) {
        int l = Math.min(Math.max(limit, 1), 200);
        List<ImportJobItem> items = importJobItemMapper.selectList(new LambdaQueryWrapper<ImportJobItem>()
            .eq(ImportJobItem::getJobId, jobId)
            .isNotNull(ImportJobItem::getQuestionId)
            .orderByDesc(ImportJobItem::getId)
            .last("limit " + l));
        if (items.isEmpty()) {
            return List.of();
        }
        List<Long> qIds = items.stream().map(ImportJobItem::getQuestionId).toList();
        return questionMapper.selectList(new LambdaQueryWrapper<Question>()
            .in(Question::getId, qIds)
            .eq(Question::getStatus, "PENDING")
            .orderByDesc(Question::getId));
    }

    @Transactional
    public int approveAllPendingByJob(long jobId) {
        List<Question> qs = listPendingQuestionsByJob(jobId, 500);
        int n = 0;
        for (Question q : qs) {
            q.setStatus("ONLINE");
            questionMapper.updateById(q);
            n++;
        }
        return n;
    }

    @Transactional
    public int rejectAllPendingByJob(long jobId) {
        List<Question> qs = listPendingQuestionsByJob(jobId, 500);
        int n = 0;
        for (Question q : qs) {
            q.setStatus("OFFLINE");
            questionMapper.updateById(q);
            n++;
        }
        return n;
    }
}
