# DDD Demo项目开发计划

## 项目概述

**项目名称：** ddd-demo  
**项目目标：** 基于DDD分层架构实现完整的用户管理系统，展示DDD核心概念和最佳实践  
**技术栈：** Java 17+、Spring Boot 3.x、MyBatis Plus、MySQL、Redis、JUnit 5  

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

## 开发阶段规划

### 阶段1：项目基础搭建（1-2天）

**目标：** 建立项目基础架构和开发环境

**任务清单：**
- [ ] 创建Maven项目结构和pom.xml配置
- [ ] 配置Spring Boot 3.x和基础依赖
- [ ] 建立DDD四层架构的包结构
- [ ] 配置MySQL数据库和连接池
- [ ] 配置Redis缓存
- [ ] 配置日志系统（Logback）
- [ ] 配置MyBatis Plus
- [ ] 创建数据库表结构
- [ ] 配置开发、测试、生产环境

**交付成果：**
- 完整的项目基础架构
- 可运行的Spring Boot应用
- 数据库连接和基础配置

### 阶段2：领域建模（2-3天）

**目标：** 设计核心领域模型和业务规则

**任务清单：**
- [ ] 设计User聚合根（包含业务逻辑）
- [ ] 设计值对象（Address、PhoneNumber等）
- [ ] 定义用户状态枚举（ACTIVE、INACTIVE、DELETED）
- [ ] 创建领域事件（UserCreated、UserUpdated等）并放在model包下
- [ ] 设计UserDomainService接口
- [ ] 定义UserRepository接口
- [ ] 实现业务规则验证逻辑
- [ ] 创建领域异常类

**核心类设计：**
```java
// User聚合根
public class User extends AggregateRoot {
    private String username;
    private String email;
    private String password;
    private UserStatus status;
    private Address address;
    // 业务方法：create(), updateProfile(), activate(), deactivate()等
}

// 值对象
public class Address {
    private final String province;
    private final String city;
    private final String detail;
    // 验证逻辑和业务方法
}

// 领域事件（放在model包下）
public class UserCreatedEvent extends AbstractDomainEvent {
    private String username;
    private String email;
    // 事件数据
}
```

**交付成果：**
- 完整的领域模型设计
- 业务规则实现
- 领域事件定义

### 阶段3：基础设施层实现（2-3天）

**目标：** 实现数据持久化和外部服务集成

**任务清单：**
- [ ] 实现UserRepositoryImpl
- [ ] 创建UserMapper（MyBatis Plus）
- [ ] 设计数据库实体类（UserEntity）
- [ ] 实现数据转换器（Domain ↔ Entity）
- [ ] 配置事务管理
- [ ] 实现缓存策略
- [ ] 创建外部服务接口（邮件服务等）
- [ ] 配置数据源和连接池
- [ ] 实现事件发布机制
- [ ] 配置异常处理和国际化

**核心实现：**
```java
// 仓储实现
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final UserMapper userMapper;
    private final UserConverter userConverter;
    
    @Override
    public User save(User user) {
        // 实现保存逻辑
    }
}

// 数据转换器
@Component
public class UserConverter {
    public UserEntity toEntity(User user) {
        // 领域对象转数据库实体
    }
    
    public User toDomain(UserEntity entity) {
        // 数据库实体转领域对象
    }
}
```

**交付成果：**
- 完整的数据访问层
- 数据转换机制
- 缓存和事务配置
- 事件发布机制
- 异常处理和国际化配置

### 阶段4：应用层实现（2-3天）

**目标：** 实现业务流程编排和事务控制

**任务清单：**
- [ ] 实现UserApplicationService
- [ ] 创建命令对象（CreateUserCommand、UpdateUserCommand等）
- [ ] 创建查询对象（UserQuery、UserListQuery等）
- [ ] 实现领域事件处理器
- [ ] 配置应用层事务
- [ ] 实现缓存服务
- [ ] 创建应用异常处理
- [ ] 实现事务边界控制
- [ ] 配置事件发布和订阅
- [ ] 实现业务异常处理

