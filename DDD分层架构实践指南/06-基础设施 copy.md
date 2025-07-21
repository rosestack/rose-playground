## 3. 基础设施配置

### 3.1 数据库配置

#### 3.1.1 MySQL 配置

**application.yml 配置：**

> **配置说明：**
> - **字符集**：使用 `utf8mb4` 支持完整的 Unicode 字符集，包括 Emoji
> - **超时设置**：包含连接超时和Socket超时配置
> - **连接池**：HikariCP 核心配置，适合大多数应用场景
> - **批量优化**：启用 `rewriteBatchedStatements` 提升批量操作性能

```yaml
spring:
  datasource:
    # MySQL 8.0 驱动
    driver-class-name: com.mysql.cj.jdbc.Driver
    # 数据库连接URL - 核心必要参数
    url: jdbc:mysql://localhost:3306/rose_db?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&connectTimeout=10000&socketTimeout=30000&rewriteBatchedStatements=true
    # 数据库用户名 - 支持环境变量覆盖
    username: ${DB_USERNAME:root}
    # 数据库密码 - 支持环境变量覆盖
    password: ${DB_PASSWORD:password}

    # HikariCP 连接池配置
    hikari:
      # 连接池名称
      pool-name: HikariCP-DDD-Demo
      # 最小空闲连接数
      minimum-idle: 5
      # 最大连接池大小
      maximum-pool-size: 20
      # 连接超时时间 (30秒)
      connection-timeout: 30000
      # 空闲连接超时时间 (10分钟)
      idle-timeout: 600000
      # 连接最大生命周期 (30分钟)
      max-lifetime: 1800000
      # 连接有效性检测查询
      connection-test-query: SELECT 1
      # 开启连接泄漏检测 (1分钟)
      leak-detection-threshold: 60000
```

**不同环境的配置调整：**

```yaml
# application-dev.yml - 开发环境
spring:
  datasource:
    hikari:
      # 开发环境使用较小的连接池
      minimum-idle: 2
      maximum-pool-size: 10
      # 开启连接泄漏检测 (1分钟)
      leak-detection-threshold: 60000

---
# application-test.yml - 测试环境
spring:
  datasource:
    hikari:
      # 测试环境中等连接池
      minimum-idle: 3
      maximum-pool-size: 15
      # 较短的连接生命周期
      max-lifetime: 900000

---
# application-prod.yml - 生产环境
spring:
  datasource:
    hikari:
      # 生产环境大连接池
      minimum-idle: 10
      maximum-pool-size: 50
      # 关闭连接泄漏检测以提高性能
      leak-detection-threshold: 0
      # 更长的连接生命周期
      max-lifetime: 3600000
```

#### 3.1.2 MyBatis Plus 配置

**YAML配置：**

```yaml
# MyBatis-Plus配置
mybatis-plus:
  configuration:
    # 开启驼峰命名转换
    map-underscore-to-camel-case: true
    # 开启二级缓存
    cache-enabled: true
    # 延迟加载
    lazy-loading-enabled: true
    aggressive-lazy-loading: false
    # 开启结果集映射
    auto-mapping-behavior: partial
    # 日志实现
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

  global-config:
    db-config:
      # 主键类型
      id-type: assign_id
      # 逻辑删除字段
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      # 字段验证策略
      insert-strategy: not_null
      update-strategy: not_null
      select-strategy: not_empty

    # 元数据处理器
    meta-object-handler: com.example.app.infrastructure.config.CustomMetaObjectHandler
  
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.example.app.**.infrastructure.persistence
```

**Java配置类：**

