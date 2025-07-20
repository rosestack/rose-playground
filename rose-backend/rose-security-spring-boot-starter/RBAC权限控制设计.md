# RBAC 权限控制系统设计文档

## 1. 概述

### 1.1 设计目标

基于 Rose Security Spring Boot Starter 项目，设计一个完整的 RBAC（基于角色的访问控制）权限系统，提供比当前 `Authority` 枚举更细粒度和灵活的权限管理能力。

### 1.2 设计原则

- **向后兼容**：保持与现有 `Authority` 枚举的兼容性
- **多租户支持**：支持租户级别的权限隔离
- **细粒度控制**：提供精确到操作级别的权限控制
- **灵活扩展**：支持动态权限分配和管理
- **性能优化**：通过缓存机制提升权限检查性能

## 2. 当前权限模型分析

### 2.1 Authority 枚举局限性

当前的 `Authority` 枚举（SYS_ADMIN, TENANT_ADMIN, CUSTOMER_USER）存在以下局限性：

- **权限粒度粗**：只能区分三个基本角色级别
- **缺乏灵活性**：无法动态分配细粒度权限
- **难以扩展**：新增权限需要修改代码
- **权限管理复杂**：无法精确控制用户的具体操作权限

### 2.2 现有权限检查流程

```
用户请求 → 获取用户信息 → 检查Authority级别 → 返回结果
```

## 3. RBAC 系统架构设计

### 3.1 核心实体设计

#### 3.1.1 Permission（权限实体）

```java
Permission {
    id: Long                    // 权限ID
    name: String               // 权限名称（如：user:create, user:read）
    description: String        // 权限描述
    category: String           // 权限分类（如：用户管理、系统管理）
    module: String             // 所属模块（如：user, system, data）
    tenantId: String           // 租户ID（支持多租户隔离）
    enabled: Boolean           // 是否启用
    createdTime: LocalDateTime // 创建时间
    updatedTime: LocalDateTime // 更新时间
}
```

#### 3.1.2 Role（角色实体）

```java
Role {
    id: Long                   // 角色ID
    name: String              // 角色名称
    description: String       // 角色描述
    tenantId: String          // 租户ID
    authority: Authority      // 基础权限级别（保持与现有Authority兼容）
    enabled: Boolean          // 是否启用
    createdTime: LocalDateTime // 创建时间
    updatedTime: LocalDateTime // 更新时间
}
```

#### 3.1.3 UserRole（用户角色关联）

```java
UserRole {
    id: Long                  // 关联ID
    userId: Long             // 用户ID
    roleId: Long             // 角色ID
    tenantId: String         // 租户ID
    createdTime: LocalDateTime // 创建时间
}
```

#### 3.1.4 RolePermission（角色权限关联）

```java
RolePermission {
    id: Long                  // 关联ID
    roleId: Long             // 角色ID
    permissionId: Long       // 权限ID
    tenantId: String         // 租户ID
    createdTime: LocalDateTime // 创建时间
}
```

### 3.2 权限层次结构

```
系统级权限（SYS_ADMIN）
├── 全局用户管理
│   ├── user:create
│   ├── user:read
│   ├── user:update
│   └── user:delete
├── 全局角色管理
│   ├── role:create
│   ├── role:read
│   ├── role:update
│   └── role:delete
├── 全局权限管理
│   ├── permission:create
│   ├── permission:read
│   ├── permission:update
│   └── permission:delete
├── 租户管理
│   ├── tenant:create
│   ├── tenant:read
│   ├── tenant:update
│   └── tenant:delete
└── 系统配置
    ├── system:config
    ├── system:log
    └── system:monitor

租户级权限（TENANT_ADMIN）
├── 租户用户管理
│   ├── tenant:user:create
│   ├── tenant:user:read
│   ├── tenant:user:update
│   └── tenant:user:delete
├── 租户角色管理
│   ├── tenant:role:create
│   ├── tenant:role:read
│   ├── tenant:role:update
│   └── tenant:role:delete
├── 租户权限管理
│   ├── tenant:permission:assign
│   └── tenant:permission:revoke
├── 租户资源管理
│   ├── tenant:resource:create
│   ├── tenant:resource:read
│   ├── tenant:resource:update
│   └── tenant:resource:delete
└── 租户配置
    ├── tenant:config:read
    └── tenant:config:update

用户级权限（CUSTOMER_USER）
├── 个人资料管理
│   ├── profile:read
│   └── profile:update
├── 数据查看权限
│   ├── data:read
│   └── data:export
├── 操作权限
│   ├── operation:create
│   ├── operation:read
│   └── operation:update
└── 功能访问权限
    ├── feature:access
    └── feature:use
```

