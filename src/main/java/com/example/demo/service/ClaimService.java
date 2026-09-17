package com.example.demo.service;

import com.example.demo.dto.ClaimDTO;
import com.example.demo.model.Claim;

import java.util.List;

public interface ClaimService {
    /**
     * 提交认领申请
     * @param claimDTO 认领申请数据
     * @param userId 认领人ID
     */
    void submitClaim(ClaimDTO claimDTO, Long userId);

    /**
     * 审批认领申请
     * @param claimId 认领记录ID
     * @param approverId 审批人ID
     */
    void approveClaim(Long claimId, Long approverId);
    
    /**
     * 拒绝认领申请
     * @param claimId 认领记录ID
     * @param approverId 审批人ID
     * @param reason 拒绝原因
     */
    void rejectClaim(Long claimId, Long approverId, String reason);
    
    /**
     * 获取所有认领记录
     * @return 认领记录列表
     */
    List<Claim> getAllClaims();
    
    /**
     * 根据ID获取认领记录
     * @param claimId 认领记录ID
     * @return 认领记录
     */
    Claim getClaimById(Long claimId);
}