package com.example.demo.controller;

import com.example.demo.model.FoundItem;
import com.example.demo.model.LostItem;
import com.example.demo.model.User;
import com.example.demo.service.FoundItemService;
import com.example.demo.service.LostItemService;
import com.example.demo.dto.LostItemDTO;
import com.example.demo.service.impl.MatchEngineService;
import com.example.demo.util.DateUtils;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/lost-items")
public class LostItemController {

    @Autowired
    private LostItemService lostItemService;

    @Autowired
    private FoundItemService foundItemService;

    @Autowired
    private MatchEngineService matchEngineService;

    @Autowired
    private DateUtils dateUtils;

    @Value("${upload.base-path}")
    private String uploadPath;

    private static final Set<String> ALLOWED_IMAGE_EXT = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    ));
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /** 列表页 — 支持分页搜索 */
    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String location,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) Long startDate,
                       @RequestParam(required = false) Long endDate,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Map<String, Object> pageResult = lostItemService.searchWithPage(
                keyword, category, location, status, startDate, endDate, page, size);

        model.addAttribute("lostItems", pageResult.get("items"));
        model.addAttribute("total", pageResult.get("total"));
        model.addAttribute("currentPage", pageResult.get("page"));
        model.addAttribute("totalPages", pageResult.get("totalPages"));
        model.addAttribute("size", size);
        model.addAttribute("dateUtils", dateUtils);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("location", location);
        model.addAttribute("status", status);
        return "lostItems/list";
    }

    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("lostItem", new LostItem());
        return "lostItems/create";
    }

    @PostMapping
    public String create(@ModelAttribute LostItemDTO lostItemDTO,
                         @RequestParam(value = "images", required = false) MultipartFile[] files,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        String imageUrls = processUploadedImages(files);
        if (imageUrls != null) {
            lostItemDTO.setImageUrls(imageUrls);
        }

        try {
            LostItem saved = lostItemService.createLostItem(lostItemDTO, user.getId());
            // 触发智能匹配
            List<MatchEngineService.MatchResult> matches = matchEngineService.findMatchesForLostItem(saved);
            if (!matches.isEmpty()) {
                // 对匹配度最高的3条发送通知
                int notifyCount = Math.min(matches.size(), 3);
                for (int i = 0; i < notifyCount; i++) {
                    MatchEngineService.MatchResult match = matches.get(i);
                    if ("FOUND".equals(match.getItemType())) {
                        FoundItem matchedFound = foundItemService.getById(match.getItemId());
                        if (matchedFound != null) {
                            matchEngineService.notifyMatch(matchedFound, saved, match.getScore());
                        }
                    }
                }
                redirectAttributes.addFlashAttribute("matchInfo",
                        "发现 " + matches.size() + " 条可能匹配的失物招领（已通知相关用户）");
            }
            redirectAttributes.addFlashAttribute("message", "寻物启事发布成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lost-items";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        try {
            LostItem lostItem = lostItemService.getLostItemById(id);
            if (lostItem == null) {
                model.addAttribute("errorMsg", "未找到该寻物启事信息");
                return "error";
            }

            if (lostItem.getImageUrls() != null && !lostItem.getImageUrls().isEmpty()) {
                model.addAttribute("imageUrls", lostItem.getImageUrls().split(","));
            } else {
                model.addAttribute("imageUrls", new String[0]);
            }

            User user = (User) session.getAttribute("user");
            boolean isOwner = user != null && user.getId().equals(lostItem.getUserId());
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("lostItem", lostItem);
            model.addAttribute("dateUtils", dateUtils);
            return "lostItems/detail";
        } catch (Exception e) {
            model.addAttribute("errorMsg", "获取寻物启事详情失败: " + e.getMessage());
            return "error";
        }
    }

    /** 编辑页面 */
    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        LostItem lostItem = lostItemService.getLostItemById(id);
        if (lostItem == null || !lostItem.getUserId().equals(user.getId())) {
            return "redirect:/lost-items";
        }
        model.addAttribute("lostItem", lostItem);
        return "lostItems/edit";
    }

    /** 提交编辑 */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute LostItemDTO lostItemDTO,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        try {
            lostItemService.updateLostItem(id, lostItemDTO, user.getId());
            redirectAttributes.addFlashAttribute("message", "寻物启事信息更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lost-items/" + id;
    }

    /** 删除 */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        try {
            lostItemService.deleteLostItem(id, user.getId());
            redirectAttributes.addFlashAttribute("message", "寻物启事已删除");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lost-items";
    }

    /** 图片上传处理 */
    private String processUploadedImages(MultipartFile[] files) {
        if (files == null || files.length == 0) return null;
        List<String> imageUrls = new ArrayList<>();
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            if (file.getSize() > MAX_FILE_SIZE) continue;

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) continue;
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!ALLOWED_IMAGE_EXT.contains(extension)) continue;

            try {
                String fileName = UUID.randomUUID().toString() + extension;
                File destFile = new File(uploadPath + File.separator + fileName);
                file.transferTo(destFile);
                imageUrls.add("/" + fileName);
            } catch (IOException ignored) {
            }
        }
        return imageUrls.isEmpty() ? null : String.join(",", imageUrls);
    }
}
