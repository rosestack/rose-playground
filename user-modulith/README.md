# User CRUD 示例项目

基于 Spring Modulith 构建的模块化单体架构示例项目，展示如何构建高质量、可维护的后端服务。

## 🏗️ 项目架构

### Spring Modulith 设计原则

- **模块边界清晰**：每个模块都有明确的职责边界
- **最小公开原则**：只暴露必要的接口，隐藏实现细节
- **包结构规范**：
  - 模块根包（如 `user`）是公开的
  - 子包（如 `internal`、`events`）是私有的
  - 数据库相关类放在 `internal` 包中
- **依赖关系合法**：模块间只能依赖公开 API，不能访问内部实现

### 项目结构

```
user-modulith/
├── src/main/java/io/github/rose/
│   ├── UserCrudApplication.java          # 主启动类
│   ├── user/                             # 用户模块（公开包）
│   │   ├── UserController.java           # 公开类 - 对外API
│   │   ├── UserService.java             # 公开类 - 业务接口
│   │   ├── UserCreateRequest.java       # 公开类 - 请求DTO
│   │   ├── UserUpdateRequest.java       # 公开类 - 请求DTO
│   │   ├── UserResponse.java            # 公开类 - 响应DTO
│   │   ├── UserPageRequest.java         # 公开类 - 分页请求
│   │   ├── UserStatus.java              # 公开类 - 用户状态枚举
│   │   ├── UserException.java           # 公开类 - 用户异常
│   │   ├── events/                      # 私有子包 - 模块内部事件
│   │   │   ├── UserCreatedEvent.java
│   │   │   └── UserUpdatedEvent.java
│   │   ├── internal/                    # 私有子包 - 隐藏实现细节
│   │   │   ├── UserServiceImpl.java     # 内部实现
│   │   │   ├── UserMapper.java          # 内部 - 数据库访问
│   │   │   ├── UserEntity.java          # 内部 - 数据库实体
│   │   │   └── UserConverter.java       # 内部 - 对象转换
│   └── shared/                          # 独立共享模块（公开包）
│       ├── ApiResponse.java             # 通用响应类
│       ├── PageResponse.java            # 通用分页响应类
│       ├── api/entity/
│       │   └── BaseEntity.java          # 基础实体类
│       └── config/
│           └── MyBatisPlusConfig.java   # MyBatis Plus 配置
├── docker/                              # Docker 配置文件
│   ├── mysql/                          # MySQL 配置
│   │   ├── conf.d/my.cnf               # MySQL 配置文件
│   │   └── init/                       # 数据库初始化脚本
│   │       ├── 01-schema.sql           # 数据库表结构
│   │       └── 02-data.sql             # 测试数据
│   ├── redis/                          # Redis 配置
│   │   └── redis.conf                  # Redis 配置文件
│   └── README.md                       # Docker 使用说明
├── docker-compose.yml                  # 完整环境（MySQL + Redis + 管理工具）
├── docker-compose-mysql.yml            # 仅 MySQL 环境
├── start-services.sh                   # 服务启动脚本
├── .env                                # 环境变量配置
└── src/main/resources/
    ├── application.yml                   # 应用配置
    └── schema.sql                       # 数据库脚本
```

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.9+
- Docker & Docker Compose（推荐）
- MySQL 8.0+（可选，可使用 Docker）

### 方式一：使用 Docker（推荐）

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd user-modulith
   ```

2. **启动数据库服务**
   ```bash
   # 启动所有服务（MySQL + Redis + 管理工具）
   ./start-services.sh

   # 或者只启动 MySQL
   ./start-services.sh mysql

   # 手动启动
   docker-compose up -d
   ```

3. **验证服务状态**
   ```bash
   docker-compose ps
   ```

4. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

5. **访问服务**
   - 应用地址：http://localhost:8080
   - 健康检查：http://localhost:8080/actuator/health
   - phpMyAdmin：http://localhost:8081
   - Redis Commander：http://localhost:8082

### 方式二：本地环境

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd user-modulith
   ```

2. **创建数据库**
   ```bash
   mysql -u root -p < src/main/resources/schema.sql
   ```

3. **修改配置**
   编辑 `src/main/resources/application.yml`，修改数据库连接信息

4. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

5. **访问接口**
   - 应用地址：http://localhost:8080
   - 健康检查：http://localhost:8080/actuator/health

## 📋 API 接口

