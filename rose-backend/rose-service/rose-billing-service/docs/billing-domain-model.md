# 计费领域模型设计方案

本文档描述 rose-billing-service 的领域边界、聚合、实体/值对象、状态机、领域服务与事件，作为当前实现与后续演进的统一参考。

## 1. 领域总览
- 目标：为多租户产品提供订阅与用量计费、开票、收款入账、通知与基础报表能力，保障幂等一致、可审计可追溯。
- 有界上下文（Bounded Contexts）
  - Catalog：套餐与定价（SubscriptionPlan）
  - Subscription：订阅生命周期与计费周期（TenantSubscription）
  - Billing & Invoicing：计费与开票（Invoice、UsageRecord）
  - Payments：收款与入账（PaymentRecord + PaymentProcessors）
  - Notifications（外部）：模板与多通道发送
  - Reporting：收入与用量统计（未来可演进至 OLAP）

## 2. 聚合与核心模型

```mermaid
classDiagram
  class SubscriptionPlan {
    +id: String
    +tenantId: String
    +code: String
    +name: String
    +billingType: String
    +basePrice: BigDecimal
    +billingCycle: int
    +enabled: Boolean
    +trialDays: int
    +effectiveTime: DateTime
    +expiryTime: DateTime
    +createTime, updateTime
  }
  class TenantSubscription {
    +id: String
    +tenantId: String
    +planId: String
    +status: SubscriptionStatus
    +startTime: DateTime
    +endTime: DateTime
    +nextBillingTime: DateTime
    +trialEndTime: DateTime
    +inTrial: Boolean
    +autoRenew: Boolean
    +currentPeriodAmount: BigDecimal
    +cancelledTime: DateTime
    +pausedTime: DateTime
    +createTime, updateTime
  }
  class Invoice {
    +id: String
    +tenantId: String
    +invoiceNumber: String
    +subscriptionId: String
    +status: InvoiceStatus
    +periodStart: Date
    +periodEnd: Date
    +baseAmount: BigDecimal
    +usageAmount: BigDecimal
    +discountAmount: BigDecimal
    +taxAmount: BigDecimal
    +totalAmount: BigDecimal
    +dueDate: Date
    +paidTime: DateTime
    +paymentMethod: String
    +paymentTransactionId: String
    +notes: String
    +createTime, updateTime
  }
  class UsageRecord {
    +id: String
    +tenantId: String
    +metricType: String
    +quantity: BigDecimal
    +unit: String
    +recordTime: DateTime
    +resourceId: String
    +resourceType: String
    +metadata: String
    +billed: Boolean
    +billedTime: DateTime
    +invoiceId: String?
    +createTime, updateTime
  }
  class PaymentRecord {
    +id: String
    +tenantId: String
    +invoiceId: String
    +amount: BigDecimal
    +paymentMethod: String
    +transactionId: String
    +gatewayResponse: JSON
    +status: PaymentRecordStatus
    +channelStatus: String
    +channelAmount: BigDecimal
    +paidTime: DateTime
    +refundedTime: DateTime
    +refundReason: String
    +createTime, updateTime
  }
  class RefundRecord {
    +id: String
    +tenantId: String
    +invoiceId: String
    +paymentMethod: String
    +transactionId: String
    +refundId: String
    +idempotencyKey: String
    +refundAmount: BigDecimal
    +reason: String
    +status: RefundStatus
    +rawCallback: JSON
    +requestedTime: DateTime
    +completedTime: DateTime
    +createTime, updateTime
  }
  class PricingCalculator {
    <<domain service>>
  }
  class BillingService {
    <<application service>>
  }
  class PaymentGatewayService {
    <<acl>>
  }

  TenantSubscription --> SubscriptionPlan : references
  Invoice --> TenantSubscription : belongs-to
  UsageRecord --> TenantSubscription : belongs-to
  PaymentRecord --> Invoice : settles
  RefundRecord --> Invoice : relates

  BillingService --> Invoice
  BillingService --> UsageRecord
  BillingService --> PaymentRecord
  BillingService --> RefundRecord
  BillingService ..> PricingCalculator
  PaymentGatewayService ..> PaymentRecord
  PaymentGatewayService ..> RefundRecord
```

- 值对象（示例）：
  - Money(amount, currency)，Tax(taxRate, taxAmount)，Discount(type, value)
  - BillingCycle(periodStart, periodEnd)，UsageLimit(metricType, quota, policy)

## 3. 状态机

