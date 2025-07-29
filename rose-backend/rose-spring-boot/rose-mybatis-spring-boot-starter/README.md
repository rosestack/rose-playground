# Rose MyBatis Plus Spring Boot Starter

🚀 企业级 MyBatis Plus 增强工具，提供多租户、数据权限、字段加密等开箱即用的功能。

## ✨ 核心特性

### 🏢 多租户支持
- **自动租户隔离**：基于 InheritableThreadLocal，支持父子线程传递
- **灵活配置**：支持自定义忽略表和前缀
- **零侵入**：无需修改现有代码，自动添加租户条件

### 🔐 敏感字段加密
- **自动加密解密**：插入时加密，查询时解密
- **多种算法**：支持 AES、DES、3DES 等
- **查询支持**：可选生成哈希字段支持精准查询
- **配置灵活**：支持动态开启/关闭

### 🛡️ 动态数据权限
- **多维度权限**：支持用户、部门、组织、角色等
- **自动SQL改写**：透明添加权限条件，支持多表 JOIN
- **灵活范围**：支持本人、本部门、全部等多种范围
- **表别名支持**：智能识别多表查询中的表别名

### 🎭 数据脱敏
- **查询自动脱敏**：查询结果返回时自动脱敏敏感字段
- **多种脱敏类型**：手机号、身份证、邮箱、银行卡等
- **自定义规则**：支持自定义脱敏规则
- **环境控制**：可配置在特定环境启用
- **集合支持**：支持单个对象和集合对象的脱敏

### 📋 SQL 审计
- **操作审计**：记录增删改查操作
- **性能监控**：记录执行时间
- **安全追踪**：记录用户和租户信息

### 🔍 字段哈希（独立于加密）
- **单向哈希**：不可逆，只能用于查询匹配
- **多种算法**：MD5、SHA-256、加盐SHA-256、HMAC-SHA256
- **精准查询**：支持加密字段的精准查询

### 📝 数据变更日志
- **字段级追踪**：记录每个字段的变更前后值
- **业务关联**：与业务操作关联，区别于SQL审计
- **历史查询**：支持数据变更历史查询

### ⚡ 其他增强
- **分页优化**：智能分页，支持多数据库
- **乐观锁**：自动版本控制，防止并发冲突
- **字段填充**：自动填充创建时间、更新时间等
- **性能监控**：慢查询监控和SQL格式化

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.rosestack</groupId>
    <artifactId>rose-mybatis-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 基础配置

```yaml
rose:
  mybatis:
    enabled: true
    
    # 多租户配置
    tenant:
      enabled: true
      column: tenant_id
      ignore-tables: []
      ignore-table-prefixes: []
    
    # 字段加密配置
    encryption:
      enabled: true
      secret-key: "MySecretKey12345"
      fail-on-error: true
      default-algorithm: "AES"
    
    # 数据权限配置
    data-permission:
      enabled: true
      use-mybatis-plus-interceptor: true  # 是否使用 MyBatis Plus 拦截器（推荐）
      default-field: "user_id"
      sql-log: false

      # 缓存配置
      cache:
        enabled: true                    # 是否启用缓存
        expire-minutes: 30               # 缓存过期时间（分钟）
        cleanup-interval-minutes: 60     # 缓存清理间隔（分钟）
        management-enabled: false        # 是否启用缓存管理接口（仅开发/测试环境）
        max-annotation-cache-size: 10000 # 最大注解缓存数量
        max-permission-cache-size: 50000 # 最大权限缓存数量

    # 数据脱敏配置
    desensitization:
      enabled: true
      environments: "prod,test"  # 在生产和测试环境启用脱敏

    # SQL 审计配置
    audit:
      enabled: true
      include-sql: true
      include-parameters: false
      log-level: "INFO"


```

## 📖 功能详解

### 🏢 多租户使用

