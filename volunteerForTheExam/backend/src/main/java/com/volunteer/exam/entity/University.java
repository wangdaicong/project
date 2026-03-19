package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("university")
public class University implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String province;
    private String city;
    private String level;
    private String type;
    private String tags;
    private String introduction;
    private String address;
    private String website;
    private String phone;
    private Integer minScore;
    private Integer maxScore;
    private String logoUrl;
    private Integer ranking;
    private String features;
}
