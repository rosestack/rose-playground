 

本文档介绍如何使用增强的弹性模式工具类 `ResilienceUtils`，它提供了断路器、指数退避重试、超时控制等高级功能，作为 Failsafe 库的轻量级替代方案。

## 概述

`ResilienceUtils` 基于 Vavr 和现有的项目架构，提供了以下弹性模式：

- **断路器模式**：防止级联故障
- **指数退避重试**：智能重试策略
- **超时控制**：防止长时间等待
- **组合策略**：多种模式的灵活组合

## 主要功能

### 1. 断路器模式

断路器模式用于防止级联故障，当依赖服务出现问题时快速失败。

```java
import io.github.rose.core.util.ResilienceUtils;
import io.vavr.control.Try;

// 创建断路器：5次失败后开启，1分钟后重置
ResilienceUtils.CircuitBreaker circuitBreaker = 
    new ResilienceUtils.CircuitBreaker(5, Duration.ofMinutes(1));

// 使用断路器包装操作
Try<String> result = circuitBreaker.execute(() -> 
    Try.of(() -> riskyOperation())
);

if (result.isSuccess()) {
    System.out.println("操作成功: " + result.get());
} else {
    System.out.println("操作失败: " + result.getCause().getMessage());
}
```

### 2. 指数退避重试

指数退避重试提供智能的重试策略，避免对失败的服务造成额外压力。

```java
// 创建指数退避重试策略
ResilienceUtils.ExponentialBackoffRetry retry = new ResilienceUtils.ExponentialBackoffRetry(
    3,                    // 最大重试次数
    Duration.ofSeconds(1), // 初始延迟
    2.0,                  // 退避倍数
    Duration.ofSeconds(30), // 最大延迟
    throwable -> throwable instanceof RuntimeException // 重试条件
);

// 使用重试策略
Try<String> result = retry.execute(() -> 
    Try.of(() -> networkCall())
);
```

### 3. 组合策略

组合多种弹性策略，提供更强大的容错能力。

```java
// 创建组合策略
ResilienceUtils.CompositeResilience resilience = 
    ResilienceUtils.createCompositeResilience();

// 使用组合策略
Try<String> result = resilience.execute(() -> 
    Try.of(() -> complexOperation())
);
```

### 4. 便捷方法

使用预定义的配置快速创建弹性策略。

```java
// 使用默认配置
ResilienceUtils.CircuitBreaker defaultBreaker = ResilienceUtils.createCircuitBreaker();
ResilienceUtils.ExponentialBackoffRetry defaultRetry = ResilienceUtils.createExponentialBackoffRetry();
ResilienceUtils.CompositeResilience defaultResilience = ResilienceUtils.createCompositeResilience();
```

## 实际应用场景

### 1. 外部 API 调用

```java
@Service
public class ExternalApiService {
    
    private final ResilienceUtils.NetworkResilienceExample networkResilience;
    
    public ExternalApiService() {
        this.networkResilience = new ResilienceUtils.NetworkResilienceExample();
    }
    
    public Try<ApiResponse> callExternalApi(String endpoint) {
        return networkResilience.executeNetworkCall(() -> 
            Try.of(() -> {
                // 实际的 API 调用
                return restTemplate.getForObject(endpoint, ApiResponse.class);
            })
        );
    }
}
```

### 2. 数据库操作

```java
@Service
public class DatabaseService {
    
    private final ResilienceUtils.CircuitBreaker circuitBreaker;
    
    public DatabaseService() {
        // 数据库操作使用更严格的断路器配置
        this.circuitBreaker = new ResilienceUtils.CircuitBreaker(3, Duration.ofMinutes(5));
    }
    
    public Try<User> findUserById(Long id) {
        return circuitBreaker.execute(() -> 
            Try.of(() -> userRepository.findById(id).orElseThrow())
        );
    }
}
```

### 3. 消息队列处理

```java
@Component
public class MessageProcessor {
    
    private final ResilienceUtils.ExponentialBackoffRetry retry;
    
    public MessageProcessor() {
        // 消息处理使用重试策略
        this.retry = new ResilienceUtils.ExponentialBackoffRetry(
            5, Duration.ofSeconds(2), 2.0, Duration.ofMinutes(1),
            throwable -> throwable instanceof ProcessingException
        );
    }
    
    public Try<Void> processMessage(Message message) {
        return retry.execute(() -> 
            Try.of(() -> {
                // 消息处理逻辑
                processMessageInternal(message);
                return null;
            })
        );
    }
}
```

