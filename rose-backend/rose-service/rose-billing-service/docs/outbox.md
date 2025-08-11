# Outbox 模式设计与实践

## 1. 概述
Outbox 模式用于解决“本地数据库写入成功，但消息发送失败（或反之）”导致的数据与事件不一致问题。核心思想：业务数据与待发送事件（Outbox 记录）在同一事务内写入数据库；随后由独立的 Relay/Publisher 进程轮询 Outbox 表，可靠地将事件投递到消息系统（或回调/Webhook）。

适用场景：
- 账单支付成功/退款成功后，需要对账单、订阅、报表等上下游做异步通知或数据同步
- 渠道回调/内部状态变更需要触发跨服务事件但不希望阻塞主业务链路
- 需要“至少一次（At-Least-Once）”语义保障，并能在消费端实现幂等

不适用场景：
- 对一致性与延迟极端敏感且无需跨边界异步的情况（可考虑强一致本地调用）

## 2. 解决的问题
- 保证“写库 + 发消息”的最终一致性
- 提供可观测与可重放能力（失败重试、死信落地）
- 与回调/重试/对账等机制配合，形成完整的可靠交付闭环

## 3. 术语
- Outbox：存放“待发送事件”的表
- Relay/Publisher：后台任务，拉取 Outbox 记录并投递到 MQ/HTTP/Webhook
- Consumer：事件消费者（可能是本系统内部服务或外部系统）

## 4. Outbox 表结构（建议最小化）
```sql
CREATE TABLE outbox (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     VARCHAR(64) NULL,
  event_type    VARCHAR(128) NOT NULL,           -- 事件类型，例如 PaymentSucceeded, RefundSucceeded
  aggregate_id  VARCHAR(64)  NOT NULL,           -- 业务聚合ID，例如 invoiceId
  payload       JSON         NOT NULL,           -- 事件体（不可变）
  status        VARCHAR(16)  NOT NULL,           -- PENDING / SENT / FAILED
  retry_count   INT          NOT NULL DEFAULT 0,  -- 已重试次数
  next_retry_time DATETIME     NULL,               -- 下次可重试时间（指数退避）
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_status_time (status, next_retry_time),
  INDEX idx_type_time (event_type, next_retry_time),
  INDEX idx_agg (aggregate_id)
);
```

说明：
- payload 建议只追加不修改，保持不可变，便于审计与重放
- 可以按租户或事件类型分片/分表（数据量大时）

## 5. 核心原理（本地事务 + 后台投递）
1) 业务事务内：写入业务表（如 invoice/payment/refund），同时插入 outbox(PENDING)
2) 事务提交成功：Outbox 记录可见，Relay 任务在短时间内拉取并投递
3) 投递成功：将 Outbox 记录置为 SENT（或删除）
4) 投递失败：记录错误并根据退避策略设置 next_retry_time，置为 FAILED/PENDING 等待重试

可靠性要点：
- 业务写入与 Outbox 写入必须在同一数据库事务内
- Relay 需要“至少一次”投递语义，并在 Consumer 端实现幂等

## 6. 与本项目的结合点
可定义这些领域事件：
- PaymentSucceededEvent(tenantId, invoiceId, transactionId, paymentMethod, amount, currency, occurredTime)
- RefundSucceededEvent(tenantId, invoiceId, refundId, amount, currency, occurredTime)
- InvoiceGeneratedEvent(tenantId, invoiceId, periodStart, periodEnd, total, currency)

触发位置：
- InvoiceService 置为 PAID 后
- RefundService 成功（同步或回调）后
- BillingService 生成账单后

## 7. 写入与投递流程（伪代码）
写入（与业务同事务）：
```pseudo
@Transactional
function markInvoicePaid(invoiceId, txId, paymentMethod) {
  // 1. 更新业务状态
  invoice.status = PAID
  invoice.paidTime = now()
  update(invoice)

  // 2. 插入支付记录（已在代码中实现 PENDING→SUCCESS 状态流）
  insert(payment_record)
  update(payment_record: SUCCESS)

  // 3. 插入 outbox
  outbox.insert({
    event_type: 'PaymentSucceeded',
    aggregate_id: invoiceId,
    payload: {
      invoiceId, txId, paymentMethod, amount: invoice.totalAmount, currency: invoice.currency, occurredTime: now()
    },
    status: 'PENDING'
  })
}
```

