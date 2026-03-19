package com.volunteer.exam.controller;

import com.volunteer.exam.common.Result;
import com.volunteer.exam.entity.Major;
import com.volunteer.exam.service.MajorService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/major")
@CrossOrigin
public class MajorController {
    
    @Resource
    private MajorService majorService;

    @GetMapping("/university/{universityId}")
    public Result<List<Major>> getByUniversity(@PathVariable Long universityId) {
        List<Major> list = majorService.getByUniversityId(universityId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Major> getById(@PathVariable Long id) {
        Major major = majorService.getById(id);
        return Result.success(major);
    }

    @GetMapping("/category")
    public Result<List<Major>> getByCategory(@RequestParam String category) {
        List<Major> list = majorService.queryByCategory(category);
        return Result.success(list);
    }

    @GetMapping("/search")
    public Result<List<Major>> search(@RequestParam String keyword) {
        List<Major> list = majorService.searchMajors(keyword);
        return Result.success(list);
    }
}