**核心实现：**
```java
// 应用服务
@Service
@Transactional(readOnly = true)
public class UserApplicationService {
    
    @Transactional(rollbackFor = Exception.class)
    public UserDTO createUser(CreateUserCommand command) {
        // 业务流程编排
        User user = User.create(command.toUserCreateInfo());
        User savedUser = userRepository.save(user);
        // 发布领域模型中的事件
        eventPublisher.publishEvents(savedUser);
        return UserConverter.toDTO(savedUser);
    }
}

// 事件处理器
@Component
public class UserEventHandler {
    @EventListener
    @Async
    public void handleUserCreated(UserCreatedEvent event) {
        // 处理用户创建事件
    }
}
```

**交付成果：**
- 完整的应用服务层
- 事件驱动机制
- 业务流程编排
- 事务边界控制
- 异常处理机制

### 阶段5：接口层实现（2-3天）

**目标：** 实现REST API和用户交互

**任务清单：**
- [ ] 创建UserController
- [ ] 设计请求/响应DTO
- [ ] 实现数据转换器（DTO ↔ Domain）
- [ ] 配置参数验证
- [ ] 实现统一异常处理
- [ ] 配置API文档（Swagger）
- [ ] 实现国际化支持
- [ ] 配置安全控制
- [ ] 配置国际化消息资源
- [ ] 实现异常国际化处理

**核心实现：**
```java
// REST控制器
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    
    @PostMapping
    public ApiResponse<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        CreateUserCommand command = CreateUserCommand.from(request);
        UserDTO userDTO = userApplicationService.createUser(command);
        return ApiResponse.success(userDTO);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getUser(@PathVariable String id) {
        UserDTO userDTO = userApplicationService.getUser(id);
        return ApiResponse.success(userDTO);
    }
}

// 统一响应格式
@Data
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
```

**交付成果：**
- 完整的REST API
- 统一响应格式
- API文档和测试
- 国际化支持
- 统一异常处理

### 阶段6：测试和优化（2-3天）

**目标：** 完善测试覆盖和性能优化

**任务清单：**
- [ ] 编写领域层单元测试
- [ ] 编写应用层单元测试
- [ ] 编写接口层集成测试
- [ ] 编写端到端测试
- [ ] 性能测试和优化
- [ ] 代码质量检查
- [ ] 完善项目文档
- [ ] 创建部署脚本

**测试策略：**
```java
// 领域层测试
@ExtendWith(MockitoExtension.class)
class UserTest {
    @Test
    void testUserCreation() {
        UserCreateInfo info = UserCreateInfo.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password123")
            .build();
        
        User user = User.create(info);
        
        assertNotNull(user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }
}

// 应用层测试
@SpringBootTest
@Transactional
class UserApplicationServiceTest {
    @Test
    void testCreateUser() {
        CreateUserCommand command = new CreateUserCommand("testuser", "test@example.com", "password123");
        UserDTO result = userApplicationService.createUser(command);
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }
}
```

**交付成果：**
- 完整的测试覆盖
- 性能优化结果
- 项目文档和部署方案

## 项目结构设计

```
ddd-user-demo/
├── src/main/java/com/example/ddddemo/
│   ├── UserApplication.java
│   ├── user/                           # 用户领域
│   │   ├── interfaces/                 # 接口层
│   │   │   ├── web/
│   │   │   │   └── UserController.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateUserRequest.java
│   │   │   │   │   ├── UpdateUserRequest.java
│   │   │   │   │   └── UserListRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── UserDTO.java
│   │   │   │       └── UserListDTO.java
│   │   │   └── assembler/
│   │   │       └── UserAssembler.java
│   │   ├── application/                # 应用层
│   │   │   ├── service/
│   │   │   │   └── UserApplicationService.java
│   │   │   ├── command/
│   │   │   │   ├── CreateUserCommand.java
│   │   │   │   └── UpdateUserCommand.java
│   │   │   ├── query/
│   │   │   │   └── UserQuery.java
│   │   │   └── event/
│   │   │       └── UserCreatedEventHandler.java
│   │   ├── domain/                     # 领域层
│   │   │   ├── model/                  # 领域模型（聚合根和事件）
│   │   │   │   ├── User.java
│   │   │   │   ├── AggregateRoot.java
│   │   │   │   └── event/
│   │   │   │       ├── UserCreatedEvent.java
│   │   │   │       ├── UserUpdatedEvent.java
│   │   │   │       └── UserDeletedEvent.java
│   │   │   ├── valueobject/            # 值对象
│   │   │   │   ├── Address.java
│   │   │   │   ├── PhoneNumber.java
│   │   │   │   └── Email.java
│   │   │   ├── service/                # 领域服务
│   │   │   │   └── UserDomainService.java
│   │   │   ├── repository/             # 仓储接口
│   │   │   │   └── UserRepository.java
│   │   │   └── factory/                # 工厂
│   │   │       └── UserFactory.java
│   │   └── infrastructure/             # 基础设施层
│   │       ├── persistence/
│   │       │   ├── UserRepositoryImpl.java
│   │       │   ├── UserMapper.java
│   │       │   ├── UserEntity.java
│   │       │   └── UserConverter.java
│   │       └── external/
│   │           └── EmailService.java
│   ├── infrastructure/                 # 全局基础设施
│   │   ├── config/
│   │   ├── exception/
│   │   └── util/
│   └── shared/                         # 共享组件
│       ├── domain/
│       ├── application/
│       └── infrastructure/
```

