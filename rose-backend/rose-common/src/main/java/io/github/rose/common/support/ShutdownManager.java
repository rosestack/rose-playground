package io.github.rose.common.support;

import io.github.rose.common.util.ThreadUtils;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;

/**
 * 确保应用退出时能关闭后台线程
 */
@Slf4j
@RequiredArgsConstructor
public class ShutdownManager {
    private final ScheduledExecutorService scheduledExecutorService;

    @PreDestroy
    public void destroy() {
        shutdownAsyncManager();
    }

    private void shutdownAsyncManager() {
        if (scheduledExecutorService != null) {
            try {
                ThreadUtils.shutdownAndAwaitTermination(scheduledExecutorService);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
