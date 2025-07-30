-- 测试数据
-- 清空现有数据
DELETE FROM `user`;

-- 插入测试用户数据
INSERT INTO `user` (`id`, `username`, `email`, `phone`, `password`, `status`, `created_by`, `updated_by`) VALUES
(1, 'admin', 'admin@example.com', '13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'ACTIVE', 'system', 'system'),
(2, 'user1', 'user1@example.com', '13800138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'ACTIVE', 'system', 'system'),
(3, 'user2', 'user2@example.com', '13800138002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'INACTIVE', 'system', 'system');
