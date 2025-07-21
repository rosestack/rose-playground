# DDD 分层架构实践指南

---

## 1. 概述

### 1.1 DDD 核心理念

领域驱动设计（Domain-Driven Design，DDD）是一种软件开发方法论，强调将业务领域的复杂性作为软件设计的核心。通过清晰的分层架构来组织代码，确保业务逻辑的纯净性和系统的可维护性。

**核心概念：**
- **领域（Domain）**：业务问题空间，包含业务规则和逻辑
- **实体（Entity）**：具有唯一标识的领域对象
- **值对象（Value Object）**：没有唯一标识，通过属性值来区分的对象
- **聚合（Aggregate）**：一组相关实体和值对象的集合
- **仓储（Repository）**：提供领域对象持久化的抽象接口
- **领域服务（Domain Service）**：不属于任何实体或值对象的业务逻辑

### 1.2 技术栈

- **Java 17+**：使用最新的Java特性
- **Spring Boot 3.x**：主框架
- **MyBatis Plus 3.x**：数据访问层ORM框架
- **MySQL 8.0**：主数据库
- **Redis**：缓存和会话存储
- **Maven**：构建工具

---

## 2. 架构设计

### 2.1 四层架构模型

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

### 2.2 项目结构

```
src/main/java/com/example/app/
├── interfaces/              # 接口层
│   ├── controller/          # REST 控制器
│   ├── dto/                 # 数据传输对象
│   ├── assembler/           # DTO 转换器
│   ├── facade/              # 外观服务
│   └── validator/           # 校验器
├── application/             # 应用层
│   ├── service/             # 应用服务
│   ├── command/             # 命令对象
│   ├── query/               # 查询对象
│   ├── event/               # 应用事件
│   └── cache/               # 缓存服务
├── domain/                  # 领域层
│   ├── entity/              # 领域实体
│   ├── valueobject/         # 值对象
│   ├── service/             # 领域服务
│   ├── repository/          # 仓储接口
│   ├── factory/             # 工厂类
│   └── event/               # 领域事件
├── infrastructure/          # 基础设施层
│   ├── persistence/         # 持久化实现
│   │   ├── mapper/          # MyBatis Plus Mapper
│   │   ├── entity/          # 数据库实体
│   │   ├── repository/      # 仓储实现
│   │   └── converter/       # 实体转换器
│   ├── config/              # 配置类
│   ├── external/            # 外部服务集成
│   ├── i18n/                # 国际化支持
│   └── util/                # 工具类
└── shared/                  # 共享层
    ├── exception/           # 异常处理
    ├── constant/            # 常量定义
    ├── response/            # 统一响应格式
    └── util/                # 通用工具
```

---

### 2.3 分层职责

#### 2.3.1 接口层 (Interfaces Layer)

**职责：**
- 处理用户请求和响应
- 数据格式转换（DTO ↔ Domain Object）
- 输入验证和参数校验
- 异常处理和错误响应
- 国际化消息处理

**主要组件：**
- **Controller**：REST API 控制器
- **DTO**：数据传输对象
- **Assembler**：DTO 与领域对象的转换器
- **Facade**：为复杂操作提供简化接口
- **Validator**：业务校验器

#### 2.3.2 应用层 (Application Layer)

**职责：**
- 业务流程编排和协调
- 事务边界控制
- 权限验证和安全控制
- 应用事件发布和处理
- 缓存管理

**主要组件：**
- **Application Service**：应用服务，编排业务流程
- **Command**：命令对象，封装操作请求
- **Query**：查询对象，封装查询请求
- **Event Handler**：应用事件处理器
- **Cache Service**：缓存服务

#### 2.3.3 领域层 (Domain Layer)

**职责：**
- 核心业务逻辑实现
- 业务规则和约束
- 领域模型定义
- 业务不变性保证

**主要组件：**
- **Entity**：领域实体
- **Value Object**：值对象
- **Domain Service**：领域服务
- **Repository Interface**：仓储接口
- **Factory**：领域对象工厂
- **Domain Event**：领域事件

#### 2.3.4 基础设施层 (Infrastructure Layer)

**职责：**
- 数据持久化实现
- 外部服务集成
- 技术框架配置
- 横切关注点实现
- 国际化和时区支持

**主要组件：**
- **Repository Implementation**：仓储实现
- **MyBatis Plus Mapper**：数据访问映射
- **Entity**：数据库实体对象
- **Converter**：领域对象与数据库实体的转换器
- **External Service**：外部服务客户端
- **Configuration**：技术配置
- **I18n Support**：国际化支持

### 2.4. 最佳实践

#### 2.4.1 依赖管理原则

1. **严格的分层依赖**
   - 接口层 → 应用层 → 领域层
   - 基础设施层 → 领域层（实现仓储接口）
   - 禁止跨层调用和循环依赖

2. **依赖倒置**
   - 高层模块不依赖低层模块
   - 抽象不依赖具体实现
   - 具体实现依赖抽象

#### 2.4.2 事务控制策略

```java
/**
 * 事务控制最佳实践
 */
@Service
@Transactional(readOnly = true)
public class UserApplicationService {
    
    /**
     * 写操作使用事务
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO createUser(CreateUserCommand command) {
        // 业务逻辑
        User user = new User(new Username(command.getUsername()), 
                           new Email(command.getEmail()));
        
        // 保存实体
        User savedUser = userRepository.save(user);
        
        // 发布事件（事务提交后执行）
        eventPublisher.publishAfterCommit(new UserCreatedEvent(savedUser.getId()));
        
        return UserAssembler.toDTO(savedUser);
    }
    
    /**
     * 只读操作不使用事务
     */
    public UserDTO findById(String id) {
        User user = userRepository.findById(new UserId(id))
                .orElseThrow(() -> UserException.userNotFound(id));
        
        return UserAssembler.toDTO(user);
    }
}
```

#### 2.4.3 异常处理策略

1. **分层异常处理**
   - 领域层：抛出领域异常
   - 应用层：处理业务流程异常
   - 接口层：统一异常响应格式

2. **国际化异常消息**
   - 使用消息键而非硬编码消息
   - 支持参数化消息
   - 提供多语言支持

#### 2.4.4 缓存使用策略

1. **缓存层次**
   - L1：本地缓存（Caffeine）
   - L2：分布式缓存（Redis）
   - L3：数据库

2. **缓存更新策略**
   - 写入时更新（Write-through）
   - 写入后更新（Write-behind）
   - 失效时更新（Cache-aside）

---

## 4. 核心实现

### 4.1 层间依赖关系

在DDD分层架构中，各层之间的依赖关系遵循严格的规则：

```
┌─────────────────────────────────────┐
│           接口层 (Interfaces)        │  ← 依赖应用层
├─────────────────────────────────────┤
│           应用层 (Application)       │  ← 依赖领域层
├─────────────────────────────────────┤
│            领域层 (Domain)           │  ← 不依赖其他层
├─────────────────────────────────────┤
│         基础设施层 (Infrastructure)   │  ← 依赖领域层（实现接口）
└─────────────────────────────────────┘
```

**依赖规则：**
- **接口层** → 应用层：调用应用服务，不直接访问领域层或基础设施层
- **应用层** → 领域层：编排领域服务和聚合，通过接口访问基础设施层
- **领域层** → 无依赖：纯业务逻辑，不依赖任何外部框架
- **基础设施层** → 领域层：实现领域层定义的接口（依赖倒置）

**调用流程：**
```
用户请求 → Controller → Application Service → Domain Service/Entity → Repository Interface → Repository Implementation
```

### 4.2 MyBatis-Plus 配置与优化

#### 4.2.1 核心配置

```java
/**
 * MyBatis-Plus 配置类
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.example.app.infrastructure.persistence.mapper")
public class MybatisPlusConfig {
    
    /**
     * 分页插件配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(1000L); // 最大分页限制
        paginationInterceptor.setOverflow(false); // 溢出总页数后是否进行处理
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        // 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        
        // 慢查询插件
        interceptor.addInnerInterceptor(new IllegalSQLInnerInterceptor());
        
        return interceptor;
    }
    
    /**
     * 自定义ID生成器
     */
    @Bean
    public IdentifierGenerator identifierGenerator() {
        return new CustomIdGenerator();
    }
    
    /**
     * 元数据处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new CustomMetaObjectHandler();
    }
    
    /**
     * SQL性能监控
     */
    @Bean
    @Profile({"dev", "test"})
    public PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
        performanceInterceptor.setMaxTime(2000); // SQL执行最大时长，超过则停止运行
        performanceInterceptor.setFormat(true); // 格式化SQL
        return performanceInterceptor;
    }
}

/**
 * 自定义ID生成器
 */
@Component
public class CustomIdGenerator implements IdentifierGenerator {
    
    private final SnowflakeIdWorker snowflakeIdWorker;
    
    public CustomIdGenerator() {
        // 从配置中获取机器ID和数据中心ID
        long workerId = getWorkerId();
        long datacenterId = getDatacenterId();
        this.snowflakeIdWorker = new SnowflakeIdWorker(workerId, datacenterId);
    }
    
    @Override
    public Number nextId(Object entity) {
        return snowflakeIdWorker.nextId();
    }
    
    @Override
    public String nextUUID(Object entity) {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

/**
 * 自动填充处理器
 */
@Component
@Slf4j
public class CustomMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充...");
        
        // 获取当前用户信息
        String currentUserId = getCurrentUserId();
        Instant now = Instant.now();
        
        // 自动填充创建时间和创建人
        this.strictInsertFill(metaObject, "createdAt", Instant.class, now);
        this.strictInsertFill(metaObject, "createdBy", String.class, currentUserId);
        this.strictInsertFill(metaObject, "updatedAt", Instant.class, now);
        this.strictInsertFill(metaObject, "updatedBy", String.class, currentUserId);
        this.strictInsertFill(metaObject, "version", Integer.class, 1);
        this.strictInsertFill(metaObject, "deleted", Boolean.class, false);
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充...");
        
        String currentUserId = getCurrentUserId();
        Instant now = Instant.now();
        
        // 自动填充更新时间和更新人
        this.strictUpdateFill(metaObject, "updatedAt", Instant.class, now);
        this.strictUpdateFill(metaObject, "updatedBy", String.class, currentUserId);
    }
    
    private String getCurrentUserId() {
        // 从安全上下文获取当前用户ID
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
}
```

#### 4.2.2 MyBatis-Plus配置

```yaml

# MyBatis-Plus配置
mybatis-plus:
  configuration:
    # 开启驼峰命名转换
    map-underscore-to-camel-case: true
    # 开启二级缓存
    cache-enabled: true
    # 延迟加载
    lazy-loading-enabled: true
    aggressive-lazy-loading: false
    # 开启结果集映射
    auto-mapping-behavior: partial
    # 日志实现
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  
  global-config:
    db-config:
      # 主键类型
      id-type: assign_id
      # 逻辑删除字段
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      # 字段验证策略
      insert-strategy: not_null
      update-strategy: not_null
      select-strategy: not_empty
    
    # 元数据处理器
    meta-object-handler: com.example.app.infrastructure.config.CustomMetaObjectHandler
```

### 4.3 统一响应格式

```java
/**
 * 统一API响应格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /** 响应码 */
    private String code;
    
    /** 响应消息 */
    private String message;
    
    /** 响应数据 */
    private T data;
    
    /** 时间戳 */
    private Long timestamp;
    
    /** 请求ID */
    private String requestId;
    
    /** 国际化语言 */
    private String locale;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code("SUCCESS")
                .message("操作成功")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .requestId(RequestContextHolder.getRequestId())
                .locale(LocaleContextHolder.getLocale().toString())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .requestId(RequestContextHolder.getRequestId())
                .locale(LocaleContextHolder.getLocale().toString())
                .build();
    }
}
```

### 4.2 分页响应格式

```java
/**
 * 分页响应格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    /** 当前页码 */
    private Long current;
    
    /** 每页大小 */
    private Long size;
    
    /** 总记录数 */
    private Long total;
    
    /** 总页数 */
    private Long pages;
    
    /** 数据列表 */
    private List<T> records;
    
    /** 是否有下一页 */
    private Boolean hasNext;
    
    /** 是否有上一页 */
    private Boolean hasPrevious;
    
    public static <T> PageResponse<T> of(IPage<T> page) {
        return PageResponse.<T>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .records(page.getRecords())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
```

### 4.3 通用分页请求

```java
/**
 * 通用分页请求对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    
    /** 当前页码，从1开始 */
    @Min(value = 1, message = "页码必须大于0")
    private Long current = 1L;
    
    /** 每页大小 */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 1000, message = "每页大小不能超过1000")
    private Long size = 10L;
    
    /** 排序字段 */
    private String orderBy;
    
    /** 排序方向：ASC/DESC */
    private String orderDirection = "ASC";
    
    /**
     * 转换为MyBatis Plus的Page对象
     */
    public <T> Page<T> toPage() {
        Page<T> page = new Page<>(current, size);
        
        if (StringUtils.hasText(orderBy)) {
            OrderItem orderItem = new OrderItem();
            orderItem.setColumn(orderBy);
            orderItem.setAsc("ASC".equalsIgnoreCase(orderDirection));
            page.addOrder(orderItem);
        }
        
        return page;
    }
}
```

---

## 5. 各层完整代码示例

### 5.1 接口层（Interface Layer）

#### 5.1.1 用户控制器

```java
/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
@Validated
@Slf4j
@Tag(name = "用户管理", description = "用户相关操作接口")
public class UserController {
    
    @Autowired
    private UserApplicationService userApplicationService;
    
    @Autowired
    private UserQueryService userQueryService;
    
    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "409", description = "用户已存在")
    })
    public ApiResponse<UserDTO> createUser(@Valid @RequestBody CreateUserCommand command) {
        log.info("创建用户请求: {}", command);
        
        UserDTO user = userApplicationService.createUser(command);
        
        return ApiResponse.success(user, "用户创建成功");
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public ApiResponse<UserDTO> updateUser(
            @PathVariable @NotBlank String userId,
            @Valid @RequestBody UpdateUserCommand command) {
        
        log.info("更新用户请求: userId={}, command={}", userId, command);
        
        command.setUserId(userId);
        UserDTO user = userApplicationService.updateUser(command);
        
        return ApiResponse.success(user, "用户更新成功");
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "删除指定用户")
    public ApiResponse<Void> deleteUser(@PathVariable @NotBlank String userId) {
        log.info("删除用户请求: userId={}", userId);
        
        DeleteUserCommand command = new DeleteUserCommand(userId);
        userApplicationService.deleteUser(command);
        
        return ApiResponse.success("用户删除成功");
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
    public ApiResponse<UserDTO> getUserById(@PathVariable @NotBlank String userId) {
        log.info("获取用户详情请求: userId={}", userId);
        
        UserDTO user = userQueryService.findById(userId);
        
        return ApiResponse.success(user);
    }
    
    /**
     * 分页查询用户
     */
    @GetMapping
    @Operation(summary = "分页查询用户", description = "根据条件分页查询用户列表")
    public ApiResponse<PageResponse<UserDTO>> queryUsers(
            @Valid UserQueryRequest queryRequest,
            @Valid PageRequest pageRequest) {
        
        log.info("分页查询用户请求: query={}, page={}", queryRequest, pageRequest);
        
        PageResponse<UserDTO> users = userQueryService.queryUsers(queryRequest, pageRequest);
        
        return ApiResponse.success(users);
    }
    
    /**
     * 激活用户
     */
    @PostMapping("/{userId}/activate")
    @Operation(summary = "激活用户", description = "激活指定用户")
    public ApiResponse<Void> activateUser(@PathVariable @NotBlank String userId) {
        log.info("激活用户请求: userId={}", userId);
        
        ActivateUserCommand command = new ActivateUserCommand(userId);
        userApplicationService.activateUser(command);
        
        return ApiResponse.success("用户激活成功");
    }
    
    /**
     * 禁用用户
     */
    @PostMapping("/{userId}/deactivate")
    @Operation(summary = "禁用用户", description = "禁用指定用户")
    public ApiResponse<Void> deactivateUser(@PathVariable @NotBlank String userId) {
        log.info("禁用用户请求: userId={}", userId);
        
        DeactivateUserCommand command = new DeactivateUserCommand(userId);
        userApplicationService.deactivateUser(command);
        
        return ApiResponse.success("用户禁用成功");
    }
}
```

#### 5.1.2 请求响应对象

```java
/**
 * 创建用户命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserCommand {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Size(max = 100, message = "昵称长度不能超过100")
    private String nickname;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    private String avatar;
    
    private String remark;
}

/**
 * 更新用户命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserCommand {
    
    private String userId;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Size(max = 100, message = "昵称长度不能超过100")
    private String nickname;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    private String avatar;
    
    private String remark;
}

/**
 * 用户查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQueryRequest {
    
    private String username;
    
    private String email;
    
    private String nickname;
    
    private String phone;
    
    private UserStatus status;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTimeStart;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTimeEnd;
}

/**
 * 用户DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private String id;
    
    private String username;
    
    private String email;
    
    private String nickname;
    
    private String phone;
    
    private String avatar;
    
    private UserStatus status;
    
    private String remark;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
    
    private String createdBy;
    
    private String updatedBy;
}
```

### 5.2 应用层（Application Layer）

#### 5.2.1 用户应用服务

```java
/**
 * 用户应用服务
 */
@Service
@Transactional
@Slf4j
public class UserApplicationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserDomainService userDomainService;
    
    @Autowired
    private DomainEventPublisher eventPublisher;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 创建用户
     */
    public UserDTO createUser(CreateUserCommand command) {
        log.info("开始创建用户: {}", command.getUsername());
        
        // 1. 验证用户名唯一性
        userDomainService.validateUsernameUniqueness(command.getUsername());
        
        // 2. 验证邮箱唯一性
        userDomainService.validateEmailUniqueness(command.getEmail());
        
        // 3. 创建用户聚合
        User user = User.create(
            UserCreateInfo.builder()
                .username(command.getUsername())
                .password(passwordEncoder.encode(command.getPassword()))
                .email(command.getEmail())
                .nickname(command.getNickname())
                .phone(command.getPhone())
                .avatar(command.getAvatar())
                .remark(command.getRemark())
                .build()
        );
        
        // 4. 保存用户
        User savedUser = userRepository.save(user);
        
        // 5. 发布领域事件
        eventPublisher.publish(new UserCreatedEvent(savedUser.getId().getValue()));
        
        log.info("用户创建成功: userId={}", savedUser.getId().getValue());
        
        return UserConverter.toDTO(savedUser);
    }
    
    /**
     * 更新用户
     */
    public UserDTO updateUser(UpdateUserCommand command) {
        log.info("开始更新用户: userId={}", command.getUserId());
        
        // 1. 获取用户聚合
        User user = userRepository.findById(new UserId(command.getUserId()))
            .orElseThrow(() -> UserException.userNotFound(command.getUserId()));
        
        // 2. 验证邮箱唯一性（如果邮箱发生变化）
        if (StringUtils.hasText(command.getEmail()) && 
            !command.getEmail().equals(user.getEmail().getValue())) {
            userDomainService.validateEmailUniqueness(command.getEmail());
        }
        
        // 3. 更新用户信息
        user.updateProfile(
            UserUpdateInfo.builder()
                .email(command.getEmail())
                .nickname(command.getNickname())
                .phone(command.getPhone())
                .avatar(command.getAvatar())
                .remark(command.getRemark())
                .build()
        );
        
        // 4. 保存用户
        User savedUser = userRepository.save(user);
        
        // 5. 发布领域事件
        eventPublisher.publish(new UserUpdatedEvent(savedUser.getId().getValue()));
        
        log.info("用户更新成功: userId={}", savedUser.getId().getValue());
        
        return UserConverter.toDTO(savedUser);
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(DeleteUserCommand command) {
        log.info("开始删除用户: userId={}", command.getUserId());
        
        // 1. 获取用户聚合
        User user = userRepository.findById(new UserId(command.getUserId()))
            .orElseThrow(() -> UserException.userNotFound(command.getUserId()));
        
        // 2. 执行删除业务逻辑
        user.delete();
        
        // 3. 保存用户状态
        userRepository.save(user);
        
        // 4. 发布领域事件
        eventPublisher.publish(new UserDeletedEvent(user.getId().getValue()));
        
        log.info("用户删除成功: userId={}", user.getId().getValue());
    }
    
    /**
     * 激活用户
     */
    public void activateUser(ActivateUserCommand command) {
        log.info("开始激活用户: userId={}", command.getUserId());
        
        // 1. 获取用户聚合
        User user = userRepository.findById(new UserId(command.getUserId()))
            .orElseThrow(() -> UserException.userNotFound(command.getUserId()));
        
        // 2. 激活用户
        user.activate();
        
        // 3. 保存用户状态
        userRepository.save(user);
        
        // 4. 发布领域事件
        eventPublisher.publish(new UserActivatedEvent(user.getId().getValue()));
        
        log.info("用户激活成功: userId={}", user.getId().getValue());
    }
    
    /**
     * 禁用用户
     */
    public void deactivateUser(DeactivateUserCommand command) {
        log.info("开始禁用用户: userId={}", command.getUserId());
        
        // 1. 获取用户聚合
        User user = userRepository.findById(new UserId(command.getUserId()))
            .orElseThrow(() -> UserException.userNotFound(command.getUserId()));
        
        // 2. 禁用用户
        user.deactivate();
        
        // 3. 保存用户状态
        userRepository.save(user);
        
        // 4. 发布领域事件
        eventPublisher.publish(new UserDeactivatedEvent(user.getId().getValue()));
        
        log.info("用户禁用成功: userId={}", user.getId().getValue());
    }
}
```

#### 5.2.2 用户查询服务

```java
/**
 * 用户查询服务
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class UserQueryService {
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 根据ID查询用户
     */
    public UserDTO findById(String userId) {
        log.info("查询用户详情: userId={}", userId);
        
        UserPO userPO = userMapper.selectById(userId);
        if (userPO == null) {
            throw UserException.userNotFound(userId);
        }
        
        return UserConverter.toDTO(userPO);
    }
    
    /**
     * 根据用户名查询用户
     */
    public UserDTO findByUsername(String username) {
        log.info("根据用户名查询用户: username={}", username);
        
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getUsername, username);
        
        UserPO userPO = userMapper.selectOne(wrapper);
        if (userPO == null) {
            throw UserException.userNotFound(username);
        }
        
        return UserConverter.toDTO(userPO);
    }
    
    /**
     * 分页查询用户
     */
    public PageResponse<UserDTO> queryUsers(UserQueryRequest queryRequest, PageRequest pageRequest) {
        log.info("分页查询用户: query={}, page={}", queryRequest, pageRequest);
        
        // 构建查询条件
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryRequest.getUsername())) {
            wrapper.like(UserPO::getUsername, queryRequest.getUsername());
        }
        
        if (StringUtils.hasText(queryRequest.getEmail())) {
            wrapper.like(UserPO::getEmail, queryRequest.getEmail());
        }
        
        if (StringUtils.hasText(queryRequest.getNickname())) {
            wrapper.like(UserPO::getNickname, queryRequest.getNickname());
        }
        
        if (StringUtils.hasText(queryRequest.getPhone())) {
            wrapper.like(UserPO::getPhone, queryRequest.getPhone());
        }
        
        if (queryRequest.getStatus() != null) {
            wrapper.eq(UserPO::getStatus, queryRequest.getStatus());
        }
        
        if (queryRequest.getCreatedTimeStart() != null) {
            wrapper.ge(UserPO::getCreatedTime, queryRequest.getCreatedTimeStart());
        }
        
        if (queryRequest.getCreatedTimeEnd() != null) {
            wrapper.le(UserPO::getCreatedTime, queryRequest.getCreatedTimeEnd());
        }
        
        // 执行分页查询
        Page<UserPO> page = userMapper.selectPage(pageRequest.toPage(), wrapper);
        
        // 转换结果
        List<UserDTO> userDTOs = page.getRecords().stream()
            .map(UserConverter::toDTO)
            .collect(Collectors.toList());
        
        PageResponse<UserDTO> response = PageResponse.<UserDTO>builder()
            .current(page.getCurrent())
            .size(page.getSize())
            .total(page.getTotal())
            .pages(page.getPages())
            .records(userDTOs)
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
        
        log.info("用户查询完成: total={}", response.getTotal());
        
        return response;
    }
    
    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getUsername, username);
        
        return userMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 检查邮箱是否存在
     */
    public boolean existsByEmail(String email) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getEmail, email);
        
        return userMapper.selectCount(wrapper) > 0;
    }
}
```

### 5.3 领域层（Domain Layer）

#### 5.3.1 用户聚合根

```java
/**
 * 用户聚合根
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends AggregateRoot<UserId> {
    
    /** 用户名 */
    private Username username;
    
    /** 密码 */
    private Password password;
    
    /** 邮箱 */
    private Email email;
    
    /** 昵称 */
    private Nickname nickname;
    
    /** 手机号 */
    private Phone phone;
    
    /** 头像 */
    private Avatar avatar;
    
    /** 用户状态 */
    private UserStatus status;
    
    /** 备注 */
    private String remark;
    
    /** 私有构造函数，防止外部直接创建 */
    private User() {
        super();
    }
    
    /**
     * 创建用户
     */
    public static User create(UserCreateInfo createInfo) {
        User user = new User();
        user.setId(UserId.generate());
        user.username = new Username(createInfo.getUsername());
        user.password = new Password(createInfo.getPassword());
        user.email = new Email(createInfo.getEmail());
        user.nickname = new Nickname(createInfo.getNickname());
        user.phone = createInfo.getPhone() != null ? new Phone(createInfo.getPhone()) : null;
        user.avatar = createInfo.getAvatar() != null ? new Avatar(createInfo.getAvatar()) : null;
        user.status = UserStatus.ACTIVE;
        user.remark = createInfo.getRemark();
        user.initializeAuditInfo();
        
        // 添加领域事件
        user.addDomainEvent(new UserCreatedEvent(user.getId().getValue()));
        
        return user;
    }
    
    /**
     * 更新用户资料
     */
    public void updateProfile(UserUpdateInfo updateInfo) {
        if (StringUtils.hasText(updateInfo.getEmail())) {
            this.email = new Email(updateInfo.getEmail());
        }
        
        if (StringUtils.hasText(updateInfo.getNickname())) {
            this.nickname = new Nickname(updateInfo.getNickname());
        }
        
        if (StringUtils.hasText(updateInfo.getPhone())) {
            this.phone = new Phone(updateInfo.getPhone());
        }
        
        if (StringUtils.hasText(updateInfo.getAvatar())) {
            this.avatar = new Avatar(updateInfo.getAvatar());
        }
        
        if (StringUtils.hasText(updateInfo.getRemark())) {
            this.remark = updateInfo.getRemark();
        }
        
        this.updateAuditInfo();
        
        // 添加领域事件
        this.addDomainEvent(new UserUpdatedEvent(this.getId().getValue()));
    }
    
    /**
     * 修改密码
     */
    public void changePassword(String oldPassword, String newPassword, PasswordEncoder passwordEncoder) {
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, this.password.getValue())) {
            throw UserException.invalidCredentials();
        }
        
        // 设置新密码
        this.password = new Password(passwordEncoder.encode(newPassword));
        this.updateAuditInfo();
        
        // 添加领域事件
        this.addDomainEvent(new UserPasswordChangedEvent(this.getId().getValue()));
    }
    
    /**
     * 激活用户
     */
    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new BusinessException("USER_ALREADY_ACTIVE", "user.already.active");
        }
        
        this.status = UserStatus.ACTIVE;
        this.updateAuditInfo();
        
        // 添加领域事件
        this.addDomainEvent(new UserActivatedEvent(this.getId().getValue()));
    }
    
    /**
     * 禁用用户
     */
    public void deactivate() {
        if (this.status == UserStatus.INACTIVE) {
            throw new BusinessException("USER_ALREADY_INACTIVE", "user.already.inactive");
        }
        
        this.status = UserStatus.INACTIVE;
        this.updateAuditInfo();
        
        // 添加领域事件
        this.addDomainEvent(new UserDeactivatedEvent(this.getId().getValue()));
    }
    
    /**
     * 删除用户
     */
    public void delete() {
        this.status = UserStatus.DELETED;
        this.updateAuditInfo();
        
        // 添加领域事件
        this.addDomainEvent(new UserDeletedEvent(this.getId().getValue()));
    }
    
    /**
     * 检查用户是否可以执行操作
     */
    public void checkCanOperate() {
        if (this.status != UserStatus.ACTIVE) {
            throw new BusinessException("USER_NOT_ACTIVE", "user.not.active");
        }
    }
}
```

#### 5.3.2 值对象

```java
/**
 * 用户ID值对象
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class UserId extends EntityId {
    
    public UserId(String value) {
        super(value);
    }
    
    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }
    
    public static UserId of(String value) {
        return new UserId(value);
    }
}

/**
 * 用户名值对象
 */
@Value
public class Username {
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");
    
    String value;
    
    public Username(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException("INVALID_USERNAME", "username.required");
        }
        
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new BusinessException("INVALID_USERNAME", "username.invalid.format");
        }
        
        this.value = value;
    }
}

/**
 * 密码值对象
 */
@Value
public class Password {
    
    String value;
    
    public Password(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException("INVALID_PASSWORD", "password.required");
        }
        
        this.value = value;
    }
}

/**
 * 邮箱值对象
 */
@Value
public class Email {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    
    String value;
    
    public Email(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException("INVALID_EMAIL", "email.required");
        }
        
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new BusinessException("INVALID_EMAIL", "email.invalid.format");
        }
        
        this.value = value;
    }
}

/**
 * 昵称值对象
 */
@Value
public class Nickname {
    
    String value;
    
    public Nickname(String value) {
        if (StringUtils.hasText(value) && value.length() > 100) {
            throw new BusinessException("INVALID_NICKNAME", "nickname.too.long");
        }
        
        this.value = value;
    }
}

/**
 * 手机号值对象
 */
@Value
public class Phone {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    String value;
    
    public Phone(String value) {
        if (StringUtils.hasText(value) && !PHONE_PATTERN.matcher(value).matches()) {
            throw new BusinessException("INVALID_PHONE", "phone.invalid.format");
        }
        
        this.value = value;
    }
}

/**
 * 头像值对象
 */
@Value
public class Avatar {
    
    String value;
    
    public Avatar(String value) {
        this.value = value;
    }
}
```

#### 5.3.3 领域服务

```java
/**
 * 用户领域服务
 */
@Service
@Slf4j
public class UserDomainService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 验证用户名唯一性
     */
    public void validateUsernameUniqueness(String username) {
        log.info("验证用户名唯一性: {}", username);
        
        if (userRepository.existsByUsername(new Username(username))) {
            throw UserException.userAlreadyExists(username);
        }
    }
    
    /**
     * 验证邮箱唯一性
     */
    public void validateEmailUniqueness(String email) {
        log.info("验证邮箱唯一性: {}", email);
        
        if (userRepository.existsByEmail(new Email(email))) {
            throw new BusinessException("EMAIL_ALREADY_EXISTS", "email.already.exists", email);
        }
    }
    
    /**
     * 验证用户权限
     */
    public void validateUserPermission(UserId userId, String operation) {
        log.info("验证用户权限: userId={}, operation={}", userId.getValue(), operation);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserException.userNotFound(userId.getValue()));
        
        user.checkCanOperate();
        
        // 这里可以添加更复杂的权限验证逻辑
    }
    
    /**
     * 生成用户默认昵称
     */
    public String generateDefaultNickname(String username) {
        return "用户" + username;
    }
}
```

#### 5.3.4 仓储接口

```java
/**
 * 用户仓储接口
 */
public interface UserRepository extends Repository<User, UserId> {
    
    /**
     * 保存用户
     */
    User save(User user);
    
    /**
     * 根据ID查找用户
     */
    Optional<User> findById(UserId userId);
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(Username username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(Email email);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(Username username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(Email email);
    
    /**
     * 删除用户
     */
    void delete(User user);
    
    /**
     * 根据ID删除用户
     */
    void deleteById(UserId userId);
}
```

