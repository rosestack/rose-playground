# User CRUD Test

用户CRUD操作测试项目，基于Spring Boot 3.5.1 + MyBatis Plus 3.5.12 + MySQL 8.0 + Redis 7.x

## 技术栈

- **Spring Boot 3.5.1**：主框架
- **MyBatis Plus 3.5.12**：ORM框架
- **MySQL 8.0**：数据库
- **Redis 7.x**：缓存
- **Java 17**：编程语言
- **Maven 3.9+**：构建工具
- **Lombok**：代码简化
- **MapStruct**：对象映射
- **Jakarta Validation**：参数校验

## 项目结构

```
src/
├── main/
│   ├── java/io/github/rose/user/
│   │   ├── controller/          # 控制器层
│   │   ├── service/             # 服务层
│   │   ├── mapper/              # 数据访问层
│   │   ├── entity/              # 实体类
│   │   ├── dto/                 # 数据传输对象
│   │   ├── converter/           # 对象转换器
│   │   ├── config/              # 配置类
│   │   ├── exception/           # 异常处理
│   │   └── UserCrudApplication.java
│   └── resources/
│       ├── application.yml      # 主配置文件
│       ├── application-dev.yml  # 开发环境配置
│       └── schema.sql           # 数据库初始化脚本
└── test/
    └── java/io/github/rose/user/
        └── UserCrudApplicationTests.java
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Redis 7.x+

### 2. 数据库准备

```sql
-- 创建数据库
CREATE DATABASE user_crud_test DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 执行初始化脚本
source schema.sql;
```

### 3. 配置修改

修改 `src/main/resources/application-dev.yml` 中的数据库和Redis连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_crud_test?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
```

### 4. 启动应用

```bash
# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

### 5. 验证启动

访问以下端点验证应用是否正常启动：

- 健康检查：http://localhost:8080/actuator/health
- 应用信息：http://localhost:8080/actuator/info

## API接口

### 用户管理接口

#### 1. 创建用户
```http
POST /api/users
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "phone": "13800138000"
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
  "email": "newemail@example.com",
  "phone": "13800138001"
}
```

#### 4. 删除用户
```http
DELETE /api/users/{id}
```

#### 5. 分页查询用户
```http
GET /api/users?current=1&size=10&username=test&status=1
```

## 开发规范

本项目严格遵循以下开发规范：

1. **Java开发规范**：命名规范、注释规范、异常处理规范
2. **Spring Boot开发规范**：分层架构、依赖注入、配置管理
3. **MyBatis Plus开发规范**：实体设计、查询优化、性能配置
4. **Maven开发规范**：依赖管理、构建配置、测试策略

## 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

## 部署

```bash
# 打包应用
mvn clean package

# 运行jar包
java -jar target/user-crud-test-1.0.0-SNAPSHOT.jar
```

## 监控

应用集成了Spring Boot Actuator，提供以下监控端点：

- `/actuator/health`：健康检查
- `/actuator/info`：应用信息
- `/actuator/metrics`：指标监控
- `/actuator/prometheus`：Prometheus指标

## 许可证

MIT License