## 核心功能特性

### 1. 用户管理功能
- **用户注册**：用户名、邮箱唯一性验证，密码强度检查
- **用户信息更新**：昵称、头像、地址等信息的修改
- **用户状态管理**：激活、禁用、删除用户
- **用户查询**：分页查询、条件筛选、详情查询
- **密码管理**：修改密码、密码强度验证

### 2. 业务规则验证
- 用户名格式和唯一性验证
- 邮箱格式和唯一性验证
- 密码强度要求（长度、复杂度）
- 用户状态转换规则
- 敏感操作审计日志

### 3. 技术特性
- **领域事件驱动**：用户创建、更新、状态变更事件
- **缓存策略**：用户信息缓存、查询结果缓存
- **异常处理**：统一异常处理和错误响应
- **数据验证**：参数校验和业务规则验证
- **安全控制**：输入过滤、权限控制

## 开发时间估算

**总开发时间：11-17天**

- 阶段1（基础搭建）：1-2天
- 阶段2（领域建模）：2-3天
- 阶段3（基础设施层）：2-3天
- 阶段4（应用层）：2-3天
- 阶段5（接口层）：2-3天
- 阶段6（测试优化）：2-3天

## 成功标准

1. **架构完整性**：代码结构清晰，严格遵循DDD分层架构
2. **功能完整性**：实现完整的User管理CRUD功能
3. **业务逻辑**：包含完整的业务规则验证和状态管理
4. **测试覆盖**：单元测试和集成测试覆盖率达到80%以上
5. **文档完善**：包含API文档、架构说明和部署指南
6. **可运行性**：项目可以正常启动、运行和演示

## 学习价值

通过这个demo项目，您将能够：

1. **深入理解DDD核心概念**：实体、值对象、聚合、领域服务等
2. **掌握分层架构设计**：清晰的职责分离和依赖关系
3. **实践领域建模**：从业务需求到代码实现的完整过程
4. **学习最佳实践**：异常处理、缓存策略、事件驱动等
5. **提升代码质量**：测试驱动开发、代码规范等

## 关键里程碑

1. **项目基础架构完成** - 阶段1结束
2. **核心领域模型实现** - 阶段2结束
3. **完整的CRUD功能** - 阶段4结束
4. **业务规则验证** - 阶段2-4完成
5. **事件驱动机制** - 阶段4完成
6. **测试覆盖完成** - 阶段6结束

## 技术栈详细说明

### 核心框架
- **Java 17+**：使用最新的Java特性，如记录类、模式匹配等
- **Spring Boot 3.5+**：主框架，提供依赖注入和自动配置
- **Spring Security 6.x**：安全框架，处理认证和授权
- **MyBatis Plus 3.x**：数据访问层ORM框架，提供强大的查询能力

### 数据存储
- **MySQL 8.0**：主数据库，支持JSON字段和窗口函数
- **HikariCP**：高性能数据库连接池
- **Redis 7.x**：缓存和会话存储，支持多种数据结构

### 构建和部署
- **Maven 3.9+**：构建工具和依赖管理
- **Docker**：容器化部署
- **Docker Compose**：本地开发环境编排

### 测试框架
- **JUnit 5**：单元测试框架，支持参数化测试和动态测试
- **Mockito 5.x**：模拟框架，用于创建测试替身
- **AssertJ**：流式断言库，提供更好的测试可读性
- **TestContainers**：集成测试容器，支持真实数据库和中间件测试

### 开发工具
- **Lombok**：减少样板代码，自动生成getter/setter等
- **Spring Boot DevTools**：开发时热重载和自动重启
- **Swagger/OpenAPI 3**：API文档生成和在线测试
- **MapStruct**：对象映射工具，编译时生成映射代码

