package io.github.rose.notice.render;

import io.github.rose.notice.spi.TemplateContentRender;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模板渲染器工厂，支持 SPI 自动发现和运行时动态注册。
 * 调用方可通过 key 获取不同的渲染实现。
 */
public class TemplateContentRenderFactory {
    private static final NoopTemplateContentRender DEFAULT_TEMPLATE_RENDER = new NoopTemplateContentRender();
    private static final Map<String, TemplateContentRender> RENDERS = new ConcurrentHashMap<>();

    static {
        ServiceLoader.load(TemplateContentRender.class)
                .forEach(render -> RENDERS.put(render.getClass().getSimpleName(), render));
    }

    public static TemplateContentRender getRender(String key) {
        if (key == null) {
            return DEFAULT_TEMPLATE_RENDER;
        }
        return RENDERS.get(key);
    }

    public static void register(String key, TemplateContentRender render) {
        RENDERS.put(key.toLowerCase(), render);
    }
}
