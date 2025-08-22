# 多租户计费系统设计方案

## 1. 概述

基于Rose Playground平台的多租户SaaS架构，设计一个完整的计费系统来支持订阅管理、使用量统计、账单生成和支付处理。该系统将集成到现有的Spring Boot 3.x架构中，利用平台的多租户能力、审计日志、通知系统等基础组件。

### 1.1 设计目标

- **多租户隔离**：确保不同租户的计费数据完全隔离
- **灵活定价**：支持多种定价模式（固定费用、按量计费、阶梯定价）
- **自动化计费**：自动计算费用、生成账单、处理续费
- **可扩展性**：支持新功能的快速接入和定价配置
- **数据一致性**：通过价格快照和事务处理保证数据一致性

### 1.2 核心特性

- 多维度订阅管理（试用、付费、企业级）
- 灵活的功能配额控制（按订阅/按席位）
- 多种计费周期支持（月度/年度/按量）
- 完整的账单和支付流程
- 实时使用量监控和告警
- 租户专属定价策略

## 2. 架构设计

### 2.1 系统架构

```mermaid
graph TB
    subgraph "表现层"
        A1[计费管理API]
        A2[订阅管理API]
        A3[账单管理API]
    end
    
    subgraph "业务逻辑层"
        B1[套餐管理服务]
        B2[订阅管理服务]
        B3[使用量统计服务]
        B4[计费引擎服务]
        B5[账单管理服务]
        B6[支付处理服务]
    end
    
    subgraph "数据访问层"
        C1[计费数据仓库]
        C2[使用量数据仓库]
        C3[账单数据仓库]
    end
    
    subgraph "基础设施层"
        D1[MySQL数据库]
        D2[Redis缓存]
        D3[定时任务调度]
        D4[消息队列]
    end
    
    subgraph "外部集成"
        E1[支付网关]
        E2[通知服务]
        E3[审计日志]
    end
    
    A1 --> B1
    A2 --> B2
    A3 --> B5
    B1 --> C1
    B2 --> C1
    B3 --> C2
    B4 --> C1
    B4 --> C2
    B5 --> C3
    B6 --> E1
    C1 --> D1
    C2 --> D1
    C3 --> D1
    B4 --> D3
    B5 --> E2
    B2 --> E3
```

### 2.2 领域模型

```mermaid
erDiagram
    BillFeature ||--o{ BillPlanFeature : "包含"
    BillPlan ||--o{ BillPlanFeature : "配置"
    BillPlan ||--o{ BillSubscription : "订阅"
    BillPlan ||--o{ BillPrice : "定价"
    BillFeature ||--o{ BillPrice : "定价"
    BillSubscription ||--o{ BillUsage : "使用"
    BillSubscription ||--o{ BillInvoice : "计费"
    BillInvoice ||--o{ BillInvoiceItem : "明细"
    BillInvoice ||--o{ BillPayment : "支付"
    BillSubscription ||--o{ BillTrialRecord : "试用"
    
    BillFeature {
        bigint id
        string code
        string name
        enum type
        string unit
        enum reset_period
        enum value_scope
    }
    
    BillPlan {
        bigint id
        string code
        string name
        string version
        enum plan_type
        enum billing_mode
        boolean trial_enabled
        int trial_days
    }
    
    BillSubscription {
        bigint id
        string sub_no
        bigint tenant_id
        bigint plan_id
        json pricing_snapshot
        int quantity
        datetime start_time
        datetime end_time
        enum status
    }
    
    BillUsage {
        bigint id
        bigint tenant_id
        bigint subscription_id
        bigint feature_id
        decimal usage_amount
        date billing_period
    }
    
    BillInvoice {
        bigint id
        string invoice_no
        bigint tenant_id
        decimal total_amount
        enum status
        datetime due_date
    }
```

## 3. 核心功能设计

### 3.1 功能模块设计

#### 功能定义表 (bill_feature)

```mermaid
classDiagram
    class BillFeature {
        +Long id
        +String code "功能代码"
        +String name "功能名称"
        +FeatureType type "功能类型"
        +String unit "计量单位"
        +ResetPeriod resetPeriod "重置周期"
        +ValueScope valueScope "功能范围"
        +FeatureStatus status "状态"
    }
    
    class FeatureType {
        <<enumeration>>
        QUOTA "配额限制型"
        USAGE "使用量计费型"
        SWITCH "开关功能型"
    }
    
    class ResetPeriod {
        <<enumeration>>
        DAY "日重置"
        MONTH "月重置" 
        YEAR "年重置"
        NEVER "不重置"
    }
    
    class ValueScope {
        <<enumeration>>
        PER_SUBSCRIPTION "按订阅"
        PER_SEAT "按席位"
    }
```

