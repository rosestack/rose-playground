# DDD 分层架构实践指南

## 1. 概述

### 1.1 DDD 核心理念

领域驱动设计（Domain-Driven Design，DDD）是一种软件开发方法论，强调将业务领域的复杂性作为软件设计的核心。通过清晰的分层架构来组织代码，确保业务逻辑的纯净性和系统的可维护性。

**核心概念：**
- **领域（Domain）**：业务问题空间，包含业务规则和逻辑
- **实体（Entity）**：具有唯一标识的领域对象
- **值对象（Value Object）**：没有唯一标识，通过属性值来区分的对象
- **聚合（Aggregate）**：一组相关实体和值对象的集合
- **聚合根（Aggregate Root）**：聚合的唯一入口，负责维护聚合的一致性
- **仓储（Repository）**：提供领域对象持久化的抽象接口
- **领域服务（Domain Service）**：不属于任何实体或值对象的业务逻辑
- **领域事件（Domain Event）**：领域中发生的重要业务事件

### 1.2 DDD 的核心价值

**1. 业务与技术的统一语言**
- 建立通用语言（Ubiquitous Language），消除业务人员与技术人员之间的沟通障碍
- 代码结构直接反映业务模型，提高代码的可读性和可维护性

**2. 复杂性管理**
- 通过限界上下文（Bounded Context）划分复杂业务领域
- 每个上下文内部保持高内聚，上下文之间保持低耦合

**3. 业务逻辑的纯净性**
- 将业务逻辑与技术实现分离
- 确保核心业务规则不受技术框架变化的影响

**4. 可扩展性和演进性**
- 支持业务需求的快速变化
- 为微服务架构演进提供良好的基础

### 1.3 适用场景

**适合使用 DDD 的场景：**
- 业务逻辑复杂，包含大量业务规则
- 需要长期维护和演进的系统
- 团队规模较大，需要明确的架构边界
- 业务领域专业性强，需要深度建模

**不适合使用 DDD 的场景：**
- 简单的 CRUD 应用
- 技术驱动的系统（如基础设施工具）
- 短期项目或原型系统
- 团队对 DDD 理解不足的情况

### 1.4 技术栈

**核心框架：**
- **Java 17+**：使用最新的Java特性，如记录类、模式匹配等
- **Spring Boot 3.5+**：主框架，提供依赖注入和自动配置
- **Spring Security 6.x**：安全框架，处理认证和授权
- **MyBatis Plus 3.x**：数据访问层ORM框架，提供强大的查询能力

**数据存储：**
- **MySQL 8.0**：主数据库，支持JSON字段和窗口函数
- **HikariCP**：高性能数据库连接池
- **Redis 7.x**：缓存和会话存储，支持多种数据结构
- **Elasticsearch**：搜索引擎，用于复杂查询和全文搜索

**构建和部署：**
- **Maven 3.9+**：构建工具和依赖管理
- **Docker**：容器化部署
- **Docker Compose**：本地开发环境编排

**监控和运维：**
- **Spring Boot Actuator**：应用监控和健康检查
- **Micrometer**：指标收集
- **Logback**：日志框架

**测试框架：**
- **JUnit 5**：单元测试框架，支持参数化测试和动态测试
- **Mockito 5.x**：模拟框架，用于创建测试替身
- **AssertJ**：流式断言库，提供更好的测试可读性
- **TestContainers**：集成测试容器，支持真实数据库和中间件测试
- **Spring Boot Test**：Spring Boot测试支持，包含各种测试切片
- **WireMock**：HTTP服务模拟，用于外部API测试
- **Testcontainers-JUnit5**：JUnit5与TestContainers集成

**开发工具：**
- **Lombok**：减少样板代码，自动生成getter/setter等
- **Spring Boot DevTools**：开发时热重载和自动重启
- **Swagger/OpenAPI 3**：API文档生成和在线测试
- **Spring Boot Configuration Processor**：配置元数据生成
- **MapStruct**：对象映射工具，编译时生成映射代码
- **Spotless**：代码格式化工具，保持代码风格一致
- **ArchUnit**：架构测试工具，验证架构规则