## 详细实现说明

### 1. 数据库实体命名规范

**命名规则：** 使用 `XXXEntity` 格式命名数据库实体类

```java
// 用户数据库实体
@Data
@TableName("t_user")
public class UserEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    @TableField("username")
    private String username;
    
    @TableField("email")
    private String email;
    
    @TableField("password")
    private String password;
    
    @TableField("status")
    private String status;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
```

### 2. 异常处理和国际化

#### 2.1 异常体系设计

```java
/**
 * 基础异常类
 */
public abstract class BaseException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;
    private final Object[] args;
    private final Throwable cause;
    
    protected BaseException(String errorCode, String errorMessage, Object... args) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.args = args;
        this.cause = null;
    }
    
    protected BaseException(String errorCode, String errorMessage, Throwable cause, Object... args) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.args = args;
        this.cause = cause;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Object[] getArgs() {
        return args;
    }
    
    public abstract ExceptionLevel getLevel();
    public abstract boolean isRetryable();
}

/**
 * 异常级别枚举
 */
public enum ExceptionLevel {
    FATAL,      // 致命错误，需要立即处理
    ERROR,      // 错误，影响业务流程
    WARN,       // 警告，不影响主流程
    INFO        // 信息，仅记录
}

/**
 * 领域异常 - 业务规则违反
 */
public class DomainException extends BaseException {
    
    public DomainException(String errorCode, String errorMessage, Object... args) {
        super(errorCode, errorMessage, args);
    }
    
    public DomainException(String errorCode, String errorMessage, Throwable cause, Object... args) {
        super(errorCode, errorMessage, cause, args);
    }
    
    @Override
    public ExceptionLevel getLevel() {
        return ExceptionLevel.ERROR;
    }
    
    @Override
    public boolean isRetryable() {
        return false; // 业务规则违反通常不可重试
    }
}

/**
 * 应用异常 - 应用层处理异常
 */
public class ApplicationException extends BaseException {
    
    private final boolean retryable;
    
    public ApplicationException(String errorCode, String errorMessage, Object... args) {
        this(errorCode, errorMessage, false, args);
    }
    
    public ApplicationException(String errorCode, String errorMessage, boolean retryable, Object... args) {
        super(errorCode, errorMessage, args);
        this.retryable = retryable;
    }
    
    public ApplicationException(String errorCode, String errorMessage, Throwable cause, Object... args) {
        super(errorCode, errorMessage, cause, args);
        this.retryable = false;
    }
    
    @Override
    public ExceptionLevel getLevel() {
        return ExceptionLevel.ERROR;
    }
    
    @Override
    public boolean isRetryable() {
        return retryable;
    }
}

/**
 * 基础设施异常 - 外部依赖异常
 */
public class InfrastructureException extends BaseException {
    
    public InfrastructureException(String errorCode, String errorMessage, Object... args) {
        super(errorCode, errorMessage, args);
    }
    
    public InfrastructureException(String errorCode, String errorMessage, Throwable cause, Object... args) {
        super(errorCode, errorMessage, cause, args);
    }
    
    @Override
    public ExceptionLevel getLevel() {
        return ExceptionLevel.ERROR;
    }
    
    @Override
    public boolean isRetryable() {
        return true; // 基础设施异常通常可重试
    }
}

/**
 * 验证异常 - 参数验证失败
 */
public class ValidationException extends BaseException {
    
    private final List<FieldError> fieldErrors;
    
    public ValidationException(String errorCode, String errorMessage, List<FieldError> fieldErrors) {
        super(errorCode, errorMessage);
        this.fieldErrors = fieldErrors != null ? fieldErrors : Collections.emptyList();
    }
    
    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
    
    @Override
    public ExceptionLevel getLevel() {
        return ExceptionLevel.WARN;
    }
    
    @Override
    public boolean isRetryable() {
        return false;
    }
    
    public static class FieldError {
        private final String field;
        private final String message;
        private final Object rejectedValue;
        
        public FieldError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }
        
        public String getField() { return field; }
        public String getMessage() { return message; }
        public Object getRejectedValue() { return rejectedValue; }
    }
}

/**
 * 业务异常 - 具体业务场景异常
 */
public class BusinessException extends DomainException {
    
    public BusinessException(String errorCode, String errorMessage, Object... args) {
        super(errorCode, errorMessage, args);
    }
    
    // 用户相关异常
    public static class UserNotFoundException extends BusinessException {
        public UserNotFoundException(Long userId) {
            super("USER_NOT_FOUND", "用户不存在", userId);
        }
    }
    
    public static class UserAlreadyExistsException extends BusinessException {
        public UserAlreadyExistsException(String email) {
            super("USER_ALREADY_EXISTS", "用户已存在", email);
        }
    }
    
    public static class InvalidPasswordException extends BusinessException {
        public InvalidPasswordException() {
            super("INVALID_PASSWORD", "密码格式不正确");
        }
    }
    
    public static class UserStatusInvalidException extends BusinessException {
        public UserStatusInvalidException(String status) {
            super("USER_STATUS_INVALID", "用户状态无效", status);
        }
    }
}
```

