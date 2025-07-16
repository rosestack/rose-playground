package io.github.rose.i18n;

import org.springframework.core.OrderComparator;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ServiceLoader;

/**
 * MessageSource 管理类，支持自动发现、组合、动态注册与优先级排序。
 */
public class MessageSourceManager {
    private static final List<MessageSource> sources = new CopyOnWriteArrayList<>();
    private static volatile CompositeMessageSource instance;

    static {
        reloadAndDiscover();
    }

    /**
     * 初始化所有已注册消息源
     */
    public static void init() {
        for (MessageSource source : sources) {
            source.init();
        }
    }

    /**
     * 销毁所有已注册消息源
     */
    public static void destroy() {
        for (MessageSource source : sources) {
            source.destroy();
        }
    }

    /**
     * 通过 SPI 自动发现并注册所有 MessageSource 实现
     */
    public static void reloadAndDiscover() {
        List<MessageSource> discovered = new ArrayList<>();
        ServiceLoader.load(MessageSource.class).forEach(discovered::add);
        OrderComparator.sort(discovered);
        sources.clear();
        sources.addAll(discovered);
        instance = new CompositeMessageSource(sources);
        init(); // 自动初始化
    }

    /**
     * 动态注册一个消息源
     */
    public static void registerSource(MessageSource source) {
        sources.add(source);
        OrderComparator.sort(sources);
        instance = new CompositeMessageSource(sources);
    }

    /**
     * 动态移除一个消息源
     */
    public static void unregisterSource(MessageSource source) {
        sources.remove(source);
        instance = new CompositeMessageSource(sources);
    }

    /**
     * 获取所有已注册的消息源（有序）
     */
    public static List<MessageSource> getRegisteredSources() {
        return Collections.unmodifiableList(sources);
    }

    /**
     * 获取自动组合的消息源（优先级排序）
     */
    public static MessageSource getInstance() {
        return instance;
    }
}
