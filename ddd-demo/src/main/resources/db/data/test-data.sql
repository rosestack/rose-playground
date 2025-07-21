-- 插入测试数据
INSERT INTO `users` (`id`, `username`, `email`, `password`, `status`, `province`, `city`, `district`, `address`) VALUES
('1', 'admin', 'admin@example.com', 'admin123', 'ACTIVE', '北京市', '北京市', '朝阳区', '三里屯街道'),
('2', 'user1', 'user1@example.com', 'user123', 'ACTIVE', '上海市', '上海市', '浦东新区', '陆家嘴街道'),
('3', 'user2', 'user2@example.com', 'user123', 'INACTIVE', '广东省', '深圳市', '南山区', '科技园街道'),
('4', 'user3', 'user3@example.com', 'user123', 'ACTIVE', '浙江省', '杭州市', '西湖区', '文三路街道');