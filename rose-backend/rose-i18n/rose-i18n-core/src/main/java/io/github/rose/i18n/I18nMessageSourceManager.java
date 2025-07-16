package io.github.rose.i18n;

import org.springframework.core.OrderComparator;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ServiceLoader;

/**
 * I18nMessageSource 管理类，支持自动发现、组合、动态注册与优先级排序。
 */
public class I18nMessageSourceManager {
    private static final List<I18nMessageSource> sources = new CopyOnWriteArrayList<>();
    private static volatile CompositeI18nMessageSource composite;

    static {
        reloadFromSPI();
    }

    /**
     * 初始化所有已注册消息源
     */
    public static void init() {
        for (I18nMessageSource source : sources) {
            source.init();
        }
    }

    /**
     * 销毁所有已注册消息源
     */
    public static void destroy() {
        for (I18nMessageSource source : sources) {
            source.destroy();
        }
    }

    /**
     * 通过 SPI 自动发现并注册所有 I18nMessageSource 实现
     */
    public static void reloadFromSPI() {
        List<I18nMessageSource> discovered = new ArrayList<>();
        ServiceLoader.load(I18nMessageSource.class).forEach(discovered::add);
        OrderComparator.sort(discovered);
        sources.clear();
        sources.addAll(discovered);
        composite = new CompositeI18nMessageSource(sources);
        init(); // 自动初始化
    }

    /**
     * 动态注册一个消息源
     */
    public static void register(I18nMessageSource source) {
        sources.add(source);
        OrderComparator.sort(sources);
        composite = new CompositeI18nMessageSource(sources);
    }

    /**
     * 动态移除一个消息源
     */
    public static void unregister(I18nMessageSource source) {
        sources.remove(source);
        composite = new CompositeI18nMessageSource(sources);
    }

    /**
     * 获取所有已注册的消息源（有序）
     */
    public static List<I18nMessageSource> getSources() {
        return Collections.unmodifiableList(sources);
    }

    /**
     * 获取自动组合的消息源（优先级排序）
     */
    public static I18nMessageSource getComposite() {
        return composite;
    }
}