**工具库：**
- **Apache Commons Lang3**：通用工具类库
- **Jackson**：JSON序列化和反序列化
- **Validation API**：参数校验
- **Guava**：Google核心库，提供集合、缓存、并发等工具

**禁止使用的依赖：**
- **Hutool**：避免过度依赖工具库，保持代码的可控性
- **Fastjson**：存在安全风险，使用Jackson替代
- **Spring Data JPA**：与MyBatis Plus冲突，统一使用MyBatis Plus
- **Apache Commons BeanUtils**：性能较差，使用MapStruct替代
- **Dozer**：映射性能差，使用MapStruct替代
- **ModelMapper**：运行时映射，性能不如编译时的MapStruct
- **Gson**：功能不如Jackson完善，统一使用Jackson
- **Log4j 1.x**：已停止维护且存在安全漏洞，使用Logback
- **Commons Logging**：桥接复杂，直接使用SLF4J
- **Quartz**：过于重量级，简单任务使用Spring Task
- **Ehcache 2.x**：版本过旧，使用Caffeine或Redis
- **Jedis**：连接池管理复杂，使用Spring Data Redis
- **HttpClient 4.x**：版本过旧，使用Spring WebClient或OkHttp
- **Swagger 2.x**：已过时，使用OpenAPI 3
- **JUnit 4**：功能有限，使用JUnit 5
- **Hamcrest**：断言不够流畅，使用AssertJ
- **PowerMock**：与现代JVM不兼容，重构代码以支持Mockito

### 1.5 项目目标

**短期目标：**
- 建立清晰的分层架构
- 实现核心业务功能
- 建立完善的测试体系
- 提供详细的开发文档

**长期目标：**
- 支持业务的快速迭代
- 为微服务架构演进做准备
- 建立可复用的架构模式
- 培养团队的DDD实践能力

### 1.6 文档结构

本指南将按照以下结构展开：

1. **概述**（当前章节）- DDD基础概念和项目介绍
2. **架构设计** - 分层架构模型和设计原则
3. **基础设施配置** - 数据库、缓存、消息队列等配置
4. **领域建模** - 实体、值对象、聚合的设计
5. **应用服务** - 业务流程编排和事务管理
6. **数据访问** - 仓储模式和数据持久化
7. **异常处理** - 统一异常处理和错误响应
8. **安全控制** - 认证、授权和权限管理
9. **性能优化** - 缓存策略和查询优化
10. **测试策略** - 单元测试、集成测试和端到端测试
11. **部署运维** - 容器化部署和监控
12. **最佳实践** - 开发规范和代码质量
13. **扩展指南** - 微服务演进和架构治理

---

## 2. 架构设计

### 2.1 四层架构模型

DDD分层架构采用经典的四层模型，每层都有明确的职责和边界：

```
┌─────────────────────────────────────┐
│           接口层 (Interfaces)        │  ← 用户交互、API接口
├─────────────────────────────────────┤
│           应用层 (Application)       │  ← 业务流程编排、事务控制
├─────────────────────────────────────┤
│            领域层 (Domain)           │  ← 核心业务逻辑、业务规则
├─────────────────────────────────────┤
│         基础设施层 (Infrastructure)   │  ← 技术实现、外部依赖
└─────────────────────────────────────┘
```

**架构特点：**
- **清晰的职责分离**：每层专注于特定的关注点
- **依赖方向控制**：高层不依赖低层的具体实现
- **业务逻辑隔离**：核心业务逻辑与技术实现分离
- **可测试性**：每层都可以独立进行单元测试

**依赖规则：**
- **接口层** → 应用层：调用应用服务，不直接访问领域层或基础设施层
- **应用层** → 领域层：编排领域服务和聚合，通过接口访问基础设施层
- **领域层** → 无依赖：纯业务逻辑，不依赖任何外部框架
- **基础设施层** → 领域层：实现领域层定义的接口（依赖倒置）

