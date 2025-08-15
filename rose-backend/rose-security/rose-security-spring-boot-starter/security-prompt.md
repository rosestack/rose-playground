我需要开发一个名为 `rose-security-spring-boot-starter` 的 Spring Boot Starter 模块，实现完整的认证与授权功能。请按照以下详细规范进行开发：

**项目结构要求：**

- 在当前工作目录下创建 `rose-security-spring-boot-starter` 模块
- 使用标准的 Maven 项目结构
- 包名前缀：`io.github.rosestack.spring.boot.security`

**技术栈约束：**

- Spring Boot 3.x
- Spring Security 6+
- Java 17+
- 面向 Servlet 堆栈（Spring MVC）
- 支持前后端分离架构
- 无状态认证（基于 token）

**功能实现优先级（按顺序开发）：**

1. **基础认证模块** (`rose.security.*`)
    - 集成 Spring Security 拦截器
    - 可配置登录端点（默认 `/api/auth/login`）
    - 可配置登出端点（默认 `/api/auth/logout`）
    - 可配置刷新端点（默认 `/api/auth/refresh`）
    - 可配置基础路径（默认 `/api/**`）
    - 可配置放行路径
    - 用户名/密码认证
    - 默认提供基于内存的 UserDetailsService 实现
    - 短 token 生成与管理
    - Token 超时配置
    - Token 并发控制
    - 主动下线功能
    - 可选 Redis 分布式存储

2. **扩展机制模块** (`rose.security.extension.*`)
    - SPI 接口定义
    - 认证流程钩子（登录前后、成功失败）
    - 审计事件接口
    - 日志 Hook 机制

3. **账号安全模块** (`rose.security.account.*`)
    - 密码复杂度策略
    - 密码历史记录
    - 密码过期策略
    - 登录失败锁定
    - 防暴力破解
    - 可插拔验证码机制

4. **JWT 模块** (`rose.security.jwt.*`)
    - 支持 HS256/RS256/ES256 算法
    - JWK/Keystore 密钥管理
    - 密钥轮换机制
    - 时钟偏移校正
    - 标准声明校验（exp/iat/nbf/aud/iss/sub）
    - 自定义 Claim 映射

5**多因子认证模块** (`rose.security.mfa.*`)
    - TOTP 参考实现
    - MFA SPI 接口

6. **安全防护模块** (`rose.security.protection.*`)
    - CORS 配置
    - IP 白/黑名单
    - 速率限制
    - 防重放攻击
    - 时间窗校验

7. **OAuth2 Client 模块** (`rose.security.oauth2.*`)
    - OAuth2 客户端登录
    - 多提供商支持

**配置规范：**

- 配置前缀：`rose.security.*`
- 子模块配置示例：
  - `rose.security.token.enabled=true`
  - `rose.security.token.jwt.enabled=true`
  - `rose.security.mfa.enabled=false`
  - `rose.security.oauth2.enabled=true`
- 每个模块都有独立的开关控制
- 支持通过 Spring Bean 替换默认实现

**数据存储抽象：**

- 不强绑定特定数据源
- 支持 Redis/数据库/内存存储
- 由使用方选择和配置存储方案

**安全默认值：**

- 提供生产级安全默认配置
- 遵循 "Secure by default" 原则
- 允许完全自定义覆盖

**边界说明：**

- 专注认证与鉴权框架能力
- 不包含用户、角色、权限等具体数据模型
- 不实现授权服务器
- 使用方需实现 `UserDetailsService`

**交付要求：**

1. 完整的项目结构和 Maven 配置
2. 核心代码实现（确保可编译通过）
3. 配置类和属性定义
4. 使用示例和文档
5. 单元测试（基础覆盖）

请按照上述优先级顺序逐步实现，每完成一个模块后确认代码可以编译通过再继续下一个模块。