```java
// 设置租户上下文
TenantContextHolder.setCurrentTenantId("tenant-123");

// 查询自动添加 WHERE tenant_id = 'tenant-123'
List<User> users = userMapper.selectList(null);

// 指定租户执行
TenantContextHolder.runWithTenant("tenant-456", () -> {
    userService.createUser(user);
});

// 支持返回值
String result = TenantContextHolder.runWithTenant("tenant-789", () -> {
    return userService.getUserCount();
});

// 多线程自动继承
CompletableFuture.runAsync(() -> {
    // 子线程自动继承父线程的租户上下文
    String tenantId = TenantContextHolder.getCurrentTenantId();
});
```

### 🔐 敏感字段加密

```java
@Data
@TableName("user")
public class User {
    @TableId
    private Long id;
    
    // 基础加密
    @EncryptField(EncryptField.EncryptType.AES)
    private String phone;
    
    // 支持查询的加密字段
    @EncryptField(value = EncryptField.EncryptType.AES, searchable = true)
    private String idCard;
    private String idCardHash; // 自动生成哈希用于查询
    
    private String email; // 普通字段
}

// 使用示例
User user = new User();
user.setPhone("13800138000");     // 存储时自动加密
user.setIdCard("110101199001011234"); // 存储时自动加密并生成哈希

userMapper.insert(user); // 插入时自动加密

User saved = userMapper.selectById(1L); // 查询时自动解密
System.out.println(saved.getPhone()); // "13800138000" (已解密)

// 通过哈希字段精准查询（使用加密字段的 searchable 功能）
// 当 @EncryptField(searchable = true) 时，会自动生成哈希字段用于查询
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getIdCardHash, "生成的哈希值"); // 系统自动生成
List<User> users = userMapper.selectList(wrapper);
```

### 🛡️ 动态数据权限

Rose MyBatis 支持两种数据权限拦截器模式：

#### 1. MyBatis Plus InnerInterceptor 模式（推荐）

默认使用 MyBatis Plus 自带的 `DataPermissionInterceptor`，提供更好的性能和集成性。

```yaml
rose:
  mybatis:
    data-permission:
      enabled: true
      use-mybatis-plus-interceptor: true  # 默认值，使用 MyBatis Plus 拦截器
      default-field: user_id
```

#### 2. 传统 Interceptor 模式

使用 Rose 自定义的传统 MyBatis 拦截器。

```yaml
rose:
  mybatis:
    data-permission:
      enabled: true
      use-mybatis-plus-interceptor: false  # 使用传统拦截器
      default-field: user_id
```

#### 基本使用

```java
// 用户级权限控制 - 根据字段名自动匹配权限提供者
@DataPermission(field = "user_id")
public List<Order> getUserOrders() {
    // 自动匹配 UserDataPermissionProvider
    // 添加 WHERE user_id = '当前用户ID'
    return orderMapper.selectList(null);
}

// 门店级权限控制 - 根据字段名自动匹配权限提供者
@DataPermission(field = "store_id")
public List<Product> getStoreProducts() {
    // 自动匹配 StoreDataPermissionProvider
    // 添加 WHERE store_id IN ('当前用户可访问的门店ID列表')
    return productMapper.selectList(null);
}

// 创建者权限控制
@DataPermission(field = "creator_id")
public List<Article> getMyArticles() {
    // 自动匹配 UserDataPermissionProvider
    // 添加 WHERE creator_id = '当前用户ID'
    return articleMapper.selectList(null);
}

// 类级权限控制
@DataPermission(field = "store_id")
@RestController
public class ProductController {
    // 所有方法都会自动添加门店权限条件
}

// 支持不同数据类型
@DataPermission(field = "user_id", fieldType = DataPermission.FieldType.NUMBER)
public List<Order> getUserOrdersWithNumberId() {
    // 数值类型字段，不使用引号包围
    return orderMapper.selectList(null);
}

// 多种权限类型示例
@DataPermission(field = "owner_id")    // 用户权限
@DataPermission(field = "shop_id")     // 门店权限
@DataPermission(field = "branch_id")   // 分店权限
public List<SalesRecord> getSalesRecords() {
    // 根据字段名自动选择对应的权限提供者
    return salesMapper.selectList(null);
}
```

### ⚡ 权限提供者模式

