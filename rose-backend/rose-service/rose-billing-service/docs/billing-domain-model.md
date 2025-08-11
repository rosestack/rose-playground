## 1. 领域模型

本章节梳理计费域的核心聚合、关键字段与关系。模型以“订阅 → 计费/开票 → 支付 → 入账（Posting）→ 退款”的主链路为中心，兼顾回调与对账。

```mermaid
classDiagram
  class SubscriptionPlan {
    +id: String
    +tenantId: String
    +code: String
    +name: String
    +billingType: BillingType
    +basePrice: BigDecimal
    +billingCycle: Integer
    +trialDays: Integer
    +features: JSON
    +usagePricing: JSON
  }
  class TenantSubscription {
    +id: String
    +tenantId: String
    +planId: String
    +status: SubscriptionStatus
    +startTime: DateTime
    +endTime: DateTime?
    +nextBillingTime: DateTime
    +inTrial: Boolean
    +currentPeriodAmount: BigDecimal
  }
  class Invoice {
    +id: String
    +tenantId: String
    +invoiceNumber: String
    +subscriptionId: String
    +periodStart: Date
    +periodEnd: Date
    +status: InvoiceStatus
    +baseAmount: BigDecimal
    +usageAmount: BigDecimal
    +discountAmount: BigDecimal
    +taxAmount: BigDecimal
    +totalAmount: BigDecimal
    +currency: String
    +priceSnapshot: JSON   %% 定价/税率/币种快照
    +dueDate: Date
    +paidTime: DateTime?
    +paymentMethod: String?
    +paymentTransactionId: String?
  }
  class UsageRecord {
    +id: String
    +tenantId: String
    +subscriptionId: String?
    +metricType: String
    +quantity: BigDecimal
    +unit: String
    +recordTime: DateTime
    +resourceId: String?
    +resourceType: String?
    +metadata: String?
    +billed: Boolean
    +billedTime: DateTime?
    +invoiceId: String?
  }
  class PaymentRecord {
    +id: String
    +tenantId: String
    +invoiceId: String
    +amount: BigDecimal
    +currency: String
    +paymentMethod: String
    +transactionId: String
    +status: PaymentRecordStatus
    +gatewayResponse: JSON
    +channelStatus: String
    +channelAmount: BigDecimal
    +posted: Boolean
    +postedTime: DateTime?
    +paidTime: DateTime?
  }
  class RefundRecord {
    +id: String
    +tenantId: String
    +invoiceId: String
    +paymentMethod: String
    +transactionId: String
    +refundId: String?
    +currency: String
    +refundAmount: BigDecimal
    +status: RefundStatus
    +idempotencyKey: String?
    +rawCallback: JSON/Text
    +requestedTime: DateTime
    +completedTime: DateTime?
  }
  class OutboxRecord {
    +id: String
    +tenantId: String
    +eventType: String
    +aggregateId: String
    +payload: JSON
    +status: PENDING/SENT/FAILED
    +retryCount: int
    +nextRetryAt: DateTime?
  }

  TenantSubscription --> SubscriptionPlan
  Invoice --> TenantSubscription
  UsageRecord --> Invoice
  PaymentRecord --> Invoice
  RefundRecord --> Invoice
```

## 2. 状态机

- 订阅（SubscriptionStatus）
```mermaid
stateDiagram-v2
  [*] --> TRIAL
  TRIAL --> ACTIVE: 试用到期/转付费
  ACTIVE --> PAUSED | CANCELLED | EXPIRED | PENDING_PAYMENT
  PENDING_PAYMENT --> ACTIVE: 支付成功
  PAUSED --> ACTIVE: 恢复
```

- 账单（InvoiceStatus）
```mermaid
stateDiagram-v2
  [*] --> DRAFT
  DRAFT --> PENDING: 出具账单
  PENDING --> PAID: 支付成功
  PENDING --> OVERDUE: 逾期
  PAID --> REFUNDED: 全额退款
  OVERDUE --> CANCELLED: 关单
```

- 支付记录（PaymentRecordStatus）
```mermaid
stateDiagram-v2
  [*] --> PENDING
  PENDING --> SUCCESS: 网关确认/业务落账
  PENDING --> FAILED: 失败
  SUCCESS --> REFUNDED: 全额退款完成
```

- 退款记录（RefundStatus）
```mermaid
stateDiagram-v2
  [*] --> REQUESTED
  REQUESTED --> PROCESSING: 异步中
  REQUESTED --> SUCCESS
  REQUESTED --> FAILED
  PROCESSING --> SUCCESS | FAILED
```

## 3. 业务流程

#### 3.1 订阅 → 计费/开票

- 订阅
  1) 创建订阅（可含试用）→ 设置 nextBillingTime
  2) 变更计划：更新 planId 与 currentPeriodAmount（按策略可做按比例计费）

- 计费/开票
  1) 到期批量生成账单：periodStart/End、base/usage/discount/tax/total
  2) 写入 currency（来自租户/计划配置）与 priceSnapshot（定价/税率/币种快照）
  3) 标记区间内 UsageRecord 为 billed，关联 invoiceId

