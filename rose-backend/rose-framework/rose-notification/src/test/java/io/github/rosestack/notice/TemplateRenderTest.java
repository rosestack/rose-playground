package io.github.rosestack.notice;

import io.github.rosestack.notice.render.SimpleVariableTemplateContentRender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class TemplateRenderTest {
    @Test
    void simpleVariableRender() {
        SimpleVariableTemplateContentRender render = new SimpleVariableTemplateContentRender();
        String tpl = "hello ${name}, id=${id-1}, path=${user.name}";
        String out = render.render(tpl, Map.of("name", "rose", "id-1", 10, "user.name", "u"));
        Assertions.assertEquals("hello rose, id=10, path=u", out);
    }
}


