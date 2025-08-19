---
alwaysApply: true
---

# 后端技术栈质量清单（Spring Boot / MyBatis Plus / Redis / OpenFeign）

你是该项目的后端质量守门人，请在开发、评审与上线前，逐项对照本清单进行自检，未达标项需在合并前修复或形成改进任务。

## 1. Spring Boot 质量清单

### A. 配置管理清单

- [ ] application.yaml 按环境分层；敏感配置通过环境变量/密钥管理器注入；禁写入仓库与日志；业务参数优先用配置（如 Redis
  TTL），不在代码中硬编码。
- [ ] 所有自定义配置绑定到 @ConfigurationProperties，使用 @Validated 进行约束校验，提供默认值。
- [ ] 禁止零散 @Value 注入；统一前缀分组，文档化配置项与默认值。

### B. 依赖注入与组件可见性

- [ ] 统一使用构造器注入，依赖字段 final；禁止字段/Setter 注入。
- [ ] 组件默认 package-private，可见性最小化；仅暴露必要 Bean。
- [ ] Prototype/Request/Session Scope 使用受控；禁止在多线程持有非单例 Bean。

### C. 事务边界

- [ ] 读操作显式 @Transactional(readOnly = true)；写操作 @Transactional(rollbackFor = Exception.class)。
- [ ] 事务范围最小化；禁止在单事务内发起远程调用；必要时采用补偿/Saga。
- [ ] 只在 Service 层定义事务；Controller/DAO 层不直接控制事务；避免事务内远程调用。

### D. Web 层一致性

- [ ] Controller 保持瘦身：无业务逻辑；DTO + Jakarta Validation。
- [ ] 全局异常处理（@RestControllerAdvice）+ 统一响应模型 ApiResponse<T>。
- [ ] 分页统一使用 MyBatis-Plus 的 `Page` 模型：Controller/Service 入参采用 `Page<?>`对象，返回 `Page<T>`；禁止直接返回实体。
- [ ] 推荐启用 Swagger（springdoc）文档。

### E. 安全与运维

- [ ] Actuator 仅开放 /health /info /metrics；其余需鉴权。
- [ ] CORS/限流/防重放/CSRF 策略已实现与测试；输入输出安全编码。
- [ ] 优雅停机、Readiness/Liveness 探针配置完备并通过演练。
- [ ] JSON 仅使用 Jackson；禁用 fastjson/gson。

### F. 日志与可观测性

- [ ] 使用 SLF4J + Logback；避免敏感信息；调试日志包装 isDebugEnabled。
- [ ] TraceId 贯穿；为关键操作添加业务埋点、指标与告警阈值。

### G. 测试与门禁

- [ ] 单元/集成/契约测试覆盖率符合项目阈值；使用 Testcontainers 启动真实依赖。
- [ ] 构建中开启 Sonar（SonarQube/SonarCloud）、Spotless、JaCoCo，违规即失败；不降低阈值。

### H. 版本与依赖管理

- [ ] 使用父 POM/BOM 锁定版本：Spring Boot 3.5.x、Spring Cloud 2025.0.x、MyBatis-Plus 3.5.12+。
- [ ] 根 POM 统一 properties 管理版本（如 spring-boot.version、spring-cloud.version、mybatis-plus.version）。
- [ ] 建立依赖升级机制：优先使用 Renovate（或月度人工巡检），禁止长期滞后。

### I. 本地依赖与编排

- [ ] 仓库根提供 docker-compose.yaml 与 .env.example，包含 MySQL/Redis/RabbitMQ 的默认配置与持久化卷。
- [ ] 禁止提交生产凭据；敏感变量通过环境注入。

## 2. MyBatis Plus 质量清单

适用范围：rose-backend 持久层（Mapper/Service/Entity/XML）。用于开发自检、CR 审查与 CI 门禁。

### A. 实体与映射

- [ ] @TableName/@TableId/@TableField 映射完整；字段命名与数据库下划线一致（自动驼峰转换确认）。
- [ ] 逻辑删除（@TableLogic）、乐观锁（@Version）、审计字段（createTime/updateTime）策略已配置并生效。
- [ ] MyBatis-Plus 自动填充（MetaObjectHandler）用于审计字段；避免在业务代码手工维护时间。
- [ ] 严禁实体包含业务逻辑；禁止循环/双向膨胀关联；关联通过明确查询实现。

### B. Mapper 与 XML

- [ ] 严禁使用注解式 SQL（@Select/@Update/@Delete/@Insert 等）；统一使用 MyBatis-Plus 提供的方法与 Wrapper 条件构造。
- [ ] 严禁编写 Mapper XML 与动态 SQL；保持持久层简单、可维护、与数据库弱绑定。

