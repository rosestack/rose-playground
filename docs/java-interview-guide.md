# 🎯 高级 Java 后端研发面试题库

## 📋 目录

1. [JVM 虚拟机](#1-jvm-虚拟机)
2. [Redis 缓存](#2-redis-缓存)
3. [RocketMQ 消息队列](#3-rocketmq-消息队列)
4. [Kafka 消息队列](#4-kafka-消息队列)
5. [Elasticsearch 搜索引擎](#5-elasticsearch-搜索引擎)
6. [MySQL 数据库](#6-mysql-数据库)
7. [Spring Boot 框架](#7-spring-boot-框架)
8. [分布式系统](#8-分布式系统)
9. [系统设计](#9-系统设计)
10. [面试评分标准](#10-面试评分标准)

---

## 1. JVM 虚拟机

### 1.1 JVM 内存模型和垃圾回收机制

**题目**: 详细描述 JVM 内存模型，并解释不同垃圾回收器的特点和适用场景。

**参考答案**:

#### JVM 内存模型
```
JVM 内存结构:
├── 堆内存 (Heap)
│   ├── 新生代 (Young Generation)
│   │   ├── Eden 区
│   │   ├── Survivor 0 (S0)
│   │   └── Survivor 1 (S1)
│   └── 老年代 (Old Generation)
├── 方法区 (Method Area) / 元空间 (Metaspace)
├── 程序计数器 (PC Register)
├── 虚拟机栈 (JVM Stack)
└── 本地方法栈 (Native Method Stack)
```

#### 垃圾回收器对比
| 回收器 | 类型 | 特点 | 适用场景 |
|--------|------|------|----------|
| **Serial GC** | 单线程 | 简单、停顿时间长 | 小型应用、客户端 |
| **Parallel GC** | 多线程 | 吞吐量优先 | 服务端应用 |
| **CMS** | 并发 | 低延迟、内存碎片 | 响应时间敏感 |
| **G1** | 分区 | 可预测停顿 | 大堆内存应用 |
| **ZGC/Shenandoah** | 超低延迟 | 毫秒级停顿 | 超大堆、实时应用 |

#### 调优参数示例
```bash
# G1 垃圾回收器调优
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:G1NewSizePercent=30
-XX:G1MaxNewSizePercent=40
-XX:G1MixedGCCountTarget=8
-XX:InitiatingHeapOccupancyPercent=45
```

**评分要点**:
- 内存模型理解深度 (30%)
- 垃圾回收器特点掌握 (40%)
- 实际调优经验 (30%)

---

### 1.2 类加载机制和双亲委派模型

**题目**: 解释 Java 类加载机制，双亲委派模型的工作原理，以及如何打破双亲委派。

**参考答案**:

#### 类加载过程
```java
public class ClassLoadingDemo {
    /**
     * 类加载的五个阶段：
     * 1. 加载 (Loading)
     * 2. 验证 (Verification)
     * 3. 准备 (Preparation)
     * 4. 解析 (Resolution)
     * 5. 初始化 (Initialization)
     */
    
    // 准备阶段：静态变量分配内存并设置默认值
    private static int count = 100;  // 准备阶段 count = 0，初始化阶段 count = 100
    
    // 初始化阶段：执行静态代码块
    static {
        System.out.println("类初始化");
        count = 200;
    }
}
```

#### 双亲委派模型
```java
public class CustomClassLoader extends ClassLoader {
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 1. 检查类是否已经加载
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            try {
                // 2. 委派给父类加载器
                if (getParent() != null) {
                    c = getParent().loadClass(name, false);
                } else {
                    c = findBootstrapClassOrNull(name);
                }
            } catch (ClassNotFoundException e) {
                // 3. 父类加载器无法加载，自己尝试加载
                c = findClass(name);
            }
        }
        
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // 自定义类加载逻辑
        byte[] classData = loadClassData(name);
        return defineClass(name, classData, 0, classData.length);
    }
    
    private byte[] loadClassData(String className) {
        // 从文件系统、网络等加载字节码
        return new byte[0];
    }
}
```

**评分要点**:
- 类加载过程理解 (25%)
- 双亲委派原理 (35%)
- 打破双亲委派的场景和方法 (40%)

---

### 1.3 JVM 性能调优和故障排查

**题目**: 描述 JVM 性能调优的思路和常用工具，以及如何排查内存泄漏和 CPU 飙升问题。

**参考答案**:

#### 性能调优思路
```bash
# 1. 性能分析流程
监控指标 → 定位瓶颈 → 参数调优 → 验证效果

# 2. 关键监控指标
- GC 频率和停顿时间
- 内存使用率和分布
- CPU 使用率
- 线程状态
- 响应时间和吞吐量
```

#### 常用调优参数
```bash
# 堆内存设置
-Xms4g -Xmx4g                    # 初始和最大堆内存
-XX:NewRatio=3                   # 老年代与新生代比例
-XX:SurvivorRatio=8              # Eden 与 Survivor 比例

# 垃圾回收器选择
-XX:+UseG1GC                     # 使用 G1 回收器
-XX:MaxGCPauseMillis=200         # 最大 GC 停顿时间
-XX:G1HeapRegionSize=16m         # G1 区域大小

# GC 日志
-Xloggc:gc.log                   # GC 日志文件
-XX:+PrintGCDetails              # 详细 GC 信息
-XX:+PrintGCTimeStamps           # GC 时间戳
-XX:+UseGCLogFileRotation        # 日志轮转
-XX:NumberOfGCLogFiles=5         # 日志文件数量
-XX:GCLogFileSize=100M           # 单个日志文件大小

# 内存溢出处理
-XX:+HeapDumpOnOutOfMemoryError  # OOM 时生成堆转储
-XX:HeapDumpPath=/logs/heapdump  # 堆转储文件路径
```

**评分要点**:
- 调优思路和方法论 (30%)
- 工具使用熟练度 (25%)
- 故障排查经验 (25%)
- 实际案例分析 (20%)

---

## 2. Redis 缓存

### 2.1 Redis 数据结构和底层实现

**题目**: 详细说明 Redis 的五种基本数据类型及其底层实现，以及在什么场景下使用。

**参考答案**:

#### Redis 数据结构对比
| 数据类型 | 底层实现 | 使用场景 | 时间复杂度 |
|----------|----------|----------|------------|
| **String** | SDS (Simple Dynamic String) | 缓存、计数器、分布式锁 | O(1) |
| **Hash** | ziplist / hashtable | 对象存储、购物车 | O(1) |
| **List** | quicklist (ziplist + linkedlist) | 消息队列、时间线 | O(1) 头尾，O(N) 中间 |
| **Set** | intset / hashtable | 标签、好友关系 | O(1) |
| **ZSet** | ziplist / skiplist + hashtable | 排行榜、延时队列 | O(log N) |

#### 底层实现详解
```c
// 1. SDS (Simple Dynamic String) 结构
struct sdshdr {
    int len;        // 字符串长度
    int free;       // 剩余空间
    char buf[];     // 字符数组
};

// 优势：
// - O(1) 获取长度
// - 杜绝缓冲区溢出
// - 减少修改字符串时的内存重分配次数
// - 二进制安全
```

**评分要点**:
- 数据结构理解深度 (40%)
- 底层实现原理 (30%)
- 实际应用场景 (30%)

---

### 2.2 Redis 持久化和高可用

**题目**: 比较 RDB 和 AOF 两种持久化方式的优缺点，并说明 Redis 集群的搭建和故障转移机制。

**参考答案**:

#### 持久化方式对比
| 特性 | RDB | AOF |
|------|-----|-----|
| **文件大小** | 小，压缩存储 | 大，记录所有写操作 |
| **恢复速度** | 快 | 慢 |
| **数据安全性** | 可能丢失最后一次快照后的数据 | 可配置为每秒同步，最多丢失 1 秒数据 |
| **对性能影响** | fork 子进程时有短暂阻塞 | 持续写入，影响相对较小 |
| **文件格式** | 二进制，紧凑 | 文本，可读性好 |

**评分要点**:
- 持久化机制理解 (30%)
- 高可用架构设计 (35%)
- 故障转移原理 (35%)

---

### 2.3 Redis 性能优化和缓存策略

**题目**: 描述 Redis 性能优化的方法，以及常见的缓存策略和缓存问题的解决方案。

**参考答案**:

#### Redis 性能优化策略
```bash
# 1. 内存优化
# redis.conf
maxmemory 2gb                    # 设置最大内存
maxmemory-policy allkeys-lru     # 内存淘汰策略

# 数据结构优化
hash-max-ziplist-entries 512     # Hash 使用 ziplist 的最大条目数
hash-max-ziplist-value 64        # Hash 使用 ziplist 的最大值大小
list-max-ziplist-size -2         # List 使用 ziplist 的大小
set-max-intset-entries 512       # Set 使用 intset 的最大条目数
zset-max-ziplist-entries 128     # ZSet 使用 ziplist 的最大条目数
zset-max-ziplist-value 64        # ZSet 使用 ziplist 的最大值大小

# 2. 网络优化
tcp-keepalive 60                 # TCP keepalive 时间
timeout 300                      # 客户端空闲超时时间
```

**评分要点**:
- 性能优化方法 (30%)
- 缓存策略理解 (35%)
- 缓存问题解决方案 (35%)

---

## 3. RocketMQ 消息队列

### 3.1 RocketMQ 架构和消息模型

**题目**: 详细描述 RocketMQ 的架构组件，消息模型，以及与其他 MQ 的对比。

**参考答案**:

#### RocketMQ 架构组件
```
RocketMQ 架构:
├── NameServer
│   ├── 路由信息管理
│   ├── Broker 注册与发现
│   └── 客户端路由
├── Broker
│   ├── 消息存储
│   ├── 消息转发
│   ├── Master/Slave 模式
│   └── 消息过滤
├── Producer
│   ├── 消息发送
│   ├── 负载均衡
│   └── 故障转移
└── Consumer
    ├── 消息消费
    ├── 集群/广播模式
    └── 消费进度管理
```

#### 核心概念对比
| 概念 | RocketMQ | Kafka | RabbitMQ |
|------|----------|-------|----------|
| **消息存储** | 文件系统 | 文件系统 | 内存+磁盘 |
| **消息顺序** | 支持分区顺序 | 支持分区顺序 | 不保证 |
| **事务消息** | 支持 | 不支持 | 不支持 |
| **延时消息** | 支持 | 不支持 | 插件支持 |
| **消息回溯** | 支持 | 支持 | 不支持 |
| **消费模式** | 推拉结合 | 拉模式 | 推模式 |

**评分要点**:
- 架构理解深度 (35%)
- 消息模型掌握 (30%)
- 实际应用能力 (35%)

---

## 4. Kafka 消息队列

### 4.1 Kafka 架构和分区机制

**题目**: 详细说明 Kafka 的架构设计，分区机制，以及如何保证消息的顺序性和可靠性。

**参考答案**:

#### Kafka 架构组件
```
Kafka 架构:
├── Broker
│   ├── 消息存储
│   ├── 分区管理
│   └── 副本同步
├── Zookeeper/KRaft
│   ├── 集群协调
│   ├── 元数据管理
│   └── Leader 选举
├── Producer
│   ├── 消息发送
│   ├── 分区策略
│   └── 批量发送
└── Consumer
    ├── 消息消费
    ├── 消费组管理
    └── 偏移量管理
```

#### 分区机制详解
```java
@Service
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 1. 指定分区发送
     */
    public void sendToPartition(String topic, int partition, Object message) {
        kafkaTemplate.send(topic, partition, null, message);
    }

    /**
     * 2. 按 Key 分区发送
     */
    public void sendWithKey(String topic, String key, Object message) {
        // 相同 key 的消息会发送到同一分区，保证顺序性
        kafkaTemplate.send(topic, key, message);
    }

    /**
     * 3. 自定义分区策略
     */
    public void sendWithCustomPartitioner(String topic, Object message) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, message);
        kafkaTemplate.send(record);
    }
}

// 自定义分区器
public class CustomPartitioner implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes,
                        Object value, byte[] valueBytes, Cluster cluster) {

        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();

        if (key == null) {
            // 轮询分区
            return ThreadLocalRandom.current().nextInt(numPartitions);
        }

        // 基于 key 的哈希分区
        return Math.abs(key.hashCode()) % numPartitions;
    }

    @Override
    public void close() {}

    @Override
    public void configure(Map<String, ?> configs) {}
}
```

#### 消息可靠性保障
```java
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();

        // 基础配置
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // 可靠性配置
        props.put(ProducerConfig.ACKS_CONFIG, "all");              // 等待所有副本确认
        props.put(ProducerConfig.RETRIES_CONFIG, 3);               // 重试次数
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // 幂等性
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1); // 保证顺序

        // 性能配置
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);        // 批次大小
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);            // 等待时间
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);  // 缓冲区大小
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // 压缩算法

        return new DefaultKafkaProducerFactory<>(props);
    }
}

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // 基础配置
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // 可靠性配置
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");     // 偏移量重置策略
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);         // 手动提交偏移量
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed"); // 只读已提交消息

        // 性能配置
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);             // 最小拉取字节数
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);            // 最大等待时间
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);             // 单次拉取最大记录数

        return new DefaultKafkaConsumerFactory<>(props);
    }
}
```

**评分要点**:
- 架构理解深度 (40%)
- 分区机制掌握 (35%)
- 可靠性保障 (25%)

---

### 4.2 Kafka 性能优化和监控

**题目**: 描述 Kafka 的性能优化策略，以及如何进行集群监控和故障排查。

**参考答案**:

#### 性能优化策略
```bash
# server.properties 优化配置

# 1. 网络和 I/O 优化
num.network.threads=8                    # 网络线程数
num.io.threads=16                       # I/O 线程数
socket.send.buffer.bytes=102400         # Socket 发送缓冲区
socket.receive.buffer.bytes=102400      # Socket 接收缓冲区
socket.request.max.bytes=104857600      # 最大请求大小

# 2. 日志配置优化
log.segment.bytes=1073741824            # 日志段大小 1GB
log.retention.hours=168                 # 日志保留时间 7天
log.retention.bytes=1073741824000       # 日志保留大小 1TB
log.cleanup.policy=delete               # 日志清理策略

# 3. 副本配置优化
default.replication.factor=3            # 默认副本因子
min.insync.replicas=2                   # 最小同步副本数
unclean.leader.election.enable=false   # 禁用不完全 Leader 选举

# 4. 压缩配置
compression.type=snappy                 # 压缩算法
```

#### 监控指标和告警
```java
@Component
public class KafkaMonitorService {

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * 监控生产者指标
     */
    @EventListener
    public void handleProducerMetrics(ProducerMetricsEvent event) {
        // 记录发送速率
        meterRegistry.counter("kafka.producer.records.sent",
            "topic", event.getTopic()).increment();

        // 记录发送延迟
        meterRegistry.timer("kafka.producer.send.latency",
            "topic", event.getTopic()).record(event.getLatency(), TimeUnit.MILLISECONDS);

        // 记录错误率
        if (event.isError()) {
            meterRegistry.counter("kafka.producer.errors",
                "topic", event.getTopic(), "error", event.getErrorType()).increment();
        }
    }

    /**
     * 监控消费者指标
     */
    @EventListener
    public void handleConsumerMetrics(ConsumerMetricsEvent event) {
        // 记录消费速率
        meterRegistry.counter("kafka.consumer.records.consumed",
            "topic", event.getTopic(), "group", event.getGroupId()).increment();

        // 记录消费延迟
        meterRegistry.gauge("kafka.consumer.lag",
            Tags.of("topic", event.getTopic(), "partition", String.valueOf(event.getPartition())),
            event.getLag());

        // 记录处理时间
        meterRegistry.timer("kafka.consumer.processing.time",
            "topic", event.getTopic()).record(event.getProcessingTime(), TimeUnit.MILLISECONDS);
    }

    /**
     * 集群健康检查
     */
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void checkClusterHealth() {
        try (AdminClient adminClient = AdminClient.create(getAdminConfig())) {

            // 检查集群元数据
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            Collection<Node> nodes = clusterResult.nodes().get();

            meterRegistry.gauge("kafka.cluster.brokers.count", nodes.size());

            // 检查主题分区状态
            ListTopicsResult topicsResult = adminClient.listTopics();
            Set<String> topicNames = topicsResult.names().get();

            for (String topicName : topicNames) {
                DescribeTopicsResult topicResult = adminClient.describeTopics(
                    Collections.singletonList(topicName));

                TopicDescription description = topicResult.values().get(topicName).get();

                // 检查分区副本状态
                for (TopicPartitionInfo partition : description.partitions()) {
                    boolean hasUnderReplicatedPartitions =
                        partition.replicas().size() != partition.isr().size();

                    if (hasUnderReplicatedPartitions) {
                        log.warn("发现副本不足的分区: topic={}, partition={}",
                            topicName, partition.partition());

                        meterRegistry.counter("kafka.under.replicated.partitions",
                            "topic", topicName).increment();
                    }
                }
            }

        } catch (Exception e) {
            log.error("集群健康检查失败", e);
            meterRegistry.counter("kafka.health.check.errors").increment();
        }
    }

    private Map<String, Object> getAdminConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        return props;
    }
}
```

**评分要点**:
- 性能优化策略 (40%)
- 监控体系建设 (35%)
- 故障排查能力 (25%)

---

### 4.3 Kafka 流处理和 Kafka Streams

**题目**: 说明 Kafka Streams 的核心概念和使用场景，以及如何实现实时数据处理。

**参考答案**:

#### Kafka Streams 核心概念
```java
@Service
public class KafkaStreamsService {

    /**
     * 1. 基础流处理
     */
    @Bean
    public KStream<String, OrderEvent> orderStream(StreamsBuilder builder) {
        return builder.stream("order-events",
            Consumed.with(Serdes.String(), new JsonSerde<>(OrderEvent.class)))
            .filter((key, order) -> order.getAmount() > 100) // 过滤大额订单
            .mapValues(order -> {
                // 数据转换
                order.setProcessedTime(System.currentTimeMillis());
                return order;
            })
            .peek((key, order) -> log.info("处理订单: {}", order)); // 调试输出
    }

    /**
     * 2. 流表连接 (Stream-Table Join)
     */
    @Bean
    public KStream<String, EnrichedOrder> enrichedOrderStream(StreamsBuilder builder) {
        // 订单流
        KStream<String, OrderEvent> orderStream = builder.stream("order-events");

        // 用户表
        KTable<String, UserProfile> userTable = builder.table("user-profiles");

        // 流表连接，丰富订单信息
        return orderStream.join(userTable,
            (order, user) -> new EnrichedOrder(order, user),
            Joined.with(Serdes.String(),
                       new JsonSerde<>(OrderEvent.class),
                       new JsonSerde<>(UserProfile.class)));
    }

    /**
     * 3. 窗口聚合
     */
    @Bean
    public KTable<Windowed<String>, Long> orderCountByWindow(StreamsBuilder builder) {
        return builder.stream("order-events",
            Consumed.with(Serdes.String(), new JsonSerde<>(OrderEvent.class)))
            .groupByKey()
            .windowedBy(TimeWindows.of(Duration.ofMinutes(5))) // 5分钟窗口
            .count(Materialized.as("order-count-store"));
    }

    /**
     * 4. 复杂事件处理
     */
    @Bean
    public KStream<String, FraudAlert> fraudDetectionStream(StreamsBuilder builder) {
        KStream<String, PaymentEvent> paymentStream =
            builder.stream("payment-events");

        return paymentStream
            .groupByKey()
            .windowedBy(TimeWindows.of(Duration.ofMinutes(10)))
            .aggregate(
                PaymentAggregator::new,
                (key, payment, aggregator) -> {
                    aggregator.addPayment(payment);
                    return aggregator;
                },
                Materialized.with(Serdes.String(), new JsonSerde<>(PaymentAggregator.class))
            )
            .toStream()
            .filter((windowedKey, aggregator) -> aggregator.isSuspicious())
            .mapValues(aggregator -> new FraudAlert(
                windowedKey.key(),
                aggregator.getTotalAmount(),
                aggregator.getPaymentCount()
            ));
    }

    /**
     * 5. 状态存储查询
     */
    public Optional<Long> getOrderCount(String userId, Instant from, Instant to) {
        ReadOnlyWindowStore<String, Long> store =
            kafkaStreams.store(StoreQueryParameters.fromNameAndType(
                "order-count-store", QueryableStoreTypes.windowStore()));

        try (WindowStoreIterator<Long> iterator =
             store.fetch(userId, from, to)) {

            long totalCount = 0;
            while (iterator.hasNext()) {
                totalCount += iterator.next().value;
            }
            return Optional.of(totalCount);
        } catch (Exception e) {
            log.error("查询状态存储失败", e);
            return Optional.empty();
        }
    }
}
```

#### 流处理拓扑配置
```java
@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();

        // 基础配置
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "order-processing-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // 性能配置
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 4);              // 流处理线程数
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);           // 提交间隔
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024); // 缓存大小

        // 容错配置
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 3);              // 副本因子
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG,
                 StreamsConfig.EXACTLY_ONCE_V2);                           // 精确一次语义

        // 状态存储配置
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");    // 状态目录

        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public StreamsBuilderFactoryBeanConfigurer configurer() {
        return factoryBean -> {
            factoryBean.setStateListener((newState, oldState) -> {
                log.info("Kafka Streams 状态变更: {} -> {}", oldState, newState);

                if (newState == KafkaStreams.State.ERROR) {
                    // 处理错误状态
                    handleStreamsError();
                }
            });

            factoryBean.setUncaughtExceptionHandler((thread, exception) -> {
                log.error("Kafka Streams 未捕获异常", exception);
                return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;
            });
        };
    }

    private void handleStreamsError() {
        // 错误处理逻辑，如发送告警、重启应用等
    }
}
```

**评分要点**:
- Kafka Streams 概念理解 (35%)
- 流处理应用开发 (40%)
- 状态管理和容错 (25%)

---

## 5. Elasticsearch 搜索引擎

### 5.1 ES 索引和查询优化

**题目**: 说明 Elasticsearch 的索引结构，查询DSL，以及性能优化策略。

**参考答案**:

#### 索引结构设计
```json
{
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "analyzer": "ik_max_word",
        "search_analyzer": "ik_smart",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      },
      "content": {
        "type": "text",
        "analyzer": "ik_max_word"
      },
      "tags": {
        "type": "keyword"
      },
      "category": {
        "type": "keyword"
      },
      "price": {
        "type": "double"
      },
      "create_time": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss"
      },
      "location": {
        "type": "geo_point"
      },
      "status": {
        "type": "integer"
      },
      "suggest": {
        "type": "completion",
        "analyzer": "ik_max_word"
      }
    }
  },
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1,
    "refresh_interval": "30s",
    "analysis": {
      "analyzer": {
        "ik_max_word": {
          "type": "ik_max_word"
        },
        "ik_smart": {
          "type": "ik_smart"
        }
      }
    }
  }
}
```

#### 查询 DSL 示例
```java
@Service
public class ElasticsearchService {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    /**
     * 1. 复合查询 - 多条件搜索
     */
    public SearchHits<Product> searchProducts(ProductSearchRequest request) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 必须匹配条件
        if (StringUtils.hasText(request.getKeyword())) {
            boolQuery.must(QueryBuilders.multiMatchQuery(request.getKeyword())
                .field("title", 2.0f)  // 标题权重更高
                .field("content", 1.0f)
                .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                .fuzziness(Fuzziness.AUTO));
        }

        // 过滤条件
        if (request.getCategory() != null) {
            boolQuery.filter(QueryBuilders.termQuery("category", request.getCategory()));
        }

        if (request.getPriceRange() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price")
                .gte(request.getPriceRange().getMin())
                .lte(request.getPriceRange().getMax()));
        }

        if (request.getDateRange() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("create_time")
                .gte(request.getDateRange().getStart())
                .lte(request.getDateRange().getEnd()));
        }

        // 地理位置查询
        if (request.getLocation() != null) {
            boolQuery.filter(QueryBuilders.geoDistanceQuery("location")
                .point(request.getLocation().getLat(), request.getLocation().getLon())
                .distance(request.getLocation().getDistance(), DistanceUnit.KILOMETERS));
        }

        // 构建查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withQuery(boolQuery)
            .withSort(SortBuilders.scoreSort().order(SortOrder.DESC))
            .withSort(SortBuilders.fieldSort("create_time").order(SortOrder.DESC))
            .withPageable(PageRequest.of(request.getPage(), request.getSize()))
            .withHighlightFields(
                new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
            )
            .build();

        return elasticsearchTemplate.search(searchQuery, Product.class);
    }

    /**
     * 2. 聚合查询 - 统计分析
     */
    public Map<String, Object> getProductStatistics() {
        // 构建聚合查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .withAggregations(
                // 按分类统计
                AggregationBuilders.terms("category_stats").field("category").size(10),

                // 价格区间统计
                AggregationBuilders.histogram("price_histogram")
                    .field("price")
                    .interval(100),

                // 日期直方图
                AggregationBuilders.dateHistogram("date_histogram")
                    .field("create_time")
                    .calendarInterval(DateHistogramInterval.DAY),

                // 统计指标
                AggregationBuilders.stats("price_stats").field("price"),

                // 嵌套聚合
                AggregationBuilders.terms("category_price_stats")
                    .field("category")
                    .subAggregation(AggregationBuilders.avg("avg_price").field("price"))
            )
            .withPageable(PageRequest.of(0, 0)) // 不需要返回文档
            .build();

        SearchHits<Product> searchHits = elasticsearchTemplate.search(searchQuery, Product.class);

        // 解析聚合结果
        Map<String, Object> result = new HashMap<>();
        Aggregations aggregations = searchHits.getAggregations();

        if (aggregations != null) {
            // 分类统计
            Terms categoryStats = aggregations.get("category_stats");
            result.put("categoryStats", categoryStats.getBuckets().stream()
                .collect(Collectors.toMap(
                    Terms.Bucket::getKeyAsString,
                    Terms.Bucket::getDocCount
                )));

            // 价格统计
            Stats priceStats = aggregations.get("price_stats");
            result.put("priceStats", Map.of(
                "min", priceStats.getMin(),
                "max", priceStats.getMax(),
                "avg", priceStats.getAvg(),
                "sum", priceStats.getSum(),
                "count", priceStats.getCount()
            ));
        }

        return result;
    }

    /**
     * 3. 自动补全查询
     */
    public List<String> getSuggestions(String prefix) {
        CompletionSuggestionBuilder suggestionBuilder =
            SuggestBuilders.completionSuggestion("suggest")
                .prefix(prefix)
                .size(10);

        SuggestBuilder suggestBuilder = new SuggestBuilder()
            .addSuggestion("product_suggest", suggestionBuilder);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withSuggestBuilder(suggestBuilder)
            .build();

        SearchHits<Product> searchHits = elasticsearchTemplate.search(searchQuery, Product.class);

        return searchHits.getSuggest()
            .getSuggestion("product_suggest")
            .getEntries()
            .stream()
            .flatMap(entry -> entry.getOptions().stream())
            .map(option -> option.getText().string())
            .distinct()
            .collect(Collectors.toList());
    }
}
```

#### 性能优化策略
```java
@Configuration
public class ElasticsearchConfig {

    /**
     * 1. 连接池配置优化
     */
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        RestClientBuilder builder = RestClient.builder(
            new HttpHost("localhost", 9200, "http")
        );

        // 连接池配置
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(100);
            httpClientBuilder.setMaxConnPerRoute(50);
            httpClientBuilder.setConnectionTimeToLive(5, TimeUnit.MINUTES);
            return httpClientBuilder;
        });

        // 请求配置
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(5000);
            requestConfigBuilder.setSocketTimeout(60000);
            return requestConfigBuilder;
        });

        return new RestHighLevelClient(builder);
    }

    /**
     * 2. 批量操作优化
     */
    @Service
    public static class BulkOperationService {

        @Autowired
        private ElasticsearchRestTemplate elasticsearchTemplate;

        public void bulkIndex(List<Product> products) {
            List<IndexQuery> queries = products.stream()
                .map(product -> new IndexQueryBuilder()
                    .withId(product.getId())
                    .withObject(product)
                    .build())
                .collect(Collectors.toList());

            // 批量索引，每批1000条
            int batchSize = 1000;
            for (int i = 0; i < queries.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, queries.size());
                List<IndexQuery> batch = queries.subList(i, endIndex);

                elasticsearchTemplate.bulkIndex(batch, IndexCoordinates.of("products"));
            }
        }

        public void bulkUpdate(List<Product> products) {
            List<UpdateQuery> queries = products.stream()
                .map(product -> UpdateQuery.builder(product.getId())
                    .withDocument(Document.from(product))
                    .withDocAsUpsert(true)
                    .build())
                .collect(Collectors.toList());

            elasticsearchTemplate.bulkUpdate(queries, IndexCoordinates.of("products"));
        }
    }
}
```

**评分要点**:
- 索引设计能力 (35%)
- 查询优化技巧 (40%)
- 性能调优经验 (25%)

---

### 5.2 ES 集群架构和运维

**题目**: 描述 Elasticsearch 集群的架构设计，分片策略，以及日常运维监控。

**参考答案**:

#### 集群架构设计
```yaml
# elasticsearch.yml 集群配置

# 集群配置
cluster.name: my-es-cluster
node.name: es-node-1
node.roles: [master, data, ingest]

# 网络配置
network.host: 0.0.0.0
http.port: 9200
transport.port: 9300

# 发现配置
discovery.seed_hosts: ["es-node-1:9300", "es-node-2:9300", "es-node-3:9300"]
cluster.initial_master_nodes: ["es-node-1", "es-node-2", "es-node-3"]

# 内存配置
bootstrap.memory_lock: true

# 数据路径
path.data: /var/lib/elasticsearch
path.logs: /var/log/elasticsearch

# 性能优化
indices.memory.index_buffer_size: 20%
indices.queries.cache.size: 10%
indices.fielddata.cache.size: 20%
```

#### 分片策略和索引模板
```java
@Service
public class IndexManagementService {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    /**
     * 创建索引模板
     */
    public void createIndexTemplate() {
        Map<String, Object> template = Map.of(
            "index_patterns", List.of("logs-*"),
            "template", Map.of(
                "settings", Map.of(
                    "number_of_shards", 3,
                    "number_of_replicas", 1,
                    "refresh_interval", "30s",
                    "index.lifecycle.name", "logs-policy",
                    "index.lifecycle.rollover_alias", "logs"
                ),
                "mappings", Map.of(
                    "properties", Map.of(
                        "timestamp", Map.of("type", "date"),
                        "level", Map.of("type", "keyword"),
                        "message", Map.of("type", "text"),
                        "service", Map.of("type", "keyword")
                    )
                )
            ),
            "priority", 100
        );

        // 创建索引模板
        PutIndexTemplateRequest request = new PutIndexTemplateRequest("logs-template")
            .source(template, XContentType.JSON);

        try {
            elasticsearchTemplate.execute(client -> {
                return client.indices().putIndexTemplate(request, RequestOptions.DEFAULT);
            });
        } catch (Exception e) {
            log.error("创建索引模板失败", e);
        }
    }

    /**
     * 索引生命周期管理
     */
    public void createILMPolicy() {
        Map<String, Object> policy = Map.of(
            "policy", Map.of(
                "phases", Map.of(
                    "hot", Map.of(
                        "actions", Map.of(
                            "rollover", Map.of(
                                "max_size", "50GB",
                                "max_age", "7d"
                            )
                        )
                    ),
                    "warm", Map.of(
                        "min_age", "7d",
                        "actions", Map.of(
                            "allocate", Map.of(
                                "number_of_replicas", 0
                            ),
                            "forcemerge", Map.of(
                                "max_num_segments", 1
                            )
                        )
                    ),
                    "cold", Map.of(
                        "min_age", "30d",
                        "actions", Map.of(
                            "allocate", Map.of(
                                "number_of_replicas", 0
                            )
                        )
                    ),
                    "delete", Map.of(
                        "min_age", "90d"
                    )
                )
            )
        );

        // 创建 ILM 策略
        // 实现省略...
    }

    /**
     * 分片重新分配
     */
    public void rebalanceShards() {
        try {
            elasticsearchTemplate.execute(client -> {
                ClusterRerouteRequest request = new ClusterRerouteRequest();

                // 移动分片
                request.add(new MoveAllocationCommand("index-name", 0, "node-1", "node-2"));

                // 取消分配
                request.add(new CancelAllocationCommand("index-name", 0, "node-1", true));

                return client.cluster().reroute(request, RequestOptions.DEFAULT);
            });
        } catch (Exception e) {
            log.error("分片重新分配失败", e);
        }
    }
}
```

#### 监控和告警
```java
@Component
public class ElasticsearchMonitor {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    /**
     * 集群健康监控
     */
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void monitorClusterHealth() {
        try {
            ClusterHealthResponse health = elasticsearchTemplate.execute(client -> {
                ClusterHealthRequest request = new ClusterHealthRequest();
                request.timeout(TimeValue.timeValueSeconds(10));
                return client.cluster().health(request, RequestOptions.DEFAULT);
            });

            // 记录集群状态
            String status = health.getStatus().name();
            int numberOfNodes = health.getNumberOfNodes();
            int numberOfDataNodes = health.getNumberOfDataNodes();
            int activePrimaryShards = health.getActivePrimaryShards();
            int activeShards = health.getActiveShards();
            int unassignedShards = health.getUnassignedShards();

            log.info("集群状态: status={}, nodes={}, dataNodes={}, primaryShards={}, activeShards={}, unassignedShards={}",
                status, numberOfNodes, numberOfDataNodes, activePrimaryShards, activeShards, unassignedShards);

            // 发送告警
            if (!"GREEN".equals(status)) {
                sendAlert("集群状态异常", "当前状态: " + status);
            }

            if (unassignedShards > 0) {
                sendAlert("存在未分配分片", "未分配分片数: " + unassignedShards);
            }

        } catch (Exception e) {
            log.error("集群健康检查失败", e);
            sendAlert("集群健康检查失败", e.getMessage());
        }
    }

    /**
     * 节点统计监控
     */
    @Scheduled(fixedRate = 300000) // 每5分钟检查一次
    public void monitorNodeStats() {
        try {
            NodesStatsResponse stats = elasticsearchTemplate.execute(client -> {
                NodesStatsRequest request = new NodesStatsRequest();
                request.all();
                return client.nodes().stats(request, RequestOptions.DEFAULT);
            });

            for (NodeStats nodeStats : stats.getNodes()) {
                String nodeName = nodeStats.getNode().getName();

                // JVM 统计
                JvmStats jvm = nodeStats.getJvm();
                long heapUsedPercent = jvm.getMem().getHeapUsedPercent();
                long gcCollectionCount = jvm.getGc().getCollectors()[0].getCollectionCount();

                // 文件系统统计
                FsInfo fs = nodeStats.getFs();
                long totalBytes = fs.getTotal().getTotal().getBytes();
                long freeBytes = fs.getTotal().getFree().getBytes();
                double diskUsagePercent = (double) (totalBytes - freeBytes) / totalBytes * 100;

                log.info("节点统计: node={}, heapUsed={}%, diskUsed={}%, gcCount={}",
                    nodeName, heapUsedPercent, diskUsagePercent, gcCollectionCount);

                // 告警检查
                if (heapUsedPercent > 85) {
                    sendAlert("节点内存使用率过高",
                        String.format("节点 %s 内存使用率: %d%%", nodeName, heapUsedPercent));
                }

                if (diskUsagePercent > 90) {
                    sendAlert("节点磁盘使用率过高",
                        String.format("节点 %s 磁盘使用率: %.2f%%", nodeName, diskUsagePercent));
                }
            }

        } catch (Exception e) {
            log.error("节点统计监控失败", e);
        }
    }

    /**
     * 索引统计监控
     */
    @Scheduled(fixedRate = 600000) // 每10分钟检查一次
    public void monitorIndexStats() {
        try {
            IndicesStatsResponse stats = elasticsearchTemplate.execute(client -> {
                IndicesStatsRequest request = new IndicesStatsRequest();
                return client.indices().stats(request, RequestOptions.DEFAULT);
            });

            for (Map.Entry<String, IndexStats> entry : stats.getIndices().entrySet()) {
                String indexName = entry.getKey();
                IndexStats indexStats = entry.getValue();

                // 索引大小
                long storeSizeBytes = indexStats.getTotal().getStore().getSizeInBytes();
                long docCount = indexStats.getTotal().getDocs().getCount();

                // 搜索统计
                long queryCount = indexStats.getTotal().getSearch().getTotal().getQueryCount();
                long queryTimeMs = indexStats.getTotal().getSearch().getTotal().getQueryTimeInMillis();

                log.info("索引统计: index={}, size={}MB, docs={}, queries={}, queryTime={}ms",
                    indexName, storeSizeBytes / 1024 / 1024, docCount, queryCount, queryTimeMs);

                // 性能告警
                if (queryCount > 0) {
                    double avgQueryTime = (double) queryTimeMs / queryCount;
                    if (avgQueryTime > 1000) { // 平均查询时间超过1秒
                        sendAlert("索引查询性能告警",
                            String.format("索引 %s 平均查询时间: %.2fms", indexName, avgQueryTime));
                    }
                }
            }

        } catch (Exception e) {
            log.error("索引统计监控失败", e);
        }
    }

    private void sendAlert(String title, String message) {
        // 发送告警逻辑
        log.warn("告警: {} - {}", title, message);
    }
}
```

**评分要点**:
- 集群架构设计 (40%)
- 分片策略理解 (30%)
- 运维监控能力 (30%)

---

## 6. MySQL 数据库

### 6.1 MySQL 索引和查询优化

**题目**: 详细说明 MySQL 索引的类型和原理，以及如何进行查询优化。

**参考答案**:

#### 索引类型对比
| 索引类型 | 存储结构 | 特点 | 适用场景 |
|----------|----------|------|----------|
| **B+Tree** | 平衡多路搜索树 | 范围查询效率高 | 大部分场景 |
| **Hash** | 哈希表 | 等值查询快 | 等值查询 |
| **Full-text** | 倒排索引 | 全文搜索 | 文本搜索 |
| **Spatial** | R-Tree | 空间数据 | 地理位置 |

#### B+Tree 索引原理
```sql
-- 1. 聚簇索引 (主键索引)
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,  -- 聚簇索引
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 二级索引
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
);

-- B+Tree 结构特点：
-- 1. 非叶子节点只存储键值，不存储数据
-- 2. 叶子节点存储完整的数据记录
-- 3. 叶子节点之间通过指针连接，支持范围查询
-- 4. 所有叶子节点在同一层，保证查询性能稳定
```

#### 复合索引和最左前缀原则
```sql
-- 创建复合索引
CREATE INDEX idx_user_status_time ON orders (user_id, status, created_at);

-- 索引使用情况分析
-- ✅ 能使用索引的查询
SELECT * FROM orders WHERE user_id = 1;                                    -- 使用索引
SELECT * FROM orders WHERE user_id = 1 AND status = 'paid';               -- 使用索引
SELECT * FROM orders WHERE user_id = 1 AND status = 'paid' AND created_at > '2024-01-01'; -- 使用索引
SELECT * FROM orders WHERE user_id = 1 AND created_at > '2024-01-01';     -- 部分使用索引

-- ❌ 不能使用索引的查询
SELECT * FROM orders WHERE status = 'paid';                               -- 不使用索引
SELECT * FROM orders WHERE created_at > '2024-01-01';                     -- 不使用索引
SELECT * FROM orders WHERE status = 'paid' AND created_at > '2024-01-01'; -- 不使用索引
```

#### 查询优化实践
```sql
-- 1. 索引覆盖优化
-- 原查询：需要回表查询
SELECT user_id, status, created_at FROM orders WHERE user_id = 1;

-- 优化：创建覆盖索引
CREATE INDEX idx_user_cover ON orders (user_id, status, created_at);

-- 2. 分页查询优化
-- 原查询：深分页性能差
SELECT * FROM orders ORDER BY id LIMIT 100000, 20;

-- 优化：使用子查询
SELECT o.* FROM orders o
INNER JOIN (
    SELECT id FROM orders ORDER BY id LIMIT 100000, 20
) t ON o.id = t.id;

-- 3. 范围查询优化
-- 原查询：使用函数导致索引失效
SELECT * FROM orders WHERE DATE(created_at) = '2024-01-01';

-- 优化：使用范围查询
SELECT * FROM orders
WHERE created_at >= '2024-01-01 00:00:00'
  AND created_at < '2024-01-02 00:00:00';

-- 4. IN 查询优化
-- 原查询：IN 子查询性能差
SELECT * FROM users WHERE id IN (
    SELECT user_id FROM orders WHERE status = 'paid'
);

-- 优化：使用 EXISTS
SELECT * FROM users u WHERE EXISTS (
    SELECT 1 FROM orders o WHERE o.user_id = u.id AND o.status = 'paid'
);
```

#### 执行计划分析
```sql
-- 使用 EXPLAIN 分析查询计划
EXPLAIN SELECT * FROM orders o
JOIN users u ON o.user_id = u.id
WHERE o.status = 'paid' AND u.created_at > '2024-01-01';

-- 关键字段解释：
-- id: 查询序列号
-- select_type: 查询类型 (SIMPLE, PRIMARY, SUBQUERY, DERIVED, UNION)
-- table: 表名
-- partitions: 匹配的分区
-- type: 连接类型 (system > const > eq_ref > ref > range > index > ALL)
-- possible_keys: 可能使用的索引
-- key: 实际使用的索引
-- key_len: 使用的索引长度
-- ref: 索引的哪一列被使用
-- rows: 估算的扫描行数
-- filtered: 按表条件过滤的行百分比
-- Extra: 额外信息

-- 优化目标：
-- 1. type 尽量不要是 ALL (全表扫描)
-- 2. rows 扫描行数尽量少
-- 3. Extra 避免出现 Using filesort, Using temporary
```

#### 索引设计最佳实践
```java
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_category_price", columnList = "category_id, price"),
    @Index(name = "idx_name_fulltext", columnList = "name"), // 全文索引
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_status_updated", columnList = "status, updated_at")
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ProductStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

// 索引设计原则：
// 1. 选择性高的列放在复合索引前面
// 2. 经常用于 WHERE 条件的列建索引
// 3. 经常用于 ORDER BY 的列建索引
// 4. 避免在小表上建过多索引
// 5. 定期分析和清理无用索引
```

**评分要点**:
- 索引原理理解 (40%)
- 查询优化能力 (35%)
- 实际调优经验 (25%)

---

### 6.2 MySQL 事务和锁机制

**题目**: 详细说明 MySQL 的事务隔离级别，锁机制，以及如何解决死锁问题。

**参考答案**:

#### 事务隔离级别
| 隔离级别 | 脏读 | 不可重复读 | 幻读 | 实现方式 |
|----------|------|------------|------|----------|
| **READ UNCOMMITTED** | ✓ | ✓ | ✓ | 无锁 |
| **READ COMMITTED** | ✗ | ✓ | ✓ | 读锁 |
| **REPEATABLE READ** | ✗ | ✗ | ✓ | MVCC + Gap Lock |
| **SERIALIZABLE** | ✗ | ✗ | ✗ | 读写锁 |

#### MVCC 多版本并发控制
```sql
-- InnoDB 通过 MVCC 实现非阻塞读
-- 每行记录包含隐藏字段：
-- DB_TRX_ID: 事务ID
-- DB_ROLL_PTR: 回滚指针
-- DB_ROW_ID: 行ID (如果没有主键)

-- 示例：演示 MVCC 工作原理
-- 会话1
START TRANSACTION;
SELECT * FROM users WHERE id = 1; -- 读取版本1
UPDATE users SET name = 'Alice' WHERE id = 1; -- 创建版本2
-- 未提交

-- 会话2 (同时进行)
START TRANSACTION;
SELECT * FROM users WHERE id = 1; -- 仍然读取版本1，不会被阻塞
COMMIT;

-- 会话1
COMMIT; -- 提交版本2
```

#### 锁机制详解
```sql
-- 1. 行级锁
-- 共享锁 (S锁)
SELECT * FROM users WHERE id = 1 LOCK IN SHARE MODE;

-- 排他锁 (X锁)
SELECT * FROM users WHERE id = 1 FOR UPDATE;

-- 2. 间隙锁 (Gap Lock)
-- 防止幻读，锁定索引记录之间的间隙
SELECT * FROM users WHERE id BETWEEN 10 AND 20 FOR UPDATE;

-- 3. 临键锁 (Next-Key Lock)
-- 行锁 + 间隙锁的组合
-- 在 REPEATABLE READ 隔离级别下默认使用

-- 4. 意向锁 (Intention Lock)
-- 表级锁，用于提高锁冲突检测效率
-- IS锁：意向共享锁
-- IX锁：意向排他锁
```

#### 死锁检测和解决
```java
@Service
@Transactional
public class DeadlockHandlingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * 死锁场景示例：转账操作
     */
    public void transfer(Long fromUserId, Long toUserId, BigDecimal amount) {
        // 为了避免死锁，总是按照固定顺序获取锁
        Long firstId = fromUserId.compareTo(toUserId) < 0 ? fromUserId : toUserId;
        Long secondId = fromUserId.compareTo(toUserId) < 0 ? toUserId : fromUserId;

        try {
            // 按顺序获取锁
            Account firstAccount = accountRepository.findByUserIdForUpdate(firstId);
            Account secondAccount = accountRepository.findByUserIdForUpdate(secondId);

            Account fromAccount = fromUserId.equals(firstId) ? firstAccount : secondAccount;
            Account toAccount = toUserId.equals(firstId) ? firstAccount : secondAccount;

            // 执行转账
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException("余额不足");
            }

            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

        } catch (CannotAcquireLockException e) {
            // 处理死锁异常
            log.warn("转账操作发生死锁，重试: fromUser={}, toUser={}", fromUserId, toUserId);
            throw new TransferRetryException("转账繁忙，请稍后重试");
        }
    }

    /**
     * 使用重试机制处理死锁
     */
    @Retryable(
        value = {DeadlockLoserDataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void transferWithRetry(Long fromUserId, Long toUserId, BigDecimal amount) {
        transfer(fromUserId, toUserId, amount);
    }

    /**
     * 批量操作避免死锁
     */
    public void batchUpdateUsers(List<UserUpdateRequest> requests) {
        // 按ID排序，避免死锁
        requests.sort(Comparator.comparing(UserUpdateRequest::getUserId));

        for (UserUpdateRequest request : requests) {
            User user = userRepository.findByIdForUpdate(request.getUserId());
            // 更新用户信息
            updateUser(user, request);
            userRepository.save(user);
        }
    }
}

// Repository 层实现
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a FROM Account a WHERE a.userId = :userId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Account findByUserIdForUpdate(@Param("userId") Long userId);

    @Query("SELECT a FROM Account a WHERE a.userId = :userId")
    @Lock(LockModeType.PESSIMISTIC_READ)
    Account findByUserIdForShare(@Param("userId") Long userId);
}
```

#### 锁等待和超时配置
```sql
-- 查看锁等待情况
SELECT
    r.trx_id waiting_trx_id,
    r.trx_mysql_thread_id waiting_thread,
    r.trx_query waiting_query,
    b.trx_id blocking_trx_id,
    b.trx_mysql_thread_id blocking_thread,
    b.trx_query blocking_query
FROM information_schema.innodb_lock_waits w
INNER JOIN information_schema.innodb_trx b ON b.trx_id = w.blocking_trx_id
INNER JOIN information_schema.innodb_trx r ON r.trx_id = w.requesting_trx_id;

-- 查看当前锁信息
SELECT * FROM information_schema.innodb_locks;

-- 查看死锁信息
SHOW ENGINE INNODB STATUS;

-- 配置锁等待超时
SET innodb_lock_wait_timeout = 50; -- 50秒超时

-- 配置死锁检测
SET innodb_deadlock_detect = ON;   -- 开启死锁检测
```

**评分要点**:
- 事务隔离级别理解 (35%)
- 锁机制掌握 (40%)
- 死锁解决方案 (25%)

---

### 6.3 MySQL 性能调优和高可用

**题目**: 描述 MySQL 性能调优的方法，以及主从复制和高可用架构设计。

**参考答案**:

#### 性能调优配置
```ini
# my.cnf 性能优化配置

[mysqld]
# 基础配置
port = 3306
socket = /var/lib/mysql/mysql.sock
datadir = /var/lib/mysql
pid-file = /var/run/mysqld/mysqld.pid

# 内存配置
innodb_buffer_pool_size = 8G          # 缓冲池大小，建议为内存的70-80%
innodb_buffer_pool_instances = 8      # 缓冲池实例数
innodb_log_buffer_size = 64M          # 日志缓冲区大小
key_buffer_size = 256M                # MyISAM 索引缓冲区
query_cache_size = 0                  # 禁用查询缓存 (MySQL 8.0已移除)
tmp_table_size = 256M                 # 临时表大小
max_heap_table_size = 256M            # 内存表大小

# 连接配置
max_connections = 1000                # 最大连接数
max_connect_errors = 100000           # 最大连接错误数
connect_timeout = 10                  # 连接超时时间
wait_timeout = 28800                  # 等待超时时间
interactive_timeout = 28800           # 交互超时时间

# InnoDB 配置
innodb_file_per_table = 1             # 每表一个文件
innodb_flush_log_at_trx_commit = 2    # 日志刷新策略 (1=最安全, 2=性能较好)
innodb_flush_method = O_DIRECT        # 刷新方法
innodb_io_capacity = 2000             # I/O 容量
innodb_io_capacity_max = 4000         # 最大 I/O 容量
innodb_read_io_threads = 8            # 读 I/O 线程数
innodb_write_io_threads = 8           # 写 I/O 线程数
innodb_thread_concurrency = 0         # 线程并发数 (0=自动)
innodb_lock_wait_timeout = 50         # 锁等待超时时间

# 日志配置
innodb_log_file_size = 1G             # 重做日志文件大小
innodb_log_files_in_group = 2         # 重做日志文件数量
slow_query_log = 1                    # 开启慢查询日志
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 1                   # 慢查询阈值 (秒)
log_queries_not_using_indexes = 1     # 记录未使用索引的查询

# 二进制日志配置
log_bin = mysql-bin                   # 开启二进制日志
binlog_format = ROW                   # 二进制日志格式
binlog_cache_size = 32M               # 二进制日志缓存
max_binlog_size = 1G                  # 二进制日志文件大小
expire_logs_days = 7                  # 日志保留天数
```

#### 主从复制配置
```ini
# 主库配置 (master.cnf)
[mysqld]
server-id = 1                         # 服务器ID (唯一)
log-bin = mysql-bin                   # 开启二进制日志
binlog-format = ROW                   # 行级复制
binlog-do-db = myapp                  # 复制指定数据库
binlog-ignore-db = mysql              # 忽略系统数据库
sync_binlog = 1                       # 同步二进制日志
innodb_flush_log_at_trx_commit = 1    # 事务日志刷新策略

# 从库配置 (slave.cnf)
[mysqld]
server-id = 2                         # 服务器ID (唯一)
relay-log = mysql-relay-bin           # 中继日志
relay-log-index = mysql-relay-bin.index
read-only = 1                         # 只读模式
super-read-only = 1                   # 超级只读模式
slave-skip-errors = 1062,1032         # 跳过特定错误
```

#### 主从复制搭建
```sql
-- 1. 主库创建复制用户
CREATE USER 'replication'@'%' IDENTIFIED BY 'replication_password';
GRANT REPLICATION SLAVE ON *.* TO 'replication'@'%';
FLUSH PRIVILEGES;

-- 2. 查看主库状态
SHOW MASTER STATUS;
-- 记录 File 和 Position

-- 3. 从库配置复制
CHANGE MASTER TO
    MASTER_HOST = '192.168.1.100',
    MASTER_PORT = 3306,
    MASTER_USER = 'replication',
    MASTER_PASSWORD = 'replication_password',
    MASTER_LOG_FILE = 'mysql-bin.000001',
    MASTER_LOG_POS = 154;

-- 4. 启动从库复制
START SLAVE;

-- 5. 检查复制状态
SHOW SLAVE STATUS\G
-- 关注 Slave_IO_Running 和 Slave_SQL_Running 都为 Yes
```

#### 高可用架构实现
```java
@Configuration
public class DatabaseConfig {

    /**
     * 主数据源配置
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .build();
    }

    /**
     * 从数据源配置
     */
    @Bean
    @ConfigurationProperties("spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .build();
    }

    /**
     * 动态数据源
     */
    @Bean
    public DataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DataSourceType.MASTER, masterDataSource());
        dataSourceMap.put(DataSourceType.SLAVE, slaveDataSource());

        dynamicDataSource.setTargetDataSources(dataSourceMap);
        dynamicDataSource.setDefaultTargetDataSource(masterDataSource());

        return dynamicDataSource;
    }

    /**
     * 事务管理器
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dynamicDataSource());
    }
}

// 动态数据源实现
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSourceType();
    }
}

// 数据源上下文
public class DataSourceContextHolder {

    private static final ThreadLocal<DataSourceType> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void setDataSourceType(DataSourceType dataSourceType) {
        CONTEXT_HOLDER.set(dataSourceType);
    }

    public static DataSourceType getDataSourceType() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearDataSourceType() {
        CONTEXT_HOLDER.remove();
    }
}

// 读写分离注解
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSource {
    DataSourceType value() default DataSourceType.MASTER;
}

// AOP 切面实现读写分离
@Aspect
@Component
public class DataSourceAspect {

    @Around("@annotation(dataSource)")
    public Object around(ProceedingJoinPoint point, DataSource dataSource) throws Throwable {
        DataSourceType dataSourceType = dataSource.value();
        DataSourceContextHolder.setDataSourceType(dataSourceType);

        try {
            return point.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    @Around("execution(* com.example.service.*Service.find*(..)) || " +
            "execution(* com.example.service.*Service.get*(..)) || " +
            "execution(* com.example.service.*Service.list*(..)) || " +
            "execution(* com.example.service.*Service.count*(..))")
    public Object readOperation(ProceedingJoinPoint point) throws Throwable {
        // 读操作使用从库
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            DataSourceContextHolder.setDataSourceType(DataSourceType.SLAVE);
        }

        try {
            return point.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }
}
```

#### 数据库监控和告警
```java
@Component
public class DatabaseMonitor {

    @Autowired
    private JdbcTemplate masterJdbcTemplate;

    @Autowired
    private JdbcTemplate slaveJdbcTemplate;

    /**
     * 监控主从复制延迟
     */
    @Scheduled(fixedRate = 30000) // 每30秒检查一次
    public void monitorReplicationLag() {
        try {
            // 查询主库位置
            Map<String, Object> masterStatus = masterJdbcTemplate.queryForMap(
                "SHOW MASTER STATUS");
            String masterLogFile = (String) masterStatus.get("File");
            Long masterLogPos = (Long) masterStatus.get("Position");

            // 查询从库状态
            Map<String, Object> slaveStatus = slaveJdbcTemplate.queryForMap(
                "SHOW SLAVE STATUS");
            String slaveLogFile = (String) slaveStatus.get("Master_Log_File");
            Long slaveLogPos = (Long) slaveStatus.get("Read_Master_Log_Pos");
            Long secondsBehindMaster = (Long) slaveStatus.get("Seconds_Behind_Master");

            // 检查复制状态
            String slaveIORunning = (String) slaveStatus.get("Slave_IO_Running");
            String slaveSQLRunning = (String) slaveStatus.get("Slave_SQL_Running");

            if (!"Yes".equals(slaveIORunning) || !"Yes".equals(slaveSQLRunning)) {
                sendAlert("主从复制中断",
                    String.format("IO线程: %s, SQL线程: %s", slaveIORunning, slaveSQLRunning));
            }

            if (secondsBehindMaster != null && secondsBehindMaster > 60) {
                sendAlert("主从复制延迟过大",
                    String.format("延迟时间: %d 秒", secondsBehindMaster));
            }

            log.info("主从复制状态: 主库位置={}:{}, 从库位置={}:{}, 延迟={}秒",
                masterLogFile, masterLogPos, slaveLogFile, slaveLogPos, secondsBehindMaster);

        } catch (Exception e) {
            log.error("监控主从复制状态失败", e);
            sendAlert("主从复制监控异常", e.getMessage());
        }
    }

    /**
     * 监控数据库连接池
     */
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void monitorConnectionPool() {
        try {
            HikariDataSource masterDs = (HikariDataSource) masterDataSource;
            HikariPoolMXBean poolMXBean = masterDs.getHikariPoolMXBean();

            int activeConnections = poolMXBean.getActiveConnections();
            int idleConnections = poolMXBean.getIdleConnections();
            int totalConnections = poolMXBean.getTotalConnections();
            int threadsAwaitingConnection = poolMXBean.getThreadsAwaitingConnection();

            log.info("连接池状态: 活跃连接={}, 空闲连接={}, 总连接={}, 等待连接线程={}",
                activeConnections, idleConnections, totalConnections, threadsAwaitingConnection);

            // 连接池告警
            if (activeConnections > totalConnections * 0.8) {
                sendAlert("数据库连接池使用率过高",
                    String.format("活跃连接: %d/%d", activeConnections, totalConnections));
            }

            if (threadsAwaitingConnection > 0) {
                sendAlert("数据库连接池等待",
                    String.format("等待连接线程数: %d", threadsAwaitingConnection));
            }

        } catch (Exception e) {
            log.error("监控连接池状态失败", e);
        }
    }

    private void sendAlert(String title, String message) {
        // 发送告警逻辑
        log.warn("数据库告警: {} - {}", title, message);
    }
}
```

**评分要点**:
- 性能调优策略 (40%)
- 主从复制配置 (35%)
- 高可用架构设计 (25%)

---

## 7. Spring Boot 框架

### 7.1 Spring Boot 自动配置原理

**题目**: 解释 Spring Boot 自动配置的实现原理，以及如何自定义 Starter。

**参考答案**:

#### 自动配置原理
```java
@SpringBootApplication
public class Application {
    // @SpringBootApplication 包含：
    // @SpringBootConfiguration
    // @EnableAutoConfiguration  // 启用自动配置
    // @ComponentScan            // 组件扫描
}

// 自动配置核心注解
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {
    // 排除特定的自动配置类
    Class<?>[] exclude() default {};
    String[] excludeName() default {};
}
```

#### 自动配置加载流程
```java
// 1. AutoConfigurationImportSelector 核心逻辑
public class AutoConfigurationImportSelector implements DeferredImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        if (!isEnabled(annotationMetadata)) {
            return NO_IMPORTS;
        }

        // 获取自动配置条目
        AutoConfigurationEntry autoConfigurationEntry =
            getAutoConfigurationEntry(annotationMetadata);
        return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
    }

    protected AutoConfigurationEntry getAutoConfigurationEntry(AnnotationMetadata annotationMetadata) {
        if (!isEnabled(annotationMetadata)) {
            return EMPTY_ENTRY;
        }

        AnnotationAttributes attributes = getAttributes(annotationMetadata);

        // 1. 加载候选配置类
        List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);

        // 2. 去重
        configurations = removeDuplicates(configurations);

        // 3. 排除指定的配置类
        Set<String> exclusions = getExclusions(annotationMetadata, attributes);
        checkExcludedClasses(configurations, exclusions);
        configurations.removeAll(exclusions);

        // 4. 过滤不满足条件的配置类
        configurations = getConfigurationClassFilter().filter(configurations);

        // 5. 触发自动配置导入事件
        fireAutoConfigurationImportEvents(configurations, exclusions);

        return new AutoConfigurationEntry(configurations, exclusions);
    }

    protected List<String> getCandidateConfigurations(AnnotationMetadata metadata,
                                                      AnnotationAttributes attributes) {
        // 从 META-INF/spring.factories 加载配置类
        List<String> configurations = SpringFactoriesLoader.loadFactoryNames(
            getSpringFactoriesLoaderFactoryClass(), getBeanClassLoader());

        Assert.notEmpty(configurations,
            "No auto configuration classes found in META-INF/spring.factories");
        return configurations;
    }
}
```

#### 条件注解机制
```java
// 常用条件注解
@Configuration
@ConditionalOnClass(DataSource.class)                    // 类路径存在指定类
@ConditionalOnMissingBean(DataSource.class)             // 容器中不存在指定Bean
@ConditionalOnProperty(prefix = "spring.datasource",     // 配置属性满足条件
                      name = "url")
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}

// 自定义条件注解
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnRedisCondition.class)
public @interface ConditionalOnRedis {
    String[] value() default {};
}

public class OnRedisCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();

        // 检查 Redis 相关配置
        String host = environment.getProperty("spring.redis.host");
        String port = environment.getProperty("spring.redis.port");

        return StringUtils.hasText(host) && StringUtils.hasText(port);
    }
}
```

#### 自定义 Starter 开发
```java
// 1. 配置属性类
@ConfigurationProperties(prefix = "myapp.sms")
@Data
public class SmsProperties {

    /**
     * 是否启用短信服务
     */
    private boolean enabled = true;

    /**
     * 短信服务提供商
     */
    private Provider provider = Provider.ALIYUN;

    /**
     * API 密钥
     */
    private String accessKey;

    /**
     * API 密钥
     */
    private String secretKey;

    /**
     * 签名
     */
    private String signature;

    /**
     * 模板配置
     */
    private Map<String, String> templates = new HashMap<>();

    public enum Provider {
        ALIYUN, TENCENT, HUAWEI
    }
}

// 2. 服务接口定义
public interface SmsService {

    /**
     * 发送短信
     */
    boolean sendSms(String phone, String templateCode, Map<String, Object> params);

    /**
     * 批量发送短信
     */
    boolean batchSendSms(List<String> phones, String templateCode, Map<String, Object> params);
}

// 3. 服务实现类
public class AliyunSmsService implements SmsService {

    private final SmsProperties properties;
    private final IAcsClient client;

    public AliyunSmsService(SmsProperties properties) {
        this.properties = properties;
        this.client = createClient();
    }

    @Override
    public boolean sendSms(String phone, String templateCode, Map<String, Object> params) {
        try {
            SendSmsRequest request = new SendSmsRequest();
            request.setPhoneNumbers(phone);
            request.setSignName(properties.getSignature());
            request.setTemplateCode(templateCode);
            request.setTemplateParam(JSON.toJSONString(params));

            SendSmsResponse response = client.getAcsResponse(request);
            return "OK".equals(response.getCode());

        } catch (Exception e) {
            log.error("发送短信失败: phone={}, template={}", phone, templateCode, e);
            return false;
        }
    }

    @Override
    public boolean batchSendSms(List<String> phones, String templateCode, Map<String, Object> params) {
        // 批量发送实现
        return phones.stream()
            .allMatch(phone -> sendSms(phone, templateCode, params));
    }

    private IAcsClient createClient() {
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou",
            properties.getAccessKey(), properties.getSecretKey());
        return new DefaultAcsClient(profile);
    }
}

// 4. 自动配置类
@Configuration
@EnableConfigurationProperties(SmsProperties.class)
@ConditionalOnProperty(prefix = "myapp.sms", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SmsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "myapp.sms", name = "provider", havingValue = "aliyun")
    public SmsService aliyunSmsService(SmsProperties properties) {
        return new AliyunSmsService(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "myapp.sms", name = "provider", havingValue = "tencent")
    public SmsService tencentSmsService(SmsProperties properties) {
        return new TencentSmsService(properties);
    }

    @Bean
    @ConditionalOnBean(SmsService.class)
    public SmsTemplate smsTemplate(SmsService smsService, SmsProperties properties) {
        return new SmsTemplate(smsService, properties);
    }
}

// 5. 便捷操作模板
public class SmsTemplate {

    private final SmsService smsService;
    private final SmsProperties properties;

    public SmsTemplate(SmsService smsService, SmsProperties properties) {
        this.smsService = smsService;
        this.properties = properties;
    }

    /**
     * 发送验证码
     */
    public boolean sendVerificationCode(String phone, String code) {
        String templateCode = properties.getTemplates().get("verification");
        return smsService.sendSms(phone, templateCode, Map.of("code", code));
    }

    /**
     * 发送通知
     */
    public boolean sendNotification(String phone, String message) {
        String templateCode = properties.getTemplates().get("notification");
        return smsService.sendSms(phone, templateCode, Map.of("message", message));
    }
}
```

#### Starter 项目结构
```
myapp-sms-spring-boot-starter/
├── pom.xml
└── src/main/
    ├── java/com/myapp/sms/
    │   ├── SmsService.java
    │   ├── SmsTemplate.java
    │   ├── SmsProperties.java
    │   ├── SmsAutoConfiguration.java
    │   └── impl/
    │       ├── AliyunSmsService.java
    │       └── TencentSmsService.java
    └── resources/
        └── META-INF/
            ├── spring.factories
            └── spring-configuration-metadata.json
```

```properties
# META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.myapp.sms.SmsAutoConfiguration
```

```json
// META-INF/spring-configuration-metadata.json
{
  "groups": [
    {
      "name": "myapp.sms",
      "type": "com.myapp.sms.SmsProperties",
      "sourceType": "com.myapp.sms.SmsProperties"
    }
  ],
  "properties": [
    {
      "name": "myapp.sms.enabled",
      "type": "java.lang.Boolean",
      "defaultValue": true,
      "description": "是否启用短信服务"
    },
    {
      "name": "myapp.sms.provider",
      "type": "com.myapp.sms.SmsProperties$Provider",
      "defaultValue": "aliyun",
      "description": "短信服务提供商"
    }
  ]
}
```

**评分要点**:
- 自动配置原理理解 (40%)
- Starter 开发能力 (35%)
- 源码分析深度 (25%)

---

### 7.2 Spring Boot 核心特性和最佳实践

**题目**: 说明 Spring Boot 的核心特性，包括配置管理、监控、测试等最佳实践。

**参考答案**:

#### 配置管理最佳实践
```java
// 1. 多环境配置
// application.yml
spring:
  profiles:
    active: @spring.profiles.active@  # Maven 占位符

---
spring:
  profiles: dev
  datasource:
    url: jdbc:mysql://localhost:3306/myapp_dev
    username: dev_user
    password: dev_password

---
spring:
  profiles: prod
  datasource:
    url: jdbc:mysql://prod-db:3306/myapp
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

// 2. 配置属性验证
@ConfigurationProperties(prefix = "myapp.api")
@Validated
@Data
public class ApiProperties {

    @NotBlank(message = "API URL 不能为空")
    @URL(message = "API URL 格式不正确")
    private String url;

    @Min(value = 1000, message = "超时时间不能小于1000ms")
    @Max(value = 60000, message = "超时时间不能大于60000ms")
    private int timeout = 5000;

    @NotNull(message = "重试配置不能为空")
    @Valid
    private RetryConfig retry = new RetryConfig();

    @Data
    public static class RetryConfig {

        @Min(value = 0, message = "重试次数不能小于0")
        @Max(value = 10, message = "重试次数不能大于10")
        private int maxAttempts = 3;

        @Min(value = 100, message = "重试间隔不能小于100ms")
        private long delay = 1000;
    }
}

// 3. 配置加密
@Component
public class EncryptablePropertyResolver implements PropertyResolver {

    private final AESUtil aesUtil;

    @Override
    public String getProperty(String key) {
        String value = environment.getProperty(key);
        if (value != null && value.startsWith("ENC(") && value.endsWith(")")) {
            String encryptedValue = value.substring(4, value.length() - 1);
            return aesUtil.decrypt(encryptedValue);
        }
        return value;
    }
}
```

#### 监控和健康检查
```java
// 1. 自定义健康检查
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "Available")
                    .withDetail("validationQuery", "SELECT 1")
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "Connection invalid")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("database", "Connection failed")
                .withException(e)
                .build();
        }
    }
}

// 2. 自定义指标
@Component
public class BusinessMetrics {

    private final Counter orderCounter;
    private final Timer orderProcessingTimer;
    private final Gauge activeUsersGauge;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.orderCounter = Counter.builder("orders.created")
            .description("订单创建数量")
            .register(meterRegistry);

        this.orderProcessingTimer = Timer.builder("orders.processing.time")
            .description("订单处理时间")
            .register(meterRegistry);

        this.activeUsersGauge = Gauge.builder("users.active")
            .description("活跃用户数")
            .register(meterRegistry, this, BusinessMetrics::getActiveUserCount);
    }

    public void recordOrderCreated() {
        orderCounter.increment();
    }

    public void recordOrderProcessingTime(Duration duration) {
        orderProcessingTimer.record(duration);
    }

    private double getActiveUserCount() {
        // 获取活跃用户数的逻辑
        return 0.0;
    }
}

// 3. 应用信息端点
@Component
public class AppInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("app", Map.of(
            "name", "MyApp",
            "version", "1.0.0",
            "description", "我的应用程序"
        ));

        builder.withDetail("team", Map.of(
            "name", "开发团队",
            "email", "dev-team@example.com"
        ));

        // 运行时信息
        Runtime runtime = Runtime.getRuntime();
        builder.withDetail("runtime", Map.of(
            "processors", runtime.availableProcessors(),
            "freeMemory", runtime.freeMemory(),
            "totalMemory", runtime.totalMemory(),
            "maxMemory", runtime.maxMemory()
        ));
    }
}
```

#### 测试最佳实践
```java
// 1. 集成测试
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
@Rollback
class UserServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Test
    void shouldCreateUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest("test@example.com", "Test User");

        // When
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
            "/api/users", request, UserResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getEmail()).isEqualTo("test@example.com");
    }
}

// 2. 切片测试
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void shouldReturnUser() throws Exception {
        // Given
        User user = new User(1L, "test@example.com", "Test User");
        when(userService.findById(1L)).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.name").value("Test User"));
    }
}

// 3. 数据层测试
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindByEmail() {
        // Given
        User user = new User("test@example.com", "Test User");
        entityManager.persistAndFlush(user);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test User");
    }
}

// 4. 测试配置
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
    }

    @Bean
    @Primary
    public EmailService mockEmailService() {
        return Mockito.mock(EmailService.class);
    }
}
```

**评分要点**:
- 配置管理能力 (35%)
- 监控和指标理解 (30%)
- 测试最佳实践 (35%)

---

## 8. 分布式系统

### 8.1 分布式事务解决方案

**题目**: 比较不同分布式事务解决方案的优缺点，并说明在什么场景下使用。

**参考答案**:

#### 分布式事务方案对比
| 方案 | 一致性 | 可用性 | 性能 | 复杂度 | 适用场景 |
|------|--------|--------|------|--------|----------|
| **2PC** | 强一致 | 低 | 低 | 中 | 强一致性要求 |
| **TCC** | 最终一致 | 高 | 中 | 高 | 业务补偿 |
| **Saga** | 最终一致 | 高 | 高 | 中 | 长流程事务 |
| **消息事务** | 最终一致 | 高 | 高 | 低 | 异步处理 |

#### TCC 模式实现
```java
// 1. TCC 接口定义
public interface PaymentTccService {

    /**
     * Try 阶段：尝试执行业务
     */
    @TwoPhaseBusinessAction(name = "paymentTcc", commitMethod = "confirm", rollbackMethod = "cancel")
    boolean prepare(@BusinessActionContextParameter(paramName = "orderId") String orderId,
                   @BusinessActionContextParameter(paramName = "amount") BigDecimal amount);

    /**
     * Confirm 阶段：确认执行业务
     */
    boolean confirm(BusinessActionContext context);

    /**
     * Cancel 阶段：取消执行业务
     */
    boolean cancel(BusinessActionContext context);
}

// 2. TCC 实现类
@Service
public class PaymentTccServiceImpl implements PaymentTccService {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AccountService accountService;

    @Override
    @Transactional
    public boolean prepare(String orderId, BigDecimal amount) {
        try {
            // 1. 检查账户余额
            Account account = accountService.getAccountByOrderId(orderId);
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException("余额不足");
            }

            // 2. 冻结资金
            accountService.freeze(account.getId(), amount);

            // 3. 创建支付记录（预处理状态）
            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setAmount(amount);
            payment.setStatus(PaymentStatus.PREPARING);
            paymentService.save(payment);

            log.info("支付 Try 阶段成功: orderId={}, amount={}", orderId, amount);
            return true;

        } catch (Exception e) {
            log.error("支付 Try 阶段失败: orderId={}, amount={}", orderId, amount, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean confirm(BusinessActionContext context) {
        String orderId = context.getActionContext("orderId").toString();
        BigDecimal amount = new BigDecimal(context.getActionContext("amount").toString());

        try {
            // 1. 扣减冻结资金
            Account account = accountService.getAccountByOrderId(orderId);
            accountService.deductFrozen(account.getId(), amount);

            // 2. 更新支付状态
            Payment payment = paymentService.getByOrderId(orderId);
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentService.update(payment);

            log.info("支付 Confirm 阶段成功: orderId={}, amount={}", orderId, amount);
            return true;

        } catch (Exception e) {
            log.error("支付 Confirm 阶段失败: orderId={}, amount={}", orderId, amount, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean cancel(BusinessActionContext context) {
        String orderId = context.getActionContext("orderId").toString();
        BigDecimal amount = new BigDecimal(context.getActionContext("amount").toString());

        try {
            // 1. 释放冻结资金
            Account account = accountService.getAccountByOrderId(orderId);
            accountService.unfreeze(account.getId(), amount);

            // 2. 更新支付状态
            Payment payment = paymentService.getByOrderId(orderId);
            if (payment != null) {
                payment.setStatus(PaymentStatus.CANCELLED);
                paymentService.update(payment);
            }

            log.info("支付 Cancel 阶段成功: orderId={}, amount={}", orderId, amount);
            return true;

        } catch (Exception e) {
            log.error("支付 Cancel 阶段失败: orderId={}, amount={}", orderId, amount, e);
            return false;
        }
    }
}
```

#### Saga 模式实现
```java
// 1. Saga 编排器
@Component
public class OrderSagaOrchestrator {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    /**
     * 订单处理 Saga
     */
    public void processOrder(OrderCreateRequest request) {
        SagaTransaction saga = SagaTransaction.builder()
            .sagaId(UUID.randomUUID().toString())
            .build();

        try {
            // 步骤1：创建订单
            saga.addStep("createOrder",
                () -> orderService.createOrder(request),
                () -> orderService.cancelOrder(request.getOrderId()));

            // 步骤2：扣减库存
            saga.addStep("deductInventory",
                () -> inventoryService.deduct(request.getProductId(), request.getQuantity()),
                () -> inventoryService.restore(request.getProductId(), request.getQuantity()));

            // 步骤3：处理支付
            saga.addStep("processPayment",
                () -> paymentService.pay(request.getOrderId(), request.getAmount()),
                () -> paymentService.refund(request.getOrderId(), request.getAmount()));

            // 执行 Saga
            saga.execute();

        } catch (SagaExecutionException e) {
            log.error("订单处理 Saga 执行失败: {}", request.getOrderId(), e);
            // Saga 会自动执行补偿操作
        }
    }
}

// 2. Saga 事务管理器
public class SagaTransaction {

    private String sagaId;
    private List<SagaStep> steps = new ArrayList<>();
    private int currentStep = 0;

    public static SagaTransactionBuilder builder() {
        return new SagaTransactionBuilder();
    }

    public void addStep(String stepName, Runnable action, Runnable compensation) {
        steps.add(new SagaStep(stepName, action, compensation));
    }

    public void execute() throws SagaExecutionException {
        try {
            // 正向执行所有步骤
            for (currentStep = 0; currentStep < steps.size(); currentStep++) {
                SagaStep step = steps.get(currentStep);
                log.info("执行 Saga 步骤: sagaId={}, step={}", sagaId, step.getName());
                step.getAction().run();
            }

            log.info("Saga 执行成功: sagaId={}", sagaId);

        } catch (Exception e) {
            log.error("Saga 执行失败，开始补偿: sagaId={}, failedStep={}", sagaId, currentStep, e);
            compensate();
            throw new SagaExecutionException("Saga 执行失败", e);
        }
    }

    private void compensate() {
        // 反向执行补偿操作
        for (int i = currentStep - 1; i >= 0; i--) {
            try {
                SagaStep step = steps.get(i);
                log.info("执行 Saga 补偿: sagaId={}, step={}", sagaId, step.getName());
                step.getCompensation().run();
            } catch (Exception e) {
                log.error("Saga 补偿失败: sagaId={}, step={}", sagaId, i, e);
                // 补偿失败需要人工介入
            }
        }
    }

    @Data
    @AllArgsConstructor
    private static class SagaStep {
        private String name;
        private Runnable action;
        private Runnable compensation;
    }
}
```

#### 消息事务实现
```java
// 1. 本地消息表方案
@Service
@Transactional
public class OrderMessageService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MessageLogService messageLogService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 创建订单并发送消息
     */
    public void createOrderWithMessage(OrderCreateRequest request) {
        // 1. 在同一事务中创建订单和消息记录
        Order order = orderService.createOrder(request);

        MessageLog messageLog = new MessageLog();
        messageLog.setMessageId(UUID.randomUUID().toString());
        messageLog.setTopic("order-created");
        messageLog.setContent(JSON.toJSONString(order));
        messageLog.setStatus(MessageStatus.PENDING);
        messageLogService.save(messageLog);

        // 2. 事务提交后异步发送消息
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendMessage(messageLog);
                }
            });
    }

    private void sendMessage(MessageLog messageLog) {
        try {
            SendResult result = rocketMQTemplate.syncSend(
                messageLog.getTopic(), messageLog.getContent());

            if (result.getSendStatus() == SendStatus.SEND_OK) {
                messageLogService.updateStatus(messageLog.getId(), MessageStatus.SENT);
            } else {
                messageLogService.updateStatus(messageLog.getId(), MessageStatus.FAILED);
            }

        } catch (Exception e) {
            log.error("发送消息失败: messageId={}", messageLog.getMessageId(), e);
            messageLogService.updateStatus(messageLog.getId(), MessageStatus.FAILED);
        }
    }

    /**
     * 定时重发失败消息
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void retryFailedMessages() {
        List<MessageLog> failedMessages = messageLogService.findFailedMessages();

        for (MessageLog messageLog : failedMessages) {
            if (messageLog.getRetryCount() < 3) {
                sendMessage(messageLog);
                messageLogService.incrementRetryCount(messageLog.getId());
            }
        }
    }
}

// 2. 事务消息方案 (RocketMQ)
@Component
@RocketMQTransactionListener
public class OrderTransactionListener implements RocketMQLocalTransactionListener {

    @Autowired
    private OrderService orderService;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            OrderCreateRequest request = (OrderCreateRequest) arg;

            // 执行本地事务
            orderService.createOrder(request);

            return RocketMQLocalTransactionState.COMMIT;

        } catch (Exception e) {
            log.error("本地事务执行失败", e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        try {
            String orderId = new String(msg.getBody());

            // 检查订单是否存在
            Order order = orderService.getById(orderId);
            if (order != null) {
                return RocketMQLocalTransactionState.COMMIT;
            } else {
                return RocketMQLocalTransactionState.ROLLBACK;
            }

        } catch (Exception e) {
            log.error("检查本地事务状态失败", e);
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }
}
```

**评分要点**:
- 方案理解深度 (40%)
- 场景选择能力 (35%)
- 实现经验 (25%)

---

### 8.2 分布式锁和一致性算法

**题目**: 详细说明分布式锁的实现方案，以及 Raft、Paxos 等一致性算法的原理。

**参考答案**:

#### 分布式锁实现方案
```java
// 1. Redis 分布式锁
@Component
public class RedisDistributedLock {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String LOCK_PREFIX = "distributed_lock:";
    private static final String UNLOCK_SCRIPT =
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "return redis.call('del', KEYS[1]) " +
        "else return 0 end";

    /**
     * 获取锁
     */
    public boolean tryLock(String lockKey, String requestId, long expireTime) {
        String key = LOCK_PREFIX + lockKey;

        Boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            return connection.set(key.getBytes(), requestId.getBytes(),
                Expiration.milliseconds(expireTime), RedisStringCommands.SetOption.SET_IF_ABSENT);
        });

        return Boolean.TRUE.equals(result);
    }

    /**
     * 释放锁
     */
    public boolean releaseLock(String lockKey, String requestId) {
        String key = LOCK_PREFIX + lockKey;

        Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
            return connection.eval(UNLOCK_SCRIPT.getBytes(), ReturnType.INTEGER, 1,
                key.getBytes(), requestId.getBytes());
        });

        return Long.valueOf(1).equals(result);
    }

    /**
     * 自动续期锁
     */
    public void renewLock(String lockKey, String requestId, long expireTime) {
        String key = LOCK_PREFIX + lockKey;

        String script =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "return redis.call('expire', KEYS[1], ARGV[2]) " +
            "else return 0 end";

        redisTemplate.execute((RedisCallback<Long>) connection -> {
            return connection.eval(script.getBytes(), ReturnType.INTEGER, 1,
                key.getBytes(), requestId.getBytes(), String.valueOf(expireTime / 1000).getBytes());
        });
    }
}

// 2. Redisson 分布式锁
@Component
public class RedissonLockService {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 可重入锁
     */
    public void executeWithLock(String lockKey, Runnable task) {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                task.run();
            } else {
                throw new LockAcquisitionException("获取锁失败: " + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionException("获取锁被中断: " + lockKey);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 读写锁
     */
    public void executeWithReadLock(String lockKey, Runnable task) {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(lockKey);
        RLock readLock = readWriteLock.readLock();

        try {
            if (readLock.tryLock(10, 30, TimeUnit.SECONDS)) {
                task.run();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (readLock.isHeldByCurrentThread()) {
                readLock.unlock();
            }
        }
    }

    /**
     * 公平锁
     */
    public void executeWithFairLock(String lockKey, Runnable task) {
        RLock fairLock = redissonClient.getFairLock(lockKey);

        try {
            if (fairLock.tryLock(10, 30, TimeUnit.SECONDS)) {
                task.run();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (fairLock.isHeldByCurrentThread()) {
                fairLock.unlock();
            }
        }
    }
}
```

#### Raft 一致性算法原理
```java
// Raft 算法核心概念实现示例
public class RaftNode {

    // 节点状态
    private volatile NodeState state = NodeState.FOLLOWER;

    // 持久化状态
    private volatile long currentTerm = 0;
    private volatile String votedFor = null;
    private final List<LogEntry> log = new ArrayList<>();

    // 易失状态
    private volatile long commitIndex = 0;
    private volatile long lastApplied = 0;

    // Leader 状态
    private final Map<String, Long> nextIndex = new ConcurrentHashMap<>();
    private final Map<String, Long> matchIndex = new ConcurrentHashMap<>();

    /**
     * 选举超时处理
     */
    @Scheduled(fixedDelay = 150) // 150-300ms 随机超时
    public void electionTimeout() {
        if (state == NodeState.FOLLOWER || state == NodeState.CANDIDATE) {
            long lastHeartbeat = getLastHeartbeatTime();
            long timeout = getElectionTimeout();

            if (System.currentTimeMillis() - lastHeartbeat > timeout) {
                startElection();
            }
        }
    }

    /**
     * 开始选举
     */
    private void startElection() {
        state = NodeState.CANDIDATE;
        currentTerm++;
        votedFor = getNodeId();

        log.info("开始选举: term={}, nodeId={}", currentTerm, getNodeId());

        // 向所有其他节点发送投票请求
        List<String> otherNodes = getOtherNodes();
        AtomicInteger voteCount = new AtomicInteger(1); // 自己投自己一票

        for (String nodeId : otherNodes) {
            CompletableFuture.runAsync(() -> {
                VoteRequest request = new VoteRequest(
                    currentTerm, getNodeId(), getLastLogIndex(), getLastLogTerm());

                VoteResponse response = sendVoteRequest(nodeId, request);

                if (response.isVoteGranted()) {
                    int votes = voteCount.incrementAndGet();

                    // 获得多数票，成为 Leader
                    if (votes > otherNodes.size() / 2 && state == NodeState.CANDIDATE) {
                        becomeLeader();
                    }
                } else if (response.getTerm() > currentTerm) {
                    // 发现更高的 term，转为 Follower
                    currentTerm = response.getTerm();
                    state = NodeState.FOLLOWER;
                    votedFor = null;
                }
            });
        }
    }

    /**
     * 成为 Leader
     */
    private void becomeLeader() {
        state = NodeState.LEADER;
        log.info("成为 Leader: term={}, nodeId={}", currentTerm, getNodeId());

        // 初始化 Leader 状态
        List<String> otherNodes = getOtherNodes();
        for (String nodeId : otherNodes) {
            nextIndex.put(nodeId, log.size());
            matchIndex.put(nodeId, 0L);
        }

        // 立即发送心跳
        sendHeartbeat();
    }

    /**
     * 发送心跳
     */
    @Scheduled(fixedRate = 50) // 50ms 心跳间隔
    public void sendHeartbeat() {
        if (state == NodeState.LEADER) {
            List<String> otherNodes = getOtherNodes();

            for (String nodeId : otherNodes) {
                CompletableFuture.runAsync(() -> {
                    long nextIdx = nextIndex.get(nodeId);

                    AppendEntriesRequest request = new AppendEntriesRequest(
                        currentTerm, getNodeId(), nextIdx - 1,
                        getLogTerm(nextIdx - 1), getLogEntries(nextIdx), commitIndex);

                    AppendEntriesResponse response = sendAppendEntries(nodeId, request);

                    if (response.isSuccess()) {
                        // 更新 nextIndex 和 matchIndex
                        nextIndex.put(nodeId, nextIdx + request.getEntries().size());
                        matchIndex.put(nodeId, nextIdx + request.getEntries().size() - 1);

                        // 更新 commitIndex
                        updateCommitIndex();
                    } else {
                        if (response.getTerm() > currentTerm) {
                            // 发现更高的 term，转为 Follower
                            currentTerm = response.getTerm();
                            state = NodeState.FOLLOWER;
                            votedFor = null;
                        } else {
                            // 日志不一致，回退 nextIndex
                            nextIndex.put(nodeId, Math.max(1, nextIndex.get(nodeId) - 1));
                        }
                    }
                });
            }
        }
    }

    /**
     * 处理投票请求
     */
    public VoteResponse handleVoteRequest(VoteRequest request) {
        if (request.getTerm() < currentTerm) {
            return new VoteResponse(currentTerm, false);
        }

        if (request.getTerm() > currentTerm) {
            currentTerm = request.getTerm();
            state = NodeState.FOLLOWER;
            votedFor = null;
        }

        boolean voteGranted = false;
        if ((votedFor == null || votedFor.equals(request.getCandidateId())) &&
            isLogUpToDate(request.getLastLogIndex(), request.getLastLogTerm())) {
            votedFor = request.getCandidateId();
            voteGranted = true;
            resetElectionTimeout();
        }

        return new VoteResponse(currentTerm, voteGranted);
    }

    /**
     * 处理日志追加请求
     */
    public AppendEntriesResponse handleAppendEntries(AppendEntriesRequest request) {
        if (request.getTerm() < currentTerm) {
            return new AppendEntriesResponse(currentTerm, false);
        }

        if (request.getTerm() > currentTerm) {
            currentTerm = request.getTerm();
            votedFor = null;
        }

        state = NodeState.FOLLOWER;
        resetElectionTimeout();

        // 检查日志一致性
        if (request.getPrevLogIndex() > 0 &&
            (log.size() <= request.getPrevLogIndex() ||
             log.get((int) request.getPrevLogIndex() - 1).getTerm() != request.getPrevLogTerm())) {
            return new AppendEntriesResponse(currentTerm, false);
        }

        // 追加新日志条目
        if (!request.getEntries().isEmpty()) {
            // 删除冲突的日志条目
            if (log.size() > request.getPrevLogIndex()) {
                log.subList((int) request.getPrevLogIndex(), log.size()).clear();
            }

            // 追加新条目
            log.addAll(request.getEntries());
        }

        // 更新 commitIndex
        if (request.getLeaderCommit() > commitIndex) {
            commitIndex = Math.min(request.getLeaderCommit(), log.size());
            applyLogEntries();
        }

        return new AppendEntriesResponse(currentTerm, true);
    }

    private void updateCommitIndex() {
        // 计算可以提交的最大索引
        List<Long> matchIndices = new ArrayList<>(matchIndex.values());
        matchIndices.add((long) log.size()); // 加上自己的索引
        matchIndices.sort(Collections.reverseOrder());

        long majorityIndex = matchIndices.get(matchIndices.size() / 2);

        if (majorityIndex > commitIndex &&
            log.get((int) majorityIndex - 1).getTerm() == currentTerm) {
            commitIndex = majorityIndex;
            applyLogEntries();
        }
    }

    private void applyLogEntries() {
        while (lastApplied < commitIndex) {
            lastApplied++;
            LogEntry entry = log.get((int) lastApplied - 1);
            applyToStateMachine(entry);
        }
    }

    // 其他辅助方法...
    private String getNodeId() { return "node-1"; }
    private List<String> getOtherNodes() { return Arrays.asList("node-2", "node-3"); }
    private long getLastHeartbeatTime() { return System.currentTimeMillis(); }
    private long getElectionTimeout() { return 150 + new Random().nextInt(150); }
    private long getLastLogIndex() { return log.size(); }
    private long getLastLogTerm() { return log.isEmpty() ? 0 : log.get(log.size() - 1).getTerm(); }
    private long getLogTerm(long index) { return index <= 0 ? 0 : log.get((int) index - 1).getTerm(); }
    private List<LogEntry> getLogEntries(long fromIndex) { return log.subList((int) fromIndex, log.size()); }
    private boolean isLogUpToDate(long lastLogIndex, long lastLogTerm) { return true; }
    private void resetElectionTimeout() {}
    private VoteResponse sendVoteRequest(String nodeId, VoteRequest request) { return new VoteResponse(0, false); }
    private AppendEntriesResponse sendAppendEntries(String nodeId, AppendEntriesRequest request) { return new AppendEntriesResponse(0, false); }
    private void applyToStateMachine(LogEntry entry) {}

    enum NodeState {
        FOLLOWER, CANDIDATE, LEADER
    }
}
```

**评分要点**:
- 分布式锁实现理解 (40%)
- 一致性算法原理掌握 (35%)
- 实际应用经验 (25%)

---

## 9. 系统设计

### 9.1 高并发系统设计

**题目**: 设计一个支持千万级用户的电商秒杀系统，说明架构设计和关键技术点。

**参考答案**:

#### 系统架构设计
```
秒杀系统架构:
├── CDN + 静态资源
├── 负载均衡 (Nginx/LVS)
├── 网关层 (限流/鉴权)
├── 应用层 (微服务)
├── 缓存层 (Redis Cluster)
├── 消息队列 (RocketMQ)
├── 数据库 (MySQL 主从)
└── 监控告警系统
```

#### 详细架构设计
```java
// 1. 秒杀服务核心实现
@RestController
@RequestMapping("/api/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 秒杀接口 - 核心优化点
     */
    @PostMapping("/kill/{productId}")
    @RateLimiter(rate = 1000, burst = 2000) // 限流
    public ApiResponse<SeckillResult> seckill(
            @PathVariable Long productId,
            @RequestHeader("User-Id") Long userId) {

        // 1. 参数校验
        if (productId == null || userId == null) {
            return ApiResponse.error("参数错误");
        }

        // 2. 用户限制检查 (Redis)
        String userKey = "seckill:user:" + userId + ":" + productId;
        if (redisTemplate.hasKey(userKey)) {
            return ApiResponse.error("您已经参与过该商品的秒杀");
        }

        // 3. 库存预检查 (Redis)
        String stockKey = "seckill:stock:" + productId;
        Long stock = redisTemplate.opsForValue().decrement(stockKey);
        if (stock < 0) {
            // 库存不足，恢复库存
            redisTemplate.opsForValue().increment(stockKey);
            return ApiResponse.error("商品已售罄");
        }

        // 4. 异步处理秒杀逻辑
        SeckillMessage message = new SeckillMessage(productId, userId);
        seckillService.asyncProcessSeckill(message);

        // 5. 设置用户参与标记
        redisTemplate.opsForValue().set(userKey, "1", Duration.ofHours(24));

        return ApiResponse.success(new SeckillResult("排队中，请稍后查询结果"));
    }

    /**
     * 查询秒杀结果
     */
    @GetMapping("/result/{productId}")
    public ApiResponse<SeckillResult> getSeckillResult(
            @PathVariable Long productId,
            @RequestHeader("User-Id") Long userId) {

        SeckillResult result = seckillService.getSeckillResult(productId, userId);
        return ApiResponse.success(result);
    }
}

// 2. 秒杀服务实现
@Service
public class SeckillService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SeckillOrderService orderService;

    /**
     * 异步处理秒杀
     */
    public void asyncProcessSeckill(SeckillMessage message) {
        // 发送到消息队列异步处理
        rocketMQTemplate.asyncSend("seckill-topic", message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("秒杀消息发送成功: {}", message);
            }

            @Override
            public void onException(Throwable e) {
                log.error("秒杀消息发送失败: {}", message, e);
                // 恢复库存
                recoverStock(message.getProductId());
            }
        });
    }

    /**
     * 处理秒杀消息
     */
    @RocketMQMessageListener(topic = "seckill-topic", consumerGroup = "seckill-consumer")
    @Component
    public static class SeckillMessageListener implements RocketMQListener<SeckillMessage> {

        @Autowired
        private SeckillOrderService orderService;

        @Override
        public void onMessage(SeckillMessage message) {
            try {
                // 创建秒杀订单
                SeckillOrder order = orderService.createSeckillOrder(
                    message.getProductId(), message.getUserId());

                // 更新结果缓存
                updateSeckillResult(message.getProductId(), message.getUserId(),
                    SeckillStatus.SUCCESS, order.getOrderId());

            } catch (Exception e) {
                log.error("处理秒杀消息失败: {}", message, e);

                // 更新结果缓存
                updateSeckillResult(message.getProductId(), message.getUserId(),
                    SeckillStatus.FAILED, null);

                // 恢复库存
                recoverStock(message.getProductId());
            }
        }

        private void updateSeckillResult(Long productId, Long userId,
                                       SeckillStatus status, String orderId) {
            String resultKey = "seckill:result:" + productId + ":" + userId;
            SeckillResult result = new SeckillResult(status, orderId);

            RedisTemplate<String, Object> redisTemplate = SpringContextUtil.getBean(RedisTemplate.class);
            redisTemplate.opsForValue().set(resultKey, result, Duration.ofHours(24));
        }
    }

    /**
     * 获取秒杀结果
     */
    public SeckillResult getSeckillResult(Long productId, Long userId) {
        String resultKey = "seckill:result:" + productId + ":" + userId;
        SeckillResult result = (SeckillResult) redisTemplate.opsForValue().get(resultKey);

        if (result == null) {
            return new SeckillResult(SeckillStatus.PROCESSING, null);
        }

        return result;
    }

    private void recoverStock(Long productId) {
        String stockKey = "seckill:stock:" + productId;
        redisTemplate.opsForValue().increment(stockKey);
    }
}
```

#### 性能优化策略
```java
// 1. 多级缓存架构
@Configuration
public class CacheConfig {

    /**
     * 本地缓存 (L1)
     */
    @Bean
    public Cache<String, Object> localCache() {
        return Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .recordStats()
            .build();
    }

    /**
     * 分布式缓存 (L2)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());

        // 序列化配置
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}

// 2. 限流策略
@Component
public class RateLimitService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 令牌桶限流
     */
    public boolean tryAcquire(String key, int permits, int capacity, int refillRate) {
        String script =
            "local key = KEYS[1] " +
            "local capacity = tonumber(ARGV[1]) " +
            "local tokens = tonumber(ARGV[2]) " +
            "local interval = tonumber(ARGV[3]) " +
            "local current = redis.call('hmget', key, 'tokens', 'timestamp') " +
            "local now = redis.call('time')[1] " +
            "if current[1] == false then " +
            "  redis.call('hmset', key, 'tokens', capacity - tokens, 'timestamp', now) " +
            "  redis.call('expire', key, interval * 2) " +
            "  return 1 " +
            "end " +
            "local currentTokens = tonumber(current[1]) " +
            "local lastRefill = tonumber(current[2]) " +
            "local elapsed = math.max(0, now - lastRefill) " +
            "local newTokens = math.min(capacity, currentTokens + elapsed * " + refillRate + ") " +
            "if newTokens >= tokens then " +
            "  redis.call('hmset', key, 'tokens', newTokens - tokens, 'timestamp', now) " +
            "  return 1 " +
            "else " +
            "  redis.call('hmset', key, 'tokens', newTokens, 'timestamp', now) " +
            "  return 0 " +
            "end";

        Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
            return connection.eval(script.getBytes(), ReturnType.INTEGER, 1,
                key.getBytes(),
                String.valueOf(capacity).getBytes(),
                String.valueOf(permits).getBytes(),
                String.valueOf(60).getBytes());
        });

        return Long.valueOf(1).equals(result);
    }

    /**
     * 滑动窗口限流
     */
    public boolean slidingWindowRateLimit(String key, int limit, int windowSize) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowSize * 1000L;

        String script =
            "redis.call('zremrangebyscore', KEYS[1], 0, ARGV[1]) " +
            "local count = redis.call('zcard', KEYS[1]) " +
            "if count < tonumber(ARGV[3]) then " +
            "  redis.call('zadd', KEYS[1], ARGV[2], ARGV[2]) " +
            "  redis.call('expire', KEYS[1], ARGV[4]) " +
            "  return 1 " +
            "else " +
            "  return 0 " +
            "end";

        Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
            return connection.eval(script.getBytes(), ReturnType.INTEGER, 1,
                key.getBytes(),
                String.valueOf(windowStart).getBytes(),
                String.valueOf(now).getBytes(),
                String.valueOf(limit).getBytes(),
                String.valueOf(windowSize).getBytes());
        });

        return Long.valueOf(1).equals(result);
    }
}

// 3. 数据库优化
@Configuration
public class DatabaseOptimization {

    /**
     * 读写分离配置
     */
    @Bean
    public DataSource dynamicDataSource() {
        DynamicDataSource dataSource = new DynamicDataSource();

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", masterDataSource());
        targetDataSources.put("slave1", slave1DataSource());
        targetDataSources.put("slave2", slave2DataSource());

        dataSource.setTargetDataSources(targetDataSources);
        dataSource.setDefaultTargetDataSource(masterDataSource());

        return dataSource;
    }

    /**
     * 分库分表配置
     */
    @Bean
    public ShardingDataSource shardingDataSource() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();

        // 订单表分片规则
        TableRuleConfiguration orderTableRule = new TableRuleConfiguration("seckill_order");
        orderTableRule.setActualDataNodes("ds${0..1}.seckill_order_${0..15}");
        orderTableRule.setDatabaseShardingStrategyConfig(
            new InlineShardingStrategyConfiguration("user_id", "ds${user_id % 2}"));
        orderTableRule.setTableShardingStrategyConfig(
            new InlineShardingStrategyConfiguration("user_id", "seckill_order_${user_id % 16}"));

        shardingRuleConfig.getTableRuleConfigs().add(orderTableRule);

        return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig, new Properties());
    }

    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds0", createDataSource("jdbc:mysql://db1:3306/seckill"));
        dataSourceMap.put("ds1", createDataSource("jdbc:mysql://db2:3306/seckill"));
        return dataSourceMap;
    }
}
```

#### 监控和告警
```java
@Component
public class SeckillMonitor {

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * 业务指标监控
     */
    public void recordSeckillMetrics(String productId, String result) {
        // 秒杀请求计数
        meterRegistry.counter("seckill.requests",
            "product", productId, "result", result).increment();

        // 成功率统计
        if ("success".equals(result)) {
            meterRegistry.counter("seckill.success", "product", productId).increment();
        }
    }

    /**
     * 系统性能监控
     */
    @Scheduled(fixedRate = 10000) // 每10秒监控一次
    public void monitorSystemPerformance() {
        // JVM 内存使用率
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        double heapUsagePercent = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;

        meterRegistry.gauge("jvm.memory.heap.usage.percent", heapUsagePercent);

        // CPU 使用率
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuUsage = osBean.getProcessCpuLoad() * 100;

        meterRegistry.gauge("system.cpu.usage.percent", cpuUsage);

        // 告警检查
        if (heapUsagePercent > 80) {
            sendAlert("JVM 内存使用率过高", String.format("当前使用率: %.2f%%", heapUsagePercent));
        }

        if (cpuUsage > 80) {
            sendAlert("CPU 使用率过高", String.format("当前使用率: %.2f%%", cpuUsage));
        }
    }

    private void sendAlert(String title, String message) {
        // 发送告警逻辑
        log.warn("系统告警: {} - {}", title, message);
    }
}
```

**评分要点**:
- 架构设计能力 (40%)
- 技术选型合理性 (30%)
- 性能优化思路 (30%)

---

## 10. 面试评分标准

### 10.1 技术深度评分

| 等级 | 分数 | 标准 |
|------|------|------|
| **优秀** | 90-100 | 深入理解原理，有丰富实战经验，能解决复杂问题 |
| **良好** | 80-89 | 理解核心概念，有一定实战经验，能解决常见问题 |
| **一般** | 70-79 | 了解基本概念，缺乏深入理解和实战经验 |
| **较差** | 60-69 | 概念模糊，缺乏实际应用经验 |
| **不合格** | <60 | 基本概念不清楚，无法胜任工作 |

### 10.2 综合评估维度

- **技术深度** (40%): 对技术原理的理解深度
- **实战经验** (30%): 实际项目经验和问题解决能力
- **学习能力** (15%): 对新技术的学习和适应能力
- **沟通表达** (15%): 技术表达和沟通能力

### 10.3 各技术栈评分细则

#### JVM 评分标准
- **优秀 (90-100分)**:
  - 深入理解 JVM 内存模型和垃圾回收原理
  - 熟练使用各种调优工具和参数
  - 有丰富的生产环境调优经验
  - 能够分析复杂的性能问题

- **良好 (80-89分)**:
  - 理解 JVM 基本原理和常用垃圾回收器
  - 掌握基本的调优方法和工具
  - 有一定的实际调优经验
  - 能够解决常见的性能问题

#### Redis 评分标准
- **优秀 (90-100分)**:
  - 深入理解 Redis 数据结构和底层实现
  - 熟练掌握各种缓存策略和问题解决方案
  - 有大规模 Redis 集群运维经验
  - 能够设计高可用的缓存架构

- **良好 (80-89分)**:
  - 理解 Redis 基本数据类型和使用场景
  - 掌握基本的缓存策略
  - 了解 Redis 集群和持久化机制
  - 能够解决常见的缓存问题

#### 消息队列评分标准
- **优秀 (90-100分)**:
  - 深入理解消息队列的架构和原理
  - 熟练掌握事务消息、顺序消息等高级特性
  - 有大规模消息系统设计和运维经验
  - 能够解决复杂的消息可靠性问题

- **良好 (80-89分)**:
  - 理解消息队列的基本概念和使用场景
  - 掌握基本的消息发送和消费模式
  - 了解消息可靠性保障机制
  - 能够进行基本的消息系统设计

### 10.4 面试流程建议

#### 初级工程师 (1-3年)
1. **基础知识** (30分钟):
   - Java 基础语法和面向对象
   - 集合框架和多线程
   - Spring Boot 基本使用

2. **数据库** (20分钟):
   - MySQL 基本操作和索引
   - 简单的 SQL 优化

3. **编程题** (30分钟):
   - 算法和数据结构基础题
   - 简单的业务逻辑实现

#### 中级工程师 (3-5年)
1. **框架原理** (40分钟):
   - Spring Boot 自动配置原理
   - MyBatis 工作原理
   - Redis 数据结构和应用

2. **系统设计** (30分钟):
   - 中等规模系统架构设计
   - 缓存策略和数据库优化

3. **问题解决** (20分钟):
   - 生产环境问题排查
   - 性能优化经验

#### 高级工程师 (5-8年)
1. **深度技术** (50分钟):
   - JVM 调优和故障排查
   - 分布式系统设计
   - 消息队列和一致性算法

2. **架构设计** (40分钟):
   - 大规模系统架构设计
   - 技术选型和权衡
   - 高可用和容灾设计

3. **团队协作** (20分钟):
   - 技术方案评审
   - 团队技术分享
   - 新人培养经验

### 10.5 面试官指导

#### 提问技巧
1. **层层深入**: 从基础概念开始，逐步深入到实现原理
2. **结合实际**: 询问具体的项目经验和问题解决过程
3. **开放性问题**: 让候选人展示思考过程和解决方案
4. **压力测试**: 适当提出有挑战性的问题

#### 评分原则
1. **客观公正**: 基于技术能力和经验进行评分
2. **全面考察**: 不仅看技术深度，也要看广度和实战能力
3. **发展潜力**: 考虑候选人的学习能力和成长空间
4. **团队匹配**: 评估是否适合团队文化和项目需求

---

## 📚 推荐学习资源

### 书籍推荐
- 《深入理解Java虚拟机》- 周志明
- 《Redis设计与实现》- 黄健宏
- 《高性能MySQL》- Baron Schwartz
- 《分布式系统概念与设计》- George Coulouris

### 在线资源
- Oracle Java 官方文档
- Redis 官方文档
- Spring Boot 官方指南
- Apache RocketMQ 官方文档

---

**文档版本**: v1.0
**最后更新**: 2024年1月
**适用对象**: 3-8年 Java 后端开发工程师

---

## 10. 面试评分标准

### 10.1 技术深度评分

| 等级 | 分数 | 标准 |
|------|------|------|
| **优秀** | 90-100 | 深入理解原理，有丰富实战经验，能解决复杂问题 |
| **良好** | 80-89 | 理解核心概念，有一定实战经验，能解决常见问题 |
| **一般** | 70-79 | 了解基本概念，缺乏深入理解和实战经验 |
| **较差** | 60-69 | 概念模糊，缺乏实际应用经验 |
| **不合格** | <60 | 基本概念不清楚，无法胜任工作 |

### 10.2 综合评估维度

- **技术深度** (40%): 对技术原理的理解深度
- **实战经验** (30%): 实际项目经验和问题解决能力
- **学习能力** (15%): 对新技术的学习和适应能力
- **沟通表达** (15%): 技术表达和沟通能力

---

## 📚 推荐学习资源

### 书籍推荐
- 《深入理解Java虚拟机》- 周志明
- 《Redis设计与实现》- 黄健宏
- 《高性能MySQL》- Baron Schwartz
- 《分布式系统概念与设计》- George Coulouris

### 在线资源
- Oracle Java 官方文档
- Redis 官方文档
- Spring Boot 官方指南
- Apache RocketMQ 官方文档

---

**文档版本**: v1.0  
**最后更新**: 2024年1月  
**适用对象**: 3-8年 Java 后端开发工程师
