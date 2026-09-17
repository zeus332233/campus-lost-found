# 校园失物招领系统

> 基于 Spring Boot 的校园失物招领平台，提供失物发布、寻物启事、智能匹配、认领审批等核心功能。

---

## 项目简介

校园失物招领系统旨在解决校园内物品丢失后信息不对称的问题。用户可以发布拾到的物品或丢失的物品信息，系统通过**智能匹配引擎**自动关联可能匹配的记录，并通过站内通知提醒双方，提升物品找回效率。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.7.10 |
| 持久层 | MyBatis | 2.2.2 |
| 数据库 | MySQL | 8.x |
| 模板引擎 | Thymeleaf | - |
| 密码加密 | BCrypt | - |
| 构建工具 | Maven | - |
| 编程语言 | Java | 11 |

## 功能模块

- **用户模块**：注册/登录（图形验证码 + BCrypt 加密）、个人中心、头像上传
- **失物招领**：发布/编辑/删除、多图上传、分页搜索
- **寻物启事**：发布/编辑/删除、多图上传、分页搜索
- **智能匹配引擎**：分类(30分) + 地点相似度(30分) + 时间接近度(40分) + 标题关键词(20分)
- **认领审批**：提交申请/审批/拒绝，完整状态机流转
- **通知系统**：站内通知、标记已读、未读计数
- **数据仪表盘**：统计展示最新发布和整体数据

## 数据库

共 5 张表：`user` / `found_item` / `lost_item` / `claim` / `notification`

建表 SQL：`src/main/resources/db.sql`

## 快速开始

1. 克隆仓库：`git clone https://github.com/zeus332233/campus-lost-found.git`
2. 创建数据库：`CREATE DATABASE campus_lost_found;`
3. 执行建表脚本：`mysql -u root -p campus_lost_found < src/main/resources/db.sql`
4. 复制 `application.yml.example` 为 `application.yml`，修改数据库密码
5. 运行：`mvn spring-boot:run`
6. 访问：`http://localhost:8080`
