-- 创建数据库
CREATE DATABASE IF NOT EXISTS ddd_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ddd_demo;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `gender` TINYINT COMMENT '性别：0-未知，1-男，2-女',
    `birthday` DATETIME COMMENT '生日',
    `country` VARCHAR(50) COMMENT '国家',
    `province` VARCHAR(50) COMMENT '省份',
    `city` VARCHAR(50) COMMENT '城市',
    `district` VARCHAR(50) COMMENT '区县',
    `detail_address` VARCHAR(255) COMMENT '详细地址',
    `postal_code` VARCHAR(20) COMMENT '邮政编码',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_username_email` (`username`, `email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入测试数据
INSERT INTO `user` (`username`, `email`, `phone`, `password`, `real_name`, `nickname`, `status`) VALUES
('admin', 'admin@example.com', '13800138000', '123456', '管理员', 'Admin', 1),
('test001', 'test001@example.com', '13800138001', '123456', '测试用户1', 'Test1', 1),
('test002', 'test002@example.com', '13800138002', '123456', '测试用户2', 'Test2', 1); 