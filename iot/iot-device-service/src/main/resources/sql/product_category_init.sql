-- 产品分类表初始化脚本
-- 创建时间：2024-01-01
-- 说明：初始化产品分类相关表结构和基础数据

-- 创建产品分类表
CREATE TABLE IF NOT EXISTS iot_product_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(128) NOT NULL COMMENT '分类名称',
    code VARCHAR(64) UNIQUE NOT NULL COMMENT '分类标识符',
    parent_id BIGINT COMMENT '父分类ID',
    level INT DEFAULT 1 COMMENT '分类层级',
    sort_order INT DEFAULT 0 COMMENT '排序',
    icon VARCHAR(255) COMMENT '分类图标',
    description TEXT COMMENT '分类描述',
    type ENUM('STANDARD', 'CUSTOM') DEFAULT 'CUSTOM' COMMENT '分类类型：标准行业分类/自定义分类',
    template_id BIGINT COMMENT '关联的物模型模板ID',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '分类状态',
    tenant_id BIGINT COMMENT '租户ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) COMMENT '创建人',
    updated_by VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_product_category_parent_id (parent_id),
    INDEX idx_product_category_code (code),
    INDEX idx_product_category_type (type),
    INDEX idx_product_category_status (status),
    INDEX idx_product_category_tenant_id (tenant_id),
    INDEX idx_product_category_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品分类表';

-- 插入基础分类数据
INSERT INTO iot_product_category (name, code, parent_id, level, sort_order, icon, description, type, status, created_by) VALUES
-- 根分类
('智能家居', 'smart_home', NULL, 1, 1, 'icon-home', '智能家居设备分类', 'CUSTOM', 'ACTIVE', 'system'),
('工业设备', 'industrial_equipment', NULL, 1, 2, 'icon-industry', '工业设备分类', 'STANDARD', 'ACTIVE', 'system'),
('智能穿戴', 'smart_wearable', NULL, 1, 3, 'icon-watch', '智能穿戴设备分类', 'CUSTOM', 'ACTIVE', 'system'),
('医疗健康', 'medical_health', NULL, 1, 4, 'icon-medical', '医疗健康设备分类', 'STANDARD', 'ACTIVE', 'system'),
('农业物联网', 'agriculture_iot', NULL, 1, 5, 'icon-agriculture', '农业物联网设备分类', 'STANDARD', 'ACTIVE', 'system'),

-- 智能家居子分类
('智能照明', 'smart_lighting', 1, 2, 1, 'icon-lightbulb', '智能照明设备分类', 'CUSTOM', 'ACTIVE', 'system'),
('智能安防', 'smart_security', 1, 2, 2, 'icon-shield', '智能安防设备分类', 'CUSTOM', 'ACTIVE', 'system'),
('智能家电', 'smart_appliance', 1, 2, 3, 'icon-appliance', '智能家电设备分类', 'CUSTOM', 'ACTIVE', 'system'),
('智能控制', 'smart_control', 1, 2, 4, 'icon-control', '智能控制设备分类', 'CUSTOM', 'ACTIVE', 'system'),

-- 工业设备子分类
('工业传感器', 'industrial_sensor', 2, 2, 1, 'icon-sensor', '工业传感器设备分类', 'STANDARD', 'ACTIVE', 'system'),
('工业控制器', 'industrial_controller', 2, 2, 2, 'icon-controller', '工业控制器设备分类', 'STANDARD', 'ACTIVE', 'system'),
('工业网关', 'industrial_gateway', 2, 2, 3, 'icon-gateway', '工业网关设备分类', 'STANDARD', 'ACTIVE', 'system'),
('工业执行器', 'industrial_actuator', 2, 2, 4, 'icon-actuator', '工业执行器设备分类', 'STANDARD', 'ACTIVE', 'system'),

-- 智能穿戴子分类
('智能手表', 'smart_watch', 3, 2, 1, 'icon-watch', '智能手表设备分类', 'CUSTOM', 'ACTIVE', 'system'),
('智能手环', 'smart_band', 3, 2, 2, 'icon-band', '智能手环设备分类', 'CUSTOM', 'ACTIVE', 'system'),
('智能眼镜', 'smart_glasses', 3, 2, 3, 'icon-glasses', '智能眼镜设备分类', 'CUSTOM', 'ACTIVE', 'system'),
('智能耳机', 'smart_headphones', 3, 2, 4, 'icon-headphones', '智能耳机设备分类', 'CUSTOM', 'ACTIVE', 'system'),

