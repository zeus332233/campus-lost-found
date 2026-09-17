package com.example.demo.controller;

import com.example.demo.dto.UserProfileDTO;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.example.demo.service.FoundItemService;
import com.example.demo.service.LostItemService;
import com.example.demo.service.NotificationService;
import com.example.demo.util.CaptchaUtils;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.HashMap;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.HashMap;
import java.util.Map;
import com.example.demo.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 添加服务依赖注入
    @Autowired
    private FoundItemService foundItemService;
    
    @Autowired
    private LostItemService lostItemService;
    // 添加通知服务依赖
    @Autowired
    private NotificationService notificationService;

    //添加验证码服务依赖
    @Autowired
    private CaptchaUtils captchaUtils;

    //添加时间处理方法
    @Autowired
    private DateUtils dateUtils;

    // 修改profile方法，添加未读通知数量
    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        
        // 获取用户发布的失物招领和寻物启事
        model.addAttribute("user", user);
        model.addAttribute("myFoundItems", foundItemService.getFoundItemsByUserId(user.getId()));
        model.addAttribute("myLostItems", lostItemService.getLostItemsByUserId(user.getId()));
        // 添加dateUtils到模型
        model.addAttribute("dateUtils", dateUtils);
        // 添加未读通知数量
        int unreadCount = notificationService.getUnreadCount(user.getId());
        model.addAttribute("unreadCount", unreadCount);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute UserProfileDTO profileDTO, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return "redirect:/user/login";
            }
            
            boolean success = userService.updateUserProfile(user.getId(), profileDTO);
            if (success) {
                user.setPhone(profileDTO.getPhone());
                user.setEmail(profileDTO.getEmail());
                session.setAttribute("user", user);
                redirectAttributes.addFlashAttribute("message", "个人信息更新成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "更新失败，请重试");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "系统繁忙，请稍后再试");
        }
        return "redirect:/user/profile";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 上传并更新用户头像
     */
    @PostMapping("/avatar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAvatar(@RequestParam("avatarUrl") String avatarUrl,
                                                              HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "头像地址不能为空");
            return ResponseEntity.badRequest().body(result);
        }
        try {
            boolean updated = userService.updateAvatar(user.getId(), avatarUrl.trim());
            if (updated) {
                user.setAvatar(avatarUrl.trim());
                session.setAttribute("user", user);
                result.put("success", true);
                result.put("message", "头像更新成功");
                result.put("avatarUrl", avatarUrl.trim());
            } else {
                result.put("success", false);
                result.put("message", "头像更新失败，请重试");
            }
        } catch (Exception e) {
            logger.error("更新头像失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "系统繁忙，请稍后再试");
        }
        return ResponseEntity.ok(result);
    }


    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(User user, Model model) {
        try {
            userService.register(user);
            model.addAttribute("message", "注册成功，请登录");
            return "login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "register";
        }
    }


    // 添加生成验证码的方法
    @GetMapping("/captcha")
    @ResponseBody
    public Map<String, String> generateCaptcha(HttpSession session) {
        String code = captchaUtils.generateCode();
        String imageBase64 = captchaUtils.generateImage(code);
        session.setAttribute("captcha", code);

        Map<String, String> result = new HashMap<>();
        result.put("image", imageBase64);
        return result;
    }

    // 修改登录方法，添加验证码验证
    @PostMapping("/login")
    public String login(String username, String password, String captcha, Model model, HttpSession session) {
        try {
            // 测试环境：临时跳过验证码验证（实际部署时请注释掉以下三行）
            if ("test".equals(username) && "123456".equals(password)) {
                User user = userService.getUserByUsername(username);
                session.setAttribute("user", user);
                return "redirect:/dashboard";
            }
            
            // 生产环境：验证验证码
            String sessionCaptcha = (String) session.getAttribute("captcha");
            if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
                model.addAttribute("error", "验证码错误");
                model.addAttribute("username", username);
                return "login";
            }

            User user = userService.login(username, password);
            session.setAttribute("user", user);
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            return "login";
        }
    }
    // 在登录方法之后添加
    // 在类级别添加日志记录器
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    // 修改logout方法
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        try {
            logger.info("用户退出登录");
            session.invalidate();  // 使当前会话失效
            return "redirect:/user/login";  // 重定向到登录页面
        } catch (Exception e) {
            logger.error("退出登录失败: {}", e.getMessage(), e);
            throw new RuntimeException("退出登录失败，请重试");
        }
    }
}