Rose MyBatis 采用权限提供者模式，根据字段名自动匹配对应的权限提供者，权限范围由提供者内部决定：

#### 字段名自动匹配规则

| 字段名模式 | 权限提供者 | 权限范围 | 说明 |
|-----------|------------|----------|------|
| `user_id`, `creator_id`, `owner_id`, `author_id` | UserDataPermissionProvider | 当前用户ID | 用户相关字段 |
| `store_id`, `shop_id`, `branch_id`, `outlet_id` | StoreDataPermissionProvider | 当前用户可访问的门店ID列表 | 门店相关字段 |
| `dept_id`, `department_id` | DeptDataPermissionProvider | 当前用户部门及下级部门ID | 部门相关字段 |
| `org_id`, `organization_id`, `company_id` | OrgDataPermissionProvider | 当前用户组织及下级组织ID | 组织相关字段 |

#### 权限提供者优先级

当多个提供者支持同一字段时，按优先级选择：

| 提供者 | 优先级 | 说明 |
|--------|--------|------|
| UserDataPermissionProvider | 10 | 用户权限，最高优先级 |
| StoreDataPermissionProvider | 20 | 门店权限，中等优先级 |
| DeptDataPermissionProvider | 30 | 部门权限，中等优先级 |
| OrgDataPermissionProvider | 40 | 组织权限，较低优先级 |
| 自定义提供者 | 100 | 默认优先级 |

#### 字段类型支持

```java
// 字符串类型（默认）- 使用单引号包围
@DataPermission(field = "user_id", fieldType = DataPermission.FieldType.STRING)

// 数值类型 - 不使用引号
@DataPermission(field = "user_id", fieldType = DataPermission.FieldType.NUMBER)
```

#### 自定义权限提供者

```java
@Component
public class CustomDataPermissionProvider implements DataPermissionProvider {

    @Override
    public boolean supports(String field) {
        return "custom_field".equals(field);
    }

    @Override
    public List<String> getPermissionValues(String field) {
        // 根据业务逻辑返回权限值
        return getCurrentUserCustomPermissions();
    }

    @Override
    public int getPriority() {
        return 50; // 自定义优先级
    }
}
```

### ⚡ 实体类配置

```java
@Data
@TableName("user")
public class User {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    private String username;
    
    // 敏感字段加密
    @EncryptField(value = EncryptField.EncryptType.AES, searchable = true)
    private String phone;
    private String phoneHash; // 查询用哈希字段
    
    @EncryptField(EncryptField.EncryptType.AES)
    private String idCard;
    
    // 自动填充字段
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // 乐观锁版本字段
    @Version
    private Integer version;
    
    // 租户字段（自动填充）
    private String tenantId;
    
    // 数据权限字段
    private String userId;
    private String deptId;

    // 数据脱敏字段（查询时自动脱敏）
    @SensitiveField(SensitiveField.SensitiveType.PHONE)
    private String phone; // 查询结果自动脱敏为：138****8000

    @SensitiveField(SensitiveField.SensitiveType.EMAIL)
    private String email; // 查询结果自动脱敏为：abc***@example.com

    // 注意：哈希字段通过 @EncryptField(searchable = true) 自动生成
    // 无需单独的 @HashField 注解
}

// 变更日志示例
@ChangeLog(module = "用户管理", operation = "更新用户信息")
@Data
@TableName("user")
public class User {
    // 实体字段...
}
```

## 📋 完整配置参数

```yaml
rose:
  mybatis:
    enabled: true
    
    # 多租户配置
    tenant:
      enabled: false
      column: tenant_id
      ignore-tables: []
      ignore-table-prefixes: []
    
    # 分页配置
    pagination:
      enabled: true
      max-limit: 1000
      db-type: mysql
    
    # 乐观锁配置
    optimistic-lock:
      enabled: true
      column: version
    
    # 字段自动填充配置
    field-fill:
      enabled: true
      create-time-column: created_time
      update-time-column: updated_time
    
    # 字段加密配置
    encryption:
      enabled: false
      secret-key: ""
      fail-on-error: true
      default-algorithm: "AES"
    
    # 数据权限配置
    data-permission:
      enabled: false
      use-mybatis-plus-interceptor: true
      default-field: "user_id"
      sql-log: false
    
    # 性能监控配置
    performance:
      enabled: false
      slow-sql-threshold: 1000
      format-sql: true
```

