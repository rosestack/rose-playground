# 等保三级审计日志系统设计文档

## 1. 概述

本文档描述了符合《网络安全等级保护基本要求》GB/T 22239-2019 第三级要求的审计日志系统设计方案。

## 2. 需要记录的日志类型

### 2.1 用户行为日志（User Behavior Logs）

#### 2.1.1 认证相关
- **用户登录**：成功/失败登录记录，包含IP、时间、设备信息
- **用户登出**：主动登出、超时登出、强制登出
- **密码操作**：密码修改、重置、过期提醒
- **多因子认证**：短信验证、邮箱验证、硬件令牌
- **会话管理**：会话创建、销毁、超时、并发控制

#### 2.1.2 权限管理相关
- **角色操作**：角色分配、撤销、权限变更
- **权限检查**：访问控制检查结果（允许/拒绝）
- **用户组管理**：用户组创建、修改、删除、成员变更
- **权限提升**：临时权限申请、审批、使用

### 2.2 数据操作日志（Data Operation Logs）

#### 2.2.1 数据库操作
- **CRUD操作**：增删改查操作，记录SQL语句、参数、执行时间
- **批量操作**：批量导入、导出、更新、删除
- **敏感数据访问**：个人信息、财务数据、机密信息访问
- **数据变更**：字段级别的数据变更前后对比

#### 2.2.2 业务数据操作
- **订单管理**：订单创建、修改、取消、退款
- **用户信息**：用户资料修改、状态变更
- **财务操作**：支付、退款、账户余额变更
- **配置管理**：系统参数、业务规则配置变更

### 2.3 系统操作日志（System Operation Logs）

#### 2.3.1 系统管理
- **服务管理**：服务启动、停止、重启、健康检查
- **配置变更**：系统配置、应用配置、环境变量修改
- **定时任务**：定时任务执行、失败、重试
- **系统维护**：数据库维护、缓存清理、日志清理

#### 2.3.2 文件操作
- **文件管理**：文件上传、下载、删除、移动
- **权限变更**：文件访问权限修改
- **敏感文件**：配置文件、证书文件、密钥文件操作

### 2.4 网络活动日志（Network Activity Logs）

#### 2.4.1 API调用
- **REST API**：HTTP请求/响应、状态码、耗时
- **内部服务**：微服务间调用、RPC调用
- **外部集成**：第三方API调用、回调处理
- **WebSocket**：连接建立、消息传输、连接断开

#### 2.4.2 网络安全
- **异常访问**：异常IP、异常时间、异常频率
- **跨域访问**：CORS请求、跨域资源访问
- **防火墙**：防火墙规则触发、阻断记录
- **负载均衡**：请求分发、健康检查

### 2.5 安全事件日志（Security Event Logs）

#### 2.5.1 攻击检测
- **注入攻击**：SQL注入、NoSQL注入、命令注入
- **跨站攻击**：XSS攻击、CSRF攻击
- **暴力破解**：密码暴力破解、验证码暴力破解
- **恶意扫描**：端口扫描、漏洞扫描

#### 2.5.2 异常行为
- **异常登录**：异地登录、异常时间登录、多设备登录
- **权限异常**：权限提升尝试、越权访问
- **数据异常**：大量数据下载、敏感数据批量访问
- **系统异常**：系统错误、服务异常、性能异常

## 3. 数据模型设计

### 3.1 核心审计日志表（audit_log）

