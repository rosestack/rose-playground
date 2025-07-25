package io.github.rosestack.notice.spi;

import io.github.rosestack.notice.NoticeException;
import io.github.rosestack.notice.SenderConfiguration;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractConfigure implements Configurable {
    protected volatile AtomicBoolean isConfigured = new AtomicBoolean(false);
    protected volatile SenderConfiguration config;

    @Override
    public void configure(SenderConfiguration config) {
        this.config = config;

        if (isConfigured.get()) {
            return;
        }
        synchronized (this) {
            if (isConfigured.get()) {
                return;
            }
            try {
                doConfigure(config);
            } catch (Exception e) {
                throw new NoticeException("阿里云短信配置不完整", e);
            }
            isConfigured.set(true);
        }
    }

    public abstract void doConfigure(SenderConfiguration config) throws Exception;
}
