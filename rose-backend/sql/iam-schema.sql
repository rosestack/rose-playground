CREATE TABLE `user` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(64) NOT NULL UNIQUE,
  `password` VARCHAR(128) NOT NULL,
  `email` VARCHAR(128),
  `phone` VARCHAR(32),
  `status` INT DEFAULT 1,
  `tenant_id` BIGINT
) ;

-- 通知主表
CREATE TABLE notice (
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
    CONSTRAINT uk_notice_request_id UNIQUE (request_id)
);

-- 通知模板表
CREATE TABLE `notice_template` (
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
CREATE TABLE `notice_preference` (
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
CREATE TABLE `notice_channel` (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64),
    channel_type VARCHAR(32),
    config TEXT,
    enabled BOOLEAN
);

CREATE TABLE `notice_template_channel` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id VARCHAR(64),
    channel_id VARCHAR(64)
);
