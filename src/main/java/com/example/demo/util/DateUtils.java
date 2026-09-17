package com.example.demo.util;

import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DateUtils {
    // 格式化时间戳（Long类型）为字符串
    public String formatTimestamp(Long timestamp) {
        if (timestamp == null) return null;
        
        // 检查时间戳是否为秒级
        if (timestamp < 1000000000000L) {
            // 转换为毫秒级
            timestamp = timestamp * 1000;
        }
        
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
    }

    // 其他方法可以根据需要保留或删除
    public Long dateToTimestamp(Date date) {
        if (date == null) return null;
        return date.getTime();  // 
    }

    public Long currentTimestamp() {
        return System.currentTimeMillis();  // 
    }
}