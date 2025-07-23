# ThingsBoard接口设计参考

## 概述

本文档整理了ThingsBoard平台的各种协议接口设计，作为IoT平台开发的参考。

## 1. REST API接口

### 1.1 认证接口

```
POST /api/auth/login
POST /api/auth/token
POST /api/auth/logout
```

### 1.2 设备管理接口

```
# 设备CRUD
POST   /api/device
GET    /api/device/{deviceId}
PUT    /api/device/{deviceId}
DELETE /api/device/{deviceId}

# 设备属性
GET    /api/device/{deviceId}/attributes
POST   /api/device/{deviceId}/attributes
DELETE /api/device/{deviceId}/attributes/{scope}/{attributeKey}

# 设备关系
GET    /api/device/{deviceId}/relations
POST   /api/device/{deviceId}/relation
DELETE /api/device/{deviceId}/relation
```

### 1.3 遥测数据接口

```
# 时间序列数据
POST   /api/plugins/telemetry/DEVICE/{deviceId}/values/timeseries
GET    /api/plugins/telemetry/DEVICE/{deviceId}/values/timeseries
DELETE /api/plugins/telemetry/DEVICE/{deviceId}/values/timeseries

# 属性数据
POST   /api/plugins/telemetry/DEVICE/{deviceId}/values/attributes
GET    /api/plugins/telemetry/DEVICE/{deviceId}/values/attributes
DELETE /api/plugins/telemetry/DEVICE/{deviceId}/values/attributes

# 聚合数据
GET    /api/plugins/telemetry/DEVICE/{deviceId}/values/aggregation
```

### 1.4 租户管理接口

```
# 租户CRUD
POST   /api/tenant
GET    /api/tenant/{tenantId}
PUT    /api/tenant/{tenantId}
DELETE /api/tenant/{tenantId}

# 租户信息
GET    /api/tenant/info
```

### 1.5 客户管理接口

```
# 客户CRUD
POST   /api/customer
GET    /api/customer/{customerId}
PUT    /api/customer/{customerId}
DELETE /api/customer/{customerId}

# 客户设备
GET    /api/customer/{customerId}/devices
POST   /api/customer/{customerId}/device/{deviceId}
DELETE /api/customer/{customerId}/device/{deviceId}
```

### 1.6 用户管理接口

```
# 用户CRUD
POST   /api/user
GET    /api/user/{userId}
PUT    /api/user/{userId}
DELETE /api/user/{userId}

# 用户权限
GET    /api/user/{userId}/authority
POST   /api/user/{userId}/authority
```

### 1.7 规则引擎接口

```
# 规则链
POST   /api/ruleChain
GET    /api/ruleChain/{ruleChainId}
PUT    /api/ruleChain/{ruleChainId}
DELETE /api/ruleChain/{ruleChainId}

# 规则节点
POST   /api/ruleNode
GET    /api/ruleNode/{ruleNodeId}
PUT    /api/ruleNode/{ruleNodeId}
DELETE /api/ruleNode/{ruleNodeId}
```

## 2. MQTT接口

### 2.1 设备连接

```
# 设备认证
POST /v1/devices/me/telemetry
POST /v1/devices/me/attributes
GET  /v1/devices/me/attributes
POST /v1/devices/me/attributes/request
POST /v1/devices/me/attributes/response
```

### 2.2 遥测数据

```
# 发布遥测数据
Topic: v1/devices/me/telemetry
Payload: {"temperature": 25.5, "humidity": 60.2}

# 发布属性
Topic: v1/devices/me/attributes
Payload: {"firmware_version": "1.0.0"}
```

### 2.3 命令下发

```
# 订阅命令
Topic: v1/devices/me/commands/request/+

# 响应命令
Topic: v1/devices/me/commands/response/{requestId}
Payload: {"status": "SUCCESS", "data": {...}}
```

### 2.4 属性请求

```
# 请求属性
Topic: v1/devices/me/attributes/request/{requestId}
Payload: {"clientKeys": ["key1", "key2"], "sharedKeys": ["sharedKey1"]}

# 响应属性
Topic: v1/devices/me/attributes/response/{requestId}
Payload: {"key1": "value1", "key2": "value2"}
```

