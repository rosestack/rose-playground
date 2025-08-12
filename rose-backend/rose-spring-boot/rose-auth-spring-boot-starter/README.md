# Rose Auth Spring Boot Starter

一个专为前后端分离项目设计的认证授权 Spring Boot Starter 模块。基于 Spring Security 6.x 最新版本，提供完整的用户认证、权限控制和安全防护功能。

## 🚀 快速开始

### 1. 添加依赖

```xml

<dependency>
    <groupId>io.github.rosestack</groupId>
    <artifactId>rose-auth-spring-boot-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 配置文件

在 `application.yml` 中添加配置：

```yaml
rose:
  auth:
    # 是否启用认证模块
    enabled: true

    # JWT 配置
    jwt:
      # JWT 密钥（生产环境请修改）
      secret: ${JWT_SECRET:your-secret-key}
      # 访问令牌过期时间（1小时）
      access-token-expiration: PT1H
      # 刷新令牌过期时间（7天）
      refresh-token-expiration: P7D

    # 安全配置
    security:
      # 最大登录尝试次数
      max-login-attempts: 5
      # 账户锁定时间（15分钟）
      lockout-duration: PT15M

      # 密码策略
      password:
        min-length: 8
        require-uppercase: true
        require-lowercase: true
        require-digits: true

    # CORS 配置
    cors:
      allowed-origins:
        - http://localhost:3000
        - http://localhost:8080
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
        - OPTIONS
```

### 3. 启用认证

在主应用类上添加注解（可选，自动配置会自动启用）：

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 📋 功能特性

### ✅ 已实现功能

- **基础配置框架**
    - Spring Boot 自动配置
    - 配置属性绑定
    - 多环境配置支持

- **安全配置**
    - Spring Security 基础配置
    - CORS 跨域配置
    - 密码编码器配置
    - 无状态会话管理

- **配置属性**
    - JWT 配置
    - OAuth2 客户端配置
    - 安全策略配置
    - 缓存配置
    - CORS 配置

### 🚧 待实现功能

- **JWT 认证**
    - JWT Token 生成和验证
    - 认证过滤器
    - Token 刷新机制
    - Token 黑名单管理

- **OAuth2 客户端**
    - 第三方登录集成
    - 用户信息映射
    - 登录成功处理

- **权限控制**
    - RBAC 权限模型
    - 方法级权限注解
    - 动态权限加载

- **安全防护**
    - 登录失败限制
    - 请求频率限制
    - 设备管理

## 🔧 配置说明

### JWT 配置

```yaml
rose:
  auth:
    jwt:
      secret: your-secret-key              # JWT 密钥
      access-token-expiration: PT1H        # 访问令牌过期时间
      refresh-token-expiration: P7D        # 刷新令牌过期时间
      issuer: rose-auth                    # JWT 发行者
      audience: rose-app                   # JWT 受众
      token-prefix: "Bearer "              # 令牌前缀
      header-name: Authorization           # 请求头名称
```

### OAuth2 配置

```yaml
rose:
  auth:
    oauth2:
      enabled: true                        # 是否启用 OAuth2
      success-redirect-url: /              # 登录成功重定向URL
      failure-redirect-url: /login?error   # 登录失败重定向URL
      clients:
        github:
          client-id: ${GITHUB_CLIENT_ID}
          client-secret: ${GITHUB_CLIENT_SECRET}
          scope: user:email
```

### 安全策略配置

```yaml
rose:
  auth:
    security:
      max-login-attempts: 5                # 最大登录尝试次数
      lockout-duration: PT15M              # 账户锁定时间
      lockout-strategy: IP_AND_USER        # 锁定策略
      enable-device-tracking: true         # 是否启用设备跟踪

      password:
        min-length: 8                      # 密码最小长度
        require-uppercase: true            # 是否需要大写字母
        require-lowercase: true            # 是否需要小写字母
        require-digits: true               # 是否需要数字
        require-special-chars: true        # 是否需要特殊字符
```

## 📁 项目结构

```
src/main/java/io/github/rose/auth/
├── config/                     # 配置类
│   ├── AuthAutoConfiguration.java
│   ├── JwtConfiguration.java
│   ├── OAuth2ClientConfiguration.java
│   └── SecurityConfiguration.java
├── jwt/                        # JWT 处理（待实现）
├── oauth2/                     # OAuth2 客户端（待实现）
├── security/                   # 安全组件（待实现）
└── properties/                 # 配置属性
    └── AuthProperties.java
```

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

- 项目链接: [https://github.com/chensoul/rose-monolithic](https://github.com/chensoul/rose-monolithic)
- 问题反馈: [Issues](https://github.com/chensoul/rose-monolithic/issues)
