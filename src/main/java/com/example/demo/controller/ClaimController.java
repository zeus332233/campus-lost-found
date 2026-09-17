package com.example.demo.controller;

import com.example.demo.dto.ClaimDTO;
import com.example.demo.model.Claim;
import com.example.demo.model.FoundItem;
import com.example.demo.model.LostItem;
import com.example.demo.model.User;
import com.example.demo.service.ClaimService;
import com.example.demo.service.FoundItemService;
import com.example.demo.service.LostItemService;
import com.example.demo.service.UserService;
import com.example.demo.util.DateUtils;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/claims")
public class ClaimController {
    @Autowired
    private ClaimService claimService;
    @Autowired
    private FoundItemService foundItemService;
    @Autowired
    private LostItemService lostItemService;
    @Autowired
    private UserService userService;
    @Autowired
    private DateUtils dateUtils;

    // 提交认领申请
    @PostMapping
    public String submitClaim(@ModelAttribute ClaimDTO claimDTO, HttpSession session) {
        // 修改：从session获取User对象而不是userId
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        Long userId = user.getId();
        claimService.submitClaim(claimDTO, userId);
        return "redirect:/found-items/" + claimDTO.getItemId();
    }

    // 认领申请页面
    @GetMapping("/create")
    public String createPage(@RequestParam Long itemId, @RequestParam String itemType, Model model) {
        model.addAttribute("itemId", itemId);
        model.addAttribute("itemType", itemType);
        return "claims/create";
    }

    // 审批认领申请
    @PostMapping("/{id}/approve")
    public String approveClaim(@PathVariable Long id, HttpSession session) {
        // 修改：从session获取User对象
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        Long approverId = user.getId();
        claimService.approveClaim(id, approverId);
        return "redirect:/notifications";
    }

    // 拒绝认领申请
    @PostMapping("/{id}/reject")
    public String rejectClaim(@PathVariable Long id, @RequestParam String reason, HttpSession session) {
        // 修改：从session获取User对象
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        Long approverId = user.getId();
        claimService.rejectClaim(id, approverId, reason);
        return "redirect:/notifications";
    }

    // 认领记录列表
    @GetMapping
    public String list(Model model) {
        // 实现列表查询逻辑
        List<Claim> claims = claimService.getAllClaims();
        
        // 添加物品标题和认领人姓名信息
        for (Claim claim : claims) {
            // 添加物品标题
            String itemTitle = "未知";
            if ("FOUND".equals(claim.getItemType())) {
                FoundItem foundItem = foundItemService.getById(claim.getItemId());
                if (foundItem != null) {
                    itemTitle = foundItem.getTitle();
                }
            } else if ("LOST".equals(claim.getItemType())) {
                LostItem lostItem = lostItemService.getById(claim.getItemId());
                if (lostItem != null) {
                    itemTitle = lostItem.getTitle();
                }
            }
            model.addAttribute("itemTitle_" + claim.getId(), itemTitle);
            
            // 添加认领人姓名
            User claimant = userService.getUserById(claim.getClaimantId());
            model.addAttribute("claimantName_" + claim.getId(), claimant != null ? claimant.getUsername() : "未知");
        }
        
        model.addAttribute("claims", claims);
        model.addAttribute("dateUtils", dateUtils);
        return "claims/list";
    }

    // 认领详情
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Claim claim = claimService.getClaimById(id);
        if (claim == null) {
            return "error/404";
        }
        // 获取关联信息
        User claimant = userService.getUserById(claim.getClaimantId());
        model.addAttribute("claimantName", claimant != null ? claimant.getUsername() : "未知");

        if (claim.getApproverId() != null) {
            User approver = userService.getUserById(claim.getApproverId());
            model.addAttribute("approverName", approver != null ? approver.getUsername() : "未知");
        }

        // 获取物品标题
        if ("FOUND".equals(claim.getItemType())) {
            FoundItem foundItem = foundItemService.getById(claim.getItemId());
            model.addAttribute("itemTitle", foundItem != null ? foundItem.getTitle() : "未知");
        } else {
            LostItem lostItem = lostItemService.getById(claim.getItemId());
            model.addAttribute("itemTitle", lostItem != null ? lostItem.getTitle() : "未知");
        }

        model.addAttribute("claim", claim);
        model.addAttribute("dateUtils", dateUtils);
        return "claims/detail";
    }
}