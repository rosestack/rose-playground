package io.github.rose.i18n.interpolation;

import java.util.Locale;
import java.util.Map;

/**
 * 消息插值器接口
 *
 * <p>负责处理消息模板中的参数插值，支持多种插值方式：</p>
 * <ul>
 *   <li>数组参数插值：处理 {} 占位符和 {0}, {1} 等 MessageFormat 风格</li>
 *   <li>Map 参数插值：处理 {name} 命名参数和 ${expression} 表达式风格</li>
 *   <li>InterpolationParameter：推荐使用的有序参数构建器</li>
 * </ul>
 *
 * <p>实现类会根据 args 参数的类型自动选择合适的插值方式：</p>
 * <ul>
 *   <li>InterpolationParameter：推荐使用，保证参数顺序和类型安全</li>
 *   <li>Object[] 或 Object...：使用数组插值方式</li>
 *   <li>Map&lt;String, Object&gt;：使用 Map 插值方式（注意有序性问题）</li>
 *   <li>其他类型：转换为字符串后处理</li>
 * </ul>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 */
public interface MessageInterpolator {

    /**
     * 消息插值（推荐使用 InterpolationParameter）
     *
     * @param message 模板字符串
     * @param locale  语言环境
     * @param arg     插值参数
     * @return 替换后的字符串
     */
    String interpolate(String message, Locale locale, Object arg);
}
