package com.example.demo.model;

import lombok.Data;

@Data
public class Notification {
    private Long id;             // 主键
    private Long userId;         // 接收用户ID
    private String type;         // 通知类型（如"CLAIM_APPLY"表示认领申请）
    private String title;        // 通知标题
    private String content;      // 通知内容
    private Long relatedId;      // 相关业务ID（如认领记录ID）
    private Integer isRead=0;      // 已读状态(0:未读, 1:已读)
    private Long createTime;     // 创建时间戳
    
    // 修改isRead()方法
    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }
}