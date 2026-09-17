package com.example.demo.service.impl;

import com.example.demo.mapper.NotificationMapper;
import com.example.demo.model.Notification;
import com.example.demo.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public void createNotification(Notification notification) {
        if (notification.getIsRead() == null) {
            notification.setIsRead(0);
        }
        notificationMapper.insert(notification);
        log.debug("通知已创建: userId={}, type={}", notification.getUserId(), notification.getType());
    }

    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId) {
        log.debug("标记通知已读: id={}", notificationId);
        Map<String, Object> params = new HashMap<>();
        params.put("id", notificationId);
        params.put("isRead", 1);
        int rowsAffected = notificationMapper.updateReadStatus(params);
        if (rowsAffected == 0) {
            log.warn("未找到通知: id={}", notificationId);
            throw new RuntimeException("未找到通知ID: " + notificationId);
        }
    }

    @Override
    @Transactional
    public void markRelatedAsRead(String type, Long relatedId) {
        log.debug("标记关联通知已读: type={}, relatedId={}", type, relatedId);
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("relatedId", relatedId);
        params.put("isRead", 1);
        int rowsAffected = notificationMapper.updateReadStatusByTypeAndRelatedId(params);
        if (rowsAffected == 0) {
            log.debug("未找到关联通知: type={}, relatedId={}", type, relatedId);
        }
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        log.debug("标记所有通知已读: userId={}", userId);
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("isRead", 1);
        int rowsAffected = notificationMapper.updateAllReadStatus(params);
        log.info("用户 {} 的所有通知已标记为已读，影响行数: {}", userId, rowsAffected);
    }
}
