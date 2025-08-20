package io.github.rosestack.notify.spi;

import java.util.Map;

public interface TemplateContentRender {
    String render(String templateContent, Map<String, Object> variables);
}
