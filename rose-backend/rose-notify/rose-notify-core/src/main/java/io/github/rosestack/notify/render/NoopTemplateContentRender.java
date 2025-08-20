package io.github.rosestack.notify.render;

import io.github.rosestack.notify.spi.TemplateContentRender;
import java.util.Map;

/**
 * 不做任何渲染，直接返回原内容。 适合纯文本/无变量场景。
 */
public class NoopTemplateContentRender extends AbstractTemplateContentRender implements TemplateContentRender {
    @Override
    public String doRender(String templateContent, Map<String, Object> variables) {
        return templateContent;
    }

    @Override
    public void validate(String templateContent, Map<String, Object> variables) {
        // 永远合法
    }
}