### C. 查询与分页

- [ ] 分页统一使用 MyBatis-Plus 分页插件；禁止手写 limit 偏移造成全表扫描。（自 v3.5.9 起需引入 `mybatis-plus-jsqlparser`
  依赖以启用 `PaginationInnerInterceptor`）。
- [ ] 条件构造统一使用 LambdaQueryWrapper/LambdaUpdateWrapper；避免字符串拼 SQL 与注入风险。
- [ ] 逻辑删除已开启时，严禁在条件构造中手动添加 deleted/is_deleted 等逻辑删除字段条件；框架自动处理。
- [ ] 大结果集采用游标/流式处理或分片拉取；查询超时与最大行数限制已配置。

### D. 写入与批处理

- [ ] 批量写入使用 saveBatch()/ExecutorType.BATCH；控制批大小（如 500-1000）与事务边界；避免 OOM。
- [ ] 插入/更新仅写必要字段，避免全字段更新；更新语句 set 列表使用 <set> 自动去空。
- [ ] 唯一约束冲突有幂等处理（如 upsert/先查后插/分布式锁）。

### E. 事务与一致性

- [ ] Service 层方法具备明确事务注解；跨资源使用最终一致性/补偿方案；禁止在事务内远程调用。
- [ ] 主键生成策略统一（雪花/序列/DB 自增）；时区与时序一致性校验（UTC 存储）。

### F. 性能与监控

- [ ] 启用 MyBatis 日志/慢 SQL 监控；对慢查询建立告警与基线（TP90/TP99）。
- [ ] HikariCP 连接池参数依据负载与 DB 限额配置；池耗尽保护与指标上报到可观测平台。

## 3. Redis 质量清单

适用范围：Spring Data Redis/Redisson 在缓存、锁、消息等场景的使用。

### A. Key 设计与序列化

- [ ] Key 规范：{domain}:{resource}:{id}（例如 user:profile:123）；避免长 key 与高基数前缀。
- [ ] 统一序列化：StringRedisSerializer + Jackson2JsonRedisSerializer；禁用 JDK 序列化。
- [ ] JSON 序列化字段显式版本控制（@JsonTypeInfo 禁用）；兼容性升级策略明确。

### B. TTL 与容量管理

- [ ] 所有缓存写入设置 TTL，默认不超过 7 天；不同类型数据有独立 TTL 策略与随机抖动；TTL 应通过 `application.yaml` 配置（如
  `spring.cache.redis.time-to-live`），禁止在代码中硬编码。
- [ ] 内存水位监控与淘汰策略配置（建议 allkeys-lru/lfu）；超阈值报警。
- [ ] 禁止在生产路径使用 KEYS/SCAN 大范围遍历；后台工具类库进行维护操作。

### C. 缓存三难问题防护

- [ ] 雪崩：TTL 加随机、预热热点、分散重建；必要时使用多级缓存（Caffeine + Redis）。
- [ ] 穿透：对不存在的 key 缓存空值（短 TTL），或引入布隆过滤器。
- [ ] 击穿：热点 key 使用互斥锁/逻辑过期防抖；异步重建与过期兜底。

### D. 分布式锁与并发

- [ ] 分布式锁使用 Redisson 或原子 set nx px + 校验释放；超时时间与业务耗时匹配；避免死锁。
- [ ] 锁粒度与范围可控；不可重入场景显式限制；失败与重试策略明确。

### E. 一致性与回源策略

- [ ] DB 写 -> 删缓存 -> 最终一致性修复（Binlog/消息）；避免先删后写的竞态。
- [ ] 双写或订阅机制具备幂等与去重；异常场景有回滚或重建策略。

### F. 监控与告警

- [ ] 指标：命中率、QPS、慢查询、失败率、连接池、命令超时；可视化与告警。
- [ ] 异常：连接超时、反序列化失败、内存不足等都应有明确日志与 retry/backoff。

## 4. OpenFeign 质量清单

适用范围：OpenFeign + Spring Cloud CircuitBreaker/Resilience4j 调用外部/内部服务。

### A. 客户端超时与重试

- [ ] 为每个 FeignClient 设置连接/读超时（例如 500ms/2s，视业务调整）；禁用全局无限超时。
- [ ] 重试仅用于幂等 GET/HEAD；限制重试次数与退避策略；对 POST/PUT 默认不重试。

### B. 熔断、隔离与降级

- [ ] 使用 Resilience4j/Spring Cloud CircuitBreaker：配置超时、舱壁隔离、熔断窗口与阈值。
- [ ] 为关键接口提供降级实现或兜底响应；降级路径记录可观测性指标与告警。

### C. 负载均衡与路由

