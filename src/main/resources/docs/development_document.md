# 校园失物招领平台开发文档

## 1. 项目概述
校园失物招领平台是一个基于Spring Boot开发的Web应用，旨在帮助用户发布和管理失物招领与寻物启事信息，提供便捷的物品查找与归还渠道。系统采用前后端不分离架构，使用Thymeleaf模板引擎渲染页面，结合MyBatis进行数据访问，MySQL作为数据库存储。

## 2. 环境配置
### 2.1 开发环境
- JDK: 17
- Spring Boot: 3.0.2
- MySQL: 8.0+
- Maven: 3.8.1
- 开发工具: IntelliJ IDEA

### 2.2 项目配置
**application.yml** 核心配置：
```yaml
server:
  port: 8080
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/campus_lost_found?serverTimezone=Asia/Shanghai
    username: root
    password: 332233
  thymeleaf:
    cache: false
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.example.demo.model
```

## 3. 技术栈
- **后端**: Spring Boot, Spring MVC, MyBatis
- **前端**: Thymeleaf, Bootstrap 5, Bootstrap Icons
- **数据库**: MySQL
- **构建工具**: Maven
- **其他**: Lombok (简化Java代码)

## 4. 项目结构
### 4.1 目录结构
- src/main/java/com/example/demo
- ├── DemoApplication.java           # 应用入口
- ├── controller/                    # 控制器层
- ├── service/                       # 服务层
- ├── mapper/                        # 数据访问层
- ├── model/                         # 数据模型
- ├── dto/                           # 数据传输对象
- ├── util/                          # 工具类
- └── config/                        # 配置类

### 4.2 核心模块
- **用户模块**: 登录、注册、个人信息管理
- **失物招领模块**: 发布、查看、管理捡到的物品信息
- **寻物启事模块**: 发布、查看、管理丢失的物品信息
- **首页/仪表盘**: 展示最新信息，提供功能入口

## 5. 数据库设计
### 5.1 数据库表结构
**db.sql** 定义了3个核心表：

(1). **user表** - 存储用户信息
```sql
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    created_at BIGINT NOT NULL DEFAULT 0,
    updated_at BIGINT NOT NULL DEFAULT 0
);
```

(2). **found_item表** - 存储失物招领信息（捡到的物品）
```sql
CREATE TABLE found_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    location VARCHAR(100),
    found_time BIGINT,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    status ENUM('FOUND', 'CLAIMED', 'RETURNED') DEFAULT 'FOUND',
    FOREIGN KEY (user_id) REFERENCES user(id)
);
```

(3). **lost_item表** - 存储寻物启事信息（丢失的物品）
```sql
CREATE TABLE lost_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    location VARCHAR(100),
    lost_time BIGINT,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    status ENUM('SEARCHING', 'CLAIMED', 'RETURNED') DEFAULT 'SEARCHING',
    FOREIGN KEY (user_id) REFERENCES user(id)
);
```
(4). **claim表** - 认领记录表
```sql
CREATE TABLE claim (
id BIGINT PRIMARY KEY AUTO_INCREMENT,
item_id BIGINT NOT NULL COMMENT '物品ID',
item_type ENUM('FOUND', 'LOST') NOT NULL COMMENT '物品类型',
claimant_id BIGINT NOT NULL COMMENT '认领人ID',
verification_answer VARCHAR(255) COMMENT '验证问题答案',
status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '认领状态',
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
FOREIGN KEY (claimant_id) REFERENCES user(id),
INDEX idx_item (item_id, item_type)
) COMMENT '认领记录表';
```
### 5.2 实体属性图
#### 5.2.1 User 实体

| 属性名 | 数据类型 | 说明 |
|--------|----------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(100) | 密码 |
| phone | VARCHAR(20) | 联系电话 |
| email | VARCHAR(100) | 电子邮箱 |
| created_at | BIGINT | 创建时间戳 |
| updated_at | BIGINT | 更新时间戳 |

#### 5.2.2 FoundItem 实体
| 属性名 | 数据类型 | 说明 |
|--------|----------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 外键，关联User表 |
| title | VARCHAR(100) | 物品标题 |
| description | TEXT | 物品描述 |
| category | VARCHAR(50) | 物品类别 |
| location | VARCHAR(100) | 发现地点 |
| found_time | BIGINT | 发现时间戳 |
| contact_phone | VARCHAR(20) | 联系电话 |
| contact_email | VARCHAR(100) | 联系邮箱 |
| status | ENUM | 状态(FOUND, CLAIMED, RETURNED) |

#### 5.2.3 LostItem 实体
| 属性名 | 数据类型 | 说明 |
|--------|----------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 外键，关联User表 |
| title | VARCHAR(100) | 物品标题 |
| description | TEXT | 物品描述 |
| category | VARCHAR(50) | 物品类别 |
| location | VARCHAR(100) | 丢失地点 |
| lost_time | BIGINT | 丢失时间戳 |
| contact_phone | VARCHAR(20) | 联系电话 |
| contact_email | VARCHAR(100) | 联系邮箱 |
| status | ENUM | 状态(SEARCHING, CLAIMED, RETURNED) |

### 5.3 关系说明
(1).**User 和 FoundItem**: 一对多关系 (一个用户可以发布多个失物招领)
(2).**User 和 LostItem**: 一对多关系 (一个用户可以发布多个寻物启事)
(3).**FoundItem 和 LostItem**: 之间没有直接关联关系


## 6. 核心功能实现
### 6.1 用户模块
**UserController** 提供用户登录、注册和个人信息管理功能：
- POST /user/login - 用户登录
- POST /user/register - 用户注册
- GET /user/profile - 查看个人信息
- POST /user/profile - 更新个人信息
- POST /user/logout - 用户登出

### 6.2 失物招领模块
**FoundItemController** 处理失物招领相关请求：
- GET /found-items - 查看所有失物招领
- GET /found-items/create - 发布失物招领页面
- POST /found-items - 提交失物招领信息
- GET /found-items/{id} - 查看失物招领详情

### 6.3 寻物启事模块
**LostItemController** 处理寻物启事相关请求：
- GET /lost-items - 查看所有寻物启事
- GET /lost-items/create - 发布寻物启事页面
- POST /lost-items - 提交寻物启事信息
- GET /lost-items/{id} - 查看寻物启事详情

## 7. 前端页面结构
系统前端采用Thymeleaf模板，主要页面包括：
- login.html - 用户登录页
- register.html - 用户注册页
- dashboard.html - 系统首页/仪表盘
- foundItems/ - 失物招领相关页面
- lostItems/ - 寻物启事相关页面
- user/profile.html - 个人信息管理

## 8. 项目部署
### 8.1 数据库准备
1. 创建数据库：`CREATE DATABASE campus_lost_found;`
2. 执行SQL脚本：db.sql

### 8.2 应用部署
1. 修改application.yml中的数据库连接信息
2. 使用Maven打包：`mvn clean package`
3. 运行jar包：`java -jar demo-0.0.1-SNAPSHOT.jar`
4. 访问系统：http://localhost:8080

## 9. 开发注意事项
1. **安全提示**：当前系统密码未加密存储，生产环境需添加密码加密功能
2. **代码规范**：遵循Java编码规范，使用Lombok简化依赖注入
3. **异常处理**：全局异常由GlobalExceptionHandler统一处理
4. **模板缓存**：开发环境已禁用Thymeleaf缓存