```sql
CREATE TABLE audit_log (
    -- 基础信息
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    event_time DATETIME(3) NOT NULL COMMENT '事件时间戳（精确到毫秒）',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
    event_subtype VARCHAR(50) COMMENT '事件子类型',
    risk_level VARCHAR(20) NOT NULL COMMENT '风险等级',
    
    -- 用户信息
    user_id VARCHAR(64) COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户名',

    -- HTTP信息
    request_uri VARCHAR(500) COMMENT '请求URI',
    http_method VARCHAR(10) COMMENT 'HTTP方法',
    http_status INT COMMENT 'HTTP状态码',

    -- 会话和网络信息
    session_id VARCHAR(128) COMMENT '会话ID',
    client_ip VARCHAR(45) COMMENT '客户端IP地址',
    geo_location VARCHAR(200) COMMENT '地理位置信息',
    user_agent VARCHAR(100) COMMENT '用户代理简要信息',

    -- 操作信息
    operation_name VARCHAR(200) COMMENT '具体业务操作名称',
    status VARCHAR(20) NOT NULL COMMENT '操作状态',

    -- 系统信息
    app_name VARCHAR(100) COMMENT '应用名称',
    server_ip varchar(45) DEFAULT NULL COMMENT '服务器IP',
    tenant_id VARCHAR(50) COMMENT '租户ID',
    trace_id VARCHAR(100) COMMENT '追踪ID',
    
    -- 结果信息
    execution_time BIGINT COMMENT '执行耗时（毫秒）',
    error_code VARCHAR(50) COMMENT '错误代码',
    
    -- 完整性保护
    digital_signature VARCHAR(512) COMMENT '数字签名',
    hash_value VARCHAR(128) COMMENT '哈希值',
    prev_hash VARCHAR(128) COMMENT '前一条记录的哈希值',
    
    -- 系统字段
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '逻辑删除标记'
) COMMENT='等保三级审计日志主表'
PARTITION BY RANGE (YEAR(event_time) * 100 + MONTH(event_time)) (
    PARTITION p202401 VALUES LESS THAN (202402),
    PARTITION p202402 VALUES LESS THAN (202403),
    -- 按月分区，提高查询性能
);
```

### 3.2 审计日志详情表（audit_log_detail）

#### 3.2.1 设计目的
审计日志详情表专门用于存储**大文本数据**和**复杂结构信息**，实现主表瘦身和性能优化：

- **主表瘦身**：主表只存储核心审计信息和高频查询字段
- **数据分类存储**：详情表存储大文本、复杂对象等详细信息
- **存储优化**：使用TEXT类型存储大文本，支持数据压缩

#### 3.2.2 字段分配原则

**主表字段（高频查询 + 小数据量）**：
- 核心审计信息：event_type, event_subtype, operation_name, status
- HTTP基础信息：request_uri, http_method, http_status
- 会话网络信息：session_id, client_ip, geo_location
- 用户代理简要：user_agent（如"Chrome/120.0 Windows"）
- 系统信息：app_name, tenant_id, trace_id

**详情表字段（大文本 + 复杂结构）**：
- HTTP详细信息：REQUEST_PARAMS, REQUEST_BODY, REQUEST_HEADERS
- 完整用户代理：REQUEST_HEADERS中包含完整User-Agent字符串
- 响应详情：RESPONSE_RESULT, RESPONSE_HEADERS
- 数据变更：DATA_CHANGE_BEFORE, DATA_CHANGE_AFTER
- 异常信息：EXCEPTION_STACK, ERROR_CONTEXT

#### 3.2.2 表结构设计

```sql
CREATE TABLE audit_log_detail (
    -- 基础字段
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    audit_log_id BIGINT NOT NULL COMMENT '审计日志ID',

    -- 详情分类
    detail_type VARCHAR(50) NOT NULL COMMENT '详情类型',
    detail_key VARCHAR(50) NOT NULL COMMENT '详情键',
    detail_value TEXT COMMENT '详情值（JSON格式，可能加密脱敏）',

    -- 安全标记
    is_sensitive BOOLEAN DEFAULT FALSE COMMENT '是否包含敏感数据',
    is_encrypted BOOLEAN DEFAULT FALSE COMMENT '是否已加密存储',

    -- 系统字段
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    tenant_id VARCHAR(50) COMMENT '租户ID'
) COMMENT='审计日志详情表'
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
ROW_FORMAT=COMPRESSED;  -- 启用压缩存储
```

#### 3.2.3 枚举定义

**详情类型枚举（AuditDetailType）**：
```java
@Getter
public enum AuditDetailType {
    HTTP_REQUEST("HTTP请求相关"),
    OPERATION_TARGET("操作对象相关"),
    DATA_CHANGE("数据变更相关"),
    SYSTEM_TECH("系统技术相关"),
    SECURITY("安全相关");

    private final String description;

    AuditDetailType(String description) {
        this.description = description;
    }
}
```

