# Rose I18n Framework

Rose I18n是一个现代化的Java国际化框架，基于SPI机制设计，提供简洁而强大的API，支持多种数据源和企业级特性。

## 特性

- **基于SPI机制**：支持可插拔的消息提供者
- **多数据源支持**：Properties、JSON、YAML、数据库、HTTP API等
- **简洁的API**：精心设计的接口，易于使用和扩展
- **Spring集成**：完全兼容Spring MessageSource
- **热重载**：支持运行时重新加载消息
- **多级缓存**：提供高性能的消息缓存机制
- **类型安全**：支持编译时消息键验证
- **现代化特性**：支持嵌套消息、批量操作、异步加载等

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.rosestack</groupId>
    <artifactId>rose-i18n-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 创建消息文件

**Properties格式** (`messages_en.properties`):
```properties
welcome.message=Welcome to Rose Framework!
user.greeting=Hello, {0}!
```

**JSON格式** (`messages_en.json`):
```json
{
  "welcome": {
    "message": "Welcome to Rose Framework!"
  },
  "user": {
    "greeting": "Hello, {0}!"
  }
}
```

**YAML格式** (`messages_en.yml`):
```yaml
welcome:
  message: Welcome to Rose Framework!
user:
  greeting: Hello, {0}!
```

### 3. 使用构建器创建消息源

```java
I18nMessageSource messageSource = I18nMessageSourceBuilder.create()
    .addPropertiesProvider("messages", "classpath:i18n/")
    .addJsonProvider("messages", "classpath:i18n/")
    .addYamlProvider("messages", "classpath:i18n/")
    .setDefaultLocale(Locale.ENGLISH)
    .setSupportedLocales(Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE)
    .enableCache(true)
    .build();
```

### 4. 获取消息

```java
// 简单消息
String message = messageSource.getMessage("welcome.message", Locale.ENGLISH);

// 带参数的消息
String greeting = messageSource.getMessage("user.greeting", 
    new Object[]{"John"}, Locale.ENGLISH);

// 带默认值的消息
String message = messageSource.getMessage("unknown.key", 
    Locale.ENGLISH, "Default Message");

// 批量获取消息
Set<String> keys = Set.of("welcome.message", "user.greeting");
Map<String, String> messages = messageSource.getAllMessages(keys, Locale.ENGLISH);
```

## Spring集成

### 1. 配置MessageSource Bean

```java
@Configuration
public class I18nConfig {
    
    @Bean
    public MessageSource messageSource() {
        I18nMessageSource i18nMessageSource = I18nMessageSourceBuilder.create()
            .addPropertiesProvider("messages", "classpath:i18n/")
            .setDefaultLocale(Locale.ENGLISH)
            .setSupportedLocales(Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE)
            .build();
        
        return new SpringI18nMessageSourceAdapter(i18nMessageSource);
    }
}
```

### 2. 在Controller中使用

```java
@RestController
public class HomeController {
    
    @Autowired
    private MessageSource messageSource;
    
    @GetMapping("/welcome")
    public String welcome(Locale locale) {
        return messageSource.getMessage("welcome.message", null, locale);
    }
}
```

### 3. 在模板中使用

**Thymeleaf**:
```html
<p th:text="#{welcome.message}">Welcome</p>
<p th:text="#{user.greeting('John')}">Hello, John!</p>
```

**JSP**:
```jsp
<spring:message code="welcome.message" />
<spring:message code="user.greeting" arguments="John" />
```

## 高级特性

### 1. 热重载

```java
I18nMessageSource messageSource = I18nMessageSourceBuilder.create()
    .addPropertiesProvider("messages", "file:/path/to/messages", true, 5000) // 5秒检查一次
    .build();
```

### 2. 自定义消息插值器

```java
public class CustomMessageInterpolator implements MessageInterpolator {
    @Override
    public String interpolate(String template, Object[] args, Locale locale) {
        // 自定义插值逻辑
        return template;
    }
    
    // 其他方法实现...
}

I18nMessageSource messageSource = I18nMessageSourceBuilder.create()
    .addPropertiesProvider("messages")
    .setMessageInterpolator(new CustomMessageInterpolator())
    .build();
```

### 3. 自定义消息提供者

```java
public class DatabaseMessageProvider implements I18nMessageProvider {
    @Override
    public String getName() {
        return "DatabaseMessageProvider";
    }
    
    @Override
    public Map<String, String> loadMessages(Locale locale) {
        // 从数据库加载消息
        return loadFromDatabase(locale);
    }
    
    // 其他方法实现...
}

// 注册提供者
I18nProviderConfig config = I18nProviderConfig.builder()
    .type("database")
    .property("url", "jdbc:mysql://localhost/messages")
    .build();

I18nMessageSource messageSource = I18nMessageSourceBuilder.create()
    .addProvider(new DatabaseMessageProvider(), config)
    .build();
```

## API参考

### I18nMessageSource接口

```java
public interface I18nMessageSource {
    String getMessage(String key, Object[] args, Locale locale, String defaultMessage);
    String getMessage(String key, Object[] args, Locale locale) throws I18nMessageNotFoundException;
    String getMessage(String key, Locale locale) throws I18nMessageNotFoundException;
    String getMessage(String key, Locale locale, String defaultMessage);
    
    Map<String, String> getAllMessages(Set<String> keys, Locale locale);
    Map<String, String> getAllMessages(Locale locale);
    
    Set<Locale> getSupportedLocales();
    Locale getDefaultLocale();
    Locale getLocale();
    
    void init();
    void destroy();
    void refresh();
    
    boolean containsMessage(String key, Locale locale);
    String getName();
    int getPriority();
}
```

### I18nMessageProvider接口

```java
public interface I18nMessageProvider extends Prioritized {
    String getName();
    boolean supports(I18nProviderConfig config);
    void initialize(I18nProviderConfig config);
    
    Map<String, String> loadMessages(Locale locale);
    Set<Locale> getSupportedLocales();
    
    boolean supportsHotReload();
    boolean supportsWrite();
    
    void saveMessage(String key, String value, Locale locale);
    void deleteMessage(String key, Locale locale);
    
    void addChangeListener(I18nMessageChangeListener listener);
    void removeChangeListener(I18nMessageChangeListener listener);
    
    void refresh();
    void destroy();
}
```

## 最佳实践

1. **消息键命名**：使用层次化的命名方式，如`module.component.action`
2. **默认值**：总是为消息提供合理的默认值
3. **缓存策略**：在生产环境中启用缓存以提高性能
4. **热重载**：仅在开发环境中启用热重载
5. **提供者优先级**：合理设置提供者优先级，确保消息覆盖顺序正确

## 性能优化

1. **预加载**：在应用启动时预加载常用消息
2. **批量操作**：使用批量API减少I/O操作
3. **缓存配置**：根据应用特点配置合适的缓存策略
4. **异步加载**：对于大量消息，考虑使用异步加载

## 故障排除

### 常见问题

1. **消息未找到**：检查文件路径和命名规则
2. **编码问题**：确保文件编码设置正确
3. **热重载不工作**：检查文件路径是否为绝对路径
4. **性能问题**：检查缓存配置和提供者数量

### 调试技巧

1. 启用DEBUG日志查看详细信息
2. 使用`containsMessage`方法检查消息是否存在
3. 使用`getSupportedLocales`检查支持的语言环境
4. 使用`getAllMessages`查看所有加载的消息

## 许可证

本项目采用MIT许可证，详见LICENSE文件。
