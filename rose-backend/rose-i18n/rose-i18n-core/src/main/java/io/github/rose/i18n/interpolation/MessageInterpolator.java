package io.github.rose.i18n.interpolation;

import java.util.Locale;
import java.util.Map;

/**
 * 消息插值器接口
 *
 * <p>负责处理消息模板中的参数插值，支持两种插值方式：</p>
 * <ul>
 *   <li>数组参数插值：处理 {} 占位符和 {0}, {1} 等 MessageFormat 风格</li>
 *   <li>Map 参数插值：处理 {name} 命名参数和 ${expression} 表达式风格</li>
 * </ul>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 */
public interface MessageInterpolator {

    /**
     * 使用数组参数进行消息插值
     *
     * <p>处理以下格式：</p>
     * <ul>
     *   <li>{} 占位符：FormatUtils.format() 风格，如 "Hello {}, you are {} years old!"</li>
     *   <li>{0}, {1}, {2}...：MessageFormat 风格，如 "Hello {0}, you are {1} years old!"</li>
     * </ul>
     *
     * @param message 模板字符串
     * @param args    参数数组
     * @param locale  区域设置
     * @return 替换后的字符串
     */
    String interpolate(String message, Object[] args, Locale locale);

    /**
     * 使用 Map 参数进行消息插值
     *
     * <p>处理以下格式：</p>
     * <ul>
     *   <li>{name} 命名参数：如 "Hello {name}, you are {age} years old!"</li>
     *   <li>${expression} 表达式：如 "Hello ${user.name}, you are ${user.age} years old!"</li>
     * </ul>
     *
     * @param message 模板字符串
     * @param args    参数 Map
     * @param locale  区域设置
     * @return 替换后的字符串
     */
    String interpolate(String message, Map<String, Object> args, Locale locale);
}
