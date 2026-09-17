package com.example.demo.mapper;

import com.example.demo.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    // 查询方法
    User selectById(Long id);
    User selectByUsername(String username);
    
    // 新增方法
    int insert(User user);
    
    // 更新方法
    int updateById(User user);
}