-- 医疗健康子分类
('医疗监护', 'medical_monitoring', 4, 2, 1, 'icon-monitoring', '医疗监护设备分类', 'STANDARD', 'ACTIVE', 'system'),
('医疗诊断', 'medical_diagnosis', 4, 2, 2, 'icon-diagnosis', '医疗诊断设备分类', 'STANDARD', 'ACTIVE', 'system'),
('康复设备', 'rehabilitation_equipment', 4, 2, 3, 'icon-rehabilitation', '康复设备分类', 'STANDARD', 'ACTIVE', 'system'),
('健康管理', 'health_management', 4, 2, 4, 'icon-health', '健康管理设备分类', 'STANDARD', 'ACTIVE', 'system'),

-- 农业物联网子分类
('环境监测', 'environmental_monitoring', 5, 2, 1, 'icon-environment', '环境监测设备分类', 'STANDARD', 'ACTIVE', 'system'),
('精准灌溉', 'precision_irrigation', 5, 2, 2, 'icon-irrigation', '精准灌溉设备分类', 'STANDARD', 'ACTIVE', 'system'),
('智能养殖', 'smart_farming', 5, 2, 3, 'icon-farming', '智能养殖设备分类', 'STANDARD', 'ACTIVE', 'system'),
('农产品追溯', 'product_traceability', 5, 2, 4, 'icon-traceability', '农产品追溯设备分类', 'STANDARD', 'ACTIVE', 'system'),

-- 三级分类示例
('LED照明', 'led_lighting', 6, 3, 1, 'icon-led', 'LED照明设备分类', 'CUSTOM', 'ACTIVE', 'system'),
('智能开关', 'smart_switch', 6, 3, 2, 'icon-switch', '智能开关设备分类', 'CUSTOM', 'ACTIVE', 'system'),
('摄像头', 'camera', 7, 3, 1, 'icon-camera', '摄像头设备分类', 'CUSTOM', 'ACTIVE', 'system'),
('门锁', 'smart_lock', 7, 3, 2, 'icon-lock', '智能门锁设备分类', 'CUSTOM', 'ACTIVE', 'system'),
('空调', 'air_conditioner', 8, 3, 1, 'icon-ac', '智能空调设备分类', 'CUSTOM', 'ACTIVE', 'system'),
('冰箱', 'refrigerator', 8, 3, 2, 'icon-fridge', '智能冰箱设备分类', 'CUSTOM', 'ACTIVE', 'system'),
('温度传感器', 'temperature_sensor', 10, 3, 1, 'icon-temp-sensor', '温度传感器设备分类', 'STANDARD', 'ACTIVE', 'system'),
('压力传感器', 'pressure_sensor', 10, 3, 2, 'icon-pressure-sensor', '压力传感器设备分类', 'STANDARD', 'ACTIVE', 'system'),
('心率监测', 'heart_rate_monitor', 18, 3, 1, 'icon-heart-rate', '心率监测设备分类', 'STANDARD', 'ACTIVE', 'system'),
('血压监测', 'blood_pressure_monitor', 18, 3, 2, 'icon-bp-monitor', '血压监测设备分类', 'STANDARD', 'ACTIVE', 'system'),
('土壤监测', 'soil_monitoring', 22, 3, 1, 'icon-soil', '土壤监测设备分类', 'STANDARD', 'ACTIVE', 'system'),
('气象监测', 'weather_monitoring', 22, 3, 2, 'icon-weather', '气象监测设备分类', 'STANDARD', 'ACTIVE', 'system');

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_product_category_parent_id ON iot_product_category(parent_id);
CREATE INDEX IF NOT EXISTS idx_product_category_code ON iot_product_category(code);
CREATE INDEX IF NOT EXISTS idx_product_category_type ON iot_product_category(type);
CREATE INDEX IF NOT EXISTS idx_product_category_status ON iot_product_category(status);
CREATE INDEX IF NOT EXISTS idx_product_category_tenant_id ON iot_product_category(tenant_id);
CREATE INDEX IF NOT EXISTS idx_product_category_template_id ON iot_product_category(template_id);

-- 查询验证
SELECT '产品分类表创建完成' AS message;
SELECT COUNT(*) AS total_categories FROM iot_product_category;
SELECT name, code, parent_id, level, type, status FROM iot_product_category ORDER BY level, sort_order, id; 