### 用户管理

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/users` | 创建用户 |
| GET | `/api/users/{id}` | 获取用户 |
| PUT | `/api/users/{id}` | 更新用户 |
| DELETE | `/api/users/{id}` | 删除用户 |
| GET | `/api/users` | 分页查询用户 |
| GET | `/api/users/list` | 获取所有用户 |

### 请求示例

**创建用户**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "phone": "13800138000",
    "password": "password123"
  }'
```

**查询用户**
```bash
curl http://localhost:8080/api/users/1
```

**分页查询**
```bash
curl "http://localhost:8080/api/users?current=1&size=10&username=test"
```

**获取所有用户**
```bash
curl http://localhost:8080/api/users/list
```

## 🛠️ 技术栈

- **Spring Boot 3.5.1**：主框架
- **Spring Modulith 1.4.1**：模块化架构
- **MyBatis Plus 3.5.12**：ORM 框架
- **MySQL 8.0**：数据库
- **Redis 7.x**：缓存服务
- **Docker & Docker Compose**：容器化部署
- **Lombok**：代码生成
- **TestContainers 1.19.1**：集成测试

## 🐳 Docker 环境

### 服务组件

| 服务 | 端口 | 说明 | 管理工具 |
|------|------|------|----------|
| MySQL | 3306 | 主数据库 | phpMyAdmin (8081) |
| Redis | 6379 | 缓存服务 | Redis Commander (8082) |

### 数据库连接信息

**MySQL：**
- 主机：localhost
- 端口：3306
- 数据库：user_modulith
- 用户名：root
- 密码：password

**Redis：**
- 主机：localhost
- 端口：6379
- 无密码

### Docker 常用命令

```bash
# 启动服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f mysql
docker-compose logs -f redis

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 清理数据（谨慎使用）
docker-compose down -v

# 进入容器
docker-compose exec mysql bash
docker-compose exec redis sh
```

## 📚 设计模式

### 1. 模块化设计

- **用户模块**：用户管理相关功能
  - 公开 API：控制器、服务接口、DTO
  - 内部实现：服务实现、数据访问、实体类
  - 事件机制：用户创建、更新事件
- **共享模块**：公共组件和配置
  - 通用响应类
  - 基础实体类
  - 配置类

### 2. 分层架构

- **控制器层**：处理 HTTP 请求，参数校验，响应格式化
- **服务层**：业务逻辑处理，事务管理
- **数据访问层**：数据库操作，ORM 映射

### 3. 事件驱动

- 使用 Spring Modulith 事件机制
- 模块间通过事件进行通信
- 保持模块间的松耦合

### 4. 通用组件设计

- **ApiResponse<T>**：统一 API 响应格式
- **PageResponse<T>**：通用分页响应，支持泛型
- **BaseEntity**：基础实体类，提供通用字段

## 🔧 开发指南

### 添加新模块

1. 在 `src/main/java/io/github/rose/` 下创建模块包
2. 在模块包下创建公开接口和类
3. 在 `internal` 子包中创建实现类
4. 在 `event` 子包中创建事件类

### 数据库实体

- 继承 `BaseEntity` 获取通用字段
- 使用 MyBatis Plus 注解进行映射
- 放在 `internal` 包中隐藏实现细节

### 异常处理

- 使用自定义异常类 `UserException`
- 提供静态工厂方法创建特定异常
- 异常信息国际化支持

### 分页查询

- 使用 `PageResponse<T>` 通用分页响应
- 支持泛型，可复用
- 自动计算分页信息

## 🧪 测试

### 模块依赖验证

```bash
# 验证 Spring Modulith 模块依赖
mvn test -Dtest=ModulithDependencyTest#verifyModuleDependencies
```

### 运行测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

## 📊 模块依赖验证

项目使用 Spring Modulith 进行模块依赖验证，确保：

- ✅ 模块边界清晰
- ✅ 公开 API 明确
- ✅ 依赖关系合法
- ✅ 内部实现隐藏

### 验证结果

```
=== 模块依赖验证通过 ===
模块数量: 1
模块: User
  包: io.github.rose.user
```

## 🚀 部署

### Docker 部署

```bash
# 构建镜像
docker build -t user-modulith .

# 运行容器
docker run -p 8080:8080 user-modulith
```

### 生产环境配置

```yaml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:mysql://prod-db:3306/user_crud
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📞 联系方式

- 作者：Chen Soul
- 邮箱：ichensoul@gmail.com 