-- 租户计费系统数据库设计

-- 订阅计划表
CREATE TABLE `subscription_plan` (
    `id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(64),
    `name` VARCHAR(128) NOT NULL,
    `code` VARCHAR(64) NOT NULL UNIQUE,
    `description` TEXT,
    `billing_type` VARCHAR(32) NOT NULL, -- MONTHLY, YEARLY, USAGE_BASED, HYBRID
    `base_price` DECIMAL(10,2) DEFAULT 0.00,
    `billing_cycle` INT DEFAULT 30, -- 计费周期（天）
    `max_users` INT,
    `max_storage` BIGINT, -- 存储限制（字节）
    `api_call_limit` BIGINT, -- API调用限制
    `custom_branding_enabled` BOOLEAN DEFAULT FALSE,
    `features` JSON, -- 功能特性配置
    `usage_pricing` JSON, -- 使用量定价配置
    `enabled` BOOLEAN DEFAULT TRUE,
    `trial_days` INT DEFAULT 0,
    `effective_date` DATETIME,
    `expiry_date` DATETIME,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (`code`),
    INDEX idx_enabled (`enabled`)
);

-- 租户订阅表
CREATE TABLE `tenant_subscription` (
    `id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL,
    `plan_id` VARCHAR(64) NOT NULL,
    `status` VARCHAR(32) NOT NULL, -- TRIAL, ACTIVE, PAUSED, CANCELLED, EXPIRED, PENDING_PAYMENT
    `start_date` DATETIME NOT NULL,
    `end_date` DATETIME,
    `next_billing_date` DATETIME,
    `trial_end_date` DATETIME,
    `in_trial` BOOLEAN DEFAULT FALSE,
    `auto_renew` BOOLEAN DEFAULT TRUE,
    `current_period_amount` DECIMAL(10,2),
    `cancelled_at` DATETIME,
    `cancellation_reason` VARCHAR(255),
    `paused_at` DATETIME,
    `pause_reason` VARCHAR(255),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_subscription (`tenant_id`),
    INDEX idx_status (`status`),
    INDEX idx_next_billing (`next_billing_date`),
    FOREIGN KEY (`plan_id`) REFERENCES `subscription_plan`(`id`)
);

-- 账单表
CREATE TABLE `invoice` (
    `id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL,
    `invoice_number` VARCHAR(128) NOT NULL UNIQUE,
    `subscription_id` VARCHAR(64) NOT NULL,
    `period_start` DATE NOT NULL,
    `period_end` DATE NOT NULL,
    `base_amount` DECIMAL(10,2) DEFAULT 0.00,
    `usage_amount` DECIMAL(10,2) DEFAULT 0.00,
    `discount_amount` DECIMAL(10,2) DEFAULT 0.00,
    `tax_amount` DECIMAL(10,2) DEFAULT 0.00,
    `total_amount` DECIMAL(10,2) NOT NULL,
    `status` VARCHAR(32) NOT NULL, -- DRAFT, PENDING, PAID, OVERDUE, CANCELLED, REFUNDED
    `due_date` DATE NOT NULL,
    `paid_at` DATETIME,
    `payment_method` VARCHAR(64),
    `payment_transaction_id` VARCHAR(128),
    `notes` TEXT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (`tenant_id`),
    INDEX idx_subscription (`subscription_id`),
    INDEX idx_status (`status`),
    INDEX idx_due_date (`due_date`),
    INDEX idx_invoice_number (`invoice_number`),
    FOREIGN KEY (`subscription_id`) REFERENCES `tenant_subscription`(`id`)
);

-- 账单明细表
CREATE TABLE `invoice_line_item` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `invoice_id` VARCHAR(64) NOT NULL,
    `description` VARCHAR(255),
    `metric_type` VARCHAR(64),
    `quantity` DECIMAL(15,4),
    `unit_price` DECIMAL(10,4),
    `amount` DECIMAL(10,2),
    INDEX idx_invoice (`invoice_id`),
    FOREIGN KEY (`invoice_id`) REFERENCES `invoice`(`id`) ON DELETE CASCADE
);

-- 使用量记录表
CREATE TABLE `usage_record` (
    `id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL,
    `metric_type` VARCHAR(64) NOT NULL, -- API_CALLS, STORAGE, USERS, SMS, EMAIL等
    `quantity` DECIMAL(15,4) NOT NULL,
    `unit` VARCHAR(32),
    `record_time` DATETIME NOT NULL,
    `resource_id` VARCHAR(128),
    `resource_type` VARCHAR(64),
    `metadata` TEXT,
    `billed` BOOLEAN DEFAULT FALSE,
    `billed_at` DATETIME,
    `invoice_id` VARCHAR(64),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant_metric (`tenant_id`, `metric_type`),
    INDEX idx_record_time (`record_time`),
    INDEX idx_billed (`billed`),
    INDEX idx_tenant_period (`tenant_id`, `record_time`),
    FOREIGN KEY (`invoice_id`) REFERENCES `invoice`(`id`)
);

