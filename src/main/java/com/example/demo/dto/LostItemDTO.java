package com.example.demo.dto;

import lombok.Data;

@Data
public class LostItemDTO {
    private String title;
    private String description;
    private String location;
    private String contactInfo;
    private String imageUrls; // 存储图片URL的逗号分隔字符串
    
    // 添加缺失的字段
    private String category;
    private Long lostTime;
    private String contactPhone;
    private String contactEmail;
}