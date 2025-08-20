# Rose MyBatis Plus Spring Boot Starter

🚀 企业级 MyBatis Plus 增强工具，提供多租户、数据权限、字段加密等开箱即用的功能。

## ✨ 核心特性

### 🏢 多租户支持

- **自动租户隔离**：基于 InheritableThreadLocal，支持父子线程传递
- **灵活配置**：支持自定义忽略表和前缀
- **零侵入**：无需修改现有代码，自动添加租户条件
- **上下文管理**：提供 TenantContextHolder 管理租户上下文

### 🔐 敏感字段加密

- **自动加密解密**：插入时加密，查询时解密
- **多种算法**：支持 AES、DES、3DES、SM2、SM4 等
- **查询支持**：可选生成哈希字段支持精准查询
- **配置灵活**：支持动态开启/关闭
- **性能优化**：内置缓存机制，减少加密开销

### 🛡️ 动态数据权限

- **多维度权限**：支持用户、部门、组织、门店等
- **自动SQL改写**：透明添加权限条件，支持多表 JOIN
- **权限提供者模式**：根据字段名自动匹配权限提供者
- **缓存优化**：内置权限缓存，提升查询性能
- **双模式支持**：支持 MyBatis Plus 拦截器和传统拦截器

### 📋 SQL 审计

- **操作审计**：记录增删改查操作
- **性能监控**：记录执行时间和参数
- **安全追踪**：记录用户和租户信息
- **事件发布**：支持 Spring 事件机制

### 📝 数据变更日志

- **字段级追踪**：基于 @AuditLog 注解，记录每个字段的变更前后值
- **业务关联**：与业务操作关联，区别于SQL审计
- **敏感字段保护**：自动识别敏感字段并脱敏记录
- **可扩展存储**：支持自定义存储实现

### 🔍 字段哈希（独立于加密）

- **单向哈希**：不可逆，只能用于查询匹配
- **多种算法**：MD5、SHA-256、加盐SHA-256、HMAC-SHA256
- **精准查询**：支持加密字段的精准查询

### ⚡ 其他增强

- **分页优化**：智能分页，支持多数据库
- **乐观锁**：自动版本控制，防止并发冲突
- **字段填充**：自动填充创建时间、更新时间、创建人、更新人等
- **逻辑删除**：支持逻辑删除，保护数据安全

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

      # 哈希配置
      hash:
        # 是否启用哈希功能
        enabled: true
        # 全局盐值（生产环境应该从外部配置获取）
        global-salt: "rose-mybatis-global-salt-2024"
        # 默认哈希算法（当注解使用 AUTO 时的算法选择）
        algorithm: "SHA256"  # 可选：SHA256, SHA512, HMAC_SHA256, HMAC_SHA512
        # HMAC 密钥（生产环境应该从外部配置获取）
        hmac-key: "rose-mybatis-hmac-key-2024"
        # 是否优先使用 HMAC 算法（影响 AUTO 选择）
        use-hmac: true

    # 数据权限配置
    permission:
      enabled: true
      use-mybatis-plus-interceptor: true  # 是否使用 MyBatis Plus 拦截器（推荐）
      default-field: "user_id"
      sql-log: false

      # 缓存配置
      cache:
        enabled: true                    # 是否启用缓存
        expire-minutes: 30               # 缓存过期时间（分钟）
        cleanup-interval-minutes: 60     # 缓存清理间隔（分钟）
        max-annotation-cache-size: 10000 # 最大注解缓存数量
        max-permission-cache-size: 50000 # 最大权限缓存数量

    # 审计日志配置
    audit:
      enabled: true

    # 字段自动填充配置
    field-fill:
      enabled: true
      create-time-column: created_time
      update-time-column: updated_time
      created-by-column: created_by
      updated-by-column: updated_by
```

## 📖 功能详解

### 🔐 敏感字段加密

```java

@Data
@TableName("user")
public class User {
    @TableId
    private Long id;

    // 手机号：安全加密存储 + 哈希查询（使用配置中的默认算法）
    @EncryptField(value = EncryptType.AES, searchable = true)
    private String phone;

    @TableField("phone_hash")
    private String phoneHash; // 自动维护，无需手动设置

    // 邮箱：安全加密存储 + 哈希查询（明确指定算法）
    @EncryptField(value = EncryptType.AES, searchable = true, hashType = HashType.HMAC_SHA256)
    private String email;

    @TableField("email_hash")
    private String emailHash; // 自动维护

