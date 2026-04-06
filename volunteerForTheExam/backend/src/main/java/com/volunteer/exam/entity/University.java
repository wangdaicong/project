package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("university")
public class University implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("school_name")
    private String schoolName;
    
    @TableField("school_id_code")
    private String schoolIdCode;
    
    private String supervisor;
    
    private String location;
    
    @TableField("school_level")
    private String schoolLevel;
    
    private String remarks;
    
    private String website;
    
    private String address;
    
    private String phone;
    
    @TableField("logo_url")
    private String logoUrl;
    
    @TableField("wechat_name")
    private String wechatName;
    
    @TableField("wechat_id")
    private String wechatId;
    
    @TableField("weibo_name")
    private String weiboName;
    
    @TableField("weibo_id")
    private String weiboId;
    
    @TableField("baijia_name")
    private String baijiaName;
    
    @TableField("baijia_id")
    private String baijiaId;
    
    @TableField("video_name")
    private String videoName;
    
    @TableField("video_id")
    private String videoId;
    
    private String introduction;
    
    @TableField("is_985")
    private Boolean is985;
    
    @TableField("is_211")
    private Boolean is211;
    
    @TableField("is_double_first_class")
    private Boolean isDoubleFirstClass;
    
    @TableField("created_time")
    private LocalDateTime createdTime;
    
    @TableField("updated_time")
    private LocalDateTime updatedTime;
}
