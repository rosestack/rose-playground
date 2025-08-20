package io.github.rosestack.notify.render;

import io.github.rosestack.notify.spi.TemplateContentRender;
import java.util.Map;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public abstract class AbstractTemplateContentRender implements TemplateContentRender {
    @Override
    public String render(String templateContent, Map<String, Object> variables) {
        validate(templateContent, variables);
        return doRender(templateContent, variables);
    }

    public abstract void validate(String templateContent, Map<String, Object> variables);

    public abstract String doRender(String templateContent, Map<String, Object> variables);
}