#### 2.2 国际化消息资源

**messages/errors.properties (默认中文)**
```properties
# 用户相关错误消息
USER_NOT_FOUND=用户不存在
USER_ALREADY_EXISTS=用户已存在
USERNAME_REQUIRED=用户名不能为空
USERNAME_INVALID=用户名格式不正确
USERNAME_EXISTS=用户名已存在
EMAIL_REQUIRED=邮箱不能为空
EMAIL_INVALID=邮箱格式不正确
EMAIL_EXISTS=邮箱已存在
PASSWORD_WEAK=密码强度不够
PASSWORD_INVALID=密码格式不正确
USER_STATUS_INVALID=用户状态无效
USER_OPERATION_FORBIDDEN=操作被禁止

# 系统错误消息
SYSTEM_ERROR=系统错误
SYSTEM_UNAUTHORIZED=未授权访问
SYSTEM_FORBIDDEN=访问被拒绝
SYSTEM_NOT_FOUND=资源不存在
VALIDATION_ERROR=参数验证失败
DATA_ACCESS_ERROR=数据访问异常
CACHE_ERROR=缓存服务异常
EXTERNAL_SERVICE_ERROR=外部服务调用失败
UNKNOWN_ERROR=未知异常
DATA_INTEGRITY_VIOLATION=数据完整性约束违反

# 领域事件相关错误消息
DOMAIN_EVENT_PUBLISH_ERROR=领域事件发布失败
DOMAIN_EVENT_HANDLE_ERROR=领域事件处理失败
```

**messages/errors_en.properties (英文)**
```properties
# User related error messages
USER_NOT_FOUND=User not found
USER_ALREADY_EXISTS=User already exists
USERNAME_REQUIRED=Username is required
USERNAME_INVALID=Invalid username format
USERNAME_EXISTS=Username already exists
EMAIL_REQUIRED=Email is required
EMAIL_INVALID=Invalid email format
EMAIL_EXISTS=Email already exists
PASSWORD_WEAK=Password is too weak
PASSWORD_INVALID=Invalid password format
USER_STATUS_INVALID=Invalid user status
USER_OPERATION_FORBIDDEN=Operation forbidden

# System error messages
SYSTEM_ERROR=System error
SYSTEM_UNAUTHORIZED=Unauthorized access
SYSTEM_FORBIDDEN=Access denied
SYSTEM_NOT_FOUND=Resource not found
VALIDATION_ERROR=Parameter validation failed
DATA_ACCESS_ERROR=Data access error
CACHE_ERROR=Cache service error
EXTERNAL_SERVICE_ERROR=External service call failed
UNKNOWN_ERROR=Unknown error
DATA_INTEGRITY_VIOLATION=Data integrity constraint violation

# Domain event related error messages
DOMAIN_EVENT_PUBLISH_ERROR=Domain event publish failed
DOMAIN_EVENT_HANDLE_ERROR=Domain event handle failed
```

#### 2.3 统一异常处理器

