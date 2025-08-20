### 开发计划（按阶段迭代，步步可编译）

- 第一阶段：最小可运行骨架
    - 范围:
        - 自动装配与属性
        - 最小 SecurityFilterChain（无状态、放行列表、统一401/403输出）
    - 产物:
        - `config.SecurityProperties`（prefix: `rose.security`，含
          enabled/basePath/permitAll/loginPath/logoutPath、token.*、protect.* 占位）
        - `config.SecurityAutoConfig`（启用属性并注册最小安全链）
        - `core.RestAuthenticationEntryPoint`、`core.RestAccessDeniedHandler`
        - `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
    - 验证:
        - 在模块目录执行：`mvn -q -DskipTests package`

- 第二阶段：基础认证能力（内存用户 + 密码校验）
    - 范围:
        - `AuthenticationManager`/`PasswordEncoder`
        - 默认 `InMemoryUserDetailsService`（仅用于开发）
    - 产物:
        - `core.AuthenticationBeans`（暴露 `AuthenticationManager`、`PasswordEncoder`）
        - `user.InMemoryUserDetailsService`（可被自定义 Bean 覆盖）
    - 验证:
        - 编译通过

- 第三阶段：登录（用户名/密码）与不透明 Token 签发
    - 范围:
        - `LoginAuthenticationFilter`（POST `login-path`，JSON username/password）
        - Token SPI 与本地实现（Caffeine）
    - 产物:
        - `token.TokenService`、`token.TokenStore`、`token.TokenGenerator`
        - `token.OpaqueTokenService`（type=LOCAL，TTL、并发限制占位）
        - `model.AuthRequest`、`model.AuthResponse`
        - `web.LoginAuthenticationFilter`（认证成功返回 token JSON）
    - 验证:
        - 编译通过

- 第四阶段：Token 校验过滤器与上下文认证
    - 范围:
        - `TokenAuthenticationFilter` 从 Header 取 Token → 认证上下文
        - 受保护路径默认必须持有有效 Token
    - 产物:
        - `web.TokenAuthenticationFilter`
        - 安全链中注册顺序：放行 → 登录过滤器 → Token 过滤器
    - 验证:
        - 编译通过

- 第五阶段：登出与主动下线
    - 范围:
        - `logout-path` 清除当前 Token
        - 提供服务方法手动撤销 Token
    - 产物:
        - `web.LogoutHandler`
        - `service.SessionKicker`（`TokenService.revoke(token)`）
    - 验证:
        - 编译通过

- 第六阶段：认证事件（事件优先 + 最小 SPI）
    - 范围:
        - 使用 Spring/Spring Security 内置事件
        - 增补领域事件（仅必要的）：`TokenIssuedEvent`、`TokenRevokedEvent`、`ConcurrentKickedEvent`、`ManualKickedEvent`
    - 产物:
        - `event.*`（领域事件定义与发布点）
        - 在登录成功/失败、登出、撤销、并发挤下线等位置发布事件
    - 验证:
        - 编译通过

- 第七阶段：账号安全（最小）
    - 范围:
        - 密码策略（Passay，开关、最小长度、混合字符、数字、特殊字符）
        - 登录失败计数与锁定（内存实现；Redis 作为可选增强，待后）
    - 产物:
        - `password.PasswordPolicyValidator`
        - `account.LoginFailureCounter`、`account.LoginLockoutService`
    - 验证:
        - 编译通过

- 第八阶段：安全防护（访问名单简化版）
    - 范围:
        - Access List 仅支持维度 IP/username
        - 组合策略仅 ANY/ALL（默认 ANY）
        - 存储 MEMORY/REDIS（定时刷新 + Caffeine 本地缓存）
    - 产物:
        - `protect.AccessListStore`（Memory/Redis）
        - `protect.AccessListService`（缓存+刷新）
        - `protect.AccessListMatcher`（ANY/ALL）
        - `web.AccessListFilter`（位于 Token 过滤器之前）
    - 验证:
        - 编译通过

- 第九阶段：防重放与时间窗（可选开启）
    - 范围:
        - 简单 Nonce + 时间窗，依赖 Caffeine 或 Redis
    - 产物:
        - `protect.ReplayProtection`、`web.ReplayFilter`
    - 验证:
        - 编译通过

- 第十阶段：限流（滑动窗口，默认关闭）
    - 范围:
        - 每窗口限次，基于 Caffeine 或 Redis
    - 产物:
        - `protect.RateLimiter`、`web.RateLimitFilter`
    - 验证:
        - 编译通过

- 第十一阶段：JWT 模块（默认禁用）
    - 范围:
        - 仅在 `rose.security.jwt.enabled=true` 且存在 `nimbus-jose-jwt` 时装配
        - 支持 HS256（先），再扩展 RS/ES、JWK/Keystore、时钟偏移与声明校验
        - `token.type=JWT` 时，用 `JwtTokenService` 替换 `OpaqueTokenService`
    - 产物:
        - `token.jwt.JwtTokenService`、`token.jwt.KeyProvider`
        - 自动装配条件：`@ConditionalOnClass` + `@ConditionalOnProperty`
    - 验证:
        - 编译通过（业务侧显式引入 nimbus 依赖时）

- 第十二阶段：MFA（默认关闭）
    - 范围:
        - SPI 设计与登录流程嵌入（密码通过后判断是否触发）
    - 产物:
        - `mfa.MfaProvider`、`mfa.MfaDecision`、`mfa.MfaService`
    - 验证:
        - 编译通过

- 第十三阶段：OAuth2 Client（默认关闭）
    - 范围:
        - 存在 `spring-security-oauth2-client` 且开启时装配
        - 成功后颁发平台 Token
    - 产物:
        - `oauth2.*` 适配器（成功处理器接入 `TokenService`）
    - 验证:
        - 编译通过（业务侧显式引入依赖时）

### 实施约束与准则

- 可选能力严格条件装配：`@ConditionalOnClass` + `@ConditionalOnProperty`
- 允许业务覆盖：默认实现统一 `@ConditionalOnMissingBean`
- 安全默认值：JWT 默认禁用；Access List 默认 ANY；仅从 Header 读取 Token
- 构建命令（每步执行）：
    - 在模块目录：`mvn -q -DskipTests package`

- 回归清单（每阶段完成后快速验证）
    - 可编译、能启动、放行路径有效、受保护路径返回 401
    - 登录成功/失败、退出、Token 有效性
    - Access List 开启后对 IP/username 的允许/拒绝
    - 可选模块在未引依赖时不装配，且不影响编译
