package com.example.demo.service;

import com.example.demo.model.Notification;

import java.util.List;

public interface NotificationService {
    void createNotification(Notification notification);
    List<Notification> getUserNotifications(Long userId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
    int getUnreadCount(Long userId);
    void markRelatedAsRead(String type, Long relatedId);
}