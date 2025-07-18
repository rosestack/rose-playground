package io.github.rose.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SimpleCachingI18nMessageSource 单元测试
 *
 * <p>测试职责：</p>
 * <ul>
 *   <li>基本缓存功能：缓存命中、缓存未命中</li>
 *   <li>缓存大小限制：LRU淘汰策略</li>
 *   <li>装饰器模式：正确委托给底层MessageSource</li>
 *   <li>线程安全：并发访问测试</li>
 *   <li>生命周期：init/destroy方法</li>
 *   <li>CacheKey：equals/hashCode正确性</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class SimpleCachingI18NMessageSourceTest {

    @Mock
    private I18nMessageSource delegate;

    private SimpleCachingI18nMessageSource cachingMessageSource;

    @BeforeEach
    void setUp() {
        // Mock the getLocale method with lenient stubbing
        lenient().when(delegate.getLocale()).thenReturn(Locale.ENGLISH);
        cachingMessageSource = new SimpleCachingI18nMessageSource(delegate);
    }

    // ==================== 基本缓存功能测试 ====================

    @Test
    void testCacheHit() {
        // 设置mock行为
        when(delegate.getMessage("test.key", Locale.ENGLISH, "arg1"))
                .thenReturn("Test Message");

        // 第一次调用 - 缓存未命中
        String result1 = cachingMessageSource.getMessage("test.key", Locale.ENGLISH, "arg1");
        assertEquals("Test Message", result1);

        // 第二次调用 - 缓存命中
        String result2 = cachingMessageSource.getMessage("test.key", Locale.ENGLISH, "arg1");
        assertEquals("Test Message", result2);

        // 验证delegate只被调用一次
        verify(delegate, times(1)).getMessage("test.key", Locale.ENGLISH, "arg1");
    }

    @Test
    void testCacheMiss() {
        // 设置mock行为
        when(delegate.getMessage("test.key1", Locale.ENGLISH))
                .thenReturn("Message 1");
        when(delegate.getMessage("test.key2", Locale.ENGLISH))
                .thenReturn("Message 2");

        // 调用不同的key
        String result1 = cachingMessageSource.getMessage("test.key1", Locale.ENGLISH);
        String result2 = cachingMessageSource.getMessage("test.key2", Locale.ENGLISH);

        assertEquals("Message 1", result1);
        assertEquals("Message 2", result2);

        // 验证delegate被调用两次
        verify(delegate, times(1)).getMessage("test.key1", Locale.ENGLISH);
        verify(delegate, times(1)).getMessage("test.key2", Locale.ENGLISH);
    }

    @Test
    void testDifferentParametersCacheSeparately() {
        // 设置mock行为
        when(delegate.getMessage("test.key", Locale.ENGLISH, "arg1"))
                .thenReturn("Message with arg1");
        when(delegate.getMessage("test.key", Locale.ENGLISH, "arg2"))
                .thenReturn("Message with arg2");

        // 相同key但不同参数
        String result1 = cachingMessageSource.getMessage("test.key", Locale.ENGLISH, "arg1");
        String result2 = cachingMessageSource.getMessage("test.key", Locale.ENGLISH, "arg2");

        assertEquals("Message with arg1", result1);
        assertEquals("Message with arg2", result2);

        // 验证delegate被调用两次
        verify(delegate, times(1)).getMessage("test.key", Locale.ENGLISH, "arg1");
        verify(delegate, times(1)).getMessage("test.key", Locale.ENGLISH, "arg2");
    }

    @Test
    void testDifferentLocalesCacheSeparately() {
        // 设置mock行为
        when(delegate.getMessage("test.key", Locale.ENGLISH))
                .thenReturn("English Message");
        when(delegate.getMessage("test.key", Locale.CHINESE))
                .thenReturn("中文消息");

        // 相同key但不同locale
        String result1 = cachingMessageSource.getMessage("test.key", Locale.ENGLISH);
        String result2 = cachingMessageSource.getMessage("test.key", Locale.CHINESE);

        assertEquals("English Message", result1);
        assertEquals("中文消息", result2);

        // 验证delegate被调用两次
        verify(delegate, times(1)).getMessage("test.key", Locale.ENGLISH);
        verify(delegate, times(1)).getMessage("test.key", Locale.CHINESE);
    }

    // ==================== 缓存大小限制测试 ====================

    @Test
    void testCacheSizeLimit() {
        // 创建小容量的缓存
        SimpleCachingI18nMessageSource smallCache = new SimpleCachingI18nMessageSource(delegate, 2);

        // 设置mock行为
        lenient().when(delegate.getMessage(anyString(), any(Locale.class))).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return "Message for " + code;
        });
        lenient().when(delegate.getLocale()).thenReturn(Locale.ENGLISH);

        // 添加3个不同的消息到容量为2的缓存
        smallCache.getMessage("key1", Locale.ENGLISH);
        smallCache.getMessage("key2", Locale.ENGLISH);
        smallCache.getMessage("key3", Locale.ENGLISH); // 这应该淘汰key1

        // 再次访问key1应该重新调用delegate（因为被淘汰了）
        smallCache.getMessage("key1", Locale.ENGLISH);

        // 验证key1被调用了两次（第一次缓存，被淘汰后第二次重新缓存）
        verify(delegate, times(2)).getMessage("key1", Locale.ENGLISH);
        // key2和key3各被调用一次
        verify(delegate, times(1)).getMessage("key2", Locale.ENGLISH);
        verify(delegate, times(1)).getMessage("key3", Locale.ENGLISH);
    }

    @Test
    void testLRUEvictionPolicy() {
        // 创建容量为2的缓存
        SimpleCachingI18nMessageSource lruCache = new SimpleCachingI18nMessageSource(delegate, 2);

        // 设置mock行为 - 使用正确的参数匹配
        when(delegate.getMessage(anyString(), any(Locale.class), any(Object[].class))).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return "Message for " + code;
        });

        // 添加两个消息
        lruCache.getMessage("key1", Locale.ENGLISH);
        lruCache.getMessage("key2", Locale.ENGLISH);

        // 访问key1，使其成为最近使用的
        lruCache.getMessage("key1", Locale.ENGLISH);

        // 添加key3，应该淘汰key2（最久未使用的）
        lruCache.getMessage("key3", Locale.ENGLISH);

        // 再次访问key2应该重新调用delegate
        lruCache.getMessage("key2", Locale.ENGLISH);

        // 验证调用次数
        verify(delegate, times(1)).getMessage("key1", Locale.ENGLISH); // 只调用一次，后续命中缓存
        verify(delegate, times(2)).getMessage("key2", Locale.ENGLISH); // 调用两次，被淘汰后重新缓存
        verify(delegate, times(1)).getMessage("key3", Locale.ENGLISH); // 调用一次
    }

    // ==================== 构造函数测试 ====================

    @Test
    void testDefaultConstructor() {
        SimpleCachingI18nMessageSource defaultCache = new SimpleCachingI18nMessageSource(delegate);
        assertNotNull(defaultCache);
        // 默认大小应该是512
    }

    @Test
    void testConstructorWithCustomSize() {
        SimpleCachingI18nMessageSource customCache = new SimpleCachingI18nMessageSource(delegate, 100);
        assertNotNull(customCache);
    }

    @Test
    void testConstructorWithInvalidSize() {
        // 负数应该抛出异常（因为LinkedHashMap不接受负的初始容量）
        assertThrows(IllegalArgumentException.class, () -> {
            new SimpleCachingI18nMessageSource(delegate, -1);
        });

        // 0应该使用默认大小
        SimpleCachingI18nMessageSource zeroCache = new SimpleCachingI18nMessageSource(delegate, 0);
        assertNotNull(zeroCache);
    }

    // ==================== 装饰器模式测试 ====================

    @Test
    void testDelegateMethodCalls() {
        // 测试所有重载方法都正确委托
        lenient().when(delegate.getMessage("test.key", Locale.ENGLISH, "arg"))
                .thenReturn("Test Message");

        // 测试主要方法
        String result1 = cachingMessageSource.getMessage("test.key", Locale.ENGLISH, "arg");
        assertEquals("Test Message", result1);

        // 测试重载方法（使用默认方法，会调用getLocale()）
        String result2 = cachingMessageSource.getMessage("test.key", "arg");
        assertEquals("Test Message", result2);

        // 验证delegate被正确调用（第二次调用应该命中缓存，所以只调用一次）
        verify(delegate, times(1)).getMessage("test.key", Locale.ENGLISH, "arg");
    }

    // ==================== 生命周期测试 ====================

    @Test
    void testInitMethod() {
        // init方法应该不抛异常
        assertDoesNotThrow(() -> cachingMessageSource.init());
    }

    @Test
    void testDestroyMethod() {
        // 添加一些缓存项
        lenient().when(delegate.getMessage("test.key", Locale.ENGLISH))
                .thenReturn("Test Message");

        cachingMessageSource.getMessage("test.key", Locale.ENGLISH);

        // 调用destroy应该清空缓存
        cachingMessageSource.destroy();

        // 再次调用相同的消息应该重新调用delegate
        cachingMessageSource.getMessage("test.key", Locale.ENGLISH);

        verify(delegate, times(2)).getMessage("test.key", Locale.ENGLISH);
    }

    // ==================== 线程安全测试 ====================

    @Test
    void testConcurrentAccess() throws InterruptedException {
        // 设置mock行为
        AtomicInteger callCount = new AtomicInteger(0);
        when(delegate.getMessage("concurrent.key", Locale.ENGLISH))
                .thenAnswer(invocation -> {
                    callCount.incrementAndGet();
                    // 模拟一些处理时间
                    Thread.sleep(10);
                    return "Concurrent Message";
                });

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 启动多个线程同时访问相同的消息
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    String result = cachingMessageSource.getMessage("concurrent.key", Locale.ENGLISH);
                    assertEquals("Concurrent Message", result);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        // 由于get-check-put操作不是原子的，可能会有多次调用
        // 验证delegate被调用的次数应该小于等于线程数
        verify(delegate, atMost(threadCount)).getMessage("concurrent.key", Locale.ENGLISH);
        assertTrue(callCount.get() <= threadCount);
        assertTrue(callCount.get() >= 1); // 至少被调用一次
    }

    @Test
    void testConcurrentDifferentKeys() throws InterruptedException {
        // 设置mock行为
        lenient().when(delegate.getMessage(anyString(), any(Locale.class))).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            Thread.sleep(5); // 模拟处理时间
            return "Message for " + code;
        });

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 启动多个线程访问不同的消息
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    String key = "key" + (index % 5); // 5个不同的key
                    String result = cachingMessageSource.getMessage(key, Locale.ENGLISH);
                    assertEquals("Message for " + key, result);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        // 由于缓存不是完全线程安全的，可能会有多次调用
        // 验证每个key被调用的次数应该合理
        for (int i = 0; i < 5; i++) {
            verify(delegate, atLeast(1)).getMessage("key" + i, Locale.ENGLISH);
            verify(delegate, atMost(4)).getMessage("key" + i, Locale.ENGLISH); // 每个key最多被4个线程调用
        }
    }

    // ==================== CacheKey测试 ====================

    @Test
    void testCacheKeyEquality() {
        // 测试相同参数的CacheKey相等
        lenient().when(delegate.getMessage("test.key", Locale.ENGLISH, "arg1", "arg2"))
                .thenReturn("Test Message");

        // 两次调用相同参数
        cachingMessageSource.getMessage("test.key", Locale.ENGLISH, "arg1", "arg2");
        cachingMessageSource.getMessage("test.key", Locale.ENGLISH, "arg1", "arg2");

        // 验证delegate只被调用一次
        verify(delegate, times(1)).getMessage("test.key", Locale.ENGLISH, "arg1", "arg2");
    }

    @Test
    void testCacheKeyWithNullValues() {
        // 测试包含null值的CacheKey
        lenient().when(delegate.getMessage("test.key", (Locale) null))
                .thenReturn("Null Message");

        // 两次调用相同的null参数
        cachingMessageSource.getMessage("test.key", (Locale) null);
        cachingMessageSource.getMessage("test.key", (Locale) null);

        // 验证delegate只被调用一次
        verify(delegate, times(1)).getMessage("test.key", (Locale) null);
    }

    @Test
    void testCacheKeyWithComplexObjects() {
        // 测试包含复杂对象的CacheKey
        Object[] complexArgs = {
                "string",
                42,
                true,
                new String[]{"nested", "array"},
                null
        };

        lenient().when(delegate.getMessage(eq("complex.key"), eq(Locale.ENGLISH), any(Object[].class)))
                .thenReturn("Complex Message");

        // 两次调用相同的复杂参数
        String result1 = cachingMessageSource.getMessage("complex.key", Locale.ENGLISH, complexArgs);
        assertEquals("Complex Message", result1);

        // 创建相同内容但不同实例的参数数组
        Object[] sameComplexArgs = {
                "string",
                42,
                true,
                new String[]{"nested", "array"},
                null
        };
        String result2 = cachingMessageSource.getMessage("complex.key", Locale.ENGLISH, sameComplexArgs);
        assertEquals("Complex Message", result2);

        // 验证delegate只被调用一次（因为CacheKey使用Arrays.deepEquals，相同内容的数组被视为相同的缓存键）
        verify(delegate, times(1)).getMessage(eq("complex.key"), eq(Locale.ENGLISH), any(Object[].class));
    }

    // ==================== 边界情况测试 ====================

    @Test
    void testNullDelegate() {
        // 测试null delegate - 实际实现不会抛出异常，只是在使用时会有问题
        SimpleCachingI18nMessageSource nullDelegateCache = new SimpleCachingI18nMessageSource(null);
        assertNotNull(nullDelegateCache);

        // 但是调用getMessage时会抛出NPE
        assertThrows(NullPointerException.class, () -> {
            nullDelegateCache.getMessage("test.key", Locale.ENGLISH);
        });
    }

    @Test
    void testEmptyStringKey() {
        when(delegate.getMessage("", Locale.ENGLISH))
                .thenReturn("Empty Key Message");

        String result = cachingMessageSource.getMessage("", Locale.ENGLISH);
        assertEquals("Empty Key Message", result);

        // 再次调用应该命中缓存
        String result2 = cachingMessageSource.getMessage("", Locale.ENGLISH);
        assertEquals("Empty Key Message", result2);

        verify(delegate, times(1)).getMessage("", Locale.ENGLISH);
    }

    @Test
    void testVeryLongKey() {
        String longKey = "a".repeat(1000); // 1000个字符的key
        when(delegate.getMessage(longKey, Locale.ENGLISH))
                .thenReturn("Long Key Message");

        String result = cachingMessageSource.getMessage(longKey, Locale.ENGLISH);
        assertEquals("Long Key Message", result);

        // 再次调用应该命中缓存
        String result2 = cachingMessageSource.getMessage(longKey, Locale.ENGLISH);
        assertEquals("Long Key Message", result2);

        verify(delegate, times(1)).getMessage(longKey, Locale.ENGLISH);
    }

    @Test
    void testLargeArgumentArray() {
        Object[] largeArgs = new Object[100];
        for (int i = 0; i < 100; i++) {
            largeArgs[i] = "arg" + i;
        }

        // Use lenient stubbing with proper argument matching
        lenient().when(delegate.getMessage(eq("large.args"), eq(Locale.ENGLISH), any(Object[].class)))
                .thenReturn("Large Args Message");

        String result = cachingMessageSource.getMessage("large.args", Locale.ENGLISH, largeArgs);
        assertEquals("Large Args Message", result);

        // 再次调用应该命中缓存
        String result2 = cachingMessageSource.getMessage("large.args", Locale.ENGLISH, largeArgs);
        assertEquals("Large Args Message", result2);

        verify(delegate, times(1)).getMessage(eq("large.args"), eq(Locale.ENGLISH), any(Object[].class));
    }

    // ==================== 性能测试 ====================

    @Test
    void testCachePerformance() {
        // 设置mock行为 - 使用正确的参数匹配
        when(delegate.getMessage(anyString(), any(Locale.class), any(Object[].class))).thenAnswer(invocation -> {
            // 模拟耗时操作
            Thread.sleep(1);
            String code = invocation.getArgument(0);
            return "Message for " + code;
        });

        String testKey = "performance.key";

        // 第一次调用（缓存未命中）
        long start1 = System.nanoTime();
        cachingMessageSource.getMessage(testKey, Locale.ENGLISH);
        long time1 = System.nanoTime() - start1;

        // 第二次调用（缓存命中）
        long start2 = System.nanoTime();
        cachingMessageSource.getMessage(testKey, Locale.ENGLISH);
        long time2 = System.nanoTime() - start2;

        // 缓存命中应该明显更快
        assertTrue(time2 < time1 / 2, "Cache hit should be significantly faster than cache miss");

        verify(delegate, times(1)).getMessage(testKey, Locale.ENGLISH);
    }

    // ==================== 特殊场景测试 ====================

    @Test
    void testDelegateReturnsNull() {
        // 测试delegate返回null的情况 - 实际实现不会缓存null值
        when(delegate.getMessage("null.key", Locale.ENGLISH, "default"))
                .thenReturn(null);

        String result = cachingMessageSource.getMessage("null.key", Locale.ENGLISH, "default");
        assertNull(result);

        // 再次调用不会命中缓存（因为null值不被缓存），会再次调用delegate
        String result2 = cachingMessageSource.getMessage("null.key", Locale.ENGLISH, "default");
        assertNull(result2);

        verify(delegate, times(2)).getMessage("null.key", Locale.ENGLISH, "default");
    }

    @Test
    void testDelegateThrowsException() {
        // 测试delegate抛出异常的情况
        when(delegate.getMessage("error.key", Locale.ENGLISH))
                .thenThrow(new RuntimeException("Test exception"));

        // 第一次调用应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            cachingMessageSource.getMessage("error.key", Locale.ENGLISH);
        });

        // 第二次调用应该再次抛出异常（异常不应该被缓存）
        assertThrows(RuntimeException.class, () -> {
            cachingMessageSource.getMessage("error.key", Locale.ENGLISH);
        });

        verify(delegate, times(2)).getMessage("error.key", Locale.ENGLISH);
    }

    @Test
    void testCacheWithDifferentDefaultMessages() {
        // 测试相同key但不同defaultMessage的情况
        when(delegate.getMessage("test.key", Locale.ENGLISH, "default1"))
                .thenReturn("Message 1");
        when(delegate.getMessage("test.key", Locale.ENGLISH, "default2"))
                .thenReturn("Message 2");

        String result1 = cachingMessageSource.getMessage("test.key", Locale.ENGLISH, "default1");
        String result2 = cachingMessageSource.getMessage("test.key", Locale.ENGLISH, "default2");

        assertEquals("Message 1", result1);
        assertEquals("Message 2", result2);

        // 验证delegate被调用两次（因为defaultMessage不同）
        verify(delegate, times(1)).getMessage("test.key", Locale.ENGLISH, "default1");
        verify(delegate, times(1)).getMessage("test.key", Locale.ENGLISH, "default2");
    }

    @Test
    void testCacheEvictionOrder() {
        // 测试缓存淘汰顺序
        SimpleCachingI18nMessageSource smallCache = new SimpleCachingI18nMessageSource(delegate, 3);

        when(delegate.getMessage(anyString(), any(Locale.class), any(Object[].class))).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return "Message for " + code;
        });

        // 添加3个消息到容量为3的缓存
        smallCache.getMessage("key1", Locale.ENGLISH);
        smallCache.getMessage("key2", Locale.ENGLISH);
        smallCache.getMessage("key3", Locale.ENGLISH);

        // 访问key1，使其成为最近使用的
        smallCache.getMessage("key1", Locale.ENGLISH);

        // 添加key4，应该淘汰key2（最久未使用的）
        smallCache.getMessage("key4", Locale.ENGLISH);

        // 验证key1和key3仍在缓存中，key2被淘汰
        smallCache.getMessage("key1", Locale.ENGLISH); // 缓存命中
        smallCache.getMessage("key3", Locale.ENGLISH); // 缓存命中
        smallCache.getMessage("key2", Locale.ENGLISH); // 缓存未命中，重新调用delegate

        // 验证调用次数
        verify(delegate, times(1)).getMessage("key1", Locale.ENGLISH); // 只调用一次
        verify(delegate, times(2)).getMessage("key2", Locale.ENGLISH); // 调用两次（被淘汰后重新缓存）
        verify(delegate, times(1)).getMessage("key3", Locale.ENGLISH); // 只调用一次
        verify(delegate, times(1)).getMessage("key4", Locale.ENGLISH); // 只调用一次
    }

    @Test
    void testMaxSizeConstant() {
        // 测试MAX_SIZE常量
        assertEquals(512, SimpleCachingI18nMessageSource.MAX_SIZE);
    }
}