**典型调用流程：**

```
用户请求 → Controller → Application Service → Domain Service/Entity → Repository Interface → Repository Implementation
```

**详细流程：**
1. **用户发起请求**：通过HTTP请求到达Controller
2. **接口层处理**：Controller验证参数，转换DTO
3. **应用层编排**：Application Service编排业务流程
4. **领域层执行**：Domain Service或Entity执行业务逻辑
5. **数据层访问**：通过Repository接口访问数据
6. **基础设施实现**：Repository Implementation执行具体数据操作
7. **结果返回**：逐层返回结果，最终响应用户

### 2.2 项目结构

#### 2.2.1 单体项目结构

基于DDD分层架构的标准项目结构，支持多领域场景下的"领域+分层"组织方式：

```
src/
├── main/
│   ├── java/
│   │   └── com/example/app/
│   │       ├── AppApplication.java                    # 应用启动类
│   │       │
│   │       ├── user/                                  # 用户领域
│   │       │   ├── interfaces/                        # 用户接口层
│   │       │   │   ├── web/
│   │       │   │   │   ├── UserController.java
│   │       │   │   │   └── UserProfileController.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── request/
│   │       │   │   │   │   ├── CreateUserRequest.java
│   │       │   │   │   │   └── UpdateUserRequest.java
│   │       │   │   │   └── response/
│   │       │   │   │       ├── UserResponse.java
│   │       │   │   │       └── UserProfileResponse.java
│   │       │   │   └── assembler/
│   │       │   │       └── UserAssembler.java
│   │       │   ├── application/                       # 用户应用层
│   │       │   │   ├── service/
│   │       │   │   │   └── UserApplicationService.java
│   │       │   │   ├── command/
│   │       │   │   │   ├── CreateUserCommand.java
│   │       │   │   │   └── UpdateUserCommand.java
│   │       │   │   ├── query/
│   │       │   │   │   └── UserQuery.java
│   │       │   │   └── event/
│   │       │   │       └── UserCreatedEventHandler.java
│   │       │   ├── domain/                            # 用户领域核心
│   │       │   │   ├── entity/
│   │       │   │   │   ├── User.java
│   │       │   │   │   └── UserProfile.java
│   │       │   │   ├── valueobject/                   # 复杂值对象
│   │       │   │   │   └── PhoneNumber.java           # 复杂业务概念才使用值对象
│   │       │   │   ├── service/
│   │       │   │   │   └── UserDomainService.java
│   │       │   │   ├── repository/
│   │       │   │   │   └── UserRepository.java
│   │       │   │   ├── factory/
│   │       │   │   │   └── UserFactory.java
│   │       │   │   └── event/
│   │       │   │       ├── UserCreatedEvent.java
│   │       │   │       └── UserUpdatedEvent.java
│   │       │   └── infrastructure/                    # 用户基础设施层
│   │       │       ├── persistence/
│   │       │       │   ├── UserRepositoryImpl.java
│   │       │       │   ├── UserMapper.java
│   │       │       │   ├── UserPO.java
│   │       │       │   └── UserConverter.java
│   │       │       └── external/
│   │       │           └── UserNotificationService.java
│   │       │
│   │       ├── order/                                 # 订单领域
│   │       │   ├── interfaces/                        # 订单接口层
│   │       │   │   ├── web/
│   │       │   │   │   └── OrderController.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── request/
│   │       │   │   │   │   └── CreateOrderRequest.java
│   │       │   │   │   └── response/
│   │       │   │   │       └── OrderResponse.java
│   │       │   │   └── assembler/
│   │       │   │       └── OrderAssembler.java
│   │       │   ├── application/                       # 订单应用层
│   │       │   │   ├── service/
│   │       │   │   │   └── OrderApplicationService.java
│   │       │   │   ├── command/
│   │       │   │   │   └── CreateOrderCommand.java
│   │       │   │   ├── query/
│   │       │   │   │   └── OrderQuery.java
│   │       │   │   └── event/
│   │       │   │       └── OrderCreatedEventHandler.java
│   │       │   ├── domain/                            # 订单领域核心
│   │       │   │   ├── entity/
│   │       │   │   │   ├── Order.java
│   │       │   │   │   └── OrderItem.java
│   │       │   │   ├── valueobject/                   # 复杂值对象
│   │       │   │   │   ├── Money.java                 # 金额值对象
│   │       │   │   │   └── OrderStatus.java           # 订单状态值对象
│   │       │   │   ├── service/
│   │       │   │   │   └── OrderDomainService.java
│   │       │   │   ├── repository/
│   │       │   │   │   └── OrderRepository.java
│   │       │   │   ├── factory/
│   │       │   │   │   └── OrderFactory.java
│   │       │   │   └── event/
│   │       │   │       ├── OrderCreatedEvent.java
│   │       │   │       └── OrderCancelledEvent.java
│   │       │   └── infrastructure/                    # 订单基础设施层
│   │       │       ├── persistence/
│   │       │       │   ├── OrderRepositoryImpl.java
│   │       │       │   ├── OrderMapper.java
│   │       │       │   ├── OrderPO.java
│   │       │       │   └── OrderConverter.java
│   │       │       └── external/
│   │       │           └── PaymentServiceClient.java
│   │       │
│   │       ├── infrastructure/                        # 全局基础设施层
│   │       │   ├── config/                            # 全局配置类
│   │       │   │   ├── DatabaseConfig.java
│   │       │   │   ├── RedisConfig.java
│   │       │   │   ├── SecurityConfig.java
│   │       │   │   └── WebConfig.java
│   │       │   ├── messaging/                         # 消息处理
│   │       │   │   ├── EventPublisher.java
│   │       │   │   └── MessageHandler.java
│   │       │   ├── security/                          # 安全实现
│   │       │   │   ├── authentication/
│   │       │   │   │   └── JwtAuthenticationProvider.java
│   │       │   │   └── authorization/
│   │       │   │       └── BasedAccessControl.java
│   │       │   └── util/                              # 基础设施工具
│   │       │       ├── JsonUtils.java
│   │       │       ├── DateUtils.java
│   │       │       └── ValidationUtils.java
│   │       │
│   │       └── shared/                                # 共享层
│   │           ├── domain/                            # 共享领域组件
│   │           │   ├── valueobject/                   # 共享值对象
│   │           │   │   ├── PageRequest.java           # 分页请求对象
│   │           │   │   └── Address.java               # 地址值对象
│   │           │   ├── entity/                        # 共享实体基类
│   │           │   │   └── BaseEntity.java
│   │           │   ├── specification/                 # 规约模式
│   │           │   │   └── Specification.java
│   │           │   └── event/                         # 共享事件
│   │           │       └── DomainEvent.java
│   │           ├── application/                       # 共享应用组件
│   │           │   ├── dto/                           # 共享DTO
│   │           │   │   ├── PageResult.java            # 分页响应对象
│   │           │   │   └── ApiResponse.java           # 统一响应对象
│   │           │   └── service/                       # 共享应用服务
│   │           │       └── NotificationService.java
│   │           ├── infrastructure/                    # 共享基础设施
│   │           │   ├── persistence/                   # 共享持久化组件
│   │           │   │   └── BaseMapper.java
│   │           │   └── cache/                         # 缓存组件
│   │           │       └── CacheManager.java
│   │           ├── exception/                         # 异常处理
│   │           │   ├── BusinessException.java
│   │           │   ├── DomainException.java
│   │           │   ├── InfrastructureException.java
│   │           │   └── GlobalExceptionHandler.java
│   │           ├── constant/                          # 常量定义
│   │           │   ├── ErrorCode.java
│   │           │   ├── BusinessConstant.java
│   │           │   └── SystemConstant.java
│   │           ├── util/                              # 通用工具
│   │           │   ├── IdGenerator.java
│   │           │   ├── BeanUtils.java
│   │           │   └── StringUtils.java
│   │           ├── annotation/                        # 自定义注解
│   │           │   ├── DomainService.java
│   │           │   └── ApplicationService.java
│   │           └── base/                              # 基础类
│   │               ├── BaseEntity.java
│   │               ├── BaseValueObject.java
│   │               └── BaseRepository.java
│   │
│   └── resources/
│       ├── application.yml                            # 主配置文件
│       ├── application-dev.yml                        # 开发环境配置
│       ├── application-test.yml                       # 测试环境配置
│       ├── application-prod.yml                       # 生产环境配置
│       ├── mapper/                                    # MyBatis XML映射
│       │   ├── user/
│       │   │   └── UserMapper.xml
│       │   └── order/
│       │       └── OrderMapper.xml
│       ├── db/                                        # 数据库脚本
│       │   ├── migration/                             # 数据库迁移脚本
│       │   │   ├── V1__Create_user_table.sql
│       │   │   └── V2__Create_order_table.sql
│       │   └── data/                                  # 测试数据
│       │       └── test-data.sql
│       ├── messages/                                      # 国际化资源
│       │   ├── messages.properties
│       │   ├── messages_en.properties
│       │   └── messages_zh_CN.properties
│       └── static/                                    # 静态资源
```