    // 身份证：仅加密存储（不需要查询）
    @EncryptField(EncryptType.AES)
    private String idCard;
}

// 使用示例
User user = new User();
user.

setPhone("13800138000");     // 存储时自动加密并生成哈希
user.

setEmail("admin@example.com"); // 存储时自动加密并生成哈希
user.

setIdCard("110101199001011234"); // 仅加密存储

userMapper.

insert(user); // 插入时自动加密敏感字段并生成哈希字段

User saved = userMapper.selectById(1L); // 查询时自动解密
System.out.

println(saved.getPhone()); // "13800138000" (已解密)
        System.out.

println(saved.getPhoneHash()); // "a1b2c3..." (哈希值，用于查询)

// 通过哈希字段精确查询
@Service
public class UserQueryService {
    @Autowired
    private HashService hashService;

    // 使用默认算法（根据配置自动选择）
    public User findByPhone(String phone) {
        String phoneHash = hashService.generateHashWithDefault(phone);
        return userMapper.findByPhoneHash(phoneHash);
    }

    // 或者明确指定算法
    public User findByEmail(String email) {
        String emailHash = hashService.generateHash(email, HashType.HMAC_SHA256);
        return userMapper.findByEmailHash(emailHash);
    }
}
```

### 🛡️ 动态数据权限

#### 基本使用

```java

@DataPermission(field = "user_id")
public List<Order> getUserOrders() {
    // 自动匹配 UserDataPermissionProvider
    // 添加 WHERE user_id = '当前用户ID'
    return orderMapper.selectList(null);
}

// 门店级权限控制
@DataPermission(field = "store_id")
public List<Product> getStoreProducts() {
    // 自动匹配 StoreDataPermissionProvider
    // 添加 WHERE store_id IN ('当前用户可访问的门店ID列表')
    return productMapper.selectList(null);
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
```

### 📝 数据变更日志

```java
// 在实体类或方法上使用 @AuditLog 注解
@AuditLog(module = "用户管理", operation = "更新用户信息")
@Data
@TableName("user")
public class User {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    // 敏感字段加密
    @EncryptField(value = EncryptType.AES)
    private String phone;

    @EncryptField(EncryptType.AES)
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
    private String deptId;
}
```

### 🏢 多租户使用

```java
// 设置租户上下文
TenantContextHolder.setCurrentTenantId("tenant_001");

// 在租户上下文中执行操作
TenantContextHolder.

runWithTenant("tenant_002",() ->{
        // 这里的数据库操作会自动添加 tenant_id = 'tenant_002' 条件
        userMapper.

selectList(null);
});

// 清除租户上下文
        TenantContextHolder.

clear();
```

## 🔧 高级用法

### 数据权限缓存管理

当启用缓存管理接口时，可以通过以下接口管理权限缓存：

```bash
# 获取缓存统计
GET /api/internal/permission-cache/stats

# 检查缓存健康
GET /api/internal/permission-cache/health

# 清空所有缓存
DELETE /api/internal/permission-cache/all

# 清空用户缓存
DELETE /api/internal/permission-cache/user/{userId}
```

### 自定义审计存储

```java
@Component
public class DatabaseAuditStorage implements AuditStorage {

    @Override
    public void save(AuditLogEntry auditLogEntry) {
        // 保存到数据库
        // 或发送到消息队列
        // 或写入文件系统
    }
}
```

### 加密算法支持

支持的加密算法：

- **AES**：高级加密标准，推荐使用
- **DES**：数据加密标准
- **3DES**：三重数据加密算法
- **SM2**：国密椭圆曲线公钥密码算法
- **SM4**：国密对称加密算法

## ⚠️ 重要提醒

### 安全建议

1. **密钥管理**：生产环境从外部配置或密钥管理系统获取
2. **哈希查询**：加密字段无法直接查询，使用哈希字段
3. **索引优化**：对租户字段、权限字段、哈希字段建立索引

### 性能考虑

1. **加密开销**：只对真正敏感的字段加密
2. **权限范围**：避免过于复杂的权限范围查询
3. **批量操作**：大批量操作时注意加密性能影响
4. **缓存使用**：合理配置权限缓存，提升查询性能

### 开发注意

1. **上下文清理**：确保请求结束时清理上下文
2. **测试环境**：可以关闭加密便于调试
3. **日志安全**：避免在日志中输出敏感信息

## 📄 许可证

MIT License - 查看 [LICENSE](LICENSE) 文件了解详情。