## 3. WebSocket接口

### 3.1 连接URL

```
# 设备连接
ws://thingsboard:8080/api/ws/plugins/telemetry?token={deviceToken}

# 用户连接
ws://thingsboard:8080/api/ws/plugins/telemetry?token={userToken}
```

### 3.2 消息格式

#### 3.2.1 订阅消息

```json
{
  "deviceId": "device_001",
  "keys": ["temperature", "humidity"],
  "startTs": 1640995200000,
  "endTs": 1641081600000,
  "interval": 1000,
  "limit": 100,
  "agg": "AVG"
}
```

#### 3.2.2 实时数据推送

```json
{
  "subscriptionId": 1,
  "errorCode": 0,
  "errorMsg": null,
  "data": {
    "temperature": 25.5,
    "humidity": 60.2,
    "ts": 1640995200000
  }
}
```

#### 3.2.3 命令下发

```json
{
  "deviceId": "device_001",
  "command": "setTemperature",
  "params": {
    "temperature": 26.0
  }
}
```

## 4. CoAP接口

### 4.1 设备连接

```
# 设备认证
POST /api/v1/{deviceToken}/telemetry
POST /api/v1/{deviceToken}/attributes
GET  /api/v1/{deviceToken}/attributes
```

### 4.2 数据上报

```
# 遥测数据
POST /api/v1/{deviceToken}/telemetry
Content-Type: application/json
Payload: {"temperature": 25.5, "humidity": 60.2}

# 属性数据
POST /api/v1/{deviceToken}/attributes
Content-Type: application/json
Payload: {"firmware_version": "1.0.0"}
```

## 5. HTTP接口

### 5.1 设备数据接口

```
# 遥测数据上报
POST /api/v1/{deviceToken}/telemetry
Content-Type: application/json
Payload: {"temperature": 25.5, "humidity": 60.2}

# 属性上报
POST /api/v1/{deviceToken}/attributes
Content-Type: application/json
Payload: {"firmware_version": "1.0.0"}

# 获取属性
GET /api/v1/{deviceToken}/attributes
```

### 5.2 命令接口

```
# 获取命令
GET /api/v1/{deviceToken}/commands?timeout={timeout}

# 响应命令
POST /api/v1/{deviceToken}/commands/{commandId}
Content-Type: application/json
Payload: {"status": "SUCCESS", "data": {...}}
```

## 6. 接口设计特点

### 6.1 统一认证

- **JWT Token**: REST API和WebSocket使用JWT Token认证
- **设备Token**: MQTT、CoAP、HTTP设备接口使用设备Token认证
- **Token刷新**: 支持Token自动刷新机制

### 6.2 数据格式

- **JSON格式**: 所有接口使用JSON作为数据交换格式
- **时间戳**: 使用毫秒级时间戳
- **错误码**: 统一的错误码和错误信息格式

### 6.3 版本控制

- **URL版本**: 使用 `/api/v1/` 进行版本控制
- **向后兼容**: 新版本保持对旧版本的兼容性
- **版本弃用**: 提供版本弃用通知和迁移指南

### 6.4 权限控制

- **租户隔离**: 多租户数据隔离
- **角色权限**: 基于角色的权限控制
- **资源权限**: 细粒度资源权限控制

## 7. 最佳实践

### 7.1 设备连接

- 使用设备Token进行设备认证
- 实现断线重连机制
- 支持心跳保活

### 7.2 数据上报

- 批量上报减少网络开销
- 使用压缩减少数据传输量
- 实现数据缓存和重传机制

### 7.3 命令下发

- 使用唯一命令ID
- 实现命令超时处理
- 支持命令状态跟踪

### 7.4 错误处理

- 统一的错误码定义
- 详细的错误信息
- 错误重试机制

## 8. 参考资源

- [ThingsBoard官方文档](https://thingsboard.io/docs/)
- [ThingsBoard API参考](https://thingsboard.io/docs/reference/rest-api/)
- [ThingsBoard MQTT API](https://thingsboard.io/docs/reference/mqtt-api/)
- [ThingsBoard WebSocket API](https://thingsboard.io/docs/reference/websocket-api/)