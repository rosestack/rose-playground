package io.github.rose.notice.spi;

import io.github.rose.notice.SendRequest;
import io.github.rose.notice.SendResult;

/**
 * 通用发送拦截器 SPI，支持发送前、后、异常时自定义处理。
 */
public interface NoticeSendInterceptor {
    /**
     * 发送前
     */
    default void beforeSend(SendRequest request) {}

    /**
     * 发送后
     */
    default void afterSend(SendRequest request, SendResult result) {}

    /**
     * 异常时
     */
    default void onError(SendRequest request, Exception ex) {}
}