**项目结构设计原则：**
1. **领域+分层组织**：多领域场景下，按"领域+DDD分层"方式组织包结构，每个领域内部完整实现DDD四层架构
2. **共享组件分离**：分页对象、统一响应对象等共享组件放在shared层，避免重复定义
3. **值对象按需设计**：避免过度设计，简单的字符串类型（如email、userId）可直接使用基本类型，复杂的业务概念（如Money、PhoneNumber）才使用值对象
4. **严格的分层隔离**：每层都有明确的边界，不允许跨层直接访问
5. **接口与实现分离**：领域层定义接口，基础设施层提供实现
6. **配置外部化**：所有配置都放在resources目录下，支持多环境
7. **资源按领域分类**：XML映射文件等资源按领域分类存放

#### 2.2.2 微服务项目结构

在微服务架构中，每个服务都是一个独立的DDD应用，遵循分层架构。服务之间通过API网关进行通信，共享的领域模型和基础设施可以提取为公共库。

```
project/
├── project-shared/                # 共享模块
├── project-user/                  # 用户服务
│   ├── project-user-api/          # 用户服务API模块
│   └── project-user-service/      # 用户服务实现模块（包含应用、领域、基础设施层）
├── project-order/                 # 订单服务
│   ├── project-order-api/         # 订单服务API模块
│   └── project-order-service/     # 订单服务实现模块
├── project-payment/               # 支付服务
│   ├── project-payment-api/       # 支付服务API模块
│   └── project-payment-service/   # 支付服务实现模块
├── project-gateway/               # 网关服务
└── project-config/                # 配置服务
```

