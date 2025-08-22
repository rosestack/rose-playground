-- 计费系统数据库初始化脚本
-- 适用于MySQL 8.0+
-- 支持多租户SaaS计费系统

-- 功能表
CREATE TABLE IF NOT EXISTS `bill_feature` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='功能表';

-- 套餐表
CREATE TABLE IF NOT EXISTS `bill_plan` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐表';

-- 套餐功能关联表
CREATE TABLE IF NOT EXISTS `bill_plan_feature` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `plan_id` BIGINT NOT NULL COMMENT '套餐ID',
  `feature_id` BIGINT NOT NULL COMMENT '功能ID',
  `feature_value` VARCHAR(200) COMMENT '功能值配置',
  `status` ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` VARCHAR(100),
  `updated_by` VARCHAR(100),
  
  UNIQUE KEY `uk_plan_feature` (`plan_id`, `feature_id`),
  KEY `idx_feature_id` (`feature_id`),
  FOREIGN KEY (`plan_id`) REFERENCES `bill_plan`(`id`),
  FOREIGN KEY (`feature_id`) REFERENCES `bill_feature`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐功能关联表';

-- 统一定价表
CREATE TABLE IF NOT EXISTS `bill_price` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `type` ENUM('PLAN','FEATURE','TENANT_PLAN','TENANT_FEATURE') NOT NULL COMMENT '定价类型',
  `target_type` ENUM('PLAN','FEATURE') NOT NULL COMMENT '目标类型',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `tenant_id` BIGINT COMMENT '租户ID',
  `price` DECIMAL(18,4) DEFAULT 0 COMMENT '价格',
  `currency` VARCHAR(10) DEFAULT 'USD' COMMENT '货币单位',
  `billing_cycle` ENUM('MONTHLY','YEARLY','USAGE') NOT NULL COMMENT '计费周期',
  `pricing_config` JSON COMMENT '定价配置',
  `effective_time` DATETIME NOT NULL COMMENT '生效时间',
  `expire_time` DATETIME COMMENT '失效时间',
  `status` ENUM('DRAFT','ACTIVE','INACTIVE','EXPIRED') DEFAULT 'DRAFT' COMMENT '状态',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` VARCHAR(100),
  `updated_by` VARCHAR(100),
  
  UNIQUE KEY `uk_pricing_rule` (`type`, `target_type`, `target_id`, `tenant_id`, `billing_cycle`, `effective_time`),
  KEY `idx_type_target` (`type`, `target_type`, `target_id`),
  KEY `idx_tenant_pricing` (`tenant_id`, `type`),
  KEY `idx_effective_time` (`effective_time`, `expire_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一定价表';

-- 试用记录表
CREATE TABLE IF NOT EXISTS `bill_trial_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `plan_id` BIGINT NOT NULL COMMENT '套餐ID',
  `trial_start_time` DATETIME NOT NULL COMMENT '试用开始时间',
  `trial_end_time` DATETIME NOT NULL COMMENT '试用结束时间',
  `status` ENUM('ACTIVE','EXPIRED','CONVERTED','CANCELLED') DEFAULT 'ACTIVE' COMMENT '试用状态',
  `converted_time` DATETIME COMMENT '转换为付费时间',
  `cancel_reason` VARCHAR(200) COMMENT '取消原因',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` VARCHAR(100),
  `updated_by` VARCHAR(100),
  
  KEY `idx_tenant_plan` (`tenant_id`, `plan_id`),
  KEY `idx_trial_time` (`trial_start_time`, `trial_end_time`),
  KEY `idx_status` (`status`),
  FOREIGN KEY (`plan_id`) REFERENCES `bill_plan`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试用记录表';

-- 订阅表
CREATE TABLE IF NOT EXISTS `bill_subscription` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订阅记录表';

-- 使用量记录表
CREATE TABLE IF NOT EXISTS `bill_usage` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用量记录表';

-- 账单表
CREATE TABLE IF NOT EXISTS `bill_invoice` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `invoice_no` VARCHAR(50) NOT NULL COMMENT '账单编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `subscription_id` BIGINT NOT NULL COMMENT '订阅ID',
  `billing_period_start` DATE NOT NULL COMMENT '计费周期开始日期',
  `billing_period_end` DATE NOT NULL COMMENT '计费周期结束日期',
  `invoice_type` ENUM('SUBSCRIPTION','USAGE','ADJUSTMENT','REFUND') NOT NULL COMMENT '账单类型',
  `subtotal` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '小计金额',
  `tax_amount` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '税费金额',
  `discount_amount` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '折扣金额',
  `total_amount` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '总金额',
  `currency` VARCHAR(10) DEFAULT 'USD' COMMENT '货币单位',
  `status` ENUM('DRAFT','PENDING','PAID','OVERDUE','CANCELLED','REFUNDED') DEFAULT 'DRAFT' COMMENT '账单状态',
  `due_date` DATETIME NOT NULL COMMENT '到期时间',
  `paid_time` DATETIME COMMENT '支付时间',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` VARCHAR(100),
  `updated_by` VARCHAR(100),
  
  UNIQUE KEY `uk_invoice_no` (`invoice_no`),
  KEY `idx_tenant_subscription` (`tenant_id`, `subscription_id`),
  KEY `idx_billing_period` (`billing_period_start`, `billing_period_end`),
  KEY `idx_status_due_date` (`status`, `due_date`),
  KEY `idx_invoice_type` (`invoice_type`),
  FOREIGN KEY (`subscription_id`) REFERENCES `bill_subscription`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账单表';

-- 账单明细表
CREATE TABLE IF NOT EXISTS `bill_invoice_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `invoice_id` BIGINT NOT NULL COMMENT '账单ID',
  `item_type` ENUM('PLAN','FEATURE','DISCOUNT','TAX','ADJUSTMENT') NOT NULL COMMENT '明细类型',
  `target_type` ENUM('PLAN','FEATURE') COMMENT '目标类型',
  `target_id` BIGINT COMMENT '目标ID',
  `description` VARCHAR(200) NOT NULL COMMENT '明细描述',
  `quantity` DECIMAL(18,4) NOT NULL DEFAULT 1 COMMENT '数量',
  `unit_price` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '单价',
  `amount` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '金额',
  `currency` VARCHAR(10) DEFAULT 'USD' COMMENT '货币单位',
  `billing_period_start` DATE COMMENT '计费周期开始',
  `billing_period_end` DATE COMMENT '计费周期结束',
  `metadata` JSON COMMENT '明细元数据',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` VARCHAR(100),
  `updated_by` VARCHAR(100),
  
  KEY `idx_invoice_id` (`invoice_id`),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_target` (`target_type`, `target_id`),
  FOREIGN KEY (`invoice_id`) REFERENCES `bill_invoice`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账单明细表';

-- 支付记录表
CREATE TABLE IF NOT EXISTS `bill_payment` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `payment_no` VARCHAR(50) NOT NULL COMMENT '支付编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `invoice_id` BIGINT COMMENT '账单ID',
  `amount` DECIMAL(18,4) NOT NULL COMMENT '支付金额',
  `currency` VARCHAR(10) DEFAULT 'USD' COMMENT '货币单位',
  `payment_method` ENUM('CREDIT_CARD','DEBIT_CARD','PAYPAL','BANK_TRANSFER','WALLET','OTHER') NOT NULL COMMENT '支付方式',
  `payment_gateway` VARCHAR(50) COMMENT '支付网关',
  `gateway_transaction_id` VARCHAR(100) COMMENT '网关交易ID',
  `status` ENUM('PENDING','SUCCESS','FAILED','CANCELLED','REFUNDED') DEFAULT 'PENDING' COMMENT '支付状态',
  `paid_time` DATETIME COMMENT '支付时间',
  `failure_reason` VARCHAR(200) COMMENT '失败原因',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` VARCHAR(100),
  `updated_by` VARCHAR(100),
  
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_invoice_id` (`invoice_id`),
  KEY `idx_status_paid_time` (`status`, `paid_time`),
  KEY `idx_gateway_transaction` (`payment_gateway`, `gateway_transaction_id`),
  FOREIGN KEY (`invoice_id`) REFERENCES `bill_invoice`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- 插入基础功能数据
INSERT IGNORE INTO `bill_feature` (`tenant_id`, `code`, `name`, `type`, `unit`, `reset_period`, `value_scope`) VALUES
(0, 'api_calls', 'API调用', 'USAGE', '次', 'MONTH', 'PER_SUBSCRIPTION'),
(0, 'storage', '存储空间', 'QUOTA', 'GB', 'NEVER', 'PER_SUBSCRIPTION'),
(0, 'users', '用户数量', 'QUOTA', '个', 'NEVER', 'PER_SUBSCRIPTION'),
(0, 'advanced_analytics', '高级分析', 'SWITCH', NULL, 'NEVER', 'PER_SUBSCRIPTION');

-- 插入套餐数据
INSERT IGNORE INTO `bill_plan` (
    `tenant_id`, `code`, `name`, `plan_type`, `billing_mode`, `trial_enabled`, 
    `trial_days`, `status`, `effective_time`
) VALUES 
(0, 'FREE', '免费版', 'FREE', 'PREPAID', 0, 0, 'ACTIVE', NOW()),
(0, 'PRO', '专业版', 'PRO', 'POSTPAID', 1, 15, 'ACTIVE', NOW()),
(0, 'ENTERPRISE', '企业版', 'ENTERPRISE', 'POSTPAID', 1, 30, 'ACTIVE', NOW());