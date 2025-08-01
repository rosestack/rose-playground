# Rose Audit Spring Boot Starter

Rose 审计日志 Spring Boot Starter，提供完整的审计日志功能。经过优化重构，专注于核心功能，提供更好的性能和安全性。

## 功能特性

- 🔍 **完整审计**：支持 HTTP 请求、业务操作、数据变更、安全事件等全方位审计
- 🔒 **安全保护**：增强的加密、脱敏功能，自动检测和保护敏感数据
- 🚀 **高性能**：优化的异步处理、智能批量存储，性能监控和慢查询检测
- 🏢 **多租户**：原生支持多租户环境
- 📊 **数据库存储**：专注于数据库存储，简化配置，提高稳定性
- 🎯 **注解驱动**：通过 `@Audit` 注解轻松实现自动审计
- 📈 **可扩展**：模块化设计，支持自定义扩展
- 🛡️ **数据完整性**：SHA-256 哈希值和数字签名确保数据完整性

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.rosestack</groupId>
    <artifactId>rose-audit-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 配置

```yaml
rose:
  audit:
    # 是否启用审计日志功能
    enabled: true

    # 敏感字段脱敏配置
    mask-fields: ["password", "token", "secret", "key"]

    # 存储配置
    storage:
      type: database
      async: true
      batch-size: 50
      batch-interval: 5000

    # 数据保留配置
    retention:
      days: 365
      auto-cleanup: true
      cleanup-cron: "0 0 2 * * ?"

    # 事件过滤配置
    filter:
      ignore-users: ["system", "admin"]
      ignore-ips: ["127.0.0.1", "::1"]
      ignore-uri-patterns: ["/health/**", "/actuator/**", "/favicon.ico"]
      min-risk-level: LOW
```

### 3. 使用注解

```java
@RestController
public class UserController {
    
    @Audit(eventType = "数据", eventSubtype = "数据创建", operationName = "创建用户")
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.create(user);
    }
}
```

## 详细文档

更多详细信息请参考：
- [设计文档](../../../docs/audit-log-design.md)
- [配置说明](docs/configuration.md)
- [使用指南](docs/usage.md)

## 开发状态

🚧 **开发中** - 当前版本为开发版本，功能正在逐步完善中。

## 许可证

本项目采用 MIT 许可证。