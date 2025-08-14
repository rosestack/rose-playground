我想开发一个Spring Boot Starter，实现完整的认证与授权功能模块。具体需求如下：

**核心功能要求：**
1. 基础认证：用户名/密码登录，集成 Spring Security 过滤器链与自定义 AuthenticationProvider
2. 密码与账号安全：密码复杂度/历史/过期策略，登录失败锁定、防爆破与验证码（可插拔）
3. 用户生命周期：提供事件与接口（参考样例），不内置注册/找回/重置/资料修改/模板；具体流程由使用方实现
4. 会话管理：默认无状态（stateless API）；可选开启会话：会话超时、并发会话控制、Remember-Me、主动下线（踢出），分布式会话（可选 Redis）
5. Token 与密钥：仅面向 JWT（与 OAuth2 无关）。支持 HS256/RS256/ES256 等算法的签名验证，JWK/Keystore 密钥装载与轮换，时钟偏移校正（clock skew），标准声明校验（exp/iat/nbf/aud/iss/sub），自定义 Claim 到 GrantedAuthority/Scope 的映射；提供可选的 Token 撤销/黑名单 SPI；不负责令牌的颁发/刷新。
6. 第三方登录与 SSO：后台仅作为资源服务器/OAuth2 Client；前端负责跳转与授权流程；不内置各家 IdP Connector/SDK，SAML2/企业单点为可选集成样例
7. 多因子认证（MFA）：提供 TOTP 参考实现与 SPI；短信/邮件验证码由使用方接入网关，WebAuthn/FIDO2 为可选扩展
8. 授权模型：仅提供基于 Spring Security 的权限校验与注解支持；不内置用户-角色-权限数据模型，权限由使用方通过 UserDetailsService 提供
9. 访问控制：提供 URL/方法级权限注解与表达式，数据行/列级权限不内置实现，仅提供扩展点
10. 服务到服务认证：API Key/HMAC/Client Credentials，密钥管理与轮换
11. 安全防护：CSRF/CORS、IP 白/黑名单、速率限制与节流、防重放与时间窗校验
12. 审计与合规：提供审计事件与日志 Hook；不内置审计存储/检索 UI/留存策略，实现由使用方决定
13. 可观测性：认证成功率、延迟等指标，结构化日志与 Trace 上报
14. 配置与扩展：Starter 自动配置、配置属性映射；SPI 扩展认证流与事件钩子（登录/注册前后）、Webhooks
15. 国际化：多语言与时区支持，消息资源可自定义
16. 性能与伸缩：缓存策略、冷/热点配置、无共享横向扩展与压测基线


**技术实现要求：**
- 基于 Spring Boot 3.x / Spring Security 6+
- 面向前后端分离的后端 API：默认无状态、基于 JWT 的认证；仅聚焦 Servlet 堆栈（Spring MVC），WebFlux 可选
- 不实现授权服务器；聚焦认证/鉴权与 JWT 验证能力
- 提供自动配置与配置属性（前缀建议：rose.auth.*）
- 核心扩展点 SPI：UserDetailsService、AuthenticationProvider、PasswordEncoder、AuthenticationEventPublisher、PermissionEvaluator、AccessDecisionVoter、MethodSecurityExpressionHandler、MfaProvider、TokenRevocationStore、AuditEventPublisher；可选 TenantResolver
- JWT 能力：HS256/RS256/ES256、JWK/Keystore 装载与轮换、clock skew 校正、标准声明校验（exp/iat/nbf/aud/iss/sub）、Claim→GrantedAuthority/Scope 映射
- 会话能力：默认 stateless；可选开启会话：会话超时、并发控制、Remember-Me；分布式会话（可选 Redis）
- 安全响应与异常：统一异常与错误码；AuthenticationEntryPoint/AccessDeniedHandler 定制
- 可观测性：Micrometer 指标、结构化日志、Trace 上报
- 示例与文档：提供最小示例与使用文档；示例中的登录/注册仅作演示，不作为 Starter 内置功能

**配置灵活性：**
- 配置前缀：rose.security.*（示例：rose.security.jwt.*, rose.security.oauth2.*）
- 开关化控制：各模块可独立启用/禁用（如 jwt.enabled, oauth2.enabled, mfa.enabled, cors.enabled）
- 可替换实现：UserDetailsService、AuthenticationProvider、PermissionEvaluator、MfaProvider、TokenRevocationStore、AuditEventPublisher 等均可通过 Spring Bean 覆盖
- 策略可配置：密码策略、会话并发、限流/节流、跨域、CSRF、安全头等
- 数据存储抽象：Starter 不强绑定数据源，Redis/DB/内存由使用方选择并配置
- 国际化与消息：支持自定义 MessageSource 与时区
- 扩展钩子：登录/注册/鉴权前后事件 Hook、Webhooks（如需）
- 生产建议：提供安全默认值（Secure by default），但允许完全自定义

**范围边界说明：**
- 本 Starter 聚焦认证与鉴权框架能力，不内置具体用户、角色、权限、组织、多租户等数据模型与隔离策略
- 本 Starter 不实现授权服务器
- 使用方需实现 UserDetailsService（或 ReactiveUserDetailsService），并按需提供 GrantedAuthority/Scope 等权限信息
- 如需组织/层级/行列级数据权限，请在业务侧实现并通过扩展点（自定义 Filter、AccessDecisionVoter、PermissionEvaluator、MethodSecurityExpressionHandler 等）接入

请提供完整的项目结构、核心代码实现、配置示例和使用说明。