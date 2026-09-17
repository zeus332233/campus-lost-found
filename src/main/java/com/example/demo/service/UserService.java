package com.example.demo.service;

import com.example.demo.dto.UserProfileDTO;
import com.example.demo.model.User;

public interface UserService {
    // 个人信息相关方法
    User getUserById(Long id);
    boolean updateUserProfile(Long userId, UserProfileDTO dto);
    boolean updateAvatar(Long userId, String avatarUrl);
    
    // 认证相关方法
    User register(User user);
    User login(String username, String password);
    User getUserByUsername(String username);
}