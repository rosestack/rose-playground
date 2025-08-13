## 计费核心数据模型（套餐 + 权益 + 订阅）

本文件梳理“套餐/套餐版本/权益/定价/订阅”核心建模，聚焦国内主流“套餐 + 权益”能力，暂不包含加购、发票、支付、退款。

- 数据库：MySQL 5.7
- 字符集：utf8mb4
- 存储引擎：InnoDB
- 兼容性：MySQL 5.7，不使用 CHECK 约束与生成列；触发器使用 SIGNAL；ENUM 与触发器均为 5.7 支持；如需 JSON 可用 5.7.8+ 版本

### 1) 套餐

```sql
-- 套餐
CREATE TABLE IF NOT EXISTS `package` (
  `id` BIGINT PRIMARY KEY COMMENT '主键',
  `code` VARCHAR(64) NOT NULL UNIQUE COMMENT '套餐编码（对外唯一）',
  `name` VARCHAR(128) NOT NULL COMMENT '套餐名称',
  `description` VARCHAR(512) NULL COMMENT '套餐描述',
  `status` ENUM('DRAFT','ACTIVE','DEPRECATED','ARCHIVED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态：草稿/上架/停新购/归档',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐';

-- 套餐版本
CREATE TABLE IF NOT EXISTS `package_version` (
  `id` BIGINT PRIMARY KEY COMMENT '主键',
  `package_id` BIGINT NOT NULL COMMENT '所属套餐ID',
  `code` VARCHAR(64) NOT NULL COMMENT '版本编码（对外唯一）',
  `name` VARCHAR(128) NOT NULL COMMENT '版本名称',
  `description` VARCHAR(512) NULL COMMENT '版本描述',
  `status` ENUM('DRAFT','ACTIVE','DEPRECATED','ARCHIVED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态：草稿/上架/停新购/归档',
  `effective_from` DATETIME NULL COMMENT '生效开始时间',
  `effective_to` DATETIME NULL COMMENT '生效结束时间',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  UNIQUE KEY `uk_pkg_ver` (`package_id`,`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐版本';