**功能类型说明：**
- **QUOTA（配额限制型）**：固定使用上限，包含在套餐费用中（如存储空间100GB）
- **USAGE（使用量计费型）**：按实际使用量计费，可设免费额度（如API调用）
- **SWITCH（开关功能型）**：功能开关，通常按周期收费（如高级分析功能）

#### 套餐管理设计

```mermaid
classDiagram
    class BillPlan {
        +Long id
        +String code "套餐代码"
        +String name "套餐名称" 
        +String version "版本号"
        +PlanType planType "套餐类型"
        +BillingMode billingMode "计费模式"
        +Boolean trialEnabled "支持试用"
        +Integer trialDays "试用天数"
        +PlanStatus status "状态"
    }
    
    class BillPlanFeature {
        +Long planId
        +Long featureId
        +String featureValue "功能配置值"
        +FeatureStatus status
    }
    
    class PlanType {
        <<enumeration>>
        FREE "免费版"
        BASIC "基础版"
        PRO "专业版"
        ENTERPRISE "企业版"
    }
    
    class BillingMode {
        <<enumeration>>
        PREPAID "预付费"
        POSTPAID "后付费"
        HYBRID "混合模式"
    }
    
    BillPlan "1" *-- "n" BillPlanFeature : "包含功能"
```

### 3.2 定价策略设计

#### 统一定价表设计

```mermaid
classDiagram
    class BillPrice {
        +Long id
        +PriceType type "定价类型"
        +TargetType targetType "目标类型"
        +Long targetId "目标ID"
        +Long tenantId "租户ID"
        +BigDecimal price "价格"
        +String currency "货币"
        +BillingCycle billingCycle "计费周期"
        +JSON pricingConfig "定价配置"
        +LocalDateTime effectiveTime
        +LocalDateTime expireTime
    }
    
    class PriceType {
        <<enumeration>>
        PLAN "标准套餐定价"
        FEATURE "标准功能定价"
        TENANT_PLAN "租户专属套餐定价"
        TENANT_FEATURE "租户专属功能定价"
    }
    
    class BillingCycle {
        <<enumeration>>
        MONTHLY "月度计费"
        YEARLY "年度计费"
        USAGE "使用量计费"
    }
```

**定价配置JSON结构：**

```json
{
  "type": "quota|tiered|usage|package|tiered_fixed",
  "values": [
    {
      "min": 0,
      "max": 1000,
      "quantity": 10000,
      "price": 0.001
    }
  ]
}
```

### 3.3 订阅管理设计

#### 订阅生命周期

```mermaid
stateDiagram-v2
    [*] --> TRIAL : 开始试用
    [*] --> ACTIVE : 直接订阅
    TRIAL --> ACTIVE : 试用转正
    TRIAL --> EXPIRED : 试用过期
    ACTIVE --> PAST_DUE : 付费失败
    ACTIVE --> SUSPENDED : 主动暂停
    ACTIVE --> CANCELLED : 用户取消
    PAST_DUE --> ACTIVE : 付费成功
    PAST_DUE --> SUSPENDED : 逾期暂停
    SUSPENDED --> ACTIVE : 恢复服务
    CANCELLED --> EXPIRED : 周期结束
    SUSPENDED --> EXPIRED : 永久停用
    EXPIRED --> [*]
```

## 4. 计费引擎设计

### 4.1 计费处理流程

```mermaid
flowchart TD
    A[定时任务触发] --> B[扫描到期订阅]
    B --> C[加载订阅信息]
    C --> D[获取价格快照]
    D --> E[计算固定费用]
    E --> F[统计使用量]
    F --> G[计算超量费用]
    G --> H[应用折扣规则]
    H --> I[生成账单]
    I --> J[发送账单通知]
    J --> K[处理自动支付]
    K --> L{支付成功?}
    L -->|是| M[更新订阅状态]
    L -->|否| N[标记逾期]
    M --> O[发送确认通知]
    N --> P[发送逾期通知]
```

### 4.2 计费引擎核心组件

