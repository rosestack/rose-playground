# 多租户SaaS平台 - Rose

## 项目概述
Rose是一个基于领域驱动设计(DDD)的多租户SaaS平台，采用前后端分离架构，提供企业级应用开发框架和核心功能模块。

## 核心功能
- 多租户管理
- 身份认证与访问控制(IAM)
- 通知服务
- 国际化支持
- 安全防护

## 技术架构
### 后端技术栈
- Spring Boot
- DDD分层架构
- 事件驱动
- 模块化设计

### 前端技术栈
(待补充)

## 项目结构
```
rose-monolithic/
├── rose-backend/          # 后端项目
│   ├── rose-core/         # 核心业务模块
│   ├── rose-iam/          # 身份认证模块
│   ├── rose-notification/ # 通知服务
│   ├── rose-security/     # 安全模块
│   └── rose-server/       # 主应用入口
└── rose-frontend/         # 前端项目(待分析)
```

## 开发规范
### DDD分层架构
1. 用户接口层(Interfaces)
2. 应用层(Application)
3. 领域层(Domain)
4. 基础设施层(Infrastructure)

### 领域建模原则
- 聚合根负责数据一致性
- 实体与值对象明确区分
- 领域服务处理复杂业务逻辑
- 仓储接口定义持久化操作

## 快速开始
1. 后端启动:
```bash
cd rose-backend
mvn spring-boot:run
```

2. 前端启动:
(待补充)

## 开发计划
详见[多租户SaaS平台开发计划.md](多租户SaaS平台开发计划.md)

## 功能清单
详见[多租户SaaS平台功能清单.md](多租户SaaS平台功能清单.md)