```java
/**
 * MyBatis Plus 配置类
 */
@Configuration
@MapperScan("com.example.ddddemo.infrastructure.persistence.mapper")
@EnableTransactionManagement
@Slf4j
public class MyBatisPlusConfig {

    /**
     * 配置分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        // 动态表名插件
        interceptor.addInnerInterceptor(new DynamicTableNameInnerInterceptor());
        
        return interceptor;
    }

    /**
     * 配置SQL性能分析插件
     */
    @Bean
    @Profile({"dev", "test"})
    public SqlPerformanceInterceptor sqlPerformanceInterceptor() {
        SqlPerformanceInterceptor interceptor = new SqlPerformanceInterceptor();
        interceptor.setMaxTime(1000); // 超过1秒记录警告
        return interceptor;
    }
}

/**
 * 自定义元数据处理器
 */
@Component
@Slf4j
public class CustomMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始执行插入填充...");
        
        // 设置创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        
        // 设置更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 设置创建人
        this.strictInsertFill(metaObject, "createBy", String.class, getCurrentUsername());
        
        // 设置更新人
        this.strictInsertFill(metaObject, "updateBy", String.class, getCurrentUsername());
        
        // 设置逻辑删除字段
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始执行更新填充...");
        
        // 设置更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 设置更新人
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUsername());
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        try {
            // 从当前请求中获取用户名
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // 方式1: 从请求头获取用户名
                String username = request.getHeader("X-User-Name");
                if (StringUtils.hasText(username)) {
                    return username;
                }
                
                // 方式2: 从请求参数获取用户名
                username = request.getParameter("username");
                if (StringUtils.hasText(username)) {
                    return username;
                }
                
                // 方式3: 从JWT Token中解析用户名
                username = extractUsernameFromToken(request);
                if (StringUtils.hasText(username)) {
                    return username;
                }
                
                // 方式4: 从Session中获取用户名
                HttpSession session = request.getSession(false);
                if (session != null) {
                    Object sessionUser = session.getAttribute("currentUser");
                    if (sessionUser != null) {
                        return sessionUser.toString();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户名失败", e);
        }
        
        // 默认返回系统用户
        return "system";
    }

    /**
     * 从JWT Token中提取用户名
     */
    private String extractUsernameFromToken(HttpServletRequest request) {
        try {
            // 从Authorization头获取token
            String authHeader = request.getHeader("Authorization");
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                // 解析JWT Token（这里需要根据你的JWT实现来调整）
                return JwtUtils.extractUsername(token);
            }
            
            // 从Cookie中获取token
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("token".equals(cookie.getName())) {
                        return JwtUtils.extractUsername(cookie.getValue());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("从Token中提取用户名失败", e);
        }
        return null;
    }
}

/**
 * 请求上下文工具类
 */
@Component
@Slf4j
public class RequestContextUtils {

    /**
     * 获取当前请求
     */
    public static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.warn("获取当前请求失败", e);
            return null;
        }
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "system";
        }

        // 按优先级获取用户名
        String username = getUsernameFromHeader(request);
        if (StringUtils.hasText(username)) {
            return username;
        }

        username = getUsernameFromParameter(request);
        if (StringUtils.hasText(username)) {
            return username;
        }

        username = getUsernameFromSession(request);
        if (StringUtils.hasText(username)) {
            return username;
        }

        return "system";
    }

    /**
     * 从请求头获取用户名
     */
    private static String getUsernameFromHeader(HttpServletRequest request) {
        return request.getHeader("X-User-Name");
    }

    /**
     * 从请求参数获取用户名
     */
    private static String getUsernameFromParameter(HttpServletRequest request) {
        return request.getParameter("username");
    }

    /**
     * 从Session获取用户名
     */
    private static String getUsernameFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object user = session.getAttribute("currentUser");
            return user != null ? user.toString() : null;
        }
        return null;
    }
}

/**
 * SQL性能分析插件
 */
@Slf4j
public class SqlPerformanceInterceptor implements Interceptor {

    private long maxTime = 1000; // 默认1秒

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = invocation.proceed();
            long endTime = System.currentTimeMillis();
            long sqlTime = endTime - startTime;
            
            if (sqlTime > maxTime) {
                log.warn("SQL执行时间过长: {}ms, SQL: {}", sqlTime, 
                    getSql(invocation));
            } else {
                log.debug("SQL执行时间: {}ms, SQL: {}", sqlTime, 
                    getSql(invocation));
            }
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("SQL执行异常: {}ms, SQL: {}, Error: {}", 
                endTime - startTime, getSql(invocation), e.getMessage());
            throw e;
        }
    }

    private String getSql(Invocation invocation) {
        if (invocation.getArgs().length > 0 && 
            invocation.getArgs()[0] instanceof MappedStatement) {
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            return mappedStatement.getId();
        }
        return "Unknown SQL";
    }
}

/**
 * 动态表名插件
 */
@Slf4j
public class DynamicTableNameInnerInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                           RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        
        // 处理动态表名
        String sql = boundSql.getSql();
        if (sql.contains("${tableName}")) {
            String tableName = getTableName();
            sql = sql.replace("${tableName}", tableName);
            log.debug("动态替换表名: {}", tableName);
        }
    }

    private String getTableName() {
        // 根据业务逻辑获取表名，例如多租户场景
        String tenantId = getCurrentTenantId();
        return "user_" + tenantId;
    }

    private String getCurrentTenantId() {
        // 从上下文获取租户ID
        return "default";
    }
}
```

