-- 租户计费系统数据库设计

-- 订阅计划表
CREATE TABLE `subscription_plan` (
    `id` VARCHAR(64) PRIMARY KEY COMMENT '主键',
    `tenant_id` VARCHAR(64) COMMENT '租户ID',
    `name` VARCHAR(128) NOT NULL COMMENT '计划名称',
    `code` VARCHAR(64) NOT NULL UNIQUE COMMENT '计划编码',
    `description` TEXT COMMENT '计划描述',
    `billing_type` VARCHAR(32) NOT NULL COMMENT '计费类型: MONTHLY/YEARLY/USAGE_BASED/HYBRID',
    `base_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '基础价格',
    `billing_cycle` INT DEFAULT 30 COMMENT '计费周期(天)',
    `max_users` INT COMMENT '最大用户数',
    `max_storage` BIGINT COMMENT '最大存储(字节)',
    `api_call_limit` BIGINT COMMENT 'API 调用上限',
    `custom_branding_enabled` BOOLEAN DEFAULT FALSE COMMENT '允许自定义品牌',
    `features` JSON COMMENT '功能特性配置',
    `usage_pricing` JSON COMMENT '使用量定价配置',
    `enabled` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    `trial_days` INT DEFAULT 0 COMMENT '试用天数',
    `effective_time` DATETIME COMMENT '生效时间',
    `expiry_time` DATETIME COMMENT '过期时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_code (`code`),
    INDEX idx_enabled (`enabled`)
);

-- 租户订阅表
CREATE TABLE `tenant_subscription` (
    `id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL,
    `plan_id` VARCHAR(64) NOT NULL,
    `status` VARCHAR(32) NOT NULL COMMENT '订阅状态: TRIAL/ACTIVE/PAUSED/CANCELLED/EXPIRED/PENDING_PAYMENT',
    `start_time` DATETIME NOT NULL COMMENT '订阅开始时间',
    `end_time` DATETIME COMMENT '订阅结束时间',
    `next_billing_time` DATETIME COMMENT '下次计费时间',
    `trial_end_time` DATETIME COMMENT '试用结束时间',
    `in_trial` BOOLEAN DEFAULT FALSE COMMENT '是否在试用期',
    `auto_renew` BOOLEAN DEFAULT TRUE COMMENT '是否自动续费',
    `current_period_amount` DECIMAL(10,2) COMMENT '当前计费周期基础费用',
    `cancelled_time` DATETIME COMMENT '取消时间',
    `cancellation_reason` VARCHAR(255) COMMENT '取消原因',
    `paused_time` DATETIME COMMENT '暂停时间',
    `pause_reason` VARCHAR(255) COMMENT '暂停原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_tenant_subscription (`tenant_id`),
    INDEX idx_status (`status`),
    INDEX idx_next_billing (`next_billing_time`),
    FOREIGN KEY (`plan_id`) REFERENCES `subscription_plan`(`id`)
);

