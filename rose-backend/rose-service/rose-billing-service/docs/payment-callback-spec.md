# 支付回调字段与签名规范（轻量版）

本文档描述当前未接入官方 SDK 前，网关回调所采用的字段约定与签名校验方案，以便前后端及支付通道联调。

## 通用回调URL

- `POST /api/billing/payment/callback/{paymentMethod}`
    - `paymentMethod` 取值：`ALIPAY`、`WECHAT`、`STRIPE`

## 字段映射（按优先级取值）

- invoiceId
    - `invoiceId` → `invoice_id` → `out_trade_no`（支付宝） → `client_reference_id`（Stripe）→ `invoice`
- transactionId
    - `transactionId` → `transaction_id`（微信）→ `trade_no`（支付宝）→ `payment_intent_id`（Stripe）→ `id`

不同通道的典型回调字段（仅示例，实际以通道/SDK为准）：

- 支付宝：`trade_no`, `trade_status`, `out_trade_no`, `timestamp`, `sign`
- 微信：`transaction_id`, `result_code`, `out_trade_no`, `timestamp`, `sign`
- Stripe：`id`, `type`, `data.object.payment_intent` 或 `payment_intent_id`, `client_reference_id`, `timestamp`, `sign`

## 轻量签名校验

为先行联调，启用“HMAC + 时间窗”方案；接入官方 SDK 后将替换为官方验签。

- 时间窗
    - 请求体携带 `timestamp`（秒级）
    - 服务器端允许与当前时间的偏差：`allowed-skew-seconds`（默认 300 秒）
- HMAC 签名
    - 可选字段 `sign`；配置了 `*.hmac-secret` 才会校验
    - 载荷拼接规则（按通道）：
        - Alipay: `invoiceId|trade_no|timestamp`
        - Wechat: `invoiceId|transaction_id|timestamp`
        - Stripe: `invoiceId|id|timestamp`
    - 签名算法：`HMAC-SHA256`，输出 hex 小写

## 配置项

```
rose.billing.payment.alipay.hmac-secret: ""
rose.billing.payment.alipay.allowed-skew-seconds: 300
rose.billing.payment.wechat.hmac-secret: ""
rose.billing.payment.wechat.allowed-skew-seconds: 300
rose.billing.payment.stripe.hmac-secret: ""
rose.billing.payment.stripe.allowed-skew-seconds: 300
```

## 幂等与安全

- 服务端以 `transaction_id`（或等价字段）作为唯一索引进行防重
- 同一 `invoiceId` 若已处于 PAID 状态，回调将被忽略并记录告警
- 建议网关层叠加：IP 白名单、速率限制、重放保护

## 错误响应

- 验签失败/字段缺失返回：`{"code":500, "message":"invalid callback"}`，HTTP 200，便于通道判定“已接收但不处理”
- 成功返回：`{"code":200}`

## 后续计划

- 接入官方 SDK（支付宝/微信/Stripe）进行标准验签
- 将“轻量签名”逐步下线，仅保留 SDK 验签为准

## 退款回调（轻量版）

- 回调URL（建议）：`POST /api/billing/refund/callback/{paymentMethod}`
- 字段映射：
    - invoiceId: `invoiceId`|`invoice_id`|`out_trade_no`|`client_reference_id`
    - refundId: `refund_id`|`id`
    - timestamp: 回调秒级时间戳
    - sign: HMAC-SHA256 签名（可选）
- 轻量校验（与支付回调一致）：
    - 时间窗：`allowed-skew-seconds`（默认 300 秒）
    - 签名载荷：`invoiceId|refund_id|timestamp`（不足字段为空串拼接）
- SDK 替代：接入支付宝/微信/Stripe 官方 SDK 后，以官方验签为准

回调处理建议：

- 校验通过后，根据 refundId/交易号更新 RefundRecord 状态（SUCCESS/FAILED）与 rawCallback
- 如全额退款，更新发票状态为 REFUNDED
- 事件外发：发布 RefundSucceededEvent（可选），用于通知与报表

## 通道字段对照表（退款）

- Alipay
    - 状态字段：refund_status → REFUND_SUCCESS/SUCCESS
    - 金额字段：refund_amount（单位：元）
    - 标识字段：refund_id，invoiceId（或 out_trade_no）
- Wechat
    - 状态字段：refund_status/status → SUCCESS
    - 金额字段：refund_fee（单位：分）
    - 标识字段：refund_id，invoiceId（或 out_trade_no）
- Stripe
    - 状态字段：status → SUCCEEDED
    - 金额字段：amount（单位：元）
    - 标识字段：id，invoiceId（或 client_reference_id）

## 错误码规范（退款回调）

- 4001 invalid refund callback（验签失败或字段缺失）
- 4002 callback update failed（数据库更新失败）
- 4003 idempotency conflict（幂等键冲突）

## 样例 Payload

- Alipay 成功

```json
{"invoiceId":"INV-1","refund_id":"ALI_RF_1","refund_amount":"30.00","refund_status":"REFUND_SUCCESS","timestamp":1720000000,"sign":"..."}
```

- Wechat 成功

```json
{"invoice_id":"INV-2","id":"WX_RF_1","refund_fee":"500","status":"SUCCESS","timestamp":1720000001,"sign":"..."}
```

- Stripe 成功

```json
{"invoiceId":"INV-3","id":"re_1","amount":"12.34","status":"succeeded","timestamp":1720000002,"sign":"..."}
```