```java
/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @Autowired
    private MessageSource messageSource;
    
    @Autowired
    private ExceptionMetricsCollector metricsCollector;
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(getLocalizedMessage(ex))
            .traceId(traceId)
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .build();
            
        metricsCollector.recordException(ex);
        
        log.warn("Business exception occurred: code={}, message={}, traceId={}", 
            ex.getErrorCode(), ex.getErrorMessage(), traceId);
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * 处理验证异常
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getFieldErrors().stream()
            .map(fieldError -> ErrorResponse.FieldError.builder()
                .field(fieldError.getField())
                .message(fieldError.getMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build())
            .collect(Collectors.toList());
            
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(getLocalizedMessage(ex))
            .fieldErrors(fieldErrors)
            .traceId(traceId)
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .build();
            
        metricsCollector.recordException(ex);
        
        log.warn("Validation exception occurred: code={}, fieldErrors={}, traceId={}", 
            ex.getErrorCode(), fieldErrors.size(), traceId);
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * 处理应用异常
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(getLocalizedMessage(ex))
            .traceId(traceId)
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .retryable(ex.isRetryable())
            .build();
            
        metricsCollector.recordException(ex);
        
        log.error("Application exception occurred: code={}, message={}, retryable={}, traceId={}", 
            ex.getErrorCode(), ex.getErrorMessage(), ex.isRetryable(), traceId, ex);
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * 处理基础设施异常
     */
    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ErrorResponse> handleInfrastructureException(InfrastructureException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message("系统暂时不可用，请稍后重试")
            .traceId(traceId)
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .retryable(ex.isRetryable())
            .build();
            
        metricsCollector.recordException(ex);
        
        log.error("Infrastructure exception occurred: code={}, message={}, traceId={}", 
            ex.getErrorCode(), ex.getErrorMessage(), traceId, ex);
            
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
    
    /**
     * 处理Spring Validation异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> ErrorResponse.FieldError.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build())
            .collect(Collectors.toList());
            
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message("参数验证失败")
            .fieldErrors(fieldErrors)
            .traceId(traceId)
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .build();
            
        log.warn("Method argument validation failed: fieldErrors={}, traceId={}", fieldErrors.size(), traceId);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code("SYSTEM_ERROR")
            .message("系统内部错误")
            .traceId(traceId)
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .build();
            
        log.error("Unexpected exception occurred: traceId={}", traceId, ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * 获取本地化消息
     */
    private String getLocalizedMessage(BaseException ex) {
        try {
            return messageSource.getMessage(ex.getErrorCode(), ex.getArgs(), LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return ex.getErrorMessage();
        }
    }
    
    /**
     * 获取追踪ID
     */
    private String getTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }
}

/**
 * 错误响应对象
 */
@Data
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private String traceId;
    private Instant timestamp;
    private String path;
    private Boolean retryable;
    private List<FieldError> fieldErrors;
    
    @Data
    @Builder
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
```

### 3. 事件发布机制

#### 3.1 事件发布器

```java
/**
 * 领域事件发布器
 */
@Component
@Slf4j
public class DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public DomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 发布单个事件
     */
    public void publish(DomainEvent event) {
        log.debug("发布领域事件: {}", event.getEventType());
        
        try {
            DomainEventWrapper wrapper = new DomainEventWrapper(event);
            applicationEventPublisher.publishEvent(wrapper);
            
            log.info("领域事件发布成功: eventId={}, eventType={}, aggregateId={}",
                    event.getEventId(), event.getEventType(), event.getAggregateId());
        } catch (Exception e) {
            log.error("领域事件发布失败: eventId={}, eventType={}",
                     event.getEventId(), event.getEventType(), e);
            throw new DomainEventPublishException("事件发布失败", e);
        }
    }

    /**
     * 发布聚合根的所有事件
     */
    public void publishEvents(AggregateRoot aggregateRoot) {
        if (aggregateRoot == null || !aggregateRoot.hasDomainEvents()) {
            return;
        }

        List<DomainEvent> events = aggregateRoot.getDomainEvents();
        events.forEach(this::publish);
        
        // 清除已发布的事件
        aggregateRoot.clearDomainEvents();
    }
}
```

#### 3.2 事件处理器

