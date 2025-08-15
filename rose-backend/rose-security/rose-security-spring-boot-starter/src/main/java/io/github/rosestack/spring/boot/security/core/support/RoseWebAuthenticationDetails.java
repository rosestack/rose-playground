package io.github.rosestack.spring.boot.security.core.support;

import io.github.rosestack.spring.util.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static io.github.rosestack.spring.boot.security.core.service.TokenService.TOKEN_HEADER;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Data
@Builder
public class RoseWebAuthenticationDetails implements Serializable {
    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * 用户代理字符串
     */
    private String userAgent;

    /**
     * HTTP引用页面
     */
    private String referer;

    /**
     * 请求ID（用于追踪）
     */
    private String requestId;

    /**
     * 请求URI
     */
    private String requestUri;

    /**
     * HTTP方法
     */
    private String httpMethod;

    // ========== 会话信息 ==========

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * Token信息
     */
    private String token;

    /**
     * 设备指纹
     */
    private String deviceFingerprint;

    /**
     * 地理位置信息
     */
    private String location;

    /**
     * 是否为可信设备
     */
    @Builder.Default
    private Boolean trustedDevice = false;

    /**
     * 用户ID
     */
    private String userId;

    private String username;

    /**
     * 认证级别
     */
    private String authLevel;

    // ========== 错误信息 ==========

    /**
     * 认证是否成功
     */
    private Boolean authSuccess;

    /**
     * 错误代码（认证失败时）
     */
    private String errorCode;

    /**
     * 错误消息（认证失败时）
     */
    private String errorMessage;

    /**
     * 异常堆栈信息（发生异常时）
     */
    private String stackTrace;

    // ========== 请求数据 ==========

    /**
     * 请求参数（Query Parameters）
     */
    @Builder.Default
    private Map<String, String[]> requestParameters = new HashMap<>();

    /**
     * 请求体内容（仅记录非敏感内容）
     */
    private String requestBody;

    /**
     * 请求头信息（排除敏感头）
     */
    @Builder.Default
    private Map<String, String> requestHeaders = new HashMap<>();

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 扩展属性
     */
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * 设置扩展属性
     */
    public void setAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(key, value);
    }

    /**
     * 获取扩展属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        if (attributes == null) {
            return null;
        }
        return (T) attributes.get(key);
    }

    /**
     * 标记认证成功
     */
    public RoseWebAuthenticationDetails markAuthSuccess() {
        this.authSuccess = true;
        this.errorCode = null;
        this.errorMessage = null;
        this.stackTrace = null;
        return this;
    }

    /**
     * 标记认证失败
     *
     * @param errorCode 错误代码
     * @param errorMessage 错误消息
     */
    public RoseWebAuthenticationDetails markAuthFailure(String errorCode, String errorMessage) {
        this.authSuccess = false;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * 设置异常信息
     *
     * @param throwable 异常对象
     */
    public RoseWebAuthenticationDetails withException(Throwable throwable) {
        if (throwable != null) {
            this.stackTrace = getStackTraceAsString(throwable);
            if (this.errorMessage == null) {
                this.errorMessage = throwable.getMessage();
            }
        }
        return this;
    }

    /**
     * 获取异常堆栈字符串
     */
    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getSimpleName()).append(": ").append(throwable.getMessage());

        StackTraceElement[] elements = throwable.getStackTrace();
        // 只保留前5行堆栈信息，避免过长
        int limit = Math.min(5, elements.length);
        for (int i = 0; i < limit; i++) {
            sb.append("\n\tat ").append(elements[i].toString());
        }

        if (elements.length > limit) {
            sb.append("\n\t... ").append(elements.length - limit).append(" more");
        }

        return sb.toString();
    }

    /**
     * 复制当前上下文
     */
    public RoseWebAuthenticationDetails copy() {
        return RoseWebAuthenticationDetails.builder()
                .clientIp(this.clientIp)
                .userAgent(this.userAgent)
                .referer(this.referer)
                .requestId(this.requestId)
                .requestUri(this.requestUri)
                .httpMethod(this.httpMethod)
                .sessionId(this.sessionId)
                .token(this.token)
                .deviceFingerprint(this.deviceFingerprint)
                .location(this.location)
                .trustedDevice(this.trustedDevice)
                .userId(this.userId)
                .username(this.username)
                .authLevel(this.authLevel)
                .authSuccess(this.authSuccess)
                .errorCode(this.errorCode)
                .errorMessage(this.errorMessage)
                .stackTrace(this.stackTrace)
                .requestParameters(
                        this.requestParameters != null ? new HashMap<>(this.requestParameters) : new HashMap<>())
                .requestBody(this.requestBody)
                .requestHeaders(this.requestHeaders != null ? new HashMap<>(this.requestHeaders) : new HashMap<>())
                .contentType(this.contentType)
                .attributes(this.attributes != null ? new HashMap<>(this.attributes) : new HashMap<>())
                .build();
    }

    private static String generateDeviceFingerprint(HttpServletRequest request) {
        StringBuilder fingerprint = new StringBuilder();

        // User-Agent
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            fingerprint.append(userAgent.hashCode());
        }

        // Accept-Language
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null) {
            fingerprint.append("-").append(acceptLanguage.hashCode());
        }

        // Accept-Encoding
        String acceptEncoding = request.getHeader("Accept-Encoding");
        if (acceptEncoding != null) {
            fingerprint.append("-").append(acceptEncoding.hashCode());
        }

        return "FP-" + Math.abs(fingerprint.toString().hashCode());
    }

    public static RoseWebAuthenticationDetails fromRequest(HttpServletRequest request) {
        if (request == null) {
            return RoseWebAuthenticationDetails.builder().build();
        }

        return RoseWebAuthenticationDetails.builder()
                .clientIp(ServletUtils.getClientIp())
                .location("TODO")
                .userAgent(ServletUtils.getUserAgent())
                .referer(ServletUtils.getReferer())
                .requestUri(request.getRequestURI())
                .httpMethod(request.getMethod())
                .requestId(ServletUtils.getRequestId())
                .sessionId(ServletUtils.getSessionId())
                .token(ServletUtils.getRequestHeader(TOKEN_HEADER))
                .deviceFingerprint(generateDeviceFingerprint(request))
                .requestParameters(ServletUtils.collectRequestParameters(request))
                .requestHeaders(ServletUtils.collectRequestHeaders(request))
                .requestBody(extractRequestBody(request))
                .contentType(request.getContentType())
                .build();
    }

    /**
     * 提取请求体（仅对小文件和文本类型）
     */
    private static String extractRequestBody(HttpServletRequest request) {
        String contentType = request.getContentType();

        // 只处理文本类型的请求体
        if (contentType == null
                || (!contentType.startsWith("application/json")
                        && !contentType.startsWith("application/xml")
                        && !contentType.startsWith("text/"))) {
            return null;
        }

        // 限制请求体大小，避免记录过大的内容
        int contentLength = request.getContentLength();
        if (contentLength > 10240) { // 10KB 限制
            return "Request body too large (" + contentLength + " bytes)";
        }

        try {
            // 这里只记录 POST/PUT 等方法的请求体
            if ("POST".equalsIgnoreCase(request.getMethod())
                    || "PUT".equalsIgnoreCase(request.getMethod())
                    || "PATCH".equalsIgnoreCase(request.getMethod())) {

                // 注意：实际项目中，读取请求体需要小心处理
                // 因为 InputStream 只能读取一次，需要使用包装器或缓存
                return "Request body available (not extracted to avoid stream issues)";
            }
        } catch (Exception e) {
            return "Failed to extract request body: " + e.getMessage();
        }

        return null;
    }
}