### 2.3 分层职责

#### 2.3.1 接口层 (Interfaces Layer)

**核心职责：**
- 处理HTTP请求和响应
- 数据格式转换（DTO ↔ Domain Object）
- 输入验证和参数校验
- 异常处理和错误响应
- 国际化消息处理
- API文档生成

**主要组件：**
- **Controller**：REST API 控制器，处理HTTP请求
- **DTO**：数据传输对象，定义API的输入输出格式
- **Assembler**：DTO 与领域对象的转换器
- **Facade**：为复杂操作提供简化接口

**设计原则：**
- 薄接口层：不包含业务逻辑，只负责协议转换
- 统一响应格式：所有API返回统一的响应结构
- 完整的输入验证：确保数据的有效性和安全性

#### 2.3.2 应用层 (Application Layer)

**核心职责：**
- 业务流程编排和协调
- 事务边界控制
- 权限验证和安全控制
- 应用事件发布和处理
- 缓存管理
- 外部服务调用协调

**主要组件：**
- **Application Service**：应用服务，编排业务流程
- **Command**：命令对象，封装操作请求
- **Query**：查询对象，封装查询请求
- **Event Handler**：应用事件处理器
- **Cache Service**：缓存服务

**设计原则：**
- 无状态服务：应用服务不保存状态信息
- 事务边界：在应用层控制事务的开始和结束
- 编排不实现：编排领域服务，不实现具体业务逻辑