```mermaid
sequenceDiagram
  autonumber
  participant TS as TenantSubscription
  participant BS as BillingService
  participant IR as InvoiceRepository
  participant UR as UsageRecordRepository

  TS->>BS: 到期触发生成账单
  BS->>UR: 聚合区间使用量（按 tenant/subscription）
  BS->>BS: 计算 base/usage/discount/tax/total
  BS->>IR: insert Invoice{currency, priceSnapshot}
  BS-->>TS: 返回 Invoice 概要
```

#### 3.2 支付

  1) 回调/主动确认 → 幂等校验(transactionId 唯一)
  2) 新建 PaymentRecord=PENDING → 成功后更新为 SUCCESS 并写 paidTime
  3) Invoice 置为 PAID，记录 paymentMethod/transactionId
  4) Outbox 写入 PaymentSucceeded（与业务同事务）


```mermaid
sequenceDiagram
  autonumber
  participant PC as PaymentController
  participant IS as InvoiceService
  participant PR as PaymentRecordRepository
  participant IR as InvoiceRepository
  participant OB as OutboxService

  PC->>IS: processPayment(invoiceId, method, txId)
  IS->>PR: insert PaymentRecord{PENDING}
  IS->>PR: update PaymentRecord{SUCCESS, paidTime}
  IS->>IR: update Invoice{PAID, paidTime, method, txId}
  alt outbox 启用
    IS->>OB: saveEvent PaymentSucceeded
  end
  IS-->>PC: {code=200}
```

#### 3.2.1 支付回调幂等（建议）
```mermaid
sequenceDiagram
  autonumber
  participant CH as Channel
  participant PC as PaymentController
  participant GW as PaymentGatewayService
  participant IS as InvoiceService
  participant PR as PaymentRecordRepository

  CH->>PC: 支付回调(payload)
  PC->>GW: verifyCallback(method, payload)
  alt 验签失败
    PC-->>CH: {code=INVALID_SIGNATURE}
  else 验签通过
    IS->>PR: 按 transactionId 幂等查询
    alt 已存在
      IS-->>PC: {code=200}
    else 不存在
      IS->>PR: insert PaymentRecord{PENDING}
      IS->>PR: update PaymentRecord{SUCCESS, paidTime}
      IS-->>PC: {code=200}
    end
  end
```

#### 3.3 入账（Posting）

  1) 对账/结算作业扫描 SUCCESS 但未 posted 的 PaymentRecord
  2) 记总账成功 → posted=true, postedTime=now（乐观锁防并发）

```mermaid
sequenceDiagram
  autonumber
  participant RJ as OutboxRelayJob
  participant PR as PaymentRecordRepo

  RJ->>PR: 查询 SUCCESS 且 posted=false 的记录
  RJ->>PR: update posted=true, postedTime=now (乐观锁)
  RJ-->>RJ: 记录批次与差错
```

#### 3.4 退款

  1) requestRefund(invoiceId, amount, reason, idempotencyKey?) 幂等：传入幂等键已成功则直接返回
  2) 校验剩余可退余额（累计 SUCCESS 的 RefundRecord）
  3) 网关同步成功 → 插入 RefundRecord{SUCCESS, refundId} 并外发 RefundSucceeded
  4) 网关异步 → 回调 processRefundCallback：按 (invoiceId, refundId) upsert，更新金额/状态/原文
  5) 部分退款口径：Invoice 仍为 PAID；累计成功退款金额 ≥ totalAmount 时置为 REFUNDED 并外发 InvoiceRefunded

```mermaid
sequenceDiagram
  autonumber
  participant API as RefundAPI
  participant RS as RefundService
  participant RR as RefundRecordRepo
  participant IR as InvoiceRepository
  participant OB as OutboxService

  API->>RS: requestRefund(invoiceId, amount, reason, idemKey?)
  RS->>RR: 幂等查询/累计金额校验
  RS->>RR: insert RefundRecord{SUCCESS/FAILED}
  alt success
    RS->>OB: saveEvent RefundSucceeded
  end
  RS-->>API: 返回结果
  Note over RS: 回调到达 → processRefundCallback
  RS->>RR: upsert RefundRecord by (invoiceId, refundId)
  RS->>IR: 若累计成功退款金额 >= total → Invoice=REFUNDED
  RS->>OB: saveEvent InvoiceRefunded
```

#### 3.5 对账

  1) 维度：按交易日、渠道分页/分租户并发
  2) 同步 PaymentRecord.channelAmount/channelStatus（保留网关原值）
  3) 差错记录：缺失/金额不符/状态不符，支持重试与人工复核
  4) 幂等落库：唯一键或 version 乐观锁

```mermaid
sequenceDiagram
  autonumber
  participant CH as Channel
  participant RC as ReconcileJob
  participant PR as PaymentRecordRepo

  RC->>CH: 拉取对账单(分渠道/分日/分页)
  RC->>PR: 对齐 channelStatus/channelAmount
  RC-->>RC: 差错落库、重试与人工复核
```