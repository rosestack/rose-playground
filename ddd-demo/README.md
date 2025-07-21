# DDD Demo 项目

## 项目简介

基于DDD（领域驱动设计）分层架构的用户管理系统Demo项目，展示DDD核心概念和最佳实践。

## 技术栈

- **Java 17+**：使用最新的Java特性
- **Spring Boot 3.5+**：主框架
- **MyBatis Plus 3.x**：数据访问层
- **MySQL 8.0**：主数据库
- **Redis 7.x**：缓存
- **Maven**：构建工具
- **Docker**：容器化部署

## 项目结构

```
ddd-demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/ddddemo/
│   │   │       ├── DddDemoApplication.java
│   │   │       ├── user/                           # 用户领域
│   │   │       │   ├── interfaces/                 # 接口层
│   │   │       │   ├── application/                # 应用层
│   │   │       │   ├── domain/                     # 领域层
│   │   │       │   └── infrastructure/             # 基础设施层
│   │   │       └── shared/                         # 共享层
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── mapper/
│   │       └── db/migration/
│   └── test/
└── docs/
```

## DDD分层架构

### 接口层 (Interfaces Layer)
- 处理HTTP请求和响应
- 数据格式转换（DTO ↔ Domain Object）
- 输入验证和参数校验
- 异常处理和错误响应

### 应用层 (Application Layer)
- 业务流程编排和协调
- 事务边界控制
- 应用事件发布和处理
- 缓存管理

### 领域层 (Domain Layer)
- 核心业务逻辑实现
- 业务规则和约束
- 领域模型定义
- 业务不变性保证

### 基础设施层 (Infrastructure Layer)
- 数据持久化实现
- 外部服务集成
- 技术框架配置
- 横切关注点实现

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Redis 7.x+

### 数据库配置
1. 创建数据库：
```sql
CREATE DATABASE ddd_demo_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改配置文件 `application-dev.yml` 中的数据库连接信息

### 启动应用
```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动应用
mvn spring-boot:run
```

### 访问地址
- 应用地址：http://localhost:8080
- API文档：http://localhost:8080/swagger-ui.html
- 健康检查：http://localhost:8080/actuator/health

## API接口

### 用户管理接口
- `POST /api/users` - 创建用户
- `GET /api/users/{id}` - 获取用户
- `PUT /api/users/{id}/email` - 更新邮箱
- `PUT /api/users/{id}/password` - 更新密码
- `PUT /api/users/{id}/activation` - 激活用户
- `DELETE /api/users/{id}/activation` - 停用用户
- `GET /api/users` - 查询用户列表
- `DELETE /api/users/{id}` - 删除用户

## 开发规范

### 代码规范
- 遵循Java编码规范
- 使用中文注释
- 遵循DDD分层架构原则
- 使用统一的命名规范

### 提交规范
- 使用约定式提交（Conventional Commits）
- 每次提交只包含一个独立的功能
- 提交前进行代码格式化

### 测试规范
- 单元测试覆盖率不低于80%
- 集成测试覆盖主要业务流程
- 端到端测试覆盖关键用户场景

## 部署

### Docker部署
```bash
# 构建镜像
docker build -t ddd-demo .

# 运行容器
docker run -d -p 8080:8080 --name ddd-demo ddd-demo
```

### Docker Compose部署
```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 停止所有服务
docker-compose down
```

## 项目特色

### DDD核心概念
- **聚合根（Aggregate Root）**：用户实体作为聚合根
- **值对象（Value Object）**：地址值对象
- **领域服务（Domain Service）**：用户领域服务
- **领域事件（Domain Event）**：用户相关事件
- **仓储模式（Repository Pattern）**：数据访问抽象

### 架构优势
- **清晰的职责分离**：每层专注于特定的关注点
- **依赖方向控制**：高层不依赖低层的具体实现
- **业务逻辑隔离**：核心业务逻辑与技术实现分离
- **可测试性**：每层都可以独立进行单元测试

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

- 项目维护者：DDD Demo Team
- 邮箱：demo@example.com
- 项目地址：https://github.com/example/ddd-demo