package io.github.rosestack.core.util;

import io.github.rosestack.core.Constants;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Servlet 工具类
 * <p>
 * 提供 HTTP 请求响应处理的常用工具方法，支持参数提取、客户端信息获取、响应渲染等功能。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>类型安全的参数提取和转换</li>
 *   <li>客户端 IP 地址和 User-Agent 获取</li>
 *   <li>Cookie 操作和会话管理</li>
 *   <li>URL 编码解码缓存优化</li>
 * </ul>
 * <p>
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 参数提取
 * String name = ServletUtils.getParameter("name", "默认值");
 *
 * // 客户端信息
 * String clientIp = ServletUtils.getClientIp();
 *
 * // 响应渲染
 * ServletUtils.renderJson(response, "{\"status\":\"success\"}");
 * }</pre>
 * <p>
 * <strong>注意：</strong>所有方法都是线程安全的，支持高并发访问。
 *
 * @author chensoul
 * @see HttpServletRequest
 * @see HttpServletResponse
 * @see RequestContextHolder
 * @since 1.0.0
 */
@Slf4j
public abstract class ServletUtils {
    private static final Map<String, String> URL_DECODE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> URL_ENCODE_CACHE = new ConcurrentHashMap<>();

    /**
     * 用于检测客户端真实 IP 的 HTTP 头列表，按优先级排序
     */
    private static final List<String> DEFAULT_IP_HEADERS = Arrays.asList(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
    );

    /**
     * 私有构造函数，防止实例化
     */
    private ServletUtils() {
    }

    /**
     * 获取请求参数值
     *
     * @param name 参数名
     * @return 参数值，不存在时返回 null
     */
    public static String getParameter(String name) {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getParameter(name) : null;
    }

