package com.example.demo.controller.api;

import com.example.demo.model.LostItem;
import com.example.demo.model.User;
import com.example.demo.service.LostItemService;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/lost-items")
public class LostItemApiController {
    @Autowired
    private LostItemService lostItemService;

    /**
     * 获取所有寻物启事列表（JSON格式）
     */
    @GetMapping
    public ResponseEntity<List<LostItem>> getAllLostItems() {
        List<LostItem> lostItems = lostItemService.getAllLostItems();
        return ResponseEntity.ok(lostItems != null ? lostItems : new ArrayList<>());
    }

    /**
     * 获取单个寻物启事详情（JSON格式）
     */
    @GetMapping("/{id}")
    public ResponseEntity<LostItem> getLostItemById(@PathVariable Long id) {
        LostItem lostItem = lostItemService.getLostItemById(id);
        if (lostItem == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lostItem);
    }

    /**
     * 获取当前登录用户发布的寻物启事列表（JSON格式）
     */
    @GetMapping("/my")
    public ResponseEntity<List<LostItem>> getMyLostItems(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(new ArrayList<>()); // 未登录返回401状态码
        }
        List<LostItem> lostItems = lostItemService.getLostItemsByUserId(user.getId());
        return ResponseEntity.ok(lostItems != null ? lostItems : new ArrayList<>());
    }
}
