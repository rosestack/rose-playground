CREATE TABLE `user` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(64) NOT NULL UNIQUE,
  `password` VARCHAR(128) NOT NULL,
  `email` VARCHAR(128),
  `phone` VARCHAR(32),
  `status` INT DEFAULT 1,
  `tenant_id` BIGINT
) ;

-- 租户计费系统表结构
-- 订阅计划表
CREATE TABLE `subscription_plan` (
    `id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(64),
    `name` VARCHAR(128) NOT NULL,
    `code` VARCHAR(64) NOT NULL UNIQUE,
    `description` TEXT,
    `billing_type` VARCHAR(32) NOT NULL,
    `base_price` DECIMAL(10,2) DEFAULT 0.00,
    `billing_cycle` INT DEFAULT 30,
    `max_users` INT,
    `max_storage` BIGINT,
    `api_call_limit` BIGINT,
    `custom_branding_enabled` BOOLEAN DEFAULT FALSE,
    `features` JSON,
    `usage_pricing` JSON,
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
    `status` VARCHAR(32) NOT NULL,
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
    `status` VARCHAR(32) NOT NULL,
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

-- 使用量记录表
CREATE TABLE `usage_record` (
    `id` VARCHAR(64) PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL,
    `metric_type` VARCHAR(64) NOT NULL,
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

-- 初始化默认订阅计划
INSERT INTO `subscription_plan` (`id`, `name`, `code`, `description`, `billing_type`, `base_price`, `billing_cycle`, `max_users`, `max_storage`, `api_call_limit`, `trial_days`, `enabled`) VALUES
('plan_free', '免费版', 'FREE', '适合个人用户和小团队', 'MONTHLY', 0.00, 30, 5, 1073741824, 1000, 7, TRUE),
('plan_basic', '基础版', 'BASIC', '适合小型企业', 'MONTHLY', 29.99, 30, 25, 10737418240, 10000, 14, TRUE),
('plan_pro', '专业版', 'PRO', '适合成长型企业', 'MONTHLY', 99.99, 30, 100, 107374182400, 100000, 14, TRUE),
('plan_enterprise', '企业版', 'ENTERPRISE', '适合大型企业', 'MONTHLY', 299.99, 30, 1000, 1073741824000, 1000000, 30, TRUE);

-- 通知主表
CREATE TABLE `notification` (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64),
    channel_id VARCHAR(64),
    template_id VARCHAR(64),
    channel_type VARCHAR(32),
    target VARCHAR(128),
    target_type VARCHAR(32),
    content TEXT,
    status VARCHAR(32),
    fail_reason VARCHAR(255),
    send_time DATETIME,
    read_time DATETIME,
    retry_count INT default -1,
    trace_id VARCHAR(64),
    request_id VARCHAR(64),
    UNIQUE KEY uk_notification_request_id (request_id)
);

-- 通知模板表
CREATE TABLE `notification_template` (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64),
    name VARCHAR(64),
    code VARCHAR(64),
    description VARCHAR(255),
    type VARCHAR(32),
    content TEXT,
    enabled BOOLEAN,
    version INT,
    lang VARCHAR(16)
);

-- 用户通知偏好表
CREATE TABLE `notification_preference` (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64),
    user_id VARCHAR(64),
    channel_type VARCHAR(32),
    enabled BOOLEAN,
    quiet_period VARCHAR(64),
    channel_blacklist TEXT,
    channel_whitelist TEXT,
    frequency_limit INT
);

-- 渠道配置表
CREATE TABLE `notification_channel` (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64),
    channel_type VARCHAR(32),
    config TEXT,
    enabled BOOLEAN
);

CREATE TABLE `notification_template_channel` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id VARCHAR(64),
    channel_id VARCHAR(64)
);
