package io.github.rose.i18n.interpolation;

import java.util.Locale;

/**
 * 消息插值器接口
 * 
 * <p>负责处理消息模板中的参数插值，支持多种插值语法和格式化选项。</p>
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
     * 插值消息模板
     * 
     * @param template 消息模板
     * @param args 参数数组
     * @param locale 语言环境
     * @return 插值后的消息
     */
    String interpolate(String template, Object[] args, Locale locale);

    /**
     * 插值消息模板（命名参数）
     * 
     * @param template 消息模板
     * @param namedArgs 命名参数映射
     * @param locale 语言环境
     * @return 插值后的消息
     */
    String interpolate(String template, java.util.Map<String, Object> namedArgs, Locale locale);

    /**
     * 检查模板是否需要插值
     * 
     * @param template 消息模板
     * @return 如果需要插值返回true，否则返回false
     */
    boolean needsInterpolation(String template);

    /**
     * 获取插值器名称
     * 
     * @return 插值器名称
     */
    String getName();

    /**
     * 获取支持的插值语法类型
     * 
     * @return 支持的语法类型集合
     */
    java.util.Set<InterpolationSyntax> getSupportedSyntax();

    /**
     * 插值语法类型枚举
     */
    enum InterpolationSyntax {
        /**
         * MessageFormat风格：{0}, {1}, {2}...
         */
        MESSAGE_FORMAT,

        /**
         * 命名参数风格：{name}, {age}, {email}...
         */
        NAMED_PARAMETERS,

        /**
         * 表达式风格：${user.name}, ${user.age}...
         * 支持简单属性访问和复杂的EL表达式（如果有EL实现可用）
         */
        EXPRESSION
    }
}