### 5.4 基础设施层（Infrastructure Layer）

#### 5.4.1 用户仓储实现

```java
/**
 * 用户仓储实现
 */
@Repository
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private DomainEventPublisher eventPublisher;
    
    @Override
    public User save(User user) {
        log.info("保存用户: userId={}", user.getId().getValue());
        
        UserPO userPO = UserConverter.toPO(user);
        
        if (userMapper.selectById(userPO.getId()) == null) {
            // 新增
            userMapper.insert(userPO);
        } else {
            // 更新
            userMapper.updateById(userPO);
        }
        
        // 发布领域事件
        publishDomainEvents(user);
        
        return user;
    }
    
    @Override
    public Optional<User> findById(UserId userId) {
        log.info("根据ID查找用户: userId={}", userId.getValue());
        
        UserPO userPO = userMapper.selectById(userId.getValue());
        if (userPO == null) {
            return Optional.empty();
        }
        
        return Optional.of(UserConverter.toDomain(userPO));
    }
    
    @Override
    public Optional<User> findByUsername(Username username) {
        log.info("根据用户名查找用户: username={}", username.getValue());
        
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getUsername, username.getValue());
        
        UserPO userPO = userMapper.selectOne(wrapper);
        if (userPO == null) {
            return Optional.empty();
        }
        
        return Optional.of(UserConverter.toDomain(userPO));
    }
    
    @Override
    public Optional<User> findByEmail(Email email) {
        log.info("根据邮箱查找用户: email={}", email.getValue());
        
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getEmail, email.getValue());
        
        UserPO userPO = userMapper.selectOne(wrapper);
        if (userPO == null) {
            return Optional.empty();
        }
        
        return Optional.of(UserConverter.toDomain(userPO));
    }
    
    @Override
    public boolean existsByUsername(Username username) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getUsername, username.getValue());
        
        return userMapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public boolean existsByEmail(Email email) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getEmail, email.getValue());
        
        return userMapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public void delete(User user) {
        log.info("删除用户: userId={}", user.getId().getValue());
        
        userMapper.deleteById(user.getId().getValue());
        
        // 发布领域事件
        publishDomainEvents(user);
    }
    
    @Override
    public void deleteById(UserId userId) {
        log.info("根据ID删除用户: userId={}", userId.getValue());
        
        userMapper.deleteById(userId.getValue());
    }
    
    /**
     * 发布领域事件
     */
    private void publishDomainEvents(User user) {
        user.getDomainEvents().forEach(eventPublisher::publish);
        user.clearDomainEvents();
    }
}
```

#### 5.4.2 数据访问对象

```java
/**
 * 用户持久化对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class UserPO extends BasePO {
    
    /** 用户名 */
    @TableField("username")
    private String username;
    
    /** 密码 */
    @TableField("password")
    private String password;
    
    /** 邮箱 */
    @TableField("email")
    private String email;
    
    /** 昵称 */
    @TableField("nickname")
    private String nickname;
    
    /** 手机号 */
    @TableField("phone")
    private String phone;
    
    /** 头像 */
    @TableField("avatar")
    private String avatar;
    
    /** 用户状态 */
    @TableField("status")
    private UserStatus status;
    
    /** 备注 */
    @TableField("remark")
    private String remark;
}

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {
    
    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM t_user WHERE username = #{username} AND deleted = 0")
    UserPO findByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM t_user WHERE email = #{email} AND deleted = 0")
    UserPO findByEmail(@Param("email") String email);
    
    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE username = #{username} AND deleted = 0")
    int countByUsername(@Param("username") String username);
    
    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE email = #{email} AND deleted = 0")
    int countByEmail(@Param("email") String email);
    
    /**
     * 分页查询用户
     */
    @Select("<script>" +
            "SELECT * FROM t_user WHERE deleted = 0" +
            "<if test='query.username != null and query.username != \"\"'>" +
            " AND username LIKE CONCAT('%', #{query.username}, '%')" +
            "</if>" +
            "<if test='query.email != null and query.email != \"\"'>" +
            " AND email LIKE CONCAT('%', #{query.email}, '%')" +
            "</if>" +
            "<if test='query.nickname != null and query.nickname != \"\"'>" +
            " AND nickname LIKE CONCAT('%', #{query.nickname}, '%')" +
            "</if>" +
            "<if test='query.phone != null and query.phone != \"\"'>" +
            " AND phone LIKE CONCAT('%', #{query.phone}, '%')" +
            "</if>" +
            "<if test='query.status != null'>" +
            " AND status = #{query.status}" +
            "</if>" +
            "<if test='query.createdTimeStart != null'>" +
            " AND created_time >= #{query.createdTimeStart}" +
            "</if>" +
            "<if test='query.createdTimeEnd != null'>" +
            " AND created_time <= #{query.createdTimeEnd}" +
            "</if>" +
            " ORDER BY created_time DESC" +
            "</script>")
    IPage<UserPO> queryUsers(IPage<UserPO> page, @Param("query") UserQueryRequest query);
}
```

#### 5.4.3 对象转换器

```java
/**
 * 用户对象转换器
 */
@Component
public class UserConverter {
    
    /**
     * 领域对象转DTO
     */
    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return UserDTO.builder()
            .id(user.getId().getValue())
            .username(user.getUsername().getValue())
            .email(user.getEmail().getValue())
            .nickname(user.getNickname() != null ? user.getNickname().getValue() : null)
            .phone(user.getPhone() != null ? user.getPhone().getValue() : null)
            .avatar(user.getAvatar() != null ? user.getAvatar().getValue() : null)
            .status(user.getStatus())
            .remark(user.getRemark())
            .createdTime(user.getCreatedTime())
            .updatedTime(user.getUpdatedTime())
            .createdBy(user.getCreatedBy())
            .updatedBy(user.getUpdatedBy())
            .build();
    }
    
    /**
     * PO转DTO
     */
    public static UserDTO toDTO(UserPO userPO) {
        if (userPO == null) {
            return null;
        }
        
        return UserDTO.builder()
            .id(userPO.getId())
            .username(userPO.getUsername())
            .email(userPO.getEmail())
            .nickname(userPO.getNickname())
            .phone(userPO.getPhone())
            .avatar(userPO.getAvatar())
            .status(userPO.getStatus())
            .remark(userPO.getRemark())
            .createdTime(userPO.getCreatedTime())
            .updatedTime(userPO.getUpdatedTime())
            .createdBy(userPO.getCreatedBy())
            .updatedBy(userPO.getUpdatedBy())
            .build();
    }
    
    /**
     * 领域对象转PO
     */
    public static UserPO toPO(User user) {
        if (user == null) {
            return null;
        }
        
        UserPO userPO = new UserPO();
        userPO.setId(user.getId().getValue());
        userPO.setUsername(user.getUsername().getValue());
        userPO.setPassword(user.getPassword().getValue());
        userPO.setEmail(user.getEmail().getValue());
        userPO.setNickname(user.getNickname() != null ? user.getNickname().getValue() : null);
        userPO.setPhone(user.getPhone() != null ? user.getPhone().getValue() : null);
        userPO.setAvatar(user.getAvatar() != null ? user.getAvatar().getValue() : null);
        userPO.setStatus(user.getStatus());
        userPO.setRemark(user.getRemark());
        userPO.setCreatedTime(user.getCreatedTime());
        userPO.setUpdatedTime(user.getUpdatedTime());
        userPO.setCreatedBy(user.getCreatedBy());
        userPO.setUpdatedBy(user.getUpdatedBy());
        userPO.setDeleted(user.isDeleted());
        
        return userPO;
    }
    
    /**
     * PO转领域对象
     */
    public static User toDomain(UserPO userPO) {
        if (userPO == null) {
            return null;
        }
        
        User user = new User();
        user.setId(new UserId(userPO.getId()));
        user.setUsername(new Username(userPO.getUsername()));
        user.setPassword(new Password(userPO.getPassword()));
        user.setEmail(new Email(userPO.getEmail()));
        user.setNickname(userPO.getNickname() != null ? new Nickname(userPO.getNickname()) : null);
        user.setPhone(userPO.getPhone() != null ? new Phone(userPO.getPhone()) : null);
        user.setAvatar(userPO.getAvatar() != null ? new Avatar(userPO.getAvatar()) : null);
        user.setStatus(userPO.getStatus());
        user.setRemark(userPO.getRemark());
        user.setCreatedTime(userPO.getCreatedTime());
        user.setUpdatedTime(userPO.getUpdatedTime());
        user.setCreatedBy(userPO.getCreatedBy());
        user.setUpdatedBy(userPO.getUpdatedBy());
        user.setDeleted(userPO.getDeleted());
        
        return user;
    }
}
```

---

## 6. 异常处理与国际化

### 6.1 异常类

 * 业务异常基类

  ```java
     public abstract class BusinessException extends RuntimeException {
     
       /** 错误码 */
       private final String errorCode;
     
       /** 错误参数 */
       private final Object[] args;
     
       /** 国际化消息键 */
       private final String messageKey;
     
       public BusinessException(String errorCode, String messageKey, Object... args) {
           super(messageKey);
           this.errorCode = errorCode;
           this.messageKey = messageKey;
           this.args = args;
       }
     
       public String getErrorCode() {
           return errorCode;
       }
     
       public Object[] getArgs() {
           return args;
       }
     
       public String getMessageKey() {
           return messageKey;
    }
  }
  ```

 * 用户相关异常

 ```java
    public class UserException extends BusinessException {
    
      public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
      public static final String USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
      public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    
      public UserException(String errorCode, String messageKey, Object... args) {
          super(errorCode, messageKey, args);
      }
    
      public static UserException userNotFound(String userId) {
          return new UserException(USER_NOT_FOUND, "user.not.found", userId);
      }
    
      public static UserException userAlreadyExists(String username) {
          return new UserException(USER_ALREADY_EXISTS, "user.already.exists", username);
      }
    
      public static UserException invalidCredentials() {
          return new UserException(INVALID_CREDENTIALS, "user.invalid.credentials");
   }
 }
 ```
### 6.2 全局异常处理器

```java

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @Autowired
    private MessageSource messageSource;
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        String message = getLocalizedMessage(ex.getMessageKey(), ex.getArgs());
        
        log.warn("业务异常: code={}, message={}, uri={}", 
                ex.getErrorCode(), message, request.getRequestURI());
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ex.getErrorCode())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .requestId(RequestContextHolder.getRequestId())
                .locale(LocaleContextHolder.getLocale().toString())
                .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();
            String message = getLocalizedMessage(error.getDefaultMessage(), null);
            errors.put(field, message);
        });
        
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code("VALIDATION_ERROR")
                .message(getLocalizedMessage("validation.error", null))
                .data(errors)
                .timestamp(System.currentTimeMillis())
                .requestId(RequestContextHolder.getRequestId())
                .locale(LocaleContextHolder.getLocale().toString())
                .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理系统异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleSystemException(
            Exception ex, HttpServletRequest request) {
        
        log.error("系统异常: uri={}", request.getRequestURI(), ex);
        
        String message = getLocalizedMessage("system.error", null);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code("SYSTEM_ERROR")
                .message(message)
                .timestamp(System.currentTimeMillis())
                .requestId(RequestContextHolder.getRequestId())
                .locale(LocaleContextHolder.getLocale().toString())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 获取国际化消息
     */
    private String getLocalizedMessage(String key, Object[] args) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            return messageSource.getMessage(key, args, locale);
        } catch (Exception e) {
            log.warn("获取国际化消息失败: key={}", key, e);
            return key;
        }
    }
}
```

### 6.3 国际化配置

```java
/**
 * 国际化配置
 */
@Configuration
public class I18nConfig {
    
    /**
     * 消息源配置
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("i18n/messages", "i18n/validation", "i18n/error");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600);
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }
    
    /**
     * 区域解析器
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return resolver;
    }
    
    /**
     * 区域拦截器
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
```

### 6.4 国际化消息文件

**messages_zh_CN.properties**
```properties
# 通用消息
success=操作成功
system.error=系统错误，请稍后重试
validation.error=参数校验失败

# 用户相关消息
user.not.found=用户不存在：{0}
user.already.exists=用户名已存在：{0}
user.invalid.credentials=用户名或密码错误
user.created.success=用户创建成功
user.updated.success=用户更新成功
user.deleted.success=用户删除成功

# 校验消息
validation.required=该字段不能为空
validation.email.invalid=邮箱格式不正确
validation.password.weak=密码强度不够
validation.length.min=长度不能少于{0}个字符
validation.length.max=长度不能超过{0}个字符
```

**messages_en_US.properties**
```properties
# Common messages
success=Operation successful
system.error=System error, please try again later
validation.error=Validation failed

# User related messages
user.not.found=User not found: {0}
user.already.exists=Username already exists: {0}
user.invalid.credentials=Invalid username or password
user.created.success=User created successfully
user.updated.success=User updated successfully
user.deleted.success=User deleted successfully

# Validation messages
validation.required=This field is required
validation.email.invalid=Invalid email format
validation.password.weak=Password is too weak
validation.length.min=Length must be at least {0} characters
validation.length.max=Length must not exceed {0} characters
```

---

## 7. 日期时区处理

### 7.1 时区配置

```java
/**
 * 时区配置
 */
@Configuration
public class TimeZoneConfig {
    
    /**
     * 设置默认时区
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }
    
    /**
     * Jackson时间序列化配置
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册JavaTime模块
        mapper.registerModule(new JavaTimeModule());
        
        // 禁用时间戳序列化
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 设置时区
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        
        // 设置日期格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        mapper.setDateFormat(dateFormat);
        
        return mapper;
    }
    
    /**
     * 时区转换器
     */
    @Component
    public static class TimeZoneConverter {
        
        /**
         * 将UTC时间转换为指定时区时间
         */
        public static LocalDateTime convertToTimeZone(LocalDateTime utcTime, String timeZoneId) {
            if (utcTime == null) {
                return null;
            }
            
            ZoneId utcZone = ZoneId.of("UTC");
            ZoneId targetZone = ZoneId.of(timeZoneId);
            
            return utcTime.atZone(utcZone)
                    .withZoneSameInstant(targetZone)
                    .toLocalDateTime();
        }
        
        /**
         * 将指定时区时间转换为UTC时间
         */
        public static LocalDateTime convertToUtc(LocalDateTime localTime, String timeZoneId) {
            if (localTime == null) {
                return null;
            }
            
            ZoneId sourceZone = ZoneId.of(timeZoneId);
            ZoneId utcZone = ZoneId.of("UTC");
            
            return localTime.atZone(sourceZone)
                    .withZoneSameInstant(utcZone)
                    .toLocalDateTime();
        }
        
        /**
         * 获取当前用户时区的时间
         */
        public static LocalDateTime getCurrentUserTime() {
            String userTimeZone = UserContextHolder.getCurrentUserTimeZone();
            if (userTimeZone == null) {
                userTimeZone = "Asia/Shanghai";
            }
            
            return LocalDateTime.now(ZoneId.of(userTimeZone));
        }
    }
}
```

### 7.2 用户时区上下文

```java
/**
 * 用户上下文持有者
 */
public class UserContextHolder {
    
    private static final ThreadLocal<UserContext> CONTEXT_HOLDER = new ThreadLocal<>();
    
    public static void setUserContext(UserContext context) {
        CONTEXT_HOLDER.set(context);
    }
    
    public static UserContext getUserContext() {
        return CONTEXT_HOLDER.get();
    }
    
    public static String getCurrentUserTimeZone() {
        UserContext context = getUserContext();
        return context != null ? context.getTimeZone() : "Asia/Shanghai";
    }
    
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
    
    /**
     * 用户上下文信息
     */
    @Data
    @Builder
    public static class UserContext {
        private String userId;
        private String username;
        private String timeZone;
        private String locale;
    }
}

/**
 * 用户上下文拦截器
 */
@Component
public class UserContextInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        
        // 从请求头或用户信息中获取时区
        String timeZone = request.getHeader("X-Time-Zone");
        if (timeZone == null) {
            timeZone = getCurrentUserTimeZone(request);
        }
        
        // 从请求头获取语言
        String locale = request.getHeader("Accept-Language");
        if (locale == null) {
            locale = "zh-CN";
        }
        
        // 设置用户上下文
        UserContextHolder.UserContext context = UserContextHolder.UserContext.builder()
                .timeZone(timeZone)
                .locale(locale)
                .build();
        
        UserContextHolder.setUserContext(context);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                              HttpServletResponse response, 
                              Object handler, 
                              Exception ex) throws Exception {
        UserContextHolder.clear();
    }
    
    private String getCurrentUserTimeZone(HttpServletRequest request) {
        // 从JWT token或session中获取用户时区设置
        // 这里简化处理，实际应该从用户配置中获取
        return "Asia/Shanghai";
    }
}
```

### 7.3 时间字段处理

```java
/**
 * 基础实体类
 */
@MappedSuperclass
@Data
public abstract class BaseEntity {
    
    /** 创建时间 - 存储UTC时间 */
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /** 更新时间 - 存储UTC时间 */
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /** 创建人 */
    @Column(name = "created_by")
    private String createdBy;
    
    /** 更新人 */
    @Column(name = "updated_by")
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneId.of("UTC"));
    }
}

/**
 * 时间转换工具
 */
@Component
public class DateTimeConverter {
    
    /**
     * 将数据库UTC时间转换为用户时区时间
     */
    public LocalDateTime convertToUserTime(LocalDateTime utcTime) {
        if (utcTime == null) {
            return null;
        }
        
        String userTimeZone = UserContextHolder.getCurrentUserTimeZone();
        return TimeZoneConverter.convertToTimeZone(utcTime, userTimeZone);
    }
    
    /**
     * 将用户时区时间转换为UTC时间存储
     */
    public LocalDateTime convertToUtcTime(LocalDateTime userTime) {
        if (userTime == null) {
            return null;
        }
        
        String userTimeZone = UserContextHolder.getCurrentUserTimeZone();
        return TimeZoneConverter.convertToUtc(userTime, userTimeZone);
    }
}
```

### 7.4 时间格式化注解

```java
/**
 * 用户时区时间格式化注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = UserTimeZoneSerializer.class)
@JsonDeserialize(using = UserTimeZoneDeserializer.class)
public @interface UserTimeZone {
    String pattern() default "yyyy-MM-dd HH:mm:ss";
}

/**
 * 用户时区序列化器
 */
public class UserTimeZoneSerializer extends JsonSerializer<LocalDateTime> {
    
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        
        if (value == null) {
            gen.writeNull();
            return;
        }
        
        // 将UTC时间转换为用户时区时间
        String userTimeZone = UserContextHolder.getCurrentUserTimeZone();
        LocalDateTime userTime = TimeZoneConverter.convertToTimeZone(value, userTimeZone);
        
        // 格式化输出
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        gen.writeString(userTime.format(formatter));
    }
}

/**
 * 用户时区反序列化器
 */
public class UserTimeZoneDeserializer extends JsonDeserializer<LocalDateTime> {
    
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException {
        
        String dateString = p.getText();
        if (StringUtils.isEmpty(dateString)) {
            return null;
        }
        
        // 解析用户时区时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime userTime = LocalDateTime.parse(dateString, formatter);
        
        // 转换为UTC时间存储
        String userTimeZone = UserContextHolder.getCurrentUserTimeZone();
        return TimeZoneConverter.convertToUtc(userTime, userTimeZone);
    }
}
```

---
User
## 7. 领域事件与CQRS

### 7.1 领域事件设计

#### 7.1.1 领域事件基础架构

```java
/**
 * 领域事件基类
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public abstract class DomainEvent {
    
    /** 事件ID */
    private String eventId;
    
    /** 聚合根ID */
    private String aggregateId;
    
    /** 聚合根类型 */
    private String aggregateType;
    
    /** 事件版本 */
    private Integer version;
    
    /** 事件时间 */
    private LocalDateTime occurredOn;
    
    /** 事件类型 */
    private String eventType;
    
    /** 事件数据 */
    private Map<String, Object> eventData;
    
    /** 事件元数据 */
    private Map<String, Object> metadata;
    
    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now(ZoneId.of("UTC"));
        this.eventType = this.getClass().getSimpleName();
        this.version = 1;
        this.eventData = new HashMap<>();
        this.metadata = new HashMap<>();
    }
    
    protected DomainEvent(String aggregateId, String aggregateType) {
        this();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
    }
    
    /**
     * 添加事件数据
     */
    public void addEventData(String key, Object value) {
        this.eventData.put(key, value);
    }
    
    /**
     * 添加元数据
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
}

/**
 * 用户领域事件
 */
public abstract class UserDomainEvent extends DomainEvent {
    
    protected UserDomainEvent(String userId) {
        super(userId, "User");
    }
}

/**
 * 用户创建事件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserCreatedEvent extends UserDomainEvent {
    
    private String username;
    private String email;
    private String fullName;
    private UserStatus status;
    
    public UserCreatedEvent(String userId, String username, String email, String fullName) {
        super(userId);
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.status = UserStatus.ACTIVE;
        
        // 添加事件数据
        addEventData("username", username);
        addEventData("email", email);
        addEventData("fullName", fullName);
        addEventData("status", status);
    }
}

/**
 * 用户状态变更事件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserStatusChangedEvent extends UserDomainEvent {
    
    private UserStatus oldStatus;
    private UserStatus newStatus;
    private String reason;
    
    public UserStatusChangedEvent(String userId, UserStatus oldStatus, UserStatus newStatus, String reason) {
        super(userId);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        
        // 添加事件数据
        addEventData("oldStatus", oldStatus);
        addEventData("newStatus", newStatus);
        addEventData("reason", reason);
    }
}

/**
 * 用户删除事件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDeletedEvent extends UserDomainEvent {
    
    private String username;
    private String reason;
    
    public UserDeletedEvent(String userId, String username, String reason) {
        super(userId);
        this.username = username;
        this.reason = reason;
        
        // 添加事件数据
        addEventData("username", username);
        addEventData("reason", reason);
    }
}
```

#### 7.1.2 事件发布器

```java
/**
 * 领域事件发布器接口
 */
public interface DomainEventPublisher {
    
    /**
     * 发布单个事件
     */
    void publish(DomainEvent event);
    
    /**
     * 批量发布事件
     */
    void publishAll(List<DomainEvent> events);
    
    /**
     * 异步发布事件
     */
    CompletableFuture<Void> publishAsync(DomainEvent event);
    
    /**
     * 延迟发布事件
     */
    void publishDelayed(DomainEvent event, Duration delay);
}

/**
 * Spring事件发布器实现
 */
@Component
@Slf4j
public class SpringDomainEventPublisher implements DomainEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;
    private final EventStore eventStore;
    private final TaskExecutor taskExecutor;
    private final TaskScheduler taskScheduler;
    
    public SpringDomainEventPublisher(ApplicationEventPublisher eventPublisher,
                                    EventStore eventStore,
                                    @Qualifier("eventTaskExecutor") TaskExecutor taskExecutor,
                                    TaskScheduler taskScheduler) {
        this.eventPublisher = eventPublisher;
        this.eventStore = eventStore;
        this.taskExecutor = taskExecutor;
        this.taskScheduler = taskScheduler;
    }
    
    @Override
    @Transactional
    public void publish(DomainEvent event) {
        try {
            // 1. 存储事件
            eventStore.save(event);
            
            // 2. 发布事件
            DomainEventWrapper wrapper = new DomainEventWrapper(event);
            eventPublisher.publishEvent(wrapper);
            
            log.debug("领域事件发布成功: eventId={}, eventType={}", 
                    event.getEventId(), event.getEventType());
            
        } catch (Exception e) {
            log.error("领域事件发布失败: eventId={}, eventType={}", 
                    event.getEventId(), event.getEventType(), e);
            throw new EventPublishException("事件发布失败", e);
        }
    }
    
    @Override
    @Transactional
    public void publishAll(List<DomainEvent> events) {
        if (CollectionUtils.isEmpty(events)) {
            return;
        }
        
        try {
            // 1. 批量存储事件
            eventStore.saveAll(events);
            
            // 2. 批量发布事件
            events.forEach(event -> {
                DomainEventWrapper wrapper = new DomainEventWrapper(event);
                eventPublisher.publishEvent(wrapper);
            });
            
            log.debug("批量领域事件发布成功: count={}", events.size());
            
        } catch (Exception e) {
            log.error("批量领域事件发布失败: count={}", events.size(), e);
            throw new EventPublishException("批量事件发布失败", e);
        }
    }
    
    @Override
    public CompletableFuture<Void> publishAsync(DomainEvent event) {
        return CompletableFuture.runAsync(() -> publish(event), taskExecutor);
    }
    
    @Override
    public void publishDelayed(DomainEvent event, Duration delay) {
        Instant scheduledTime = Instant.now().plus(delay);
        taskScheduler.schedule(() -> publish(event), scheduledTime);
    }
}

/**
 * 事件包装器
 */
@Data
@AllArgsConstructor
public class DomainEventWrapper {
    private DomainEvent domainEvent;
    private LocalDateTime publishTime;
    
    public DomainEventWrapper(DomainEvent domainEvent) {
        this.domainEvent = domainEvent;
        this.publishTime = LocalDateTime.now(ZoneId.of("UTC"));
    }
}

/**
 * 事件发布异常
 */
public class EventPublishException extends RuntimeException {
    
    public EventPublishException(String message) {
        super(message);
    }
    
    public EventPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

#### 7.1.3 事件处理器

```java
/**
 * 用户事件处理器
 */
@Component
@Slf4j
@Transactional
public class UserEventHandler {
    
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final UserStatisticsService userStatisticsService;
    private final CacheManager cacheManager;
    
    public UserEventHandler(NotificationService notificationService,
                          AuditLogService auditLogService,
                          UserStatisticsService userStatisticsService,
                          CacheManager cacheManager) {
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.userStatisticsService = userStatisticsService;
        this.cacheManager = cacheManager;
    }
    
    /**
     * 处理用户创建事件
     */
    @EventListener
    @Async("eventTaskExecutor")
    public void handleUserCreated(DomainEventWrapper wrapper) {
        DomainEvent event = wrapper.getDomainEvent();
        if (!(event instanceof UserCreatedEvent)) {
            return;
        }
        
        UserCreatedEvent userCreatedEvent = (UserCreatedEvent) event;
        
        try {
            // 1. 发送欢迎通知
            sendWelcomeNotification(userCreatedEvent);
            
            // 2. 记录审计日志
            recordAuditLog(userCreatedEvent);
            
            // 3. 更新用户统计
            updateUserStatistics(userCreatedEvent);
            
            log.info("用户创建事件处理完成: userId={}, username={}", 
                    userCreatedEvent.getAggregateId(), userCreatedEvent.getUsername());
            
        } catch (Exception e) {
            log.error("用户创建事件处理失败: userId={}", userCreatedEvent.getAggregateId(), e);
            // 这里可以实现重试机制或者发送到死信队列
        }
    }
    
    /**
     * 处理用户状态变更事件
     */
    @EventListener
    @Async("eventTaskExecutor")
    public void handleUserStatusChanged(DomainEventWrapper wrapper) {
        DomainEvent event = wrapper.getDomainEvent();
        if (!(event instanceof UserStatusChangedEvent)) {
            return;
        }
        
        UserStatusChangedEvent statusChangedEvent = (UserStatusChangedEvent) event;
        
        try {
            // 1. 发送状态变更通知
            sendStatusChangeNotification(statusChangedEvent);
            
            // 2. 记录审计日志
            recordStatusChangeAuditLog(statusChangedEvent);
            
            // 3. 清理用户缓存
            clearUserCache(statusChangedEvent.getAggregateId());
            
            // 4. 更新统计信息
            updateStatusStatistics(statusChangedEvent);
            
            log.info("用户状态变更事件处理完成: userId={}, oldStatus={}, newStatus={}", 
                    statusChangedEvent.getAggregateId(), 
                    statusChangedEvent.getOldStatus(), 
                    statusChangedEvent.getNewStatus());
            
        } catch (Exception e) {
            log.error("用户状态变更事件处理失败: userId={}", statusChangedEvent.getAggregateId(), e);
        }
    }
    
    /**
     * 处理用户删除事件
     */
    @EventListener
    @Async("eventTaskExecutor")
    public void handleUserDeleted(DomainEventWrapper wrapper) {
        DomainEvent event = wrapper.getDomainEvent();
        if (!(event instanceof UserDeletedEvent)) {
            return;
        }
        
        UserDeletedEvent userDeletedEvent = (UserDeletedEvent) event;
        
        try {
            // 1. 清理用户相关数据
            cleanupUserData(userDeletedEvent.getAggregateId());
            
            // 2. 记录审计日志
            recordUserDeletionAuditLog(userDeletedEvent);
            
            // 3. 更新统计信息
            updateDeletionStatistics(userDeletedEvent);
            
            // 4. 清理缓存
            clearUserCache(userDeletedEvent.getAggregateId());
            
            log.info("用户删除事件处理完成: userId={}, username={}", 
                    userDeletedEvent.getAggregateId(), userDeletedEvent.getUsername());
            
        } catch (Exception e) {
            log.error("用户删除事件处理失败: userId={}", userDeletedEvent.getAggregateId(), e);
        }
    }
    
    private void sendWelcomeNotification(UserCreatedEvent event) {
        NotificationRequest request = NotificationRequest.builder()
                .userId(event.getAggregateId())
                .type(NotificationType.WELCOME)
                .title("欢迎加入")
                .content("欢迎您加入我们的平台！")
                .build();
        
        notificationService.send(request);
    }
    
    private void sendStatusChangeNotification(UserStatusChangedEvent event) {
        String content = String.format("您的账户状态已从 %s 变更为 %s", 
                event.getOldStatus(), event.getNewStatus());
        
        NotificationRequest request = NotificationRequest.builder()
                .userId(event.getAggregateId())
                .type(NotificationType.STATUS_CHANGE)
                .title("账户状态变更")
                .content(content)
                .build();
        
        notificationService.send(request);
    }
    
    private void recordAuditLog(UserCreatedEvent event) {
        AuditLog auditLog = AuditLog.builder()
                .userId(event.getAggregateId())
                .action("USER_CREATED")
                .resource("User")
                .resourceId(event.getAggregateId())
                .details(String.format("用户创建: username=%s, email=%s", 
                        event.getUsername(), event.getEmail()))
                .timestamp(event.getOccurredOn())
                .build();
        
        auditLogService.save(auditLog);
    }
    
    private void recordStatusChangeAuditLog(UserStatusChangedEvent event) {
        AuditLog auditLog = AuditLog.builder()
                .userId(event.getAggregateId())
                .action("USER_STATUS_CHANGED")
                .resource("User")
                .resourceId(event.getAggregateId())
                .details(String.format("用户状态变更: %s -> %s, 原因: %s", 
                        event.getOldStatus(), event.getNewStatus(), event.getReason()))
                .timestamp(event.getOccurredOn())
                .build();
        
        auditLogService.save(auditLog);
    }
    
    private void recordUserDeletionAuditLog(UserDeletedEvent event) {
        AuditLog auditLog = AuditLog.builder()
                .userId(event.getAggregateId())
                .action("USER_DELETED")
                .resource("User")
                .resourceId(event.getAggregateId())
                .details(String.format("用户删除: username=%s, 原因: %s", 
                        event.getUsername(), event.getReason()))
                .timestamp(event.getOccurredOn())
                .build();
        
        auditLogService.save(auditLog);
    }
    
    private void updateUserStatistics(UserCreatedEvent event) {
        userStatisticsService.incrementUserCount();
        userStatisticsService.incrementActiveUserCount();
    }
    
    private void updateStatusStatistics(UserStatusChangedEvent event) {
        if (event.getOldStatus() == UserStatus.ACTIVE && event.getNewStatus() != UserStatus.ACTIVE) {
            userStatisticsService.decrementActiveUserCount();
        } else if (event.getOldStatus() != UserStatus.ACTIVE && event.getNewStatus() == UserStatus.ACTIVE) {
            userStatisticsService.incrementActiveUserCount();
        }
    }
    
