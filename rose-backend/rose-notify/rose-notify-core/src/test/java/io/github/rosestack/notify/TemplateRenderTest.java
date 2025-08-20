package io.github.rosestack.notify;

import io.github.rosestack.notify.render.SimpleVariableTemplateContentRender;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TemplateRenderTest {
    @Test
    void simpleVariableRender() {
        SimpleVariableTemplateContentRender render = new SimpleVariableTemplateContentRender();
        String tpl = "hello ${name}, id=${id-1}, path=${user.name}";
        String out = render.render(tpl, Map.of("name", "rose", "id-1", 10, "user.name", "u"));
        Assertions.assertEquals("hello rose, id=10, path=u", out);
    }
}
