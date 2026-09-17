package com.example.demo.controller;

import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(HttpSession session) {
        // 如果用户已登录，重定向到仪表板；否则重定向到登录页面
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        return "redirect:/user/login";
    }
}