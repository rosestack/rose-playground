# IoT物联网平台

## 项目概述

IoT物联网平台是一个综合性的云端系统，基于Spring Boot单体架构设计，支持设备接入、数据管理、规则引擎等核心功能。平台采用Maven多模块架构，便于后续微服务拆分。

## 技术栈

- **Java 17+**: 使用最新的Java特性
- **Spring Boot 3.2.0**: 应用框架
- **MyBatis Plus 3.5.4.1**: 数据访问层ORM框架
- **MySQL 8.0**: 主数据库
- **Redis 7.0**: 缓存数据库
- **InfluxDB 2.7**: 时序数据库
- **RabbitMQ 3.12**: 消息队列
- **EMQ X 5.0**: MQTT消息代理
- **Spring Security 6.2**: 安全框架
- **OpenAPI 3.0**: API文档

## 项目结构

```
iot/
├── pom.xml                                   # 父模块POM
├── iot-common/                               # 公共组件模块
├── iot-security/                             # 安全组件模块
├── iot-utils/                                # 工具类模块
├── iot-device-service/                       # 设备管理服务模块
├── iot-message-service/                      # 消息通信服务模块
├── iot-data-service/                         # 数据服务模块
├── iot-monitor-service/                      # 监控运维服务模块
├── iot-auth-service/                         # 安全认证服务模块
├── iot-system-service/                       # 系统管理服务模块
├── iot-transport-mqtt/                       # MQTT协议模块
├── iot-transport-http/                       # HTTP协议模块
├── iot-transport-coap/                       # CoAP协议模块
├── iot-transport-websocket/                  # WebSocket协议模块
└── iot-starter/                              # 启动模块
```

## 模块说明

### 共享模块
- **iot-common**: 公共组件、工具类、常量定义
- **iot-security**: 安全相关组件、加密工具、认证框架
- **iot-utils**: 通用工具类、异常处理、日志工具

### 服务模块
- **iot-device-service**: 设备接入和管理服务实现
- **iot-message-service**: 消息通信和路由服务实现
- **iot-data-service**: 数据存储和分析服务实现
- **iot-monitor-service**: 监控告警和运维服务实现
- **iot-auth-service**: 安全认证和权限服务实现
- **iot-system-service**: 系统管理服务实现

### 协议模块
- **iot-transport-mqtt**: MQTT协议传输实现
- **iot-transport-http**: HTTP协议传输实现
- **iot-transport-coap**: CoAP协议传输实现
- **iot-transport-websocket**: WebSocket协议传输实现

### 启动模块
- **iot-starter**: 应用启动入口、配置加载、模块集成

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Redis 7.0+
- RabbitMQ 3.12+

### 安装步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd iot
```

2. **初始化数据库**
```bash
# 创建数据库
mysql -u root -p < iot-starter/src/main/resources/schema.sql
```

3. **配置环境变量**
```bash
# 开发环境
export DB_USERNAME=root
export DB_PASSWORD=password
export REDIS_HOST=localhost
export REDIS_PORT=6379
export RABBITMQ_HOST=localhost
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=guest
```

4. **编译项目**
```bash
mvn clean compile
```

5. **运行项目**
```bash
# 开发环境
mvn spring-boot:run -pl iot-starter -Dspring-boot.run.profiles=dev

# 或者直接运行jar包
java -jar iot-starter/target/iot-starter-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

### 访问地址

- **应用地址**: http://localhost:8080/iot
- **API文档**: http://localhost:8080/iot/swagger-ui.html
- **健康检查**: http://localhost:8080/iot/actuator/health
- **监控指标**: http://localhost:8080/iot/actuator/metrics

## 功能特性

### 核心功能
- **设备管理**: 设备注册、认证、状态监控、生命周期管理
- **数据管理**: 数据采集、存储、查询、分析
- **消息通信**: 多协议支持、消息路由、订阅管理
- **规则引擎**: 数据转发、告警规则、场景联动
- **安全认证**: 用户管理、权限控制、设备认证
- **监控运维**: 系统监控、告警管理、日志管理

### 协议支持
- **MQTT**: 支持QoS级别、消息持久化
- **HTTP/HTTPS**: RESTful API、设备接入
- **CoAP**: 轻量级通信协议
- **WebSocket**: 实时双向通信

### 数据存储
- **MySQL**: 业务数据存储
- **Redis**: 缓存和会话存储
- **InfluxDB**: 时序数据存储

## 开发规范

### 代码规范
- 严格遵循Java开发规范和Spring Boot最佳实践
- 所有关键代码必须包含中文注释
- 完善的异常处理和错误响应机制
- 核心业务逻辑必须包含单元测试

### 命名规范
- 类名：大驼峰命名法（PascalCase）
- 方法名：小驼峰命名法（camelCase）
- 常量：全大写+下划线（UPPER_SNAKE_CASE）
- 数据库表名：小写+下划线（snake_case）

### 架构原则
- 模块化设计，模块间松耦合
- 分层架构，清晰分离关注点
- 单一职责，每个模块只负责特定功能
- 开闭原则，对扩展开放，对修改封闭

## 部署说明

### Docker部署
```bash
# 构建镜像
docker build -t iot-platform:1.0.0 .

# 运行容器
docker run -d -p 8080:8080 --name iot-platform iot-platform:1.0.0
```

### 生产环境配置
- 修改 `application-prod.yml` 配置文件
- 设置环境变量
- 配置数据库连接池参数
- 启用安全配置

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

- 项目维护者: 技术团队
- 邮箱: tech@iot.com
- 项目地址: [GitHub Repository](https://github.com/your-org/iot-platform)

## 更新日志

### v1.0.0-SNAPSHOT (2024-01-01)
- 初始版本发布
- 基础功能实现
- 多协议支持
- 安全认证框架