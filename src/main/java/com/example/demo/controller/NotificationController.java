package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
import com.example.demo.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private DateUtils dateUtils;

    @GetMapping
    public String list(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        List<Notification> notifications = notificationService.getUserNotifications(user.getId());

        List<Map<String, Object>> notificationDtos = new ArrayList<>();
        for (Notification notification : notifications) {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", notification.getId());
            dto.put("title", notification.getTitle());
            dto.put("content", notification.getContent());
            dto.put("createTime", notification.getCreateTime());
            dto.put("type", notification.getType());
            dto.put("relatedId", notification.getRelatedId());
            dto.put("isRead", notification.getIsRead());
            notificationDtos.add(dto);
        }

        model.addAttribute("notifications", notificationDtos);
        model.addAttribute("dateUtils", dateUtils);
        return "notifications/list";
    }

    @PostMapping("/{id}/read")
    @ResponseBody
    public Map<String, Boolean> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", true);
            return response;
        } catch (Exception e) {
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", false);
            return response;
        }
    }

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Notification>> getNotificationsApi(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            log.warn("未登录用户尝试获取通知");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.debug("查询用户 {} 的通知", user.getId());
        List<Notification> notifications = notificationService.getUserNotifications(user.getId());
        log.debug("用户 {} 的通知数量: {}", user.getId(), notifications.size());
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/api/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsReadApi(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "用户未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        log.debug("标记通知已读请求: id={}, userId={}", id, user.getId());
        Map<String, Object> response = new HashMap<>();
        try {
            if (id == null || id <= 0) {
                response.put("success", false);
                response.put("error", "无效的通知ID");
                return ResponseEntity.badRequest().body(response);
            }

            notificationService.markAsRead(id);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("标记通知已读失败: id={}, error={}", id, e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/read-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            notificationService.markAllAsRead(user.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/api/unread-count")
    @ResponseBody
    public Map<String, Object> getUnreadCount(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "用户未登录");
            return errorResult;
        }
        int count = notificationService.getUnreadCount(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return result;
    }
}
