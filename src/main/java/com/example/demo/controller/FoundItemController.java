package com.example.demo.controller;

import com.example.demo.model.FoundItem;
import com.example.demo.model.LostItem;
import com.example.demo.model.User;
import com.example.demo.service.FoundItemService;
import com.example.demo.service.LostItemService;
import com.example.demo.dto.FoundItemDTO;
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
@RequestMapping("/found-items")
public class FoundItemController {

    @Autowired
    private FoundItemService foundItemService;

    @Autowired
    private LostItemService lostItemService;

    @Autowired
    private MatchEngineService matchEngineService;

    @Autowired
    private DateUtils dateUtils;

    @Value("${upload.base-path}")
    private String uploadPath;

    /** 允许的图片扩展名 */
    private static final Set<String> ALLOWED_IMAGE_EXT = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    ));
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 列表页 — 支持分页和搜索
     */
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
        Map<String, Object> pageResult = foundItemService.searchWithPage(
                keyword, category, location, status, startDate, endDate, page, size);

        model.addAttribute("foundItems", pageResult.get("items"));
        model.addAttribute("total", pageResult.get("total"));
        model.addAttribute("currentPage", pageResult.get("page"));
        model.addAttribute("totalPages", pageResult.get("totalPages"));
        model.addAttribute("size", size);
        model.addAttribute("dateUtils", dateUtils);
        // 回传搜索条件
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("location", location);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate != null ? dateUtils.formatTimestamp(startDate) : null);
        model.addAttribute("endDate", endDate != null ? dateUtils.formatTimestamp(endDate) : null);
        return "foundItems/list";
    }

    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("foundItem", new FoundItem());
        return "foundItems/create";
    }

    @PostMapping
    public String create(@ModelAttribute FoundItemDTO foundItemDTO,
                         @RequestParam(value = "images", required = false) MultipartFile[] files,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        // 处理图片上传（含安全校验）
        String imageUrls = processUploadedImages(files);
        if (imageUrls != null) {
            foundItemDTO.setImageUrls(imageUrls);
        }

        try {
            FoundItem saved = foundItemService.createFoundItem(foundItemDTO, user.getId());
            // 触发智能匹配
            List<MatchEngineService.MatchResult> matches = matchEngineService.findMatchesForFoundItem(saved);
            if (!matches.isEmpty()) {
                // 对匹配度最高的3条发送通知
                int notifyCount = Math.min(matches.size(), 3);
                for (int i = 0; i < notifyCount; i++) {
                    MatchEngineService.MatchResult match = matches.get(i);
                    if ("LOST".equals(match.getItemType())) {
                        LostItem matchedLost = lostItemService.getLostItemById(match.getItemId());
                        if (matchedLost != null) {
                            matchEngineService.notifyMatch(saved, matchedLost, match.getScore());
                        }
                    }
                }
                redirectAttributes.addFlashAttribute("matchInfo",
                        "发现 " + matches.size() + " 条可能匹配的寻物启事（已通知相关用户）");
            }
            redirectAttributes.addFlashAttribute("message", "失物招领发布成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/found-items";
    }

    @GetMapping("/{id}")
    public String getFoundItemDetail(@PathVariable Long id, Model model, HttpSession session) {
        FoundItem foundItem = foundItemService.getById(id);
        if (foundItem == null) {
            return "error/404";
        }
        if (foundItem.getImageUrls() != null && !foundItem.getImageUrls().isEmpty()) {
            model.addAttribute("imageUrls", foundItem.getImageUrls().split(","));
        } else {
            model.addAttribute("imageUrls", new String[0]);
        }

        // 判断当前用户是否是发布者
        User user = (User) session.getAttribute("user");
        boolean isOwner = user != null && user.getId().equals(foundItem.getUserId());
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("foundItem", foundItem);
        model.addAttribute("dateUtils", dateUtils);
        return "foundItems/detail";
    }

    /** 编辑页面 */
    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        FoundItem foundItem = foundItemService.getById(id);
        if (foundItem == null || !foundItem.getUserId().equals(user.getId())) {
            return "redirect:/found-items";
        }
        model.addAttribute("foundItem", foundItem);
        return "foundItems/edit";
    }

    /** 提交编辑 */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute FoundItemDTO foundItemDTO,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        try {
            foundItemService.updateFoundItem(id, foundItemDTO, user.getId());
            redirectAttributes.addFlashAttribute("message", "失物招领信息更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/found-items/" + id;
    }

    /** 删除 */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        try {
            foundItemService.deleteFoundItem(id, user.getId());
            redirectAttributes.addFlashAttribute("message", "失物招领已删除");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/found-items";
    }

    @GetMapping("/{id}/claim")
    public String claimPage(@PathVariable Long id, Model model) {
        model.addAttribute("itemId", id);
        model.addAttribute("itemType", "FOUND");
        return "claims/create";
    }

    @GetMapping("/my")
    @ResponseBody
    public List<FoundItem> getMyFoundItems(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new ArrayList<>();
        }
        return foundItemService.getFoundItemsByUserId(user.getId());
    }

    /** 图片上传处理（含安全校验） */
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
            } catch (IOException e) {
                // 跳过上传失败的文件
            }
        }
        return imageUrls.isEmpty() ? null : String.join(",", imageUrls);
    }
}
