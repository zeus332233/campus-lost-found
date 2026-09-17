package com.example.demo.service.impl;

import com.example.demo.dto.UserProfileDTO;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public boolean updateUserProfile(Long userId, UserProfileDTO profileDTO) {
        if (profileDTO.getPassword() != null && profileDTO.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }

        User user = new User();
        user.setId(userId);
        user.setPhone(profileDTO.getPhone());
        user.setEmail(profileDTO.getEmail());

        // 密码加密存储
        if (profileDTO.getPassword() != null && !profileDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(profileDTO.getPassword()));
        }

        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean updateAvatar(Long userId, String avatarUrl) {
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatarUrl);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public User register(User user) {
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 密码加密存储
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        long currentTime = System.currentTimeMillis();
        user.setCreatedAt(currentTime);
        user.setUpdatedAt(currentTime);
        userMapper.insert(user);
        log.info("新用户注册成功: {}", user.getUsername());
        return user;
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        // 兼容旧密码（明文）和新密码（BCrypt加密）
        boolean passwordMatches;
        if (user.getPassword() != null && user.getPassword().startsWith("$2a$")) {
            // BCrypt加密密码
            passwordMatches = passwordEncoder.matches(password, user.getPassword());
        } else {
            // 旧明文密码，直接比对
            passwordMatches = password.equals(user.getPassword());
            if (passwordMatches) {
                // 自动升级为BCrypt加密
                user.setPassword(passwordEncoder.encode(password));
                userMapper.updateById(user);
                log.info("用户 {} 密码自动升级为BCrypt加密", username);
            }
        }
        if (!passwordMatches) {
            throw new RuntimeException("用户名或密码错误");
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }
}
