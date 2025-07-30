-- 用户表（演示安全加密 + 哈希查询）
CREATE TABLE user (
    id BIGINT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    
    -- 手机号：安全加密存储 + 哈希查询
    phone TEXT COMMENT '手机号（AES加密存储）',
    phone_hash VARCHAR(128) COMMENT '手机号哈希（HMAC-SHA256，用于查询）',
    
    -- 邮箱：安全加密存储 + 哈希查询  
    email TEXT COMMENT '邮箱（AES加密存储）',
    email_hash VARCHAR(128) COMMENT '邮箱哈希（HMAC-SHA256，用于查询）',
    
    -- 身份证：仅加密存储（不需要查询）
    id_card TEXT COMMENT '身份证号（AES加密存储）',
    
    -- 银行卡号：仅加密存储（不需要查询）
    bank_card TEXT COMMENT '银行卡号（AES加密存储）',
    
    -- 审计字段
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(50) COMMENT '创建人',
    updated_by VARCHAR(50) COMMENT '更新人',
    
    -- 乐观锁版本字段
    version INT DEFAULT 0 COMMENT '版本号',
    
    -- 多租户字段
    tenant_id VARCHAR(50) COMMENT '租户ID',
    
    -- 数据权限字段
    user_id VARCHAR(50) COMMENT '用户ID（数据权限）',
    dept_id VARCHAR(50) COMMENT '部门ID（数据权限）',
    
    -- 逻辑删除字段
    deleted TINYINT DEFAULT 0 COMMENT '是否删除（0-未删除，1-已删除）',
    
    -- 索引
    INDEX idx_username (username),
    INDEX idx_phone_hash (phone_hash),
    INDEX idx_email_hash (email_hash),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_user_id (user_id),
    INDEX idx_dept_id (dept_id),
    INDEX idx_created_time (created_time),
    
    -- 复合索引
    INDEX idx_tenant_user (tenant_id, user_id),
    INDEX idx_phone_email_hash (phone_hash, email_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入测试数据（注意：实际使用时敏感字段会被自动加密）
INSERT INTO user (id, username, phone, email, id_card, bank_card, created_by, updated_by, tenant_id, user_id, dept_id) VALUES
(1, 'admin', '13800138000', 'admin@example.com', '110101199001011234', '6222021234567890123', 'system', 'system', 'tenant_001', 'user_001', 'dept_001'),
(2, 'user1', '13800138001', 'user1@example.com', '110101199001011235', '6222021234567890124', 'admin', 'admin', 'tenant_001', 'user_002', 'dept_001'),
(3, 'user2', '13800138002', 'user2@example.com', '110101199001011236', '6222021234567890125', 'admin', 'admin', 'tenant_002', 'user_003', 'dept_002');

-- 查询示例（使用哈希字段）
-- 注意：实际使用时需要先计算哈希值
-- SELECT * FROM user WHERE phone_hash = 'computed_hash_value';
-- SELECT * FROM user WHERE email_hash = 'computed_hash_value';
-- SELECT * FROM user WHERE phone_hash = 'hash1' OR email_hash = 'hash2';
