# rose-notification

`rose-notification` 是一个通用、可扩展的通知发送组件，支持邮件、短信（阿里云、腾讯云）、语音等多种渠道，具备同步/异步、批量、重试、限流、黑名单、拦截器、幂等等特性，便于在企业级应用中集成和扩展。

**本项目采用 [Apache License 2.0](LICENSE) 开源协议。**

## 特性

- **多渠道支持**：内置 Email、短信（阿里云、腾讯云）、语音（可扩展）等发送渠道
- **模板渲染**：支持变量替换、Groovy 模板等多种模板渲染方式
- **限流/黑名单/幂等**：内置内存实现，支持自定义 SPI 扩展
  - 幂等支持 Caffeine/Redis；黑名单支持内存/Redis
- **拦截器机制**：支持发送前、后、异常时自定义处理
- **批量/异步发送**：支持批量、异步发送
- **SPI 插件式扩展**：所有核心能力均可通过 SPI 插件扩展
- **指标可观测性（可选）**：支持 Micrometer 指标（成功/失败/耗时）

## 快速开始

### 依赖

```xml

<dependency>
    <groupId>io.github.rosestack</groupId>
    <artifactId>rose-notification</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 主要 API

#### 1. 构建 NoticeService

```java
NoticeService noticeService = new NoticeService();
// 可选：替换为具备 TTL/容量的内存幂等实现
noticeService.setIdempotencyStore(new CaffeineIdempotencyStore(20000, java.time.Duration.ofMinutes(30)));
// 可选：基于 Redis 的幂等/黑名单（需引入 spring-data-redis & lettuce）
// StringRedisTemplate redisTemplate = ...
// noticeService.setIdempotencyStore(new RedisIdempotencyStore(redisTemplate, "rose:notification:idemp:", java.time.Duration.ofHours(1)));
// noticeService.setBlacklistChecker(new RedisBlacklistChecker(redisTemplate, "rose:notification:blacklist"));
// 可选：Micrometer 指标
// MeterRegistry registry = ...
// noticeService.setMetrics(new NoticeMetrics(registry));
```

#### 2. 构造 SendRequest + SenderConfiguration

```java
SendRequest request = SendRequest.builder()
        .requestId(UUID.randomUUID().toString())
        .target("user@example.com") // 邮箱/手机号
        .templateContent("您的验证码是：${code}")
        .build();

SenderConfiguration config = SenderConfiguration.builder()
        .channelType(Sender.EMAIL) // 或 Sender.SMS
        .templateType("SimpleVariableTemplateContentRender")
        .config(Map.of(
                "mail.smtp.host", "smtp.example.com",
                "mail.smtp.username", "user",
                "mail.smtp.password", "pass",
                "mail.smtp.port", 465,
                "mail.smtp.from", "noreply@example.com",
                // 重试（指数退避+抖动）
                "retry.maxAttempts", 3,
                "retry.initialDelayMillis", 200,
                "retry.jitterMillis", 100
        ))
        .build();
```

#### 3. 发送通知

```java
SendResult result = noticeService.send(request, config);
```

#### 4. 异步/批量发送

```java
// 异步
CompletableFuture<SendResult> future = noticeService.sendAsync(request, config);

// 批量（内部并行）
List<SendResult> batchResult = noticeService.sendBatch(List.of(request1, request2), config);
CompletableFuture<List<SendResult>> batchAsync = noticeService.sendBatchAsync(List.of(request1, request2), config);
```

## 配置说明
### 工厂缓存参数（JVM 级）

- `-Drose.notification.sender.cache.maxSize=1000`
- `-Drose.notification.sender.cache.expireAfterAccessSeconds=1800`
- `-Drose.notification.smsProvider.cache.maxSize=1000`
- `-Drose.notification.smsProvider.cache.expireAfterAccessSeconds=1800`


### 邮件渠道

- `mail.smtp.host` SMTP服务器地址
- `mail.smtp.username` 用户名
- `mail.smtp.password` 密码
- `mail.smtp.port` 端口（默认25）
- `mail.smtp.from` 发件人（可选）

### 短信渠道

#### 阿里云

- `aliyun.sms.accessKeyId`
- `aliyun.sms.accessKeySecret`
- `aliyun.sms.signName`
- `aliyun.sms.templateCode`
- `templateContent` 建议为 JSON 字符串

#### 腾讯云

- `tencent.sms.secretId`
- `tencent.sms.secretKey`
- `tencent.sms.sdkAppId`
- `tencent.sms.signName`
- `tencent.sms.templateId`
- `templateContent` 建议为 JSON 字符串

## Spring Boot Starter 集成

### 依赖

```xml
<dependency>
  <groupId>io.github.rosestack</groupId>
  <artifactId>rose-notification-spring-boot-starter</artifactId>
  <version>${project.version}</version>
  <scope>compile</scope>
</dependency>
```

### 应用配置（application.yml）

```yaml
rose:
  notification:
    retryable: true
    executor-core-size: 8
    sender-cache-max-size: 2000
    sender-cache-expire-after-access-seconds: 1200
    sms-provider-cache-max-size: 2000
    sms-provider-cache-expire-after-access-seconds: 1200
```

如需完整模板，可参考 starter 内置的 `application-rose-notification.yml`。

### 代码示例

```java
@Autowired
private NoticeService noticeService;

public void sendEmail() {
  SendRequest request = SendRequest.builder()
      .requestId(UUID.randomUUID().toString())
      .target("user@example.com")
      .templateContent("您的验证码是：${code}")
      .build();

  SenderConfiguration config = SenderConfiguration.builder()
      .channelType("email")
      .templateType("SimpleVariableTemplateContentRender")
      .config(Map.of(
          "mail.smtp.host", "smtp.example.com",
          "mail.smtp.username", "user",
          "mail.smtp.password", "pass",
          "mail.smtp.port", 465,
          "mail.smtp.from", "noreply@example.com",
          "retry.maxAttempts", 3,
          "retry.initialDelayMillis", 200,
          "retry.jitterMillis", 100
      ))
      .build();

  SendResult result = noticeService.send(request, config);
}
```

## 扩展点（SPI）

- **Sender**：自定义发送渠道，实现 `io.github.rose.notice.spi.Sender` 并配置 SPI 文件
- **SmsProvider**：自定义短信服务商，实现 `io.github.rose.notice.sender.sms.SmsProvider`
- **TemplateContentRender**：自定义模板渲染，实现 `io.github.rose.notice.spi.TemplateContentRender`
- **RateLimiter/BlacklistChecker/IdempotencyStore**：自定义限流、黑名单、幂等实现
- **NoticeSendInterceptor**：自定义拦截器，支持发送前、后、异常处理

## 目录结构

```
src/main/java/io/github/rose/notice/
  ├── NoticeService.java         // 核心服务（校验、拦截、渲染、路由、异步/批量、重试）
  ├── SendRequest.java           // 发送请求参数
  ├── SendResult.java            // 发送结果
  ├── sender/                    // 各类发送渠道
  ├── render/                    // 模板渲染
  ├── spi/                       // 各类SPI扩展点
  ├── support/                   // 内存实现（黑名单、幂等等）
```

## 开源项目

实现了通知功能的一些开源项目：
- thingsboard

## 贡献

欢迎提交 Issue 和 PR，完善更多渠道和能力！

## License

本项目基于 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 开源发布。

---

如需详细用法或二次开发，请参考源码和注释。
