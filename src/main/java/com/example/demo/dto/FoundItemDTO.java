package com.example.demo.dto;

import lombok.Data;

@Data
public class FoundItemDTO {
    private String title;
    private String description;
    private String category;
    private String location;
    private Long foundTime; // 时间戳(毫秒)
    private String contactPhone;
    private String contactEmail;
    private String imageUrls;//上传图片
}