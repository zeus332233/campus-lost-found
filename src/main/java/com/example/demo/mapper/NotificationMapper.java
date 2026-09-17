package com.example.demo.mapper;

import com.example.demo.model.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface NotificationMapper {
    void insert(Notification notification);
    List<Notification> selectByUserId(Long userId);
    // 将void改为int
    int updateReadStatus(Map<String, Object> params);
    
    // 同时修改其他更新方法的返回类型以保持一致性
    int updateAllReadStatus(Map<String, Object> params);
    int updateReadStatusByTypeAndRelatedId(Map<String, Object> params);
    int countUnreadByUserId(Long userId);
}