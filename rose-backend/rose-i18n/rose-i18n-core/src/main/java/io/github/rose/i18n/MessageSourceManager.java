package io.github.rose.i18n;

import io.github.rose.core.lang.Prioritized;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * MessageSource 管理类，支持自动发现、组合、动态注册与优先级排序。
 * <p>
 * 提供统一的消息源管理、组合与生命周期管理。
 */
public final class MessageSourceManager {
    /**
     * SPI 发现的消息源列表
     */
    private static final List<MessageSource> SOURCES = new CopyOnWriteArrayList<>();
    /**
     * 组合消息源单例
     */
    private static volatile CompositeMessageSource instance;

    private MessageSourceManager() {
    }

    static {
        reloadAndDiscover();
    }

    /**
     * 初始化所有已注册消息源
     */
    public static void init() {
        for (MessageSource source : SOURCES) {
            source.init();
        }
    }

    /**
     * 销毁所有已注册消息源
     */
    public static void destroy() {
        for (MessageSource source : SOURCES) {
            source.destroy();
        }
    }

    /**
     * 通过 SPI 自动发现并注册所有 MessageSource 实现
     */
    public static void reloadAndDiscover() {
        List<MessageSource> discovered = new ArrayList<>();
        ServiceLoader.load(MessageSource.class).forEach(discovered::add);
        discovered.sort(Comparator.comparingInt(Prioritized::getPriority));
        SOURCES.clear();
        SOURCES.addAll(discovered);
        instance = new CompositeMessageSource(SOURCES);
        init(); // 自动初始化
    }

    /**
     * 动态注册一个消息源
     */
    public static void registerSource(MessageSource source) {
        SOURCES.add(source);
        SOURCES.sort(Comparator.comparingInt(Prioritized::getPriority));
        instance = new CompositeMessageSource(SOURCES);
    }

    /**
     * 动态移除一个消息源
     */
    public static void unregisterSource(MessageSource source) {
        SOURCES.remove(source);
        instance = new CompositeMessageSource(SOURCES);
    }

    /**
     * 获取所有已注册的消息源（有序）
     */
    public static List<MessageSource> getRegisteredSources() {
        return Collections.unmodifiableList(SOURCES);
    }

    /**
     * 获取自动组合的消息源（优先级排序）
     */
    public static MessageSource getInstance() {
        return instance;
    }
}