CREATE TABLE package_version_status_history (
  id BIGINT PRIMARY KEY,
  package_version_id BIGINT NOT NULL,
  from_status ENUM('DRAFT','ACTIVE','DEPRECATED','ARCHIVED') NULL,
  to_status   ENUM('DRAFT','ACTIVE','DEPRECATED','ARCHIVED') NOT NULL,
  changed_time  DATETIME NOT NULL,
  changed_by  VARCHAR(64) NULL,
  reason      VARCHAR(256) NULL,
  created_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by  VARCHAR(64) NULL COMMENT '创建人',
  updated_by  VARCHAR(64) NULL COMMENT '更新人',
  deleted  TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  version  INT NOT NULL DEFAULT 0 COMMENT '版本号'
) ENGINE=InnoDB;
```

### 2) 权益

```sql
CREATE TABLE IF NOT EXISTS `entitlement` (
  `id` BIGINT PRIMARY KEY COMMENT '主键',
  `code` VARCHAR(64) NOT NULL UNIQUE COMMENT '权益编码（全局唯一）',
  `category` VARCHAR(64) NOT NULL COMMENT '分类（如 CORE/SECURITY）',
  `name` VARCHAR(128) NOT NULL COMMENT '权益名称',
  `description` VARCHAR(512) NULL COMMENT '权益描述',
  `unit` VARCHAR(32) NOT NULL COMMENT '计量单位，如 GB/COUNT/SEAT/QPS',
  `value_type` ENUM('BOOLEAN','NUMERIC','STRING') NOT NULL,
  `pricing_mode` ENUM('NONE','TIERED') NOT NULL DEFAULT 'NONE' COMMENT '计价方式：NONE=不阶梯；TIERED=按区间固定价',
  `value_scope` ENUM('PER_SUBSCRIPTION','PER_SEAT') DEFAULT 'PER_SUBSCRIPTION',
  `reset_period` ENUM('DAY','MONTH','YEAR','NEVER') NOT NULL DEFAULT 'MONTH' COMMENT '重置周期',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权益';
```

### 3) 套餐启用的权益、套餐定价

```sql
-- 套餐启用的权益（额度/限流/可超量）
CREATE TABLE IF NOT EXISTS `package_entitlement` (
  `id` BIGINT PRIMARY KEY COMMENT '主键',
  `package_version_id` BIGINT NOT NULL COMMENT '套餐版本ID',
  `entitlement_id` BIGINT NOT NULL COMMENT '权益ID',
  `value` VARCHAR(32) NULL COMMENT '权益值',
  `value_scope` ENUM('PER_SUBSCRIPTION','PER_SEAT') DEFAULT 'PER_SUBSCRIPTION',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  UNIQUE KEY `uk_pvr` (`package_version_id`,`entitlement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 套餐定价
CREATE TABLE IF NOT EXISTS `package_price` (
  `id` BIGINT PRIMARY KEY COMMENT '主键',
  `package_version_id` BIGINT NOT NULL COMMENT '套餐版本ID',
  `currency` CHAR(3) NOT NULL COMMENT '币种（ISO）',
  `period_unit` ENUM('MONTH','YEAR') NOT NULL COMMENT '周期单位',
  `base_price` DECIMAL(18,4) NOT NULL COMMENT '基础定价/月',
  `period_count` INT UNSIGNED NOT NULL COMMENT '月数：1、12',
  `billing_mode` ENUM('STANDARD','FREE','CONTRACT') NOT NULL DEFAULT 'STANDARD',
  `trial_days` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '试用天数',
  `effective_from` DATETIME,
  `effective_to` DATETIME,
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  UNIQUE KEY `uk_pvp` (`package_version_id`,`currency`,`period_unit`,`period_count`,`effective_from`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 4) 订阅 / 订阅-权益用量

```sql
-- 订阅（固定指向某个套餐版本，保证历史条款与价格可追溯）
CREATE TABLE IF NOT EXISTS `subscription` (
  `id` BIGINT PRIMARY KEY COMMENT '主键',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `package_version_id` BIGINT NOT NULL COMMENT '套餐版本ID',
  `package_price_id` BIGINT NOT NULL COMMENT '套餐定价ID',
  `status` ENUM('TRIAL','ACTIVE','PAST_DUE','SUSPENDED','CANCELLED','EXPIRED') NOT NULL DEFAULT 'ACTIVE' COMMENT '订阅状态',
  `price_source` ENUM('STANDARD','CONTRACT') NOT NULL DEFAULT 'STANDARD',
  `unit_price` DECIMAL(18,4) NULL,
  `auto_renew` TINYINT(1) NOT NULL DEFAULT 1,
  `trial_start` DATETIME NULL,
  `trial_end` DATETIME NULL,
  `next_billing_time` DATETIME NULL,
  `start_time` DATETIME NOT NULL COMMENT '订阅开始时间',
  `end_time` DATETIME NULL COMMENT '订阅结束时间',
  `current_period_start` DATETIME NOT NULL COMMENT '当前账期开始',
  `current_period_end` DATETIME NOT NULL COMMENT '当前账期结束',
  `cancel_at_period_end` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '到期取消',
  `currency` CHAR(3) NOT NULL COMMENT '币种',
  `quantity` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '订阅席位数量（建议与 MAX_USERS 保持一致）',
  `notes` VARCHAR(512) NULL COMMENT '备注',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  KEY idx_tenant_status_period (tenant_id, status, current_period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订阅-权益用量（按权益的 reset_period 切片）
CREATE TABLE IF NOT EXISTS `subscription_entitlement_usage` (
  `id` BIGINT PRIMARY KEY COMMENT '主键',
  `subscription_id` BIGINT NOT NULL COMMENT '订阅ID',
  `entitlement_id` BIGINT NOT NULL COMMENT '权益ID',
  `period_start` DATETIME NOT NULL COMMENT '计量周期开始',
  `period_end` DATETIME NOT NULL COMMENT '计量周期结束',
  `used_value` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '用量',
  `last_event_time` DATETIME NULL COMMENT '最后一次计量事件时间',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  UNIQUE KEY `uk_usage_period` (`subscription_id`,`entitlement_id`,`period_start`,`period_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订阅变更历史（套餐升级/降级记录）
CREATE TABLE IF NOT EXISTS `subscription_change_history` (
  `id` BIGINT PRIMARY KEY COMMENT '主键',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `subscription_id` BIGINT NOT NULL COMMENT '订阅ID',
  `change_type` ENUM('UPGRADE','DOWNGRADE','SWITCH','QUANTITY_CHANGE','PRICE_CHANGE') NOT NULL COMMENT '变更类型：升级/降级/切换/数量变更/价格变更',
  `from_package_version_id` BIGINT NULL COMMENT '原套餐版本ID',
  `to_package_version_id` BIGINT NULL COMMENT '新套餐版本ID',
  `from_package_price_id` BIGINT NULL COMMENT '原定价ID',
  `to_package_price_id` BIGINT NULL COMMENT '新定价ID',
  `from_quantity` INT UNSIGNED NULL COMMENT '原数量',
  `to_quantity` INT UNSIGNED NULL COMMENT '新数量',
  `from_unit_price` DECIMAL(18,4) NULL COMMENT '原单价',
  `to_unit_price` DECIMAL(18,4) NULL COMMENT '新单价',
  `currency` CHAR(3) NOT NULL COMMENT '币种',
  `effective_time` DATETIME NOT NULL COMMENT '生效时间',
  `proration_type` ENUM('IMMEDIATE','END_OF_PERIOD','NEXT_BILLING') NOT NULL DEFAULT 'IMMEDIATE' COMMENT '按比例计费类型：立即生效/周期结束/下个计费周期',
  `proration_amount` DECIMAL(18,4) NULL COMMENT '按比例调整金额（正数补缴，负数退费）',
  `reason` ENUM('USER_REQUESTED','AUTOMATIC','ADMIN','TRIAL_CONVERSION','PAYMENT_FAILURE','SYSTEM') NOT NULL DEFAULT 'USER_REQUESTED' COMMENT '变更原因',
  `notes` VARCHAR(512) NULL COMMENT '变更备注',
  `invoice_id` BIGINT NULL COMMENT '关联账单ID（如有按比例计费）',
  `operator_id` VARCHAR(64) NULL COMMENT '操作人ID',
  `operator_type` ENUM('USER','ADMIN','SYSTEM') NOT NULL DEFAULT 'USER' COMMENT '操作人类型',
  `approval_required` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否需要审批',
  `approval_status` ENUM('PENDING','APPROVED','REJECTED','NOT_REQUIRED') NOT NULL DEFAULT 'NOT_REQUIRED' COMMENT '审批状态',
  `approved_by` VARCHAR(64) NULL COMMENT '审批人ID',
  `approved_time` DATETIME NULL COMMENT '审批时间',
  `rollback_available` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否支持回滚',
  `rollback_deadline` DATETIME NULL COMMENT '回滚截止时间',
  `status` ENUM('PENDING','PROCESSING','COMPLETED','FAILED','ROLLED_BACK') NOT NULL DEFAULT 'COMPLETED' COMMENT '变更状态',
  `metadata` JSON NULL COMMENT '扩展信息（如变更前后的权益对比等）',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  KEY `idx_subscription_id` (`subscription_id`),
  KEY `idx_tenant_effective` (`tenant_id`, `effective_time`),
  KEY `idx_change_type` (`change_type`),
  KEY `idx_approval_status` (`approval_status`),
  KEY `idx_status` (`status`),
  KEY `idx_operator` (`operator_id`, `operator_type`),
  KEY `idx_effective_time` (`effective_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订阅变更历史表';

```

### 5) 权益阶梯定价（按区间固定价：例如最大用户数）

```sql
CREATE TABLE IF NOT EXISTS `entitlement_tier_price` (
  `id` BIGINT PRIMARY KEY COMMENT '主键',
  `package_version_id` BIGINT NOT NULL COMMENT '套餐版本ID',
  `entitlement_id` BIGINT NOT NULL COMMENT '权益ID（如 MAX_USERS）',
  `currency` CHAR(3) NOT NULL COMMENT '币种',
  `cap_min` INT UNSIGNED NOT NULL COMMENT '区间下限（含）',
  `cap_max` INT UNSIGNED NULL COMMENT '区间上限（含；NULL=无上限）',
  `period_price` DECIMAL(18,4) NOT NULL COMMENT '该区间每计费周期固定总价',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  UNIQUE KEY `uk_pvrtp` (`package_version_id`,`entitlement_id`,`currency`,`cap_min`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 6) 租户合同价

```sql
-- 合同价（示意）
CREATE TABLE tenant_package_price (
  `id` BIGINT PRIMARY KEY COMMENT '主键',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `package_version_id` BIGINT NOT NULL COMMENT '套餐版本ID',
  `currency` CHAR(3) NOT NULL COMMENT '币种（ISO）',
  `period_unit` ENUM('MONTH','YEAR') NOT NULL COMMENT '周期单位',
  `base_price` DECIMAL(18,4) NOT NULL COMMENT '基础定价/月',
  `period_count` INT NOT NULL COMMENT '月数：1、12',
  `effective_from` DATETIME,
  `effective_to` DATETIME,
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  UNIQUE KEY `uk_pvp` (`package_version_id`,`currency`,`period_unit`,`period_count`,`effective_from`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tenant_entitlement_tier_price` (
  `id` BIGINT PRIMARY KEY COMMENT '主键',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `package_version_id` BIGINT NOT NULL COMMENT '套餐版本ID',
  `entitlement_id` BIGINT NOT NULL COMMENT '权益ID（如 MAX_USERS）',
  `currency` CHAR(3) NOT NULL COMMENT '币种',
  `cap_min` INT UNSIGNED NOT NULL COMMENT '区间下限（含）',
  `cap_max` INT UNSIGNED NULL COMMENT '区间上限（含；NULL=无上限）',
  `period_price` DECIMAL(18,4) NOT NULL COMMENT '该区间每计费周期固定总价',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  UNIQUE KEY `uk_pvrtp` (`package_version_id`,`entitlement_id`,`currency`,`cap_min`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 7) 账单管理表

```sql
CREATE TABLE IF NOT EXISTS `invoice` (
  `id` BIGINT PRIMARY KEY COMMENT '账单ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `subscription_id` BIGINT NOT NULL COMMENT '订阅ID',
  `invoice_number` VARCHAR(64) NOT NULL COMMENT '账单编号',
  `period_start` DATE NOT NULL COMMENT '计费周期开始日期',
  `period_end` DATE NOT NULL COMMENT '计费周期结束日期',
  `status` ENUM('DRAFT','PENDING','PAID','OVERDUE','CANCELLED','REFUNDED') NOT NULL DEFAULT 'DRAFT' COMMENT '账单状态',
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `base_amount` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '基础费用',
  `usage_amount` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '用量费用',
  `discount_amount` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '折扣金额',
  `tax_amount` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '税费',
  `total_amount` DECIMAL(18,4) NOT NULL COMMENT '总金额',
  `due_date` DATE NOT NULL COMMENT '到期日期',
  `paid_time` DATETIME NULL COMMENT '支付时间',
  `payment_method` VARCHAR(32) NULL COMMENT '支付方式',
  `payment_transaction_id` VARCHAR(128) NULL COMMENT '支付交易ID',
  `price_snapshot` JSON NULL COMMENT '定价快照',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  UNIQUE KEY `uk_invoice_number` (`invoice_number`),
  KEY `idx_tenant_status` (`tenant_id`, `status`),
  KEY `idx_subscription_period` (`subscription_id`, `period_start`, `period_end`),
  KEY `idx_due_date` (`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单表';

CREATE TABLE IF NOT EXISTS `invoice_item` (
  `id` BIGINT PRIMARY KEY COMMENT '账单明细ID',
  `invoice_id` BIGINT NOT NULL COMMENT '账单ID',
  `type` ENUM('BASE','USAGE','DISCOUNT','TAX') NOT NULL COMMENT '明细类型',
  `description` VARCHAR(255) NOT NULL COMMENT '明细描述',
  `quantity` DECIMAL(18,4) NOT NULL DEFAULT 1 COMMENT '数量',
  `unit_price` DECIMAL(18,4) NOT NULL COMMENT '单价',
  `amount` DECIMAL(18,4) NOT NULL COMMENT '金额',
  `entitlement_id` BIGINT NULL COMMENT '关联权益ID',
  `metadata` JSON NULL COMMENT '扩展信息',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  KEY `idx_invoice_id` (`invoice_id`),
  KEY `idx_entitlement_id` (`entitlement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单明细表';

-- =====================================================
-- 支付管理表
-- =====================================================

CREATE TABLE IF NOT EXISTS `payment_record` (
  `id` BIGINT PRIMARY KEY COMMENT '支付记录ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `invoice_id` BIGINT NOT NULL COMMENT '账单ID',
  `payment_method` VARCHAR(32) NOT NULL COMMENT '支付方式（ALIPAY,WECHAT,BANK_CARD等）',
  `transaction_id` VARCHAR(128) NOT NULL COMMENT '交易ID',
  `amount` DECIMAL(18,4) NOT NULL COMMENT '支付金额',
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `status` ENUM('PENDING','SUCCESS','FAILED','CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '支付状态',
  `gateway_response` JSON NULL COMMENT '网关响应信息',
  `channel_status` VARCHAR(32) NULL COMMENT '渠道状态',
  `channel_amount` DECIMAL(18,4) NULL COMMENT '渠道金额',
  `posted` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已入账',
  `posted_time` DATETIME NULL COMMENT '入账时间',
  `paid_time` DATETIME NULL COMMENT '支付完成时间',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_tenant_status` (`tenant_id`, `status`),
  KEY `idx_invoice_id` (`invoice_id`),
  KEY `idx_posted` (`posted`, `posted_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- =====================================================
-- 退款管理表
-- =====================================================

CREATE TABLE IF NOT EXISTS `refund_record` (
  `id` BIGINT PRIMARY KEY COMMENT '退款记录ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `invoice_id` BIGINT NOT NULL COMMENT '账单ID',
  `payment_record_id` BIGINT NOT NULL COMMENT '原支付记录ID',
  `refund_id` VARCHAR(128) NULL COMMENT '退款ID',
  `refund_amount` DECIMAL(18,4) NOT NULL COMMENT '退款金额',
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `reason` VARCHAR(255) NULL COMMENT '退款原因',
  `status` ENUM('REQUESTED','PROCESSING','SUCCESS','FAILED','CANCELLED') NOT NULL DEFAULT 'REQUESTED' COMMENT '退款状态',
  `payment_method` VARCHAR(32) NOT NULL COMMENT '退款方式',
  `idempotency_key` VARCHAR(128) NULL COMMENT '幂等键',
  `gateway_response` JSON NULL COMMENT '网关响应信息',
  `requested_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `completed_time` DATETIME NULL COMMENT '完成时间',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  KEY `idx_tenant_status` (`tenant_id`, `status`),
  KEY `idx_invoice_id` (`invoice_id`),
  KEY `idx_payment_record_id` (`payment_record_id`),
  KEY `idx_idempotency_key` (`idempotency_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- =====================================================
-- 优惠券管理表
-- =====================================================

CREATE TABLE IF NOT EXISTS `coupon` (
  `id` BIGINT PRIMARY KEY COMMENT '优惠券ID',
  `code` VARCHAR(64) NOT NULL COMMENT '优惠券代码',
  `name` VARCHAR(128) NOT NULL COMMENT '优惠券名称',
  `description` TEXT NULL COMMENT '优惠券描述',
  `type` ENUM('PERCENTAGE','FIXED_AMOUNT','FREE_TRIAL') NOT NULL COMMENT '优惠券类型',
  `value` DECIMAL(18,4) NOT NULL COMMENT '优惠值（百分比或固定金额）',
  `currency` CHAR(3) NULL COMMENT '币种（固定金额类型必填）',
  `min_amount` DECIMAL(18,4) NULL COMMENT '最小消费金额',
  `max_discount` DECIMAL(18,4) NULL COMMENT '最大折扣金额',
  `usage_limit` INT NULL COMMENT '使用次数限制',
  `used_count` INT NOT NULL DEFAULT 0 COMMENT '已使用次数',
  `per_user_limit` INT NULL COMMENT '每用户使用限制',
  `valid_from` DATETIME NOT NULL COMMENT '有效期开始',
  `valid_until` DATETIME NOT NULL COMMENT '有效期结束',
  `applicable_packages` JSON NULL COMMENT '适用套餐ID列表',
  `status` ENUM('ACTIVE','INACTIVE','EXPIRED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
 `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status_valid` (`status`, `valid_from`, `valid_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

CREATE TABLE IF NOT EXISTS `coupon_usage` (
  `id` BIGINT PRIMARY KEY COMMENT '优惠券使用记录ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `subscription_id` BIGINT NOT NULL COMMENT '订阅ID',
  `invoice_id` BIGINT NULL COMMENT '账单ID',
  `discount_amount` DECIMAL(18,4) NOT NULL COMMENT '折扣金额',
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `used_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '使用时间',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_subscription_id` (`subscription_id`),
  KEY `idx_invoice_id` (`invoice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券使用记录表';

-- =====================================================
-- 对账管理表
-- =====================================================

CREATE TABLE IF NOT EXISTS `reconciliation_batch` (
  `id` BIGINT PRIMARY KEY COMMENT '对账批次ID',
  `batch_number` VARCHAR(64) NOT NULL COMMENT '批次号',
  `reconcile_date` DATE NOT NULL COMMENT '对账日期',
  `payment_method` VARCHAR(32) NOT NULL COMMENT '支付方式',
  `status` ENUM('PENDING','PROCESSING','COMPLETED','FAILED') NOT NULL DEFAULT 'PENDING' COMMENT '对账状态',
  `total_records` INT NOT NULL DEFAULT 0 COMMENT '总记录数',
  `matched_records` INT NOT NULL DEFAULT 0 COMMENT '匹配记录数',
  `unmatched_records` INT NOT NULL DEFAULT 0 COMMENT '未匹配记录数',
  `error_records` INT NOT NULL DEFAULT 0 COMMENT '异常记录数',
  `start_time` DATETIME NULL COMMENT '开始时间',
  `complete_time` DATETIME NULL COMMENT '完成时间',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  UNIQUE KEY `uk_batch_number` (`batch_number`),
  KEY `idx_reconcile_date` (`reconcile_date`),
  KEY `idx_payment_method` (`payment_method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账批次表';

CREATE TABLE IF NOT EXISTS `reconciliation_record` (
  `id` BIGINT PRIMARY KEY COMMENT '对账记录ID',
  `batch_id` BIGINT NOT NULL COMMENT '对账批次ID',
  `payment_record_id` BIGINT NULL COMMENT '支付记录ID',
  `channel_transaction_id` VARCHAR(128) NOT NULL COMMENT '渠道交易ID',
  `channel_amount` DECIMAL(18,4) NOT NULL COMMENT '渠道金额',
  `channel_status` VARCHAR(32) NOT NULL COMMENT '渠道状态',
  `channel_time` DATETIME NOT NULL COMMENT '渠道时间',
  `our_amount` DECIMAL(18,4) NULL COMMENT '我方金额',
  `our_status` VARCHAR(32) NULL COMMENT '我方状态',
  `our_time` DATETIME NULL COMMENT '我方时间',
  `match_status` ENUM('MATCHED','UNMATCHED','AMOUNT_DIFF','STATUS_DIFF','TIME_DIFF') NOT NULL COMMENT '匹配状态',
  `difference_amount` DECIMAL(18,4) NULL COMMENT '金额差异',
  `remarks` TEXT NULL COMMENT '备注',
  `resolved` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已解决',
  `resolved_time` DATETIME NULL COMMENT '解决时间',
   `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_payment_record_id` (`payment_record_id`),
  KEY `idx_channel_transaction_id` (`channel_transaction_id`),
  KEY `idx_match_status` (`match_status`),
  KEY `idx_resolved` (`resolved`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账记录表';

-- =====================================================
-- 事件发件箱表（支持事件驱动架构）
-- =====================================================

CREATE TABLE IF NOT EXISTS `outbox_event` (
  `id` BIGINT PRIMARY KEY COMMENT '事件ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `event_type` VARCHAR(64) NOT NULL COMMENT '事件类型',
  `aggregate_id` VARCHAR(64) NOT NULL COMMENT '聚合根ID',
  `aggregate_type` VARCHAR(64) NOT NULL COMMENT '聚合根类型',
  `payload` JSON NOT NULL COMMENT '事件载荷',
  `status` ENUM('PENDING','SENT','FAILED') NOT NULL DEFAULT 'PENDING' COMMENT '发送状态',
  `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  `max_retry_count` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
  `next_retry_time` DATETIME NULL COMMENT '下次重试时间',
  `sent_time` DATETIME NULL COMMENT '发送时间',
  `error_message` TEXT NULL COMMENT '错误信息',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status_retry` (`status`, `next_retry_time`),
  KEY `idx_aggregate` (`aggregate_type`, `aggregate_id`),
  KEY `idx_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事件发件箱表';
```