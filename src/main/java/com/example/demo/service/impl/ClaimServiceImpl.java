package com.example.demo.service.impl;

import com.example.demo.dto.ClaimDTO;
import com.example.demo.mapper.ClaimMapper;
import com.example.demo.mapper.FoundItemMapper;
import com.example.demo.mapper.LostItemMapper;
import com.example.demo.model.Claim;
import com.example.demo.model.FoundItem;
import com.example.demo.model.LostItem;
import com.example.demo.model.Notification;
import com.example.demo.service.ClaimService;
import com.example.demo.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
@Service
public class ClaimServiceImpl implements ClaimService {
    // 添加依赖注入注解
    @Autowired
    private ClaimMapper claimMapper;
    @Autowired
    private FoundItemMapper foundItemMapper;
    @Autowired
    private LostItemMapper lostItemMapper;
    @Autowired
    private NotificationService notificationService;

    // 添加状态常量定义
    private static final String STATUS_SEARCHING = "SEARCHING"; // 寻找中
    private static final String STATUS_CLAIM_PENDING = "CLAIM_PENDING"; // 待认领
    private static final String STATUS_PENDING = "PENDING"; // 认领申请待处理
    private static final String STATUS_APPROVED = "APPROVED"; // 认领申请已批准
    private static final String STATUS_REJECTED = "REJECTED"; // 认领申请已拒绝
    private static final String STATUS_CLAIMED = "CLAIMED"; // 已认领
    private static final String ITEM_TYPE_FOUND = "FOUND"; // 失物招领

    @Override
    @Transactional
    public void submitClaim(ClaimDTO claimDTO, Long userId) {
        // 1. 验证物品状态
        Long itemPublisherId = null;
        if (ITEM_TYPE_FOUND.equals(claimDTO.getItemType())) {
            FoundItem foundItem = foundItemMapper.selectById(claimDTO.getItemId());
            Assert.notNull(foundItem, "物品不存在");
            Assert.isTrue(STATUS_SEARCHING.equals(foundItem.getStatus()), "该物品已被认领");
            // 新增：验证不能认领自己发布的物品
            Assert.isTrue(!foundItem.getUserId().equals(userId), "不能认领自己发布的失物招领");
            itemPublisherId = foundItem.getUserId();
            // 更新物品状态为待认领
            foundItemMapper.updateStatus(claimDTO.getItemId(), STATUS_CLAIM_PENDING);
        } else {
            // 寻物启事同理
            LostItem lostItem = lostItemMapper.selectById(claimDTO.getItemId());
            Assert.notNull(lostItem, "物品不存在");
            Assert.isTrue(STATUS_SEARCHING.equals(lostItem.getStatus()), "该物品已被认领");
            // 验证不能认领自己发布的物品
            Assert.isTrue(!lostItem.getUserId().equals(userId), "不能认领自己发布的寻物启事");
            itemPublisherId = lostItem.getUserId();
            lostItemMapper.updateStatus(claimDTO.getItemId(), STATUS_CLAIM_PENDING);
        }

        // 2. 保存认领记录
        Claim claim = new Claim();
        claim.setItemId(claimDTO.getItemId());
        claim.setItemType(claimDTO.getItemType());
        claim.setClaimantId(userId);
        claim.setClaimDescription(claimDTO.getClaimDescription());
        claim.setStatus(STATUS_PENDING);
        // 移除以下两行代码
        // long currentTime = System.currentTimeMillis();
        // claim.setCreateTime(currentTime);
        // claim.setUpdateTime(currentTime);
        claimMapper.insert(claim);

        // 3. 创建通知
        Notification notification = new Notification();
        notification.setUserId(itemPublisherId);
        notification.setType("CLAIM_APPLY");
        notification.setTitle("新的认领申请");
        notification.setContent("您发布的物品有新的认领申请，请及时处理。");
        notification.setRelatedId(claim.getId());
        notification.setCreateTime(System.currentTimeMillis()); // 使用毫秒级时间戳
        notification.setIsRead(0); // 显式设置为未读状态
        notificationService.createNotification(notification);
    }

