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