    private void updateDeletionStatistics(UserDeletedEvent event) {
        userStatisticsService.decrementUserCount();
    }
    
    private void cleanupUserData(String userId) {
        // 清理用户相关的业务数据
        // 例如：用户的订单、评论、收藏等
        log.info("清理用户数据: userId={}", userId);
    }
    
    private void clearUserCache(String userId) {
        Cache userCache = cacheManager.getCache("users");
        if (userCache != null) {
            userCache.evict(userId);
        }
        
        Cache userProfileCache = cacheManager.getCache("userProfiles");
        if (userProfileCache != null) {
            userProfileCache.evict(userId);
        }
    }
### 7.2 CQRS模式实现

#### 7.2.1 命令查询分离

```java
/**
 * 命令接口
 */
public interface Command {
    /**
     * 获取命令ID
     */
    String getCommandId();
    
    /**
     * 获取聚合根ID
     */
    String getAggregateId();
    
    /**
     * 获取命令时间戳
     */
    LocalDateTime getTimestamp();
}

/**
 * 查询接口
 */
public interface Query {
    /**
     * 获取查询ID
     */
    String getQueryId();
    
    /**
     * 获取查询时间戳
     */
    LocalDateTime getTimestamp();
}

/**
 * 命令基类
 */
@Data
public abstract class BaseCommand implements Command {
    
    private String commandId;
    private String aggregateId;
    private LocalDateTime timestamp;
    
    protected BaseCommand() {
        this.commandId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now(ZoneId.of("UTC"));
    }
    
    protected BaseCommand(String aggregateId) {
        this();
        this.aggregateId = aggregateId;
    }
}

/**
 * 查询基类
 */
@Data
public abstract class BaseQuery implements Query {
    
    private String queryId;
    private LocalDateTime timestamp;
    
    protected BaseQuery() {
        this.queryId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now(ZoneId.of("UTC"));
    }
}

/**
 * 用户命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateUserCommand extends BaseCommand {
    
    private String username;
    private String email;
    private String password;
    private String fullName;
    
    public CreateUserCommand(String username, String email, String password, String fullName) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateUserCommand extends BaseCommand {
    
    private String email;
    private String fullName;
    
    public UpdateUserCommand(String userId, String email, String fullName) {
        super(userId);
        this.email = email;
        this.fullName = fullName;
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
public class ChangeUserStatusCommand extends BaseCommand {
    
    private UserStatus newStatus;
    private String reason;
    
    public ChangeUserStatusCommand(String userId, UserStatus newStatus, String reason) {
        super(userId);
        this.newStatus = newStatus;
        this.reason = reason;
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
public class DeleteUserCommand extends BaseCommand {
    
    private String reason;
    
    public DeleteUserCommand(String userId, String reason) {
        super(userId);
        this.reason = reason;
    }
}

/**
 * 用户查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GetUserByIdQuery extends BaseQuery {
    
    private String userId;
    
    public GetUserByIdQuery(String userId) {
        super();
        this.userId = userId;
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
public class GetUserByUsernameQuery extends BaseQuery {
    
    private String username;
    
    public GetUserByUsernameQuery(String username) {
        super();
        this.username = username;
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
public class SearchUsersQuery extends BaseQuery {
    
    private String keyword;
    private UserStatus status;
    private int page;
    private int size;
    private String sortBy;
    private String sortDirection;
    
    public SearchUsersQuery(String keyword, UserStatus status, int page, int size, String sortBy, String sortDirection) {
        super();
        this.keyword = keyword;
        this.status = status;
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }
}
```

#### 7.2.2 命令处理器

```java
/**
 * 命令处理器接口
 */
public interface CommandHandler<T extends Command, R> {
    
    /**
     * 处理命令
     */
    R handle(T command);
    
    /**
     * 获取支持的命令类型
     */
    Class<T> getCommandType();
}

/**
 * 用户命令处理器
 */
@Component
@Slf4j
@Transactional
public class UserCommandHandler implements 
        CommandHandler<CreateUserCommand, String>,
        CommandHandler<UpdateUserCommand, Void>,
        CommandHandler<ChangeUserStatusCommand, Void>,
        CommandHandler<DeleteUserCommand, Void> {
    
    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final DomainEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    
    public UserCommandHandler(UserRepository userRepository,
                            UserDomainService userDomainService,
                            DomainEventPublisher eventPublisher,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * 处理创建用户命令
     */
    public String handle(CreateUserCommand command) {
        log.info("处理创建用户命令: username={}", command.getUsername());
        
        // 1. 验证用户名唯一性
        if (userRepository.existsByUsername(command.getUsername())) {
            throw UserException.userAlreadyExists(command.getUsername());
        }
        
        // 2. 验证邮箱唯一性
        if (userRepository.existsByEmail(command.getEmail())) {
            throw new UserException("EMAIL_ALREADY_EXISTS", "user.email.already.exists", command.getEmail());
        }
        
        // 3. 创建用户聚合
        String userId = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(command.getPassword());
        
        User user = userDomainService.createUser(
                userId,
                command.getUsername(),
                command.getEmail(),
                encodedPassword,
                command.getFullName()
        );
        
        // 4. 保存用户
        userRepository.save(user);
        
        // 5. 发布领域事件
        UserCreatedEvent event = new UserCreatedEvent(
                userId,
                command.getUsername(),
                command.getEmail(),
                command.getFullName()
        );
        eventPublisher.publish(event);
        
        log.info("用户创建成功: userId={}, username={}", userId, command.getUsername());
        return userId;
    }
    
    /**
     * 处理更新用户命令
     */
    public Void handle(UpdateUserCommand command) {
        log.info("处理更新用户命令: userId={}", command.getAggregateId());
        
        // 1. 获取用户
        User user = userRepository.findById(command.getAggregateId())
                .orElseThrow(() -> UserException.userNotFound(command.getAggregateId()));
        
        // 2. 验证邮箱唯一性（如果邮箱发生变化）
        if (!user.getEmail().equals(command.getEmail()) && 
            userRepository.existsByEmail(command.getEmail())) {
            throw new UserException("EMAIL_ALREADY_EXISTS", "user.email.already.exists", command.getEmail());
        }
        
        // 3. 更新用户信息
        user.updateProfile(command.getEmail(), command.getFullName());
        
        // 4. 保存用户
        userRepository.save(user);
        
        // 5. 发布领域事件
        UserUpdatedEvent event = new UserUpdatedEvent(
                command.getAggregateId(),
                command.getEmail(),
                command.getFullName()
        );
        eventPublisher.publish(event);
        
        log.info("用户更新成功: userId={}", command.getAggregateId());
        return null;
    }
    
    /**
     * 处理用户状态变更命令
     */
    public Void handle(ChangeUserStatusCommand command) {
        log.info("处理用户状态变更命令: userId={}, newStatus={}", 
                command.getAggregateId(), command.getNewStatus());
        
        // 1. 获取用户
        User user = userRepository.findById(command.getAggregateId())
                .orElseThrow(() -> UserException.userNotFound(command.getAggregateId()));
        
        // 2. 记录旧状态
        UserStatus oldStatus = user.getStatus();
        
        // 3. 变更状态
        user.changeStatus(command.getNewStatus(), command.getReason());
        
        // 4. 保存用户
        userRepository.save(user);
        
        // 5. 发布领域事件
        UserStatusChangedEvent event = new UserStatusChangedEvent(
                command.getAggregateId(),
                oldStatus,
                command.getNewStatus(),
                command.getReason()
        );
        eventPublisher.publish(event);
        
        log.info("用户状态变更成功: userId={}, oldStatus={}, newStatus={}", 
                command.getAggregateId(), oldStatus, command.getNewStatus());
        return null;
    }
    
    /**
     * 处理删除用户命令
     */
    public Void handle(DeleteUserCommand command) {
        log.info("处理删除用户命令: userId={}", command.getAggregateId());
        
        // 1. 获取用户
        User user = userRepository.findById(command.getAggregateId())
                .orElseThrow(() -> UserException.userNotFound(command.getAggregateId()));
        
        // 2. 检查是否可以删除
        if (!userDomainService.canDeleteUser(user)) {
            throw new UserException("USER_CANNOT_DELETE", "user.cannot.delete", command.getAggregateId());
        }
        
        // 3. 记录用户信息
        String username = user.getUsername();
        
        // 4. 删除用户
        userRepository.delete(user);
        
        // 5. 发布领域事件
        UserDeletedEvent event = new UserDeletedEvent(
                command.getAggregateId(),
                username,
                command.getReason()
        );
        eventPublisher.publish(event);
        
        log.info("用户删除成功: userId={}, username={}", command.getAggregateId(), username);
        return null;
    }
    
    @Override
    public Class<CreateUserCommand> getCommandType() {
        return CreateUserCommand.class;
    }
}
```

#### 7.2.3 查询处理器

```java
/**
 * 查询处理器接口
 */
public interface QueryHandler<T extends Query, R> {
    
    /**
     * 处理查询
     */
    R handle(T query);
    
    /**
     * 获取支持的查询类型
     */
    Class<T> getQueryType();
}

/**
 * 用户查询处理器
 */
@Component
@Slf4j
@Transactional(readOnly = true)
public class UserQueryHandler implements 
        QueryHandler<GetUserByIdQuery, UserDTO>,
        QueryHandler<GetUserByUsernameQuery, UserDTO>,
        QueryHandler<SearchUsersQuery, PageResponse<UserDTO>> {
    
    private final UserQueryRepository userQueryRepository;
    private final UserConverter userConverter;
    private final CacheManager cacheManager;
    
    public UserQueryHandler(UserQueryRepository userQueryRepository,
                          UserConverter userConverter,
                          CacheManager cacheManager) {
        this.userQueryRepository = userQueryRepository;
        this.userConverter = userConverter;
        this.cacheManager = cacheManager;
    }
    
    /**
     * 根据ID查询用户
     */
    @Cacheable(value = "users", key = "#query.userId")
    public UserDTO handle(GetUserByIdQuery query) {
        log.debug("处理根据ID查询用户: userId={}", query.getUserId());
        
        UserPO userPO = userQueryRepository.findById(query.getUserId())
                .orElseThrow(() -> UserException.userNotFound(query.getUserId()));
        
        return userConverter.toDTO(userPO);
    }
    
    /**
     * 根据用户名查询用户
     */
    @Cacheable(value = "users", key = "'username:' + #query.username")
    public UserDTO handle(GetUserByUsernameQuery query) {
        log.debug("处理根据用户名查询用户: username={}", query.getUsername());
        
        UserPO userPO = userQueryRepository.findByUsername(query.getUsername())
                .orElseThrow(() -> UserException.userNotFound(query.getUsername()));
        
        return userConverter.toDTO(userPO);
    }
    
    /**
     * 搜索用户
     */
    public PageResponse<UserDTO> handle(SearchUsersQuery query) {
        log.debug("处理搜索用户: keyword={}, status={}, page={}, size={}", 
                query.getKeyword(), query.getStatus(), query.getPage(), query.getSize());
        
        // 构建查询条件
        UserSearchCriteria criteria = UserSearchCriteria.builder()
                .keyword(query.getKeyword())
                .status(query.getStatus())
                .build();
        
        // 构建分页参数
        Pageable pageable = PageRequest.of(
                query.getPage(),
                query.getSize(),
                Sort.by(Sort.Direction.fromString(query.getSortDirection()), query.getSortBy())
        );
        
        // 执行查询
        Page<UserPO> userPage = userQueryRepository.search(criteria, pageable);
        
        // 转换结果
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(userConverter::toDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<UserDTO>builder()
                .content(userDTOs)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();
    }
    
    @Override
    public Class<GetUserByIdQuery> getQueryType() {
        return GetUserByIdQuery.class;
    }
}

/**
 * 用户查询仓储
 */
public interface UserQueryRepository extends JpaRepository<UserPO, String> {
    
    /**
     * 根据用户名查询
     */
    Optional<UserPO> findByUsername(String username);
    
    /**
     * 根据邮箱查询
     */
    Optional<UserPO> findByEmail(String email);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 根据状态查询用户数量
     */
    long countByStatus(UserStatus status);
    
    /**
     * 搜索用户
     */
    @Query("SELECT u FROM UserPO u WHERE " +
           "(:#{#criteria.keyword} IS NULL OR " +
           " u.username LIKE %:#{#criteria.keyword}% OR " +
           " u.email LIKE %:#{#criteria.keyword}% OR " +
           " u.fullName LIKE %:#{#criteria.keyword}%) AND " +
           "(:#{#criteria.status} IS NULL OR u.status = :#{#criteria.status})")
    Page<UserPO> search(@Param("criteria") UserSearchCriteria criteria, Pageable pageable);
}

/**
 * 用户搜索条件
 */
@Data
@Builder
public class UserSearchCriteria {
    private String keyword;
    private UserStatus status;
}
```

#### 7.2.4 命令查询总线

```java
/**
 * 命令总线接口
 */
public interface CommandBus {
    
    /**
     * 发送命令
     */
    <R> R send(Command command);
    
    /**
     * 异步发送命令
     */
    <R> CompletableFuture<R> sendAsync(Command command);
}

/**
 * 查询总线接口
 */
public interface QueryBus {
    
    /**
     * 发送查询
     */
    <R> R send(Query query);
    
    /**
     * 异步发送查询
     */
    <R> CompletableFuture<R> sendAsync(Query query);
}

/**
 * 命令总线实现
 */
@Component
@Slf4j
public class SpringCommandBus implements CommandBus {
    
    private final Map<Class<? extends Command>, CommandHandler<?, ?>> handlers = new HashMap<>();
    private final TaskExecutor taskExecutor;
    
    public SpringCommandBus(@Qualifier("commandTaskExecutor") TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }
    
    /**
     * 注册命令处理器
     */
    @EventListener
    public void registerHandlers(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        
        // 获取所有命令处理器
        Map<String, CommandHandler> handlerBeans = context.getBeansOfType(CommandHandler.class);
        
        handlerBeans.values().forEach(handler -> {
            Class<? extends Command> commandType = handler.getCommandType();
            handlers.put(commandType, handler);
            log.info("注册命令处理器: {} -> {}", commandType.getSimpleName(), handler.getClass().getSimpleName());
        });
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <R> R send(Command command) {
        log.debug("发送命令: {}", command.getClass().getSimpleName());
        
        CommandHandler<Command, R> handler = (CommandHandler<Command, R>) handlers.get(command.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("未找到命令处理器: " + command.getClass().getSimpleName());
        }
        
        try {
            R result = handler.handle(command);
            log.debug("命令处理完成: {}", command.getClass().getSimpleName());
            return result;
        } catch (Exception e) {
            log.error("命令处理失败: {}", command.getClass().getSimpleName(), e);
            throw e;
        }
    }
    
    @Override
    public <R> CompletableFuture<R> sendAsync(Command command) {
        return CompletableFuture.supplyAsync(() -> send(command), taskExecutor);
    }
}

/**
 * 查询总线实现
 */
@Component
@Slf4j
public class SpringQueryBus implements QueryBus {
    
    private final Map<Class<? extends Query>, QueryHandler<?, ?>> handlers = new HashMap<>();
    private final TaskExecutor taskExecutor;
    
    public SpringQueryBus(@Qualifier("queryTaskExecutor") TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }
    
    /**
     * 注册查询处理器
     */
    @EventListener
    public void registerHandlers(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        
        // 获取所有查询处理器
        Map<String, QueryHandler> handlerBeans = context.getBeansOfType(QueryHandler.class);
        
        handlerBeans.values().forEach(handler -> {
            Class<? extends Query> queryType = handler.getQueryType();
            handlers.put(queryType, handler);
            log.info("注册查询处理器: {} -> {}", queryType.getSimpleName(), handler.getClass().getSimpleName());
        });
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <R> R send(Query query) {
        log.debug("发送查询: {}", query.getClass().getSimpleName());
        
        QueryHandler<Query, R> handler = (QueryHandler<Query, R>) handlers.get(query.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("未找到查询处理器: " + query.getClass().getSimpleName());
        }
        
        try {
            R result = handler.handle(query);
            log.debug("查询处理完成: {}", query.getClass().getSimpleName());
            return result;
        } catch (Exception e) {
            log.error("查询处理失败: {}", query.getClass().getSimpleName(), e);
            throw e;
        }
    }
    
    @Override
    public <R> CompletableFuture<R> sendAsync(Query query) {
        return CompletableFuture.supplyAsync(() -> send(query), taskExecutor);
    }
}
```

    /** 事件ID */
    private final String eventId = UUID.randomUUID().toString();
    
    /** 聚合根ID */
    private final String aggregateId;
    
    /** 事件发生时间 */
    private final Instant occurredOn = Instant.now();
    
    /** 事件版本 */
    private final Integer version;
    
    /** 事件类型 */
    public abstract String getEventType();
}

/**
 * 用户创建事件
 */
  @Data
  @EqualsAndHashCode(callSuper = true)
  public class UserCreatedEvent extends DomainEvent {
    
    private final String username;
    private final String email;
    private final UserStatus status;
    
    public UserCreatedEvent(String aggregateId, Integer version, 
                           String username, String email, UserStatus status) {
        super(aggregateId, version);
        this.username = username;
        this.email = email;
        this.status = status;
    }
    
    @Override
    public String getEventType() {
        return "UserCreated";
    }
  }

/**
 * 聚合根基类
 */
   @MappedSuperclass
   public abstract class AggregateRoot {
    
    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    /**
     * 添加领域事件
     */
      protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
      }
    
    /**
     * 获取并清空领域事件
     */
      public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
      }
    
    /**
     * 清空领域事件
     */
       public void clearDomainEvents() {
        this.domainEvents.clear();
       }
      }
```

#### 事件发布机制

```java
/**
 * 领域事件发布器
 */
@Component
@Slf4j
public class DomainEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;
    private final EventStore eventStore;
    
    public DomainEventPublisher(ApplicationEventPublisher eventPublisher, 
                               EventStore eventStore) {
        this.eventPublisher = eventPublisher;
        this.eventStore = eventStore;
    }
    
    /**
     * 发布领域事件
     */
    public void publish(DomainEvent event) {
        try {
            // 1. 持久化事件
            eventStore.save(event);
            
            // 2. 发布事件
            eventPublisher.publishEvent(event);
            
            log.info("领域事件发布成功: {}", event.getEventType());
            
        } catch (Exception e) {
            log.error("领域事件发布失败: {}", event.getEventType(), e);
            throw new DomainEventException("事件发布失败", e);
        }
    }
    
    /**
     * 批量发布事件
     */
    public void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}

/**
 * 事件存储接口
 */
public interface EventStore {
    
    /**
     * 保存事件
     */
    void save(DomainEvent event);
    
    /**
     * 根据聚合ID查询事件
     */
    List<DomainEvent> findByAggregateId(String aggregateId);
    
    /**
     * 根据事件类型查询事件
     */
    List<DomainEvent> findByEventType(String eventType);
    
    /**
     * 查询指定时间范围内的事件
     */
    List<DomainEvent> findByTimeRange(Instant start, Instant end);
}

/**
 * 事件存储实现
 */
@Repository
public class EventStoreImpl implements EventStore {
    
    private final EventStoreMapper eventStoreMapper;
    private final ObjectMapper objectMapper;
    
    @Override
    public void save(DomainEvent event) {
        try {
            EventStorePO eventPO = EventStorePO.builder()
                .eventId(event.getEventId())
                .aggregateId(event.getAggregateId())
                .eventType(event.getEventType())
                .eventData(objectMapper.writeValueAsString(event))
                .occurredOn(event.getOccurredOn())
                .version(event.getVersion())
                .build();
                
            eventStoreMapper.insert(eventPO);
            
        } catch (Exception e) {
            throw new EventStoreException("事件存储失败", e);
        }
    }
    
    @Override
    public List<DomainEvent> findByAggregateId(String aggregateId) {
        List<EventStorePO> events = eventStoreMapper.selectByAggregateId(aggregateId);
        return events.stream()
                    .map(this::toDomainEvent)
                    .collect(Collectors.toList());
    }
    
    private DomainEvent toDomainEvent(EventStorePO eventPO) {
        try {
            Class<?> eventClass = Class.forName(getEventClassName(eventPO.getEventType()));
            return (DomainEvent) objectMapper.readValue(eventPO.getEventData(), eventClass);
    }
}
```

#### 7.2.5 CQRS配置

```java
/**
 * CQRS配置
 */
@Configuration
@EnableAsync
@Slf4j
public class CqrsConfig {
    
    /**
     * 命令执行器
     */
    @Bean("commandTaskExecutor")
    public TaskExecutor commandTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Command-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    /**
     * 查询执行器
     */
    @Bean("queryTaskExecutor")
    public TaskExecutor queryTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Query-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

/**
 * 应用层使用CQRS
 */
@Service
@Slf4j
@Transactional
public class UserApplicationService {
    
    private final CommandBus commandBus;
    private final QueryBus queryBus;
    
    public UserApplicationService(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }
    
    /**
     * 创建用户
     */
    public String createUser(CreateUserRequest request) {
        CreateUserCommand command = new CreateUserCommand(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFullName()
        );
        return commandBus.send(command);
    }
    
    /**
     * 查询用户
     */
    public UserDTO getUserById(String userId) {
        GetUserByIdQuery query = new GetUserByIdQuery(userId);
        return queryBus.send(query);
    }
}
```

## 7. 事件发布与事务管理

### 7.1 事务管理配置

#### 7.1.1 事务管理器配置

```java
/**
 * 事务管理配置
 */
@Configuration
@EnableTransactionManagement
@Slf4j
public class TransactionConfig {
    
    /**
     * 数据源事务管理器
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionManager.setDefaultTimeout(30); // 默认超时30秒
        return transactionManager;
    }
    
    /**
     * 事务模板
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setTimeout(30);
        return template;
    }
    
    /**
     * 事务拦截器
     */
    @Bean
    public TransactionInterceptor transactionInterceptor(PlatformTransactionManager transactionManager) {
        Properties transactionAttributes = new Properties();
        
        // 查询方法配置为只读事务
        transactionAttributes.setProperty("get*", "PROPAGATION_REQUIRED,readOnly");
        transactionAttributes.setProperty("find*", "PROPAGATION_REQUIRED,readOnly");
        transactionAttributes.setProperty("query*", "PROPAGATION_REQUIRED,readOnly");
        transactionAttributes.setProperty("list*", "PROPAGATION_REQUIRED,readOnly");
        transactionAttributes.setProperty("count*", "PROPAGATION_REQUIRED,readOnly");
        transactionAttributes.setProperty("exists*", "PROPAGATION_REQUIRED,readOnly");
        
        // 修改方法配置为读写事务
        transactionAttributes.setProperty("save*", "PROPAGATION_REQUIRED");
        transactionAttributes.setProperty("insert*", "PROPAGATION_REQUIRED");
        transactionAttributes.setProperty("update*", "PROPAGATION_REQUIRED");
        transactionAttributes.setProperty("delete*", "PROPAGATION_REQUIRED");
        transactionAttributes.setProperty("remove*", "PROPAGATION_REQUIRED");
        transactionAttributes.setProperty("create*", "PROPAGATION_REQUIRED");
        transactionAttributes.setProperty("modify*", "PROPAGATION_REQUIRED");
        
        // 其他方法默认配置
        transactionAttributes.setProperty("*", "PROPAGATION_REQUIRED");
        
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(transactionManager);
        interceptor.setTransactionAttributes(transactionAttributes);
        
        return interceptor;
    }
}
```

#### 7.1.2 分布式事务配置

```java
/**
 * 分布式事务配置
 */
@Configuration
@ConditionalOnProperty(name = "spring.transaction.distributed.enabled", havingValue = "true")
@Slf4j
public class DistributedTransactionConfig {
    
    /**
     * Seata数据源代理
     */
    @Bean
    @Primary
    public DataSource dataSourceProxy(DataSource dataSource) {
        return new DataSourceProxy(dataSource);
    }
    
    /**
     * Seata全局事务扫描器
     */
    @Bean
    public GlobalTransactionScanner globalTransactionScanner() {
        return new GlobalTransactionScanner("rose-monolithic", "default");
    }
    
    /**
     * 分布式事务模板
     */
    @Bean
    public GlobalTransactionTemplate globalTransactionTemplate() {
        return new GlobalTransactionTemplate();
    }
}
```

### 7.2 领域事件发布机制

#### 7.2.1 事件发布器接口

```java
/**
 * 领域事件发布器接口
 */
public interface DomainEventPublisher {
    
    /**
     * 发布单个事件
     */
    void publish(DomainEvent event);
    
    /**
     * 批量发布事件
     */
    void publishAll(List<DomainEvent> events);
    
    /**
     * 异步发布事件
     */
    CompletableFuture<Void> publishAsync(DomainEvent event);
    
    /**
     * 延迟发布事件
     */
    void publishDelayed(DomainEvent event, Duration delay);
}
```

#### 7.2.2 事件发布器实现

```java
/**
 * Spring事件发布器实现
 */
@Component
@Slf4j
public class SpringDomainEventPublisher implements DomainEventPublisher {
    
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    
    @Autowired
    private TaskExecutor taskExecutor;
    
    @Autowired
    private TaskScheduler taskScheduler;
    
    @Override
    public void publish(DomainEvent event) {
        log.info("发布领域事件: {}", event.getClass().getSimpleName());
        
        try {
            // 包装为Spring事件
            DomainEventWrapper wrapper = new DomainEventWrapper(event);
            applicationEventPublisher.publishEvent(wrapper);
            
            log.info("领域事件发布成功: eventId={}", event.getEventId());
        } catch (Exception e) {
            log.error("领域事件发布失败: eventId={}", event.getEventId(), e);
            throw new BusinessException("EVENT_PUBLISH_FAILED", "event.publish.failed", e.getMessage());
        }
    }
    
    @Override
    public void publishAll(List<DomainEvent> events) {
        log.info("批量发布领域事件: count={}", events.size());
        
        events.forEach(this::publish);
    }
    
    @Override
    public CompletableFuture<Void> publishAsync(DomainEvent event) {
        log.info("异步发布领域事件: {}", event.getClass().getSimpleName());
        
        return CompletableFuture.runAsync(() -> publish(event), taskExecutor);
    }
    
    @Override
    public void publishDelayed(DomainEvent event, Duration delay) {
        log.info("延迟发布领域事件: {}, delay={}ms", 
                event.getClass().getSimpleName(), delay.toMillis());
        
        taskScheduler.schedule(
            () -> publish(event),
            Instant.now().plus(delay)
        );
    }
}
```

#### 7.2.3 事件包装器

```java
/**
 * 领域事件包装器
 */
@Data
@AllArgsConstructor
public class DomainEventWrapper {
    
    private DomainEvent domainEvent;
    
    private LocalDateTime publishTime;
    
    public DomainEventWrapper(DomainEvent domainEvent) {
        this.domainEvent = domainEvent;
        this.publishTime = LocalDateTime.now();
    }
}
```

### 7.3 事件处理器

#### 7.3.1 用户事件处理器

```java
/**
 * 用户事件处理器
 */
@Component
@Slf4j
public class UserEventHandler {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private UserStatisticsService userStatisticsService;
    