**详情键枚举（AuditDetailKey）**：
```java
@Getter
public enum AuditDetailKey {
    // HTTP请求相关
    REQUEST_PARAMS("REQUEST_PARAMS", "HTTP请求参数"),
    REQUEST_BODY("REQUEST_BODY", "HTTP请求体"),
    REQUEST_HEADERS("REQUEST_HEADERS", "HTTP请求头"),
    REQUEST_COOKIES("REQUEST_COOKIES", "HTTP请求Cookie"),
    RESPONSE_RESULT("RESPONSE_RESULT", "HTTP响应结果"),
    RESPONSE_HEADERS("RESPONSE_HEADERS", "HTTP响应头"),

    // 操作对象相关
    TARGET_INFO("TARGET_INFO", "操作目标对象信息"),
    OPERATION_CONTEXT("OPERATION_CONTEXT", "操作上下文"),
    BUSINESS_DATA("BUSINESS_DATA", "业务数据快照"),

    // 数据变更相关
    DATA_CHANGE_BEFORE("DATA_CHANGE_BEFORE", "变更前数据"),
    DATA_CHANGE_AFTER("DATA_CHANGE_AFTER", "变更后数据"),
    DATA_CHANGE_DIFF("DATA_CHANGE_DIFF", "变更差异对比"),
    SQL_STATEMENT("SQL_STATEMENT", "执行的SQL语句"),
    SQL_PARAMETERS("SQL_PARAMETERS", "SQL参数"),

    // 系统技术相关
    SYSTEM_ENV("SYSTEM_ENV", "系统环境信息"),
    PERFORMANCE_METRICS("PERFORMANCE_METRICS", "性能指标"),
    ERROR_DETAIL("ERROR_DETAIL", "错误详情"),
    EXCEPTION_STACK("EXCEPTION_STACK", "异常堆栈"),
    DEBUG_INFO("DEBUG_INFO", "调试信息"),

    // 安全相关
    SECURITY_CONTEXT("SECURITY_CONTEXT", "安全上下文"),
    PERMISSION_CHECK("PERMISSION_CHECK", "权限检查详情"),
    RISK_ASSESSMENT("RISK_ASSESSMENT", "风险评估结果"),
    THREAT_INDICATORS("THREAT_INDICATORS", "威胁指标");

    private final String code;
    private final String description;

    AuditDetailKey(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
```

#### 3.2.4 错误信息存储优化

**问题分析**：
- 主表的 `error_message` 与详情表的 `RESPONSE_RESULT` 存在重复
- 错误信息可能很长，不适合放在主表

**优化方案**：
- **主表**：只保留 `error_code`（如"INVALID_PARAMETER"、"NETWORK_TIMEOUT"）
- **详情表**：使用 `ERROR_DETAIL` 存储完整错误信息

**存储策略**：
```java
// 主表：简洁的错误代码
audit_log.error_code = "VALIDATION_FAILED";

// 详情表：完整的错误详情
{
  "ERROR_DETAIL": {
    "errorMessage": "用户名不能为空，密码长度不能少于8位",
    "errorCode": "VALIDATION_FAILED",
    "fieldErrors": [
      {"field": "username", "message": "用户名不能为空"},
      {"field": "password", "message": "密码长度不能少于8位"}
    ],
    "stackTrace": "com.example.ValidationException: ...",
    "timestamp": "2024-01-31T10:30:45.123Z"
  }
}
```

**与 RESPONSE_RESULT 的区别**：
- **RESPONSE_RESULT**：HTTP响应的完整结果（包含业务数据）
- **ERROR_DETAIL**：专门的错误详情（仅在出错时记录）

```java
// 成功响应
{
  "RESPONSE_RESULT": {
    "statusCode": 200,
    "data": {"userId": 123, "userName": "张三"},
    "message": "操作成功"
  }
}

// 失败响应
{
  "RESPONSE_RESULT": {
    "statusCode": 400,
    "message": "请求参数错误"
  },
  "ERROR_DETAIL": {
    "errorMessage": "用户名不能为空，密码长度不能少于8位",
    "errorCode": "VALIDATION_FAILED",
    "fieldErrors": [...],
    "stackTrace": "..."
  }
}
```

#### 3.2.4 存储格式说明

**JSON格式存储优势**：
- **结构化数据**：复杂对象以JSON格式存储，便于查询和解析
- **扩展性强**：可以灵活添加新字段，无需修改表结构
- **查询便利**：MySQL 5.7+ 支持JSON字段的高效查询
- **数据完整**：保持原始数据的完整结构

