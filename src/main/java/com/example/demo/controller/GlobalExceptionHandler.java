package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e, Model model) {
        log.warn("参数校验失败: {}", e.getMessage());
        model.addAttribute("errorMsg", e.getMessage());
        return "error";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e, Model model) {
        log.error("业务异常: {}", e.getMessage(), e);
        model.addAttribute("errorMsg", e.getMessage());
        return "error";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSize(MaxUploadSizeExceededException e, Model model) {
        log.warn("文件上传超出大小限制");
        model.addAttribute("errorMsg", "上传文件大小超出限制，单个文件最大10MB");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        log.error("系统异常: {}", e.getMessage(), e);
        model.addAttribute("errorMsg", "系统错误: " + e.getMessage());
        return "error";
    }
}
