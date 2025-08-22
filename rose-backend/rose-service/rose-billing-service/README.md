# Rose 计费系统 (Rose Billing System)

## 概述

Rose 计费系统是一个企业级的多租户SaaS计费解决方案，基于Spring Boot 3.x和MyBatis Plus构建。系统支持多种计费模式、套餐管理、用量统计、账单生成等核心功能。

## 核心功能

### 1. 套餐管理 (Plan Management)
- ✅ 套餐的创建、更新、版本管理
- ✅ 功能配置和定价策略
- ✅ 试用配置和时间管理
- ✅ 套餐状态管理（激活、禁用、弃用、归档）

### 2. 订阅管理 (Subscription Management)
- ✅ 订阅的创建和生命周期管理
- ✅ 试用转正和续费处理
- ✅ 订阅状态转换和优雅取消
- ✅ 自动续费和到期处理

### 3. 用量统计 (Usage Tracking)
- ✅ 实时用量记录和统计
- ✅ 多维度用量分析和报表
- ✅ 配额管理和用量告警
- ✅ API调用和存储用量的专用记录方法

### 4. 账单管理 (Invoice Management)
- ✅ 自动账单生成和发布
- ✅ 支付记录和状态管理
- ✅ 逾期处理和退款功能
- ✅ 批量账单生成和处理

### 5. 定时任务 (Scheduled Tasks)
- ✅ 过期订阅自动处理
- ✅ 逾期账单自动标记
- ✅ 月度账单自动生成
- ✅ 历史数据清理（可配置）

## 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Layer     │    │  Service Layer  │    │   Data Layer    │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ PlanController  │───▶│ PlanService     │───▶│ PlanMapper      │
│SubscriptionCtrl│───▶│SubscriptionSvc │───▶│SubscriptionMpr │
│ UsageController │───▶│ UsageService    │───▶│ UsageMapper     │
│ InvoiceCtrl     │───▶│ InvoiceService  │───▶│ InvoiceMapper   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │              ┌─────────────────┐              │
         └─────────────▶│ ScheduledTasks  │◀─────────────┘
                        └─────────────────┘
```

## 数据模型

### 核心实体关系
```
BillPlan (套餐)
    ↓ 1:N
BillSubscription (订阅)
    ↓ 1:N
BillUsage (用量记录)
    ↓ N:1
BillInvoice (账单)
```

### 主要状态机

**订阅状态流转**:
```
TRIAL → ACTIVE → PAST_DUE → SUSPENDED
  ↓       ↓         ↓          ↓
EXPIRED ← CANCELLED ←─────────────┘
```

**账单状态流转**:
```
DRAFT → OPEN → PAID
  ↓       ↓      ↓
VOID   OVERDUE  REFUNDED
         ↓
    PARTIAL_PAID → PAID
```

## API 接口

### 1. 套餐管理 API
```bash
# 创建套餐
POST /api/v1/billing/plans

# 查询可用套餐
GET /api/v1/billing/plans/available

# 激活套餐
POST /api/v1/billing/plans/{id}/activate
```

### 2. 订阅管理 API
```bash
# 创建订阅
POST /api/v1/billing/subscriptions

# 试用转正
POST /api/v1/billing/subscriptions/{id}/convert-trial

# 续费订阅
POST /api/v1/billing/subscriptions/{id}/renew
```

### 3. 用量统计 API
```bash
# 记录用量
POST /api/v1/billing/usage

# 查询当月用量
GET /api/v1/billing/usage/subscription/{id}/current-month

# 检查配额
GET /api/v1/billing/usage/subscription/{id}/feature/{id}/quota-check
```

### 4. 账单管理 API
```bash
# 生成账单
POST /api/v1/billing/invoices/generate/{subscriptionId}

# 记录支付
POST /api/v1/billing/invoices/{id}/payment

# 查询逾期账单
GET /api/v1/billing/invoices/overdue
```

## 快速开始

### 1. 数据库初始化
```sql
-- 执行数据库脚本
source src/main/resources/sql/billing-schema.sql
```

### 2. 配置依赖
确保项目中包含以下依赖：
- Spring Boot 3.x
- MyBatis Plus 3.5.12+
- MySQL 8.0+
- Redis (用于缓存)

### 3. 基本使用示例

#### 创建套餐
```java
@Autowired
private BillPlanService planService;

BillPlan plan = new BillPlan();
plan.setCode("BASIC_PLAN");
plan.setName("基础版");
plan.setVersion("1.0");
plan.setPlanType(PlanType.PAID);
plan.setBillingMode(BillingMode.MONTHLY);
plan.setTrialEnabled(true);
plan.setTrialDays(7);

planService.createPlan(plan);
```

#### 创建订阅
```java
@Autowired
private BillSubscriptionService subscriptionService;

BillSubscription subscription = new BillSubscription();
subscription.setPlanId(planId);
subscription.setQuantity(1);
subscription.setTenantId("tenant001");

subscriptionService.createSubscription(subscription);
```

#### 记录用量
```java
@Autowired
private BillUsageService usageService;

// 记录API调用
usageService.recordApiUsage("tenant001", subscriptionId, featureId, 
    "/api/users", "GET", 200);

// 记录存储用量
usageService.recordStorageUsage("tenant001", subscriptionId, featureId, 
    BigDecimal.valueOf(1048576), "image");
```

#### 生成账单
```java
@Autowired
private BillInvoiceService invoiceService;

LocalDate periodStart = LocalDate.of(2024, 12, 1);
LocalDate periodEnd = LocalDate.of(2024, 12, 31);

BillInvoice invoice = invoiceService.generateBillForSubscription(
    subscriptionId, periodStart, periodEnd);
```

## 配置说明

### 1. 定时任务配置
```java
@Configuration
@EnableScheduling
public class BillingConfiguration {
    // 定时任务已自动配置，可根据需要调整执行时间
}
```

### 2. 多租户配置
系统基于Rose MyBatis多租户插件，自动处理tenant_id的数据隔离。

### 3. 缓存配置
建议配置Redis用于用量数据的实时缓存，提升查询性能。

## 扩展开发

### 1. 自定义计费规则
可以扩展`BillInvoiceService.calculateBillAmount()`方法实现自定义计费逻辑。

### 2. 支付集成
可以扩展`BillInvoiceService.recordPayment()`方法集成第三方支付网关。

### 3. 通知系统
可以集成Rose通知系统，在账单到期、订阅过期时发送提醒。

## 监控和运维

### 1. 关键指标监控
- 活跃订阅数量
- 账单生成成功率
- 支付成功率
- 用量统计准确性

### 2. 定时任务监控
- 过期订阅处理结果
- 账单生成任务状态
- 历史数据清理情况

### 3. 数据备份
建议定期备份以下关键数据：
- 订阅信息
- 账单记录
- 用量统计数据

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目使用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 项目维护者: Rose Team
- 邮箱: team@rosestack.io
- 项目地址: https://github.com/chensoul/rose-playground

---

**注意**: 这是一个企业级计费系统，在生产环境使用前请进行充分的测试和性能调优。