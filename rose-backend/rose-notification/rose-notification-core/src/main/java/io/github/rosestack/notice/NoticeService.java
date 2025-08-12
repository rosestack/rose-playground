package io.github.rosestack.notice;

import io.github.rosestack.notice.render.TemplateContentRenderFactory;
import io.github.rosestack.notice.sender.RetryableSender;
import io.github.rosestack.notice.sender.SenderFactory;
import io.github.rosestack.notice.spi.BlacklistChecker;
import io.github.rosestack.notice.spi.IdempotencyStore;
import io.github.rosestack.notice.spi.NoticeSendInterceptor;
import io.github.rosestack.notice.spi.Sender;
import io.github.rosestack.notice.support.NoopBlacklistChecker;
import io.github.rosestack.notice.support.NoopIdempotencyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 通用通知发送服务，支持同步、异步、批量、重试、黑名单、拦截器、幂等。 */
public class NoticeService {
    private static final Logger log = LoggerFactory.getLogger(NoticeService.class);

    private final List<NoticeSendInterceptor> interceptors = new ArrayList<>();

    private BlacklistChecker blacklistChecker;
    private IdempotencyStore idempotencyStore;
    private ExecutorService executor;
    private boolean retryable = false;
    private NoticeMetrics metrics;
    private boolean executorManagedExternally = false;

    public NoticeService() {
        ServiceLoader.load(NoticeSendInterceptor.class).forEach(interceptors::add);

        this.blacklistChecker = new NoopBlacklistChecker();
        this.idempotencyStore = new NoopIdempotencyStore();
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.executorManagedExternally = false;
    }

    public void addInterceptor(NoticeSendInterceptor interceptor) {
        this.interceptors.add(interceptor);
    }

    public void removeInterceptor(NoticeSendInterceptor interceptor) {
        this.interceptors.remove(interceptor);
    }

    public void setBlacklistChecker(BlacklistChecker blacklistChecker) {
        this.blacklistChecker = blacklistChecker;
    }

    public void setIdempotencyStore(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
        this.executorManagedExternally = true;
    }

    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }

    /** 可选注入 metrics（Micrometer） */
    public void setMetrics(NoticeMetrics metrics) {
        this.metrics = metrics;
    }

    public SendResult send(SendRequest request, SenderConfiguration config) {
        validate(request);
        try {
            preCheck(request);

            renderTemplate(request, config.getTemplateType());

            long start = System.nanoTime();
            SendResult result = doSend(request, config);
            if (metrics != null) {
                metrics.recordSuccess(System.nanoTime() - start);
            }

            postProcess(request, result);
            return result;
        } catch (NoticeException e) {
            if (metrics != null) {
                metrics.recordFailure(0);
            }
            for (NoticeSendInterceptor interceptor : interceptors) {
                interceptor.onError(request, e);
            }
            log.error("通知异常: {}", e.getMessage());
            return SendResult.fail(e.getMessage(), request.getRequestId());
        } catch (Exception e) {
            if (metrics != null) {
                metrics.recordFailure(0);
            }
            for (NoticeSendInterceptor interceptor : interceptors) {
                interceptor.onError(request, e);
            }
            log.error("通知异常", e);
            return SendResult.fail(e.getMessage(), request.getRequestId());
        }
    }

    private SendResult doSend(SendRequest request, SenderConfiguration config) {
        Sender baseSender = SenderFactory.getSender(config.getChannelType(), config);
        Sender sender = baseSender;
        if (retryable) {
            sender = new RetryableSender(baseSender);
            // 让重试包装器读取重试相关配置
            sender.configure(config);
        }

        String receiptId = sender.send(request);
        return SendResult.success(request.getRequestId(), receiptId);
    }

    private void preCheck(SendRequest request) {
        if (idempotencyStore.exists(request.getRequestId())) {
            log.warn("命中幂等: requestId={}", request.getRequestId());
            throw new NoticeException("重复请求，已处理: " + request.getRequestId());
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
        for (NoticeSendInterceptor interceptor : interceptors) {
            interceptor.afterSend(request, result);
        }
        log.info("通知发送结果: {}", result);
    }

    public CompletableFuture<SendResult> sendAsync(SendRequest request, SenderConfiguration channelConfig) {
        return CompletableFuture.supplyAsync(() -> send(request, channelConfig), executor);
    }

    public List<SendResult> sendBatch(List<SendRequest> requests, SenderConfiguration channelConfig) {
        if (requests == null || requests.isEmpty()) return Collections.emptyList();
        List<CompletableFuture<SendResult>> futures = new ArrayList<>(requests.size());
        for (SendRequest req : requests) {
            futures.add(sendAsync(req, channelConfig));
        }
        List<SendResult> results = new ArrayList<>(requests.size());
        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<SendResult> f = futures.get(i);
            SendRequest req = requests.get(i);
            try {
                results.add(f.join());
            } catch (Exception ex) {
                String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                results.add(SendResult.fail(msg, req.getRequestId()));
            }
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
        if (!executorManagedExternally && executor != null) {
            executor.shutdown();
        }
        SenderFactory.destroy();
        io.github.rosestack.notice.sender.sms.SmsProviderFactory.destroy();
    }
}
