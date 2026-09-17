package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public Converter<String, Long> stringToTimestampConverter() {
        return new Converter<String, Long>() {
            @Override
            public Long convert(String source) {
                try {
                    // 解析datetime-local格式字符串为时间戳
                    LocalDateTime dateTime = LocalDateTime.parse(
                        source, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    return dateTime.atZone(java.time.ZoneId.systemDefault())
                           .toInstant().toEpochMilli();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid datetime format: " + source);
                }
            }
        };
    }
}