### 3.2 Redis 配置

**YAML配置：**

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
      timeout: 6000ms
      lettuce:
        pool:
          max-active: 8
          max-wait: -1ms
          max-idle: 8
          min-idle: 0
      # 集群配置（可选）
      cluster:
        nodes: ${REDIS_CLUSTER_NODES:}
        max-redirects: 3
      # Sentinel配置（可选）
      sentinel:
        master: ${REDIS_SENTINEL_MASTER:}
        nodes: ${REDIS_SENTINEL_NODES:}
```

**Java配置类：**

```java
/**
 * Redis 配置类
 */
@Configuration
@EnableCaching
@Slf4j
public class RedisConfig {
    /**
     * 配置Redis模板
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 设置key序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // 设置value序列化器
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, 
            ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        jsonSerializer.setObjectMapper(objectMapper);
        
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置String Redis模板
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    /**
     * 配置Redis监听器
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // 配置监听器
        container.addMessageListener(new CacheEvictionListener(), 
            new ChannelTopic("cache:eviction"));
        
        return container;
    }

    /**
     * 配置Redis操作工具类
     */
    @Bean
    public RedisUtils redisUtils(RedisTemplate<String, Object> redisTemplate) {
        return new RedisUtils(redisTemplate);
    }
}

/**
 * Redis操作工具类
 */
