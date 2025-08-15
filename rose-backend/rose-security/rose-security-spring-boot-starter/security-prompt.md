# rose-security-spring-boot-starter 开发提示词（权威版）

本提示词用于驱动在 Spring Boot 3.x/Spring Security 6+ 下开发一个名为「rose-security-spring-boot-starter」的安全 Starter，聚焦无状态认证与鉴权能力，提供生产级默认值并支持高度可扩展。

---
## 1. 目标与边界
- 目标：提供基于 Token 的无状态认证、可插拔的安全扩展机制与生产级默认值，适配前后端分离架构。
- 边界：
  - 不提供用户、角色、权限等具体数据模型；
  - 不实现授权服务器；
  - 默认内置内存型 UserDetailsService 仅用于开发测试；
  - 使用方可自定义并替换任何默认实现。

## 2. 技术栈与总体约束
- Java 17+、Spring Boot 3.x、Spring Security 6+、Servlet (Spring MVC)。
- 包名前缀：`io.github.rosestack.spring.boot.security`。
- 参考实现：https://github.com/thingsboard/thingsboard（参考设计理念，也可以参考代码）。
- 配置前缀统一：`rose.security.*`，模块化开关控制，支持通过 Spring Bean 覆盖默认实现。
- 扩展策略：优先使用 Spring/Spring Security 内置事件与处理器；确有能力缺口时再补充自定义“领域事件”；仅当无法通过处理器/Provider 表达的同步决策点，才考虑引入“点状 Hook”，不提供通用全能 Hook。

## 3. 开发顺序与里程碑（必须逐步编译通过）
1) 基础认证模块 → 2) 扩展机制模块（事件优先 + 最小SPI） → 3) 账号安全模块 → 4) 安全防护模块 → 5) JWT 模块 → 6) 多因子认证模块 → 7) OAuth2 Client 模块。
- 每完成一个模块：代码可编译、基础单元测试通过、默认配置可运行。

## 4. 项目与包结构
- 模块：`rose-security-spring-boot-starter`（Maven 标准结构）。
- 典型包：
  - config（自动装配与属性）
  - core（认证流程与处理器、安全过滤器、Token存储与服务、扩展点与回调）
  - account（密码策略、登录失败策略等）
  - protect（访问名单/白黑名单、限流、防重放等）
  - jwt（JWT 支持）
  - mfa（多因子认证）
  - oauth2（OAuth2 客户端）

说明：core 是可以启动运行的最基础的模块，其他模块都是在 core 基础上扩展的。

## 5. 关键设计与默认值
- 无状态认证：基于 HTTP Header 自定义 Token；
- 令牌模型：支持本地 Token 或 JWT；提供统一 TokenService SPI；可选 Redis 分布式存储。
- 默认路径：
  - 登录：`/api/auth/login`
  - 登出：`/api/auth/logout`
  - 基础保护路径：`/api/**`
  - 可配置匿名放行路径白名单。
- 安全默认：关闭无用端点、合理的 Token TTL、限流与时间窗校验默认开启但可配置。

## 6. 配置清单（示例 + 说明）