## 4. 权限控制策略

### 4.1 权限检查流程

```
用户请求 → 获取用户信息 → 检查Authority级别 → 检查RBAC权限 → 返回结果
```

### 4.2 权限检查逻辑

```java
// 权限检查伪代码
public boolean hasPermission(SecurityUser user, String permissionName) {
    // 1. 检查Authority级别
    if (user.getAuthority() == Authority.SYS_ADMIN) {
        return true; // 系统管理员拥有所有权限
    }
    
    // 2. 检查RBAC权限
    Set<String> userPermissions = getUserPermissions(user.getId(), user.getTenantId());
    return userPermissions.contains(permissionName);
}

// 获取用户权限
public Set<String> getUserPermissions(Long userId, String tenantId) {
    // 1. 从缓存获取
    String cacheKey = userId + ":" + tenantId;
    Set<String> permissions = permissionCache.get(cacheKey);
    
    if (permissions == null) {
        // 2. 从数据库获取
        permissions = loadUserPermissionsFromDatabase(userId, tenantId);
        // 3. 放入缓存
        permissionCache.put(cacheKey, permissions);
    }
    
    return permissions;
}
```

### 4.3 权限继承关系

```
SYS_ADMIN
├── 拥有所有全局权限
├── 拥有所有租户权限
└── 可以管理所有租户

TENANT_ADMIN
├── 拥有本租户所有权限
├── 可以管理本租户用户
└── 可以分配本租户角色

CUSTOMER_USER
├── 拥有分配的具体权限
└── 只能访问授权资源
```

## 5. 权限命名规范

### 5.1 权限命名格式

```
{模块}:{操作}
例如：
- user:create    // 创建用户
- user:read      // 查看用户
- user:update    // 更新用户
- user:delete    // 删除用户
- role:assign    // 分配角色
- permission:grant // 授予权限
```

### 5.2 权限分类示例

#### 5.2.1 用户管理模块
```
user:create      // 创建用户
user:read        // 查看用户
user:update      // 更新用户
user:delete      // 删除用户
user:list        // 用户列表
user:export      // 导出用户
user:import      // 导入用户
user:reset       // 重置密码
user:lock        // 锁定用户
user:unlock      // 解锁用户
```

#### 5.2.2 角色管理模块
```
role:create      // 创建角色
role:read        // 查看角色
role:update      // 更新角色
role:delete      // 删除角色
role:list        // 角色列表
role:assign      // 分配角色
role:revoke      // 撤销角色
role:permission  // 角色权限管理
```

#### 5.2.3 系统管理模块
```
system:config    // 系统配置
system:log       // 系统日志
system:monitor   // 系统监控
system:backup    // 系统备份
system:restore   // 系统恢复
system:shutdown  // 系统关闭
system:restart   // 系统重启
```

## 6. 多租户权限隔离

### 6.1 权限隔离策略

- **全局权限**：`tenantId = null`，只有系统管理员可以管理
- **租户权限**：`tenantId = 具体租户ID`，租户管理员可以管理
- **用户权限**：基于用户所属租户进行权限检查

### 6.2 租户权限检查

```java
// 租户权限检查伪代码
public boolean hasTenantPermission(SecurityUser user, String permissionName, String targetTenantId) {
    // 系统管理员可以访问所有租户
    if (user.getAuthority() == Authority.SYS_ADMIN) {
        return true;
    }
    
    // 租户管理员只能访问本租户
    if (user.getAuthority() == Authority.TENANT_ADMIN) {
        return user.getTenantId().equals(targetTenantId);
    }
    
    // 普通用户只能访问本租户
    if (user.getAuthority() == Authority.CUSTOMER_USER) {
        return user.getTenantId().equals(targetTenantId);
    }
    
    return false;
}
```

## 7. 权限缓存策略

### 7.1 缓存设计

```java
// 用户权限缓存
Map<String, Set<String>> userPermissionCache;
// Key: userId:tenantId
// Value: Set<permissionName>

// 角色权限缓存
Map<String, Set<String>> rolePermissionCache;
// Key: roleId:tenantId
// Value: Set<permissionName>

// 用户角色缓存
Map<String, Set<String>> userRoleCache;
// Key: userId:tenantId
// Value: Set<roleName>
```

### 7.2 缓存更新策略

- **权限变更时**：清除相关用户和角色的权限缓存
- **角色分配时**：清除用户权限缓存
- **定时刷新**：定期刷新权限缓存，确保数据一致性
- **缓存失效**：设置合理的缓存过期时间