    /**
     * 处理用户创建事件
     */
    @EventListener
    @Async("eventExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserCreated(DomainEventWrapper wrapper) {
        if (!(wrapper.getDomainEvent() instanceof UserCreatedEvent)) {
            return;
        }
        
        UserCreatedEvent event = (UserCreatedEvent) wrapper.getDomainEvent();
        log.info("处理用户创建事件: userId={}", event.getUserId());
        
        try {
            // 1. 发送欢迎通知
            sendWelcomeNotification(event.getUserId());
            
            // 2. 记录审计日志
            recordAuditLog(event);
            
            // 3. 更新用户统计
            updateUserStatistics(event);
            
            log.info("用户创建事件处理完成: userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("用户创建事件处理失败: userId={}", event.getUserId(), e);
            // 这里可以选择重试或者记录失败日志
        }
    }
    
    /**
     * 处理用户更新事件
     */
    @EventListener
    @Async("eventExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserUpdated(DomainEventWrapper wrapper) {
        if (!(wrapper.getDomainEvent() instanceof UserUpdatedEvent)) {
            return;
        }
        
        UserUpdatedEvent event = (UserUpdatedEvent) wrapper.getDomainEvent();
        log.info("处理用户更新事件: userId={}", event.getUserId());
        
        try {
            // 1. 记录审计日志
            recordAuditLog(event);
            
            // 2. 清除相关缓存
            clearUserCache(event.getUserId());
            
            log.info("用户更新事件处理完成: userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("用户更新事件处理失败: userId={}", event.getUserId(), e);
        }
    }
    
    /**
     * 处理用户删除事件
     */
    @EventListener
    @Async("eventExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserDeleted(DomainEventWrapper wrapper) {
        if (!(wrapper.getDomainEvent() instanceof UserDeletedEvent)) {
            return;
        }
        
        UserDeletedEvent event = (UserDeletedEvent) wrapper.getDomainEvent();
        log.info("处理用户删除事件: userId={}", event.getUserId());
        
        try {
            // 1. 清理用户相关数据
            cleanupUserData(event.getUserId());
            
            // 2. 记录审计日志
            recordAuditLog(event);
            
            // 3. 更新用户统计
            updateUserStatistics(event);
            
            log.info("用户删除事件处理完成: userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("用户删除事件处理失败: userId={}", event.getUserId(), e);
        }
    }
    
    /**
     * 发送欢迎通知
     */
    private void sendWelcomeNotification(String userId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .type(NotificationType.WELCOME)
            .title("欢迎加入")
            .content("欢迎您加入我们的平台！")
            .build();
        
        notificationService.send(request);
    }
    
    /**
     * 记录审计日志
     */
    private void recordAuditLog(DomainEvent event) {
        AuditLog auditLog = AuditLog.builder()
            .eventType(event.getClass().getSimpleName())
            .eventId(event.getEventId())
            .occurredOn(event.getOccurredOn())
            .details(JsonUtils.toJson(event))
            .build();
        
        auditLogService.save(auditLog);
    }
    
    /**
     * 更新用户统计
     */
    private void updateUserStatistics(DomainEvent event) {
        if (event instanceof UserCreatedEvent) {
            userStatisticsService.incrementUserCount();
        } else if (event instanceof UserDeletedEvent) {
            userStatisticsService.decrementUserCount();
        }
    }
    
    /**
     * 清除用户缓存
     */
    private void clearUserCache(String userId) {
        // 清除用户相关的缓存
        log.info("清除用户缓存: userId={}", userId);
    }
    
    /**
     * 清理用户数据
     */
    private void cleanupUserData(String userId) {
        // 清理用户相关的数据
        log.info("清理用户数据: userId={}", userId);
    }
}
```

### 7.4 事务事件处理

#### 7.4.1 事务事件监听器

```java
/**
 * 事务事件监听器
 */
@Component
@Slf4j
public class TransactionEventListener {
    
    @Autowired
    private DomainEventPublisher eventPublisher;
    
    /**
     * 事务提交后发布事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAfterCommit(DomainEventWrapper wrapper) {
        DomainEvent event = wrapper.getDomainEvent();
        log.info("事务提交后处理事件: {}", event.getClass().getSimpleName());
        
        // 在这里可以执行一些需要在事务提交后才能执行的操作
        // 比如发送消息队列、调用外部服务等
    }
    
    /**
     * 事务回滚后处理
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleAfterRollback(DomainEventWrapper wrapper) {
        DomainEvent event = wrapper.getDomainEvent();
        log.warn("事务回滚后处理事件: {}", event.getClass().getSimpleName());
        
        // 在这里可以执行一些补偿操作
    }
    
    /**
     * 事务完成后处理（无论提交还是回滚）
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleAfterCompletion(DomainEventWrapper wrapper) {
        DomainEvent event = wrapper.getDomainEvent();
        log.info("事务完成后处理事件: {}", event.getClass().getSimpleName());
        
        // 在这里可以执行一些清理操作
    }
}
```

### 7.5 事件存储

#### 7.5.1 事件存储接口

```java
/**
 * 事件存储接口
 */
public interface EventStore {
    
    /**
     * 保存事件
     */
    void save(DomainEvent event);
    
    /**
     * 批量保存事件
     */
    void saveAll(List<DomainEvent> events);
    
    /**
     * 根据聚合ID查询事件
     */
    List<DomainEvent> findByAggregateId(String aggregateId);
    
    /**
     * 根据事件类型查询事件
     */
    List<DomainEvent> findByEventType(String eventType);
    
    /**
     * 分页查询事件
     */
    PageResponse<DomainEvent> findEvents(EventQueryRequest request, PageRequest pageRequest);
}
```

#### 7.5.2 事件存储实现

```java
/**
 * 事件存储实现
 */
@Repository
@Slf4j
public class EventStoreImpl implements EventStore {
    
    @Autowired
    private EventMapper eventMapper;
    
    @Override
    public void save(DomainEvent event) {
        log.info("保存领域事件: eventId={}", event.getEventId());
        
        EventPO eventPO = EventConverter.toPO(event);
        eventMapper.insert(eventPO);
    }
    
    @Override
    public void saveAll(List<DomainEvent> events) {
        log.info("批量保存领域事件: count={}", events.size());
        
        List<EventPO> eventPOs = events.stream()
            .map(EventConverter::toPO)
            .collect(Collectors.toList());
        
        eventMapper.insertBatch(eventPOs);
    }
    
    @Override
    public List<DomainEvent> findByAggregateId(String aggregateId) {
        log.info("根据聚合ID查询事件: aggregateId={}", aggregateId);
        
        LambdaQueryWrapper<EventPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventPO::getAggregateId, aggregateId)
               .orderByAsc(EventPO::getOccurredOn);
        
        List<EventPO> eventPOs = eventMapper.selectList(wrapper);
        
        return eventPOs.stream()
            .map(EventConverter::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<DomainEvent> findByEventType(String eventType) {
        log.info("根据事件类型查询事件: eventType={}", eventType);
        
        LambdaQueryWrapper<EventPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventPO::getEventType, eventType)
               .orderByDesc(EventPO::getOccurredOn);
        
        List<EventPO> eventPOs = eventMapper.selectList(wrapper);
        
        return eventPOs.stream()
            .map(EventConverter::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public PageResponse<DomainEvent> findEvents(EventQueryRequest request, PageRequest pageRequest) {
        log.info("分页查询事件: request={}", request);
        
        LambdaQueryWrapper<EventPO> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(request.getAggregateId())) {
            wrapper.eq(EventPO::getAggregateId, request.getAggregateId());
        }
        
        if (StringUtils.hasText(request.getEventType())) {
            wrapper.eq(EventPO::getEventType, request.getEventType());
        }
        
        if (request.getOccurredOnStart() != null) {
            wrapper.ge(EventPO::getOccurredOn, request.getOccurredOnStart());
        }
        
        if (request.getOccurredOnEnd() != null) {
            wrapper.le(EventPO::getOccurredOn, request.getOccurredOnEnd());
        }
        
        wrapper.orderByDesc(EventPO::getOccurredOn);
        
        Page<EventPO> page = eventMapper.selectPage(pageRequest.toPage(), wrapper);
        
        List<DomainEvent> events = page.getRecords().stream()
            .map(EventConverter::toDomain)
            .collect(Collectors.toList());
        
        return PageResponse.<DomainEvent>builder()
            .current(page.getCurrent())
            .size(page.getSize())
            .total(page.getTotal())
            .pages(page.getPages())
            .records(events)
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}
```

### 7.6 事件重试机制

#### 7.6.1 事件重试配置

```java
/**
 * 事件重试配置
 */
@Configuration
@EnableRetry
@Slf4j
public class EventRetryConfig {
    
    /**
     * 重试模板
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // 重试策略：最多重试3次
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // 退避策略：指数退避，初始间隔1秒，最大间隔10秒
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMaxInterval(10000);
        backOffPolicy.setMultiplier(2.0);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // 重试监听器
        retryTemplate.registerListener(new RetryListenerSupport() {
            @Override
            public <T, E extends Throwable> void onError(
                    RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                log.warn("事件处理重试: attempt={}, error={}", 
                        context.getRetryCount(), throwable.getMessage());
            }
        });
        
        return retryTemplate;
    }
}
```

#### 7.6.2 可重试的事件处理器

```java
/**
 * 可重试的事件处理器
 */
@Component
@Slf4j
public class RetryableEventHandler {
    
    @Autowired
    private RetryTemplate retryTemplate;
    
    /**
     * 可重试的事件处理
     */
    @EventListener
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleEventWithRetry(DomainEventWrapper wrapper) {
        DomainEvent event = wrapper.getDomainEvent();
        log.info("处理事件（可重试）: {}", event.getClass().getSimpleName());
        
        try {
            // 执行事件处理逻辑
            processEvent(event);
        } catch (Exception e) {
            log.error("事件处理失败，将进行重试: eventId={}", event.getEventId(), e);
            throw e; // 重新抛出异常以触发重试
        }
    }
    
    /**
     * 重试失败后的处理
     */
    @Recover
    public void recover(Exception ex, DomainEventWrapper wrapper) {
        DomainEvent event = wrapper.getDomainEvent();
        log.error("事件处理重试失败，记录到死信队列: eventId={}", event.getEventId(), ex);
        
        // 将失败的事件记录到死信队列或错误日志表
        recordFailedEvent(event, ex);
    }
    
    /**
     * 处理事件
     */
    private void processEvent(DomainEvent event) {
        // 具体的事件处理逻辑
        log.info("执行事件处理逻辑: eventId={}", event.getEventId());
        
        // 模拟可能失败的操作
        if (Math.random() < 0.3) {
            throw new RuntimeException("模拟事件处理失败");
        }
    }
    
    /**
     * 记录失败的事件
     */
    private void recordFailedEvent(DomainEvent event, Exception ex) {
        // 记录到死信队列或错误日志表
        log.error("记录失败事件: eventId={}, error={}", event.getEventId(), ex.getMessage());
    }
}
```



## 8. 缓存策略与实现

### 8.1 多级缓存架构

```java
/**
 * 缓存配置
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {
    
    /**
     * 缓存管理器
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();
        
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }
    
    /**
     * 本地缓存管理器
     */
    @Bean("localCacheManager")
    public CacheManager localCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats());
        return cacheManager;
    }
    
    /**
     * 多级缓存管理器
     */
    @Bean("multiLevelCacheManager")
    public MultiLevelCacheManager multiLevelCacheManager(
            @Qualifier("localCacheManager") CacheManager localCacheManager,
            CacheManager redisCacheManager) {
        return new MultiLevelCacheManager(localCacheManager, redisCacheManager);
    }
}

/**
 * 多级缓存管理器
 */
@Slf4j
public class MultiLevelCacheManager implements CacheManager {
    
    private final CacheManager localCacheManager;
    private final CacheManager redisCacheManager;
    private final Map<String, MultiLevelCache> cacheMap = new ConcurrentHashMap<>();
    
    public MultiLevelCacheManager(CacheManager localCacheManager, 
                                 CacheManager redisCacheManager) {
        this.localCacheManager = localCacheManager;
        this.redisCacheManager = redisCacheManager;
    }
    
    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, cacheName -> {
            Cache localCache = localCacheManager.getCache(cacheName);
            Cache redisCache = redisCacheManager.getCache(cacheName);
            return new MultiLevelCache(cacheName, localCache, redisCache);
        });
    }
    
    @Override
    public Collection<String> getCacheNames() {
        Set<String> names = new HashSet<>();
        names.addAll(localCacheManager.getCacheNames());
        names.addAll(redisCacheManager.getCacheNames());
        return names;
    }
}

/**
 * 多级缓存实现
 */
@Slf4j
public class MultiLevelCache implements Cache {
    
    private final String name;
    private final Cache localCache;
    private final Cache redisCache;
    
    public MultiLevelCache(String name, Cache localCache, Cache redisCache) {
        this.name = name;
        this.localCache = localCache;
        this.redisCache = redisCache;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Object getNativeCache() {
        return this;
    }
    
    @Override
    public ValueWrapper get(Object key) {
        // 1. 先查本地缓存
        ValueWrapper localValue = localCache.get(key);
        if (localValue != null) {
            log.debug("命中本地缓存: cache={}, key={}", name, key);
            return localValue;
        }
        
        // 2. 查Redis缓存
        ValueWrapper redisValue = redisCache.get(key);
        if (redisValue != null) {
            log.debug("命中Redis缓存: cache={}, key={}", name, key);
            // 回写到本地缓存
            localCache.put(key, redisValue.get());
            return redisValue;
        }
        
        log.debug("缓存未命中: cache={}, key={}", name, key);
        return null;
    }
    
    @Override
    public <T> T get(Object key, Class<T> type) {
        ValueWrapper wrapper = get(key);
        return wrapper != null ? (T) wrapper.get() : null;
    }
    
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper wrapper = get(key);
        if (wrapper != null) {
            return (T) wrapper.get();
        }
        
        try {
            T value = valueLoader.call();
            put(key, value);
            return value;
        } catch (Exception e) {
            throw new RuntimeException("缓存值加载失败", e);
        }
    }
    
    @Override
    public void put(Object key, Object value) {
        // 同时写入本地缓存和Redis缓存
        localCache.put(key, value);
        redisCache.put(key, value);
        log.debug("写入多级缓存: cache={}, key={}", name, key);
    }
    
    @Override
    public void evict(Object key) {
        // 同时清除本地缓存和Redis缓存
        localCache.evict(key);
        redisCache.evict(key);
        log.debug("清除多级缓存: cache={}, key={}", name, key);
    }
    
    @Override
    public void clear() {
        localCache.clear();
        redisCache.clear();
        log.debug("清空多级缓存: cache={}", name);
    }
}
```

### 8.2 缓存策略实现

```java
/**
 * 用户缓存服务
 */
@Service
@Slf4j
public class UserCacheService {
    
    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    
    public UserCacheService(CacheManager cacheManager,
                           RedisTemplate<String, Object> redisTemplate,
                           UserRepository userRepository) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }
    
    /**
     * 缓存用户信息
     */
    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    public UserDTO getUserById(String userId) {
        log.debug("从数据库查询用户: userId={}", userId);
        return userRepository.findById(userId)
                .map(this::convertToDTO)
                .orElse(null);
    }
    
    /**
     * 缓存用户信息（根据用户名）
     */
    @Cacheable(value = "users", key = "'username:' + #username", unless = "#result == null")
    public UserDTO getUserByUsername(String username) {
        log.debug("从数据库查询用户: username={}", username);
        return userRepository.findByUsername(username)
                .map(this::convertToDTO)
                .orElse(null);
    }
    
    /**
     * 更新用户缓存
     */
    @CachePut(value = "users", key = "#user.userId")
    public UserDTO updateUserCache(UserDTO user) {
        log.debug("更新用户缓存: userId={}", user.getUserId());
        return user;
    }
    
    /**
     * 清除用户缓存
     */
    @CacheEvict(value = "users", key = "#userId")
    public void evictUserCache(String userId) {
        log.debug("清除用户缓存: userId={}", userId);
        // 同时清除用户名相关的缓存
        evictUserCacheByUsername(userId);
    }
    
    /**
     * 清除所有用户缓存
     */
    @CacheEvict(value = "users", allEntries = true)
    public void evictAllUserCache() {
        log.debug("清除所有用户缓存");
    }
    
    /**
     * 预热用户缓存
     */
    @PostConstruct
    public void warmUpCache() {
        log.info("开始预热用户缓存");
        
        // 查询活跃用户
        List<User> activeUsers = userRepository.findActiveUsers(PageRequest.of(0, 100));
        
        activeUsers.forEach(user -> {
            UserDTO userDTO = convertToDTO(user);
            
            // 缓存用户信息
            Cache usersCache = cacheManager.getCache("users");
            if (usersCache != null) {
                usersCache.put(user.getId(), userDTO);
                usersCache.put("username:" + user.getUsername(), userDTO);
            }
        });
        
        log.info("用户缓存预热完成: count={}", activeUsers.size());
    }
    
    /**
     * 批量缓存用户信息
     */
    public void batchCacheUsers(List<String> userIds) {
        log.debug("批量缓存用户信息: count={}", userIds.size());
        
        List<User> users = userRepository.findAllById(userIds);
        
        Cache usersCache = cacheManager.getCache("users");
        if (usersCache != null) {
            users.forEach(user -> {
                UserDTO userDTO = convertToDTO(user);
                usersCache.put(user.getId(), userDTO);
                usersCache.put("username:" + user.getUsername(), userDTO);
            });
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats() {
        Cache usersCache = cacheManager.getCache("users");
        if (usersCache instanceof CaffeineCache) {
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = 
                    (com.github.benmanes.caffeine.cache.Cache<Object, Object>) usersCache.getNativeCache();
            
            com.github.benmanes.caffeine.cache.stats.CacheStats stats = nativeCache.stats();
            
            return CacheStats.builder()
                    .hitCount(stats.hitCount())
                    .missCount(stats.missCount())
                    .hitRate(stats.hitRate())
                    .evictionCount(stats.evictionCount())
                    .averageLoadTime(stats.averageLoadPenalty())
                    .build();
        }
        
        return CacheStats.builder().build();
    }
    
    private void evictUserCacheByUsername(String userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                Cache usersCache = cacheManager.getCache("users");
                if (usersCache != null) {
                    usersCache.evict("username:" + user.getUsername());
                }
            }
        } catch (Exception e) {
            log.warn("清除用户名缓存失败: userId={}", userId, e);
        }
    }
    
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

/**
 * 缓存统计信息
 */
@Data
@Builder
public class CacheStats {
    private long hitCount;
    private long missCount;
    private double hitRate;
    private long evictionCount;
    private double averageLoadTime;
}
```

### 8.3 缓存事件处理

```java
/**
 * 缓存事件处理器
 */
@Component
@Slf4j
public class CacheEventHandler {
    
    private final UserCacheService userCacheService;
    private final ApplicationEventPublisher eventPublisher;
    
    public CacheEventHandler(UserCacheService userCacheService,
                           ApplicationEventPublisher eventPublisher) {
        this.userCacheService = userCacheService;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 处理用户创建事件
     */
    @EventListener
    @Async
    public void handleUserCreated(UserCreatedEvent event) {
        log.debug("处理用户创建事件: userId={}", event.getAggregateId());
        
        try {
            // 预加载用户到缓存
            userCacheService.getUserById(event.getAggregateId());
            
            // 发布缓存事件
            CacheUpdatedEvent cacheEvent = new CacheUpdatedEvent(
                    "users",
                    event.getAggregateId(),
                    CacheOperation.PUT
            );
            eventPublisher.publishEvent(cacheEvent);
            
        } catch (Exception e) {
            log.error("处理用户创建缓存事件失败: userId={}", event.getAggregateId(), e);
        }
    }
    
    /**
     * 处理用户更新事件
     */
    @EventListener
    @Async
    public void handleUserUpdated(UserUpdatedEvent event) {
        log.debug("处理用户更新事件: userId={}", event.getAggregateId());
        
        try {
            // 清除旧缓存
            userCacheService.evictUserCache(event.getAggregateId());
            
            // 重新加载到缓存
            UserDTO user = userCacheService.getUserById(event.getAggregateId());
            if (user != null) {
                userCacheService.updateUserCache(user);
            }
            
            // 发布缓存事件
            CacheUpdatedEvent cacheEvent = new CacheUpdatedEvent(
                    "users",
                    event.getAggregateId(),
                    CacheOperation.UPDATE
            );
            eventPublisher.publishEvent(cacheEvent);
            
        } catch (Exception e) {
            log.error("处理用户更新缓存事件失败: userId={}", event.getAggregateId(), e);
        }
    }
    
    /**
     * 处理用户删除事件
     */
    @EventListener
    @Async
    public void handleUserDeleted(UserDeletedEvent event) {
        log.debug("处理用户删除事件: userId={}", event.getAggregateId());
        
        try {
            // 清除缓存
            userCacheService.evictUserCache(event.getAggregateId());
            
            // 发布缓存事件
            CacheUpdatedEvent cacheEvent = new CacheUpdatedEvent(
                    "users",
                    event.getAggregateId(),
                    CacheOperation.EVICT
            );
            eventPublisher.publishEvent(cacheEvent);
            
        } catch (Exception e) {
            log.error("处理用户删除缓存事件失败: userId={}", event.getAggregateId(), e);
        }
    }
}

/**
 * 缓存更新事件
 */
@Data
@AllArgsConstructor
public class CacheUpdatedEvent {
    private String cacheName;
    private String key;
    private CacheOperation operation;
}

/**
 * 缓存操作类型
 */
public enum CacheOperation {
    PUT,
    UPDATE,
    EVICT,
    CLEAR
}

/**
 * 缓存监控服务
 */
@Service
@Slf4j
public class CacheMonitorService {
    
    private final MeterRegistry meterRegistry;
    private final UserCacheService userCacheService;
    
    public CacheMonitorService(MeterRegistry meterRegistry,
                             UserCacheService userCacheService) {
        this.meterRegistry = meterRegistry;
        this.userCacheService = userCacheService;
    }
    
    /**
     * 记录缓存命中率
     */
    @EventListener
    public void recordCacheHit(CacheUpdatedEvent event) {
        meterRegistry.counter("cache.operations",
                "cache", event.getCacheName(),
                "operation", event.getOperation().name())
                .increment();
    }
    
    /**
     * 定期收集缓存统计信息
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void collectCacheStats() {
        try {
            CacheStats stats = userCacheService.getCacheStats();
            
            // 记录缓存命中率
            meterRegistry.gauge("cache.hit.rate", stats.getHitRate());
            
            // 记录缓存命中次数
            meterRegistry.gauge("cache.hit.count", stats.getHitCount());
            
            // 记录缓存未命中次数
            meterRegistry.gauge("cache.miss.count", stats.getMissCount());
            
            // 记录缓存驱逐次数
            meterRegistry.gauge("cache.eviction.count", stats.getEvictionCount());
            
            log.debug("缓存统计信息收集完成: hitRate={}, hitCount={}, missCount={}",
                    stats.getHitRate(), stats.getHitCount(), stats.getMissCount());
            
        } catch (Exception e) {
            log.error("收集缓存统计信息失败", e);
        }
    }
}
```

### 8.4 分布式缓存一致性

```java
/**
 * 分布式缓存一致性管理器
 */
@Component
@Slf4j
public class DistributedCacheConsistencyManager {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher eventPublisher;
    
    public DistributedCacheConsistencyManager(RedisTemplate<String, Object> redisTemplate,
                                            CacheManager cacheManager,
                                            ApplicationEventPublisher eventPublisher) {
        this.redisTemplate = redisTemplate;
        this.cacheManager = cacheManager;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 发布缓存失效消息
     */
    public void publishCacheEviction(String cacheName, String key) {
        try {
            CacheEvictionMessage message = new CacheEvictionMessage(cacheName, key, System.currentTimeMillis());
            
            redisTemplate.convertAndSend("cache:eviction", message);
            
            log.debug("发布缓存失效消息: cache={}, key={}", cacheName, key);
            
        } catch (Exception e) {
            log.error("发布缓存失效消息失败: cache={}, key={}", cacheName, key, e);
        }
    }
    
    /**
     * 监听缓存失效消息
     */
    @RedisListener(topics = "cache:eviction")
    public void handleCacheEviction(CacheEvictionMessage message) {
        try {
            log.debug("接收到缓存失效消息: cache={}, key={}", message.getCacheName(), message.getKey());
            
            Cache cache = cacheManager.getCache(message.getCacheName());
            if (cache != null) {
                cache.evict(message.getKey());
                log.debug("本地缓存失效完成: cache={}, key={}", message.getCacheName(), message.getKey());
            }
            
            // 发布本地缓存失效事件
            LocalCacheEvictedEvent event = new LocalCacheEvictedEvent(
                    message.getCacheName(),
                    message.getKey()
            );
            eventPublisher.publishEvent(event);
            
        } catch (Exception e) {
            log.error("处理缓存失效消息失败: cache={}, key={}", 
                    message.getCacheName(), message.getKey(), e);
        }
    }
    
    /**
     * 批量失效缓存
     */
    public void batchEvictCache(String cacheName, List<String> keys) {
        try {
            BatchCacheEvictionMessage message = new BatchCacheEvictionMessage(
                    cacheName, keys, System.currentTimeMillis());
            
            redisTemplate.convertAndSend("cache:batch-eviction", message);
            
            log.debug("发布批量缓存失效消息: cache={}, keyCount={}", cacheName, keys.size());
            
        } catch (Exception e) {
            log.error("发布批量缓存失效消息失败: cache={}, keyCount={}", 
                    cacheName, keys.size(), e);
        }
    }
    
    /**
     * 监听批量缓存失效消息
     */
    @RedisListener(topics = "cache:batch-eviction")
    public void handleBatchCacheEviction(BatchCacheEvictionMessage message) {
        try {
            log.debug("接收到批量缓存失效消息: cache={}, keyCount={}", 
                    message.getCacheName(), message.getKeys().size());
            
            Cache cache = cacheManager.getCache(message.getCacheName());
            if (cache != null) {
                message.getKeys().forEach(cache::evict);
                log.debug("批量本地缓存失效完成: cache={}, keyCount={}", 
                        message.getCacheName(), message.getKeys().size());
            }
            
        } catch (Exception e) {
            log.error("处理批量缓存失效消息失败: cache={}, keyCount={}", 
                    message.getCacheName(), message.getKeys().size(), e);
        }
    }
}

/**
 * 缓存失效消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheEvictionMessage {
    private String cacheName;
    private String key;
    private long timestamp;
}

/**
 * 批量缓存失效消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchCacheEvictionMessage {
    private String cacheName;
    private List<String> keys;
    private long timestamp;
}

/**
 * 本地缓存失效事件
 */
@Data
@AllArgsConstructor
public class LocalCacheEvictedEvent {
    private String cacheName;
    private String key;
}

/**
 * Redis监听器注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisListener {
    String[] topics();
}
```

-- 用户表
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    full_name VARCHAR(100) COMMENT '全名',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version INT DEFAULT 0 COMMENT '版本号',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 角色表
CREATE TABLE roles (
    id VARCHAR(36) PRIMARY KEY COMMENT '角色ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    description VARCHAR(200) COMMENT '角色描述',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表
CREATE TABLE permissions (
    id VARCHAR(36) PRIMARY KEY COMMENT '权限ID',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '权限名称',
    resource VARCHAR(100) NOT NULL COMMENT '资源',
    action VARCHAR(50) NOT NULL COMMENT '操作',
    description VARCHAR(200) COMMENT '权限描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_name (name),
    INDEX idx_resource (resource),
    INDEX idx_action (action),
    UNIQUE KEY uk_resource_action (resource, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE user_roles (
    id VARCHAR(36) PRIMARY KEY COMMENT 'ID',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    role_id VARCHAR(36) NOT NULL COMMENT '角色ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE role_permissions (
    id VARCHAR(36) PRIMARY KEY COMMENT 'ID',
    role_id VARCHAR(36) NOT NULL COMMENT '角色ID',
    permission_id VARCHAR(36) NOT NULL COMMENT '权限ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 事件存储表
CREATE TABLE event_store (
    id VARCHAR(36) PRIMARY KEY COMMENT '事件ID',
    aggregate_id VARCHAR(36) NOT NULL COMMENT '聚合根ID',
    aggregate_type VARCHAR(100) NOT NULL COMMENT '聚合根类型',
    event_type VARCHAR(100) NOT NULL COMMENT '事件类型',
    event_data JSON NOT NULL COMMENT '事件数据',
    event_version INT NOT NULL COMMENT '事件版本',
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
    
    INDEX idx_aggregate_id (aggregate_id),
    INDEX idx_aggregate_type (aggregate_type),
    INDEX idx_event_type (event_type),
    INDEX idx_occurred_at (occurred_at),
    INDEX idx_aggregate_version (aggregate_id, event_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件存储表';

-- 审计日志表
CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY COMMENT '日志ID',
    user_id VARCHAR(36) COMMENT '操作用户ID',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(100) NOT NULL COMMENT '资源类型',
    resource_id VARCHAR(36) COMMENT '资源ID',
    old_data JSON COMMENT '旧数据',
    new_data JSON COMMENT '新数据',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_resource_type (resource_type),
    INDEX idx_resource_id (resource_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- 慢查询日志表
CREATE TABLE slow_query_logs (
    id VARCHAR(36) PRIMARY KEY COMMENT '日志ID',
    sql_text TEXT NOT NULL COMMENT 'SQL语句',
    execution_time BIGINT NOT NULL COMMENT '执行时间(毫秒)',
    parameters JSON COMMENT '参数',
    stack_trace TEXT COMMENT '堆栈跟踪',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_execution_time (execution_time),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='慢查询日志表';
```

### 9.2 数据库连接池配置

```java
/**
 * 数据库配置
 */
@Configuration
@Slf4j
public class DatabaseConfig {
    
    /**
     * 主数据源配置
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.primary")
    public DataSource primaryDataSource() {
        HikariConfig config = new HikariConfig();
        
        // 连接池基本配置
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        
        // 连接池性能配置
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(3000);
        config.setInitializationFailTimeout(1);
        
        // 连接池监控配置
        config.setRegisterMbeans(true);
        config.setPoolName("PrimaryHikariPool");
        
        // MySQL特定配置
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        return new HikariDataSource(config);
    }
    
    /**
     * 只读数据源配置
     */
    @Bean
    @ConfigurationProperties("spring.datasource.readonly")
    public DataSource readOnlyDataSource() {
        HikariConfig config = new HikariConfig();
        
        // 只读数据源配置
        config.setMaximumPoolSize(15);
        config.setMinimumIdle(3);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setReadOnly(true);
        config.setPoolName("ReadOnlyHikariPool");
        
        return new HikariDataSource(config);
    }
    
    /**
     * 动态数据源配置
     */
    @Bean
    public DataSource dynamicDataSource(@Qualifier("primaryDataSource") DataSource primaryDataSource,
                                      @Qualifier("readOnlyDataSource") DataSource readOnlyDataSource) {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DataSourceType.PRIMARY, primaryDataSource);
        dataSourceMap.put(DataSourceType.READONLY, readOnlyDataSource);
        
        dynamicDataSource.setTargetDataSources(dataSourceMap);
        dynamicDataSource.setDefaultTargetDataSource(primaryDataSource);
        
        return dynamicDataSource;
    }
    
    /**
     * 事务管理器
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dynamicDataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dynamicDataSource);
        transactionManager.setRollbackOnCommitFailure(true);
        return transactionManager;
    }
}

/**
 * 动态数据源
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
    
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSourceType();
    }
}

/**
 * 数据源类型
 */
public enum DataSourceType {
    PRIMARY,
    READONLY
}

/**
 * 数据源上下文持有者
 */
public class DataSourceContextHolder {
    
    private static final ThreadLocal<DataSourceType> CONTEXT_HOLDER = new ThreadLocal<>();
    
    public static void setDataSourceType(DataSourceType dataSourceType) {
        CONTEXT_HOLDER.set(dataSourceType);
    }
    
    public static DataSourceType getDataSourceType() {
        return CONTEXT_HOLDER.get();
    }
    
    public static void clearDataSourceType() {
        CONTEXT_HOLDER.remove();
    }
}

/**
 * 读写分离注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReadOnly {
    boolean value() default true;
}

/**
 * 读写分离切面
 */
@Aspect
@Component
@Slf4j
public class ReadOnlyAspect {
    
    @Around("@annotation(readOnly)")
    public Object around(ProceedingJoinPoint point, ReadOnly readOnly) throws Throwable {
        try {
            if (readOnly.value()) {
                DataSourceContextHolder.setDataSourceType(DataSourceType.READONLY);
                log.debug("切换到只读数据源");
            }
            return point.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
            log.debug("清除数据源上下文");
        }
    }
}
```

### 9.3 数据库监控与优化

```java
/**
 * 数据库监控配置
 */
@Configuration
@Slf4j
public class DatabaseMonitorConfig {
    
    /**
     * 连接池监控
     */
    @Bean
    public HikariPoolMXBean hikariPoolMXBean(@Qualifier("primaryDataSource") DataSource dataSource) {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            return hikariDataSource.getHikariPoolMXBean();
        }
        return null;
    }
    
    /**
     * 数据库性能监控
     */
    @Component
    @Slf4j
    public static class DatabasePerformanceMonitor {
        
        private final MeterRegistry meterRegistry;
        private final HikariPoolMXBean hikariPoolMXBean;
        
        public DatabasePerformanceMonitor(MeterRegistry meterRegistry,
                                        @Autowired(required = false) HikariPoolMXBean hikariPoolMXBean) {
            this.meterRegistry = meterRegistry;
            this.hikariPoolMXBean = hikariPoolMXBean;
        }
        
        /**
         * 定期收集连接池指标
         */
        @Scheduled(fixedRate = 30000) // 每30秒执行一次
        public void collectConnectionPoolMetrics() {
            if (hikariPoolMXBean != null) {
                try {
                    // 活跃连接数
                    meterRegistry.gauge("db.pool.active", hikariPoolMXBean.getActiveConnections());
                    
                    // 空闲连接数
                    meterRegistry.gauge("db.pool.idle", hikariPoolMXBean.getIdleConnections());
                    
                    // 总连接数
                    meterRegistry.gauge("db.pool.total", hikariPoolMXBean.getTotalConnections());
                    
                    // 等待连接的线程数
                    meterRegistry.gauge("db.pool.pending", hikariPoolMXBean.getThreadsAwaitingConnection());
                    
                    log.debug("连接池指标收集完成: active={}, idle={}, total={}, pending={}",
                            hikariPoolMXBean.getActiveConnections(),
                            hikariPoolMXBean.getIdleConnections(),
                            hikariPoolMXBean.getTotalConnections(),
                            hikariPoolMXBean.getThreadsAwaitingConnection());
                    
                } catch (Exception e) {
                    log.error("收集连接池指标失败", e);
                }
            }
        }
    }
}

/**
 * SQL性能监控拦截器
 */
@Component
@Slf4j
public class SqlPerformanceInterceptor implements Interceptor {
    
    private final MeterRegistry meterRegistry;
    private final SlowQueryService slowQueryService;
    
    // 慢查询阈值（毫秒）
    private static final long SLOW_QUERY_THRESHOLD = 1000;
    
    public SqlPerformanceInterceptor(MeterRegistry meterRegistry,
                                   SlowQueryService slowQueryService) {
        this.meterRegistry = meterRegistry;
        this.slowQueryService = slowQueryService;
    }
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        String sql = null;
        Object[] parameters = null;
        
        try {
            // 获取SQL语句和参数
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            Object parameterObject = invocation.getArgs()[1];
            
            BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
            sql = boundSql.getSql();
            parameters = getParameters(boundSql, parameterObject);
            
            // 执行SQL
            Object result = invocation.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录SQL执行指标
            recordSqlMetrics(mappedStatement.getId(), executionTime);
            
            // 检查是否为慢查询
            if (executionTime > SLOW_QUERY_THRESHOLD) {
                recordSlowQuery(sql, parameters, executionTime);
            }
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录SQL错误指标
            meterRegistry.counter("sql.errors").increment();
            
            log.error("SQL执行异常: sql={}, executionTime={}ms", sql, executionTime, e);
            throw e;
        }
    }
    
    private void recordSqlMetrics(String sqlId, long executionTime) {
        // 记录SQL执行次数
        meterRegistry.counter("sql.executions", "sqlId", sqlId).increment();
        
        // 记录SQL执行时间
        meterRegistry.timer("sql.execution.time", "sqlId", sqlId)
                .record(executionTime, TimeUnit.MILLISECONDS);
        
        // 记录SQL执行时间分布
        if (executionTime < 100) {
            meterRegistry.counter("sql.execution.fast").increment();
        } else if (executionTime < 500) {
            meterRegistry.counter("sql.execution.normal").increment();
        } else if (executionTime < 1000) {
            meterRegistry.counter("sql.execution.slow").increment();
        } else {
            meterRegistry.counter("sql.execution.very_slow").increment();
        }
    }
    
    private void recordSlowQuery(String sql, Object[] parameters, long executionTime) {
        try {
            SlowQueryLog slowQueryLog = SlowQueryLog.builder()
                    .sqlText(sql)
                    .executionTime(executionTime)
                    .parameters(parameters != null ? Arrays.toString(parameters) : null)
                    .stackTrace(getStackTrace())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            slowQueryService.saveSlowQuery(slowQueryLog);
            
            log.warn("检测到慢查询: sql={}, executionTime={}ms", sql, executionTime);
            
        } catch (Exception e) {
            log.error("记录慢查询失败", e);
        }
    }
    
    private Object[] getParameters(BoundSql boundSql, Object parameterObject) {
        // 简化实现，实际应该解析参数映射
        if (parameterObject instanceof Map) {
            Map<?, ?> paramMap = (Map<?, ?>) parameterObject;
            return paramMap.values().toArray();
        } else if (parameterObject != null) {
            return new Object[]{parameterObject};
        }
        return null;
    }
    
    private String getStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(stackTrace.length, 10); i++) {
            sb.append(stackTrace[i].toString()).append("\n");
        }
        return sb.toString();
    }
    
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    
    @Override
    public void setProperties(Properties properties) {
        // 可以从配置文件读取慢查询阈值等参数
    }
}

/**
 * 慢查询日志实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlowQueryLog {
    private String id;
    private String sqlText;
    private Long executionTime;
    private String parameters;
    private String stackTrace;
    private LocalDateTime createdAt;
}

/**
 * 慢查询服务
 */
@Service
@Slf4j
public class SlowQueryService {
    
