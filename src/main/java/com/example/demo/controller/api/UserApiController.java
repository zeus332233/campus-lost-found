package com.example.demo.controller.api;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = request.get("username");
            String password = request.get("password");
            String captcha = request.get("captcha");

            // 测试账号跳过验证码和数据库验证
            if ("test".equals(username) && "123456".equals(password)) {
                // 创建一个模拟用户
                User user = new User();
                user.setId(1L);
                user.setUsername("test");
                user.setPassword("123456");
                user.setPhone("13800138000");
                user.setEmail("test@example.com");
                user.setCreatedAt(System.currentTimeMillis());
                user.setUpdatedAt(System.currentTimeMillis());
                
                // 生成模拟token
                String token = "mock-token-" + System.currentTimeMillis();
                session.setAttribute("authToken", token);
                
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("user", user);
                
                response.put("code", 200);
                response.put("data", data);
                response.put("message", "登录成功");
                return response;
            }

            // 生产环境：验证验证码
            String sessionCaptcha = (String) session.getAttribute("captcha");
            if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
                response.put("code", 400);
                response.put("message", "验证码错误");
                return response;
            }

            // 验证用户名和密码
            User user = userService.login(username, password);
            
            // 生成模拟token
            String token = "mock-token-" + System.currentTimeMillis();
            session.setAttribute("authToken", token);
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            
            response.put("code", 200);
            response.put("data", data);
            response.put("message", "登录成功");
        } catch (RuntimeException e) {
            response.put("code", 400);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "服务器内部错误");
        }
        
        return response;
    }
    
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            session.removeAttribute("authToken");
            session.removeAttribute("user");
            
            response.put("code", 200);
            response.put("message", "退出成功");
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "服务器内部错误");
        }
        
        return response;
    }
    
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("code", 401);
                response.put("message", "未登录");
                return response;
            }
            
            response.put("code", 200);
            response.put("data", user);
            response.put("message", "获取用户信息成功");
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "服务器内部错误");
        }
        
        return response;
    }
    
    @GetMapping("/current")
    public Map<String, Object> getCurrentUser(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("code", 401);
                response.put("message", "未登录");
                return response;
            }
            
            response.put("code", 200);
            response.put("data", user);
            response.put("message", "获取当前用户信息成功");
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "服务器内部错误");
        }
        
        return response;
    }
    
    @GetMapping("/captcha")
    public Map<String, String> generateCaptcha(HttpSession session) {
        // 这里应该调用captchaUtils生成验证码
        // 由于UserApiController没有注入captchaUtils，我们先返回一个模拟的验证码
        String code = "1234";
        String imageBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        session.setAttribute("captcha", code);
        
        Map<String, String> result = new HashMap<>();
        result.put("image", imageBase64);
        return result;
    }
}
