# 测试资源文件说明

## 国际化消息文件

本目录包含用于测试ExceptionMessageResolver的国际化消息文件：

### 文件列表

1. **messages.properties** - 默认英文消息
2. **messages_zh_CN.properties** - 中文消息（简体中文）
3. **messages_fr.properties** - 法文消息
4. **application-test.properties** - 测试配置

### 中文编码说明

为了避免中文字符在不同环境下的编码问题，`messages_zh_CN.properties`文件使用了Unicode转义序列：

```properties
# 原始中文：用户ID {0} 不存在
# Unicode转义：\u7528\u6237ID {0} \u4e0d\u5b58\u5728
user.not.found=\u7528\u6237ID {0} \u4e0d\u5b58\u5728
```

### Unicode转义序列对照表

| 中文字符 | Unicode转义 | 说明 |
|---------|------------|------|
| 用 | \u7528 | 用户 |
| 户 | \u6237 | 用户 |
| 不 | \u4e0d | 不存在 |
| 存 | \u5b58 | 存在 |
| 在 | \u5728 | 存在 |
| 字 | \u5b57 | 字段 |
| 段 | \u6bb5 | 字段 |
| 验 | \u9a8c | 验证 |
| 证 | \u8bc1 | 验证 |
| 失 | \u5931 | 失败 |
| 败 | \u8d25 | 失败 |
| 邮 | \u90ae | 邮箱 |
| 箱 | \u7bb1 | 邮箱 |
| 格 | \u683c | 格式 |
| 式 | \u5f0f | 格式 |
| 正 | \u6b63 | 正确 |
| 确 | \u786e | 正确 |
| 必 | \u5fc5 | 必填 |
| 填 | \u586b | 必填 |
| 的 | \u7684 | 的 |
| 认 | \u8ba4 | 认证 |
| 令 | \u4ee4 | 令牌 |
| 牌 | \u724c | 令牌 |
| 无 | \u65e0 | 无效 |
| 效 | \u6548 | 无效 |
| 已 | \u5df2 | 已过期 |
| 过 | \u8fc7 | 过期 |
| 期 | \u671f | 过期 |
| 请 | \u8bf7 | 请求 |
| 求 | \u6c42 | 请求 |
| 于 | \u4e8e | 过于 |
| 频 | \u9891 | 频繁 |
| 繁 | \u7e41 | 频繁 |
| 系 | \u7cfb | 系统 |
| 统 | \u7edf | 系统 |
| 内 | \u5185 | 内部 |
| 部 | \u90e8 | 内部 |
| 错 | \u9519 | 错误 |
| 误 | \u8bef | 错误 |

### 如何生成Unicode转义序列

如果需要添加新的中文消息，可以使用以下方法生成Unicode转义序列：

#### 方法1：使用Java代码
```java
public class UnicodeConverter {
    public static void main(String[] args) {
        String chinese = "用户ID {0} 不存在";
        StringBuilder unicode = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            if (c > 127) {
                unicode.append("\\u").append(String.format("%04x", (int) c));
            } else {
                unicode.append(c);
            }
        }
        System.out.println(unicode.toString());
    }
}
```

#### 方法2：使用在线工具
- Unicode转换工具：https://www.unicode-converter.com/
- 中文Unicode转换：https://tool.chinaz.com/tools/unicode.aspx

#### 方法3：使用IDE插件
- IntelliJ IDEA：Properties Editor插件
- Eclipse：Properties Editor插件

### 测试验证

运行测试时，Spring的ResourceBundleMessageSource会自动将Unicode转义序列转换为正确的中文字符，确保在不同环境下都能正确显示中文消息。

### 注意事项

1. **编码一致性**：确保所有.properties文件都使用相同的编码方式
2. **IDE设置**：建议IDE设置为UTF-8编码
3. **构建工具**：确保Maven/Gradle构建时使用UTF-8编码
4. **运行环境**：确保JVM运行时使用UTF-8编码

### 配置示例

在`application-test.properties`中的相关配置：

```properties
spring.messages.basename=messages
spring.messages.encoding=UTF-8
spring.messages.cache-duration=3600
spring.messages.fallback-to-system-locale=false
```

这样配置确保了消息资源的正确加载和编码处理。