- 基础开关与路径
  - rose.security.enabled=true | 是否启用安全 Starter
  - rose.security.base-path=/api/** | 受保护的基础路径模式
  - rose.security.permit-all[0]=/api/auth/login | 放行路径列表（支持通配符）
  - rose.security.permit-all[1]=/actuator/health
  - rose.security.login-path=/api/auth/login
  - rose.security.logout-path=/api/auth/logout

- Token 设置
  - rose.security.token.enabled=true
  - rose.security.token.type=LOCAL | 可选 LOCAL、JWT
  - rose.security.token.header=X-Auth-Token | 读取的 Header
  - rose.security.token.ttl=PT2H | Token 过期时间 ISO-8601
  - rose.security.token.concurrent-limit=1 | 单用户并发 Token 数
  - rose.security.token.kickout-oldest=true | 超限时是否踢出最早 Token
  - rose.security.token.store=MEMORY | 可选 MEMORY、REDIS
  - rose.security.token.redis.key-prefix=rose:sec:token:

- 账号安全
  - rose.security.account.password-policy.enabled=true
  - rose.security.account.password-policy.min-length=12
  - rose.security.account.password-policy.require-mixed-case=true
  - rose.security.account.password-policy.require-digit=true
  - rose.security.account.password-policy.require-special=true
  - rose.security.account.password-history.size=5
  - rose.security.account.password-expire.ttl=P90D
  - rose.security.account.login-lock.max-failures=5
  - rose.security.account.login-lock.cooldown=PT15M
  - rose.security.account.captcha.enabled=false

- 安全防护
  - rose.security.protect.rate-limit.enabled=true
  - rose.security.protect.rate-limit.limit=100 | 每窗口最大请求数
  - rose.security.protect.rate-limit.window=PT1M
  - rose.security.protect.replay.enabled=true
  - rose.security.protect.replay.window=PT5M
  - rose.security.protect.clock-skew=PT30S
  
  - 访问名单（动态）
    - rose.security.protect.access-list.enabled=true | 是否启用访问名单
    - rose.security.protect.access-list.default-policy=deny | 默认策略：allow/deny
    - rose.security.protect.access-list.combine=ANY | 组合策略：ANY/ALL（简化版，默认 ANY）
    - rose.security.protect.access-list.store=REDIS | 存储方式：MEMORY/REDIS（简化版）
    - rose.security.protect.access-list.redis.key-prefix=rose:sec:access-list: | Redis Key 前缀
    - rose.security.protect.access-list.cache.enabled=true | 是否启用本地缓存
    - rose.security.protect.access-list.cache.ttl=PT5M | 本地缓存 TTL
    - rose.security.protect.access-list.refresh-interval=PT1M | 动态同步刷新间隔（从存储源拉取）
    - rose.security.protect.access-list.dimensions=ip,username | 启用的维度（简化版，仅 IP/用户名）

- JWT
  - rose.security.jwt.enabled=false
  - rose.security.jwt.alg=HS256 | 可选 HS256/RS256/ES256
  - rose.security.jwt.jwk-set-uri= | 远程 JWK（可选）
  - rose.security.jwt.keystore.location= | classpath:/keystore.jks（可选）
  - rose.security.jwt.keystore.password=
  - rose.security.jwt.key-id=
  - rose.security.jwt.rotate.enabled=false
  - rose.security.jwt.rotate.period=P30D
  - rose.security.jwt.validate.aud=
  - rose.security.jwt.validate.iss=
  - rose.security.jwt.validate.clock-skew=PT60S

- MFA
  - rose.security.mfa.enabled=false

- OAuth2 Client
  - rose.security.oauth2.enabled=false


## 7. 模块分解与实现要点（按优先级）

### 7.1 基础认证模块
- 目标：提供用户名/密码登录、登出、令牌颁发与校验，集成到 Spring Security 过滤链。
- 组件：
  - 配置属性类：RoseSecurityProperties（`rose.security.*`）
  - Web 安全自动配置：RoseSecurityAutoConfiguration
  - 过滤器：LoginAuthenticationFilter、TokenAuthenticationFilter
  - 认证入口点与失败处理器：RestAuthenticationEntryPoint、RestAccessDeniedHandler
  - Token SPI：TokenService、TokenStore、TokenGenerator
  - 默认实现：InMemoryUserDetailsService（仅开发测试）
- 行为：
  - POST {login-path} 接收 JSON：{"username":"","password":""}
  - 调用 AuthenticationManager 完成认证；成功后由 TokenService 生成令牌并返回
  - TokenAuthenticationFilter 从 Header 解析 Token → 校验 → 构建 Authentication
  - 登出：删除 Token、触发回调、返回 200

### 7.2 扩展机制模块（事件优先 + 最小 SPI）
- 优先使用 Spring/Spring Security 内置事件与处理器：
  - 认证事件：`AuthenticationSuccessEvent`、`AbstractAuthenticationFailureEvent` 及子类、`InteractiveAuthenticationSuccessEvent`
  - 授权事件（可选）：通过 `AuthorizationEventPublisher` 发布授权相关事件
  - 标准处理器：`AuthenticationSuccessHandler`、`AuthenticationFailureHandler`、`LogoutHandler`、`LogoutSuccessHandler`
- 自定义“领域事件”（用于补齐内置事件的审计/集成诉求）：
  - `TokenIssuedEvent`、`TokenRefreshedEvent`、`TokenRevokedEvent`
  - `ConcurrentKickedEvent`、`ManualKickedEvent`
  - `LoginLockedEvent`、`LoginUnlockedEvent`
  - `AccessListDeniedEvent`、`RateLimitHitEvent`、`ReplayDetectedEvent`
- 最小 SPI 清单（可被业务侧覆盖）：
  - Token：`TokenService`、`TokenStore`、`TokenGenerator`
  - 防护：`AccessGuard`、`RateLimiter`
  - 账号安全：`PasswordPolicyValidator`
- 说明：不提供通用的 `AuthProcessHook`。若出现“必须在认证流程中做同步决策且无法用处理器/Provider 表达”的明确场景，再新增“点状 Hook”。

### 7.3 账号安全模块
- 密码策略校验器 PasswordPolicyValidator；
- 密码历史记录 PasswordHistoryStore（MEMORY/REDIS 可选）；
- 密码过期 PasswordExpiryChecker；
- 登录失败计数与锁定 LoginFailureTracker + LoginLockoutService；
- 验证码 CaptchaValidator SPI。

### 7.4 安全防护模块
- 访问名单（Access List）匹配器（简化版）：支持 IP/用户名 维度；支持 ANY/ALL 组合策略；支持 Memory/Redis 存储；提供本地缓存与定时刷新能力（不含表达式、PRIORITY、维度组合与 DB 存储，后续增强）。
- 限流 RateLimiter（滑动窗口/令牌桶皆可，默认滑动窗口）；
- 防重放 ReplayProtection（Nonce + 时间窗）；
- 时间同步/偏移允许 ClockSkew。

### 7.5 JWT 模块
- 支持 HS256/RS256/ES256；JWK/Keystore 加载；
- JWTService：签发、解析、校验；与 TokenService 适配；
- 密钥轮换 KeyRotationService：按周期生成新 Key 并设置 kid；
- 校验标准声明：exp/iat/nbf/aud/iss/sub；时钟偏移容忍可调；
- 自定义 Claim 映射器 ClaimMapper SPI。

### 7.6 多因子认证（MFA）
- MFAChallengeService 与 MFAVerifier SPI（如 TOTP、短信、邮件等）；
- 登录流程挂钩，支持基于风险或策略触发二次校验；
- 令牌中嵌入 mfa=true/level。

### 7.7 OAuth2 Client 模块
- 集成 Spring Security OAuth2 Client；
- 多提供商配置映射，统一回调与用户绑定扩展点；
- 与 TokenService 打通，实现统一令牌下发。

## 8. 安全过滤链与时序

1) 请求进入 → 静态放行匹配（permitAll）→ 命中则直接通过。
2) 命中登录路径且 POST → LoginAuthenticationFilter 读取 JSON → 认证 → 触发回调 → 返回 Token。
3) 非登录请求：TokenAuthenticationFilter 从 Header 取 Token → 校验有效性/并发/黑名单 → 构造 SecurityContext。
4) 进入控制器 → 出错时走 RestAccessDeniedHandler/RestAuthenticationEntryPoint。
5) 登出路径：清理 Token、触发 onLogout。

注意：过滤器顺序必须在 SecurityFilterChain 中明确，避免与 Spring Security 默认过滤器冲突。

## 9. 统一响应与错误码建议

使用 ApiResponse 类

- 成功：{"code":0,"message":"ok","data":{...}}
- 认证失败：{"code":40101,"message":"bad credentials"}
- 未认证：{"code":40100,"message":"unauthorized"}
- 无权限：{"code":40300,"message":"forbidden"}
- 速率限制：{"code":42900,"message":"too many requests"}
- 请求重放：{"code":40010,"message":"replay detected"}
- 参数错误：{"code":40000,"message":"bad request"}

## 10. 单元测试范围（基础覆盖）
- SecurityAutoConfiguration 加载测试；
- 登录成功/失败用例（内存用户）；
- Token 生成、续期、并发限制与踢出策略；
- 放行路径、受保护路径访问测试；
- JWT 解析与标准声明校验（含 clock skew）；
- 限流与防重放测试。

## 11. 使用示例（最小可用）

- 依赖：
  <dependency>
    <groupId>io.github.rosestack</groupId>
    <artifactId>rose-security-spring-boot-starter</artifactId>
    <version>${latest}</version>
  </dependency>
  <!-- 如需启用 JWT、OAuth2 Client 或 Redis，请在业务侧显式引入对应依赖 -->
  <!-- JWT: -->
  <dependency>
    <groupId>com.nimbusds</groupId>
    <artifactId>nimbus-jose-jwt</artifactId>
    <version>9.37.3</version>
  </dependency>
  <!-- OAuth2 Client: -->
  <dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-client</artifactId>
  </dependency>
  <!-- Redis: -->
  <dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
  </dependency>

- application.yml（演示核心开关，访问名单数据不在配置文件中维护）：
  rose:
    security:
      enabled: true
      protect:
        access-list:
          enabled: true
          store: REDIS
          refresh-interval: PT1M
      token:
        type: LOCAL
        ttl: PT2H
      permit-all:
        - /api/auth/login
        - /actuator/health

- Redis 示例数据（仅用于说明，实际由后台管理/配置中心写入）：
  - SET rose:sec:access-list:allow:ip "192.168.1.0/24"
  - SADD rose:sec:access-list:deny:username "guest" "banned_*"

- 自定义用户服务（可选）：声明 UserDetailsService Bean 覆盖默认内存实现。

## 12. 代码生成与实现要求

- 访问名单（Access List）
  - 属性类：RoseSecurityProperties.Protection.AccessList（enabled、defaultPolicy、combine、store、cache.enabled、cache.ttl、refreshInterval、dimensions、redis.keyPrefix）；
    - 说明：YAML 使用短横线命名（如 `redis.key-prefix`、`cache.ttl`），属性类对应驼峰（`redis.keyPrefix`、`cacheTtl`）。
  - SPI：AccessListStore（接口，定义查询 allow/deny 列表与表达式的契约，按维度返回；支持分页/增量）
  - 默认实现：
    - MemoryAccessListStore（启动时加载一份内存副本，适合开发/测试）；
    - RedisAccessListStore（生产推荐，Hash/Set 结构，支持前缀与批量扫描）；
    - JdbcAccessListStore（JDBC，适合已有 DB 表）
  - 运行时组件：AccessListService（带本地缓存与定时刷新），AccessListMatcher（支持 ANY/ALL 组合策略）。
  - 表设计建议（如后续引入 DB 存储）：
    - access_list_rule（id, dimension[IP|USERNAME], type[ALLOW|DENY], pattern, enabled, updated_at）
  - Redis Key 约定：
    - {prefix}allow:ip, {prefix}deny:ip（Set：CIDR 或 IP）
    - {prefix}allow:username, {prefix}deny:username（Set：通配符/正则）
    - （设备维度、组合维度与表达式 Key 后续增强时再引入）
  - 刷新策略：
    - 启动立即全量拉取，之后按 refresh-interval 增量拉取（updated_at > last_sync）
    - 支持 Pub/Sub 主动失效：收到频道消息后清本地缓存并立即拉取
  - 缓存：Caffeine 本地缓存；
  - 线程安全：使用 ReadWriteLock 保证切换/刷新期间的匹配一致性。
- 每个模块完成后必须保证可编译；
- 自动配置类需添加 `spring.factories`/`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`；
- 属性类需加 `@ConfigurationProperties(prefix="rose.security")` 并在自动配置中启用；
- 遵循「安全优先」默认值；
- 扩展采用“标准处理器/Provider 替换 + 事件监听”为主，自定义“领域事件”用于审计与外部集成；
- 默认不提供通用 Hook，仅在确有必要的同步决策点引入极小范围的点状 Hook；
- 所有可替换点定义为公共 SPI 接口，默认实现通过 `@ConditionalOnMissingBean` 提供。
- 自动装配条件：
  - 依赖存在（`@ConditionalOnClass`）且开关开启（`@ConditionalOnProperty`）时才装配 JWT、OAuth2 Client、Redis 等可选模块。

## 13. 验收清单
- [ ] Maven 结构与 GAV 正确
- [ ] 属性与自动配置生效
- [ ] 登录/登出/Token 校验能力可用
- [ ] 扩展点、审计事件可触达
- [ ] 账号安全与防护默认开启并可配置
- [ ] JWT/MFA/OAuth2 Client 可按开关独立启用
- [ ] 基础单测覆盖通过