- [ ] 使用 Spring Cloud LoadBalancer；超时与重试策略与服务注册信息一致；避免跨区域高延迟调用。
- [ ] 对多区域/多版本服务实施基于标头/标签的路由策略；灰度/金丝雀具备回滚预案。

### D. API 契约与错误处理

- [ ] DTO 与错误码契约化，不泄露内部模型；兼容性按最小可用原则演进（新增字段向后兼容）。
- [ ] HTTP 状态码与业务错误码映射明确；对 4xx/5xx 分类处理并有清晰日志上下文。
- [ ] 使用 ErrorDecoder 统一解析远端异常；调用侧转译为业务异常并带上 traceId。

### E. 安全与合规

- [ ] 认证凭据通过安全渠道注入（Header/Token）；不在日志中打印敏感数据；最小权限访问下游。
- [ ] 幂等保障：对外部接口在调用侧引入幂等键/防重复提交；对支付/订单等关键接口重点校验。

### F. 测试与可观测性

- [ ] 契约/集成测试覆盖：正常/超时/熔断/降级/重试/限流等路径；使用 WireMock/MockWebServer。
- [ ] 指标：成功率、延迟分位（TP90/TP99）、熔断打开率、重试次数、降级次数；异常比率告警。

## 5. MySQL 数据库质量清单

适用范围：MySQL 5.7 + InnoDB 存储引擎，数据库设计与性能优化。

### A. 表与字段设计

- [ ] 表名使用英文小写+下划线，单数形式（如 user 非 users）；避免 MySQL 保留字；每个表有主键。
- [ ] 字段命名简洁明了，使用英文小写+下划线；避免冗余前缀；主键统一 id；时间字段 created_time/updated_time。
- [ ] 字符集统一 utf8mb4；排序规则统一 utf8mb4_general_ci；根据数据范围选择合适类型（TINYINT/SMALLINT/INT/BIGINT）；金额字段使用
  DECIMAL。
- [ ] 重要字段 NOT NULL 约束；表和字段有 COMMENT 注释；遵循第三范式避免数据冗余。
- [ ] 运行参数：binlog_format=ROW，sql_mode=STRICT_TRANS_TABLES；统一时区 UTC；隔离级别 REPEATABLE READ。

### B. 索引设计与优化

- [ ] 索引命名规范：idx_表名_字段名（单字段）、idx_表名_字段1_字段2（复合）、uk_表名_字段名（唯一）。
- [ ] 复合索引字段顺序考虑查询频率与选择性；避免创建重复索引。
- [ ] 禁用外键约束，但保留外键列与相应索引以保障查询性能与数据治理。
- [ ] 查询覆盖索引使用；长字段前缀索引；避免 SELECT *；WHERE/ORDER BY/GROUP BY 字段有索引支撑。

### C. SQL 性能与规范

- [ ] 慢 SQL 监控与优化：查询时间超过 1s 告警；使用 EXPLAIN 分析执行计划；避免全表扫描。
- [ ] 禁止在 WHERE 中使用函数、隐式类型转换、前缀模糊查询；合理使用 LIMIT 分页。
- [ ] 批量操作控制大小（如 500-1000 行）；大表 DDL 变更使用在线工具（gh-ost/pt-online-schema-change）。

### D. 事务与并发控制

- [ ] 事务范围最小化；避免长事务锁表；读写分离场景注意主从延迟一致性。
- [ ] 死锁检测与重试机制；高并发场景使用乐观锁（版本号）或分布式锁。
- [ ] 事务隔离级别与业务场景匹配；避免幻读与不可重复读问题。

### E. 容量规划与监控

- [ ] 表容量预估与分片策略（垂直/水平）；单表行数控制在合理范围（建议 < 500w）。
- [ ] 监控指标：QPS/TPS、慢查询数量、连接数、锁等待、主从延迟；设置告警阈值。
- [ ] 定期备份与恢复演练；binlog 保留策略；数据归档与清理机制。

### F. 安全与合规

- [ ] 数据库权限最小化；敏感字段加密存储；访问审计日志完备。
- [ ] SQL 注入防护；输入参数化查询；避免动态 SQL 拼接。
- [ ] 生产环境禁止 DDL 直接操作；变更通过工单与审核流程。

## 6. 质量门禁与集成

- 静态检查：Sonar、Spotless（统一格式）已在 CI 启用；失败即阻断。
- 单元/集成/契约测试覆盖率门槛：核心模块 80%+，边缘模块 60%+；新增代码不得降低全局阈值。
- 性能基线：关键接口具备基准压测报告（TP90/TP99、错误率、CPU/内存），上线前回归。
- 文档与变更：面向用户/API 的变更已在文档与变更日志中更新；运维手册同步。
