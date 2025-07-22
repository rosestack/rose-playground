package io.github.rose.notice.sender;

import io.github.rose.notice.SendRequest;
import io.github.rose.notice.spi.AbstractConfigure;
import io.github.rose.notice.spi.Sender;
import lombok.extern.slf4j.Slf4j;

/**
 * 控制台打印 Sender，作为兜底实现。
 */
@Slf4j
public class ConsoleSender extends AbstractConfigure implements Sender {
    @Override
    public String getChannelType() {
        return CONSOLE;
    }

    @Override
    public String send(SendRequest request) {
        log.info("[ConsoleSender] send message {} to {}", request.getTemplateContent(), request.getTarget());
        return null;
    }

    @Override
    public void destroy() {}
}
