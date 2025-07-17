package io.github.rose.i18n.interpolation;

import java.util.Locale;

/**
 * 消息插值器接口
 *
 * <p>负责处理消息模板中的参数插值，支持多种插值语法。</p>
 *
 * <p>支持的插值语法：</p>
 * <ul>
 *   <li>MessageFormat风格：{0}, {1}, {2}...</li>
 *   <li>命名参数风格：{name}, {age}, {email}...</li>
 *   <li>表达式风格：${user.name}, ${user.age}...</li>
 * </ul>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 */
public interface MessageInterpolator {

    /**
     * 对 message 进行参数插值
     *
     * @param message 模板字符串，如 "Hello, {0}!" 或 "Hi, {name}!"
     * @param args    参数数组或 Map
     * @param locale
     * @return 替换后的字符串
     */
    String interpolate(String message, Object args, Locale locale);
}