    public static String getParameter(String name, String defaultValue) {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return defaultValue;
        }
        String value = request.getParameter(name);
        return StringUtils.hasLength(value) ? value : defaultValue;
    }

    public static Map<String, String[]> getParams(ServletRequest request) {
        if (request == null) {
            return Collections.emptyMap();
        }
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    public static Map<String, String[]> getParams() {
        HttpServletRequest request = getCurrentRequest();
        return getParams(request);
    }

    public static Map<String, String> getParamMap(ServletRequest request) {
        if (request == null) {
            return Collections.emptyMap();
        }

        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : getParams(request).entrySet()) {
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                params.put(entry.getKey(), String.join(",", values));
            }
        }
        return params;
    }

    public static Map<String, String> getParamMap() {
        HttpServletRequest request = getCurrentRequest();
        return getParamMap(request);
    }

    /**
     * 获得所有请求参数（保留多值）
     *
     * @param request 请求对象
     * @return 参数Map
     */
    public static Map<String, List<String>> getParamListMap(ServletRequest request) {
        if (request == null) {
            return Collections.emptyMap();
        }

        Map<String, List<String>> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : getParams(request).entrySet()) {
            String[] values = entry.getValue();
            if (values != null) {
                params.put(entry.getKey(), Arrays.asList(values));
            }
        }
        return params;
    }

    /**
     * 获得所有请求参数（保留多值）
     *
     * @return 参数Map
     */
    public static Map<String, List<String>> getParamListMap() {
        HttpServletRequest request = getCurrentRequest();
        return getParamListMap(request);
    }

    /**
     * Retrieves a request header value by name.
     *
     * @param name The header name to retrieve
     * @return The header value, or null if not found or no request context
     */
    public static String getHeader(String name) {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getHeader(name) : null;
    }

    public static String getHeader(String name, String defaultValue) {
        String value = getHeader(name);
        return StringUtils.hasLength(value) ? value : defaultValue;
    }

    public static Map<String, String> getHeaders() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return Collections.emptyMap();
        }

        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                headers.put(name, request.getHeader(name));
            }
        }
        return headers;
    }

    /**
     * 获取当前 HTTP 请求对象
     *
     * @return 当前请求对象，无请求上下文时返回 null
     */
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes requestAttributes = getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return requestAttributes.getRequest();
    }

    public static HttpServletResponse getCurrentResponse() {
        ServletRequestAttributes requestAttributes = getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return requestAttributes.getResponse();
    }

    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes ? (ServletRequestAttributes) attributes : null;
    }

    /**
     * 将字符串渲染到客户端
     *
     * @param response    响应对象
     * @param string      待渲染的字符串
     * @param contentType 内容类型
     */
    public static void renderString(HttpServletResponse response, String string, String contentType) {
        try {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(contentType);
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.getWriter().print(string);
        } catch (IOException e) {
            log.error("Failed to render string to response", e);
            throw new RuntimeException("Failed to render string to response", e);
        }
    }

    /**
     * 将JSON字符串渲染到客户端
     *
     * @param response 响应对象
     * @param json     JSON字符串
     */
    public static void renderJson(HttpServletResponse response, String json) {
        renderString(response, json, MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * URL编码
     *
     * @param str 待编码的字符串
     * @return 编码后的字符串
     * @throws UnsupportedEncodingException 编码异常
     */
    public static String urlEncode(String str) throws UnsupportedEncodingException {
        if (!StringUtils.hasLength(str)) {
            return str;
        }

        // 检查缓存
        String cached = URL_ENCODE_CACHE.get(str);
        if (cached != null) {
            return cached;
        }

        String encoded = URLEncoder.encode(str, StandardCharsets.UTF_8.name());
        URL_ENCODE_CACHE.put(str, encoded);
        return encoded;
    }

    /**
     * URL解码
     *
     * @param str 待解码的字符串
     * @return 解码后的字符串
     * @throws UnsupportedEncodingException 解码异常
     */
    public static String urlDecode(String str) throws UnsupportedEncodingException {
        if (!StringUtils.hasLength(str)) {
            return str;
        }

        // 检查缓存
        String cached = URL_DECODE_CACHE.get(str);
        if (cached != null) {
            return cached;
        }

        String decoded = URLDecoder.decode(str, StandardCharsets.UTF_8.name());
        URL_DECODE_CACHE.put(str, decoded);
        return decoded;
    }

    /**
     * 安全的URL编码（不抛出异常）
     *
     * @param str 待编码的字符串
     * @return 编码后的字符串，失败时返回原字符串
     */
    public static String urlEncodeSafe(String str) {
        try {
            return urlEncode(str);
        } catch (UnsupportedEncodingException e) {
            log.warn("Failed to URL encode string: {}", str, e);
            return str;
        }
    }

    /**
     * 安全的URL解码（不抛出异常）
     *
     * @param str 待解码的字符串
     * @return 解码后的字符串，失败时返回原字符串
     */
    public static String urlDecodeSafe(String str) {
        try {
            return urlDecode(str);
        } catch (UnsupportedEncodingException e) {
            log.warn("Failed to URL decode string: {}", str, e);
            return str;
        }
    }

    public static String getClientIp(HttpServletRequest request, String... otherHeaderNames) {
        if (request == null) {
            return null;
        }

        Set<String> headerNames = new LinkedHashSet<>(DEFAULT_IP_HEADERS);
        headerNames.addAll(Arrays.asList(otherHeaderNames));

        String ip;
        for (String header : headerNames) {
            ip = request.getHeader(header);
            ip = getReverseProxyIp(ip);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }

        // Fall back to remote address if no proxy headers found
        ip = request.getRemoteAddr();
        return getReverseProxyIp(ip);
    }

    /**
     * Processes a potentially comma-separated IP address string from proxy headers.
     * <p>
     * When requests pass through multiple proxies or load balancers, the forwarded IP
     * headers often contain comma-separated lists of IP addresses representing the
     * chain of proxies. This method extracts the first valid IP address from such lists.
     *
     * <p><strong>Processing Logic:</strong>
     * <ul>
     *   <li>If IP contains commas, split and check each part</li>
     *   <li>Return first non-blank, non-"unknown" IP found</li>
     *   <li>Return original IP if no commas or no valid IPs found</li>
     * </ul>
     *
     * <p><strong>Example Input/Output:</strong>
     * <pre>
     * "192.168.1.100, 10.0.0.1, unknown" → "192.168.1.100"
     * "unknown, 203.0.113.1" → "203.0.113.1"
     * "192.168.1.100" → "192.168.1.100"
     * </pre>
     *
     * @param ip The IP address string to process, potentially comma-separated.
     *           Can be null or empty.
     * @return The first valid IP address found, or the original string if no processing needed
     */
    public static String getReverseProxyIp(String ip) {
        if (ip != null && ip.contains(",")) {
            for (String subIp : ip.split(",")) {
                String trimmedIp = subIp.trim();
                if (StringUtils.hasLength(trimmedIp) && !"unknown".equalsIgnoreCase(trimmedIp)) {
                    return trimmedIp;
                }
            }
        }
        return ip;
    }

    /**
     * 获取客户端真实 IP 地址
     *
     * @return 客户端 IP 地址，获取失败返回 null
     */
    public static String getClientIp() {
        HttpServletRequest request = getCurrentRequest();
        return getClientIp(request);
    }

    /**
     * 获取Cookie值
     *
     * @param name Cookie名称
     * @return Cookie值
     */
    public static String getCookieValue(String name) {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 获取Cookie对象
     *
     * @param name Cookie名称
     * @return Cookie对象
     */
    public static Cookie getCookie(String name) {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * 添加Cookie
     *
     * @param response 响应对象
     * @param name     Cookie名称
     * @param value    Cookie值
     */
    public static void addCookie(HttpServletResponse response, String name, String value) {
        addCookie(response, name, value, -1);
    }

    /**
     * 添加Cookie
     *
     * @param response 响应对象
     * @param name     Cookie名称
     * @param value    Cookie值
     * @param maxAge   最大存活时间（秒）
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * 清理缓存
     */
    public static void clearCache() {
        URL_DECODE_CACHE.clear();
        URL_ENCODE_CACHE.clear();
    }

    /**
     * 获取请求的完整URL（使用当前请求）
     *
     * @return 完整URL
     */
    public static String getFullUrl() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        String queryString = request.getQueryString();
        String requestURI = request.getRequestURI();

        if (StringUtils.hasLength(queryString)) {
            return requestURI + "?" + queryString;
        }
        return requestURI;
    }

    /**
     * 获取请求的User-Agent（使用当前请求）
     *
     * @return User-Agent
     */
    public static String getUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getHeader(HttpHeaders.USER_AGENT) : null;
    }

    /**
     * 获取请求的详细信息（用于调试）
     *
     * @return 请求详细信息
     */
    public static String getRequestDetails() {
        HttpServletRequest request = getCurrentRequest();

        if (request == null) {
            return "Request is null";
        }

        StringBuilder details = new StringBuilder();
        details.append("Method: ").append(request.getMethod()).append("\n");
        details.append("URL: ").append(getFullUrl()).append("\n");
        details.append("Protocol: ").append(request.getScheme()).append("\n");
        details.append("Server: ").append(request.getServerName()).append(":").append(request.getServerPort()).append("\n");
        details.append("Context Path: ").append(request.getContextPath()).append("\n");
        details.append("Servlet Path: ").append(request.getServletPath()).append("\n");
        details.append("Path Info: ").append(request.getPathInfo()).append("\n");
        details.append("Query String: ").append(request.getQueryString()).append("\n");
        details.append("Remote Address: ").append(request.getRemoteAddr()).append("\n");
        details.append("Remote Host: ").append(request.getRemoteHost()).append("\n");
        details.append("Remote Port: ").append(request.getRemotePort()).append("\n");
        details.append("User Agent: ").append(getUserAgent()).append("\n");

        return details.toString();
    }

    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "SYSTEM";
        }
        String userId = request.getHeader(Constants.HeaderName.HEADER_USER_ID);
        if (StringUtils.hasLength(userId)) {
            return userId;
        }

        userId = MDC.get(Constants.MdcName.MDC_USER_ID);
        if (StringUtils.hasLength(userId)) {
            return userId;
        }
        return "SYSTEM";
    }

    public static String getCurrentRequestId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        String requestId = request.getHeader(Constants.HeaderName.HEADER_REQUEST_ID);
        if (StringUtils.hasLength(requestId)) {
            return requestId;
        }

        requestId = MDC.get(Constants.MdcName.MDC_REQUEST_ID);
        if (StringUtils.hasLength(requestId)) {
            return requestId;
        }
        return requestId;
    }

    public static String getCurrentTenantId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        String requestId = request.getHeader(Constants.HeaderName.HEADER_TENANT_ID);
        if (StringUtils.hasLength(requestId)) {
            return requestId;
        }

        requestId = MDC.get(Constants.MdcName.MDC_TENANT_ID);
        if (StringUtils.hasLength(requestId)) {
            return requestId;
        }
        return "DEFAULT";
    }

    public static String getCurrentUsername() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        return request.getUserPrincipal().getName();
    }
}