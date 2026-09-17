package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
public class UploadController {

    @Value("${upload.base-path}")
    private String uploadPath;

    /** 允许的图片类型 */
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    ));
    /** 文件大小限制：5MB */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @PostMapping("/api/upload/image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "文件不能为空");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            response.put("success", false);
            response.put("message", "文件大小不能超过5MB");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 文件类型校验
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            response.put("success", false);
            response.put("message", "无效的文件名");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            response.put("success", false);
            response.put("message", "不支持的文件类型，仅允许: " + String.join(", ", ALLOWED_EXTENSIONS));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    response.put("success", false);
                    response.put("message", "无法创建上传目录");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }

            String fileName = UUID.randomUUID().toString() + extension;
            File destFile = new File(uploadPath + File.separator + fileName);
            file.transferTo(destFile);

            log.info("图片上传成功: {}", fileName);
            response.put("success", true);
            response.put("data", Map.of("imageUrl", "/" + fileName));
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "文件上传失败，请重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
