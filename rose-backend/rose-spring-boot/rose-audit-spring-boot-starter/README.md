# Rose Audit Spring Boot Starter

Rose 审计日志 Spring Boot Starter，提供完整的审计日志功能。

## 功能特性

- 🔍 **完整审计**：支持 HTTP 请求、业务操作、数据变更、安全事件等全方位审计
- 🔒 **安全保护**：内置加密、脱敏功能，保护敏感数据
- 🚀 **高性能**：异步处理、批量存储，不影响业务性能
- 🏢 **多租户**：原生支持多租户环境
- 📊 **灵活存储**：支持数据库、文件、消息队列等多种存储方式
- 🎯 **注解驱动**：通过 `@Audit` 注解轻松实现自动审计
- 📈 **可扩展**：模块化设计，支持自定义扩展

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
    enabled: true
    storage:
      type: database
      async: true
    encryption:
      enabled: true
    masking:
      enabled: true
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