**存储示例对比**：
```sql
-- 旧方式：多行存储
(1, 1001, 'REQUEST_PARAMS', 'userId', '12345', 'STRING'),
(2, 1001, 'REQUEST_PARAMS', 'pageSize', '20', 'INTEGER'),
(3, 1001, 'REQUEST_PARAMS', 'sortBy', 'createTime', 'STRING'),

-- 新方式：JSON存储
(1, 1001, 'HTTP请求相关', 'REQUEST_PARAMS', '{
  "userId": "12345",
  "pageSize": 20,
  "sortBy": "createTime"
}', 'JSON')
```

**查询示例**：
```sql
-- 查询特定参数
SELECT detail_value->'$.userId' as user_id
FROM audit_log_detail
WHERE detail_key = 'REQUEST_PARAMS';

-- 查询包含特定字段的记录
SELECT * FROM audit_log_detail
WHERE detail_key = 'REQUEST_PARAMS'
  AND JSON_CONTAINS_PATH(detail_value, 'one', '$.userId');
```

#### 3.2.4 加密和脱敏处理

**敏感数据识别**：
- 包含个人信息、密码、Token等敏感字段
- 设置 `is_sensitive = TRUE`
- 自动进行脱敏和加密处理

**脱敏规则**：
```java
// 常见敏感字段脱敏
private static final Map<String, String> MASK_PATTERNS = Map.of(
    "password", "**MASKED**",
    "token", "**MASKED**",
    "authorization", "Bearer **MASKED**",
    "phone", "138****5678",
    "idCard", "110101****1234",
    "bankCard", "6222****1234"
);
```

**加密存储**：
```java
// 敏感数据加密存储
public void saveAuditDetail(AuditDetailRequest request) {
    String detailValue = JsonUtils.toJson(request.getData());
    boolean isSensitive = containsSensitiveData(request.getData());

    if (isSensitive) {
        // 1. 脱敏处理
        detailValue = maskSensitiveFields(detailValue);
        // 2. 加密存储
        detailValue = encryptService.encrypt(detailValue);
    }

    AuditLogDetail detail = AuditLogDetail.builder()
        .auditLogId(request.getAuditLogId())
        .detailType(request.getDetailType().getDescription())
        .detailKey(request.getDetailKey().getCode())
        .detailValue(detailValue)
        .isSensitive(isSensitive)
        .isEncrypted(isSensitive)
        .build();

    detailMapper.insert(detail);
}
```

**查询解密**：
```java
// 查询时解密处理
public String getDetailValue(AuditLogDetail detail) {
    String value = detail.getDetailValue();

    if (detail.getIsEncrypted()) {
        // 需要权限验证
        if (!hasDecryptPermission()) {
            return "***ENCRYPTED***";
        }
        value = encryptService.decrypt(value);
    }

    return value;
}
```

## 4. 枚举定义

### 4.1 事件类型（AuditEventType）

```java
@Getter
public enum AuditEventType {

    // ==================== 认证类 ====================
    AUTH_LOGIN("认证", "用户登录"),           // 成功失败由status决定
    AUTH_LOGOUT("认证", "用户登出"),
    AUTH_PASSWORD_CHANGE("认证", "密码修改"),
    AUTH_SESSION_TIMEOUT("认证", "会话超时"),

    // ==================== 授权类 ====================
    AUTHZ_PERMISSION_DENIED("授权", "权限拒绝"),  // 包含各种访问违规
    AUTHZ_CHANGE("授权", "授权变更"),

    // ==================== 数据类 ====================
    DATA_CREATE("数据", "数据创建"),
    DATA_READ("数据", "数据读取"),
    DATA_UPDATE("数据", "数据更新"),
    DATA_DELETE("数据", "数据删除"),
    DATA_BATCH_OPERATION("数据", "批量操作"),
    DATA_EXPORT("数据", "数据导出"),
    DATA_IMPORT("数据", "数据导入"),
    DATA_SENSITIVE_ACCESS("数据", "敏感数据访问"),

    // ==================== 系统类 ====================
    SYS_CONFIG_CHANGE("系统", "配置变更"),
    SYS_SERVICE_CONTROL("系统", "服务控制"),
    SYS_FILE_OPERATION("系统", "文件操作"),
    SYS_MAINTENANCE("系统", "系统维护"),
    SYS_EXTERNAL_REQUEST("系统", "外部请求"),    // 原网络类迁移过来

    // ==================== 安全类 ====================
    SEC_ATTACK_DETECTION("安全", "攻击检测"),
    SEC_ABNORMAL_BEHAVIOR("安全", "异常行为");
}
```