    private final SlowQueryLogRepository slowQueryLogRepository;
    private final AsyncTaskExecutor asyncTaskExecutor;
    
    public SlowQueryService(SlowQueryLogRepository slowQueryLogRepository,
                          @Qualifier("taskExecutor") AsyncTaskExecutor asyncTaskExecutor) {
        this.slowQueryLogRepository = slowQueryLogRepository;
        this.asyncTaskExecutor = asyncTaskExecutor;
    }
    
    /**
     * 异步保存慢查询日志
     */
    @Async("taskExecutor")
    public void saveSlowQuery(SlowQueryLog slowQueryLog) {
        try {
            slowQueryLog.setId(UUID.randomUUID().toString());
            slowQueryLogRepository.save(slowQueryLog);
            
            log.debug("慢查询日志保存成功: id={}", slowQueryLog.getId());
            
        } catch (Exception e) {
            log.error("保存慢查询日志失败", e);
        }
    }
    
    /**
     * 查询慢查询统计信息
     */
    public SlowQueryStats getSlowQueryStats(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            List<SlowQueryLog> slowQueries = slowQueryLogRepository
                    .findByCreatedAtBetween(startTime, endTime);
            
            if (slowQueries.isEmpty()) {
                return SlowQueryStats.builder()
                        .totalCount(0)
                        .averageExecutionTime(0.0)
                        .maxExecutionTime(0L)
                        .minExecutionTime(0L)
                        .build();
            }
            
            long totalCount = slowQueries.size();
            double averageExecutionTime = slowQueries.stream()
                    .mapToLong(SlowQueryLog::getExecutionTime)
                    .average()
                    .orElse(0.0);
            long maxExecutionTime = slowQueries.stream()
                    .mapToLong(SlowQueryLog::getExecutionTime)
                    .max()
                    .orElse(0L);
            long minExecutionTime = slowQueries.stream()
                    .mapToLong(SlowQueryLog::getExecutionTime)
                    .min()
                    .orElse(0L);
            
            return SlowQueryStats.builder()
                    .totalCount(totalCount)
                    .averageExecutionTime(averageExecutionTime)
                    .maxExecutionTime(maxExecutionTime)
                    .minExecutionTime(minExecutionTime)
                    .build();
            
        } catch (Exception e) {
            log.error("获取慢查询统计信息失败", e);
            throw new RuntimeException("获取慢查询统计信息失败", e);
        }
    }
    
    /**
     * 清理过期的慢查询日志
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupExpiredSlowQueryLogs() {
        try {
            LocalDateTime expiredTime = LocalDateTime.now().minusDays(30); // 保留30天
            int deletedCount = slowQueryLogRepository.deleteByCreatedAtBefore(expiredTime);
            
            log.info("清理过期慢查询日志完成: deletedCount={}", deletedCount);
            
        } catch (Exception e) {
            log.error("清理过期慢查询日志失败", e);
        }
    }
}

/**
 * 慢查询统计信息
 */
@Data
@Builder
public class SlowQueryStats {
    private long totalCount;
    private double averageExecutionTime;
    private long maxExecutionTime;
    private long minExecutionTime;
}
```

### 9.4 数据库索引优化

```java
/**
 * 索引优化建议服务
 */
@Service
@Slf4j
public class IndexOptimizationService {
    
    private final JdbcTemplate jdbcTemplate;
    
    public IndexOptimizationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * 分析表的索引使用情况
     */
    public List<IndexUsageStats> analyzeIndexUsage(String tableName) {
        String sql = """
            SELECT 
                s.TABLE_NAME,
                s.INDEX_NAME,
                s.COLUMN_NAME,
                s.CARDINALITY,
                s.SUB_PART,
                s.NULLABLE,
                t.TABLE_ROWS,
                CASE 
                    WHEN s.CARDINALITY = 0 THEN 0
                    ELSE ROUND((s.CARDINALITY / t.TABLE_ROWS) * 100, 2)
                END as SELECTIVITY
            FROM 
                information_schema.STATISTICS s
            JOIN 
                information_schema.TABLES t ON s.TABLE_NAME = t.TABLE_NAME 
                AND s.TABLE_SCHEMA = t.TABLE_SCHEMA
            WHERE 
                s.TABLE_SCHEMA = DATABASE()
                AND s.TABLE_NAME = ?
            ORDER BY 
                s.INDEX_NAME, s.SEQ_IN_INDEX
            """;
        
        return jdbcTemplate.query(sql, new Object[]{tableName}, (rs, rowNum) -> 
            IndexUsageStats.builder()
                .tableName(rs.getString("TABLE_NAME"))
                .indexName(rs.getString("INDEX_NAME"))
                .columnName(rs.getString("COLUMN_NAME"))
                .cardinality(rs.getLong("CARDINALITY"))
                .tableRows(rs.getLong("TABLE_ROWS"))
                .selectivity(rs.getDouble("SELECTIVITY"))
                .build()
        );
    }
    
    /**
     * 检查重复索引
     */
    public List<DuplicateIndexInfo> findDuplicateIndexes() {
        String sql = """
            SELECT 
                TABLE_NAME,
                GROUP_CONCAT(INDEX_NAME) as INDEX_NAMES,
                GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) as COLUMNS,
                COUNT(*) as INDEX_COUNT
            FROM 
                information_schema.STATISTICS 
            WHERE 
                TABLE_SCHEMA = DATABASE()
                AND INDEX_NAME != 'PRIMARY'
            GROUP BY 
                TABLE_NAME, 
                GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX)
            HAVING 
                COUNT(*) > 1
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            DuplicateIndexInfo.builder()
                .tableName(rs.getString("TABLE_NAME"))
                .indexNames(rs.getString("INDEX_NAMES"))
                .columns(rs.getString("COLUMNS"))
                .indexCount(rs.getInt("INDEX_COUNT"))
                .build()
        );
    }
    
    /**
     * 分析未使用的索引
     */
    public List<UnusedIndexInfo> findUnusedIndexes() {
        // 这需要开启 performance_schema
        String sql = """
            SELECT 
                s.TABLE_SCHEMA,
                s.TABLE_NAME,
                s.INDEX_NAME,
                s.COLUMN_NAME
            FROM 
                information_schema.STATISTICS s
            LEFT JOIN 
                performance_schema.table_io_waits_summary_by_index_usage p
                ON s.TABLE_SCHEMA = p.OBJECT_SCHEMA 
                AND s.TABLE_NAME = p.OBJECT_NAME 
                AND s.INDEX_NAME = p.INDEX_NAME
            WHERE 
                s.TABLE_SCHEMA = DATABASE()
                AND s.INDEX_NAME != 'PRIMARY'
                AND (p.COUNT_STAR IS NULL OR p.COUNT_STAR = 0)
            GROUP BY 
                s.TABLE_SCHEMA, s.TABLE_NAME, s.INDEX_NAME
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            UnusedIndexInfo.builder()
                .tableSchema(rs.getString("TABLE_SCHEMA"))
                .tableName(rs.getString("TABLE_NAME"))
                .indexName(rs.getString("INDEX_NAME"))
                .build()
        );
    }
    