```mermaid
classDiagram
    class BillingEngine {
        +InvoiceResult processSubscriptionBilling(subscriptionId)
        +InvoiceResult processUsageBilling(subscriptionId, period)
        +InvoiceResult processAdjustment(subscriptionId, adjustmentType)
        +InvoiceResult processRefund(subscriptionId, refundAmount)
    }
    
    class PricingCalculator {
        +BigDecimal calculatePlanPrice(planId, quantity, period)
        +BigDecimal calculateUsagePrice(featureId, usageAmount, pricingConfig)
        +BigDecimal calculateTieredPrice(usageAmount, tiers)
        +BigDecimal applyDiscount(amount, discountRules)
    }
    
    class QuotaManager {
        +QuotaStatus checkQuotaLimit(subscriptionId, featureId)
        +void resetQuota(subscriptionId, featureId, resetPeriod)
        +BigDecimal getRemainingQuota(subscriptionId, featureId)
        +void updateQuotaUsage(subscriptionId, featureId, amount)
    }
    
    BillingEngine --> PricingCalculator
    BillingEngine --> QuotaManager
```

## 5. 数据库设计

### 5.1 核心表结构

```sql
-- 功能表
CREATE TABLE `bill_feature` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID，0表示系统级',
  `code` VARCHAR(50) NOT NULL COMMENT '功能代码',
  `name` VARCHAR(100) NOT NULL COMMENT '功能名称',
  `description` TEXT COMMENT '功能描述',
  `type` ENUM('QUOTA','USAGE','SWITCH') NOT NULL COMMENT '功能类型',
  `unit` VARCHAR(20) COMMENT '计量单位',
  `reset_period` ENUM('DAY','MONTH','YEAR','NEVER') NOT NULL DEFAULT 'MONTH',
  `value_scope` ENUM('PER_SUBSCRIPTION','PER_SEAT') DEFAULT 'PER_SUBSCRIPTION',
  `status` ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` VARCHAR(100),
  `updated_by` VARCHAR(100),
  
  UNIQUE KEY `uk_tenant_code` (`tenant_id`, `code`),
  KEY `idx_type_status` (`type`, `status`),
  KEY `idx_reset_period` (`reset_period`, `type`)
) COMMENT='功能表';

