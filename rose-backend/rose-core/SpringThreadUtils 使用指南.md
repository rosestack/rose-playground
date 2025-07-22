# SpringThreadUtils 使用指南

本文档介绍如何使用基于 Spring 框架的线程工具类 `SpringThreadUtils`，它是原有 `ThreadUtils` 的增强替代方案。

## 概述

`SpringThreadUtils` 基于 Spring 框架的 `ThreadPoolTaskExecutor` 和并发工具，提供了更强大和灵活的线程管理功能，包括：

- 线程池创建和管理
- 异步任务执行
- 批量任务处理
- 超时控制
- 线程池监控
- 异常处理

## 主要功能

### 1. 基础功能

#### 线程睡眠
```java
// 线程睡眠 100 毫秒
SpringThreadUtils.sleep(100);

// 零或负数不会执行睡眠
SpringThreadUtils.sleep(0);
SpringThreadUtils.sleep(-100);
```

#### 异常处理
```java
// 处理线程异常
RuntimeException exception = new RuntimeException("Test exception");
SpringThreadUtils.printException(null, exception);

// 处理 Future 异常
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
    throw new RuntimeException("Future exception");
});
SpringThreadUtils.sleep(100);
// Future 异常会被自动处理
```

### 2. 线程池管理

#### 创建线程池
```java
// 创建 ThreadPoolTaskExecutor
ThreadPoolTaskExecutor executor = SpringThreadUtils.createThreadPoolTaskExecutor(
    2,    // 核心线程数
    4,    // 最大线程数
    10,   // 队列容量
    "my-task-"  // 线程名前缀
);

// 使用线程池
executor.submit(() -> {
    // 执行任务
    System.out.println("Task executed");
});

// 优雅关闭
SpringThreadUtils.shutdownAndAwaitTermination(executor);
```

#### 获取线程池状态
```java
ThreadPoolTaskExecutor executor = SpringThreadUtils.createThreadPoolTaskExecutor(2, 4, 10, "status-");

// 获取状态信息
String status = SpringThreadUtils.getThreadPoolStatus(executor);
System.out.println(status);
// 输出: ThreadPool[status-] - Active: 0, PoolSize: 0, CorePoolSize: 2, MaxPoolSize: 4, QueueSize: 0, CompletedTasks: 0
```

#### 监控线程池
```java
// 监控线程池状态，每 100ms 输出一次，持续 1 秒
SpringThreadUtils.monitorThreadPool(executor, 100, 1000);
```

### 3. 异步任务执行

#### 使用 CompletableFuture
```java
ThreadPoolTaskExecutor executor = SpringThreadUtils.createThreadPoolTaskExecutor(2, 4, 10, "async-");

// 提交异步任务
CompletableFuture<Integer> future = SpringThreadUtils.submitCompletable(
    executor,
    () -> {
        SpringThreadUtils.sleep(100);
        return 42;
    }
);

// 获取结果
Integer apiResponse = future.get(1, TimeUnit.SECONDS);
System.out.println("Result: " + apiResponse); // 输出: ApiResponse: 42
```

#### 带超时的任务执行
```java
// 正常超时
Integer apiResponse = SpringThreadUtils.submitWithTimeout(
    executor,
    () -> {
        SpringThreadUtils.sleep(100);
        return 100;
    },
    1,
    TimeUnit.SECONDS
);

// 超时异常
try {
    SpringThreadUtils.submitWithTimeout(
        executor,
        () -> {
            SpringThreadUtils.sleep(2000);
            return 200;
        },
        500,
        TimeUnit.MILLISECONDS
    );
} catch (TimeoutException e) {
    System.out.println("Task timed out");
}
```

### 4. 批量任务处理

#### 提交批量任务
```java
ThreadPoolTaskExecutor executor = SpringThreadUtils.createThreadPoolTaskExecutor(4, 8, 20, "batch-");

// 创建多个任务
@SuppressWarnings("unchecked")
CompletableFuture<Integer>[] futures = SpringThreadUtils.submitBatch(
    executor,
    () -> {
        SpringThreadUtils.sleep(100);
        return 1;
    },
    () -> {
        SpringThreadUtils.sleep(150);
        return 2;
    },
    () -> {
        SpringThreadUtils.sleep(200);
        return 3;
    }
);

// 等待所有任务完成
Integer[] results = SpringThreadUtils.waitForAll(futures);
// results: [1, 2, 3]
```

### 5. 任务创建工具

#### 创建计数器任务
```java
// 创建计数器任务：计数 5 次，每次间隔 50ms
var counterTask = SpringThreadUtils.createCounterTask("MyCounter", 5, 50);
CompletableFuture<Integer> future = SpringThreadUtils.submitCompletable(executor, counterTask);

Integer apiResponse = future.get(1, TimeUnit.SECONDS);
System.out.println("Final count: " + apiResponse); // 输出: Final count: 5
```

#### 创建异常任务
```java
// 创建异常任务：延迟 100ms 后抛出异常
RuntimeException exception = new RuntimeException("Test exception");
var exceptionTask = SpringThreadUtils.createExceptionTask("TestException", 100, exception);
CompletableFuture<Void> future = SpringThreadUtils.submitCompletable(executor, exceptionTask);

try {
    future.get(1, TimeUnit.SECONDS);
} catch (ExecutionException e) {
    System.out.println("Caught exception: " + e.getCause().getMessage());
}
```