### 4.2 风险等级（AuditRiskLevel）

```java
public enum AuditRiskLevel {
    LOW("低风险", 1),      // 一般查询操作
    MEDIUM("中风险", 2),   // 数据修改操作
    HIGH("高风险", 3),     // 敏感数据操作、权限变更
    CRITICAL("严重", 4);   // 安全事件、系统故障
}
```

### 4.3 操作结果（AuditOperationResult）

```java
public enum AuditOperationResult {
    SUCCESS("成功"),    // 操作成功
    FAILURE("失败"),    // 操作失败
    ERROR("异常"),      // 系统异常
    DENIED("拒绝");     // 权限拒绝
    /**
     * 事件类型
     */
    private final String eventType;

    /**
     * 事件子类型
     */
    private final String eventSubType;

    AuditEventType(String eventType, String eventSubType) {
        this.eventType = eventType;
        this.eventSubType = eventSubType;
    }

    /**
     * 判断是否为安全事件
     */
    public boolean isSecurityEvent() {
        return "安全".equals(this.eventType);
    }

    /**
     * 判断是否为高风险事件
     */
    public boolean isHighRiskEvent() {
        return this.isSecurityEvent() ||
               this == DATA_SENSITIVE_ACCESS ||
               this == DATA_DELETE ||
               this == SYS_CONFIG_CHANGE ||
               this == AUTHZ_PRIVILEGE_ESCALATION;
    }
}
```

### 4.2 混合模式设计说明

#### 4.2.1 设计原理
采用 **技术分类 + 业务操作** 的混合模式：

- **event_type + event_subtype**：使用枚举，保持技术层面的标准化分类
- **operation_name**：使用字符串，记录具体的业务操作名称

#### 4.2.2 事件分类原则

**认证类**：
- 用户身份验证相关操作
- 成功/失败由 status 字段决定，不单独设置失败类型

**授权类**：
- `权限拒绝`：安全事件，记录访问被拒绝的情况
- `授权变更`：用户角色分配、权限授予等授权关系变更

**数据类**：
- 角色/权限的增删改操作归类为数据操作：
  - 新增角色 → `数据创建` + operation_name="创建管理员角色"
  - 删除权限 → `数据删除` + operation_name="删除用户查看权限"
  - 修改角色 → `数据更新` + operation_name="修改角色权限范围"

**系统类**：
- 系统级别的配置和维护操作

**安全类**：
- 安全威胁检测和异常行为

#### 4.2.3 优化说明

**权限拒绝的覆盖范围**：
```sql
-- 权限不足
('授权', '权限拒绝', '访问管理页面权限不足')

-- 越权访问
('授权', '权限拒绝', '尝试访问其他用户数据')

-- 非法访问
('授权', '权限拒绝', '未登录访问受保护资源')

-- IP限制
('授权', '权限拒绝', '非白名单IP访问')
```

#### 4.2.2 优势分析

**技术分类标准化**：
- 便于系统监控和统计分析
- 支持按技术维度进行查询过滤
- 保持审计日志的规范性

**业务操作灵活化**：
- 支持任意业务操作的扩展
- 不需要修改枚举即可添加新操作
- 业务人员可以直观理解操作含义

#### 4.2.4 使用示例

```java
// 记录订单状态修改
AuditEventType.DATA_UPDATE.getEventType()     // "数据"
AuditEventType.DATA_UPDATE.getEventSubType()  // "数据更新"
operationName = "订单状态修改"                   // 具体业务操作

// 记录用户密码重置
AuditEventType.AUTH_PASSWORD_CHANGE.getEventType()     // "认证"
AuditEventType.AUTH_PASSWORD_CHANGE.getEventSubType()  // "密码修改"
operationName = "管理员重置用户密码"                      // 具体业务操作

// 记录权限拒绝
AuditEventType.AUTHZ_PERMISSION_DENIED.getEventType()     // "授权"
AuditEventType.AUTHZ_PERMISSION_DENIED.getEventSubType()  // "权限拒绝"
operationName = "访问用户管理页面被拒绝"                    // 具体业务操作

// 记录角色分配
AuditEventType.AUTHZ_CHANGE.getEventType()     // "授权"
AuditEventType.AUTHZ_CHANGE.getEventSubType()  // "授权变更"
operationName = "为用户分配管理员角色"             // 具体业务操作

// 记录新增角色（归类为数据操作）
AuditEventType.DATA_CREATE.getEventType()     // "数据"
AuditEventType.DATA_CREATE.getEventSubType()  // "数据创建"
operationName = "创建财务管理员角色"             // 具体业务操作
```