    /**
     * 生成索引优化建议
     */
    public List<IndexOptimizationSuggestion> generateOptimizationSuggestions() {
        List<IndexOptimizationSuggestion> suggestions = new ArrayList<>();
        
        // 检查重复索引
        List<DuplicateIndexInfo> duplicateIndexes = findDuplicateIndexes();
        for (DuplicateIndexInfo duplicate : duplicateIndexes) {
            suggestions.add(IndexOptimizationSuggestion.builder()
                .type(OptimizationType.REMOVE_DUPLICATE)
                .tableName(duplicate.getTableName())
                .description("发现重复索引: " + duplicate.getIndexNames())
                .suggestion("保留一个索引，删除其他重复索引")
                .priority(Priority.HIGH)
                .build());
        }
        
        // 检查未使用的索引
        List<UnusedIndexInfo> unusedIndexes = findUnusedIndexes();
        for (UnusedIndexInfo unused : unusedIndexes) {
            suggestions.add(IndexOptimizationSuggestion.builder()
                .type(OptimizationType.REMOVE_UNUSED)
                .tableName(unused.getTableName())
                .description("发现未使用的索引: " + unused.getIndexName())
                .suggestion("考虑删除未使用的索引以减少存储空间和维护开销")
                .priority(Priority.MEDIUM)
                .build());
        }
        
        return suggestions;
    }
}

/**
 * 索引使用统计
 */
@Data
@Builder
public class IndexUsageStats {
    private String tableName;
    private String indexName;
    private String columnName;
    private Long cardinality;
    private Long tableRows;
    private Double selectivity;
}

/**
 * 重复索引信息
 */
@Data
@Builder
public class DuplicateIndexInfo {
    private String tableName;
    private String indexNames;
    private String columns;
    private Integer indexCount;
}

/**
 * 未使用索引信息
 */
@Data
@Builder
public class UnusedIndexInfo {
    private String tableSchema;
    private String tableName;
    private String indexName;
}

/**
 * 索引优化建议
 */
@Data
@Builder
public class IndexOptimizationSuggestion {
    private OptimizationType type;
    private String tableName;
    private String description;
    private String suggestion;
    private Priority priority;
}

/**
 * 优化类型
 */
public enum OptimizationType {
    REMOVE_DUPLICATE,
    REMOVE_UNUSED,
    ADD_MISSING,
    MODIFY_EXISTING
}

/**
 * 优先级
 */
public enum Priority {
    HIGH,
    MEDIUM,
    LOW
}
```

/**
 * 用户领域服务单元测试
 */
  @ExtendWith(MockitoExtension.class)
  class UserDomainServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    @InjectMocks
    private UserDomainService userDomainService;
    
    @Test
    @DisplayName("创建用户 - 成功")
    void createUser_Success() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        User result = userDomainService.createUser(username, email, password);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        
        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publish(any(UserCreatedEvent.class));
    }
    
    @Test
    @DisplayName("创建用户 - 用户名已存在")
    void createUser_UsernameExists() {
        // Given
        String username = "existinguser";
        String email = "test@example.com";
        String password = "password123";
        
        when(userRepository.existsByUsername(username)).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> userDomainService.createUser(username, email, password))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("用户名已存在: " + username);
        
        verify(userRepository).existsByUsername(username);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publish(any(DomainEvent.class));
    }
    
    @Test
    @DisplayName("创建用户 - 邮箱已存在")
    void createUser_EmailExists() {
        // Given
        String username = "testuser";
        String email = "existing@example.com";
        String password = "password123";
        
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> userDomainService.createUser(username, email, password))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("邮箱已存在: " + email);
        
        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publish(any(DomainEvent.class));
    }
    
    @Test
    @DisplayName("更新用户状态 - 成功")
    void updateUserStatus_Success() {
        // Given
        String userId = "user-123";
        UserStatus newStatus = UserStatus.INACTIVE;
        
        User existingUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .status(UserStatus.ACTIVE)
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        User result = userDomainService.updateUserStatus(userId, newStatus);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);
        
        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
        verify(eventPublisher).publish(any(UserStatusChangedEvent.class));
    }
    
    @Test
    @DisplayName("更新用户状态 - 用户不存在")
    void updateUserStatus_UserNotFound() {
        // Given
        String userId = "nonexistent-user";
        UserStatus newStatus = UserStatus.INACTIVE;
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> userDomainService.updateUserStatus(userId, newStatus))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("用户不存在: " + userId);
        
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publish(any(DomainEvent.class));
    }
  }

/**
 * 用户实体单元测试
 */
  class UserTest {
    
    @Test
    @DisplayName("创建用户 - 成功")
    void createUser_Success() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String passwordHash = "hashedpassword";
        
        // When
        User user = User.create(username, email, passwordHash);
        
        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getVersion()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("创建用户 - 用户名为空")
    void createUser_EmptyUsername() {
        // Given
        String username = "";
        String email = "test@example.com";
        String passwordHash = "hashedpassword";
        
        // When & Then
        assertThatThrownBy(() -> User.create(username, email, passwordHash))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("用户名不能为空");
    }
    
    @Test
    @DisplayName("创建用户 - 邮箱格式无效")
    void createUser_InvalidEmail() {
        // Given
        String username = "testuser";
        String email = "invalid-email";
        String passwordHash = "hashedpassword";
        
        // When & Then
        assertThatThrownBy(() -> User.create(username, email, passwordHash))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("邮箱格式无效");
    }
    
    @Test
    @DisplayName("更新用户状态 - 成功")
    void updateStatus_Success() {
        // Given
        User user = User.create("testuser", "test@example.com", "hashedpassword");
        UserStatus newStatus = UserStatus.INACTIVE;
        
        // When
        user.updateStatus(newStatus);
        
        // Then
        assertThat(user.getStatus()).isEqualTo(newStatus);
        assertThat(user.getUpdatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("更新用户状态 - 状态为空")
    void updateStatus_NullStatus() {
        // Given
        User user = User.create("testuser", "test@example.com", "hashedpassword");
        
        // When & Then
        assertThatThrownBy(() -> user.updateStatus(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("用户状态不能为空");
    }
    
    @Test
    @DisplayName("验证密码 - 成功")
    void validatePassword_Success() {
        // Given
        String password = "password123";
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash(passwordHash)
                .build();
        
        // When
        boolean result = user.validatePassword(password);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("验证密码 - 失败")
    void validatePassword_Failure() {
        // Given
        String password = "password123";
        String wrongPassword = "wrongpassword";
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash(passwordHash)
                .build();
        
        // When
        boolean result = user.validatePassword(wrongPassword);
        
        // Then
        assertThat(result).isFalse();
    }
  }

/**
 * 测试配置
 */
  @TestConfiguration
  public class TestConfig {
    
    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(Instant.parse("2023-01-01T00:00:00Z"), ZoneOffset.UTC);
    }
    
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
  }

/**
 * 测试数据构建器
 */
  public class UserTestDataBuilder {
    
    private String id = UUID.randomUUID().toString();
    private String username = "testuser";
    private String email = "test@example.com";
    private String passwordHash = "hashedpassword";
    private String fullName = "Test User";
    private UserStatus status = UserStatus.ACTIVE;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private Integer version = 0;
    
    public static UserTestDataBuilder aUser() {
        return new UserTestDataBuilder();
    }
    
    public UserTestDataBuilder withId(String id) {
        this.id = id;
        return this;
    }
    
    public UserTestDataBuilder withUsername(String username) {
        this.username = username;
        return this;
    }
    
    public UserTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public UserTestDataBuilder withPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }
    
    public UserTestDataBuilder withFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }
    
    public UserTestDataBuilder withStatus(UserStatus status) {
        this.status = status;
        return this;
    }
    
    public UserTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    public UserTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
    
    public UserTestDataBuilder withVersion(Integer version) {
        this.version = version;
        return this;
    }
    
    public User build() {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .passwordHash(passwordHash)
                .fullName(fullName)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .version(version)
                .build();
    }
  }
```

### 10.2 集成测试

```java
/**
 * 用户应用服务集成测试
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserApplicationServiceIntegrationTest {
    
    @Autowired
    private UserApplicationService userApplicationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TestEntityManager testEntityManager;
    
    @Test
    @DisplayName("创建用户 - 集成测试")
    void createUser_IntegrationTest() {
        // Given
        CreateUserCommand command = CreateUserCommand.builder()
                .username("integrationuser")
                .email("integration@example.com")
                .password("password123")
                .fullName("Integration User")
                .build();
        
        // When
        UserDTO result = userApplicationService.createUser(command);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(command.getUsername());
        assertThat(result.getEmail()).isEqualTo(command.getEmail());
        assertThat(result.getFullName()).isEqualTo(command.getFullName());
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        
        // 验证数据库中的数据
        Optional<User> savedUser = userRepository.findById(result.getId());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getUsername()).isEqualTo(command.getUsername());
        assertThat(savedUser.get().getEmail()).isEqualTo(command.getEmail());
    }
    
    @Test
    @DisplayName("查询用户 - 集成测试")
    void getUserById_IntegrationTest() {
        // Given
        User user = UserTestDataBuilder.aUser()
                .withUsername("queryuser")
                .withEmail("query@example.com")
                .build();
        
        testEntityManager.persistAndFlush(user);
        
        // When
        UserDTO result = userApplicationService.getUserById(user.getId());
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getUsername()).isEqualTo(user.getUsername());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
    }
    
    @Test
    @DisplayName("更新用户 - 集成测试")
    void updateUser_IntegrationTest() {
        // Given
        User user = UserTestDataBuilder.aUser()
                .withUsername("updateuser")
                .withEmail("update@example.com")
                .build();
        
        testEntityManager.persistAndFlush(user);
        
        UpdateUserCommand command = UpdateUserCommand.builder()
                .id(user.getId())
                .fullName("Updated User")
                .build();
        
        // When
        UserDTO result = userApplicationService.updateUser(command);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo(command.getFullName());
        
        // 验证数据库中的数据
        Optional<User> updatedUser = userRepository.findById(user.getId());
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getFullName()).isEqualTo(command.getFullName());
    }
    
    @Test
    @DisplayName("删除用户 - 集成测试")
    void deleteUser_IntegrationTest() {
        // Given
        User user = UserTestDataBuilder.aUser()
                .withUsername("deleteuser")
                .withEmail("delete@example.com")
                .build();
        
        testEntityManager.persistAndFlush(user);
        
        // When
        userApplicationService.deleteUser(user.getId());
        
        // Then
        Optional<User> deletedUser = userRepository.findById(user.getId());
        assertThat(deletedUser).isEmpty();
    }
}

/**
 * 用户仓储集成测试
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryIntegrationTest {
    
    @Autowired
    private TestEntityManager testEntityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    @DisplayName("根据用户名查找用户")
    void findByUsername_Success() {
        // Given
        User user = UserTestDataBuilder.aUser()
                .withUsername("finduser")
                .withEmail("find@example.com")
                .build();
        
        testEntityManager.persistAndFlush(user);
        
        // When
        Optional<User> result = userRepository.findByUsername("finduser");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("finduser");
        assertThat(result.get().getEmail()).isEqualTo("find@example.com");
    }
    
    @Test
    @DisplayName("根据邮箱查找用户")
    void findByEmail_Success() {
        // Given
        User user = UserTestDataBuilder.aUser()
                .withUsername("emailuser")
                .withEmail("email@example.com")
                .build();
        
        testEntityManager.persistAndFlush(user);
        
        // When
        Optional<User> result = userRepository.findByEmail("email@example.com");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("emailuser");
        assertThat(result.get().getEmail()).isEqualTo("email@example.com");
    }
    
    @Test
    @DisplayName("检查用户名是否存在")
    void existsByUsername_True() {
        // Given
        User user = UserTestDataBuilder.aUser()
                .withUsername("existsuser")
                .withEmail("exists@example.com")
                .build();
        
        testEntityManager.persistAndFlush(user);
        
        // When
        boolean result = userRepository.existsByUsername("existsuser");
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("检查用户名是否存在 - 不存在")
    void existsByUsername_False() {
        // When
        boolean result = userRepository.existsByUsername("nonexistentuser");
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("根据状态查找用户")
    void findByStatus_Success() {
        // Given
        User activeUser = UserTestDataBuilder.aUser()
                .withUsername("activeuser")
                .withEmail("active@example.com")
                .withStatus(UserStatus.ACTIVE)
                .build();
        
        User inactiveUser = UserTestDataBuilder.aUser()
                .withUsername("inactiveuser")
                .withEmail("inactive@example.com")
                .withStatus(UserStatus.INACTIVE)
                .build();
        
        testEntityManager.persistAndFlush(activeUser);
        testEntityManager.persistAndFlush(inactiveUser);
        
        // When
        List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);
        
        // Then
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getUsername()).isEqualTo("activeuser");
    }
    
    @Test
    @DisplayName("分页查询用户")
    void findAll_Pageable() {
        // Given
        for (int i = 0; i < 15; i++) {
            User user = UserTestDataBuilder.aUser()
                    .withUsername("user" + i)
                    .withEmail("user" + i + "@example.com")
                    .build();
            testEntityManager.persistAndFlush(user);
        }
        
        Pageable pageable = PageRequest.of(0, 10, Sort.by("username"));
        
        // When
        Page<User> result = userRepository.findAll(pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(15);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("user0");
    }
}

/**
 * 数据库事务测试
 */
@SpringBootTest
@Transactional
class TransactionIntegrationTest {
    
    @Autowired
    private UserApplicationService userApplicationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    @DisplayName("事务回滚测试")
    void transactionRollback_Test() {
        // Given
        CreateUserCommand command = CreateUserCommand.builder()
                .username("transactionuser")
                .email("transaction@example.com")
                .password("password123")
                .build();
        
        // When & Then
        assertThatThrownBy(() -> {
            userApplicationService.createUser(command);
            // 模拟异常
            throw new RuntimeException("模拟异常");
        }).isInstanceOf(RuntimeException.class);
        
        // 验证事务回滚
        boolean userExists = userRepository.existsByUsername("transactionuser");
        assertThat(userExists).isFalse();
    }
}
```

### 10.3 端到端测试

```java
/**
 * 用户API端到端测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:e2edb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserControllerE2ETest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @LocalServerPort
    private int port;
    
    private String baseUrl;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/users";
    }
    
    @Test
    @DisplayName("创建用户 - E2E测试")
    void createUser_E2E() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .username("e2euser")
                .email("e2e@example.com")
                .password("password123")
                .fullName("E2E User")
                .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request, headers);
        
        // When
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                baseUrl, entity, UserResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo(request.getUsername());
        assertThat(response.getBody().getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getBody().getFullName()).isEqualTo(request.getFullName());
        
        // 验证数据库
        Optional<User> savedUser = userRepository.findByUsername(request.getUsername());
        assertThat(savedUser).isPresent();
    }
    
    @Test
    @DisplayName("获取用户 - E2E测试")
    void getUser_E2E() {
        // Given
        User user = UserTestDataBuilder.aUser()
                .withUsername("getuser")
                .withEmail("get@example.com")
                .build();
        
        User savedUser = userRepository.save(user);
        
        // When
        ResponseEntity<UserResponse> response = restTemplate.getForEntity(
                baseUrl + "/" + savedUser.getId(), UserResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(savedUser.getId());
        assertThat(response.getBody().getUsername()).isEqualTo(savedUser.getUsername());
        assertThat(response.getBody().getEmail()).isEqualTo(savedUser.getEmail());
    }
    
    @Test
    @DisplayName("更新用户 - E2E测试")
    void updateUser_E2E() {
        // Given
        User user = UserTestDataBuilder.aUser()
                .withUsername("updateuser")
                .withEmail("update@example.com")
                .build();
        
        User savedUser = userRepository.save(user);
        
        UpdateUserRequest request = UpdateUserRequest.builder()
                .fullName("Updated E2E User")
                .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(request, headers);
        
        // When
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                baseUrl + "/" + savedUser.getId(),
                HttpMethod.PUT,
                entity,
                UserResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFullName()).isEqualTo(request.getFullName());
        
        // 验证数据库
        Optional<User> updatedUser = userRepository.findById(savedUser.getId());
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getFullName()).isEqualTo(request.getFullName());
    }
    
    @Test
    @DisplayName("删除用户 - E2E测试")
    void deleteUser_E2E() {
        // Given
        User user = UserTestDataBuilder.aUser()
                .withUsername("deleteuser")
                .withEmail("delete@example.com")
                .build();
        
        User savedUser = userRepository.save(user);
        
        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + savedUser.getId(),
                HttpMethod.DELETE,
                null,
                Void.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // 验证数据库
        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
    }
    
    @Test
    @DisplayName("分页查询用户 - E2E测试")
    void getUsers_Pageable_E2E() {
        // Given
        for (int i = 0; i < 15; i++) {
            User user = UserTestDataBuilder.aUser()
                    .withUsername("pageuser" + i)
                    .withEmail("page" + i + "@example.com")
                    .build();
            userRepository.save(user);
        }
        
        // When
        ResponseEntity<PageResponse<UserResponse>> response = restTemplate.exchange(
                baseUrl + "?page=0&size=10&sort=username",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PageResponse<UserResponse>>() {});
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(10);
        assertThat(response.getBody().getTotalElements()).isGreaterThanOrEqualTo(15);
    }
    
    @Test
    @DisplayName("用户名已存在 - E2E测试")
    void createUser_UsernameExists_E2E() {
        // Given
        User existingUser = UserTestDataBuilder.aUser()
                .withUsername("existinguser")
                .withEmail("existing@example.com")
                .build();
        
        userRepository.save(existingUser);
        
        CreateUserRequest request = CreateUserRequest.builder()
                .username("existinguser")
                .email("new@example.com")
                .password("password123")
                .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request, headers);
        
        // When
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                baseUrl, entity, ErrorResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("USER_ALREADY_EXISTS");
    }
    
    @Test
    @DisplayName("用户不存在 - E2E测试")
    void getUser_NotFound_E2E() {
        // Given
        String nonExistentUserId = "nonexistent-user-id";
        
        // When
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                baseUrl + "/" + nonExistentUserId, ErrorResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("USER_NOT_FOUND");
    }
}

/**
 * 性能测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:perfdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserPerformanceTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @LocalServerPort
    private int port;
    
    private String baseUrl;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/users";
    }
    
    @Test
    @DisplayName("批量创建用户性能测试")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void batchCreateUsers_PerformanceTest() {
        // Given
        int userCount = 1000;
        List<CreateUserRequest> requests = new ArrayList<>();
        
        for (int i = 0; i < userCount; i++) {
            requests.add(CreateUserRequest.builder()
                    .username("perfuser" + i)
                    .email("perf" + i + "@example.com")
                    .password("password123")
                    .build());
        }
        
        // When
        long startTime = System.currentTimeMillis();
        
        for (CreateUserRequest request : requests) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                    baseUrl, entity, UserResponse.class);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // Then
        System.out.println("批量创建 " + userCount + " 个用户耗时: " + executionTime + "ms");
        System.out.println("平均每个用户创建耗时: " + (executionTime / userCount) + "ms");
        
        // 验证数据库中的用户数量
        long savedUserCount = userRepository.count();
        assertThat(savedUserCount).isGreaterThanOrEqualTo(userCount);
    }
    
    @Test
    @DisplayName("分页查询性能测试")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void pageQuery_PerformanceTest() {
        // Given
        int userCount = 10000;
        List<User> users = new ArrayList<>();
        
        for (int i = 0; i < userCount; i++) {
            users.add(UserTestDataBuilder.aUser()
                    .withUsername("queryuser" + i)
                    .withEmail("query" + i + "@example.com")
                    .build());
        }
        
        userRepository.saveAll(users);
        
        // When
        long startTime = System.currentTimeMillis();
        
        ResponseEntity<PageResponse<UserResponse>> response = restTemplate.exchange(
                baseUrl + "?page=0&size=100",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PageResponse<UserResponse>>() {});
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // Then
        System.out.println("分页查询 " + userCount + " 个用户中的100个耗时: " + executionTime + "ms");
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(100);
        assertThat(executionTime).isLessThan(1000); // 期望在1秒内完成
    }
}
```

### 10.4 测试工具与配置

```java
/**
 * 测试容器配置
 */
@TestConfiguration
public class TestContainerConfig {
    
    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
    }
    
    @Bean
    @ServiceConnection
    public RedisContainer redisContainer() {
        return new RedisContainer("redis:7-alpine")
                .withReuse(true);
    }
}

/**
 * 测试切片配置
 */
@TestConfiguration
public class TestSliceConfig {
    
    /**
     * Web层测试配置
     */
    @TestConfiguration
    @Import({
        UserController.class,
        GlobalExceptionHandler.class,
        SecurityConfig.class
    })
    public static class WebLayerTestConfig {
        
        @MockBean
        private UserApplicationService userApplicationService;
        
        @MockBean
        private JwtTokenProvider jwtTokenProvider;
    }
    
    /**
     * 服务层测试配置
     */
    @TestConfiguration
    @Import({
        UserApplicationService.class,
        UserDomainService.class,
        UserMapper.class
    })
    public static class ServiceLayerTestConfig {
        
        @MockBean
        private UserRepository userRepository;
        
        @MockBean
        private DomainEventPublisher eventPublisher;
        
        @MockBean
        private PasswordEncoder passwordEncoder;
    }
    
    /**
     * 数据层测试配置
     */
    @TestConfiguration
    @EnableJpaRepositories(basePackages = "com.example.user.infrastructure.repository")
    @EntityScan(basePackages = "com.example.user.domain.entity")
    public static class DataLayerTestConfig {
        
        @Bean
        public AuditorAware<String> auditorProvider() {
            return () -> Optional.of("test-user");
        }
    }
}

/**
 * 测试工具类
 */
public class TestUtils {
    
    /**
     * 生成随机字符串
     */
    public static String randomString(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }
    
    /**
     * 生成随机邮箱
     */
    public static String randomEmail() {
        return randomString(8) + "@example.com";
    }
    
    /**
     * 生成随机用户名
     */
    public static String randomUsername() {
        return "user" + randomString(6);
    }
    
    /**
     * 等待异步操作完成
     */
    public static void waitForAsync(Duration timeout) {
        try {
            Thread.sleep(timeout.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 验证JSON响应
     */
    public static void assertJsonEquals(String expected, String actual) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode expectedNode = mapper.readTree(expected);
            JsonNode actualNode = mapper.readTree(actual);
            assertThat(actualNode).isEqualTo(expectedNode);
        } catch (Exception e) {
            throw new RuntimeException("JSON比较失败", e);
        }
    }
    
    /**
     * 创建JWT Token
     */
    public static String createJwtToken(String userId, String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("username", username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1小时
                .signWith(SignatureAlgorithm.HS512, "test-secret-key")
                .compact();
    }
}

/**
 * 自定义测试注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "logging.level.org.springframework.web=DEBUG"
})
public @interface IntegrationTest {
}

/**
 * Web层测试注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest
@Import(TestSliceConfig.WebLayerTestConfig.class)
public @interface WebLayerTest {
    Class<?>[] controllers() default {};
}

/**
 * 数据层测试注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DataJpaTest
@Import(TestSliceConfig.DataLayerTestConfig.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public @interface DataLayerTest {
}
```

## 8. 分布式事务处理

### 8.1 事务模式选择

#### 本地事务模式

```java
/**
 * 本地事务管理器配置
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
    
    /**
     * 事务模板配置
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setTimeout(30);
        return template;
    }
}

/**
 * 事务服务基类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public abstract class BaseTransactionalService {
    
    protected final TransactionTemplate transactionTemplate;
    
    protected BaseTransactionalService(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }
    
    /**
     * 执行事务操作
     */
    protected <T> T executeInTransaction(Supplier<T> operation) {
        return transactionTemplate.execute(status -> operation.get());
    }
    
    /**
     * 执行只读事务操作
     */
    @Transactional(readOnly = true)
    protected <T> T executeInReadOnlyTransaction(Supplier<T> operation) {
        return operation.get();
    }
}
```

#### Saga模式实现

```java
/**
 * Saga事务管理器
 */
@Component
@Slf4j
public class SagaTransactionManager {
    
    private final SagaRepository sagaRepository;
    private final SagaStepExecutor stepExecutor;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * 开始Saga事务
     */
    public SagaTransaction startSaga(SagaDefinition definition, Object payload) {
        SagaTransaction saga = SagaTransaction.builder()
                .sagaId(UUID.randomUUID().toString())
                .sagaType(definition.getSagaType())
                .status(SagaStatus.STARTED)
                .payload(payload)
                .steps(definition.getSteps())
                .currentStepIndex(0)
                .createdAt(Instant.now())
                .build();
        
        sagaRepository.save(saga);
        
        // 发布Saga开始事件
        eventPublisher.publishEvent(new SagaStartedEvent(saga.getSagaId()));
        
        // 执行第一步
        executeNextStep(saga);
        
        return saga;
    }
    
    /**
     * 执行下一步
     */
    public void executeNextStep(SagaTransaction saga) {
        if (saga.isCompleted()) {
            return;
        }
        
        SagaStep currentStep = saga.getCurrentStep();
        
        try {
            // 执行当前步骤
            SagaStepResult result = stepExecutor.execute(currentStep, saga.getPayload());
            
            if (result.isSuccess()) {
                // 步骤成功，移动到下一步
                saga.markStepCompleted(currentStep.getStepId(), result.getOutput());
                
                if (saga.hasNextStep()) {
                    saga.moveToNextStep();
                    executeNextStep(saga);
                } else {
                    // 所有步骤完成
                    saga.markCompleted();
                    eventPublisher.publishEvent(new SagaCompletedEvent(saga.getSagaId()));
                }
            } else {
                // 步骤失败，开始补偿
                saga.markStepFailed(currentStep.getStepId(), result.getError());
                startCompensation(saga);
            }
            
            sagaRepository.save(saga);
            
        } catch (Exception e) {
            log.error("Saga步骤执行失败: sagaId={}, stepId={}", 
                     saga.getSagaId(), currentStep.getStepId(), e);
            
            saga.markStepFailed(currentStep.getStepId(), e.getMessage());
            startCompensation(saga);
            sagaRepository.save(saga);
        }
    }
    
    /**
     * 开始补偿
     */
    private void startCompensation(SagaTransaction saga) {
        saga.startCompensation();
        
        // 发布补偿开始事件
        eventPublisher.publishEvent(new SagaCompensationStartedEvent(saga.getSagaId()));
        
        // 执行补偿步骤
        executeCompensation(saga);
    }
    
    /**
     * 执行补偿
     */
    private void executeCompensation(SagaTransaction saga) {
        List<SagaStep> completedSteps = saga.getCompletedSteps();
        
        // 按相反顺序执行补偿
        for (int i = completedSteps.size() - 1; i >= 0; i--) {
            SagaStep step = completedSteps.get(i);
            
            try {
                SagaStepResult result = stepExecutor.compensate(step, saga.getPayload());
                
                if (result.isSuccess()) {
                    saga.markStepCompensated(step.getStepId());
                } else {
                    saga.markCompensationFailed(step.getStepId(), result.getError());
                    break;
                }
                
            } catch (Exception e) {
                log.error("Saga补偿执行失败: sagaId={}, stepId={}", 
                         saga.getSagaId(), step.getStepId(), e);
                saga.markCompensationFailed(step.getStepId(), e.getMessage());
                break;
            }
        }
        
        if (saga.isCompensationCompleted()) {
            saga.markCompensated();
            eventPublisher.publishEvent(new SagaCompensatedEvent(saga.getSagaId()));
        } else {
            saga.markFailed();
            eventPublisher.publishEvent(new SagaFailedEvent(saga.getSagaId()));
        }
        
        sagaRepository.save(saga);
    }
}

/**
 * Saga定义
 */
@Data
@Builder
public class SagaDefinition {
    
    private String sagaType;
    private List<SagaStep> steps;
    private Duration timeout;
    
    public static SagaDefinition orderProcessingSaga() {
        return SagaDefinition.builder()
                .sagaType("OrderProcessing")
                .timeout(Duration.ofMinutes(10))
                .steps(Arrays.asList(
                    SagaStep.builder()
                        .stepId("validateOrder")
                        .stepType("OrderValidation")
                        .action("validate")
                        .compensationAction("invalidate")
                        .timeout(Duration.ofSeconds(30))
                        .build(),
                    SagaStep.builder()
                        .stepId("reserveInventory")
                        .stepType("InventoryReservation")
                        .action("reserve")
                        .compensationAction("release")
                        .timeout(Duration.ofSeconds(30))
                        .build(),
                    SagaStep.builder()
                        .stepId("processPayment")
                        .stepType("PaymentProcessing")
                        .action("charge")
                        .compensationAction("refund")
                        .timeout(Duration.ofSeconds(60))
                        .build(),
                    SagaStep.builder()
                        .stepId("createShipment")
                        .stepType("ShipmentCreation")
                        .action("create")
                        .compensationAction("cancel")
                        .timeout(Duration.ofSeconds(30))
                        .build()
                ))
                .build();
    }
}

/**
 * Saga步骤执行器
 */
@Component
public class SagaStepExecutor {
    
    private final Map<String, SagaStepHandler> handlers = new HashMap<>();
    
    @Autowired
    public SagaStepExecutor(List<SagaStepHandler> stepHandlers) {
        stepHandlers.forEach(handler -> 
            handlers.put(handler.getStepType(), handler));
    }
    
    public SagaStepResult execute(SagaStep step, Object payload) {
        SagaStepHandler handler = handlers.get(step.getStepType());
        
        if (handler == null) {
            throw new SagaStepHandlerNotFoundException(
                "未找到步骤处理器: " + step.getStepType());
        }
        
        return handler.execute(step, payload);
    }
    
    public SagaStepResult compensate(SagaStep step, Object payload) {
        SagaStepHandler handler = handlers.get(step.getStepType());
        
        if (handler == null) {
            throw new SagaStepHandlerNotFoundException(
                "未找到步骤处理器: " + step.getStepType());
        }
        
        return handler.compensate(step, payload);
    }
}
```

### 8.2 TCC模式实现

```java
/**
 * TCC事务管理器
 */
@Component
@Slf4j
public class TccTransactionManager {
    
    private final TccTransactionRepository tccRepository;
    private final TccParticipantExecutor participantExecutor;
    
    /**
     * 开始TCC事务
     */
    @Transactional(rollbackFor = Exception.class)
    public TccTransaction startTcc(List<TccParticipant> participants) {
        TccTransaction tcc = TccTransaction.builder()
                .tccId(UUID.randomUUID().toString())
                .status(TccStatus.TRYING)
                .participants(participants)
                .createdAt(Instant.now())
                .build();
        
        tccRepository.save(tcc);
        
        // 执行Try阶段
        boolean trySuccess = executeTryPhase(tcc);
        
        if (trySuccess) {
            // Try成功，执行Confirm
            executeConfirmPhase(tcc);
        } else {
            // Try失败，执行Cancel
            executeCancelPhase(tcc);
        }
        
        return tcc;
    }
    
    /**
     * 执行Try阶段
     */
    private boolean executeTryPhase(TccTransaction tcc) {
        log.info("开始执行TCC Try阶段: tccId={}", tcc.getTccId());
        
        for (TccParticipant participant : tcc.getParticipants()) {
            try {
                TccResult result = participantExecutor.executeTry(participant);
                
                if (result.isSuccess()) {
                    participant.markTrySuccess(result.getReservationId());
                } else {
                    participant.markTryFailed(result.getError());
                    tcc.setStatus(TccStatus.CANCELLED);
                    return false;
                }
                
            } catch (Exception e) {
                log.error("TCC Try阶段执行失败: tccId={}, participant={}", 
                         tcc.getTccId(), participant.getParticipantId(), e);
                participant.markTryFailed(e.getMessage());
                tcc.setStatus(TccStatus.CANCELLED);
                return false;
            }
        }
        
        tcc.setStatus(TccStatus.CONFIRMING);
        tccRepository.save(tcc);
        return true;
    }
    
    /**
     * 执行Confirm阶段
     */
    private void executeConfirmPhase(TccTransaction tcc) {
        log.info("开始执行TCC Confirm阶段: tccId={}", tcc.getTccId());
        
        boolean allConfirmed = true;
        
        for (TccParticipant participant : tcc.getParticipants()) {
            if (participant.getTryStatus() != TccParticipantStatus.TRY_SUCCESS) {
                continue;
            }
            
            try {
                TccResult result = participantExecutor.executeConfirm(participant);
                
                if (result.isSuccess()) {
                    participant.markConfirmSuccess();
                } else {
                    participant.markConfirmFailed(result.getError());
                    allConfirmed = false;
                }
                
            } catch (Exception e) {
                log.error("TCC Confirm阶段执行失败: tccId={}, participant={}", 
                         tcc.getTccId(), participant.getParticipantId(), e);
                participant.markConfirmFailed(e.getMessage());
                allConfirmed = false;
            }
        }
        
        if (allConfirmed) {
            tcc.setStatus(TccStatus.CONFIRMED);
        } else {
            tcc.setStatus(TccStatus.FAILED);
        }
        
        tccRepository.save(tcc);
    }
    
    /**
     * 执行Cancel阶段
     */
    private void executeCancelPhase(TccTransaction tcc) {
        log.info("开始执行TCC Cancel阶段: tccId={}", tcc.getTccId());
        
        for (TccParticipant participant : tcc.getParticipants()) {
            if (participant.getTryStatus() != TccParticipantStatus.TRY_SUCCESS) {
                continue;
            }
            
            try {
                TccResult result = participantExecutor.executeCancel(participant);
                
                if (result.isSuccess()) {
                    participant.markCancelSuccess();
                } else {
                    participant.markCancelFailed(result.getError());
                }
                
            } catch (Exception e) {
                log.error("TCC Cancel阶段执行失败: tccId={}, participant={}", 
                         tcc.getTccId(), participant.getParticipantId(), e);
                participant.markCancelFailed(e.getMessage());
            }
        }
        
        tcc.setStatus(TccStatus.CANCELLED);
        tccRepository.save(tcc);
    }
}

/**
 * TCC参与者接口
 */
public interface TccParticipantHandler {
    
    /**
     * Try操作
     */
    TccResult tryExecute(TccParticipant participant);
    
    /**
     * Confirm操作
     */
    TccResult confirmExecute(TccParticipant participant);
    
    /**
     * Cancel操作
     */
    TccResult cancelExecute(TccParticipant participant);
    
    /**
     * 获取参与者类型
     */
    String getParticipantType();
}

/**
 * 库存TCC参与者
 */
@Component
public class InventoryTccParticipantHandler implements TccParticipantHandler {
    
    private final InventoryService inventoryService;
    private final InventoryReservationRepository reservationRepository;
    
    @Override
    public TccResult tryExecute(TccParticipant participant) {
        try {
            InventoryReservationRequest request = 
                (InventoryReservationRequest) participant.getRequestData();
            
            // 预留库存
            InventoryReservation reservation = inventoryService.reserveInventory(
                request.getProductId(), 
                request.getQuantity()
            );
            
            return TccResult.success(reservation.getReservationId());
            
        } catch (InsufficientInventoryException e) {
            return TccResult.failure("库存不足: " + e.getMessage());
        } catch (Exception e) {
            return TccResult.failure("库存预留失败: " + e.getMessage());
        }
    }
    
    @Override
    public TccResult confirmExecute(TccParticipant participant) {
        try {
            String reservationId = participant.getReservationId();
            
            // 确认库存扣减
            inventoryService.confirmReservation(reservationId);
            
            return TccResult.success();
            
        } catch (Exception e) {
            return TccResult.failure("库存确认失败: " + e.getMessage());
        }
    }
    
    @Override
    public TccResult cancelExecute(TccParticipant participant) {
        try {
            String reservationId = participant.getReservationId();
            
            // 释放预留库存
            inventoryService.cancelReservation(reservationId);
            
            return TccResult.success();
            
        } catch (Exception e) {
            return TccResult.failure("库存释放失败: " + e.getMessage());
        }
    }
    
    @Override
    public String getParticipantType() {
        return "Inventory";
    }
}
```

### 8.3 事件驱动架构

```java
/**
 * 事件总线
 */
@Component
@Slf4j
public class EventBus {
    
    private final EventStore eventStore;
    private final EventPublisher eventPublisher;
    private final EventHandlerRegistry handlerRegistry;
    
    /**
     * 发布事件
     */
    public void publish(DomainEvent event) {
        try {
            // 1. 持久化事件
            eventStore.save(event);
            
            // 2. 发布到消息队列
            eventPublisher.publish(event);
            
            log.info("事件发布成功: eventId={}, eventType={}", 
                    event.getEventId(), event.getEventType());
            
        } catch (Exception e) {
            log.error("事件发布失败: eventId={}, eventType={}", 
                     event.getEventId(), event.getEventType(), e);
            throw new EventPublishException("事件发布失败", e);
        }
    }
    
    /**
     * 处理事件
     */
    public void handle(DomainEvent event) {
        List<EventHandler> handlers = handlerRegistry.getHandlers(event.getEventType());
        
        for (EventHandler handler : handlers) {
            try {
                handler.handle(event);
                
                log.info("事件处理成功: eventId={}, handler={}", 
                        event.getEventId(), handler.getClass().getSimpleName());
                
            } catch (Exception e) {
                log.error("事件处理失败: eventId={}, handler={}", 
                         event.getEventId(), handler.getClass().getSimpleName(), e);
                
                // 记录失败事件，用于重试
                recordFailedEvent(event, handler, e);
            }
        }
    }
    
    private void recordFailedEvent(DomainEvent event, EventHandler handler, Exception error) {
        FailedEvent failedEvent = FailedEvent.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .handlerClass(handler.getClass().getName())
                .errorMessage(error.getMessage())
                .retryCount(0)
                .maxRetryCount(3)
                .nextRetryAt(Instant.now().plus(Duration.ofMinutes(1)))
                .createdAt(Instant.now())
                .build();
        
        eventStore.saveFailedEvent(failedEvent);
    }
}

/**
 * 事件重试机制
 */
@Component
@Slf4j
public class EventRetryProcessor {
    
    private final EventStore eventStore;
    private final EventBus eventBus;
    
    @Scheduled(fixedDelay = 60000) // 每分钟执行一次
    public void processFailedEvents() {
        List<FailedEvent> failedEvents = eventStore.findRetryableFailedEvents();
        
        for (FailedEvent failedEvent : failedEvents) {
            try {
                // 重新加载原始事件
                DomainEvent originalEvent = eventStore.findById(failedEvent.getEventId());
                
                if (originalEvent != null) {
                    // 重试处理
                    eventBus.handle(originalEvent);
                    
                    // 标记为已处理
                    eventStore.markFailedEventAsProcessed(failedEvent.getId());
                    
                    log.info("失败事件重试成功: eventId={}", failedEvent.getEventId());
                }
                
            } catch (Exception e) {
                // 增加重试次数
                failedEvent.incrementRetryCount();
                
                if (failedEvent.getRetryCount() >= failedEvent.getMaxRetryCount()) {
                    // 达到最大重试次数，标记为永久失败
                    eventStore.markFailedEventAsPermanentlyFailed(failedEvent.getId());
                    
                    log.error("失败事件达到最大重试次数: eventId={}", 
                             failedEvent.getEventId());
                } else {
                    // 更新下次重试时间
                    failedEvent.setNextRetryAt(calculateNextRetryTime(failedEvent.getRetryCount()));
                    eventStore.updateFailedEvent(failedEvent);
                    
                    log.warn("失败事件重试失败，将在稍后重试: eventId={}, retryCount={}", 
                            failedEvent.getEventId(), failedEvent.getRetryCount());
                }
            }
        }
    }
    
    private Instant calculateNextRetryTime(int retryCount) {
        // 指数退避策略
        long delayMinutes = (long) Math.pow(2, retryCount);
        return Instant.now().plus(Duration.ofMinutes(delayMinutes));
    }
}
```

### 8.4 最终一致性保证

```java
/**
 * 最终一致性检查器
 */
@Component
@Slf4j
public class EventualConsistencyChecker {
    
    private final List<ConsistencyRule> consistencyRules;
    private final ConsistencyViolationHandler violationHandler;
    
    @Scheduled(fixedDelay = 300000) // 每5分钟检查一次
    public void checkConsistency() {
        for (ConsistencyRule rule : consistencyRules) {
            try {
                List<ConsistencyViolation> violations = rule.check();
                
                if (!violations.isEmpty()) {
                    log.warn("发现一致性违规: rule={}, violations={}", 
                            rule.getRuleName(), violations.size());
                    
                    for (ConsistencyViolation violation : violations) {
                        violationHandler.handle(violation);
                    }
                }
                
            } catch (Exception e) {
                log.error("一致性检查失败: rule={}", rule.getRuleName(), e);
            }
        }
    }
}

/**
 * 一致性规则接口
 */
public interface ConsistencyRule {
    
    /**
     * 检查一致性
     */
    List<ConsistencyViolation> check();
    
    /**
     * 获取规则名称
     */
    String getRuleName();
}

/**
 * 订单库存一致性规则
 */
@Component
public class OrderInventoryConsistencyRule implements ConsistencyRule {
    
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    
    @Override
    public List<ConsistencyViolation> check() {
        List<ConsistencyViolation> violations = new ArrayList<>();
        
        // 查找已确认但库存未扣减的订单
        List<Order> confirmedOrders = orderRepository.findByStatus(OrderStatus.CONFIRMED);
        
        for (Order order : confirmedOrders) {
            for (OrderItem item : order.getItems()) {
                Inventory inventory = inventoryRepository.findByProductId(item.getProductId());
                
                if (inventory.getReservedQuantity() < item.getQuantity()) {
                    violations.add(ConsistencyViolation.builder()
                            .violationType("OrderInventoryMismatch")
                            .entityId(order.getId())
                            .description(String.format(
                                "订单 %s 的商品 %s 库存预留不足，需要 %d，实际 %d",
                                order.getId(), item.getProductId(), 
                                item.getQuantity(), inventory.getReservedQuantity()))
                            .detectedAt(Instant.now())
                            .build());
                }
            }
        }
        
        return violations;
    }
    
    @Override
    public String getRuleName() {
        return "OrderInventoryConsistency";
    }
}
```

---

## 9. 性能优化

### 15.1 数据库优化

#### 索引优化

```sql
-- 用户表索引优化
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_status ON users(status);
CREATE INDEX idx_user_created_at ON users(created_at);
CREATE INDEX idx_user_status_created_at ON users(status, created_at);

-- 订单表索引优化
CREATE INDEX idx_order_user_id ON orders(user_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created_at ON orders(created_at);
CREATE INDEX idx_order_user_status ON orders(user_id, status);

-- 复合索引示例
CREATE INDEX idx_order_user_status_date ON orders(user_id, status, created_at);
```

#### 查询优化

```java
/**
 * 查询优化示例
 */
@Repository
public class OptimizedUserRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * 批量查询优化
     */
    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findByIds(@Param("ids") List<String> ids);
    
    /**
     * 分页查询优化
     */
    @Query(value = "SELECT u FROM User u WHERE u.status = :status ORDER BY u.createdAt DESC",
           countQuery = "SELECT COUNT(u) FROM User u WHERE u.status = :status")
    Page<User> findByStatusOptimized(@Param("status") UserStatus status, Pageable pageable);
    
    /**
     * 投影查询优化
     */
    @Query("SELECT new com.rose.user.dto.UserSummaryDTO(u.id, u.username, u.email, u.status) " +
           "FROM User u WHERE u.status = :status")
    List<UserSummaryDTO> findUserSummaries(@Param("status") UserStatus status);
    
    /**
     * 原生SQL查询优化
     */
    @Query(value = "SELECT * FROM users u " +
                   "WHERE u.status = ?1 " +
                   "AND u.created_at >= ?2 " +
                   "ORDER BY u.created_at DESC " +
                   "LIMIT ?3", nativeQuery = true)
    List<User> findRecentActiveUsers(String status, LocalDateTime since, int limit);
}
```

#### 连接池优化

```yaml
# application.yml
spring:
  datasource:
    hikari:
      # 连接池配置
      minimum-idle: 10
      maximum-pool-size: 50
      idle-timeout: 300000
      max-lifetime: 1800000
      connection-timeout: 30000
      validation-timeout: 5000
      leak-detection-threshold: 60000
      
      # 连接池监控
      register-mbeans: true
      
      # 数据库连接配置
      connection-test-query: SELECT 1
      connection-init-sql: SET NAMES utf8mb4
      
  jpa:
    hibernate:
      # 批量操作优化
      jdbc:
        batch_size: 50
        order_inserts: true
        order_updates: true
        batch_versioned_data: true
    properties:
      hibernate:
        # 查询优化
        query:
          plan_cache_max_size: 2048
          plan_parameter_metadata_max_size: 128
        # 缓存配置
        cache:
          use_second_level_cache: true
          use_query_cache: true
          region:
            factory_class: org.hibernate.cache.jcache.JCacheRegionFactory
```

### 15.2 缓存优化

#### 多级缓存策略

```java
/**
 * 多级缓存配置
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CompositeCacheManager cacheManager = new CompositeCacheManager();
        
        // L1缓存：本地缓存
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats());
        
        // L2缓存：Redis缓存
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30))
                        .serializeKeysWith(RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer())))
                .build();
        
        cacheManager.setCacheManagers(Arrays.asList(caffeineCacheManager, redisCacheManager));
        cacheManager.setFallbackToNoOpCache(false);
        
        return cacheManager;
    }
}

/**
 * 缓存服务
 */
@Service
@Slf4j
public class CacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final Cache<String, Object> localCache;
    
    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.localCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats()
                .build();
    }
    
    /**
     * 获取缓存数据
     */
    public <T> T get(String key, Class<T> type) {
        // 先查本地缓存
        Object value = localCache.getIfPresent(key);
        if (value != null) {
            log.debug("从本地缓存获取数据: {}", key);
            return type.cast(value);
        }
        
        // 再查Redis缓存
        value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.debug("从Redis缓存获取数据: {}", key);
            // 回写本地缓存
            localCache.put(key, value);
            return type.cast(value);
        }
        
        return null;
    }
    
    /**
     * 设置缓存数据
     */
    public void set(String key, Object value, Duration ttl) {
        // 写入Redis缓存
        redisTemplate.opsForValue().set(key, value, ttl);
        
        // 写入本地缓存
        localCache.put(key, value);
        
        log.debug("设置缓存数据: {}", key);
    }
    
    /**
     * 删除缓存数据
     */
    public void delete(String key) {
        redisTemplate.delete(key);
        localCache.invalidate(key);
        log.debug("删除缓存数据: {}", key);
    }
}
```

#### 缓存预热和更新策略

```java
/**
 * 缓存预热服务
 */
@Service
@Slf4j
public class CacheWarmupService {
    
    private final UserRepository userRepository;
    private final CacheService cacheService;
    
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCache() {
        log.info("开始缓存预热...");
        
        // 预热热点用户数据
        warmupHotUsers();
        
        // 预热系统配置
        warmupSystemConfig();
        
        log.info("缓存预热完成");
    }
    
    private void warmupHotUsers() {
        List<User> hotUsers = userRepository.findHotUsers(100);
        for (User user : hotUsers) {
            String key = "user:" + user.getId();
            cacheService.set(key, user, Duration.ofHours(1));
        }
        log.info("预热热点用户数据: {} 个", hotUsers.size());
    }
    
    private void warmupSystemConfig() {
        // 预热系统配置数据
        Map<String, Object> configs = loadSystemConfigs();
        for (Map.Entry<String, Object> entry : configs.entrySet()) {
            String key = "config:" + entry.getKey();
            cacheService.set(key, entry.getValue(), Duration.ofDays(1));
        }
        log.info("预热系统配置: {} 项", configs.size());
    }
}

/**
 * 缓存更新策略
 */
@Component
@Slf4j
public class CacheUpdateStrategy {
    
    private final CacheService cacheService;
    
    @EventListener
    @Async
    public void handleUserUpdated(UserUpdatedEvent event) {
        String key = "user:" + event.getAggregateId();
        
        // 延迟双删策略
        cacheService.delete(key);
        
        // 延迟一段时间后再次删除
        CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS)
                .execute(() -> cacheService.delete(key));
        
        log.debug("更新用户缓存: {}", event.getAggregateId());
    }
    
    @EventListener
    @Async
    public void handleUserDeleted(UserDeletedEvent event) {
        String key = "user:" + event.getAggregateId();
        cacheService.delete(key);
        log.debug("删除用户缓存: {}", event.getAggregateId());
    }
}
```

### 15.3 异步处理优化

#### 线程池配置

```java
/**
 * 异步配置
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(10);
        
        // 最大线程数
        executor.setMaxPoolSize(50);
        
        // 队列容量
        executor.setQueueCapacity(200);
        
        // 线程名前缀
        executor.setThreadNamePrefix("async-");
        
        // 拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待任务完成后关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("异步方法执行异常: method={}, params={}", method.getName(), params, ex);
        };
    }
    
    @Bean("eventExecutor")
    public Executor eventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("event-");
        executor.initialize();
        return executor;
    }
}
```

#### 批量处理优化

```java
/**
 * 批量处理服务
 */
@Service
@Slf4j
public class BatchProcessingService {
    
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    /**
     * 批量创建用户
     */
    @Transactional
    public List<User> batchCreateUsers(List<CreateUserCommand> commands) {
        List<User> users = new ArrayList<>();
        
        // 分批处理，避免内存溢出
        int batchSize = 100;
        for (int i = 0; i < commands.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, commands.size());
            List<CreateUserCommand> batch = commands.subList(i, endIndex);
            
            List<User> batchUsers = batch.stream()
                    .map(this::createUser)
                    .collect(Collectors.toList());
            
            // 批量保存
            userRepository.saveAll(batchUsers);
            users.addAll(batchUsers);
            
            // 清理持久化上下文
            entityManager.flush();
            entityManager.clear();
            
            log.debug("批量创建用户: {}/{}", endIndex, commands.size());
        }
        
        return users;
    }
    
    /**
     * 批量发送通知
     */
    @Async("eventExecutor")
    public CompletableFuture<Void> batchSendNotifications(List<String> userIds, String message) {
        return CompletableFuture.runAsync(() -> {
            // 分批发送通知
            int batchSize = 50;
            for (int i = 0; i < userIds.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, userIds.size());
                List<String> batch = userIds.subList(i, endIndex);
                
                try {
                    notificationService.batchSend(batch, message);
                    log.debug("批量发送通知: {}/{}", endIndex, userIds.size());
                } catch (Exception e) {
                    log.error("批量发送通知失败: batch={}", batch, e);
                }
                
                // 避免过快发送
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
}
```

### 15.4 JVM优化

```bash
# JVM启动参数优化
JAVA_OPTS="
# 堆内存配置
-Xms2g -Xmx4g
-XX:NewRatio=3
-XX:SurvivorRatio=8

# 垃圾收集器配置
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:G1NewSizePercent=30
-XX:G1MaxNewSizePercent=40

# GC日志配置
-XX:+PrintGC
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintGCApplicationStoppedTime
-Xloggc:/var/log/gc.log
-XX:+UseGCLogFileRotation
-XX:NumberOfGCLogFiles=5
-XX:GCLogFileSize=100M

# JIT编译优化
-XX:+TieredCompilation
-XX:TieredStopAtLevel=4

# 内存溢出处理
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/heapdump.hprof

# 远程调试
-Dcom.sun.management.jmxremote
-Dcom.sun.management.jmxremote.port=9999
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false
"
```



### 9.1 分页查询优化

```java
/**
 * 分页查询优化
 */
@Service
public class UserQueryService {
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 优化的分页查询
     */
    public PageResponse<UserDTO> findUserPage(UserListQuery query) {
        // 使用覆盖索引优化count查询
        Page<User> page = query.toPage();
        
        // 先查询总数（使用覆盖索引）
        Long total = userMapper.countByQuery(query);
        page.setTotal(total);
        
        // 如果没有数据，直接返回
        if (total == 0) {
            return PageResponse.<UserDTO>builder()
                    .current(page.getCurrent())
                    .size(page.getSize())
                    .total(0L)
                    .pages(0L)
                    .records(Collections.emptyList())
                    .hasNext(false)
                    .hasPrevious(false)
                    .build();
        }
        
        // 查询数据（避免查询不必要的字段）
        List<User> users = userMapper.selectPageByQuery(page, query);
        page.setRecords(users);
        
        // 转换为DTO
        List<UserDTO> userDTOs = users.stream()
                .map(UserAssembler::toDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<UserDTO>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .records(userDTOs)
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
    
    /**
     * 批量查询优化
     */
    @Cacheable(value = "users", key = "#ids")
    public List<UserDTO> findByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        
        // 分批查询，避免IN子句过长
        List<UserDTO> result = new ArrayList<>();
        List<List<String>> batches = Lists.partition(ids, 1000);
        
        for (List<String> batch : batches) {
            List<User> users = userMapper.selectBatchIds(batch);
            List<UserDTO> dtos = users.stream()
                    .map(UserAssembler::toDTO)
                    .collect(Collectors.toList());
            result.addAll(dtos);
        }
        
        return result;
    }
}
```

### 9.2 慢查询监控配置

#### 9.2.1 慢查询拦截器

```java
/**
 * 慢查询监控拦截器
 */
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
@Component
@Slf4j
public class SlowQueryInterceptor implements Interceptor {
    
    /**
     * 慢查询阈值（毫秒）
     */
    private static final long SLOW_QUERY_THRESHOLD = 1000L;
    
    /**
     * 超慢查询阈值（毫秒）
     */
    private static final long VERY_SLOW_QUERY_THRESHOLD = 5000L;
    
    @Autowired
    private SlowQueryService slowQueryService;
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行SQL
            Object result = invocation.proceed();
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // 检查是否为慢查询
            if (executionTime >= SLOW_QUERY_THRESHOLD) {
                handleSlowQuery(invocation, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // 记录异常查询
            handleErrorQuery(invocation, executionTime, e);
            throw e;
        }
    }
    
    /**
     * 处理慢查询
     */
    private void handleSlowQuery(Invocation invocation, long executionTime) {
        try {
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            Object parameter = invocation.getArgs()[1];
            
            // 获取SQL信息
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            String sql = boundSql.getSql();
            
            // 构建慢查询记录
            SlowQueryRecord record = SlowQueryRecord.builder()
                .sqlId(mappedStatement.getId())
                .sql(formatSql(sql))
                .parameters(JsonUtils.toJson(parameter))
                .executionTime(executionTime)
                .queryType(getQueryType(sql))
                .level(getSlowQueryLevel(executionTime))
                .stackTrace(getStackTrace())
                .occurredAt(LocalDateTime.now())
                .build();
            
            // 异步保存慢查询记录
            slowQueryService.saveAsync(record);
            
            // 记录日志
            if (executionTime >= VERY_SLOW_QUERY_THRESHOLD) {
                log.error("检测到超慢查询: sqlId={}, executionTime={}ms, sql={}", 
                         mappedStatement.getId(), executionTime, sql);
            } else {
                log.warn("检测到慢查询: sqlId={}, executionTime={}ms, sql={}", 
                        mappedStatement.getId(), executionTime, sql);
            }
            
        } catch (Exception e) {
            log.error("处理慢查询记录失败", e);
        }
    }
    
    /**
     * 处理异常查询
     */
    private void handleErrorQuery(Invocation invocation, long executionTime, Exception exception) {
        try {
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            Object parameter = invocation.getArgs()[1];
            
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            String sql = boundSql.getSql();
            
            // 构建错误查询记录
            ErrorQueryRecord record = ErrorQueryRecord.builder()
                .sqlId(mappedStatement.getId())
                .sql(formatSql(sql))
                .parameters(JsonUtils.toJson(parameter))
                .executionTime(executionTime)
                .errorMessage(exception.getMessage())
                .stackTrace(getStackTrace())
                .occurredAt(LocalDateTime.now())
                .build();
            
            // 异步保存错误查询记录
            slowQueryService.saveErrorQueryAsync(record);
            
            log.error("检测到异常查询: sqlId={}, executionTime={}ms, error={}, sql={}", 
                     mappedStatement.getId(), executionTime, exception.getMessage(), sql);
            
        } catch (Exception e) {
            log.error("处理异常查询记录失败", e);
        }
    }
    
    /**
     * 格式化SQL
     */
    private String formatSql(String sql) {
        if (sql == null) {
            return "";
        }
        
        // 移除多余的空白字符
        return sql.replaceAll("\\s+", " ").trim();
    }
    
    /**
     * 获取查询类型
     */
    private String getQueryType(String sql) {
        if (sql == null) {
            return "UNKNOWN";
        }
        
        String upperSql = sql.trim().toUpperCase();
        if (upperSql.startsWith("SELECT")) {
            return "SELECT";
        } else if (upperSql.startsWith("INSERT")) {
            return "INSERT";
        } else if (upperSql.startsWith("UPDATE")) {
            return "UPDATE";
        } else if (upperSql.startsWith("DELETE")) {
            return "DELETE";
        } else {
            return "OTHER";
        }
    }
    
    /**
     * 获取慢查询级别
     */
    private String getSlowQueryLevel(long executionTime) {
        if (executionTime >= VERY_SLOW_QUERY_THRESHOLD) {
            return "CRITICAL";
        } else if (executionTime >= SLOW_QUERY_THRESHOLD * 3) {
            return "HIGH";
        } else if (executionTime >= SLOW_QUERY_THRESHOLD * 2) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    /**
     * 获取调用栈信息
     */
    private String getStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder();
        
        // 只保留前10层调用栈，并过滤框架相关的调用
        int count = 0;
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            
            // 跳过框架相关的类
            if (className.startsWith("java.") || 
                className.startsWith("org.apache.ibatis.") ||
                className.startsWith("com.baomidou.mybatisplus.") ||
                className.startsWith("org.springframework.") ||
                className.contains("$$")) {
                continue;
            }
            
            sb.append(element.toString()).append("\n");
            count++;
            
            if (count >= 10) {
                break;
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    
    @Override
    public void setProperties(Properties properties) {
        // 可以从配置文件中读取慢查询阈值等参数
    }
}
```

#### 9.2.3 慢查询服务

```java
/**
 * 慢查询服务
 */
@Service
@Slf4j
public class SlowQueryService {
    
    @Autowired
    private SlowQueryMapper slowQueryMapper;
    
    @Autowired
    private ErrorQueryMapper errorQueryMapper;
    
    @Autowired
    @Qualifier("slowQueryExecutor")
    private TaskExecutor taskExecutor;
    
    /**
     * 异步保存慢查询记录
     */
    @Async("slowQueryExecutor")
    public void saveAsync(SlowQueryRecord record) {
        try {
            SlowQueryPO po = SlowQueryConverter.toPO(record);
            slowQueryMapper.insert(po);
            
            log.debug("慢查询记录保存成功: sqlId={}", record.getSqlId());
        } catch (Exception e) {
            log.error("保存慢查询记录失败: sqlId={}", record.getSqlId(), e);
        }
    }
    
    /**
     * 异步保存错误查询记录
     */
    @Async("slowQueryExecutor")
    public void saveErrorQueryAsync(ErrorQueryRecord record) {
        try {
            ErrorQueryPO po = ErrorQueryConverter.toPO(record);
            errorQueryMapper.insert(po);
            
            log.debug("错误查询记录保存成功: sqlId={}", record.getSqlId());
        } catch (Exception e) {
            log.error("保存错误查询记录失败: sqlId={}", record.getSqlId(), e);
        }
    }
    
    /**
     * 查询慢查询统计
     */
    public SlowQueryStatistics getStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("查询慢查询统计: startTime={}, endTime={}", startTime, endTime);
        
        // 查询总数
        Long totalCount = slowQueryMapper.countByTimeRange(startTime, endTime);
        
        // 查询各级别数量
        Map<String, Long> levelCounts = slowQueryMapper.countByLevel(startTime, endTime);
        
        // 查询各类型数量
        Map<String, Long> typeCounts = slowQueryMapper.countByType(startTime, endTime);
        
        // 查询平均执行时间
        Double avgExecutionTime = slowQueryMapper.avgExecutionTime(startTime, endTime);
        
        // 查询最慢的查询
        List<SlowQueryPO> slowestQueries = slowQueryMapper.findSlowest(startTime, endTime, 10);
        
        // 查询最频繁的慢查询
        List<SlowQueryFrequency> frequencies = slowQueryMapper.findMostFrequent(startTime, endTime, 10);
        
        return SlowQueryStatistics.builder()
            .totalCount(totalCount)
            .levelCounts(levelCounts)
            .typeCounts(typeCounts)
            .avgExecutionTime(avgExecutionTime)
            .slowestQueries(slowestQueries.stream()
                .map(SlowQueryConverter::toDomain)
                .collect(Collectors.toList()))
            .frequencies(frequencies)
            .build();
    }
    
    /**
     * 分页查询慢查询记录
     */
    public PageResponse<SlowQueryRecord> findSlowQueries(SlowQueryQueryRequest request, PageRequest pageRequest) {
        log.info("分页查询慢查询记录: request={}", request);
        
        LambdaQueryWrapper<SlowQueryPO> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(request.getSqlId())) {
            wrapper.like(SlowQueryPO::getSqlId, request.getSqlId());
        }
        
        if (StringUtils.hasText(request.getQueryType())) {
            wrapper.eq(SlowQueryPO::getQueryType, request.getQueryType());
        }
        
        if (StringUtils.hasText(request.getLevel())) {
            wrapper.eq(SlowQueryPO::getLevel, request.getLevel());
        }
        
        if (request.getMinExecutionTime() != null) {
            wrapper.ge(SlowQueryPO::getExecutionTime, request.getMinExecutionTime());
        }
        
        if (request.getMaxExecutionTime() != null) {
            wrapper.le(SlowQueryPO::getExecutionTime, request.getMaxExecutionTime());
        }
        
        if (request.getOccurredAtStart() != null) {
            wrapper.ge(SlowQueryPO::getOccurredAt, request.getOccurredAtStart());
        }
        
        if (request.getOccurredAtEnd() != null) {
            wrapper.le(SlowQueryPO::getOccurredAt, request.getOccurredAtEnd());
        }
        
        wrapper.orderByDesc(SlowQueryPO::getOccurredAt);
        
        Page<SlowQueryPO> page = slowQueryMapper.selectPage(pageRequest.toPage(), wrapper);
        
        List<SlowQueryRecord> records = page.getRecords().stream()
            .map(SlowQueryConverter::toDomain)
            .collect(Collectors.toList());
        
        return PageResponse.<SlowQueryRecord>builder()
            .current(page.getCurrent())
            .size(page.getSize())
            .total(page.getTotal())
            .pages(page.getPages())
            .records(records)
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}
```

### 9.3 查询优化工具

#### 9.3.1 SQL分析器

```java
/**
 * SQL分析器
 */
@Component
@Slf4j
public class SqlAnalyzer {
    
    /**
     * 分析SQL性能
     */
    public SqlAnalysisResult analyze(String sql, Map<String, Object> parameters) {
        log.info("开始分析SQL: {}", sql);
        
        SqlAnalysisResult result = new SqlAnalysisResult();
        result.setSql(sql);
        result.setParameters(parameters);
        
        // 1. 语法分析
        analyzeSyntax(sql, result);
        
        // 2. 性能分析
        analyzePerformance(sql, result);
        
        // 3. 索引建议
        suggestIndexes(sql, result);
        
        // 4. 优化建议
        suggestOptimizations(sql, result);
        
        log.info("SQL分析完成: score={}", result.getScore());
        return result;
    }
    
    /**
     * 语法分析
     */
    private void analyzeSyntax(String sql, SqlAnalysisResult result) {
        List<String> issues = new ArrayList<>();
        
        String upperSql = sql.toUpperCase();
        
        // 检查是否使用SELECT *
        if (upperSql.contains("SELECT *")) {
            issues.add("避免使用SELECT *，应明确指定需要的字段");
        }
        
        // 检查是否缺少WHERE条件
        if (upperSql.startsWith("SELECT") && !upperSql.contains("WHERE") && !upperSql.contains("LIMIT")) {
            issues.add("SELECT语句缺少WHERE条件，可能导致全表扫描");
        }
        
        // 检查是否使用了函数在WHERE条件中
        if (upperSql.contains("WHERE") && (upperSql.contains("UPPER(") || upperSql.contains("LOWER(") || 
            upperSql.contains("SUBSTRING(") || upperSql.contains("DATE("))) {
            issues.add("避免在WHERE条件中使用函数，会导致索引失效");
        }
        
        // 检查是否使用了LIKE '%xxx'
        if (upperSql.contains("LIKE '%")) {
            issues.add("避免使用前置通配符的LIKE查询，会导致索引失效");
        }
        
        // 检查是否使用了OR条件
        if (upperSql.contains(" OR ")) {
            issues.add("OR条件可能导致索引失效，考虑使用UNION替代");
        }
        
        // 检查是否使用了NOT IN
        if (upperSql.contains("NOT IN")) {
            issues.add("NOT IN可能导致性能问题，考虑使用NOT EXISTS替代");
        }
        
        result.setSyntaxIssues(issues);
    }
    
    /**
     * 性能分析
     */
    private void analyzePerformance(String sql, SqlAnalysisResult result) {
        int score = 100;
        List<String> suggestions = new ArrayList<>();
        
        String upperSql = sql.toUpperCase();
        
        // 根据语法问题扣分
        score -= result.getSyntaxIssues().size() * 10;
        
        // 检查JOIN数量
        long joinCount = upperSql.split("JOIN").length - 1;
        if (joinCount > 3) {
            score -= 20;
            suggestions.add("JOIN表数量过多(" + joinCount + ")，考虑优化查询逻辑");
        }
        
        // 检查子查询
        if (upperSql.contains("SELECT") && upperSql.indexOf("SELECT") != upperSql.lastIndexOf("SELECT")) {
            score -= 15;
            suggestions.add("包含子查询，考虑使用JOIN替代");
        }
        
        // 检查ORDER BY
        if (upperSql.contains("ORDER BY") && !upperSql.contains("LIMIT")) {
            score -= 10;
            suggestions.add("ORDER BY没有配合LIMIT使用，可能影响性能");
        }
        
        // 检查GROUP BY
        if (upperSql.contains("GROUP BY")) {
            score -= 5;
            suggestions.add("使用了GROUP BY，确保相关字段有索引");
        }
        
        result.setScore(Math.max(score, 0));
        result.setPerformanceSuggestions(suggestions);
    }
    
    /**
     * 索引建议
     */
    private void suggestIndexes(String sql, SqlAnalysisResult result) {
        List<String> indexSuggestions = new ArrayList<>();
        
        // 简单的索引建议逻辑
        String upperSql = sql.toUpperCase();
        
        // 提取WHERE条件中的字段
        if (upperSql.contains("WHERE")) {
            indexSuggestions.add("为WHERE条件中的字段创建索引");
        }
        
        // 提取JOIN条件中的字段
        if (upperSql.contains("JOIN")) {
            indexSuggestions.add("为JOIN条件中的字段创建索引");
        }
        
        // 提取ORDER BY中的字段
        if (upperSql.contains("ORDER BY")) {
            indexSuggestions.add("为ORDER BY字段创建索引");
        }
        
        result.setIndexSuggestions(indexSuggestions);
    }
    
    /**
     * 优化建议
     */
    private void suggestOptimizations(String sql, SqlAnalysisResult result) {
        List<String> optimizations = new ArrayList<>();
        
        String upperSql = sql.toUpperCase();
        
        // 通用优化建议
        if (upperSql.contains("SELECT *")) {
            optimizations.add("使用具体字段名替代SELECT *");
        }
        
        if (upperSql.contains("LIKE '%")) {
            optimizations.add("考虑使用全文索引替代前置通配符LIKE");
        }
        
        if (upperSql.contains(" OR ")) {
            optimizations.add("考虑将OR条件拆分为多个查询并使用UNION");
        }
        
        if (!upperSql.contains("LIMIT") && upperSql.startsWith("SELECT")) {
            optimizations.add("添加LIMIT限制返回结果数量");
        }
        
        result.setOptimizationSuggestions(optimizations);
    }
}
```

#### 9.3.2 查询缓存管理

```java
/**
 * 查询缓存管理器
 */
@Component
@Slf4j
public class QueryCacheManager {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_PREFIX = "query:cache:";
    private static final String CACHE_STATS_PREFIX = "query:stats:";
    
    /**
     * 获取缓存的查询结果
     */
    @SuppressWarnings("unchecked")
    public <T> T getCachedResult(String cacheKey, Class<T> resultType) {
        try {
            String key = CACHE_PREFIX + cacheKey;
            Object cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                // 更新缓存命中统计
                updateCacheStats(cacheKey, true);
                log.debug("缓存命中: key={}", cacheKey);
                return (T) cached;
            } else {
                // 更新缓存未命中统计
                updateCacheStats(cacheKey, false);
                log.debug("缓存未命中: key={}", cacheKey);
                return null;
            }
        } catch (Exception e) {
            log.error("获取缓存失败: key={}", cacheKey, e);
            return null;
        }
    }
    
    /**
     * 缓存查询结果
     */
    public void cacheResult(String cacheKey, Object result, Duration expiration) {
        try {
            String key = CACHE_PREFIX + cacheKey;
            redisTemplate.opsForValue().set(key, result, expiration);
            
            log.debug("缓存查询结果: key={}, expiration={}", cacheKey, expiration);
        } catch (Exception e) {
            log.error("缓存查询结果失败: key={}", cacheKey, e);
        }
    }
    
    /**
     * 清除缓存
     */
    public void evictCache(String cacheKey) {
        try {
            String key = CACHE_PREFIX + cacheKey;
            redisTemplate.delete(key);
            
            log.debug("清除缓存: key={}", cacheKey);
        } catch (Exception e) {
            log.error("清除缓存失败: key={}", cacheKey, e);
        }
    }
    
    /**
     * 批量清除缓存
     */
    public void evictCacheByPattern(String pattern) {
        try {
            String keyPattern = CACHE_PREFIX + pattern;
            Set<String> keys = redisTemplate.keys(keyPattern);
            
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("批量清除缓存: pattern={}, count={}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.error("批量清除缓存失败: pattern={}", pattern, e);
        }
    }
    
    /**
     * 更新缓存统计
     */
    private void updateCacheStats(String cacheKey, boolean hit) {
        try {
            String statsKey = CACHE_STATS_PREFIX + cacheKey;
            
            if (hit) {
                redisTemplate.opsForHash().increment(statsKey, "hits", 1);
            } else {
                redisTemplate.opsForHash().increment(statsKey, "misses", 1);
            }
            
            // 设置统计数据过期时间
            redisTemplate.expire(statsKey, Duration.ofDays(7));
        } catch (Exception e) {
            log.error("更新缓存统计失败: key={}", cacheKey, e);
        }
    }
    
    /**
     * 获取缓存统计
     */
    public CacheStatistics getCacheStatistics(String cacheKey) {
        try {
            String statsKey = CACHE_STATS_PREFIX + cacheKey;
            Map<Object, Object> stats = redisTemplate.opsForHash().entries(statsKey);
            
            long hits = Long.parseLong(stats.getOrDefault("hits", "0").toString());
            long misses = Long.parseLong(stats.getOrDefault("misses", "0").toString());
            long total = hits + misses;
            double hitRate = total > 0 ? (double) hits / total : 0.0;
            
            return CacheStatistics.builder()
                .cacheKey(cacheKey)
                .hits(hits)
                .misses(misses)
                .total(total)
                .hitRate(hitRate)
                .build();
        } catch (Exception e) {
            log.error("获取缓存统计失败: key={}", cacheKey, e);
            return CacheStatistics.builder()
                .cacheKey(cacheKey)
                .hits(0L)
                .misses(0L)
                .total(0L)
                .hitRate(0.0)
                .build();
        }
    }
}
```

### 9.4 数据库连接池优化

#### 9.4.1 HikariCP配置优化

```yaml
# application.yml
spring:
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    hikari:
      # 连接池名称
      pool-name: HikariPool-Rose
      
      # 最小空闲连接数
      minimum-idle: 10
      
      # 最大连接数
      maximum-pool-size: 50
      
      # 连接超时时间（毫秒）
      connection-timeout: 30000
      
      # 空闲连接超时时间（毫秒）
      idle-timeout: 600000
      
      # 连接最大生命周期（毫秒）
      max-lifetime: 1800000
      
      # 连接测试查询
      connection-test-query: SELECT 1
      
      # 是否自动提交
      auto-commit: true
      
      # 连接初始化SQL
      connection-init-sql: SET NAMES utf8mb4
      
      # 数据源属性
      data-source-properties:
        # 缓存预处理语句
        cachePrepStmts: true
        # 预处理语句缓存大小
        prepStmtCacheSize: 250
        # 预处理语句最大长度
        prepStmtCacheSqlLimit: 2048
        # 使用服务器端预处理语句
        useServerPrepStmts: true
        # 使用本地会话状态
        useLocalSessionState: true
        # 重写批量查询
        rewriteBatchedStatements: true
        # 缓存结果集元数据
        cacheResultSetMetadata: true
        # 缓存服务器配置
        cacheServerConfiguration: true
        # 启用查询缓存
        elideSetAutoCommits: true
        # 维护结果集元数据
        maintainTimeStats: false
```

#### 8.3.2 连接池监控

```java
/**
 * 连接池监控配置
 */
@Configuration
@Slf4j
public class DataSourceMonitorConfig {
    
    @Autowired
    private HikariDataSource dataSource;
    
    /**
     * 连接池监控任务
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void monitorConnectionPool() {
        try {
            HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
            
            if (poolMXBean != null) {
                int activeConnections = poolMXBean.getActiveConnections();
                int idleConnections = poolMXBean.getIdleConnections();
                int totalConnections = poolMXBean.getTotalConnections();
                int threadsAwaitingConnection = poolMXBean.getThreadsAwaitingConnection();
                
                log.info("连接池状态 - 活跃连接: {}, 空闲连接: {}, 总连接: {}, 等待连接的线程: {}", 
                        activeConnections, idleConnections, totalConnections, threadsAwaitingConnection);
                
                // 检查连接池健康状态
                checkConnectionPoolHealth(activeConnections, totalConnections, threadsAwaitingConnection);
            }
        } catch (Exception e) {
            log.error("监控连接池状态失败", e);
        }
    }
    
    /**
     * 检查连接池健康状态
     */
    private void checkConnectionPoolHealth(int activeConnections, int totalConnections, int threadsAwaitingConnection) {
        // 连接使用率过高警告
        double usageRate = (double) activeConnections / totalConnections;
        if (usageRate > 0.8) {
            log.warn("连接池使用率过高: {}%, 活跃连接: {}, 总连接: {}", 
                    String.format("%.2f", usageRate * 100), activeConnections, totalConnections);
        }
        
        // 等待连接的线程过多警告
        if (threadsAwaitingConnection > 5) {
            log.warn("等待连接的线程过多: {}, 可能存在连接泄漏或连接池配置不当", threadsAwaitingConnection);
        }
    }
    
    /**
     * 连接池健康检查端点
     */
    @Bean
    public HealthIndicator dataSourceHealthIndicator() {
        return new AbstractHealthIndicator() {
            @Override
            protected void doHealthCheck(Health.Builder builder) throws Exception {
                try {
                    HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
                    
                    if (poolMXBean != null) {
                        int activeConnections = poolMXBean.getActiveConnections();
                        int totalConnections = poolMXBean.getTotalConnections();
                        int threadsAwaitingConnection = poolMXBean.getThreadsAwaitingConnection();
                        
                        builder.up()
                            .withDetail("activeConnections", activeConnections)
                            .withDetail("totalConnections", totalConnections)
                            .withDetail("threadsAwaitingConnection", threadsAwaitingConnection)
                            .withDetail("usageRate", String.format("%.2f%%", 
                                (double) activeConnections / totalConnections * 100));
                        
                        // 如果等待连接的线程过多，标记为DOWN
                        if (threadsAwaitingConnection > 10) {
                            builder.down().withDetail("reason", "Too many threads awaiting connection");
                        }
                    } else {
                        builder.down().withDetail("reason", "Unable to get pool MXBean");
                    }
                } catch (Exception e) {
                    builder.down(e);
                }
            }
        };
    }
}
```

## 10. 性能监控与指标收集

### 10.1 应用性能监控

#### 10.1.1 Micrometer集成

```java
/**
 * 性能监控配置
 */
@Configuration
@EnableConfigurationProperties(MonitoringProperties.class)
@Slf4j
public class MonitoringConfig {
    
    @Autowired
    private MonitoringProperties monitoringProperties;
    
    /**
     * 自定义MeterRegistry
     */
    @Bean
    @Primary
    public MeterRegistry meterRegistry() {
        CompositeMeterRegistry composite = new CompositeMeterRegistry();
        
        // 添加Prometheus注册表
        if (monitoringProperties.getPrometheus().isEnabled()) {
            composite.add(prometheusMeterRegistry());
        }
        
        // 添加JMX注册表
        if (monitoringProperties.getJmx().isEnabled()) {
            composite.add(jmxMeterRegistry());
        }
        
        // 配置通用标签
        composite.config()
            .commonTags("application", monitoringProperties.getApplicationName())
            .commonTags("instance", getInstanceId())
            .commonTags("environment", monitoringProperties.getEnvironment());
        
        return composite;
    }
    
    /**
     * Prometheus注册表
     */
    @Bean
    @ConditionalOnProperty(name = "monitoring.prometheus.enabled", havingValue = "true")
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }
    
    /**
     * JMX注册表
     */
    @Bean
    @ConditionalOnProperty(name = "monitoring.jmx.enabled", havingValue = "true")
    public JmxMeterRegistry jmxMeterRegistry() {
        return new JmxMeterRegistry(JmxConfig.DEFAULT, Clock.SYSTEM);
    }
    
    /**
     * 自定义业务指标
     */
    @Bean
    public BusinessMetrics businessMetrics(MeterRegistry meterRegistry) {
        return new BusinessMetrics(meterRegistry);
    }
    
    /**
     * 性能监控拦截器
     */
    @Bean
    public PerformanceMonitorInterceptor performanceMonitorInterceptor(MeterRegistry meterRegistry) {
        return new PerformanceMonitorInterceptor(meterRegistry);
    }
    
    /**
     * 获取实例ID
     */
    private String getInstanceId() {
        try {
            return InetAddress.getLocalHost().getHostName() + ":" + 
                   System.getProperty("server.port", "8080");
        } catch (Exception e) {
            return "unknown";
        }
    }
}
```

#### 10.1.2 业务指标收集

```java
/**
 * 业务指标收集器
 */
@Component
@Slf4j
public class BusinessMetrics {
    
    private final MeterRegistry meterRegistry;
    
    // 计数器
    private final Counter userRegistrationCounter;
    private final Counter userLoginCounter;
    private final Counter orderCreatedCounter;
    private final Counter paymentSuccessCounter;
    private final Counter paymentFailureCounter;
    
    // 计时器
    private final Timer userServiceTimer;
    private final Timer orderServiceTimer;
    private final Timer paymentServiceTimer;
    
    // 仪表盘
    private final Gauge activeUsersGauge;
    private final Gauge pendingOrdersGauge;
    
    // 分布摘要
    private final DistributionSummary orderAmountSummary;
    
    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // 初始化计数器
        this.userRegistrationCounter = Counter.builder("user.registration.total")
            .description("Total number of user registrations")
            .register(meterRegistry);
            
        this.userLoginCounter = Counter.builder("user.login.total")
            .description("Total number of user logins")
            .register(meterRegistry);
            
        this.orderCreatedCounter = Counter.builder("order.created.total")
            .description("Total number of orders created")
            .register(meterRegistry);
            
        this.paymentSuccessCounter = Counter.builder("payment.success.total")
            .description("Total number of successful payments")
            .register(meterRegistry);
            
        this.paymentFailureCounter = Counter.builder("payment.failure.total")
            .description("Total number of failed payments")
            .register(meterRegistry);
        
        // 初始化计时器
        this.userServiceTimer = Timer.builder("user.service.duration")
            .description("User service execution time")
            .register(meterRegistry);
            
        this.orderServiceTimer = Timer.builder("order.service.duration")
            .description("Order service execution time")
            .register(meterRegistry);
            
        this.paymentServiceTimer = Timer.builder("payment.service.duration")
            .description("Payment service execution time")
            .register(meterRegistry);
        
        // 初始化仪表盘
        this.activeUsersGauge = Gauge.builder("user.active.count")
            .description("Number of active users")
            .register(meterRegistry, this, BusinessMetrics::getActiveUsersCount);
            
        this.pendingOrdersGauge = Gauge.builder("order.pending.count")
            .description("Number of pending orders")
            .register(meterRegistry, this, BusinessMetrics::getPendingOrdersCount);
        
        // 初始化分布摘要
        this.orderAmountSummary = DistributionSummary.builder("order.amount")
            .description("Order amount distribution")
            .baseUnit("yuan")
            .register(meterRegistry);
    }
    
    /**
     * 记录用户注册
     */
    public void recordUserRegistration(String source) {
        userRegistrationCounter.increment(Tags.of("source", source));
        log.debug("记录用户注册指标: source={}", source);
    }
    
    /**
     * 记录用户登录
     */
    public void recordUserLogin(String method) {
        userLoginCounter.increment(Tags.of("method", method));
        log.debug("记录用户登录指标: method={}", method);
    }
    
    /**
     * 记录订单创建
     */
    public void recordOrderCreated(String orderType, BigDecimal amount) {
        orderCreatedCounter.increment(Tags.of("type", orderType));
        orderAmountSummary.record(amount.doubleValue());
        log.debug("记录订单创建指标: type={}, amount={}", orderType, amount);
    }
    
    /**
     * 记录支付成功
     */
    public void recordPaymentSuccess(String paymentMethod, BigDecimal amount) {
        paymentSuccessCounter.increment(Tags.of("method", paymentMethod));
        log.debug("记录支付成功指标: method={}, amount={}", paymentMethod, amount);
    }
    
    /**
     * 记录支付失败
     */
    public void recordPaymentFailure(String paymentMethod, String errorCode) {
        paymentFailureCounter.increment(Tags.of("method", paymentMethod, "error", errorCode));
        log.debug("记录支付失败指标: method={}, error={}", paymentMethod, errorCode);
    }
    
    /**
     * 记录用户服务执行时间
     */
    public void recordUserServiceTime(Duration duration, String operation) {
        userServiceTimer.record(duration, Tags.of("operation", operation));
        log.debug("记录用户服务执行时间: operation={}, duration={}ms", operation, duration.toMillis());
    }
    
    /**
     * 记录订单服务执行时间
     */
    public void recordOrderServiceTime(Duration duration, String operation) {
        orderServiceTimer.record(duration, Tags.of("operation", operation));
        log.debug("记录订单服务执行时间: operation={}, duration={}ms", operation, duration.toMillis());
    }
    
    /**
     * 记录支付服务执行时间
     */
    public void recordPaymentServiceTime(Duration duration, String operation) {
        paymentServiceTimer.record(duration, Tags.of("operation", operation));
        log.debug("记录支付服务执行时间: operation={}, duration={}ms", operation, duration.toMillis());
    }
    
    /**
     * 获取活跃用户数量
     */
    private double getActiveUsersCount() {
        // 这里应该从缓存或数据库中获取实际的活跃用户数量
        // 为了示例，返回一个模拟值
        return 1000.0;
    }
    
    /**
     * 获取待处理订单数量
     */
    private double getPendingOrdersCount() {
        // 这里应该从数据库中获取实际的待处理订单数量
        // 为了示例，返回一个模拟值
        return 50.0;
    }
}
```

#### 10.1.3 性能监控拦截器

```java
/**
 * 性能监控拦截器
 */
@Component
@Slf4j
public class PerformanceMonitorInterceptor implements HandlerInterceptor {
    
    private final MeterRegistry meterRegistry;
    private final Timer httpRequestTimer;
    private final Counter httpRequestCounter;
    
    private static final String START_TIME_ATTRIBUTE = "startTime";
    
    public PerformanceMonitorInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.httpRequestTimer = Timer.builder("http.request.duration")
            .description("HTTP request execution time")
            .register(meterRegistry);
            
        this.httpRequestCounter = Counter.builder("http.request.total")
            .description("Total number of HTTP requests")
            .register(meterRegistry);
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String status = String.valueOf(response.getStatus());
            
            // 记录请求计数
            httpRequestCounter.increment(Tags.of(
                "method", method,
                "uri", uri,
                "status", status
            ));
            
            // 记录请求耗时
            httpRequestTimer.record(Duration.ofMillis(duration), Tags.of(
                "method", method,
                "uri", uri,
                "status", status
            ));
            
            // 记录慢请求
            if (duration > 1000) {
                log.warn("检测到慢HTTP请求: method={}, uri={}, duration={}ms", method, uri, duration);
            }
        }
    }
}
```

### 10.2 系统资源监控

#### 10.2.1 JVM监控

```java
/**
 * JVM监控配置
 */
@Configuration
@Slf4j
public class JvmMonitoringConfig {
    
    /**
     * JVM指标绑定
     */
    @Bean
    public MeterBinder jvmMetrics() {
        return new JvmMetrics();
    }
    
    /**
     * JVM GC指标绑定
     */
    @Bean
    public MeterBinder jvmGcMetrics() {
        return new JvmGcMetrics();
    }
    
    /**
     * JVM内存指标绑定
     */
    @Bean
    public MeterBinder jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }
    
    /**
     * JVM线程指标绑定
     */
    @Bean
    public MeterBinder jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }
    
    /**
     * 类加载指标绑定
     */
    @Bean
    public MeterBinder classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }
    
    /**
     * 自定义JVM监控
     */
    @Component
    public static class CustomJvmMonitor {
        
        private final MeterRegistry meterRegistry;
        private final Gauge heapUsageGauge;
        private final Gauge nonHeapUsageGauge;
        private final Gauge threadCountGauge;
        
        public CustomJvmMonitor(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
            
            // 堆内存使用率
            this.heapUsageGauge = Gauge.builder("jvm.memory.heap.usage.ratio")
                .description("Heap memory usage ratio")
                .register(meterRegistry, this, CustomJvmMonitor::getHeapUsageRatio);
            
            // 非堆内存使用率
            this.nonHeapUsageGauge = Gauge.builder("jvm.memory.nonheap.usage.ratio")
                .description("Non-heap memory usage ratio")
                .register(meterRegistry, this, CustomJvmMonitor::getNonHeapUsageRatio);
            
            // 线程数量
            this.threadCountGauge = Gauge.builder("jvm.threads.count")
                .description("Current thread count")
                .register(meterRegistry, this, CustomJvmMonitor::getThreadCount);
        }
        
        /**
         * 获取堆内存使用率
         */
        private double getHeapUsageRatio() {
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            
            long used = heapMemoryUsage.getUsed();
            long max = heapMemoryUsage.getMax();
            
            return max > 0 ? (double) used / max : 0.0;
        }
        
        /**
         * 获取非堆内存使用率
         */
        private double getNonHeapUsageRatio() {
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
            
            long used = nonHeapMemoryUsage.getUsed();
            long max = nonHeapMemoryUsage.getMax();
            
            return max > 0 ? (double) used / max : 0.0;
        }
        
        /**
         * 获取线程数量
         */
        private double getThreadCount() {
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            return threadMXBean.getThreadCount();
        }
        
        /**
         * JVM健康检查
         */
        @Scheduled(fixedRate = 30000) // 每30秒检查一次
        public void checkJvmHealth() {
            double heapUsage = getHeapUsageRatio();
            double nonHeapUsage = getNonHeapUsageRatio();
            double threadCount = getThreadCount();
            
            log.info("JVM状态 - 堆内存使用率: {:.2f}%, 非堆内存使用率: {:.2f}%, 线程数: {}", 
                    heapUsage * 100, nonHeapUsage * 100, threadCount);
            
            // 内存使用率过高警告
            if (heapUsage > 0.8) {
                log.warn("堆内存使用率过高: {:.2f}%", heapUsage * 100);
            }
            
            if (nonHeapUsage > 0.8) {
                log.warn("非堆内存使用率过高: {:.2f}%", nonHeapUsage * 100);
            }
            
            // 线程数过多警告
            if (threadCount > 500) {
                log.warn("线程数过多: {}", threadCount);
            }
        }
    }
}
```

#### 10.2.2 数据库连接池监控

```java
/**
 * 数据库连接池监控
 */
@Component
@Slf4j
public class DataSourceMetrics implements MeterBinder {
    
    private final HikariDataSource dataSource;
    
    public DataSourceMetrics(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void bindTo(MeterRegistry registry) {
        if (dataSource.getHikariPoolMXBean() != null) {
            HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
            
            // 活跃连接数
            Gauge.builder("hikari.connections.active")
                .description("Active connections")
                .register(registry, poolMXBean, HikariPoolMXBean::getActiveConnections);
            
            // 空闲连接数
            Gauge.builder("hikari.connections.idle")
                .description("Idle connections")
                .register(registry, poolMXBean, HikariPoolMXBean::getIdleConnections);
            
            // 总连接数
            Gauge.builder("hikari.connections.total")
                .description("Total connections")
                .register(registry, poolMXBean, HikariPoolMXBean::getTotalConnections);
            
            // 等待连接的线程数
            Gauge.builder("hikari.connections.pending")
                .description("Threads awaiting connection")
                .register(registry, poolMXBean, HikariPoolMXBean::getThreadsAwaitingConnection);
            
            // 连接使用率
            Gauge.builder("hikari.connections.usage")
                .description("Connection usage ratio")
                .register(registry, poolMXBean, bean -> {
                    int active = bean.getActiveConnections();
                    int total = bean.getTotalConnections();
                    return total > 0 ? (double) active / total : 0.0;
                });
        }
    }
}
```

### 10.3 缓存监控

#### 10.3.1 Redis监控

```java
/**
 * Redis监控
 */
@Component
@Slf4j
public class RedisMetrics implements MeterBinder {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RedisMetrics(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public void bindTo(MeterRegistry registry) {
        // Redis连接数
        Gauge.builder("redis.connections.active")
            .description("Active Redis connections")
            .register(registry, this, RedisMetrics::getActiveConnections);
        
        // Redis内存使用
        Gauge.builder("redis.memory.used")
            .description("Redis memory usage in bytes")
            .register(registry, this, RedisMetrics::getUsedMemory);
        
        // Redis键数量
        Gauge.builder("redis.keys.count")
            .description("Number of keys in Redis")
            .register(registry, this, RedisMetrics::getKeyCount);
    }
    
    /**
     * 获取活跃连接数
     */
    private double getActiveConnections() {
        try {
            Properties info = redisTemplate.execute((RedisCallback<Properties>) connection -> {
                return connection.info("clients");
            });
            
            if (info != null) {
                String connectedClients = info.getProperty("connected_clients");
                return connectedClients != null ? Double.parseDouble(connectedClients) : 0.0;
            }
        } catch (Exception e) {
            log.error("获取Redis连接数失败", e);
        }
        return 0.0;
    }
    
    /**
     * 获取内存使用量
     */
    private double getUsedMemory() {
        try {
            Properties info = redisTemplate.execute((RedisCallback<Properties>) connection -> {
                return connection.info("memory");
            });
            
            if (info != null) {
                String usedMemory = info.getProperty("used_memory");
                return usedMemory != null ? Double.parseDouble(usedMemory) : 0.0;
            }
        } catch (Exception e) {
            log.error("获取Redis内存使用量失败", e);
        }
        return 0.0;
    }
    
    /**
     * 获取键数量
     */
    private double getKeyCount() {
        try {
            Long keyCount = redisTemplate.execute((RedisCallback<Long>) connection -> {
                return connection.dbSize();
            });
            
            return keyCount != null ? keyCount.doubleValue() : 0.0;
        } catch (Exception e) {
            log.error("获取Redis键数量失败", e);
        }
        return 0.0;
    }
}
```

#### 10.3.2 缓存性能监控

```java
/**
 * 缓存性能监控
 */
@Component
@Slf4j
public class CachePerformanceMonitor {
    
    private final MeterRegistry meterRegistry;
    private final Timer cacheGetTimer;
    private final Timer cacheSetTimer;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    
    public CachePerformanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.cacheGetTimer = Timer.builder("cache.get.duration")
            .description("Cache get operation duration")
            .register(meterRegistry);
            
        this.cacheSetTimer = Timer.builder("cache.set.duration")
            .description("Cache set operation duration")
            .register(meterRegistry);
            
        this.cacheHitCounter = Counter.builder("cache.hit.total")
            .description("Total cache hits")
            .register(meterRegistry);
            
        this.cacheMissCounter = Counter.builder("cache.miss.total")
            .description("Total cache misses")
            .register(meterRegistry);
    }
    
    /**
     * 记录缓存获取操作
     */
    public void recordCacheGet(String cacheName, Duration duration, boolean hit) {
        cacheGetTimer.record(duration, Tags.of("cache", cacheName));
        
        if (hit) {
            cacheHitCounter.increment(Tags.of("cache", cacheName));
        } else {
            cacheMissCounter.increment(Tags.of("cache", cacheName));
        }
        
        log.debug("记录缓存获取指标: cache={}, duration={}ms, hit={}", 
                 cacheName, duration.toMillis(), hit);
    }
    
    /**
     * 记录缓存设置操作
     */
    public void recordCacheSet(String cacheName, Duration duration) {
        cacheSetTimer.record(duration, Tags.of("cache", cacheName));
        
        log.debug("记录缓存设置指标: cache={}, duration={}ms", 
                 cacheName, duration.toMillis());
    }
    
    /**
     * 获取缓存命中率
     */
    public double getCacheHitRate(String cacheName) {
        Counter hitCounter = meterRegistry.find("cache.hit.total")
            .tag("cache", cacheName)
            .counter();
            
        Counter missCounter = meterRegistry.find("cache.miss.total")
            .tag("cache", cacheName)
            .counter();
        
        if (hitCounter != null && missCounter != null) {
            double hits = hitCounter.count();
            double misses = missCounter.count();
            double total = hits + misses;
            
            return total > 0 ? hits / total : 0.0;
        }
        
        return 0.0;
    }
}
```

### 10.4 监控配置属性

```java
/**
 * 监控配置属性
 */
@ConfigurationProperties(prefix = "monitoring")
@Data
public class MonitoringProperties {
    
    /**
     * 应用名称
     */
    private String applicationName = "rose-monolithic";
    
    /**
     * 环境
     */
    private String environment = "dev";
    
    /**
     * Prometheus配置
     */
    private PrometheusConfig prometheus = new PrometheusConfig();
    
    /**
     * JMX配置
     */
    private JmxConfig jmx = new JmxConfig();
    
    /**
     * 业务指标配置
     */
    private BusinessConfig business = new BusinessConfig();
    
    @Data
    public static class PrometheusConfig {
        /**
         * 是否启用Prometheus
         */
        private boolean enabled = true;
        
        /**
         * 端点路径
         */
        private String path = "/actuator/prometheus";
    }
    
    @Data
    public static class JmxConfig {
        /**
         * 是否启用JMX
         */
        private boolean enabled = true;
        
        /**
         * 域名
         */
        private String domain = "rose.metrics";
    }
    
    @Data
    public static class BusinessConfig {
        /**
         * 是否启用业务指标
         */
        private boolean enabled = true;
        
        /**
         * 指标收集间隔（秒）
         */
        private int interval = 60;
    }
}
```

### 10.5 日志监控

```java
/**
 * 结构化日志配置
 */
@Configuration
public class LoggingConfig {
    
    @Bean
    public Logger structuredLogger() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        JsonEncoder jsonEncoder = new JsonEncoder();
        jsonEncoder.setContext(context);
        jsonEncoder.start();
        
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        appender.setEncoder(jsonEncoder);
        appender.start();
        
        ch.qos.logback.classic.Logger logger = context.getLogger("STRUCTURED");
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);
        logger.setAdditive(false);
        
        return logger;
    }
}

/**
 * 审计日志记录器
 */
@Component
@Slf4j
public class AuditLogger {
    
    private final Logger auditLog = LoggerFactory.getLogger("AUDIT");
    
    public void logUserAction(String userId, String action, String resource, 
                             String result, Map<String, Object> details) {
        
        AuditLogEntry entry = AuditLogEntry.builder()
                .timestamp(Instant.now())
                .userId(userId)
                .action(action)
                .resource(resource)
                .result(result)
                .details(details)
                .ipAddress(RequestContextHolder.getClientIpAddress())
                .userAgent(RequestContextHolder.getUserAgent())
                .build();
        
        auditLog.info("Audit: {}", entry);
    }
    
    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        logUserAction(
            event.getAggregateId(),
            "CREATE_USER",
            "User",
            "SUCCESS",
            Map.of(
                "username", event.getUsername(),
                "email", event.getEmail()
            )
        );
    }
}
```

### 10.6 告警配置

```java
/**
 * 告警管理器
 */
@Component
@Slf4j
public class AlertManager {
    
    private final NotificationService notificationService;
    private final AlertRuleRepository alertRuleRepository;
    
    @EventListener
    public void handleHealthStatusChanged(HealthStatusChangedEvent event) {
        if (event.getStatus() == Health.Status.DOWN) {
            Alert alert = Alert.builder()
                    .alertType("HEALTH_CHECK_FAILED")
                    .severity(AlertSeverity.CRITICAL)
                    .title("健康检查失败")
                    .message(String.format("组件 %s 健康检查失败", event.getComponent()))
                    .timestamp(Instant.now())
                    .build();
            
            sendAlert(alert);
        }
    }
    
    @EventListener
    public void handleHighErrorRate(HighErrorRateEvent event) {
        Alert alert = Alert.builder()
                .alertType("HIGH_ERROR_RATE")
                .severity(AlertSeverity.WARNING)
                .title("错误率过高")
                .message(String.format("接口 %s 错误率达到 %.2f%%", 
                        event.getEndpoint(), event.getErrorRate() * 100))
                .timestamp(Instant.now())
                .build();
        
        sendAlert(alert);
    }
    
    private void sendAlert(Alert alert) {
        List<AlertRule> rules = alertRuleRepository.findByAlertType(alert.getAlertType());
        
        for (AlertRule rule : rules) {
            if (rule.matches(alert)) {
                for (String channel : rule.getNotificationChannels()) {
                    notificationService.send(channel, alert);
                }
            }
        }
    }
}

/**
 * 通知服务
 */
@Service
@Slf4j
public class NotificationService {
    
    private final EmailService emailService;
    private final SlackService slackService;
    private final SmsService smsService;
    
    public void send(String channel, Alert alert) {
        try {
            switch (channel.toLowerCase()) {
                case "email":
                    sendEmailAlert(alert);
                    break;
                case "slack":
                    sendSlackAlert(alert);
                    break;
                case "sms":
                    sendSmsAlert(alert);
                    break;
                default:
                    log.warn("未知的通知渠道: {}", channel);
            }
        } catch (Exception e) {
            log.error("发送告警通知失败: channel={}, alert={}", channel, alert, e);
        }
    }
    
    private void sendEmailAlert(Alert alert) {
        EmailMessage message = EmailMessage.builder()
                .to(getAlertRecipients("email"))
                .subject(String.format("[%s] %s", alert.getSeverity(), alert.getTitle()))
                .body(buildAlertEmailBody(alert))
                .build();
        
        emailService.send(message);
    }
    
    private void sendSlackAlert(Alert alert) {
        SlackMessage message = SlackMessage.builder()
                .channel("#alerts")
                .text(buildAlertSlackMessage(alert))
                .color(getAlertColor(alert.getSeverity()))
                .build();
        
        slackService.send(message);
    }
}
```

---

## 12. 部署与运维

### 12.1 Docker容器化

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制jar文件
COPY target/rose-monolithic-*.jar app.jar

# 创建非root用户
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/rose
      - SPRING_REDIS_HOST=redis
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=rose
      - MYSQL_USER=rose
      - MYSQL_PASSWORD=rose123
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./scripts/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources

volumes:
  mysql_data:
  redis_data:
  prometheus_data:
  grafana_data:
```

### 12.2 Kubernetes部署

```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: rose

---
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: rose-config
  namespace: rose
data:
  application.yml: |
    spring:
      profiles:
        active: k8s
      datasource:
        url: jdbc:mysql://mysql-service:3306/rose
        username: rose
        password: ${DB_PASSWORD}
      redis:
        host: redis-service
        port: 6379

---
# k8s/secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: rose-secret
  namespace: rose
type: Opaque
data:
  db-password: cm9zZTEyMw== # base64 encoded "rose123"

---
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rose-app
  namespace: rose
  labels:
    app: rose
spec:
  replicas: 3
  selector:
    matchLabels:
      app: rose
  template:
    metadata:
      labels:
        app: rose
    spec:
      containers:
      - name: rose
        image: rose/monolithic:latest
        ports:
        - containerPort: 8080
        env:
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: rose-secret
              key: db-password
        volumeMounts:
        - name: config
          mountPath: /app/config
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
      volumes:
      - name: config
        configMap:
          name: rose-config

---
# k8s/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: rose-service
  namespace: rose
spec:
  selector:
    app: rose
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP

---
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: rose-ingress
  namespace: rose
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - api.rose.com
    secretName: rose-tls
  rules:
  - host: api.rose.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: rose-service
            port:
              number: 80

---
# k8s/hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: rose-hpa
  namespace: rose
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: rose-app
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### 12.3 CI/CD流水线

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: rose_test
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3
      
      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: >-
          --health-cmd="redis-cli ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
    