-- 账单表
CREATE TABLE `invoice` (
    `id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `invoice_number` VARCHAR(128) NOT NULL UNIQUE COMMENT '账单号',
    `subscription_id` VARCHAR(64) NOT NULL COMMENT '订阅ID',
    `period_start` DATE NOT NULL COMMENT '计费周期开始日期',
    `period_end` DATE NOT NULL COMMENT '计费周期结束日期',
    `base_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '基础费用',
    `usage_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '使用量费用',
    `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '折扣金额',
    `tax_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '税费',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
    `status` VARCHAR(32) NOT NULL COMMENT '账单状态: DRAFT/PENDING/PAID/OVERDUE/CANCELLED/REFUNDED',
    `due_date` DATE NOT NULL COMMENT '到期日期',
    `paid_time` DATETIME COMMENT '支付时间',
    `payment_method` VARCHAR(64) COMMENT '支付方式',
    `payment_transaction_id` VARCHAR(128) COMMENT '支付交易ID',
    `notes` TEXT COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tenant (`tenant_id`),
    INDEX idx_subscription (`subscription_id`),
    INDEX idx_status (`status`),
    INDEX idx_due_date (`due_date`),
    INDEX idx_invoice_number (`invoice_number`),
    FOREIGN KEY (`subscription_id`) REFERENCES `tenant_subscription`(`id`)
);

-- 账单明细表
CREATE TABLE `invoice_line_item` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `invoice_id` VARCHAR(64) NOT NULL COMMENT '账单ID',
    `description` VARCHAR(255) COMMENT '条目描述',
    `metric_type` VARCHAR(64) COMMENT '计量类型',
    `quantity` DECIMAL(15,4) COMMENT '数量',
    `unit_price` DECIMAL(10,4) COMMENT '单价',
    `amount` DECIMAL(10,2) COMMENT '金额',
    INDEX idx_invoice (`invoice_id`),
    FOREIGN KEY (`invoice_id`) REFERENCES `invoice`(`id`) ON DELETE CASCADE
);

-- 使用量记录表
CREATE TABLE `usage_record` (
    `id` VARCHAR(64) PRIMARY KEY COMMENT '主键',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `metric_type` VARCHAR(64) NOT NULL COMMENT '计量类型: API_CALLS/STORAGE/USERS/SMS/EMAIL等',
    `quantity` DECIMAL(15,4) NOT NULL COMMENT '数量',
    `unit` VARCHAR(32) COMMENT '单位',
    `record_time` DATETIME NOT NULL COMMENT '记录时间',
    `resource_id` VARCHAR(128) COMMENT '资源ID',
    `resource_type` VARCHAR(64) COMMENT '资源类型',
    `metadata` TEXT COMMENT '元数据',
    `billed` BOOLEAN DEFAULT FALSE COMMENT '是否已计费',
    `billed_time` DATETIME COMMENT '计费时间',
    `invoice_id` VARCHAR(64) COMMENT '关联账单ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tenant_metric (`tenant_id`, `metric_type`),
    INDEX idx_record_time (`record_time`),
    INDEX idx_billed (`billed`),
    INDEX idx_tenant_period (`tenant_id`, `record_time`),
    FOREIGN KEY (`invoice_id`) REFERENCES `invoice`(`id`)
);

-- 使用量聚合表（用于快速查询和报表）
CREATE TABLE `usage_aggregate` (
    `id` VARCHAR(64) PRIMARY KEY COMMENT '主键',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `metric_type` VARCHAR(64) NOT NULL COMMENT '计量类型',
    `aggregation_period` VARCHAR(16) NOT NULL COMMENT '聚合周期: HOURLY/DAILY/MONTHLY',
    `period_start` DATETIME NOT NULL COMMENT '周期开始时间',
    `period_end` DATETIME NOT NULL COMMENT '周期结束时间',
    `total_usage` DECIMAL(15,4) NOT NULL COMMENT '总使用量',
    `billing_amount` DECIMAL(10,2) COMMENT '计费金额',
    `billed` BOOLEAN DEFAULT FALSE COMMENT '是否已计费',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_tenant_metric_period (`tenant_id`, `metric_type`, `aggregation_period`, `period_start`),
    INDEX idx_period (`period_start`, `period_end`)
);

-- 支付记录表
CREATE TABLE `payment_record` (
    `id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `invoice_id` VARCHAR(64) NOT NULL COMMENT '账单ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '交易金额',
    `payment_method` VARCHAR(64) NOT NULL COMMENT '支付方式',
    `transaction_id` VARCHAR(128) COMMENT '支付交易号',
    `gateway_response` JSON COMMENT '网关原始响应',
    `status` VARCHAR(32) NOT NULL COMMENT '状态: PENDING/SUCCESS/FAILED/REFUNDED',
    `channel_status` VARCHAR(32) COMMENT '渠道状态',
    `channel_amount` DECIMAL(10,2) COMMENT '渠道确认金额',
    `paid_time` DATETIME COMMENT '支付时间',
    `refunded_time` DATETIME COMMENT '退款时间',
    `refund_reason` VARCHAR(255) COMMENT '退款原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tenant (`tenant_id`),
    INDEX idx_invoice (`invoice_id`),
    INDEX idx_transaction (`transaction_id`),
    FOREIGN KEY (`invoice_id`) REFERENCES `invoice`(`id`)
);

