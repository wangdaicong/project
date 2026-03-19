package com.volunteer.exam.controller;

import com.volunteer.exam.common.Result;
import com.volunteer.exam.entity.Career;
import com.volunteer.exam.service.CareerService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/career")
@CrossOrigin
public class CareerController {
    
    @Resource
    private CareerService careerService;

    @GetMapping("/industry")
    public Result<List<Career>> getByIndustry(@RequestParam(required = false) Long industryId) {
        List<Career> list = careerService.queryByIndustryId(industryId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Career> getById(@PathVariable Long id) {
        Career career = careerService.getById(id);
        return Result.success(career);
    }

    @GetMapping("/hot")
    public Result<List<Career>> getHot() {
        List<Career> list = careerService.getHotCareers();
        return Result.success(list);
    }

    @GetMapping("/search")
    public Result<List<Career>> search(@RequestParam String keyword) {
        List<Career> list = careerService.searchCareers(keyword);
        return Result.success(list);
    }
}