    - name: Run tests
      run: mvn clean test
      env:
        SPRING_PROFILES_ACTIVE: test
        SPRING_DATASOURCE_URL: jdbc:mysql://localhost:3306/rose_test
        SPRING_REDIS_HOST: localhost
    
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: target/surefire-reports/*.xml
        reporter: java-junit
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: target/site/jacoco/jacoco.xml

  build:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build application
      run: mvn clean package -DskipTests
    
    - name: Build Docker image
      run: |
        docker build -t rose/monolithic:${{ github.sha }} .
        docker tag rose/monolithic:${{ github.sha }} rose/monolithic:latest
    
    - name: Login to Docker Hub
      uses: docker/login-action@v2
      with:
        username: ${{ secrets.DOCKER_USERNAME }}
        password: ${{ secrets.DOCKER_PASSWORD }}
    
    - name: Push Docker image
      run: |
        docker push rose/monolithic:${{ github.sha }}
        docker push rose/monolithic:latest

  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Deploy to Kubernetes
      uses: azure/k8s-deploy@v1
      with:
        manifests: |
          k8s/deployment.yaml
          k8s/service.yaml
          k8s/ingress.yaml
        images: |
          rose/monolithic:${{ github.sha }}
        kubectl-version: 'latest'
```

### 12.4 运维脚本

```bash
#!/bin/bash
# scripts/deploy.sh

set -e

# 配置变量
APP_NAME="rose-monolithic"
DOCKER_IMAGE="rose/monolithic"
NAMESPACE="rose"
ENVIRONMENT=${1:-"staging"}

echo "开始部署 $APP_NAME 到 $ENVIRONMENT 环境..."

# 构建Docker镜像
echo "构建Docker镜像..."
docker build -t $DOCKER_IMAGE:latest .

# 推送到镜像仓库
echo "推送镜像到仓库..."
docker push $DOCKER_IMAGE:latest

# 更新Kubernetes部署
echo "更新Kubernetes部署..."
kubectl set image deployment/$APP_NAME $APP_NAME=$DOCKER_IMAGE:latest -n $NAMESPACE

# 等待部署完成
echo "等待部署完成..."
kubectl rollout status deployment/$APP_NAME -n $NAMESPACE

# 验证部署
echo "验证部署状态..."
kubectl get pods -n $NAMESPACE -l app=$APP_NAME

echo "部署完成！"
```

```bash
#!/bin/bash
# scripts/backup.sh

set -e

# 配置变量
BACKUP_DIR="/backup"
DATE=$(date +%Y%m%d_%H%M%S)
DB_HOST="localhost"
DB_NAME="rose"
DB_USER="rose"
DB_PASSWORD="rose123"

echo "开始数据库备份..."

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份数据库
mysqldump -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME > $BACKUP_DIR/rose_backup_$DATE.sql

# 压缩备份文件
gzip $BACKUP_DIR/rose_backup_$DATE.sql

# 删除7天前的备份
find $BACKUP_DIR -name "rose_backup_*.sql.gz" -mtime +7 -delete

echo "数据库备份完成: rose_backup_$DATE.sql.gz"
```

---

## 13. 测试策略

### 13.1 单元测试

#### 领域层测试

```java
/**
 * 用户聚合单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserTest {
    
    @Test
    @DisplayName("创建用户 - 成功")
    void createUser_Success() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        
        // When
        User user = User.create(username, email, password);
        
        // Then
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserCreatedEvent.class);
    }
    
    @Test
    @DisplayName("激活用户 - 成功")
    void activateUser_Success() {
        // Given
        User user = User.create("testuser", "test@example.com", "password123");
        user.clearDomainEvents();
        
        // When
        user.activate();
        
        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserActivatedEvent.class);
    }
    
    @Test
    @DisplayName("激活用户 - 用户已激活")
    void activateUser_AlreadyActive() {
        // Given
        User user = User.create("testuser", "test@example.com", "password123");
        user.activate();
        user.clearDomainEvents();
        
        // When & Then
        assertThatThrownBy(() -> user.activate())
                .isInstanceOf(UserException.class)
                .hasMessage("用户已经是激活状态");
    }
    
    @Test
    @DisplayName("更改邮箱 - 成功")
    void changeEmail_Success() {
        // Given
        User user = User.create("testuser", "test@example.com", "password123");
        user.clearDomainEvents();
        String newEmail = "newemail@example.com";
        
        // When
        user.changeEmail(newEmail);
        
        // Then
        assertThat(user.getEmail()).isEqualTo(newEmail);
        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserEmailChangedEvent.class);
    }
}
```

#### 应用层测试

```java
/**
 * 用户应用服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserApplicationService userApplicationService;
    
    @Test
    @DisplayName("创建用户 - 成功")
    void createUser_Success() {
        // Given
        CreateUserCommand command = CreateUserCommand.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        UserDTO result = userApplicationService.createUser(command);
        
        // Then
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
        
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishEvents(any(User.class));
    }
    
    @Test
    @DisplayName("创建用户 - 用户名已存在")
    void createUser_UsernameExists() {
        // Given
        CreateUserCommand command = CreateUserCommand.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> userApplicationService.createUser(command))
                .isInstanceOf(UserException.class)
                .hasMessage("用户名已存在");
        
        verify(userRepository, never()).save(any(User.class));
    }
}
```

### 13.2 集成测试

#### 仓储层测试

```java
/**
 * 用户仓储集成测试
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
class UserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    @DisplayName("根据用户名查找用户 - 存在")
    void findByUsername_Exists() {
        // Given
        User user = User.create("testuser", "test@example.com", "password123");
        entityManager.persistAndFlush(user);
        
        // When
        Optional<User> result = userRepository.findByUsername("testuser");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }
    
    @Test
    @DisplayName("根据用户名查找用户 - 不存在")
    void findByUsername_NotExists() {
        // When
        Optional<User> result = userRepository.findByUsername("nonexistent");
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("检查用户名是否存在 - 存在")
    void existsByUsername_Exists() {
        // Given
        User user = User.create("testuser", "test@example.com", "password123");
        entityManager.persistAndFlush(user);
        
        // When
        boolean exists = userRepository.existsByUsername("testuser");
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("分页查询用户")
    void findUsersWithPagination() {
        // Given
        for (int i = 1; i <= 15; i++) {
            User user = User.create("user" + i, "user" + i + "@example.com", "password");
            entityManager.persist(user);
        }
        entityManager.flush();
        
        // When
        Pageable pageable = PageRequest.of(0, 10, Sort.by("username"));
        Page<User> result = userRepository.findAll(pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(15);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("user1");
    }
}
```

#### Web层测试

```java
/**
 * 用户控制器集成测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserControllerIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }
    
    @Test
    @DisplayName("创建用户 - 成功")
    void createUser_Success() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();
        
        // When
        ResponseEntity<ApiResponse<UserDTO>> response = restTemplate.postForEntity(
                "/api/users", request, new ParameterizedTypeReference<ApiResponse<UserDTO>>() {});
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getCode()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getData().getUsername()).isEqualTo("testuser");
        
        // 验证数据库
        Optional<User> savedUser = userRepository.findByUsername("testuser");
        assertThat(savedUser).isPresent();
    }
    
    @Test
    @DisplayName("创建用户 - 用户名已存在")
    void createUser_UsernameExists() {
        // Given
        User existingUser = User.create("testuser", "existing@example.com", "password");
        userRepository.save(existingUser);
        
        CreateUserRequest request = CreateUserRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        
        // When
        ResponseEntity<ApiResponse<Void>> response = restTemplate.postForEntity(
                "/api/users", request, new ParameterizedTypeReference<ApiResponse<Void>>() {});
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getCode()).isEqualTo("USER_USERNAME_EXISTS");
    }
    
    @Test
    @DisplayName("查询用户列表 - 分页")
    void getUsers_WithPagination() {
        // Given
        for (int i = 1; i <= 15; i++) {
            User user = User.create("user" + i, "user" + i + "@example.com", "password");
            userRepository.save(user);
        }
        
        // When
        ResponseEntity<ApiResponse<PageResponse<UserDTO>>> response = restTemplate.getForEntity(
                "/api/users?page=1&size=10", 
                new ParameterizedTypeReference<ApiResponse<PageResponse<UserDTO>>>() {});
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getData()).hasSize(10);
        assertThat(response.getBody().getData().getTotal()).isEqualTo(15);
        assertThat(response.getBody().getData().getTotalPages()).isEqualTo(2);
    }
}
```

### 13.3 性能测试

```java
/**
 * 用户服务性能测试
 */
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class UserServicePerformanceTest {
    
    @Autowired
    private UserApplicationService userApplicationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    @Order(1)
    @DisplayName("批量创建用户性能测试")
    void batchCreateUsers_Performance() {
        // Given
        int userCount = 1000;
        List<CreateUserCommand> commands = new ArrayList<>();
        
        for (int i = 1; i <= userCount; i++) {
            commands.add(CreateUserCommand.builder()
                    .username("perfuser" + i)
                    .email("perfuser" + i + "@example.com")
                    .password("password123")
                    .firstName("Perf")
                    .lastName("User" + i)
                    .build());
        }
        
        // When
        long startTime = System.currentTimeMillis();
        
        for (CreateUserCommand command : commands) {
            userApplicationService.createUser(command);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        System.out.println("创建 " + userCount + " 个用户耗时: " + duration + "ms");
        System.out.println("平均每个用户耗时: " + (duration / userCount) + "ms");
        
        assertThat(duration).isLessThan(30000); // 30秒内完成
        assertThat(userRepository.count()).isEqualTo(userCount);
    }
    
    @Test
    @Order(2)
    @DisplayName("分页查询性能测试")
    void pageQuery_Performance() {
        // Given
        int pageSize = 20;
        int totalPages = 50; // 查询50页
        
        // When
        long startTime = System.currentTimeMillis();
        
        for (int page = 0; page < totalPages; page++) {
            UserQuery query = UserQuery.builder()
                    .page(page)
                    .size(pageSize)
                    .sortBy("username")
                    .sortDirection("ASC")
                    .build();
            
            userApplicationService.getUsers(query);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        System.out.println("查询 " + totalPages + " 页数据耗时: " + duration + "ms");
        System.out.println("平均每页耗时: " + (duration / totalPages) + "ms");
        
        assertThat(duration).isLessThan(5000); // 5秒内完成
    }
}
```



## 16. 总结

### 16.1 DDD分层架构的优势

1. **清晰的职责分离**
   - 每一层都有明确的职责和边界
   - 降低了系统的复杂性和耦合度
   - 提高了代码的可维护性和可测试性

2. **业务逻辑的集中管理**
   - 领域层集中管理核心业务逻辑
   - 避免业务逻辑散落在各个层次
   - 提高了业务规则的一致性

3. **良好的扩展性**
   - 基于接口的设计便于扩展和替换
   - 支持不同的技术栈和实现方式
   - 适应业务需求的变化

4. **高质量的代码**
   - 遵循SOLID原则和设计模式
   - 代码结构清晰，易于理解
   - 便于团队协作开发

### 16.2 实施建议

1. **循序渐进**
   - 从简单的聚合开始实践
   - 逐步完善领域模型
   - 持续重构和优化

2. **团队培训**
   - 确保团队理解DDD的核心概念
   - 建立统一的编码规范
   - 定期进行代码评审

3. **工具支持**
   - 使用合适的开发框架和工具
   - 建立完善的测试体系
   - 配置持续集成和部署

4. **监控和优化**
   - 建立完善的监控体系
   - 定期进行性能优化
   - 持续改进架构设计

### 16.3 注意事项

1. **避免过度设计**
   - 不要为了DDD而DDD
   - 根据业务复杂度选择合适的实现方式
   - 保持架构的简洁性

2. **性能考虑**
   - 合理使用缓存策略
   - 优化数据库查询
   - 避免过度的抽象层次

3. **团队协作**
   - 建立清晰的开发流程
   - 确保代码质量和一致性
   - 及时沟通和解决问题

### 16.4 未来发展

随着业务的发展和技术的演进，可以考虑以下发展方向：

1. **微服务架构**
   - 将单体应用拆分为微服务
   - 基于领域边界进行服务划分
   - 实现更好的可扩展性和独立部署

2. **事件驱动架构**
   - 增强事件处理能力
   - 实现更好的系统解耦
   - 支持复杂的业务流程

3. **云原生架构**
   - 容器化部署
   - 服务网格
   - 无服务器架构

4. **AI和机器学习集成**
   - 智能化的业务决策
   - 自动化的运维管理
   - 个性化的用户体验

通过本指南的学习和实践，相信您能够成功地在项目中应用DDD分层架构，构建出高质量、可维护的软件系统。记住，架构设计是一个持续演进的过程，需要根据实际情况不断调整和优化。

---

**参考资料**

1. Eric Evans - "Domain-Driven Design: Tackling Complexity in the Heart of Software"
2. Vaughn Vernon - "Implementing Domain-Driven Design"
3. Martin Fowler - "Patterns of Enterprise Application Architecture"
4. Robert C. Martin - "Clean Architecture"
5. Spring Framework官方文档
6. Spring Boot官方文档
7. Spring Data JPA官方文档

**联系方式**

如有任何问题或建议，欢迎通过以下方式联系：

- 邮箱：dev@example.com
- GitHub：https://github.com/example/rose-monolithic
- 技术博客：https://blog.example.com

---

*本文档持续更新中，最新版本请访问项目仓库。*

**问题：** 如何在DDD中避免贫血模型？

**解决方案：**
```java
// ❌ 贫血模型
public class User {
    private String id;
    private String username;
    private String email;
    // 只有getter/setter，没有业务逻辑
}

// ✅ 充血模型
public class User {
    private UserId id;
    private Username username;
    private Email email;
    private UserStatus status;
    
    // 包含业务逻辑的方法
    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new UserException("用户已经是激活状态");
        }
        this.status = UserStatus.ACTIVE;
    }
    
    public void changeEmail(Email newEmail) {
        if (this.email.equals(newEmail)) {
            return;
        }
        this.email = newEmail;
        // 发布邮箱变更事件
        DomainEventPublisher.publish(new UserEmailChangedEvent(this.id, newEmail));
    }
}
```

### 10.2 聚合边界设计

**问题：** 如何确定聚合的边界？

**原则：**
1. 业务不变性边界
2. 事务一致性边界
3. 数据修改频率
4. 团队组织结构

### 10.3 跨聚合操作

**问题：** 如何处理跨聚合的业务操作？

**解决方案：**
```java
@Service
@Transactional
public class OrderApplicationService {
    
