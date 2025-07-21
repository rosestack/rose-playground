# DDD Demo 项目

基于领域驱动设计（DDD）的分层架构用户管理系统示例项目。

## 项目概述

本项目展示了如何使用DDD分层架构设计用户管理系统，包含完整的用户CRUD操作。

### 技术栈

- **Java 17+**
- **Spring Boot 3.5+**
- **MyBatis Plus 3.x**
- **MySQL 8.0**
- **Redis 7.x**
- **Maven**
- **Docker & Docker Compose**

### 架构设计

项目采用DDD分层架构：

```
ddd-demo/
├── interfaces/          # 接口层（控制器、DTO）
├── application/         # 应用层（应用服务、命令、查询）
├── domain/             # 领域层（实体、值对象、领域服务）
├── infrastructure/     # 基础设施层（持久化、外部服务）
└── shared/             # 共享层（通用组件）
```

## 快速开始

### 1. 环境要求

- Java 17+
- Maven 3.9+
- Docker & Docker Compose

### 2. 启动依赖服务

```bash
# 启动MySQL和Redis
docker-compose up -d
```

### 3. 运行应用

```bash
# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

### 4. 验证服务

访问以下地址验证服务是否正常：

- 应用健康检查：`http://localhost:8080/actuator/health`
- Swagger API文档：`http://localhost:8080/swagger-ui.html`

## API接口

### 用户管理接口

#### 1. 创建用户

```http
POST /api/users
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "phone": "13800138000",
  "password": "123456",
  "realName": "测试用户"
}
```

#### 2. 查询用户

```http
GET /api/users/{id}
```

#### 3. 更新用户

```http
PUT /api/users/{id}
Content-Type: application/json

{
  "realName": "新姓名",
  "nickname": "昵称",
  "avatar": "头像URL",
  "gender": 1,
  "birthday": "1990-01-01T00:00:00",
  "address": {
    "country": "中国",
    "province": "北京",
    "city": "北京市",
    "district": "朝阳区",
    "detailAddress": "详细地址",
    "postalCode": "100000"
  }
}
```

#### 4. 更新用户状态

```http
PUT /api/users/{id}/status
Content-Type: application/json

{
  "status": 0
}
```

#### 5. 查询用户列表

```http
GET /api/users?page=1&size=10&status=1&keyword=test
```

#### 6. 删除用户

```http
DELETE /api/users/{id}
```

#### 7. 统计用户数量

```http
GET /api/users/count?status=1
```

## 项目结构

```
src/main/java/com/example/ddddemo/
├── user/                           # 用户聚合
│   ├── domain/                     # 领域层
│   │   ├── entity/                 # 领域实体
│   │   │   └── User.java          # 用户聚合根
│   │   ├── valueobject/            # 值对象
│   │   │   └── Address.java       # 地址值对象
│   │   ├── event/                  # 领域事件
│   │   │   ├── UserCreatedEvent.java
│   │   │   └── UserUpdatedEvent.java
│   │   └── repository/             # 仓储接口
│   │       └── UserRepository.java
│   ├── application/                # 应用层
│   │   ├── service/                # 应用服务
│   │   │   └── UserApplicationService.java
│   │   ├── command/                # 命令对象
│   │   │   ├── CreateUserCommand.java
│   │   │   ├── UpdateUserCommand.java
│   │   │   └── UpdateUserStatusCommand.java
│   │   ├── query/                  # 查询对象
│   │   │   └── UserQuery.java
│   │   └── dto/                    # 数据传输对象
│   │       ├── UserDTO.java
│   │       └── AddressDTO.java
│   ├── infrastructure/             # 基础设施层
│   │   ├── persistence/            # 持久化
│   │   │   ├── entity/             # 数据对象
│   │   │   │   └── UserDO.java
│   │   │   ├── mapper/             # MyBatis Mapper
│   │   │   │   └── UserMapper.java
│   │   │   └── repository/         # 仓储实现
│   │   │       └── UserRepositoryImpl.java
│   │   └── converter/              # 转换器
│   │       └── UserConverter.java
│   └── interfaces/                 # 接口层
│       └── controller/             # REST控制器
│           └── UserController.java
└── shared/                         # 共享层
    ├── domain/                     # 共享领域
    │   └── entity/                 # 基础实体
    │       └── AggregateRoot.java
    ├── application/                # 共享应用
    │   └── dto/                    # 共享DTO
    │       └── ApiResponse.java
    └── interfaces/                 # 共享接口
        └── exception/              # 异常处理
            └── GlobalExceptionHandler.java
```

## 数据库设计

### 用户表 (user)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 用户ID（主键） |
| username | VARCHAR(50) | 用户名（唯一） |
| email | VARCHAR(100) | 邮箱（唯一） |
| phone | VARCHAR(20) | 手机号（唯一） |
| password | VARCHAR(100) | 密码 |
| real_name | VARCHAR(50) | 真实姓名 |
| nickname | VARCHAR(50) | 昵称 |
| avatar | VARCHAR(255) | 头像URL |
| gender | TINYINT | 性别（0-未知，1-男，2-女） |
| birthday | DATETIME | 生日 |
| country | VARCHAR(50) | 国家 |
| province | VARCHAR(50) | 省份 |
| city | VARCHAR(50) | 城市 |
| district | VARCHAR(50) | 区县 |
| detail_address | VARCHAR(255) | 详细地址 |
| postal_code | VARCHAR(20) | 邮政编码 |
| status | TINYINT | 状态（0-禁用，1-正常） |
| last_login_time | DATETIME | 最后登录时间 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 是否删除（0-未删除，1-已删除） |

## 开发指南

### 添加新功能

1. **领域层**：定义实体、值对象、领域事件
2. **应用层**：实现应用服务、命令、查询
3. **基础设施层**：实现持久化、外部服务集成
4. **接口层**：实现REST接口

### 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

## 部署

### Docker部署

```bash
# 构建镜像
docker build -t ddd-demo .

# 运行容器
docker run -p 8080:8080 ddd-demo
```

## 许可证

MIT License