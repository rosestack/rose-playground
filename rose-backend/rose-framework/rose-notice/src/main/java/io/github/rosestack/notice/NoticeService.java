package io.github.rosestack.notice;

import io.github.rosestack.notice.render.TemplateContentRenderFactory;
import io.github.rosestack.notice.sender.RetryableSender;
import io.github.rosestack.notice.sender.SenderFactory;
import io.github.rosestack.notice.spi.*;
import io.github.rosestack.notice.support.NoopBlacklistChecker;
import io.github.rosestack.notice.support.NoopIdempotencyStore;
import io.github.rosestack.notice.support.NoopRateLimiter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 通用通知发送服务，支持同步、异步、批量、重试、限流、黑名单、拦截器、幂等。
 */
@Slf4j
@Setter
public class NoticeService {
    private final List<NoticeSendInterceptor> interceptors = new ArrayList<>();

    private RateLimiter rateLimiter;
    private BlacklistChecker blacklistChecker;
    private IdempotencyStore idempotencyStore;
    private ExecutorService executor;
    private boolean retryable = false;

    public NoticeService() {
        ServiceLoader.load(NoticeSendInterceptor.class).forEach(interceptors::add);

        this.rateLimiter = new NoopRateLimiter();
        this.blacklistChecker = new NoopBlacklistChecker();
        this.idempotencyStore = new NoopIdempotencyStore();
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void addInterceptor(NoticeSendInterceptor interceptor) {
        this.interceptors.add(interceptor);
    }

    public void removeInterceptor(NoticeSendInterceptor interceptor) {
        this.interceptors.remove(interceptor);
    }

    public SendResult send(SendRequest request, SenderConfiguration config) {
        validate(request);
        try {
            preCheck(request);

            renderTemplate(request, config.getTemplateType());

            SendResult result = doSend(request, config);

            postProcess(request, result);
            return result;
        } catch (Exception e) {
            for (NoticeSendInterceptor interceptor : interceptors) {
                interceptor.onError(request, e);
            }
            log.error("通知异常", e);
            return SendResult.fail(e.getMessage(), request.getRequestId());
        }
    }

    private SendResult doSend(SendRequest request, SenderConfiguration config) {
        Sender sender = SenderFactory.getSender(config.getChannelType(), config);
        if (retryable) {
            sender = new RetryableSender(sender);
        }
        sender.send(request);

        String receiptId = sender.send(request);
        return SendResult.success(request.getRequestId(), receiptId);
    }

    private void preCheck(SendRequest request) {
        if (idempotencyStore.exists(request.getRequestId())) {
            log.info("命中幂等: requestId={}", request.getRequestId());
            throw new NoticeException("重复请求，已处理: " + request.getRequestId());
        }
        if (!rateLimiter.allow(request)) {
            log.warn("超出限流: target={}", request.getTarget());
            throw new NoticeException("超出限流: " + request.getTarget());
        }
        if (blacklistChecker.isBlacklisted(request)) {
            log.warn("命中黑名单: target={}", request.getTarget());
            throw new NoticeException("命中黑名单: " + request.getTarget());
        }
        for (NoticeSendInterceptor interceptor : interceptors) {
            interceptor.beforeSend(request);
        }
    }

    private void renderTemplate(SendRequest request, String templateType) {
        String renderedTemplate = TemplateContentRenderFactory.getRender(templateType)
                .render(request.getTemplateContent(), request.getVariables());
        request.setTemplateContent(renderedTemplate);
    }

    private void postProcess(SendRequest request, SendResult result) {
        idempotencyStore.put(request.getRequestId());
        rateLimiter.record(request);
        for (NoticeSendInterceptor interceptor : interceptors) {
            interceptor.afterSend(request, result);
        }
        log.info("通知发送结果: request={}", result);
    }

    public CompletableFuture<SendResult> sendAsync(SendRequest request, SenderConfiguration channelConfig) {
        return CompletableFuture.supplyAsync(() -> send(request, channelConfig), executor);
    }

    public List<SendResult> sendBatch(List<SendRequest> requests, SenderConfiguration channelConfig) {
        if (requests == null || requests.isEmpty()) return Collections.emptyList();
        List<SendResult> results = new ArrayList<>();
        for (SendRequest req : requests) {
            results.add(send(req, channelConfig));
        }
        return results;
    }

    public CompletableFuture<List<SendResult>> sendBatchAsync(
            List<SendRequest> requests, SenderConfiguration channelConfig) {
        if (requests == null || requests.isEmpty()) return CompletableFuture.completedFuture(Collections.emptyList());
        List<CompletableFuture<SendResult>> futures = new ArrayList<>();
        for (SendRequest req : requests) {
            futures.add(sendAsync(req, channelConfig));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<SendResult> results = new ArrayList<>();
                    for (CompletableFuture<SendResult> f : futures) {
                        results.add(f.join());
                    }
                    return results;
                });
    }

    private void validate(SendRequest request) {
        if (request == null) {
            throw new NoticeException("SendRequest 为空");
        }
        if (request.getRequestId() == null || request.getRequestId().trim().isEmpty()) {
            throw new NoticeException("requestId 为空");
        }
        if (request.getTemplateContent() == null
                || request.getTemplateContent().trim().isEmpty()) {
            throw new NoticeException("通知模版为空");
        }
        if (request.getTarget() == null || request.getTarget().trim().isEmpty()) {
            throw new NoticeException("通知对象为空");
        }
    }

    public void destroy() {
        executor.shutdown();
    }
}
