package io.github.rose.notice.spi;

import java.util.Map;

public interface TemplateContentRender {
    String render(String templateContent, Map<String, Object> variables);
}