## 实际应用场景

### 1. 数据处理管道
```java
ThreadPoolTaskExecutor executor = SpringThreadUtils.createThreadPoolTaskExecutor(4, 8, 50, "data-");

try {
    // 第一阶段：数据加载
    CompletableFuture<List<String>> dataFuture = SpringThreadUtils.submitCompletable(
        executor,
        () -> loadDataFromDatabase()
    );

    // 第二阶段：数据处理
    CompletableFuture<List<String>> processedFuture = dataFuture.thenApplyAsync(
        data -> processData(data),
        executor
    );

    // 第三阶段：数据保存
    CompletableFuture<Void> saveFuture = processedFuture.thenAcceptAsync(
        processedData -> saveData(processedData),
        executor
    );

    // 等待所有阶段完成
    saveFuture.get(30, TimeUnit.SECONDS);
} finally {
    SpringThreadUtils.shutdownAndAwaitTermination(executor);
}
```

### 2. 并发 API 调用
```java
ThreadPoolTaskExecutor executor = SpringThreadUtils.createThreadPoolTaskExecutor(10, 20, 100, "api-");

try {
    List<String> urls = Arrays.asList("http://api1.com", "http://api2.com", "http://api3.com");
    
    // 并发调用多个 API
    @SuppressWarnings("unchecked")
    CompletableFuture<String>[] apiFutures = urls.stream()
        .map(url -> SpringThreadUtils.submitCompletable(
            executor,
            () -> callApi(url)
        ))
        .toArray(CompletableFuture[]::new);

    // 等待所有 API 调用完成
    String[] results = SpringThreadUtils.waitForAll(apiFutures);
    
    // 处理结果
    for (String apiResponse : results) {
        System.out.println("API apiResponse: " + apiResponse);
    }
} finally {
    SpringThreadUtils.shutdownAndAwaitTermination(executor);
}
```

### 3. 定时任务监控
```java
ThreadPoolTaskExecutor executor = SpringThreadUtils.createThreadPoolTaskExecutor(2, 4, 10, "monitor-");

// 启动监控任务
executor.submit(() -> {
    while (!Thread.currentThread().isInterrupted()) {
        String status = SpringThreadUtils.getThreadPoolStatus(executor);
        log.info("Thread pool status: {}", status);
        SpringThreadUtils.sleep(5000); // 每 5 秒监控一次
    }
});

// 提交业务任务
for (int i = 0; i < 10; i++) {
    final int taskId = i;
    executor.submit(() -> {
        log.info("Executing task {}", taskId);
        SpringThreadUtils.sleep(1000);
        log.info("Task {} completed", taskId);
    });
}

// 运行一段时间后关闭
SpringThreadUtils.sleep(30000);
SpringThreadUtils.shutdownAndAwaitTermination(executor);
```

## 最佳实践

### 1. 线程池配置
```java
// 根据任务类型配置线程池
ThreadPoolTaskExecutor executor;

// CPU 密集型任务
executor = SpringThreadUtils.createThreadPoolTaskExecutor(
    Runtime.getRuntime().availableProcessors(),
    Runtime.getRuntime().availableProcessors() * 2,
    100,
    "cpu-intensive-"
);

// IO 密集型任务
executor = SpringThreadUtils.createThreadPoolTaskExecutor(
    Runtime.getRuntime().availableProcessors() * 2,
    Runtime.getRuntime().availableProcessors() * 4,
    200,
    "io-intensive-"
);
```

### 2. 异常处理
```java
CompletableFuture<Integer> future = SpringThreadUtils.submitCompletable(
    executor,
    () -> {
        try {
            return riskyOperation();
        } catch (Exception e) {
            log.error("Operation failed", e);
            throw new RuntimeException("Operation failed", e);
        }
    }
);

try {
    Integer apiResponse = future.get(10, TimeUnit.SECONDS);
    // 处理成功结果
} catch (TimeoutException e) {
    log.error("Operation timed out");
    // 处理超时
} catch (ExecutionException e) {
    log.error("Operation failed", e.getCause());
    // 处理执行异常
}
```

### 3. 资源管理

```java
ThreadPoolTaskExecutor executor = null;
try {
    executor = SpringThreadUtils.createThreadPoolTaskExecutor(2, 4, 10, "resource-");
    
    // 使用线程池
    CompletableFuture<String> future = SpringThreadUtils.submitCompletable(
        executor,
        () -> "Task completed"
    );
    
    String apiResponse = future.get(5, TimeUnit.SECONDS);
    System.out.println(apiResponse);
    
} finally {
    // 确保线程池被正确关闭
    if (executor != null) {
        SpringThreadUtils.shutdownAndAwaitTermination(executor);
    }
}
```

## 总结

`SpringThreadUtils` 提供了比原有 `ThreadUtils` 更强大和灵活的功能，特别适合需要复杂线程管理的应用场景。它基于 Spring 框架，提供了更好的集成性和稳定性，同时保持了与原有 API 的兼容性。

建议在新项目中使用 `SpringThreadUtils`，并在现有项目中逐步迁移。 