#### 4.2.5 查询场景

```sql
-- 按技术分类查询：查看所有数据操作
SELECT * FROM audit_log WHERE event_type = '数据';

-- 按技术子类查询：查看所有数据更新操作
SELECT * FROM audit_log WHERE event_type = '数据' AND event_subtype = '数据更新';

-- 按业务操作查询：查看订单相关操作
SELECT * FROM audit_log WHERE operation_name LIKE '%订单%';

-- 组合查询：查看订单状态修改操作
SELECT * FROM audit_log
WHERE event_type = '数据'
  AND event_subtype = '数据更新'
  AND operation_name = '订单状态修改';
```

## 5. 基于事件类型的详情表内容示例

### 5.1 认证事件详情

#### AUTH_LOGIN - 用户登录
```sql
-- 主表记录
INSERT INTO audit_log VALUES (
    1001, '2024-01-31 09:15:23.123', '认证', '用户登录', 'user123', '张三',
    '/api/auth/login', 'POST', 200,
    'SESS_789456123', '192.168.1.100', '北京市', 'Chrome/120.0 Windows',
    '用户登录系统', 'SUCCESS',
    'UserApp', 'tenant001', 'trace-abc-123', 1200, NULL, NULL, ...
);

-- 详情表记录（主表已有session_id, geo_location, user_agent）
INSERT INTO audit_log_detail VALUES
(1, 1001, 'HTTP请求相关', 'REQUEST_HEADERS', '{
  "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
  "Accept": "application/json, text/plain, */*",
  "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
  "Content-Type": "application/json",
  "Authorization": "Bearer **MASKED**"
}', FALSE, FALSE, '2024-01-31 09:15:23'),

(2, 1001, '安全相关', 'SECURITY_CONTEXT', '{
  "loginMethod": "PASSWORD",
  "deviceFingerprint": "fp_**MASKED**",
  "ipLocation": "北京市朝阳区",
  "riskScore": 15,
  "authFactors": ["password", "captcha"]
}', TRUE, TRUE, '2024-01-31 09:15:23');
```

#### AUTH_LOGIN_FAILED - 登录失败
```sql
-- 主表记录（移除error_message，只保留error_code）
INSERT INTO audit_log VALUES (
    1002, '2024-01-31 09:20:15.456', '认证', '登录失败', 'user456', '李四',
    '/api/auth/login', 'POST', 401,
    'SESS_FAILED', '192.168.1.200', '北京市', 'Chrome/120.0 Windows',
    '用户登录失败', 'FAILURE',
    'UserApp', '10.0.0.5', 'tenant001', 'trace-def-456', 800, 'INVALID_PASSWORD', ...
);

-- 详情表记录（使用新的ERROR_DETAIL格式）
INSERT INTO audit_log_detail VALUES
(7, 1002, '系统技术相关', 'ERROR_DETAIL', '{
  "errorMessage": "密码错误，连续失败3次",
  "errorCode": "INVALID_PASSWORD",
  "attemptCount": 3,
  "lastSuccessLogin": "2024-01-30 18:30:00",
  "lockoutTime": "2024-01-31 09:25:15",
  "remainingAttempts": 0
}', TRUE, FALSE, '2024-01-31 09:20:15'),

(8, 1002, '安全相关', 'SECURITY_CONTEXT', '{
  "riskScore": 85,
  "riskFactors": ["frequent_failures", "suspicious_ip", "unusual_time"],
  "actionTaken": "ACCOUNT_LOCKED"
}', TRUE, TRUE, '2024-01-31 09:20:15'),

(9, 1002, '安全相关', 'THREAT_INDICATORS', '{
  "suspiciousIP": true,
  "bruteForcePattern": "detected",
  "ipReputation": "suspicious",
  "geoAnomaly": false
}', TRUE, FALSE, '2024-01-31 09:20:15');
```

### 5.2 数据事件详情

