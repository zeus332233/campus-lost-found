package com.example.demo.config;

import javax.annotation.PostConstruct;
import javax.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value; // 添加Spring的@Value导入
import org.springframework.util.unit.DataSize;

import java.io.File;

@Configuration
public class FileUploadConfig {
    @Value("${upload.base-path}") // 现在将正确引用Spring的@Value
    private String basePath;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 设置单个文件大小
        factory.setMaxFileSize(DataSize.ofMegabytes(5));
        // 设置总上传数据大小
        factory.setMaxRequestSize(DataSize.ofMegabytes(15));
        return factory.createMultipartConfig();
    }

    @PostConstruct
    public void init() {
        // 创建上传目录
        File uploadDir = new File(basePath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }
}