-- 使用量聚合表（用于快速查询和报表）
CREATE TABLE `usage_aggregate` (
    `id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL,
    `metric_type` VARCHAR(64) NOT NULL,
    `aggregation_period` VARCHAR(16) NOT NULL, -- HOURLY, DAILY, MONTHLY
    `period_start` DATETIME NOT NULL,
    `period_end` DATETIME NOT NULL,
    `total_usage` DECIMAL(15,4) NOT NULL,
    `billing_amount` DECIMAL(10,2),
    `billed` BOOLEAN DEFAULT FALSE,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_metric_period (`tenant_id`, `metric_type`, `aggregation_period`, `period_start`),
    INDEX idx_period (`period_start`, `period_end`)
);

-- 支付记录表
CREATE TABLE `payment_record` (
    `id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL,
    `invoice_id` VARCHAR(64) NOT NULL,
    `amount` DECIMAL(10,2) NOT NULL,
    `payment_method` VARCHAR(64) NOT NULL,
    `transaction_id` VARCHAR(128),
    `gateway_response` JSON,
    `status` VARCHAR(32) NOT NULL, -- PENDING, SUCCESS, FAILED, REFUNDED
    `paid_at` DATETIME,
    `refunded_at` DATETIME,
    `refund_reason` VARCHAR(255),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (`tenant_id`),
    INDEX idx_invoice (`invoice_id`),
    INDEX idx_transaction (`transaction_id`),
    FOREIGN KEY (`invoice_id`) REFERENCES `invoice`(`id`)
);

-- 折扣券表
CREATE TABLE `discount_coupon` (
    `id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(64),
    `code` VARCHAR(64) NOT NULL UNIQUE,
    `name` VARCHAR(128),
    `discount_type` VARCHAR(16) NOT NULL, -- PERCENTAGE, FIXED_AMOUNT
    `discount_value` DECIMAL(10,4) NOT NULL,
    `min_amount` DECIMAL(10,2),
    `max_discount` DECIMAL(10,2),
    `usage_limit` INT,
    `used_count` INT DEFAULT 0,
    `valid_from` DATETIME NOT NULL,
    `valid_until` DATETIME NOT NULL,
    `enabled` BOOLEAN DEFAULT TRUE,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (`code`),
    INDEX idx_valid_period (`valid_from`, `valid_until`)
);

-- 计费配置表
CREATE TABLE `billing_config` (
    `id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(64),
    `config_key` VARCHAR(128) NOT NULL,
    `config_value` TEXT,
    `description` VARCHAR(255),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_key (`tenant_id`, `config_key`)
);

-- 初始化默认订阅计划
INSERT INTO `subscription_plan` (`id`, `name`, `code`, `description`, `billing_type`, `base_price`, `billing_cycle`, `max_users`, `max_storage`, `api_call_limit`, `trial_days`, `enabled`) VALUES
('plan_free', '免费版', 'FREE', '适合个人用户和小团队', 'MONTHLY', 0.00, 30, 5, 1073741824, 1000, 7, TRUE),
('plan_basic', '基础版', 'BASIC', '适合小型企业', 'MONTHLY', 29.99, 30, 25, 10737418240, 10000, 14, TRUE),
('plan_pro', '专业版', 'PRO', '适合成长型企业', 'MONTHLY', 99.99, 30, 100, 107374182400, 100000, 14, TRUE),
('plan_enterprise', '企业版', 'ENTERPRISE', '适合大型企业', 'MONTHLY', 299.99, 30, 1000, 1073741824000, 1000000, 30, TRUE);