- 订阅（TenantSubscription.status）
```mermaid
stateDiagram-v2
  [*] --> TRIAL
  TRIAL --> ACTIVE: 到达 trialEndTime 或主动转正
  ACTIVE --> PAUSED: 暂停
  PAUSED --> ACTIVE: 恢复
  ACTIVE --> CANCELLED: 取消
  CANCELLED --> [*]
  note right of ACTIVE: 仅 ACTIVE 进入计费周期
```

- 账单（Invoice.status）
```mermaid
stateDiagram-v2
  [*] --> PENDING
  PENDING --> PAID: 入账成功
  PENDING --> OVERDUE: 逾期
  PAID --> REFUNDED: 退款全额完成
  OVERDUE --> [*]
```

- 退款记录（RefundRecord.status）
```mermaid
stateDiagram-v2
  [*] --> REQUESTED
  REQUESTED --> PROCESSING: 异步中
  PROCESSING --> SUCCESS
  PROCESSING --> FAILED
  REQUESTED --> SUCCESS: 同步成功
  REQUESTED --> FAILED: 同步失败
  SUCCESS --> [*]
  FAILED --> [*]
```

- 支付记录（PaymentRecord.status）
```mermaid
stateDiagram-v2
  [*] --> SUCCESS
  SUCCESS --> REFUNDED: 退款回调/查询确认
  SUCCESS --> FAILED: 通道失败（小概率）
  REFUNDED --> [*]
  FAILED --> [*]
```

## 4. 关键流程
### 4.1 支付成功链路
```mermaid
sequenceDiagram
  autonumber
  participant PM as Payment Method
  participant PC as PaymentController
  participant GW as PaymentGatewayService
  participant BS as BillingService
  participant IS as InvoiceService
  participant US as UsageService
  participant NS as NotificationSvc

  PM->>PC: 回调(payload)
  PC->>GW: verifyCallback(method, data)
  GW-->>PC: 验签结果/交易号/发票号
  PC->>BS: processPayment(invoiceId, method, txId)
  BS->>IS: markInvoiceAsPaid()
  BS->>US: markUsageAsBilled()
  BS->>NS: sendPaymentConfirmation()
  BS-->>PC: {code:200}
```

### 4.2 退款全量/部分退款时序
```mermaid
sequenceDiagram
  autonumber
  participant API as RefundAPI
  participant RC as RefundController
  participant RS as RefundService
  participant GW as PaymentGatewayService
  participant IR as InvoiceRepo
  participant RR as RefundRecordRepo
  participant PR as PaymentRecordRepo

  API->>RC: POST /refunds {invoiceId, amount, reason}
  RC->>RS: requestRefund(invoiceId, amount, reason, idempotencyKey?)
  RS->>IR: 获取 Invoice 并校验(状态=PAID, 交易号存在)
  RS->>RR: sumSucceededAmountByInvoiceId(invoiceId)
  RS-->>RC: 金额校验(<= 可退余额)不通过则失败
  RS->>GW: processRefund(txId, amount, reason, tenantId)
  alt 同步成功
    RS->>RR: insert RefundRecord{status=SUCCESS, refundId, refundAmount}
    RS->>IR: 若累计退款>=totalAmount 则 invoice.status=REFUNDED
  else 同步失败
    RS->>RR: insert RefundRecord{status=FAILED}
  else 异步处理中
    RS->>RR: insert RefundRecord{status=PROCESSING}
    note over RS,GW: 异步回调到达 → 触发 processRefundCallback
  end

  %% 回调路径（部分/全额）
  GW-->>RC: 回调(method, data)
  RC->>GW: verifyRefundCallback(method, data)
  alt 验签失败
    RC-->>GW: {code=INVALID_REFUND_CALLBACK}
  else 验签通过
    RC->>RS: processRefundCallback(method, data)
    RS->>RR: upsert RefundRecord{status=SUCCESS/FAILED, refundAmount, rawCallback}
    RS->>PR: 同步 PaymentRecord.channelStatus/Amount
    RS->>IR: 若累计退款(含本次成功金额)≥totalAmount → invoice.status=REFUNDED
    RS-->>RC: {code=200}
  end
```