-- 退款记录表
CREATE TABLE IF NOT EXISTS `refund_record` (
    `id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `invoice_id` VARCHAR(64) NOT NULL COMMENT '账单ID',
    `payment_method` VARCHAR(64) NOT NULL COMMENT '支付方式',
    `transaction_id` VARCHAR(128) NOT NULL COMMENT '原支付交易号',
    `refund_id` VARCHAR(128) COMMENT '通道退款单号',
    `idempotency_key` VARCHAR(128) COMMENT '幂等键',
    `refund_amount` DECIMAL(10,2) COMMENT '退款金额',
    `reason` VARCHAR(255) COMMENT '退款原因',
    `status` VARCHAR(32) NOT NULL COMMENT '退款状态',
    `raw_callback` JSON COMMENT '回调原文',
    `requested_time` DATETIME COMMENT '请求时间',
    `completed_time` DATETIME COMMENT '完成时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tenant (`tenant_id`),
    INDEX idx_invoice (`invoice_id`),
    INDEX idx_tx (`transaction_id`),
    INDEX idx_status (`status`),
    UNIQUE KEY uk_refund_idempotency (`idempotency_key`)
);


-- 折扣券表
CREATE TABLE `discount_coupon` (
    `id` VARCHAR(64) PRIMARY KEY COMMENT '主键',
    `tenant_id` VARCHAR(64) COMMENT '租户ID',
    `code` VARCHAR(64) NOT NULL UNIQUE COMMENT '优惠码',
    `name` VARCHAR(128) COMMENT '名称',
    `discount_type` VARCHAR(16) NOT NULL COMMENT '折扣类型: PERCENTAGE/FIXED_AMOUNT',
    `discount_value` DECIMAL(10,4) NOT NULL COMMENT '折扣值',
    `min_amount` DECIMAL(10,2) COMMENT '最低消费金额',
    `max_discount` DECIMAL(10,2) COMMENT '最大折扣金额',
    `usage_limit` INT COMMENT '使用次数限制',
    `used_count` INT DEFAULT 0 COMMENT '已使用次数',
    `valid_from` DATETIME NOT NULL COMMENT '生效时间',
    `valid_until` DATETIME NOT NULL COMMENT '失效时间',
    `enabled` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_code (`code`),
    INDEX idx_valid_period (`valid_from`, `valid_until`)
);

-- 计费配置表
CREATE TABLE `billing_config` (
    `id` VARCHAR(64) PRIMARY KEY COMMENT '主键',
    `tenant_id` VARCHAR(64) COMMENT '租户ID',
    `config_key` VARCHAR(128) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `description` VARCHAR(255) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_tenant_key (`tenant_id`, `config_key`)
);

-- 初始化默认订阅计划
INSERT INTO `subscription_plan` (`id`, `name`, `code`, `description`, `billing_type`, `base_price`, `billing_cycle`, `max_users`, `max_storage`, `api_call_limit`, `trial_days`, `enabled`) VALUES
('plan_free', '免费版', 'FREE', '适合个人用户和小团队', 'MONTHLY', 0.00, 30, 5, 1073741824, 1000, 7, TRUE),
('plan_basic', '基础版', 'BASIC', '适合小型企业', 'MONTHLY', 29.99, 30, 25, 10737418240, 10000, 14, TRUE),
('plan_pro', '专业版', 'PRO', '适合成长型企业', 'MONTHLY', 99.99, 30, 100, 107374182400, 100000, 14, TRUE),
('plan_enterprise', '企业版', 'ENTERPRISE', '适合大型企业', 'MONTHLY', 299.99, 30, 1000, 1073741824000, 1000000, 30, TRUE);
