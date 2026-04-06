package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private String unionid;
    private String nickname;
    private String avatar;
    private Integer gender;
    private String phone;
    private String province;
    private String city;
    private Integer score;
    private Integer year;
    private String subjectType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime lastLoginTime;
    private Integer status;
}