### 7.3 缓存配置

```yaml
# 权限缓存配置
security:
  rbac:
    cache:
      enabled: true
      ttl: 3600  # 缓存过期时间（秒）
      max-size: 10000  # 最大缓存条目数
      eviction-policy: LRU  # 缓存淘汰策略
```

## 8. 权限注解支持

### 8.1 自定义注解

```java
// 单个权限检查
@HasPermission("user:create")

// 角色检查
@HasRole("ADMIN")

// 多个权限检查（任一满足）
@HasAnyPermission({"user:create", "user:update"})

// 多个角色检查（任一满足）
@HasAnyRole({"ADMIN", "MANAGER"})

// 租户权限检查
@HasTenantPermission("user:create")
```

### 8.2 权限表达式

```java
// Spring Security 表达式
@PreAuthorize("hasPermission('user:create')")
@PreAuthorize("hasRole('ADMIN') and hasPermission('user:delete')")
@PreAuthorize("hasTenantAccess(#tenantId)")
@PreAuthorize("hasAnyPermission({'user:create', 'user:update'})")
```

### 8.3 注解处理器

```java
// 权限注解处理器
@Aspect
@Component
public class PermissionAspect {
    
    @Around("@annotation(hasPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, HasPermission hasPermission) {
        // 权限检查逻辑
        String permission = hasPermission.value();
        if (!permissionService.hasPermission(getCurrentUser(), permission)) {
            throw new AccessDeniedException("权限不足");
        }
        return joinPoint.proceed();
    }
}
```

## 9. 权限管理界面设计

### 9.1 权限管理功能

#### 9.1.1 权限列表
- 查看所有权限，支持按模块、分类筛选
- 权限搜索和分页
- 权限状态管理（启用/禁用）
- 权限详情查看

#### 9.1.2 角色管理
- 创建、编辑、删除角色
- 角色权限分配
- 角色用户管理
- 角色继承关系

#### 9.1.3 用户角色
- 为用户分配角色
- 查看用户权限
- 批量角色分配
- 角色分配历史

#### 9.1.4 权限分配
- 为角色分配权限
- 批量权限操作
- 权限分配模板
- 权限分配审计

### 9.2 权限可视化

#### 9.2.1 权限树
- 以树形结构展示权限分类
- 支持权限展开/折叠
- 权限搜索高亮
- 权限状态标识

#### 9.2.2 角色权限矩阵
- 以矩阵形式展示角色与权限的关系
- 支持批量权限分配
- 权限继承关系展示
- 权限冲突检测

#### 9.2.3 用户权限视图
- 展示用户的具体权限
- 权限来源追踪
- 权限有效期管理
- 权限使用统计

## 10. 安全考虑

### 10.1 权限验证

#### 10.1.1 服务层验证
```java
@Service
public class UserService {
    
    @Autowired
    private PermissionService permissionService;
    
    public User createUser(UserCreateRequest request) {
        // 权限检查
        if (!permissionService.hasPermission(getCurrentUser(), "user:create")) {
            throw new AccessDeniedException("无创建用户权限");
        }
        
        // 业务逻辑
        return userRepository.save(user);
    }
}
```

#### 10.1.2 控制器验证
```java
@RestController
public class UserController {
    
    @PostMapping("/users")
    @HasPermission("user:create")
    public ResponseEntity<User> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }
}
```

#### 10.1.3 数据层验证
```java
@Repository
public class UserRepository {
    
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId")
    List<User> findByTenantId(@Param("tenantId") String tenantId);
}
```

### 10.2 权限审计

#### 10.2.1 权限变更日志
- 记录所有权限变更操作
- 包含操作人、操作时间、操作内容
- 支持权限变更回滚
- 权限变更通知

#### 10.2.2 权限使用日志
- 记录权限检查和使用情况
- 异常权限使用检测
- 权限使用统计分析
- 权限使用报告

#### 10.2.3 异常权限检测
- 检测异常的权限使用行为
- 权限使用频率监控
- 权限使用模式分析
- 安全事件告警

## 11. 迁移策略

### 11.1 渐进式迁移

#### 11.1.1 阶段一：保持兼容
- 保留现有的 `Authority` 枚举
- 实现 RBAC 系统基础功能
- 两个权限系统并行运行
- 逐步迁移核心功能

#### 11.1.2 阶段二：并行运行
- RBAC 系统与现有权限系统并行
- 新功能使用 RBAC 系统
- 旧功能逐步迁移
- 权限数据同步

#### 11.1.3 阶段三：逐步迁移
- 逐步将权限控制迁移到 RBAC 系统
- 功能模块逐个迁移
- 权限数据逐步迁移
- 用户培训和支持

