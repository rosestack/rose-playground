package io.github.rosestack.notice.render;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import io.github.rosestack.notice.spi.TemplateContentRender;
import java.util.Map;

/**
 * 基于 Groovy 的动态模板渲染实现，支持复杂表达式。 需引入 groovy 依赖。
 */
public class GroovyTemplateContentRender extends AbstractTemplateContentRender implements TemplateContentRender {
    private final SimpleTemplateEngine engine = new SimpleTemplateEngine();

    @Override
    public String doRender(String templateContent, Map<String, Object> variables) {
        try {
            Template template = engine.createTemplate(templateContent);
            return template.make(variables).toString();
        } catch (Exception e) {
            throw new RuntimeException("Groovy 模板渲染失败", e);
        }
    }

    @Override
    public void validate(String templateContent, Map<String, Object> variables) {
        try {
            Template template = engine.createTemplate(templateContent);
            template.make(variables).toString();
        } catch (Exception e) {
            throw new RuntimeException("Groovy 模板校验失败: " + e.getMessage(), e);
        }
    }
}