@Component
@Slf4j
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis设置缓存失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    /**
     * 设置缓存并设置过期时间
     */
    public void set(String key, Object value, Duration timeout) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout);
        } catch (Exception e) {
            log.error("Redis设置缓存失败: key={}, timeout={}, error={}", 
                key, timeout, e.getMessage(), e);
        }
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis获取缓存失败: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis删除缓存失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量删除缓存
     */
    public Long delete(Collection<String> keys) {
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("Redis批量删除缓存失败: keys={}, error={}", keys, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, Duration timeout) {
        try {
            return redisTemplate.expire(key, timeout);
        } catch (Exception e) {
            log.error("Redis设置过期时间失败: key={}, timeout={}, error={}", 
                key, timeout, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取过期时间
     */
    public Duration getExpire(String key) {
        try {
            return redisTemplate.getExpire(key);
        } catch (Exception e) {
            log.error("Redis获取过期时间失败: key={}, error={}", key, e.getMessage(), e);
            return Duration.ZERO;
        }
    }

    /**
     * 判断key是否存在
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis判断key是否存在失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 递增
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis递增失败: key={}, delta={}, error={}", 
                key, delta, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 递减
     */
    public Long decrement(String key, long delta) {
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("Redis递减失败: key={}, delta={}, error={}", 
                key, delta, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Hash操作 - 设置
     */
    public void hSet(String key, String hashKey, Object value) {
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
        } catch (Exception e) {
            log.error("Redis Hash设置失败: key={}, hashKey={}, error={}", 
                key, hashKey, e.getMessage(), e);
        }
    }

    /**
     * Hash操作 - 获取
     */
    public Object hGet(String key, String hashKey) {
        try {
            return redisTemplate.opsForHash().get(key, hashKey);
        } catch (Exception e) {
            log.error("Redis Hash获取失败: key={}, hashKey={}, error={}", 
                key, hashKey, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Hash操作 - 获取所有
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Redis Hash获取所有失败: key={}, error={}", key, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * List操作 - 左推入
     */
    public Long lPush(String key, Object value) {
        try {
            return redisTemplate.opsForList().leftPush(key, value);
        } catch (Exception e) {
            log.error("Redis List左推入失败: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * List操作 - 右弹出
     */
    public Object rPop(String key) {
        try {
            return redisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            log.error("Redis List右弹出失败: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Set操作 - 添加
     */
    public Long sAdd(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("Redis Set添加失败: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Set操作 - 判断是否包含
     */
    public Boolean sIsMember(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error("Redis Set判断包含失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * ZSet操作 - 添加
     */
    public Boolean zAdd(String key, Object value, double score) {
        try {
            return redisTemplate.opsForZSet().add(key, value, score);
        } catch (Exception e) {
            log.error("Redis ZSet添加失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * ZSet操作 - 获取分数
     */
    public Double zScore(String key, Object value) {
        try {
            return redisTemplate.opsForZSet().score(key, value);
        } catch (Exception e) {
            log.error("Redis ZSet获取分数失败: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }
}

/**
 * 缓存失效监听器
 */
@Component
@Slf4j
public class CacheEvictionListener implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());
            
            log.info("收到缓存失效消息: channel={}, body={}", channel, body);
            
            // 处理缓存失效逻辑
            handleCacheEviction(body);
            
        } catch (Exception e) {
            log.error("处理缓存失效消息失败", e);
        }
    }

    private void handleCacheEviction(String cacheKey) {
        // 根据缓存key执行相应的失效处理逻辑
        if (cacheKey.startsWith("user:")) {
            // 用户缓存失效处理
            log.info("用户缓存失效: {}", cacheKey);
        } else if (cacheKey.startsWith("order:")) {
            // 订单缓存失效处理
            log.info("订单缓存失效: {}", cacheKey);
        }
    }
}

/**
 * 缓存注解配置
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置缓存键生成器
     */
    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName());
            sb.append(":");
            sb.append(method.getName());
            for (Object param : params) {
                sb.append(":");
                sb.append(param.toString());
            }
            return sb.toString();
        };
    }

    /**
     * 配置缓存解析器
     */
    @Bean
    public CacheResolver cacheResolver(CacheManager cacheManager) {
        return new SimpleCacheResolver(cacheManager);
    }
}
```

### 3.3 日志配置

**application.yml 日志配置：**

```yaml
# 日志配置
logging:
  # 日志级别配置
  level:
    root: INFO
    # 应用包日志级别
    com.example.ddddemo: DEBUG
    # SQL日志 - 开发环境开启
    com.example.ddddemo.infrastructure.persistence.mapper: DEBUG
    # MyBatis Plus日志
    com.baomidou.mybatisplus: DEBUG
    # HikariCP连接池日志
    com.zaxxer.hikari: INFO
    # Spring框架日志
    org.springframework: INFO
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
    # 第三方库日志
    org.apache.http: INFO
    redis.clients.jedis: INFO

  # 日志输出格式
  pattern:
    # 控制台输出格式
    console: "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx"
    # 文件输出格式
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} %5p ${PID:- } --- [%t] %-40.40logger{39} : %m%n"

  # 日志文件配置
  file:
    # 日志文件路径
    name: logs/ddd-demo.log
    # 日志文件最大大小
    max-size: 100MB
    # 日志文件最大历史数量
    max-history: 30
    # 日志文件总大小限制
    total-size-cap: 1GB
```

**不同环境的日志配置：**

```yaml
# application-dev.yml - 开发环境
logging:
  level:
    root: DEBUG
    com.example.ddddemo: DEBUG
    # 开启SQL日志
    com.example.ddddemo.infrastructure.persistence.mapper: DEBUG
    com.baomidou.mybatisplus.core.mapper: DEBUG
  file:
    name: logs/ddd-demo-dev.log

---
# application-test.yml - 测试环境
logging:
  level:
    root: INFO
    com.example.ddddemo: INFO
    # 关闭详细SQL日志
    com.example.ddddemo.infrastructure.persistence.mapper: WARN
  file:
    name: logs/ddd-demo-test.log

---
# application-prod.yml - 生产环境
logging:
  level:
    root: WARN
    com.example.ddddemo: INFO
    # 生产环境关闭SQL日志
    com.example.ddddemo.infrastructure.persistence.mapper: ERROR
  file:
    name: logs/ddd-demo-prod.log
    # 生产环境更严格的日志轮转
    max-size: 50MB
    max-history: 7
```

**Logback配置文件 (logback-spring.xml)：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 引入Spring Boot默认配置 -->
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <!-- 定义日志文件路径 -->
    <springProfile name="!prod">
        <property name="LOG_FILE" value="logs/ddd-demo"/>
    </springProfile>
    <springProfile name="prod">
        <property name="LOG_FILE" value="/var/log/ddd-demo/ddd-demo"/>
    </springProfile>

    <!-- 控制台输出 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <!-- 文件输出 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}.log</file>
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_FILE}.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
    </appender>

    <!-- 错误日志单独输出 -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}-error.log</file>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_FILE}-error.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>50MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <!-- 异步日志 -->
    <appender name="ASYNC_FILE" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="FILE"/>
        <queueSize>1024</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <includeCallerData>true</includeCallerData>
    </appender>

    <!-- 根日志配置 -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>
        <appender-ref ref="ERROR_FILE"/>
    </root>

    <!-- 应用日志配置 -->
    <logger name="com.example.ddddemo" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>
        <appender-ref ref="ERROR_FILE"/>
    </logger>

    <!-- SQL日志配置 -->
    <springProfile name="dev,test">
        <logger name="com.example.ddddemo.infrastructure.persistence.mapper" level="DEBUG"/>
    </springProfile>

    <!-- 生产环境关闭SQL日志 -->
    <springProfile name="prod">
        <logger name="com.example.ddddemo.infrastructure.persistence.mapper" level="ERROR"/>
    </springProfile>
</configuration>
```

**日志最佳实践：**

1. **日志级别使用规范**
   ```java
   @Slf4j
   @Service
   public class UserApplicationService {

       public UserDTO createUser(CreateUserCommand command) {
           // INFO: 记录重要的业务操作
           log.info("开始创建用户: username={}", command.getUsername());

           try {
               // DEBUG: 记录详细的执行步骤
               log.debug("校验用户数据: {}", command);

               User user = userFactory.create(command);

               // DEBUG: 记录中间状态
               log.debug("用户聚合创建完成: userId={}", user.getId());

               User savedUser = userRepository.save(user);

               // INFO: 记录操作结果
               log.info("用户创建成功: userId={}, username={}",
                       savedUser.getId(), savedUser.getUsername());

               return UserConverter.toDTO(savedUser);

           } catch (Exception e) {
               // ERROR: 记录异常信息
               log.error("创建用户失败: username={}, error={}",
                        command.getUsername(), e.getMessage(), e);
               throw e;
           }
       }
   }
   ```

2. **结构化日志**
   ```java
   // 使用MDC添加上下文信息
   @Component
   public class LoggingFilter implements Filter {

       @Override
       public void doFilter(ServletRequest request, ServletResponse response,
                           FilterChain chain) throws IOException, ServletException {

           HttpServletRequest httpRequest = (HttpServletRequest) request;

           // 添加请求ID到MDC
           String requestId = UUID.randomUUID().toString();
           MDC.put("requestId", requestId);
           MDC.put("userId", getCurrentUserId());

           try {
               chain.doFilter(request, response);
           } finally {
               // 清理MDC
               MDC.clear();
           }
       }
   }
   ```

3. **性能监控日志**
   ```java
   @Aspect
   @Component
   @Slf4j
   public class PerformanceLoggingAspect {

       @Around("@annotation(com.example.ddddemo.shared.annotation.LogExecutionTime)")
       public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
           long startTime = System.currentTimeMillis();

           try {
               Object result = joinPoint.proceed();
               long executionTime = System.currentTimeMillis() - startTime;

               // WARN: 记录慢操作
               if (executionTime > 1000) {
                   log.warn("慢操作检测: method={}, executionTime={}ms",
                           joinPoint.getSignature().toShortString(), executionTime);
               } else {
                   log.debug("方法执行时间: method={}, executionTime={}ms",
                           joinPoint.getSignature().toShortString(), executionTime);
               }

               return result;
           } catch (Exception e) {
               long executionTime = System.currentTimeMillis() - startTime;
               log.error("方法执行异常: method={}, executionTime={}ms, error={}",
                        joinPoint.getSignature().toShortString(), executionTime, e.getMessage());
               throw e;
           }
       }
   }
   ```

**日志配置说明：**

- **开发环境**: 详细的DEBUG日志，包含SQL执行日志
- **测试环境**: 适中的INFO日志，关闭详细SQL日志
- **生产环境**: 精简的WARN日志，完全关闭SQL日志
- **异步日志**: 使用AsyncAppender提升性能
- **日志轮转**: 按大小和时间自动轮转，避免磁盘空间耗尽
- **错误日志**: 单独的错误日志文件，便于问题排查


### 3.4 监控配置

**Spring Boot Actuator配置 (application.yml)：**

```yaml
# 监控配置
management:
  # 端点配置
  endpoints:
    web:
      # 暴露的端点
      exposure:
        include: health,info,metrics,prometheus,loggers,env,configprops,beans,mappings,hikaricp
      # 端点基础路径
      base-path: /actuator
      # CORS配置
      cors:
        allowed-origins: "*"
        allowed-methods: GET,POST

  # 健康检查配置
  endpoint:
    health:
      # 显示详细健康信息
      show-details: always
      # 显示组件信息
      show-components: always
      # 健康检查缓存时间
      cache:
        time-to-live: 10s

    # 指标端点配置
    metrics:
      enabled: true

    # 日志端点配置
    loggers:
      enabled: true

  # 健康指示器配置
  health:
    # 数据库健康检查
    db:
      enabled: true
    # Redis健康检查
    redis:
      enabled: true
    # 磁盘空间检查
    diskspace:
      enabled: true
      threshold: 100MB

  # 指标配置
  metrics:
    # 启用JVM指标
    enable:
      jvm: true
      system: true
      web: true
      hikaricp: true

    # 指标导出配置
    export:
      # Prometheus指标导出
      prometheus:
        enabled: true
        descriptions: true
        step: 60s

    # 指标标签
    tags:
      application: ddd-demo
      environment: ${spring.profiles.active:dev}
      version: ${app.version:1.0.0}

    # Web指标配置
    web:
      server:
        # 记录请求处理时间
        request:
          autotime:
            enabled: true
            percentiles: 0.5,0.95,0.99
            percentiles-histogram: true

# 应用信息配置
info:
  app:
    name: DDD Demo - 用户管理系统
    description: 基于DDD分层架构的用户管理示例项目
    version: ${app.version:1.0.0}
    encoding: UTF-8
    java:
      version: ${java.version}
  build:
    time: ${build.time:unknown}
    artifact: ${project.artifactId:ddd-demo}
    group: ${project.groupId:com.example}
```

**自定义健康检查指示器：**

```java
/**
 * 自定义健康检查指示器
 */
@Component
@Slf4j
public class CustomHealthIndicator implements HealthIndicator {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Health health() {
        try {
            // 检查数据库连接
            long userCount = userRepository.count();

            // 检查业务状态
            if (userCount >= 0) {
                return Health.up()
                    .withDetail("database", "可用")
                    .withDetail("userCount", userCount)
                    .withDetail("timestamp", Instant.now())
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "数据异常")
                    .build();
            }

        } catch (Exception e) {
            log.error("健康检查失败", e);
            return Health.down()
                .withDetail("database", "不可用")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

**自定义业务指标：**

```java
/**
 * 业务指标配置
 */
@Component
@Slf4j
public class BusinessMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter userCreatedCounter;
    private final Timer userOperationTimer;
    private final Gauge activeUserGauge;

    public BusinessMetrics(MeterRegistry meterRegistry, UserRepository userRepository) {
        this.meterRegistry = meterRegistry;

        // 用户创建计数器
        this.userCreatedCounter = Counter.builder("user.created.total")
            .description("用户创建总数")
            .tag("application", "ddd-demo")
            .register(meterRegistry);

        // 用户操作耗时
        this.userOperationTimer = Timer.builder("user.operation.duration")
            .description("用户操作耗时")
            .tag("application", "ddd-demo")
            .register(meterRegistry);

        // 活跃用户数量
        this.activeUserGauge = Gauge.builder("user.active.count")
            .description("活跃用户数量")
            .tag("application", "ddd-demo")
            .register(meterRegistry, userRepository, repo -> {
                try {
                    return repo.countByStatus(UserStatus.ACTIVE);
                } catch (Exception e) {
                    log.error("获取活跃用户数量失败", e);
                    return 0;
                }
            });
    }

    /**
     * 记录用户创建事件
     */
    public void recordUserCreated() {
        userCreatedCounter.increment();
    }

    /**
     * 记录用户操作耗时
     */
    public void recordUserOperation(String operation, Duration duration) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("user.operation.duration")
            .tag("operation", operation)
            .register(meterRegistry));
    }
}
```

**监控切面：**

```java
/**
 * 监控切面
 */
@Aspect
@Component
@Slf4j
public class MonitoringAspect {

    private final MeterRegistry meterRegistry;

    public MonitoringAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 监控应用服务方法
     */
    @Around("execution(* com.example.ddddemo.application.service.*.*(..))")
    public Object monitorApplicationService(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();

            // 记录成功调用
            sample.stop(Timer.builder("application.service.duration")
                .tag("class", className)
                .tag("method", methodName)
                .tag("status", "success")
                .register(meterRegistry));

            return result;

        } catch (Exception e) {
            // 记录失败调用
            sample.stop(Timer.builder("application.service.duration")
                .tag("class", className)
                .tag("method", methodName)
                .tag("status", "error")
                .register(meterRegistry));

            // 记录异常计数
            Counter.builder("application.service.errors")
                .tag("class", className)
                .tag("method", methodName)
                .tag("exception", e.getClass().getSimpleName())
                .register(meterRegistry)
                .increment();

            throw e;
        }
    }
}
```

**监控最佳实践：**

1. **关键指标监控**
   ```yaml
   # 推荐监控的关键指标
   metrics:
     - name: jvm.memory.used
       description: JVM内存使用量
       threshold: 80%

     - name: hikaricp.connections.active
       description: 活跃数据库连接数
       threshold: 15

     - name: http.server.requests.duration
       description: HTTP请求响应时间
       threshold: 2s

     - name: user.operation.duration
       description: 用户操作耗时
       threshold: 1s
   ```

2. **告警规则配置 (Prometheus)**
   ```yaml
   # prometheus-alerts.yml
   groups:
     - name: ddd-demo-alerts
       rules:
         # 应用健康检查
         - alert: ApplicationDown
           expr: up{job="ddd-demo"} == 0
           for: 1m
           labels:
             severity: critical
           annotations:
             summary: "DDD Demo应用不可用"
             description: "应用已停止响应超过1分钟"

         # 内存使用率过高
         - alert: HighMemoryUsage
           expr: jvm_memory_used_bytes / jvm_memory_max_bytes > 0.8
           for: 5m
           labels:
             severity: warning
           annotations:
             summary: "内存使用率过高"
             description: "JVM内存使用率超过80%"

         # 响应时间过长
         - alert: HighResponseTime
           expr: http_server_requests_seconds{quantile="0.95"} > 2
           for: 2m
           labels:
             severity: warning
           annotations:
             summary: "响应时间过长"
             description: "95%的请求响应时间超过2秒"

         # 数据库连接池耗尽
         - alert: DatabaseConnectionPoolExhausted
           expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
           for: 1m
           labels:
             severity: critical
           annotations:
             summary: "数据库连接池即将耗尽"
             description: "数据库连接池使用率超过90%"
   ```

3. **监控面板配置 (Grafana)**
   ```json
   {
     "dashboard": {
       "title": "DDD Demo 监控面板",
       "panels": [
         {
           "title": "应用健康状态",
           "type": "stat",
           "targets": [
             {
               "expr": "up{job=\"ddd-demo\"}"
             }
           ]
         },
         {
           "title": "QPS",
           "type": "graph",
           "targets": [
             {
               "expr": "rate(http_server_requests_seconds_count[5m])"
             }
           ]
         },
         {
           "title": "响应时间",
           "type": "graph",
           "targets": [
             {
               "expr": "http_server_requests_seconds{quantile=\"0.95\"}"
             }
           ]
         },
         {
           "title": "JVM内存使用",
           "type": "graph",
           "targets": [
             {
               "expr": "jvm_memory_used_bytes"
             }
           ]
         }
       ]
     }
   }
   ```

**生产环境监控配置：**

```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        # 生产环境只暴露必要端点
        include: health,metrics,prometheus
  endpoint:
    health:
      # 生产环境不显示详细信息
      show-details: when-authorized

  # 安全配置
  security:
    enabled: true
    roles: ACTUATOR

# 监控数据保留策略
metrics:
  retention:
    # 指标数据保留7天
    duration: 7d

  # 采样配置
  sampling:
    # 降低采样频率以减少存储压力
    rate: 0.1
```