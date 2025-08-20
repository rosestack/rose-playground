package io.github.rosestack.notify.spi;

import io.github.rosestack.notify.NotifyException;
import io.github.rosestack.notify.SenderConfiguration;
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
                throw new NotifyException("配置初始化失败", e);
            }
            isConfigured.set(true);
        }
    }

    public abstract void doConfigure(SenderConfiguration config) throws Exception;
}
