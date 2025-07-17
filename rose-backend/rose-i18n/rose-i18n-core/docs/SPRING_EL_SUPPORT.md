# Spring EL 表达式支持

Rose I18n框架现在支持Spring Expression Language (SpEL)作为表达式评估器，提供强大的表达式处理能力。

## 功能特性

### 自动发现机制

DefaultMessageInterpolator会按以下优先级自动发现并选择最佳的表达式评估器：

1. **Jakarta EL** (优先级: 10) - 如果容器提供EL实现
2. **Spring EL** (优先级: 5) - 如果Spring框架可用
3. **Simple Expression Evaluator** (优先级: 100) - 后备选择，无外部依赖

### Spring EL支持的语法

#### 1. 基本属性访问
```java
// 模板: "Hello, ${user.name}!"
Map<String, Object> params = Map.of("user", new User("Alice"));
// 结果: "Hello, Alice!"
```

#### 2. 方法调用
```java
// 模板: "Length: ${text.length()}"
Map<String, Object> params = Map.of("text", "Hello");
// 结果: "Length: 5"
```

#### 3. 条件表达式
```java
// 模板: "Status: ${user.age >= 18 ? 'adult' : 'minor'}"
Map<String, Object> params = Map.of("user", new User("Bob", 20));
// 结果: "Status: adult"
```

#### 4. 算术运算
```java
// 模板: "Total: ${price * quantity}"
Map<String, Object> params = Map.of("price", 100, "quantity", 3);
// 结果: "Total: 300"
```

#### 5. 逻辑运算
```java
// 模板: "Access: ${user.active && user.verified ? 'granted' : 'denied'}"
Map<String, Object> params = Map.of("user", activeUser);
// 结果: "Access: granted"
```

#### 6. 集合操作
```java
// 模板: "First: ${names[0]}, Count: ${names.size()}"
Map<String, Object> params = Map.of("names", List.of("Alice", "Bob"));
// 结果: "First: Alice, Count: 2"
```

#### 7. 空值安全操作
```java
// 模板: "Name: ${user.name != null ? user.name : 'Anonymous'}"
Map<String, Object> params = Map.of("user", new User(null));
// 结果: "Name: Anonymous"
```

#### 8. 字符串操作
```java
// 模板: "Upper: ${name.toUpperCase()}"
Map<String, Object> params = Map.of("name", "alice");
// 结果: "Upper: ALICE"
```

#### 9. 正则表达式
```java
// 模板: "Valid: ${email matches '.*@.*\\.com'}"
Map<String, Object> params = Map.of("email", "user@example.com");
// 结果: "Valid: true"
```

#### 10. 类型操作
```java
// 模板: "PI: ${T(Math).PI}"
Map<String, Object> params = Map.of();
// 结果: "PI: 3.141592653589793"
```

## 使用方式

### 1. 自动配置（推荐）
```java
// 自动发现最佳表达式评估器
DefaultMessageInterpolator interpolator = new DefaultMessageInterpolator();

String result = interpolator.interpolate(
    "Hello ${user.name}, you are ${user.age >= 18 ? 'adult' : 'minor'}!",
    Map.of("user", new User("Alice", 25)),
    Locale.ENGLISH
);
// 结果: "Hello Alice, you are adult!"
```

### 2. 显式指定Spring EL
```java
// 显式使用Spring EL评估器
SpringElExpressionEvaluator springEL = new SpringElExpressionEvaluator();
DefaultMessageInterpolator interpolator = new DefaultMessageInterpolator(springEL);

// 或者使用工厂方法
DefaultMessageInterpolator interpolator = DefaultMessageInterpolator.create(springEL);
```

### 3. 运行时切换
```java
DefaultMessageInterpolator interpolator = new DefaultMessageInterpolator();

// 运行时切换到Spring EL
interpolator.setExpressionEvaluator(new SpringElExpressionEvaluator());
```

## 依赖要求

要使用Spring EL功能，需要在项目中包含Spring Expression Language依赖：

### Maven
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-expression</artifactId>
    <version>6.0.0</version>
</dependency>
```

### Gradle
```groovy
implementation 'org.springframework:spring-expression:6.0.0'
```

## 性能考虑

1. **表达式解析**: Spring EL会解析表达式，对于复杂表达式可能有一定开销
2. **自动发现**: 表达式评估器的自动发现只在创建时执行一次
3. **缓存**: DefaultMessageInterpolator内置了表达式结果缓存机制
4. **后备机制**: 如果Spring EL不可用，会自动降级到简单表达式评估器

## 错误处理

- 表达式评估失败时返回null，不会抛出异常
- 无效表达式会被保留为原始形式（如`${invalid.expression}`）
- 缺少依赖时会自动降级到可用的评估器

## 最佳实践

1. **优先使用自动发现**: 让框架自动选择最佳的表达式评估器
2. **简单表达式优先**: 对于简单的属性访问，所有评估器都能处理
3. **复杂逻辑使用Spring EL**: 条件表达式、算术运算等使用Spring EL
4. **测试兼容性**: 确保在不同环境下都能正常工作
5. **性能测试**: 对于高频使用的表达式进行性能测试

## 示例代码

完整的使用示例请参考：
- `SpringElExpressionEvaluatorTest.java` - Spring EL功能测试
- `ExpressionEvaluatorTest.java` - 综合表达式评估器测试