```java
/**
 * 用户事件处理器
 */
@Component
@Slf4j
public class UserEventHandler {

    private final EmailService emailService;
    private final AuditLogService auditLogService;

    public UserEventHandler(EmailService emailService, AuditLogService auditLogService) {
        this.emailService = emailService;
        this.auditLogService = auditLogService;
    }

    /**
     * 处理用户创建事件
     */
    @EventListener
    @Async
    public void handleUserCreated(DomainEventWrapper wrapper) {
        if (!(wrapper.getDomainEvent() instanceof UserCreatedEvent)) {
            return;
        }

        UserCreatedEvent event = (UserCreatedEvent) wrapper.getDomainEvent();
        log.info("处理用户创建事件: userId={}, username={}",
                event.getAggregateId(), event.getUsername());

        try {
            // 发送欢迎邮件
            sendWelcomeEmail(event);
            
            // 记录审计日志
            recordAuditLog(event);
            
            log.info("用户创建事件处理完成: userId={}", event.getAggregateId());
        } catch (Exception e) {
            log.error("处理用户创建事件失败: userId={}", event.getAggregateId(), e);
        }
    }

    /**
     * 处理用户状态变更事件
     */
    @EventListener
    @Async
    public void handleUserStatusChanged(DomainEventWrapper wrapper) {
        if (!(wrapper.getDomainEvent() instanceof UserStatusChangedEvent)) {
            return;
        }

        UserStatusChangedEvent event = (UserStatusChangedEvent) wrapper.getDomainEvent();
        log.info("处理用户状态变更事件: userId={}, oldStatus={}, newStatus={}",
                event.getAggregateId(), event.getOldStatus(), event.getNewStatus());

        try {
            // 发送状态变更通知
            sendStatusChangeNotification(event);
            
            // 记录操作日志
            recordOperationLog(event);
            
            log.info("用户状态变更事件处理完成: userId={}", event.getAggregateId());
        } catch (Exception e) {
            log.error("处理用户状态变更事件失败: userId={}", event.getAggregateId(), e);
        }
    }

    private void sendWelcomeEmail(UserCreatedEvent event) {
        // 发送欢迎邮件逻辑
    }

    private void recordAuditLog(UserCreatedEvent event) {
        // 记录审计日志逻辑
    }

    private void sendStatusChangeNotification(UserStatusChangedEvent event) {
        // 发送状态变更通知逻辑
    }

    private void recordOperationLog(UserStatusChangedEvent event) {
        // 记录操作日志逻辑
    }
}
```

### 4. 事务处理

#### 4.1 应用层事务配置

```java
/**
 * 用户应用服务
 */
@Service
@Transactional(readOnly = true) // 默认只读事务
@Slf4j
public class UserApplicationService {

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final DomainEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    public UserApplicationService(UserRepository userRepository,
                                UserDomainService userDomainService,
                                DomainEventPublisher eventPublisher,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 创建用户 - 写操作事务
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO createUser(CreateUserCommand command) {
        log.info("开始创建用户: username={}", command.getUsername());

        try {
            // 1. 业务规则验证
            validateUserCreation(command);

            // 2. 创建用户聚合
            User user = User.create(command.toUserCreateInfo());

            // 3. 保存用户
            User savedUser = userRepository.save(user);

            // 4. 发布领域事件
            eventPublisher.publishEvents(savedUser);

            // 5. 返回DTO
            UserDTO userDTO = UserConverter.toDTO(savedUser);

            log.info("用户创建成功: userId={}, username={}", userDTO.getId(), userDTO.getUsername());
            return userDTO;

        } catch (Exception e) {
            log.error("创建用户失败: username={}", command.getUsername(), e);
            throw e;
        }
    }

    /**
     * 更新用户信息 - 写操作事务
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO updateUser(String userId, UpdateUserCommand command) {
        log.info("开始更新用户: userId={}", userId);

        try {
            // 1. 获取用户聚合
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("user.not.found", "用户不存在"));

            // 2. 更新用户信息
            user.updateProfile(command.toUserUpdateInfo());

            // 3. 保存用户
            User savedUser = userRepository.save(user);

            // 4. 发布领域事件
            eventPublisher.publishEvents(savedUser);

            // 5. 返回DTO
            UserDTO userDTO = UserConverter.toDTO(savedUser);

            log.info("用户更新成功: userId={}", userId);
            return userDTO;

        } catch (Exception e) {
            log.error("更新用户失败: userId={}", userId, e);
            throw e;
        }
    }

    /**
     * 查询用户 - 只读事务
     */
    public UserDTO getUser(String userId) {
        log.debug("查询用户: userId={}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("user.not.found", "用户不存在"));

        return UserConverter.toDTO(user);
    }

    /**
     * 分页查询用户 - 只读事务
     */
    public PageResult<UserDTO> getUsers(UserQuery query, Pageable pageable) {
        log.debug("分页查询用户: query={}, pageable={}", query, pageable);

        Page<User> userPage = userRepository.findByPage(query, pageable);
        
        List<UserDTO> userDTOs = userPage.getContent().stream()
            .map(UserConverter::toDTO)
            .collect(Collectors.toList());

        return PageResult.of(userDTOs, userPage.getTotalElements(), pageable);
    }

    /**
     * 验证用户创建
     */
    private void validateUserCreation(CreateUserCommand command) {
        // 检查用户名是否可用
        if (!userDomainService.isUsernameAvailable(command.getUsername())) {
            throw new BusinessException("user.username.exists", "用户名已存在");
        }

        // 检查邮箱是否可用
        if (!userDomainService.isEmailAvailable(command.getEmail())) {
            throw new BusinessException("user.email.exists", "邮箱已存在");
        }

        // 检查密码强度
        PasswordStrength strength = userDomainService.evaluatePasswordStrength(command.getPassword());
        if (!strength.isSecure()) {
            throw new BusinessException("user.password.weak", "密码强度不够");
        }
    }
}
```