## 与 VavrCheckedUtils 集成

`ResilienceUtils` 可以与现有的 `VavrCheckedUtils` 无缝集成：

```java
// 组合使用
Function1<String, Try<Void>> consumer = s -> Try.of(() -> {
    // 业务逻辑
    processString(s);
    return null;
});

// 添加断路器
Function1<String, Try<Void>> resilientConsumer = 
    ResilienceUtils.withCircuitBreaker(consumer, ResilienceUtils.createCircuitBreaker());

// 添加重试
Function1<String, Try<Void>> retryConsumer = 
    ResilienceUtils.withExponentialBackoff(consumer, ResilienceUtils.createExponentialBackoffRetry());

// 添加组合策略
Function1<String, Try<Void>> compositeConsumer = 
    ResilienceUtils.withCompositeResilience(consumer, ResilienceUtils.createCompositeResilience());
```

## 监控和日志

`ResilienceUtils` 提供了详细的日志记录，便于监控和调试：

```java
// 断路器状态监控
ResilienceUtils.CircuitBreaker breaker = ResilienceUtils.createCircuitBreaker();
System.out.println("断路器状态: " + breaker.getState());
System.out.println("失败次数: " + breaker.getFailureCount());

// 日志示例
// INFO  - Circuit breaker transitioning to HALF_OPEN
// WARN  - Circuit breaker opened after 5 failures
// INFO  - Circuit breaker reset to CLOSED
// WARN  - Operation failed, retrying in 1000ms (attempt 2/3)
```

## 配置建议

### 1. 断路器配置

- **失败阈值**：根据服务特性调整（3-10次）
- **重置超时**：根据服务恢复时间调整（1-10分钟）
- **监控**：定期检查断路器状态

### 2. 重试配置

- **最大重试次数**：避免无限重试（3-5次）
- **初始延迟**：避免立即重试（1-5秒）
- **退避倍数**：指数增长（1.5-3.0）
- **最大延迟**：限制最大等待时间（30秒-5分钟）

### 3. 超时配置

- **操作超时**：根据操作复杂度调整（5秒-30秒）
- **网络超时**：根据网络状况调整（10秒-60秒）

## 最佳实践

### 1. 选择合适的策略

- **断路器**：适用于外部依赖服务
- **重试**：适用于临时性故障
- **超时**：适用于所有网络操作
- **组合策略**：适用于关键业务操作

### 2. 异常处理

```java
Try<String> result = resilience.execute(() -> 
    Try.of(() -> riskyOperation())
);

result.onSuccess(value -> {
    // 处理成功结果
    log.info("操作成功: {}", value);
}).onFailure(error -> {
    // 处理失败情况
    log.error("操作失败: {}", error.getMessage());
    // 可以触发告警、降级等
});
```

### 3. 性能考虑

- 避免在循环中创建新的弹性策略实例
- 复用策略实例以提高性能
- 监控策略的执行时间和资源消耗

### 4. 测试策略

```java
@Test
void testCircuitBreaker() {
    ResilienceUtils.CircuitBreaker breaker = new ResilienceUtils.CircuitBreaker(2, Duration.ofMinutes(1));
    
    // 模拟失败
    Try<String> result1 = breaker.execute(() -> Try.failure(new RuntimeException("Error 1")));
    Try<String> result2 = breaker.execute(() -> Try.failure(new RuntimeException("Error 2")));
    
    // 第三次应该快速失败
    Try<String> result3 = breaker.execute(() -> Try.success("Success"));
    
    assertTrue(result1.isFailure());
    assertTrue(result2.isFailure());
    assertTrue(result3.isFailure()); // 断路器已开启
    assertEquals(ResilienceUtils.CircuitBreaker.CircuitState.OPEN, breaker.getState());
}
```

## 总结

`ResilienceUtils` 提供了强大的弹性模式支持，同时保持了与现有代码的兼容性。相比引入 Failsafe 库，这个方案具有以下优势：

1. **轻量级**：不增加额外的依赖
2. **易集成**：基于现有的 Vavr 架构
3. **可定制**：可以根据项目需求调整
4. **易维护**：团队熟悉的技术栈

建议在项目中优先使用 `ResilienceUtils`，只有在需要更复杂的弹性模式时才考虑引入 Failsafe 库。