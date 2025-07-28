-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `user_modulith` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `user_modulith`;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(20) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `phone` VARCHAR(11) DEFAULT NULL COMMENT '手机号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    `created_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `updated_by` VARCHAR(50) DEFAULT 'system' COMMENT '更新人',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    `version` INT DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建索引
CREATE UNIQUE INDEX `uk_username` ON `user` (`username`);
CREATE UNIQUE INDEX `uk_email` ON `user` (`email`);
CREATE INDEX `idx_phone` ON `user` (`phone`);
CREATE INDEX `idx_status` ON `user` (`status`);
CREATE INDEX `idx_created_time` ON `user` (`created_time`);

SET FOREIGN_KEY_CHECKS = 1;