#### 4.2 事务配置

```java
/**
 * 事务配置
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    /**
     * 事务管理器
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 事务模板
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
```

### 5. 国际化配置

#### 5.1 国际化配置类

```java
/**
 * 国际化配置
 */
@Configuration
public class InternationalizationConfig {
    
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/errors");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600);
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return messageSource;
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(Arrays.asList(
            Locale.SIMPLIFIED_CHINESE,
            Locale.ENGLISH,
            Locale.JAPANESE
        ));
        resolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return resolver;
    }
    
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

/**
 * 国际化消息服务
 */
@Service
public class I18nMessageService {
    
    @Autowired
    private MessageSource messageSource;
    
    /**
     * 获取国际化消息
     */
    public String getMessage(String code, Object... args) {
        return getMessage(code, args, LocaleContextHolder.getLocale());
    }
    
    /**
     * 获取指定语言的消息
     */
    public String getMessage(String code, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            return code; // 返回错误码作为默认值
        }
    }
    
    /**
     * 获取多语言消息映射
     */
    public Map<String, String> getMultiLanguageMessages(String code, Object... args) {
        Map<String, String> messages = new HashMap<>();
        
        // 支持的语言列表
        List<Locale> supportedLocales = Arrays.asList(
            Locale.SIMPLIFIED_CHINESE,
            Locale.ENGLISH,
            Locale.JAPANESE
        );
        
        for (Locale locale : supportedLocales) {
            try {
                String message = messageSource.getMessage(code, args, locale);
                messages.put(locale.getLanguage(), message);
            } catch (NoSuchMessageException e) {
                messages.put(locale.getLanguage(), code);
            }
        }
        
        return messages;
    }
}
```

#### 5.2 国际化工具类

```java
/**
 * 国际化工具类
 */
@Component
public class I18nUtils {
    
    @Autowired
    private MessageSource messageSource;
    
    /**
     * 获取当前语言环境的国际化消息
     */
    public String getMessage(String code, Object... args) {
        return getMessage(code, args, LocaleContextHolder.getLocale());
    }
    
    /**
     * 获取指定语言环境的国际化消息
     */
    public String getMessage(String code, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            return code;
        }
    }
    
    /**
     * 获取当前语言环境
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }
    
    /**
     * 设置当前语言环境
     */
    public void setCurrentLocale(Locale locale) {
        LocaleContextHolder.setLocale(locale);
    }
    
    /**
     * 获取支持的语言列表
     */
    public List<Locale> getSupportedLocales() {
        return Arrays.asList(
            Locale.SIMPLIFIED_CHINESE,
            Locale.ENGLISH,
            Locale.JAPANESE
        );
    }
}
```

## 架构设计说明

### 领域事件放在model包下的原因

1. **领域模型完整性**：领域事件是领域模型的重要组成部分，与聚合根紧密相关
2. **封装性**：事件应该与产生它的聚合根在同一个包下，保持内聚性
3. **DDD原则**：领域事件是领域层的一部分，不应该单独放在event包下
4. **维护性**：当聚合根发生变化时，相关的事件也在同一个包下，便于维护

### 项目结构优势

- **清晰的层次结构**：严格按照DDD四层架构组织代码
- **高内聚低耦合**：每个包都有明确的职责边界
- **易于扩展**：支持多领域扩展，每个领域独立管理
- **便于测试**：分层架构便于单元测试和集成测试

这个开发计划将帮助您系统性地学习和实践DDD分层架构，为后续的复杂项目开发奠定坚实基础。 