    @Override
    @Transactional
    public void approveClaim(Long claimId, Long approverId) {
        Claim claim = claimMapper.selectById(claimId);
        Assert.notNull(claim, "认领记录不存在");
        Assert.isTrue(STATUS_PENDING.equals(claim.getStatus()), "该认领申请已处理");

        // 1. 验证权限：确保是物品发布者才能审批
        Long itemPublisherId;
        if (ITEM_TYPE_FOUND.equals(claim.getItemType())) {
            FoundItem foundItem = foundItemMapper.selectById(claim.getItemId());
            Assert.notNull(foundItem, "物品不存在");
            itemPublisherId = foundItem.getUserId();
        } else {
            LostItem lostItem = lostItemMapper.selectById(claim.getItemId());
            Assert.notNull(lostItem, "物品不存在");
            itemPublisherId = lostItem.getUserId();
        }
        Assert.isTrue(itemPublisherId.equals(approverId), "只有物品发布者才能审批认领申请");

        // 2. 更新认领状态
        claim.setStatus(STATUS_APPROVED);
        claim.setApproverId(approverId);
        claim.setApprovalTime(System.currentTimeMillis());
        claim.setUpdateTime(System.currentTimeMillis());
        claimMapper.updateById(claim);

        // 3. 更新物品状态为已认领
        if (ITEM_TYPE_FOUND.equals(claim.getItemType())) {
            foundItemMapper.updateStatus(claim.getItemId(), STATUS_CLAIMED);
        } else {
            lostItemMapper.updateStatus(claim.getItemId(), STATUS_CLAIMED);
        }
        
        // 4. 标记原认领申请通知为已读
        notificationService.markRelatedAsRead("CLAIM_APPLY", claimId);
        
        // 5. 发送批准通知给认领申请人
        Notification notification = new Notification();
        notification.setUserId(claim.getClaimantId());
        notification.setType("CLAIM_APPROVED");
        notification.setTitle("认领申请已批准");
        notification.setContent("您的认领申请已被批准，物品已成功认领。");
        notification.setRelatedId(claim.getId());
        notification.setCreateTime(System.currentTimeMillis()); // 使用毫秒级时间戳
        notificationService.createNotification(notification);
    }

    @Override
    @Transactional
    public void rejectClaim(Long claimId, Long approverId, String reason) {
        Claim claim = claimMapper.selectById(claimId);
        Assert.notNull(claim, "认领记录不存在");
        Assert.isTrue(STATUS_PENDING.equals(claim.getStatus()), "该认领申请已处理");
    
        // 验证权限：确保是物品发布者才能拒绝
        Long itemPublisherId;
        if (ITEM_TYPE_FOUND.equals(claim.getItemType())) {
            FoundItem foundItem = foundItemMapper.selectById(claim.getItemId());
            Assert.notNull(foundItem, "物品不存在");
            itemPublisherId = foundItem.getUserId();
        } else {
            LostItem lostItem = lostItemMapper.selectById(claim.getItemId());
            Assert.notNull(lostItem, "物品不存在");
            itemPublisherId = lostItem.getUserId();
        }
        Assert.isTrue(itemPublisherId.equals(approverId), "只有物品发布者才能拒绝认领申请");
    
        // 更新认领状态
        claim.setStatus(STATUS_REJECTED);
        claim.setApproverId(approverId);
        claim.setApprovalTime(System.currentTimeMillis()); // 使用当前时间戳（毫秒级）
        claim.setRejectReason(reason);
        claim.setUpdateTime(System.currentTimeMillis()); //
        claimMapper.updateById(claim);
    
        // 恢复物品状态为寻找中
        if (ITEM_TYPE_FOUND.equals(claim.getItemType())) {
            foundItemMapper.updateStatus(claim.getItemId(), STATUS_SEARCHING);
        } else {
            lostItemMapper.updateStatus(claim.getItemId(), STATUS_SEARCHING);
        }
        // 发送拒绝通知给认领申请人
        Notification notification = new Notification();
        notification.setUserId(claim.getClaimantId());
        notification.setType("CLAIM_REJECTED");
        notification.setTitle("认领申请已拒绝");
        notification.setContent("您的认领申请已被拒绝，原因：" + reason);
        notification.setRelatedId(claim.getId());
        notification.setCreateTime(System.currentTimeMillis()); // 使用毫秒级时间戳
        notificationService.createNotification(notification);
    }
    
    @Override
    public List<Claim> getAllClaims() {
        return claimMapper.selectAll();
    }
    
    @Override
    public Claim getClaimById(Long claimId) {
        return claimMapper.selectById(claimId);
    }
}