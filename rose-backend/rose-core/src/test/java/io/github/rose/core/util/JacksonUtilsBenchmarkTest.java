package io.github.rose.core.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JacksonUtil性能基准测试
 * 用于验证优化后的性能表现
 */
@Slf4j
class JacksonUtilsBenchmarkTest {

    private List<TestData> testDataList;
    private static final int TEST_SIZE = 1000;
    private static final int ITERATIONS = 10;

    @BeforeEach
    void setUp() {
        testDataList = new ArrayList<>();
        for (int i = 0; i < TEST_SIZE; i++) {
            TestData data = new TestData();
            data.setId((long) i);
            data.setName("Test Name " + i);
            data.setActive(i % 2 == 0);
            data.setCreatedAt(LocalDateTime.now());
            data.setTags(Arrays.asList("tag" + i, "category" + (i % 10), "type" + (i % 5)));
            testDataList.add(data);
        }
    }

    @Test
    void benchmarkSerialization() {
        log.info("=== Serialization Benchmark ===");
        
        // 预热
        for (int i = 0; i < 100; i++) {
            JacksonUtils.toString(testDataList.get(i % testDataList.size()));
        }
        
        long totalTime = 0;
        for (int iteration = 0; iteration < ITERATIONS; iteration++) {
            long startTime = System.nanoTime();
            
            for (TestData data : testDataList) {
                JacksonUtils.toString(data);
            }
            
            long endTime = System.nanoTime();
            long iterationTime = endTime - startTime;
            totalTime += iterationTime;
            
            log.info("Iteration {}: {} ms",
                iteration + 1,
                TimeUnit.NANOSECONDS.toMillis(iterationTime));
        }

        long avgTime = totalTime / ITERATIONS;
        log.info("Average time: {} ms", TimeUnit.NANOSECONDS.toMillis(avgTime));
        log.info("Throughput: {:.2f} objects/ms",
            (double) TEST_SIZE / TimeUnit.NANOSECONDS.toMillis(avgTime));
    }

    @Test
    void benchmarkDeserialization() {
        log.info("=== Deserialization Benchmark ===");
        
        // 准备JSON字符串
        List<String> jsonStrings = new ArrayList<>();
        for (TestData data : testDataList) {
            jsonStrings.add(JacksonUtils.toString(data));
        }
        
        // 预热
        for (int i = 0; i < 100; i++) {
            JacksonUtils.fromString(jsonStrings.get(i % jsonStrings.size()), TestData.class);
        }
        
        long totalTime = 0;
        for (int iteration = 0; iteration < ITERATIONS; iteration++) {
            long startTime = System.nanoTime();
            
            for (String json : jsonStrings) {
                JacksonUtils.fromString(json, TestData.class);
            }
            
            long endTime = System.nanoTime();
            long iterationTime = endTime - startTime;
            totalTime += iterationTime;
            
            System.out.printf("Iteration %d: %d ms%n", 
                iteration + 1, 
                TimeUnit.NANOSECONDS.toMillis(iterationTime));
        }
        
        long avgTime = totalTime / ITERATIONS;
        System.out.printf("Average time: %d ms%n", TimeUnit.NANOSECONDS.toMillis(avgTime));
        System.out.printf("Throughput: %.2f objects/ms%n", 
            (double) TEST_SIZE / TimeUnit.NANOSECONDS.toMillis(avgTime));
    }

    @Test
    void benchmarkCloning() {
        log.info("=== Cloning Benchmark ===");
        
        // 预热
        for (int i = 0; i < 100; i++) {
            JacksonUtils.clone(testDataList.get(i % testDataList.size()));
        }
        
        long totalTime = 0;
        for (int iteration = 0; iteration < ITERATIONS; iteration++) {
            long startTime = System.nanoTime();
            
            for (TestData data : testDataList) {
                JacksonUtils.clone(data);
            }
            
            long endTime = System.nanoTime();
            long iterationTime = endTime - startTime;
            totalTime += iterationTime;
            
            System.out.printf("Iteration %d: %d ms%n", 
                iteration + 1, 
                TimeUnit.NANOSECONDS.toMillis(iterationTime));
        }
        
        long avgTime = totalTime / ITERATIONS;
        System.out.printf("Average time: %d ms%n", TimeUnit.NANOSECONDS.toMillis(avgTime));
        System.out.printf("Throughput: %.2f objects/ms%n", 
            (double) TEST_SIZE / TimeUnit.NANOSECONDS.toMillis(avgTime));
    }

    @Test
    void benchmarkPrettyPrint() {
        log.info("=== Pretty Print Benchmark ===");
        
        // 预热
        for (int i = 0; i < 100; i++) {
            JacksonUtils.toPrettyString(testDataList.get(i % testDataList.size()));
        }
        
        long totalTime = 0;
        for (int iteration = 0; iteration < ITERATIONS; iteration++) {
            long startTime = System.nanoTime();
            
            for (TestData data : testDataList) {
                JacksonUtils.toPrettyString(data);
            }
            
            long endTime = System.nanoTime();
            long iterationTime = endTime - startTime;
            totalTime += iterationTime;
            
            System.out.printf("Iteration %d: %d ms%n", 
                iteration + 1, 
                TimeUnit.NANOSECONDS.toMillis(iterationTime));
        }
        
        long avgTime = totalTime / ITERATIONS;
        System.out.printf("Average time: %d ms%n", TimeUnit.NANOSECONDS.toMillis(avgTime));
        System.out.printf("Throughput: %.2f objects/ms%n", 
            (double) TEST_SIZE / TimeUnit.NANOSECONDS.toMillis(avgTime));
    }

    @Test
    void memoryUsageTest() {
        log.info("=== Memory Usage Test ===");
        
        Runtime runtime = Runtime.getRuntime();
        
        // 强制垃圾回收
        System.gc();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 执行大量序列化操作
        List<String> results = new ArrayList<>();
        for (TestData data : testDataList) {
            results.add(JacksonUtils.toString(data));
        }
        
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = afterMemory - beforeMemory;
        
        System.out.printf("Memory used: %d KB%n", memoryUsed / 1024);
        System.out.printf("Memory per object: %d bytes%n", memoryUsed / TEST_SIZE);
        
        // 清理
        results.clear();
        System.gc();
    }

    // 测试数据类
    public static class TestData {
        private Long id;
        private String name;
        private boolean active;
        private LocalDateTime createdAt;
        private List<String> tags;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
    }
}
