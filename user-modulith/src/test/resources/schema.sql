-- H2 数据库兼容的用户表结构

-- 用户表
CREATE TABLE IF NOT EXISTS "user" (
    id BIGINT NOT NULL,
    username VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(11),
    password VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) DEFAULT 'system',
    updated_by VARCHAR(50) DEFAULT 'system',
    deleted BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 1,
    PRIMARY KEY (id)
);

-- 创建唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS uk_username ON "user" (username);
CREATE UNIQUE INDEX IF NOT EXISTS uk_email ON "user" (email);

-- 创建普通索引
CREATE INDEX IF NOT EXISTS idx_phone ON "user" (phone);
CREATE INDEX IF NOT EXISTS idx_status ON "user" (status);
CREATE INDEX IF NOT EXISTS idx_created_time ON "user" (created_time);