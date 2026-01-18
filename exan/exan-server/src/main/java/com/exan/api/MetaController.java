package com.exan.api;

import com.exan.app.service.MetaService;
import com.exan.domain.entity.EduStage;
import com.exan.domain.entity.Subject;
import com.exan.infra.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/meta")
@RequiredArgsConstructor
public class MetaController {
    private final MetaService metaService;

    @GetMapping("/stages")
    public ApiResponse<List<EduStage>> stages() {
        return ApiResponse.ok(metaService.listStages());
    }

    @GetMapping("/subjects")
    public ApiResponse<List<Subject>> subjects(@RequestParam long stageId) {
        return ApiResponse.ok(metaService.listSubjects(stageId));
    }
}