#### 2.3.3 领域层 (Domain Layer)

**核心职责：**
- 核心业务逻辑实现
- 业务规则和约束
- 领域模型定义
- 业务不变性保证
- 领域事件定义和发布

**主要组件：**
- **Entity**：领域实体，具有唯一标识的业务对象
- **Value Object**：值对象，通过属性值区分的对象
- **Domain Service**：领域服务，跨实体的业务逻辑
- **Repository Interface**：仓储接口，定义数据访问契约
- **Factory**：领域对象工厂，负责复杂对象的创建
- **Domain Event**：领域事件，表示业务中的重要事件

**设计原则：**
- 业务逻辑纯净：不依赖任何技术框架
- 充血模型：实体包含业务行为，不仅仅是数据容器
- 不变性保证：通过业务规则维护数据一致性

#### 2.3.4 基础设施层 (Infrastructure Layer)

**核心职责：**
- 数据持久化实现
- 外部服务集成
- 技术框架配置
- 横切关注点实现
- 国际化和时区支持
- 消息队列集成

**主要组件：**
- **Repository Implementation**：仓储实现，具体的数据访问逻辑
- **MyBatis Plus Mapper**：数据访问映射
- **Entity**：数据库实体对象，对应数据库表结构
- **Converter**：领域对象与数据库实体的转换器
- **External Service**：外部服务客户端
- **Configuration**：技术配置类
- **I18n Support**：国际化支持

**设计原则：**
- 实现抽象：实现领域层定义的接口
- 技术隔离：将技术细节封装在基础设施层
- 可替换性：支持不同技术实现的替换

### 2.4 最佳实践

#### 2.4.1 依赖管理原则

**1. 严格的分层依赖**
- 接口层 → 应用层 → 领域层
- 基础设施层 → 领域层（实现仓储接口）
- 禁止跨层调用和循环依赖

**2. 依赖倒置原则**
- 高层模块不依赖低层模块
- 抽象不依赖具体实现
- 具体实现依赖抽象

**3. 接口隔离原则**
- 为不同的客户端定义不同的接口
- 避免胖接口，保持接口的单一职责

#### 2.4.2 事务控制策略

**事务边界原则：**
- 在应用层控制事务边界
- 一个应用服务方法对应一个事务
- 避免跨应用服务的事务

**事务配置最佳实践：**
```java
@Service
@Transactional(readOnly = true)  // 默认只读事务
public class UserApplicationService {
    
    @Transactional(rollbackFor = Exception.class)  // 写操作事务
    public UserDTO createUser(CreateUserCommand command) {
        // 业务逻辑实现
    }
    
    // 查询操作使用默认的只读事务
    public UserDTO findById(String id) {
        // 查询逻辑实现
    }
}
```

#### 2.4.3 异常处理策略

**分层异常处理：**
- **领域层**：抛出领域异常，表示业务规则违反
- **应用层**：处理业务流程异常，协调多个领域服务
- **接口层**：统一异常响应格式，提供用户友好的错误信息

**异常设计原则：**
- 使用检查异常表示可恢复的错误
- 使用运行时异常表示编程错误
- 提供国际化的错误消息
- 记录足够的上下文信息用于问题排查

#### 2.4.4 缓存使用策略

**多级缓存架构：**
1. **L1缓存**：本地缓存（Caffeine），用于热点数据
2. **L2缓存**：分布式缓存（Redis），用于共享数据
3. **L3缓存**：数据库查询结果缓存

**缓存更新策略：**
- **Cache-Aside**：应用程序管理缓存
- **Write-Through**：写入时同步更新缓存
- **Write-Behind**：异步更新缓存
- **Refresh-Ahead**：预先刷新即将过期的缓存

---

## 3. 基础设施配置