-- 套餐表
CREATE TABLE `bill_plan` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID，0表示系统级',
  `code` VARCHAR(50) NOT NULL COMMENT '套餐代码',
  `name` VARCHAR(100) NOT NULL COMMENT '套餐名称',
  `version` VARCHAR(20) NOT NULL DEFAULT 'v1.0' COMMENT '套餐版本号',
  `description` TEXT COMMENT '套餐描述',
  `plan_type` ENUM('FREE','BASIC','PRO','ENTERPRISE') NOT NULL COMMENT '套餐类型',
  `billing_mode` ENUM('PREPAID','POSTPAID','HYBRID') NOT NULL COMMENT '计费模式',
  `trial_enabled` TINYINT(1) DEFAULT 0 COMMENT '是否支持试用',
  `trial_days` INT DEFAULT 0 COMMENT '试用天数',
  `trial_limit_per_user` INT DEFAULT 1 COMMENT '每用户试用次数限制',
  `status` ENUM('DRAFT','ACTIVE','INACTIVE','DEPRECATED','ARCHIVED') DEFAULT 'DRAFT',
  `effective_time` DATETIME NOT NULL COMMENT '生效时间',
  `expire_time` DATETIME COMMENT '失效时间',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` VARCHAR(100),
  `updated_by` VARCHAR(100),
  
  UNIQUE KEY `uk_tenant_plan_code_version` (`tenant_id`, `code`, `version`),
  KEY `idx_plan_type_status` (`plan_type`, `status`),
  KEY `idx_trial_enabled` (`trial_enabled`, `status`)
) COMMENT='套餐表';

-- 订阅表
CREATE TABLE `bill_subscription` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `sub_no` VARCHAR(50) NOT NULL COMMENT '订阅编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `plan_id` BIGINT NOT NULL COMMENT '套餐ID',
  `remark` VARCHAR(512) COMMENT '备注信息',
  `pricing_snapshot` JSON NOT NULL COMMENT '价格快照',
  `quantity` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '订阅席位数量',
  `start_time` DATETIME NOT NULL COMMENT '订阅开始时间',
  `end_time` DATETIME COMMENT '订阅结束时间',
  `current_period_start_time` DATETIME NOT NULL COMMENT '当前计费周期开始时间',
  `current_period_end_time` DATETIME NOT NULL COMMENT '当前计费周期结束时间',
  `next_billing_time` DATETIME COMMENT '下次计费时间',
  `status` ENUM('TRIAL','ACTIVE','PAST_DUE','SUSPENDED','CANCELLED','EXPIRED') DEFAULT 'ACTIVE',
  `auto_renew` TINYINT(1) DEFAULT 1 COMMENT '是否自动续费',
  `cancel_at_period_end` TINYINT(1) DEFAULT 0 COMMENT '是否在周期结束时取消',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` VARCHAR(100),
  `updated_by` VARCHAR(100),
    
  UNIQUE KEY `uk_sub_no` (`sub_no`),
  KEY `idx_tenant_status_period` (`tenant_id`, `status`, `current_period_end_time`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_next_billing` (`next_billing_time`, `auto_renew`),
  FOREIGN KEY (`plan_id`) REFERENCES `bill_plan`(`id`)
) COMMENT='订阅记录表';

-- 使用量记录表
CREATE TABLE `bill_usage` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `subscription_id` BIGINT NOT NULL COMMENT '订阅ID',
  `feature_id` BIGINT NOT NULL COMMENT '功能ID',
  `usage_time` DATETIME NOT NULL COMMENT '使用时间',
  `usage_amount` DECIMAL(18,4) NOT NULL COMMENT '使用量',
  `unit` VARCHAR(20) COMMENT '计量单位',
  `billing_period` DATE NOT NULL COMMENT '计费周期',
  `metadata` JSON COMMENT '使用量元数据',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  KEY `idx_tenant_subscription` (`tenant_id`, `subscription_id`),
  KEY `idx_feature_billing_period` (`feature_id`, `billing_period`),
  KEY `idx_usage_time` (`usage_time`),
  KEY `idx_billing_period` (`billing_period`),
  KEY `idx_subscription_feature_period` (`subscription_id`, `feature_id`, `billing_period`),
  FOREIGN KEY (`subscription_id`) REFERENCES `bill_subscription`(`id`),
  FOREIGN KEY (`feature_id`) REFERENCES `bill_feature`(`id`)
) COMMENT='使用量记录表';
```

## 6. 多租户集成

### 6.1 租户数据隔离

```mermaid
classDiagram
    class BaseTenantEntity {
        <<abstract>>
        +Long tenantId "租户ID"
        +LocalDateTime createdTime
        +LocalDateTime updatedTime
        +String createdBy
        +String updatedBy
    }
    
    class BillSubscription {
        +Long tenantId
    }
    
    class BillUsage {
        +Long tenantId
    }
    
    class BillInvoice {
        +Long tenantId
    }
    
    BaseTenantEntity <|-- BillSubscription
    BaseTenantEntity <|-- BillUsage
    BaseTenantEntity <|-- BillInvoice
```

### 6.2 MyBatis多租户拦截器集成

```mermaid
classDiagram
    class TenantInterceptor {
        +Object intercept(Invocation invocation)
        +void setTenantId(Long tenantId)
        +Long getCurrentTenantId()
    }
    
    class BillingTenantHandler {
        +void addTenantCondition(MappedStatement ms, BoundSql boundSql)
        +boolean shouldAddTenantCondition(String tableName)
        +String getTenantColumnName()
    }
    
    TenantInterceptor --> BillingTenantHandler
```

## 7. 系统集成

### 7.1 与现有模块集成

#### 审计日志集成

```mermaid
classDiagram
    class BillingAuditService {
        +void auditSubscriptionChange(subscriptionId, oldStatus, newStatus)
        +void auditPaymentResult(paymentId, result)
        +void auditPriceChange(priceId, oldPrice, newPrice)
        +void auditUsageRecord(usageId, featureId, amount)
    }
    
    class AuditLogAnnotation {
        <<annotation>>
        +String operation()
        +String category()
        +String description()
    }
```

#### 通知系统集成

```mermaid
classDiagram
    class BillingNotificationService {
        +void sendTrialStartNotification(subscriptionId)
        +void sendTrialExpiringNotification(subscriptionId)
        +void sendInvoiceNotification(invoiceId)
        +void sendPaymentSuccessNotification(paymentId)
        +void sendPaymentFailureNotification(paymentId)
        +void sendQuotaExceededNotification(subscriptionId, featureId)
    }
    
    class NotificationTemplate {
        +String templateCode
        +String subject
        +String content
        +NotificationChannel channel
    }
```

### 7.2 定时任务调度

```mermaid
classDiagram
    class BillingScheduler {
        +void processSubscriptionBilling()
        +void processUsageBilling()
        +void checkTrialExpiration()
        +void checkQuotaLimits()
        +void processOverduePayments()
        +void aggregateUsageStatistics()
    }
    
    class SchedulerConfig {
        +String billingCron "0 0 1 * * ?"
        +String usageCron "0 0 2 1 * ?"
        +String trialCheckCron "0 0 8 * * ?"
        +String quotaCheckCron "0 */30 * * * ?"
    }
```

## 8. 性能优化

### 8.1 缓存策略

```mermaid
classDiagram
    class BillingCacheService {
        +PlanInfo getPlanCache(planId)
        +PricingInfo getPricingCache(targetType, targetId)
        +QuotaInfo getQuotaCache(subscriptionId, featureId)
        +UsageSummary getUsageSummaryCache(subscriptionId, period)
        +void invalidateCache(cacheKey)
    }
    
    class CacheKey {
        +String PLAN_PREFIX "billing:plan:"
        +String PRICING_PREFIX "billing:pricing:"
        +String QUOTA_PREFIX "billing:quota:"
        +String USAGE_PREFIX "billing:usage:"
        +int DEFAULT_TTL "3600"
    }
```

### 8.2 数据库优化

#### 索引优化策略

```sql
-- 订阅查询优化索引
CREATE INDEX idx_subscription_billing ON bill_subscription 
(tenant_id, status, next_billing_time, auto_renew);

-- 使用量聚合优化索引
CREATE INDEX idx_usage_aggregation ON bill_usage 
(subscription_id, feature_id, billing_period, usage_time);

-- 账单查询优化索引
CREATE INDEX idx_invoice_query ON bill_invoice
(tenant_id, status, due_date, billing_period_start);
```

#### 分区策略

```sql
-- 使用量表按月分区
CREATE TABLE `bill_usage_partitioned` (
  -- 表结构同 bill_usage
) PARTITION BY RANGE (TO_DAYS(billing_period)) (
  PARTITION p202401 VALUES LESS THAN (TO_DAYS('2024-02-01')),
  PARTITION p202402 VALUES LESS THAN (TO_DAYS('2024-03-01')),
  PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

## 9. 安全设计

### 9.1 数据安全

| 安全措施 | 实现方式 | 说明 |
|---------|----------|------|
| 数据加密 | 利用rose-crypto模块 | 敏感数据字段加密存储 |
| 访问控制 | 租户级别隔离 | 确保租户间数据完全隔离 |
| 审计日志 | 集成rose-audit模块 | 记录所有关键操作 |
| API安全 | JWT认证 + 权限控制 | 多层安全验证 |

### 9.2 支付安全

```mermaid
flowchart TD
    A[支付请求] --> B[参数验证]
    B --> C[金额验证]
    C --> D[用户身份验证]
    D --> E[风险评估]
    E --> F[调用支付网关]
    F --> G[签名验证]
    G --> H[结果处理]
    H --> I[状态更新]
    I --> J[通知发送]
```

## 10. 监控与告警

### 10.1 业务指标监控

| 指标类型 | 监控内容 | 告警阈值 |
|---------|----------|----------|
| 订阅指标 | 新增订阅数、活跃订阅数、流失率 | 流失率 > 10% |
| 计费指标 | 计费成功率、计费延迟 | 成功率 < 95% |
| 支付指标 | 支付成功率、支付延迟 | 成功率 < 90% |
| 使用量指标 | 配额使用率、超量比例 | 使用率 > 90% |

### 10.2 告警规则

```yaml
alerts:
  - name: billing_failure_rate
    condition: billing_error_rate > 0.05
    severity: high
    actions:
      - email: admin@company.com
      - webhook: /api/alerts/billing
      
  - name: quota_exceeded
    condition: quota_utilization > 0.9
    severity: medium
    actions:
      - notification: tenant_admin
      - auto_scale: increase_quota
```

## 11. 扩展规划

### 11.1 国际化支持

- **多币种支持**：集成汇率服务，支持动态汇率转换
- **本地化定价**：根据地区制定差异化定价策略
- **税务计算**：支持不同国家和地区的税务规则

### 11.2 高级功能

- **智能定价**：基于使用模式和市场条件的动态定价
- **预测分析**：用户使用量预测和成本预估
- **自定义计费规则**：支持租户级别的个性化计费规则
- **API限流集成**：与API网关集成实现基于配额的限流
- **成本优化建议**：为用户提供成本优化建议

### 11.3 技术演进

- **微服务拆分**：随着业务复杂度增长，考虑拆分为独立的微服务
- **事件驱动架构**：引入事件溯源和CQRS模式
- **机器学习集成**：利用ML技术优化定价策略和预测分析
- **实时计费**：支持实时计费和即时扣费