### 4.3 支付回调幂等
```mermaid
sequenceDiagram
  autonumber
  participant CH as Channel
  participant PC as PaymentController
  participant RC as RefundController
  participant GW as PaymentGatewayService
  participant IS as InvoiceService
  participant RS as RefundService
  participant RR as RefundRecordRepo
  participant PR as PaymentRecordRepo

  %% 支付回调幂等（示意）
  CH->>PC: 支付回调{invoiceId, transactionId}
  PC->>GW: verifyPaymentCallback(method, data)
  alt 验签失败
    PC-->>CH: {code=INVALID_PAYMENT_CALLBACK}
  else 验签通过
    PC->>IS: processPayment(invoiceId, transactionId, method)
    note over IS: 已入账或交易号重复 → 忽略并返回成功
    IS-->>PC: {code=200}
  end

  %% 退款回调幂等
  CH->>RC: 退款回调{invoiceId, refundId, amount?}
  RC->>GW: verifyRefundCallback(method, data)
  alt 验签失败
    RC-->>CH: {code=INVALID_REFUND_CALLBACK}
  else 验签通过
    RC->>RS: processRefundCallback(method, data)
    RS->>RR: 按(invoiceId, refundId) upsert，存在则更新，不存在则插入
    RS-->>RC: {code=200}
  end
```

### 4.4 退款回调幂等
```mermaid
sequenceDiagram
  autonumber
  participant CH as Channel
  participant RC as RefundController
  participant RS as RefundService
  participant GW as PaymentGatewayService
  participant RR as RefundRecordRepo

  CH->>RC: 退款回调{invoiceId, refundId, amount?}
  RC->>GW: verifyRefundCallback(method, data)
  alt 验签失败
    RC-->>CH: {code=INVALID_REFUND_CALLBACK}
  else 验签通过
    RC->>RS: processRefundCallback(method, data)
    RS->>RR: 按(invoiceId, refundId) upsert（存在则更新，不存在则插入）
    RS-->>RC: {code=200}
  end

  note over RS: 若请求带 idempotencyKey，存在成功记录则直接返回成功(idempotent)
```

### 4.5 异常落库重试（回调/出账）
```mermaid
sequenceDiagram
  autonumber
  participant CH as Channel
  participant RC as CallbackController
  participant SVC as DomainService
  participant DB as Database
  participant OB as Outbox
  participant RJ as RetryJob

  CH->>RC: 回调(payload)
  RC->>SVC: handleCallback(payload)
  SVC->>DB: insert/update 业务记录
  alt DB异常/网络闪断
    SVC->>OB: 记录补偿任务(outbox/event)
    SVC-->>RC: 返回200(已受理)或特定错误码
    RJ->>OB: 轮询/订阅补偿任务
    RJ->>DB: 重试插入/更新(带幂等键/唯一约束)
    RJ-->>OB: 成功后标记完成
  else 正常
    SVC-->>RC: 返回200
  end

  note over SVC, RJ: 建议将回调原文(raw JSON)落盘，失败可重放；关键写入使用幂等键/唯一约束
```


### 4.5 日对账流程
```mermaid
sequenceDiagram
  participant CH as Channel API
  participant RC as ReconciliationJob
  participant PR as PaymentRecordRepo
  participant IR as InvoiceRepo
  participant DF as DiffRepo

  RC->>CH: 拉取日流水(日期、分页)
  CH-->>RC: 回执列表(JSON)
  RC->>PR: 查询本地 PaymentRecord 匹配(交易号/方法)
  RC->>IR: 查询 Invoice 核对金额/状态
  RC->>DF: 记录差错(缺失/金额/状态)
  RC-->>RC: 生成对账报告
```

### 4.6 退款时序
```mermaid
sequenceDiagram
  participant API as RefundAPI
  participant RF as RefundService
  participant GW as PaymentGatewayService
  participant CH as Channel
  participant PR as PaymentRecordRepo
  participant IR as InvoiceRepo

  API->>RF: requestRefund(invoiceId, amount, reason)
  RF->>IR: 校验发票已支付/金额足够
  RF->>GW: 调用渠道退款
  GW-->>CH: refund(invoiceId, txId, amount)
  CH-->>GW: 回执/异步回调
  RF->>PR: 记录 RefundRecord & 更新 PaymentRecord 状态
  RF-->>API: 受理/结果
```

## 5. 风险与演进
- 幂等与一致性：引入 PaymentRecord 唯一约束、Outbox + MQ 外发、失败重试与死信
- 对账与退款：日对账作业、差错表、退款/撤销流程与状态机
- 订阅复杂度：proration、试用/延期、超额与最低承诺、税务与多币种
- 可观测性：指标（成功率/延迟/掉单）、Tracing、SLO 报表
- 存储与查询：避免函数导致索引失效（派生列/物化视图/OLAP）