Relay 投递（独立定时/常驻任务）：
```pseudo
function relayLoop() {
  while (true) {
    rows = outbox.selectWhere(status in (PENDING, FAILED) and next_retry_time <= now()).limit(100)
    for row in rows {
      try {
        sendToMQorWebhook(row.event_type, row.payload)
        outbox.update(row.id, status='SENT')
      } catch (err) {
        backoff = computeBackoff(row.retry_count)     // 2^n * base 退避，设置上限
        outbox.update(row.id, status='FAILED', retry_count=retry_count+1, next_retry_time=now()+backoff)
        logError(row.id, err)
      }
    }
    sleep(200~500ms)
  }
}
```

Consumer 幂等（示例策略）：
- 以 aggregateId + 事件时间戳/序列号作为去重键
- 业务侧维护 processed_event 表或用幂等缓存（短期）

## 8. 幂等性与顺序性
- 发送端：Outbox 采用“至少一次投递”，可能重复；因此 Consumer 必须幂等
- 有序性：
  - 同一聚合（invoiceId）可在 Outbox 侧按 create_time 排序投递
  - 若 MQ 支持 Key 分区（如 Kafka），以 aggregateId 作为 key 提升局部有序
  - 无法绝对保证全局有序，消费端需要容错（按版本/时间戳忽略过期事件）

## 9. 失败重试与退避
- 指数退避：minBackoff、maxBackoff、抖动（Jitter）
- 最大重试次数：超过阈值仍失败，标记为 DEAD/FAILED 并告警
- 人工介入：提供查询与补发能力（按 id/时间范围/tenant）

## 10. 安全与合规
- payload 中避免存储敏感数据（PII/PAN），或进行脱敏
- 配置保留/归档策略：例如保留 30~90 天后归档至冷存储

## 11. 监控与告警
- 指标：发送成功率、失败率、重试次数分布、积压量（PENDING+FAILED 数量）、投递延迟P95/P99
- 日志：每次失败的错误码/异常堆栈、目标端响应
- 告警：积压超阈值、失败率超阈值、单事件重试超过阈值

## 12. 部署运行建议
- Relay 可作为 Spring 定时任务（@Scheduled）或独立微服务运行
- 任务并发：按租户/事件类型分片拉取，避免热点竞争；同一条记录通过乐观锁（version）或 where status=... and id=... 限制并发更新
- 容量规划：按每秒事件量×保留时长预估表大小；考虑分表与索引优化

## 13. 与 MyBatis-Plus 乐观锁的配合
- Outbox 表也可继承 BaseTenantEntity（含 version），在更新 status 时携带 version 进行并发控制
- 对于高并发 Relay，多实例可同时拉取；通过乐观锁更新 status，冲突自动重试

## 14. 在本项目中的落地建议
- 短期：先仅文档化方案（本文件），等待明确的外部订阅方/消息通道后再落表与 Relay 实现
- 中期：在 InvoiceService、RefundService 成功路径插入 Outbox；实现最小 Relay（定时轮询+HTTP/MQ 发送）
- 长期：引入专用事件总线（Kafka/RabbitMQ），在 Consumer 侧完善幂等与顺序处理

## 15. 常见坑
- 业务与 Outbox 不在同一事务：会产生双写不一致
- payload 可变：重放时难以审计与重现
- 重试无退避：目标端短暂故障容易被打爆
- 未做幂等：Consumer 可能重复消费导致副作用

## 16. 快速检查清单
- [ ] 业务写库与写 Outbox 在同一事务
- [ ] Relay 具备重试/退避/死信逻辑
- [ ] Consumer 幂等已实现（去重键/版本校验）
- [ ] 监控告警就绪（积压量/失败率/延迟）
- [ ] 数据合规（脱敏/归档）

