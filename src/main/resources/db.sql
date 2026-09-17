CREATE DATABASE IF NOT EXISTS campus_lost_found;
USE campus_lost_found;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar VARCHAR(255) COMMENT '头像图片 URL',
    created_at BIGINT NOT NULL DEFAULT 0,
    updated_at BIGINT NOT NULL DEFAULT 0
);

-- 失物招领表(找到的物品)
CREATE TABLE IF NOT EXISTS found_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    location VARCHAR(100),
    found_time BIGINT,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    status ENUM('SEARCHING', 'FOUND',) DEFAULT 'SEARCHING',
    created_at BIGINT NOT NULL DEFAULT 0,
    updated_at BIGINT NOT NULL DEFAULT 0,
    image_urls VARCHAR(512) COMMENT '图片URL，多图用逗号分隔',
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 寻物启事表(丢失的物品)
CREATE TABLE IF NOT EXISTS lost_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    location VARCHAR(100),
    lost_time BIGINT,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    status ENUM('SEARCHING', 'FOUND') DEFAULT 'SEARCHING',
    created_at BIGINT NOT NULL DEFAULT 0,
    updated_at BIGINT NOT NULL DEFAULT 0,
    image_urls VARCHAR(512) COMMENT '图片URL，多图用逗号分隔',
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 认领记录表
CREATE TABLE claim (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_id BIGINT NOT NULL COMMENT '物品ID',
    item_type ENUM('FOUND', 'LOST') NOT NULL COMMENT '物品类型',
    claimant_id BIGINT NOT NULL COMMENT '认领人ID',
    verification_answer VARCHAR(255) COMMENT '验证问题答案',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '认领状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    reject_reason TEXT COMMENT '拒绝原因',
    FOREIGN KEY (claimant_id) REFERENCES user(id),
    INDEX idx_item (item_id, item_type)
) COMMENT '认领记录表';
-- 创建通知表单
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    related_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    create_time BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id)
);


