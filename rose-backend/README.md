# Rose Backend

多租户 SaaS 平台后端（Java 21 + Spring Boot 3.5）。本目录聚合了基础组件（Core、Starter）与领域服务（Billing、IAM、Notification），支持国际化、字段加密、数据权限、租户隔离、审计日志、认证授权、Web
基础能力等。

- 代码根仓库：`rose-monolithic`
- 当前目录：`rose-backend`
- 运行时依赖：MySQL/Redis/RabbitMQ（可通过 docker-compose 快速启动）

## 目录结构（概要）

- `rose/`
    - `rose-core/`：通用模型、工具集、异常体系等
    - `rose-annotation-processor/`：注解处理器
- `rose-spring/`：Spring 上下文与通用工具（过滤器、脱敏、表达式等）
- `rose-i18n/`：国际化核心与 Spring/Cloud/OpenFeign 集成及 Actuator 端点
- `rose-encryption/`：字段加密核心与 Spring Boot Starter（支持密钥轮转）
- `rose-notification/`：通知核心与 Starter（模板、发送、重试、可观测）
- `rose-mybatis/`：MyBatis Plus 核心与 Starter（审计、租户、数据权限、加密拦截）
- `rose-redis/`：Redis Starter（分布式锁、限流）
- `rose-spring-boot/`：Web/Auth/Audit 等通用 Starter
- `rose-service/`：业务服务聚合
    - `rose-application/`：组合启动应用（本地运行入口）
    - `rose-billing-service/`：计费域（支付/退款/对账/Outbox）
    - `rose-iam-api/`：IAM 对外 API 契约（DTO/VO）
    - `rose-iam-service/`：身份与权限域
    - `rose-notification-service/`：通知域
- `sql/`：示例 DDL（如 `billing-schema.sql`、`iam-schema.sql`）
- `docker-compose.yml`：本地依赖编排（可配合 `.env.example`）

## 快速开始

1) 克隆项目并进入目录

```bash
git clone https://github.com/chensoul/rose-monolithic.git
cd rose-monolithic/rose-backend
```

2) 启动依赖（MySQL/Redis/RabbitMQ）

```bash
docker compose up -d
```

3) 构建 & 运行（Java 21, Maven 3.9+）

```bash
mvn -v
mvn clean package -DskipTests
cd rose-service/rose-application
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

4) 打开文档

- Swagger UI（Web Starter 提供）：`http://localhost:8080/swagger-ui/index.html`
- Actuator：`http://localhost:8080/actuator`

## 关键能力

- 国际化（`rose-i18n`）：多源消息、热加载、缓存、Actuator 端点
- 字段加密（`rose-encryption`）：`@EncryptField`、密钥轮转、Hash 服务
- 数据访问（`rose-mybatis`）：审计字段填充、租户隔离、数据权限、字段加密拦截
- 分布式能力（`rose-redis`）：分布式锁 `@Lock`、限流 `@RateLimited`
- 审计与安全（`rose-spring-boot`）：审计日志、统一异常/响应、CORS、异步、Swagger、Auth/JWT/OAuth2
- 计费域（`rose-billing-service`）：支付策略（支付宝/微信/Stripe）、退款回调、Outbox、对账 Job、报表
- 通知域（`rose-notification-service`）：模板渲染（变量/Groovy）、发送通道（短信/邮件/控制台）、重试与幂等
- 身份域（`rose-iam-service`）：用户/组织/角色/租户基础模型与 MyBatis 实体/Mapper

## 构建与质量

- JDK：17+，Maven：3.9+
- 依赖管理：Spring Boot 3.5.x Parent + Spring Cloud 2025.0.0 + MyBatis Plus 3.5.12
- 插件建议：
    - `spotless:apply` 统一格式
    - `jacoco:report` 覆盖率
    - `sonar:sonar` 质量门禁（可选）

### Maven 插件使用命令

以下命令均在 `rose-backend/` 目录执行，推荐加 `-Pquality` 启用质量配置：

- 统一质量校验（格式化+静态分析+测试+覆盖率）

```bash
mvn clean verify -Pquality
```

- 仅代码格式化（Spotless）

```bash
mvn spotless:apply -Pquality
# 只检查不修复
mvn spotless:check -Pquality
```
- 覆盖率报告（JaCoCo）

```bash
mvn test -Pquality && mvn jacoco:report -Pquality
```

- 代码质量平台（Sonar，可选）

```bash
mvn -Pquality sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=YOUR_TOKEN
```

- 跳过某些插件（需要时使用）

```bash
mvn clean verify -Pquality \
  -Dspotless.skip=true \
  -Djacoco.skip=true \
  -Dsonar.skip=true
```

- 性能优化参数（可选）

```bash
# 并行构建与不更新快照
mvn -T 1C -nsu clean verify -Pquality
```

## 配置与环境

- 全局配置：`src/main/resources/application.yml`（具体见各模块）
- 业务应用：`rose-service/rose-application/src/main/resources/application.yaml`
- 示例 SQL：`sql/`
- 本地依赖：`docker-compose.yml`（配合 `.env.example`）

## 贡献指南

- 提交规范：Conventional Commits
- 分支策略：`feature/*`、`fix/*`、`hotfix/*`
- 代码风格：遵循本仓库 Java/Spring 规范与模块化边界

## License

MIT
