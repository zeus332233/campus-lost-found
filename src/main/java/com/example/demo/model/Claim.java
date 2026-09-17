package com.example.demo.model;

import lombok.Data;

@Data
public class Claim {
    private Long id;//主键
    private Long itemId;//被认领物品ID
    private String itemType;//物品状态类型
    private Long claimantId;//认领人ID
    private String claimDescription;//认领描述
    private String status;//状态
    private Long approverId;//审核人
    private Long approvalTime;//审核时间（时间戳）
    private String rejectReason;//拒绝原因
    private Long createTime;//创建时间（时间戳）
    private Long updateTime;//更新时间（时间戳）
}