## 🔧 高级用法

### 自定义加密器

```java
@Component
public class CustomFieldEncryptor implements FieldEncryptor {
    @Override
    public String encrypt(String plainText, EncryptField.EncryptType encryptType) {
        // 自定义加密逻辑
        return customEncrypt(plainText);
    }
    
    @Override
    public String decrypt(String cipherText, EncryptField.EncryptType encryptType) {
        // 自定义解密逻辑
        return customDecrypt(cipherText);
    }
}
```

### 数据权限缓存管理

Rose MyBatis 提供了强大的数据权限缓存功能，可以显著提升性能：

#### 缓存特性
- **注解缓存**: 缓存 `@DataPermission` 注解信息，避免重复反射
- **权限值缓存**: 缓存用户权限值，减少数据库查询
- **自动过期**: 支持缓存自动过期和清理
- **并发安全**: 使用 `ConcurrentHashMap` 保证线程安全

#### 缓存管理接口

```java
@Autowired
private DataPermissionCacheService cacheService;

// 获取缓存统计信息
Map<String, Object> stats = cacheService.getCacheStatistics();

// 清空所有缓存
cacheService.clearAllCache();

// 清空指定用户缓存
cacheService.clearUserCache("user123");

// 检查缓存健康状态
CacheHealthStatus health = cacheService.checkCacheHealth();
```

#### 缓存管理 REST API

启用缓存管理接口（仅开发/测试环境）：

```yaml
rose:
  mybatis:
    data-permission:
      cache:
        management-enabled: true
```

可用接口：
- `GET /api/internal/data-permission-cache/stats` - 获取缓存统计
- `GET /api/internal/data-permission-cache/health` - 检查缓存健康
- `DELETE /api/internal/data-permission-cache/all` - 清空所有缓存
- `DELETE /api/internal/data-permission-cache/user/{userId}` - 清空用户缓存

### 自定义数据权限处理器

```java
@Component
public class CustomDataPermissionHandler implements DataPermissionHandler {
    @Override
    public List<String> getPermissionValues(DataPermission dataPermission) {
        // 从 Spring Security 或其他权限框架获取当前用户权限
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) auth.getPrincipal();

        switch (dataPermission.type()) {
            case USER:
                return Arrays.asList(user.getUserId());
            case DEPT:
                return getDeptIds(user.getDeptId(), dataPermission.scope());
            // ... 其他权限类型
        }
    }
}
```

## 🛠️ 数据库设计建议

### 加密字段表结构

```sql
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL,
    `phone` VARCHAR(255),          -- 加密字段，长度要足够
    `phone_hash` VARCHAR(64),      -- 查询用哈希字段
    `id_card` VARCHAR(255),        -- 加密字段
    `email` VARCHAR(100),
    `tenant_id` VARCHAR(50),       -- 租户字段
    `user_id` VARCHAR(50),         -- 数据权限字段
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `version` INT DEFAULT 1,       -- 乐观锁版本字段
    
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_user_id (user_id),
    INDEX idx_phone_hash (phone_hash)  -- 哈希字段索引用于查询
);
```

## ⚠️ 重要提醒

### 安全建议
1. **密钥管理**：生产环境从外部配置或密钥管理系统获取
2. **哈希查询**：加密字段无法直接查询，使用哈希字段
3. **索引优化**：对租户字段、权限字段、哈希字段建立索引

### 性能考虑
1. **加密开销**：只对真正敏感的字段加密
2. **权限范围**：避免过于复杂的权限范围查询
3. **批量操作**：大批量操作时注意加密性能影响

### 开发注意
1. **上下文清理**：确保请求结束时清理上下文
2. **测试环境**：可以关闭加密便于调试
3. **日志安全**：避免在日志中输出敏感信息

## 📄 许可证

MIT License - 查看 [LICENSE](LICENSE) 文件了解详情。