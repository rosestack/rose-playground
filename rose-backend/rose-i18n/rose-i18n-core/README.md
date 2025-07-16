# Rose I18n Core - Properties 实现

## 概述

这是 Rose I18n 国际化框架的核心模块，提供了基于 Properties 文件的消息源实现。支持从 classpath 中读取多语言配置文件，实现应用程序的国际化功能。

## 核心特性

- 🌍 **多语言支持**：支持任意数量的语言环境
- 📁 **文件自动发现**：自动从 classpath 读取 properties 文件
- 🔄 **参数替换**：支持 MessageFormat 格式的参数替换
- 💾 **智能缓存**：内置缓存机制，提高性能
- 🛡️ **线程安全**：使用读写锁保证线程安全
- 🎯 **回退机制**：支持语言回退和默认值
- 🔧 **灵活配置**：支持多个基础名称和自定义编码

## 快速开始

### 1. 文件结构

在 `src/main/resources` 目录下创建 properties 文件：

```
src/main/resources/
├── messages.properties          # 默认语言（通常是英文）
├── messages_zh_CN.properties   # 中文简体
├── messages_zh_TW.properties   # 中文繁体
├── messages_ja.properties      # 日文
├── messages_ko.properties      # 韩文
└── messages_fr.properties      # 法文
```

### 2. 基本使用

```java
// 创建消息源
PropertiesI18nMessageSource messageSource = new PropertiesI18nMessageSource("messages");

// 初始化
messageSource.init();

// 获取消息
String message = messageSource.getMessage("welcome.message", Locale.SIMPLIFIED_CHINESE);
System.out.println(message); // 输出：欢迎使用 Rose 国际化框架

// 带参数的消息
String greeting = messageSource.getMessage("user.greeting", new Object[]{"张三"}, Locale.SIMPLIFIED_CHINESE);
System.out.println(greeting); // 输出：你好，张三！

// 清理资源
messageSource.destroy();
```

### 3. 高级配置

```java
// 支持多个基础名称
PropertiesI18nMessageSource messageSource = new PropertiesI18nMessageSource("messages", "validation", "errors");

// 设置编码
messageSource.setEncoding("UTF-8");

// 设置默认语言环境
messageSource.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);

// 设置是否回退到系统语言环境
messageSource.setFallbackToSystemLocale(true);

// 初始化
messageSource.init();
```

## 文件命名规范

Properties 文件命名遵循 Java 标准的资源包命名规范：

- `basename.properties` - 默认语言
- `basename_language.properties` - 指定语言（如 `messages_en.properties`）
- `basename_language_country.properties` - 指定语言和国家（如 `messages_zh_CN.properties`）
- `basename_language_country_variant.properties` - 完整语言环境

### 语言代码示例

| 语言 | 代码 | 文件名示例 |
|------|------|------------|
| 英文 | en | messages_en.properties |
| 中文简体 | zh_CN | messages_zh_CN.properties |
| 中文繁体 | zh_TW | messages_zh_TW.properties |
| 日文 | ja | messages_ja.properties |
| 韩文 | ko | messages_ko.properties |
| 法文 | fr | messages_fr.properties |
| 德文 | de | messages_de.properties |
| 西班牙文 | es | messages_es.properties |

## 消息格式

### 简单消息

```properties
welcome.message=欢迎使用系统
app.title=我的应用
```

### 带参数的消息

```properties
user.greeting=你好，{0}！
error.validation.length={0} 的长度必须在 {1} 到 {2} 之间
order.summary=订单 {0} 总计 {1,number,currency}，创建时间：{2,date,medium}
```

### 复杂格式化

```properties
# 数字格式化
price.display=价格：{0,number,currency}
percentage.display=完成度：{0,number,percent}

# 日期格式化
date.short=日期：{0,date,short}
date.long=完整日期：{0,date,long}
time.display=时间：{0,time,medium}

# 选择格式化
file.count={0,choice,0#没有文件|1#1个文件|1<{0,number}个文件}
```

## API 参考

### PropertiesI18nMessageSource

#### 构造方法

```java
// 使用默认基础名称 "messages"
PropertiesI18nMessageSource()

// 指定单个基础名称
PropertiesI18nMessageSource(String baseName)

// 指定多个基础名称
PropertiesI18nMessageSource(String... baseNames)
```

