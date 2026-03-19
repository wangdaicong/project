package com.volunteer.exam.util;

import com.volunteer.exam.entity.University;
import com.volunteer.exam.mapper.UniversityMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Order(1)
public class DataFixUtil implements CommandLineRunner {
    
    @Resource
    private UniversityMapper universityMapper;
    
    @Override
    public void run(String... args) {
        System.out.println("开始检查并修复院校数据...");
        
        List<University> universities = universityMapper.selectList(null);
        int fixedCount = 0;
        
        for (University uni : universities) {
            boolean needUpdate = false;
            
            if (uni.getIntroduction() == null || uni.getIntroduction().isEmpty()) {
                uni.setIntroduction(uni.getName() + "是一所位于" + uni.getProvince() + uni.getCity() + 
                    "的" + uni.getType() + "类高等院校，办学层次为" + uni.getLevel() + 
                    "。学校秉承优良传统，致力于培养高素质人才。");
                needUpdate = true;
            }
            
            if (uni.getFeatures() == null || uni.getFeatures().isEmpty()) {
                uni.setFeatures(uni.getType() + "特色鲜明，学科实力雄厚");
                needUpdate = true;
            }
            
            if (uni.getAddress() == null || uni.getAddress().isEmpty()) {
                uni.setAddress(uni.getProvince() + uni.getCity() + "校区");
                needUpdate = true;
            }
            
            if (uni.getPhone() == null || uni.getPhone().isEmpty()) {
                uni.setPhone("待补充");
                needUpdate = true;
            }
            
            if (needUpdate) {
                universityMapper.updateById(uni);
                fixedCount++;
            }
        }
        
        System.out.println("数据修复完成！共修复 " + fixedCount + " 所院校数据");
    }
}