#### 11.1.4 阶段四：完全替换
- 完全使用 RBAC 系统
- 移除旧的权限控制代码
- 系统优化和性能调优
- 文档更新和培训

### 11.2 数据迁移

#### 11.2.1 权限数据初始化
- 根据现有功能初始化权限数据
- 创建基础权限分类
- 设置权限描述和模块
- 权限数据验证

#### 11.2.2 角色数据迁移
- 将现有角色映射到 RBAC 角色
- 创建默认角色模板
- 角色权限预分配
- 角色数据验证

#### 11.2.3 用户权限迁移
- 将用户权限迁移到 RBAC 系统
- 用户角色分配
- 权限继承关系建立
- 用户权限验证

## 12. 性能优化

### 12.1 数据库优化

#### 12.1.1 索引设计
```sql
-- 权限表索引
CREATE INDEX idx_permission_name ON security_permissions(name);
CREATE INDEX idx_permission_tenant ON security_permissions(tenant_id);
CREATE INDEX idx_permission_module ON security_permissions(module);

-- 角色表索引
CREATE INDEX idx_role_name ON security_roles(name);
CREATE INDEX idx_role_tenant ON security_roles(tenant_id);

-- 用户角色关联表索引
CREATE INDEX idx_user_role_user ON security_user_roles(user_id);
CREATE INDEX idx_user_role_tenant ON security_user_roles(tenant_id);

-- 角色权限关联表索引
CREATE INDEX idx_role_permission_role ON security_role_permissions(role_id);
CREATE INDEX idx_role_permission_tenant ON security_role_permissions(tenant_id);
```

#### 12.1.2 查询优化
- 使用批量查询减少数据库访问
- 优化权限检查查询语句
- 使用数据库连接池
- 定期清理无效数据

### 12.2 缓存优化

#### 12.2.1 缓存策略
- 使用 Redis 作为分布式缓存
- 实现多级缓存机制
- 缓存预热和更新策略
- 缓存监控和统计

#### 12.2.2 缓存配置
```yaml
# Redis 缓存配置
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

## 13. 监控和运维

### 13.1 性能监控

#### 13.1.1 权限检查性能
- 权限检查响应时间监控
- 权限检查频率统计
- 缓存命中率监控
- 数据库查询性能监控

#### 13.1.2 系统资源监控
- CPU 和内存使用率
- 数据库连接池状态
- 缓存使用情况
- 网络 I/O 性能

### 13.2 日志管理

#### 13.2.1 权限日志
- 权限检查日志
- 权限变更日志
- 权限异常日志
- 权限审计日志

#### 13.2.2 日志配置
```yaml
# 日志配置
logging:
  level:
    io.github.rose.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 14. 测试策略

### 14.1 单元测试

#### 14.1.1 权限服务测试
- 权限检查逻辑测试
- 角色权限关联测试
- 用户权限获取测试
- 权限缓存测试

#### 14.1.2 注解处理器测试
- 权限注解处理测试
- 角色注解处理测试
- 异常处理测试
- 性能测试

### 14.2 集成测试

#### 14.2.1 API 测试
- 权限控制 API 测试
- 角色管理 API 测试
- 用户权限 API 测试
- 权限分配 API 测试

#### 14.2.2 数据库测试
- 权限数据操作测试
- 角色数据操作测试
- 关联数据操作测试
- 事务处理测试

### 14.3 端到端测试

#### 14.3.1 用户场景测试
- 用户登录和权限检查
- 角色分配和权限验证
- 权限变更和实时生效
- 多租户权限隔离

## 15. 总结

### 15.1 设计优势

- **细粒度控制**：提供精确到操作级别的权限控制
- **灵活扩展**：支持动态权限分配和管理
- **多租户支持**：完整的租户权限隔离机制
- **性能优化**：通过缓存机制提升权限检查性能
- **向后兼容**：保持与现有系统的兼容性

### 15.2 实施建议

- **分阶段实施**：采用渐进式迁移策略
- **充分测试**：确保权限系统的正确性和性能
- **用户培训**：提供完善的用户培训和支持
- **持续优化**：根据使用情况持续优化系统

### 15.3 后续扩展

- **ABAC 支持**：基于属性的访问控制
- **动态权限**：支持运行时权限变更
- **权限模板**：预定义权限分配模板
- **权限分析**：权限使用分析和优化建议

---

*本文档为 Rose Security Spring Boot Starter 项目的 RBAC 权限控制系统设计文档，提供了完整的权限管理解决方案。* 