#### 主要方法

```java
// 获取消息
String getMessage(String code, Object[] args, Locale locale)
String getMessage(String code, Locale locale)
String getMessage(String code, Object[] args)

// 批量获取消息
Map<String, String> getMessages(Set<String> codes, Locale locale)
Map<String, String> getAllMessages(Locale locale)

// 语言环境管理
Set<Locale> getSupportedLocales()
Locale getDefaultLocale()

// 生命周期管理
void init()
void refresh()
void refresh(Locale locale)
void destroy()
```

#### 配置方法

```java
// 设置编码（默认 UTF-8）
void setEncoding(String encoding)

// 设置默认语言环境
void setDefaultLocale(Locale defaultLocale)

// 设置是否回退到系统语言环境（默认 true）
void setFallbackToSystemLocale(boolean fallbackToSystemLocale)
```

## 最佳实践

### 1. 文件组织

```
src/main/resources/
├── i18n/                    # 推荐将国际化文件放在专门目录下
│   ├── messages.properties
│   ├── messages_zh_CN.properties
│   ├── validation.properties
│   └── validation_zh_CN.properties
```

使用时指定完整路径：

```java
PropertiesI18nMessageSource messageSource = new PropertiesI18nMessageSource("i18n/messages", "i18n/validation");
```

### 2. 键值命名规范

```properties
# 使用分层命名
menu.home=首页
menu.about=关于
menu.contact=联系我们

# 按功能分组
error.validation.required=此字段为必填项
error.validation.email=请输入有效的邮箱地址
error.network.timeout=网络超时

# 使用描述性名称
button.save=保存
button.cancel=取消
status.loading=加载中...
```

### 3. 参数使用

```properties
# 简单参数
user.greeting=你好，{0}！

# 多个参数
order.info=订单 {0}，金额 {1}，状态：{2}

# 格式化参数
price.display=价格：{0,number,currency}
date.display=日期：{0,date,yyyy-MM-dd}
```

### 4. 生命周期管理

```java
public class I18nManager {
    private PropertiesI18nMessageSource messageSource;
    
    @PostConstruct
    public void init() {
        messageSource = new PropertiesI18nMessageSource("messages");
        messageSource.init();
    }
    
    @PreDestroy
    public void destroy() {
        if (messageSource != null) {
            messageSource.destroy();
        }
    }
}
```

## 性能优化

1. **缓存机制**：消息在首次加载后会被缓存，避免重复读取文件
2. **延迟加载**：只有在实际访问特定语言环境时才加载对应的文件
3. **批量操作**：使用 `getMessages()` 批量获取多个消息以提高效率
4. **线程安全**：使用读写锁确保多线程环境下的性能和安全性

## 故障排查

### 常见问题

1. **找不到消息文件**
   - 检查文件是否在 classpath 中
   - 验证文件名是否符合命名规范
   - 确认文件编码是否正确

2. **参数替换失败**
   - 检查 MessageFormat 语法是否正确
   - 确认参数类型和数量匹配

3. **中文乱码**
   - 确保 properties 文件使用 UTF-8 编码保存
   - 验证 `setEncoding("UTF-8")` 设置

### 调试技巧

```java
// 检查支持的语言环境
Set<Locale> locales = messageSource.getSupportedLocales();
System.out.println("支持的语言环境：" + locales);

// 检查资源文件是否存在
boolean exists = messageSource.isResourceAvailable("messages_zh_CN.properties");
System.out.println("资源文件存在：" + exists);

// 获取所有消息进行调试
Map<String, String> allMessages = messageSource.getAllMessages(Locale.SIMPLIFIED_CHINESE);
allMessages.forEach((key, value) -> System.out.println(key + " = " + value));
```

## 示例代码

运行测试类查看完整示例：

```bash
# 编译和运行测试
javac -cp . src/test/java/io/github/rose/i18n/PropertiesI18nMessageSourceTest.java
java -cp .:src/main/java:src/test/java io.github.rose.i18n.PropertiesI18nMessageSourceTest
```

## 扩展计划

未来版本将支持：

- JSON 格式消息文件
- YAML 格式消息文件
- 数据库消息源
- HTTP 远程消息源
- 消息热更新
- 更多的缓存策略