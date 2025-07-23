-- IoT物联网平台数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS iot_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE iot_platform;

-- 产品分类表
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

-- 物模型模板表
CREATE TABLE IF NOT EXISTS iot_thing_model_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    name VARCHAR(128) NOT NULL COMMENT '模板名称',
    code VARCHAR(64) UNIQUE NOT NULL COMMENT '模板标识符',
    description TEXT COMMENT '模板描述',
    content JSON NOT NULL COMMENT '物模型模板内容(TSL)',
    category_id BIGINT COMMENT '关联的产品分类ID',
    industry_type VARCHAR(64) COMMENT '行业类型',
    type ENUM('STANDARD', 'CUSTOM') DEFAULT 'CUSTOM' COMMENT '模板类型：标准模板/自定义模板',
    status ENUM('ACTIVE', 'INACTIVE', 'DEPRECATED') DEFAULT 'ACTIVE' COMMENT '模板状态',
    publish_status ENUM('DRAFT', 'PUBLISHED', 'OFFLINE') DEFAULT 'DRAFT' COMMENT '发布状态',
    version VARCHAR(32) DEFAULT '1.0' COMMENT '模板版本',
    change_log TEXT COMMENT '变更说明',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    last_used_time DATETIME COMMENT '最后使用时间',
    tags JSON COMMENT '模板标签',
    remarks TEXT COMMENT '备注信息',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) COMMENT '创建人',
    updated_by VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    version_num INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_thing_model_template_category_id (category_id),
    INDEX idx_thing_model_template_code (code),
    INDEX idx_thing_model_template_type (type),
    INDEX idx_thing_model_template_status (status),
    INDEX idx_thing_model_template_publish_status (publish_status),
    INDEX idx_thing_model_template_industry_type (industry_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物模型模板表';

-- 产品表
CREATE TABLE IF NOT EXISTS iot_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '产品ID',
    code VARCHAR(64) UNIQUE NOT NULL COMMENT '产品标识符',
    name VARCHAR(128) NOT NULL COMMENT '产品名称',
    logo VARCHAR(255) COMMENT '产品Logo',
    description TEXT COMMENT '产品描述',
    node_type ENUM('DIRECT', 'GATEWAY', 'SUB_DEVICE') NOT NULL COMMENT '节点类型',
    auth_type ENUM('KEY', 'CERT', 'NONE') NOT NULL COMMENT '认证方式',
    network_type ENUM('WIFI', 'ETHERNET', 'CELLULAR', 'OTHER') COMMENT '网络类型',
    protocol_type ENUM('BLE', 'ZIGBEE', 'MODBUS', 'OPC_UA', 'CUSTOM') COMMENT '协议类型',
    data_format ENUM('JSON', 'CUSTOM') DEFAULT 'JSON' COMMENT '数据格式',
    category_id BIGINT NOT NULL COMMENT '产品分类ID',
    tags JSON COMMENT '产品标签',
    status ENUM('DEVELOPING', 'PUBLISHED', 'OFFLINE') DEFAULT 'DEVELOPING' COMMENT '产品状态',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) COMMENT '创建人',
    updated_by VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_product_category_id (category_id),
    INDEX idx_product_code (code),
    INDEX idx_product_status (status),
    INDEX idx_product_node_type (node_type),
    INDEX idx_product_network_type (network_type),
    INDEX idx_product_protocol_type (protocol_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表';

-- 物模型表
CREATE TABLE IF NOT EXISTS iot_thing_model (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '物模型ID',
    name VARCHAR(128) NOT NULL COMMENT '物模型名称',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    content JSON NOT NULL COMMENT '物模型内容(TSL)',
    status ENUM('DRAFT', 'PUBLISHED', 'DEPRECATED') DEFAULT 'DRAFT' COMMENT '模型状态',
    description TEXT COMMENT '模型描述',
    change_log TEXT COMMENT '变更说明',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) COMMENT '创建人',
    updated_by VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_thing_model_product_id (product_id),
    INDEX idx_thing_model_status (status),
    INDEX idx_thing_model_version (version),
    UNIQUE KEY uk_thing_model_product_version (product_id, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物模型表';

-- 设备表
CREATE TABLE IF NOT EXISTS iot_device (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    name VARCHAR(128) NOT NULL COMMENT '设备名称',
    code VARCHAR(64) UNIQUE NOT NULL COMMENT '设备标识符',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    secret VARCHAR(128) COMMENT '设备密钥',
    nickname VARCHAR(128) COMMENT '设备昵称',
    status ENUM('ONLINE', 'OFFLINE', 'FAULT', 'MAINTENANCE') DEFAULT 'OFFLINE' COMMENT '设备状态',
    last_online_time DATETIME COMMENT '最后在线时间',
    last_offline_time DATETIME COMMENT '最后离线时间',
    ip_address VARCHAR(45) COMMENT '设备IP地址',
    firmware_version VARCHAR(64) COMMENT '固件版本',
    hardware_version VARCHAR(64) COMMENT '硬件版本',
    location_info JSON COMMENT '地理位置信息',
    tags JSON COMMENT '设备标签',
    metadata JSON COMMENT '设备元数据',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) COMMENT '创建人',
    updated_by VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_device_product_id (product_id),
    INDEX idx_device_code (code),
    INDEX idx_device_status (status),
    INDEX idx_device_last_online_time (last_online_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备表';

-- 设备属性表（时序数据）
CREATE TABLE IF NOT EXISTS iot_device_property (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '属性记录ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    code VARCHAR(64) NOT NULL COMMENT '属性标识符',
    value JSON NOT NULL COMMENT '属性值',
    timestamp DATETIME NOT NULL COMMENT '时间戳',
    quality INT DEFAULT 1 COMMENT '数据质量(0-1)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_device_property_device_code_timestamp (device_id, code, timestamp),
    INDEX idx_device_property_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备属性表';

-- 设备影子表（最新状态）
CREATE TABLE IF NOT EXISTS iot_device_shadow (
    device_id BIGINT PRIMARY KEY COMMENT '设备ID',
    properties JSON COMMENT '当前属性值',
    desired_properties JSON COMMENT '期望属性值',
    reported_properties JSON COMMENT '上报属性值',
    version BIGINT DEFAULT 1 COMMENT '版本号',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_device_shadow_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备影子表';

-- 用户表
CREATE TABLE IF NOT EXISTS iot_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(64) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(128) UNIQUE NOT NULL COMMENT '邮箱',
    password VARCHAR(128) NOT NULL COMMENT '密码',
    real_name VARCHAR(64) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') DEFAULT 'ACTIVE' COMMENT '用户状态',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(45) COMMENT '最后登录IP',
    tenant_id BIGINT COMMENT '租户ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) COMMENT '创建人',
    updated_by VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_user_username (username),
    INDEX idx_user_email (email),
    INDEX idx_user_tenant_id (tenant_id),
    INDEX idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS iot_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    name VARCHAR(64) NOT NULL COMMENT '角色名称',
    code VARCHAR(64) UNIQUE NOT NULL COMMENT '角色标识符',
    type ENUM('SYSTEM', 'CUSTOM') DEFAULT 'CUSTOM' COMMENT '角色类型',
    description TEXT COMMENT '角色描述',
    tenant_id BIGINT COMMENT '租户ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) COMMENT '创建人',
    updated_by VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_role_code (code),
    INDEX idx_role_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS iot_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    name VARCHAR(128) NOT NULL COMMENT '权限名称',
    code VARCHAR(128) UNIQUE NOT NULL COMMENT '权限标识符',
    type ENUM('MENU', 'BUTTON', 'API') NOT NULL COMMENT '权限类型',
    resource_path VARCHAR(255) COMMENT '资源路径',
    parent_id BIGINT COMMENT '父权限ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    description TEXT COMMENT '权限描述',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) COMMENT '创建人',
    updated_by VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_permission_code (code),
    INDEX idx_permission_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS iot_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    tenant_id BIGINT COMMENT '租户ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) COMMENT '创建人',
    updated_by VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_user_role_user_id (user_id),
    INDEX idx_user_role_role_id (role_id),
    INDEX idx_user_role_tenant_id (tenant_id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS iot_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    tenant_id BIGINT COMMENT '租户ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) COMMENT '创建人',
    updated_by VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_role_permission_role_id (role_id),
    INDEX idx_role_permission_permission_id (permission_id),
    INDEX idx_role_permission_tenant_id (tenant_id),
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS iot_system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(128) UNIQUE NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'FILE') DEFAULT 'STRING' COMMENT '配置类型',
    description TEXT COMMENT '配置描述',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统配置',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '配置状态',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) COMMENT '创建人',
    updated_by VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_system_config_config_key (config_key),
    INDEX idx_system_config_is_system (is_system),
    INDEX idx_system_config_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS iot_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(64) COMMENT '操作用户名',
    operation_type ENUM('CREATE', 'UPDATE', 'DELETE', 'QUERY', 'LOGIN', 'LOGOUT', 'OTHER') NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(64) COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    operation_desc VARCHAR(255) COMMENT '操作描述',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_params JSON COMMENT '请求参数',
    response_result JSON COMMENT '响应结果',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    status ENUM('SUCCESS', 'FAILED') DEFAULT 'SUCCESS' COMMENT '操作状态',
    error_message TEXT COMMENT '错误信息',
    execution_time INT COMMENT '执行时间(毫秒)',
    tenant_id BIGINT COMMENT '租户ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_operation_log_user_id (user_id),
    INDEX idx_operation_log_operation_type (operation_type),
    INDEX idx_operation_log_resource_type (resource_type),
    INDEX idx_operation_log_status (status),
    INDEX idx_operation_log_tenant_id (tenant_id),
    INDEX idx_operation_log_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 插入初始数据
INSERT INTO iot_user (username, email, password, real_name, status) VALUES 
('admin', 'admin@iot.com', '$2a$10$7JB720yubVSOfvVWdBYoOeymQxqKxqKxqKxqKxqKxqKxqKxqKxqK', '系统管理员', 'ACTIVE');

INSERT INTO iot_role (name, code, type, description) VALUES 
('系统管理员', 'ADMIN', 'SYSTEM', '系统管理员角色'),
('普通用户', 'USER', 'SYSTEM', '普通用户角色');

INSERT INTO iot_user_role (user_id, role_id) VALUES (1, 1);