    /**
     * 使用领域事件处理跨聚合操作
     */
    public void createOrder(CreateOrderCommand command) {
        // 1. 创建订单聚合
        Order order = new Order(command.getUserId(), command.getItems());
        orderRepository.save(order);
        
        // 2. 发布订单创建事件
        eventPublisher.publish(new OrderCreatedEvent(order.getId()));
        
        // 3. 事件处理器会异步处理库存扣减等操作
    }
    
    @EventHandler
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 扣减库存
        inventoryService.reserveItems(event.getOrderId());
        
        // 发送通知
        notificationService.sendOrderConfirmation(event.getOrderId());
    }
}
```

---

## 11. 扩展指南

### 11.1 微服务演进

当单体应用需要拆分为微服务时：

1. **按聚合拆分**
   - 每个聚合成为独立的微服务
   - 保持数据的独立性

2. **事件驱动架构**
   - 使用消息队列进行服务间通信
   - 实现最终一致性

3. **API网关**
   - 统一入口和路由
   - 认证授权
   - 限流熔断

### 11.2 领域建模深化

1. **事件风暴**
   - 识别领域事件
   - 发现聚合边界
   - 明确上下文边界

2. **战略设计**
   - 限界上下文
   - 上下文映射
   - 防腐层设计

### 11.3 架构治理

1. **代码质量**
   - 静态代码分析
   - 架构合规检查
   - 依赖关系验证

2. **性能监控**
   - APM工具集成
   - 业务指标监控
   - 异常告警

---

## 📝 总结

本指南提供了一个完整的DDD分层架构实践方案，涵盖了：

### 🎯 核心特色
- **严格的分层架构**：清晰的职责分离和依赖关系
- **统一的响应格式**：标准化的API响应和错误处理
- **完整的CRUD操作**：包含批量操作和分页查询
- **事件驱动架构**：领域事件和应用事件的完整实现
- **国际化支持**：多语言异常消息和时区处理
- **性能优化策略**：缓存、分页、批量操作优化
- **全面的测试策略**：单元测试、集成测试、性能测试

### 🛠 技术栈集成
- **Spring Boot生态**：完整的企业级开发框架
- **MyBatis Plus增强**：简化数据访问层开发
- **Redis缓存**：多级缓存策略
- **国际化和时区**：完整的多语言和时区支持

### 📋 最佳实践总结
- **依赖管理**：严格的分层依赖和依赖倒置
- **事务控制**：合理的事务边界和传播机制
- **异常处理**：统一的异常体系和国际化消息
- **查询优化**：分页优化和批量操作
- **缓存策略**：多级缓存和更新策略
- **时区处理**：UTC存储和用户时区显示

### 🚀 扩展建议
- **微服务演进**：基于聚合的服务拆分策略
- **领域建模深化**：事件风暴和战略设计
- **架构治理**：代码质量和性能监控
- **性能优化**：进一步的查询和缓存优化

通过遵循本指南的设计原则和实践方法，可以构建出高质量、可维护、可扩展的企业级应用系统。