# rose-notice

`rose-notice` 是一个通用、可扩展的通知发送组件，支持邮件、短信（阿里云、腾讯云）、语音等多种渠道，具备同步/异步、批量、重试、限流、黑名单、拦截器、幂等等特性，便于在企业级应用中集成和扩展。

**本项目采用 [Apache License 2.0](LICENSE) 开源协议。**

## 特性

- **多渠道支持**：内置 Email、短信（阿里云、腾讯云）、语音（可扩展）等发送渠道
- **模板渲染**：支持变量替换、Groovy 模板等多种模板渲染方式
- **限流/黑名单/幂等**：内置内存实现，支持自定义 SPI 扩展
- **拦截器机制**：支持发送前、后、异常时自定义处理
- **批量/异步发送**：支持批量、异步发送
- **SPI 插件式扩展**：所有核心能力均可通过 SPI 插件扩展

## 快速开始

### 依赖

```xml

<dependency>
    <groupId>io.github.rosestack</groupId>
    <artifactId>rose-notice</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 主要 API

#### 1. 构建 NoticeService

```java
NoticeService noticeService = new NoticeService(new TemplateContentRenderFactory());
```

#### 2. 构造 SendRequest

```java
SendRequest request = SendRequest.builder()
        .requestId(UUID.randomUUID().toString())
        .channelType(Sender.EMAIL) // 或 Sender.SMS
        .target("user@example.com") // 邮箱/手机号
        .subject("通知标题")
        .templateContent("您的验证码是：${code}")
        .templateType("SimpleVariableTemplateContentRender")
        .channelConfig(Map.of(
                "mail.smtp.host", "smtp.example.com",
                "mail.smtp.username", "user",
                "mail.smtp.password", "pass",
                "mail.smtp.port", 465,
                "mail.smtp.from", "noreply@example.com"
        ))
        .build();
```

#### 3. 发送通知

```java
SendResult result = noticeService.send(request);
```

#### 4. 异步/批量发送

```java
noticeService.sendAsync(request);
noticeService.

sendBatch(List.of(request1, request2));
        noticeService.

sendBatchAsync(List.of(request1, request2));
```

## 配置说明

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

## 扩展点（SPI）

- **Sender**：自定义发送渠道，实现 `io.github.rose.notice.spi.Sender` 并配置 SPI 文件
- **SmsProvider**：自定义短信服务商，实现 `io.github.rose.notice.sender.sms.SmsProvider`
- **TemplateContentRender**：自定义模板渲染，实现 `io.github.rose.notice.spi.TemplateContentRender`
- **RateLimiter/BlacklistChecker/IdempotencyStore**：自定义限流、黑名单、幂等实现
- **NoticeSendInterceptor**：自定义拦截器，支持发送前、后、异常处理

## 目录结构

```
src/main/java/io/github/rose/notice/
  ├── NoticeService.java         // 核心服务
  ├── SendRequest.java           // 发送请求参数
  ├── SendResult.java            // 发送结果
  ├── sender/                    // 各类发送渠道
  ├── render/                    // 模板渲染
  ├── spi/                       // 各类SPI扩展点
  ├── impl/                      // 内存实现（限流、黑名单、幂等等）
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
