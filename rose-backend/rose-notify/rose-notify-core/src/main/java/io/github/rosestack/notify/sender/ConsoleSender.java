package io.github.rosestack.notify.sender;

import io.github.rosestack.notify.SendRequest;
import io.github.rosestack.notify.SenderConfiguration;
import io.github.rosestack.notify.spi.AbstractConfigure;
import io.github.rosestack.notify.spi.Sender;
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

    @Override
    public void doConfigure(SenderConfiguration config) throws Exception {}
}
