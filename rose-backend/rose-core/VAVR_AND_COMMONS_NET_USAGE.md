# Vavr 和 Commons Net 使用指南

本文档介绍如何在 Rose 项目中使用 Vavr 和 Commons Net 库。

## Vavr 使用指南

Vavr 是一个 Java 函数式编程库，提供了不可变集合、函数式数据结构等特性。

### 主要特性

#### 1. Try - 异常处理

```java
import io.vavr.control.Try;

// 基本用法
Try<Integer> result = Try.of(() -> Integer.parseInt("123"));
if (result.isSuccess()) {
    System.out.println("结果: " + result.get());
} else {
    System.out.println("解析失败: " + result.getCause());
}

// 使用默认值
Integer value = Try.of(() -> Integer.parseInt("invalid"))
    .getOrElse(0);

// 链式操作
String result = Try.of(() -> "123")
    .map(Integer::parseInt)
    .map(i -> i * 2)
    .map(String::valueOf)
    .getOrElse("0");
```

#### 2. Either - 成功或失败

```java
import io.vavr.control.Either;

// 表示成功或失败
Either<String, Integer> result = Try.of(() -> Integer.parseInt("123"))
    .toEither()
    .mapLeft(Throwable::getMessage);

if (result.isRight()) {
    System.out.println("成功: " + result.get());
} else {
    System.out.println("失败: " + result.getLeft());
}
```

#### 3. Option - 可选值

```java
import io.vavr.control.Option;

// 处理可能为空的值
Option<String> some = Option.of("hello");
Option<String> none = Option.none();

// 安全获取值
String value = some.getOrElse("default");

// 链式操作
Option<Integer> result = Option.of("123")
    .map(Integer::parseInt);
```

### 实际应用场景

#### 用户输入验证

```java
public Either<String, Integer> validateAge(String input) {
    return Try.of(() -> Integer.parseInt(input))
        .filter(age -> age >= 0 && age <= 150)
        .toEither()
        .mapLeft(error -> "Invalid age: " + input);
}
```

#### 数据处理管道

```java
public Try<Integer> processData(String[] inputs) {
    return Try.of(() -> inputs)
        .map(array -> {
            int sum = 0;
            for (String input : array) {
                sum += Try.of(() -> Integer.parseInt(input))
                    .getOrElse(0);
            }
            return sum;
        });
}
```

## Commons Net 使用指南

Commons Net 提供了网络相关的工具类，特别是 IP 地址和子网处理。

### 主要特性

#### 1. IP 地址验证

```java
import java.util.regex.Pattern;

// IPv4 验证正则
private static final Pattern IPV4_PATTERN = Pattern.compile(
    "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$"
);

public boolean isValidIPv4(String ipAddress) {
    if (ipAddress == null || ipAddress.trim().isEmpty()) {
        return false;
    }
    return IPV4_PATTERN.matcher(ipAddress).matches();
}
```

#### 2. CIDR 范围检查

```java
import org.apache.commons.net.util.SubnetUtils;

public boolean isInCIDR(String ipAddress, String cidr) {
    try {
        SubnetUtils subnetUtils = new SubnetUtils(cidr);
        subnetUtils.setInclusiveHostCount(true);
        return subnetUtils.getInfo().isInRange(ipAddress);
    } catch (Exception e) {
        return false;
    }
}

// 使用示例
boolean inRange = isInCIDR("192.168.1.5", "192.168.1.0/24");
```

#### 3. 子网信息获取

```java
public void getSubnetInfo(String cidr) {
    SubnetUtils subnetUtils = new SubnetUtils(cidr);
    subnetUtils.setInclusiveHostCount(true);
    
    SubnetUtils.SubnetInfo info = subnetUtils.getInfo();
    
    System.out.println("网络地址: " + info.getNetworkAddress());
    System.out.println("广播地址: " + info.getBroadcastAddress());
    System.out.println("子网掩码: " + info.getNetmask());
    System.out.println("地址数量: " + info.getAddressCount());
    System.out.println("最低地址: " + info.getLowAddress());
    System.out.println("最高地址: " + info.getHighAddress());
}
```

#### 4. 端口验证

```java
public boolean isValidPort(int port) {
    return port >= 0 && port <= 65535;
}

public boolean isUserPort(int port) {
    return port >= 1024 && port <= 65535;
}
```

### 实际应用场景

#### 网络配置验证

```java
public class NetworkConfig {
    private final String ip;
    private final String cidr;
    private final int port;
    
    public boolean isValid() {
        return isValidIP(ip) &&
               isInCIDR(ip, cidr) &&
               isValidPort(port);
    }
}
```

#### 私有IP检查

```java
public boolean isPrivateIP(String ipAddress) {
    if (!isValidIPv4(ipAddress)) {
        return false;
    }
    
    return isInCIDR(ipAddress, "10.0.0.0/8") ||
           isInCIDR(ipAddress, "172.16.0.0/12") ||
           isInCIDR(ipAddress, "192.168.0.0/16") ||
           isInCIDR(ipAddress, "127.0.0.0/8");
}
```

## 依赖配置

确保在 `pom.xml` 中添加以下依赖：

```xml
<!-- Vavr -->
<dependency>
    <groupId>io.vavr</groupId>
    <artifactId>vavr</artifactId>
    <version>0.10.4</version>
</dependency>

<!-- Commons Net -->
<dependency>
    <groupId>commons-net</groupId>
    <artifactId>commons-net</artifactId>
    <version>3.9.0</version>
</dependency>

<!-- JUnit 5 for testing -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.9.2</version>
    <scope>test</scope>
</dependency>
```

## 最佳实践

### Vavr 最佳实践

1. **使用 Try 处理异常**：避免使用 try-catch 块，使用 Try 进行函数式异常处理
2. **使用 Either 表示结果**：明确区分成功和失败情况
3. **使用 Option 处理空值**：避免 null 检查，使用 Option 类型
4. **链式操作**：利用 map、flatMap 等方法进行数据转换

### Commons Net 最佳实践

1. **IP 地址验证**：始终验证 IP 地址格式
2. **CIDR 范围检查**：使用 SubnetUtils 进行子网范围验证
3. **端口验证**：检查端口号的有效范围
4. **异常处理**：妥善处理网络操作可能抛出的异常

## 测试用例

项目包含了完整的测试用例，展示了各种使用场景：

- `VavrUsageTest.java` - Vavr 使用示例
- `CommonsNetUsageTest.java` - Commons Net 使用示例

运行测试以查看具体的使用方法：

```bash
mvn test -Dtest=VavrUsageTest
mvn test -Dtest=CommonsNetUsageTest
```

## 总结

Vavr 和 Commons Net 为 Java 开发提供了强大的函数式编程和网络处理能力。通过合理使用这些库，可以：

- 提高代码的可读性和可维护性
- 减少异常处理的复杂性
- 简化网络相关的操作
- 提供更好的类型安全性

建议在项目中充分利用这些库的特性，提升开发效率和代码质量。 