#### DATA_UPDATE - 数据更新（业务示例）
```sql
-- 订单状态修改
INSERT INTO audit_log VALUES (
    2001, '2024-01-31 10:30:45.789', '数据', '数据更新', '订单状态修改', 'MEDIUM',
    'user789', '王五', '192.168.1.50', '10.0.0.8', 'SUCCESS',
    'OrderApp', 'tenant002', 'trace-ghi-789', 2500, NULL, NULL, ...
);

-- 商品价格调整
INSERT INTO audit_log VALUES (
    2002, '2024-01-31 10:35:20.123', '数据', '数据更新', '商品价格调整', 'HIGH',
    'admin001', '管理员', '192.168.1.10', '10.0.0.8', 'SUCCESS',
    'ProductApp', 'tenant002', 'trace-xyz-456', 1800, NULL, NULL, ...
);

-- 用户信息更新
INSERT INTO audit_log VALUES (
    2003, '2024-01-31 10:40:15.456', '数据', '数据更新', '用户信息更新', 'LOW',
    'user456', '李四', '192.168.1.50', '10.0.0.8', 'SUCCESS',
    'UserApp', 'tenant001', 'trace-abc-789', 1200, NULL, NULL, ...
);

-- 详情表记录
INSERT INTO audit_log_detail VALUES
(12, 2001, '操作对象相关', 'TARGET_INFO', '{
  "tableName": "orders",
  "recordId": "ORD_20240131_001",
  "entityType": "Order",
  "businessKey": "订单号: 2024013100001"
}', 'JSON'),

(13, 2001, '数据变更相关', 'DATA_CHANGE_BEFORE', '{
  "status": "PENDING",
  "amount": 1000.00,
  "updateTime": "2024-01-31 10:25:30",
  "version": 1
}', 'JSON'),

(14, 2001, '数据变更相关', 'DATA_CHANGE_AFTER', '{
  "status": "CONFIRMED",
  "amount": 1200.00,
  "updateTime": "2024-01-31 10:30:45",
  "version": 2
}', 'JSON'),

(15, 2001, '数据变更相关', 'SQL_STATEMENT', 'UPDATE orders SET status=?, amount=?, update_time=?, version=? WHERE id=? AND version=?', 'STRING'),

(16, 2001, '数据变更相关', 'SQL_PARAMETERS', '{
  "param1": "CONFIRMED",
  "param2": 1200.00,
  "param3": "2024-01-31 10:30:45",
  "param4": 2,
  "param5": "ORD_20240131_001",
  "param6": 1
}', 'JSON');
```

#### DATA_SENSITIVE_ACCESS - 敏感数据访问
```sql
-- 主表记录
INSERT INTO audit_log VALUES (
    2002, '2024-01-31 11:15:30.234', '数据', '敏感数据访问', 'HIGH',
    'admin001', '管理员', '192.168.1.10', '10.0.0.3', 'SUCCESS',
    'AdminApp', 'tenant001', 'trace-jkl-012', 1800, NULL, NULL, ...
);

-- 详情表记录
INSERT INTO audit_log_detail VALUES
(22, 2002, 'TARGET_INFO', 'dataType', 'PERSONAL_INFO', 'STRING'),
(23, 2002, 'TARGET_INFO', 'recordCount', '50', 'INTEGER'),
(24, 2002, 'BUSINESS_DATA', 'queryCondition', 'phone LIKE "138%"', 'STRING'),
(25, 2002, 'PERMISSION_CHECK', 'requiredRole', 'DATA_ANALYST', 'STRING'),
(26, 2002, 'PERMISSION_CHECK', 'userRole', 'ADMIN', 'STRING'),
(27, 2002, 'RISK_ASSESSMENT', 'dataClassification', 'CONFIDENTIAL', 'STRING'),
(28, 2002, 'RISK_ASSESSMENT', 'accessPurpose', 'COMPLIANCE_AUDIT', 'STRING');
```

### 5.3 系统事件详情

#### SYS_CONFIG_CHANGE - 系统配置变更
```sql
-- 主表记录
INSERT INTO audit_log VALUES (
    3001, '2024-01-31 14:20:10.567', '系统', '配置变更', 'HIGH',
    'sysadmin', '系统管理员', '192.168.1.5', '10.0.0.1', 'SUCCESS',
    'ConfigApp', 'tenant001', 'trace-mno-345', 3200, NULL, NULL, ...
);

-- 详情表记录
INSERT INTO audit_log_detail VALUES
(29, 3001, 'TARGET_INFO', 'configModule', 'DATABASE', 'STRING'),
(30, 3001, 'TARGET_INFO', 'configKey', 'max_connections', 'STRING'),
(31, 3001, 'DATA_CHANGE_BEFORE', 'max_connections', '100', 'INTEGER'),
(32, 3001, 'DATA_CHANGE_AFTER', 'max_connections', '200', 'INTEGER'),
(33, 3001, 'OPERATION_CONTEXT', 'changeReason', '性能优化', 'STRING'),
(34, 3001, 'OPERATION_CONTEXT', 'approvalTicket', 'TICKET_2024_001', 'STRING'),
(35, 3001, 'SYSTEM_ENV', 'serverVersion', 'MySQL 8.0.35', 'STRING');
```

