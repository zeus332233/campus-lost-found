package com.example.demo.controller;
import com.example.demo.model.User;
import com.example.demo.service.LostItemService;
import com.example.demo.service.FoundItemService;
import com.example.demo.service.NotificationService;
import com.example.demo.util.DateUtils;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private FoundItemService foundItemService;

    @Autowired
    private LostItemService lostItemService;

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("dateUtils", dateUtils);
        model.addAttribute("foundItems", foundItemService.getLatestFoundItems());
        model.addAttribute("lostItems", lostItemService.getLatestLostItems());

        int unreadCount = notificationService.getUnreadCount(user.getId());
        model.addAttribute("unreadCount", unreadCount);

        // 统计数据
        int totalFound = foundItemService.countAll();
        int totalLost = lostItemService.countAll();
        int claimedFound = foundItemService.countByStatus("CLAIMED");
        int claimedLost = lostItemService.countByStatus("CLAIMED");
        int pendingFound = foundItemService.countByStatus("CLAIM_PENDING");
        int pendingLost = lostItemService.countByStatus("CLAIM_PENDING");

        model.addAttribute("totalFound", totalFound);
        model.addAttribute("totalLost", totalLost);
        model.addAttribute("totalItems", totalFound + totalLost);
        model.addAttribute("claimedTotal", claimedFound + claimedLost);
        model.addAttribute("pendingTotal", pendingFound + pendingLost);
        model.addAttribute("searchingFound", totalFound - claimedFound - pendingFound);

        return "dashboard";
    }
}
