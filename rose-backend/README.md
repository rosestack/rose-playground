# rose



## DDD（领域驱动设计）规范

### 1. 分层架构

DDD 推荐分为四层，每层职责清晰、依赖单向：

1. 用户接口层（Interfaces/Application Layer）

- 负责接收外部请求（如 REST API、消息、UI），参数校验、权限校验、DTO 转换。

- 只做“用例”编排，不包含领域规则。

- 依赖应用服务。

2. 应用层（Application Layer）

- 负责业务流程编排（如参数校验、事务、调用领域服务/仓储、事件发布等）。

- 不包含领域规则，只协调领域对象、领域服务、仓储等。

- 依赖领域服务、领域仓储接口。

3. 领域层（Domain Layer）

- 业务核心，包含领域模型（聚合根、实体、值对象）、领域服务、领域事件、仓储接口。

- 领域服务只处理无法归属到聚合根/实体/值对象的方法。

- 领域模型不依赖任何基础设施/ORM 框架。

4. 基础设施层（Infrastructure Layer）

- 提供技术实现，如数据库、消息、第三方服务、持久化、类型转换等。

- 只依赖领域层的接口，不反向依赖领域层。

### 2. 领域建模

- 聚合根（Aggregate Root）：领域内的主对象，负责聚合内数据一致性（如 Notification）。

- 实体（Entity）：有唯一标识的对象（如 User）。

- 值对象（Value Object）：无唯一标识、不可变（如 Money、TimeWindow）。

- 领域服务（Domain Service）：无法归属到聚合根/实体/值对象的方法（如限流、黑名单、优先级）。

- 仓储接口（Repository）：只定义聚合根的持久化操作（如 NotificationRepository）。

- 领域事件（Domain Event）：领域内的重要事件（如 NotificationSentEvent）。

### 3. 依赖与解耦

- 领域层不依赖任何基础设施、ORM、框架。

- 基础设施层实现领域层的接口（如 RepositoryImpl），并做 PO/Entity <-> Domain 的转换。

- 应用层只依赖领域服务、仓储接口，不直接依赖基础设施实现。

- 用户接口层只依赖应用服务。

### 4. 代码结构推荐

```bash
src/main/java/com/example/app/
  ├── application/
  │   ├── service/         # 应用服务
  │   └── event/           # 应用层事件与监听
  ├── domain/
  │   ├── model/           # 领域模型（聚合根、实体、值对象）
  │   ├── repository/      # 仓储接口
  │   ├── service/         # 领域服务
  │   ├── value/           # 值对象、枚举
  ├── infra/
  │   ├── entity/          # 持久化对象（PO/Entity/DO）
  │   ├── convert/         # PO/Entity <-> Domain 转换器
  │   ├── repository/      # 仓储实现
  │   ├── typehandler/     # 类型转换器
  │   └── acl/             # 外部系统适配层
  ├── interfaces/
  │   └── controller/      # API 层
```

### 5. 领域服务与应用服务

- 应用服务：负责业务流程编排、用例实现，调用领域服务和仓储接口。

- 领域服务：只处理领域规则，无法归属到聚合根/实体/值对象的方法。

- 仓储接口：只定义聚合根的持久化操作，复杂查询可用 QueryService/ReadModel。

### 6. 事件驱动

- 领域事件用于解耦聚合根与外部副作用（如发送通知、积分变更等）。

- 应用服务/基础设施监听领域事件，异步处理副作用。

### 7. 其他规范

- 领域模型无任何 ORM 注解。

- 持久化对象（PO/Entity）只在 infra/entity。

- 转换器负责 PO <-> Domain 的转换。

- 领域服务、仓储接口、事件等均在 domain 层。

- 只在需要时引入领域服务，简单 CRUD 直接用仓储接口。

- 领域对象、服务、仓储接口均应有单元测试。

- 事件、ACL、补偿、监控等横切关注点可独立分包。

### 8. 典型调用链

```
Controller → ApplicationService → (DomainService, DomainRepository) → DomainModel
```

### 9. 领域驱动设计的目标

- 让业务核心与技术实现彻底解耦

- 让业务语言和代码结构高度一致

- 让系统可扩展、可测试、可演进