### 5.4 网络事件详情

#### NET_API_CALL - API调用
```sql
-- 主表记录
INSERT INTO audit_log VALUES (
    4001, '2024-01-31 15:45:22.890', '网络', 'API调用', 'LOW',
    'api_user', 'API用户', '203.0.113.10', '10.0.0.12', 'SUCCESS',
    'APIGateway', 'tenant003', 'trace-pqr-678', 450, NULL, NULL, ...
);

-- 详情表记录
INSERT INTO audit_log_detail VALUES
(36, 4001, 'HTTP请求相关', 'REQUEST_PARAMS', '{
  "userId": "12345",
  "pageSize": 20,
  "pageNum": 1,
  "sortBy": "createTime",
  "sortOrder": "desc"
}', 'JSON'),

(37, 4001, 'HTTP请求相关', 'REQUEST_BODY', '{
  "filter": {
    "status": "active",
    "dateRange": {
      "start": "2024-01-01",
      "end": "2024-01-31"
    }
  }
}', 'JSON'),

(38, 4001, 'HTTP请求相关', 'REQUEST_HEADERS', '{
  "Content-Type": "application/json",
  "Authorization": "Bearer **MASKED**",
  "X-Request-ID": "req_abc123",
  "User-Agent": "ApiClient/1.0"
}', 'JSON'),

(39, 4001, 'HTTP请求相关', 'RESPONSE_RESULT', '{
  "statusCode": 200,
  "recordCount": 15,
  "totalCount": 150,
  "hasMore": true,
  "data": "[...响应数据...]"
}', 'JSON'),

(40, 4001, '系统技术相关', 'PERFORMANCE_METRICS', '{
  "dbQueryTime": 120,
  "cacheHitRate": 85.5,
  "responseTime": 450,
  "memoryUsage": "45MB",
  "cpuUsage": "12%"
}', 'JSON');
```

### 5.5 安全事件详情

#### SEC_SQL_INJECTION - SQL注入检测
```sql
-- 主表记录
INSERT INTO audit_log VALUES (
    5001, '2024-01-31 16:30:15.123', '安全', 'SQL注入检测', 'CRITICAL',
    'unknown', '未知用户', '198.51.100.50', '10.0.0.15', 'DENIED',
    'WebApp', 'tenant001', 'trace-stu-901', 50, 'SECURITY_VIOLATION', 'SQL注入攻击被阻断', ...
);

-- 详情表记录
INSERT INTO audit_log_detail VALUES
(45, 5001, '安全相关', 'THREAT_INDICATORS', '{
  "attackType": "SQL_INJECTION",
  "maliciousPayload": "\' OR 1=1 --",
  "attackVector": "GET_PARAMETER",
  "sourceIP": "198.51.100.50",
  "userAgent": "sqlmap/1.6.12",
  "detectionTime": "2024-01-31 16:30:15.123"
}', 'JSON'),

(46, 5001, '安全相关', 'SECURITY_CONTEXT', '{
  "wafRule": "RULE_SQL_001",
  "blockAction": "IMMEDIATE_BLOCK",
  "ruleCategory": "SQL_INJECTION_DETECTION",
  "severity": "CRITICAL",
  "actionTaken": "BLOCK_AND_LOG"
}', 'JSON'),

(47, 5001, '安全相关', 'RISK_ASSESSMENT', '{
  "threatLevel": "CRITICAL",
  "confidenceScore": 95,
  "riskFactors": ["known_attack_pattern", "malicious_payload", "suspicious_user_agent"],
  "recommendedAction": "BLOCK_IP_PERMANENTLY"
}', 'JSON'),

(48, 5001, 'HTTP请求相关', 'REQUEST_PARAMS', '{
  "id": "1\' OR 1=1 --",
  "action": "list",
  "format": "json"
}', 'JSON');
```
