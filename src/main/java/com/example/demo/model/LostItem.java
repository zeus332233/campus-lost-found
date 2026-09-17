package com.example.demo.model;

import lombok.Data;
import java.util.Date;

@Data
public class LostItem {
    private Long id;
    private Long userId;
    private String title; // //标题
    private String description;//描述
    private String category;
    private String location;//丢失地点
    private Long lostTime; // 丢失时间时间戳
    private String status; // SEARCHING/FOUND
    private String contactPhone;//联系电话
    private String contactEmail;//电子邮箱
    private Long createdAt; // 创建时间
    private Long updatedAt; // 更新时间
    private String imageUrls; // 图片url
}

