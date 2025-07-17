package io.github.rose.i18n.interpolation;

import java.util.Locale;
import java.util.Map;

/**
 * 模板插值器接口
 * 
 * <p>单一职责：负责将模板中的占位符替换为实际值</p>
 * 
 * <p>遵循SOLID原则：</p>
 * <ul>
 *   <li>S - 单一职责：只负责模板插值</li>
 *   <li>O - 开闭原则：可以扩展新的插值实现</li>
 *   <li>L - 里氏替换：所有实现都可以互相替换</li>
 *   <li>I - 接口隔离：接口最小化，只包含必要方法</li>
 *   <li>D - 依赖倒置：依赖抽象而不是具体实现</li>
 * </ul>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public interface TemplateInterpolator {

    /**
     * 插值模板
     * 
     * @param template 模板字符串
     * @param variables 变量映射
     * @param locale 语言环境
     * @return 插值后的字符串
     */
    String interpolate(String template, Map<String, Object> variables, Locale locale);

    /**
     * 检查是否需要插值
     * 
     * @param template 模板字符串
     * @return 如果需要插值返回true，否则返回false
     */
    boolean needsInterpolation(String template);

    /**
     * 检查是否支持指定的模板
     * 
     * @param template 模板字符串
     * @return 如果支持返回true，否则返回false
     */
    boolean supports(String template);
}
