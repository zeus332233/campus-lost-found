package com.example.demo.controller.api;

import com.example.demo.model.FoundItem;
import com.example.demo.model.User;
import com.example.demo.service.FoundItemService;
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
@RequestMapping("/api/found-items")
public class FoundItemApiController {
    @Autowired
    private FoundItemService foundItemService;

    /**
     * 获取所有失物招领列表（JSON格式）
     */
    @GetMapping
    public ResponseEntity<List<FoundItem>> getAllFoundItems() {
        List<FoundItem> foundItems = foundItemService.getAllFoundItems();
        return ResponseEntity.ok(foundItems != null ? foundItems : new ArrayList<>());
    }

    /**
     * 获取单个失物招领详情（JSON格式）
     */
    @GetMapping("/{id}")
    public ResponseEntity<FoundItem> getFoundItemById(@PathVariable Long id) {
        FoundItem foundItem = foundItemService.getById(id);
        if (foundItem == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(foundItem);
    }

    /**
     * 获取当前登录用户发布的失物招领列表（JSON格式）
     */
    @GetMapping("/my")
    public ResponseEntity<List<FoundItem>> getMyFoundItems(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(new ArrayList<>()); // 未登录返回401状态码
        }
        List<FoundItem> foundItems = foundItemService.getFoundItemsByUserId(user.getId());
        return ResponseEntity.ok(foundItems != null ? foundItems : new ArrayList<>());
    }
}
