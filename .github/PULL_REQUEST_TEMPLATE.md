## 变更类型
- [ ] feat 新功能
- [ ] fix 修复问题
- [ ] refactor 重构
- [ ] perf 性能优化
- [ ] test 测试相关
- [ ] docs 文档
- [ ] build/ci 构建/CI
- [ ] chore 其他

## 变更说明（必填）
- 描述问题与解决方案，涉及模块与影响范围

## 质量自查清单（提交前逐项勾选）
- [ ] 事务边界：仅在 Service 层；读方法 `@Transactional(readOnly = true)`，写方法 `@Transactional(rollbackFor = Exception.class)`；未在事务内发起远程调用
- [ ] 控制器瘦身：无业务逻辑；请求/响应均为 DTO；Jakarta Validation 校验完整
- [ ] 统一响应：成功返回 `ApiResponse<T>`；异常经 `@RestControllerAdvice` 处理
- [ ] MyBatis-Plus：未使用注解 SQL/XML；分页使用 `Page<?>`；已注册拦截器：分页/乐观锁/防全表更新删除
- [ ] Redis：统一序列化（StringRedisSerializer + GenericJackson2JsonRedisSerializer）；TTL 通过配置，含 5%-10% 抖动；Lettuce 连接超时/连接池已配置
- [ ] OpenFeign：为客户端设置 connect/read 超时；客户端实现（OkHttp/Apache HC5）与连接池参数已配置；GET 重试受限且有退避；熔断/隔离/降级路径已考虑
- [ ] 可观测性：Micrometer + Actuator 指标暴露；日志透传 MDC/traceId；如启用虚拟线程已验证 MDC 透传
- [ ] MySQL 5.7：索引/SQL 性能检查；避免 SELECT *；必要字段 NOT NULL；字符集/时区一致
- [ ] 安全与合规：未打印敏感信息；无硬编码凭据；参数化查询防 SQL 注入
- [ ] 测试：新增/修改部分已覆盖单元或集成测试；核心模块覆盖率未下降；数据库/Redis 测试优先使用 Testcontainers
- [ ] CI 门禁：本地已通过 `mvn spotless:apply`、`mvn -q -DskipTests=false test`、`mvn -q -Pquality verify`；启用 Maven Enforcer 与 OWASP Dependency-Check（高危 CVE 构建失败）

## 配置变更
- [ ] 更新了 application.yaml（分页/缓存 TTL/Feign 超时/Actuator 等）
- [ ] 更新了文档（README/配置说明）

## 风险与回滚
- 风险点：
- 回滚方案：
