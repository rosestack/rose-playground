package io.github.rose.common.util;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
 * Comprehensive servlet utility class providing HTTP request/response processing capabilities.
 * <p>
 * This utility class serves as a centralized toolkit for common servlet operations in web applications.
 * It provides a wide range of functionality for handling HTTP requests, responses, parameters, headers,
 * cookies, and client information extraction. The class is designed to work seamlessly with Spring's
 * RequestContextHolder to provide convenient access to current request context.
 *
 * <h3>Core Functionality Areas:</h3>
 * <ul>
 *   <li><strong>Parameter Extraction:</strong> Type-safe parameter retrieval with default values</li>
 *   <li><strong>Header Management:</strong> Request header access and response header manipulation</li>
 *   <li><strong>Request Analysis:</strong> HTTP method detection, content type analysis, Ajax detection</li>
 *   <li><strong>Client Information:</strong> IP address extraction, User-Agent parsing, protocol detection</li>
 *   <li><strong>Response Rendering:</strong> JSON, XML, HTML content rendering with proper content types</li>
 *   <li><strong>Cookie Operations:</strong> Cookie creation, retrieval, and management</li>
 *   <li><strong>URL Processing:</strong> URL encoding/decoding with caching for performance</li>
 *   <li><strong>Security Features:</strong> HTTPS detection, real IP extraction through proxies</li>
 * </ul>
 *
 * <h3>Design Principles:</h3>
 * <ul>
 *   <li><strong>Thread Safety:</strong> All methods are thread-safe and stateless</li>
 *   <li><strong>Null Safety:</strong> Comprehensive null checking with graceful degradation</li>
 *   <li><strong>Performance:</strong> Caching mechanisms for expensive operations like URL encoding</li>
 *   <li><strong>Flexibility:</strong> Multiple overloaded methods with sensible defaults</li>
 *   <li><strong>Spring Integration:</strong> Seamless integration with Spring's request context</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Parameter extraction with type conversion
 * String name = ServletUtils.getParameter("name", "anonymous");
 * Integer age = ServletUtils.getParameterToInt("age", 18);
 *
 * // Client information
 * String clientIp = ServletUtils.getClientIp();
 * String userAgent = ServletUtils.getUserAgent();
 *
 * // Request analysis
 * boolean isAjax = ServletUtils.isAjaxRequest();
 * boolean isJson = ServletUtils.isJsonRequest();
 *
 * // Response rendering
 * ServletUtils.renderJson(response, "{\"status\":\"success\"}");
 * }</pre>
 *
 * <h3>Thread Safety and Performance:</h3>
 * This class is fully thread-safe and designed for high-concurrency web applications.
 * URL encoding/decoding operations are cached using ConcurrentHashMap for improved performance.
 * All methods handle null inputs gracefully and provide meaningful defaults.
 *
 * @author zhijun.chen
 * @see HttpServletRequest
 * @see HttpServletResponse
 * @see RequestContextHolder
 * @since 0.0.1
 */
@Slf4j
public abstract class ServletUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * <p>
     * This class is designed to be used as a static utility and should not be instantiated.
     * All methods are static and operate on the current request context or provided parameters.
     */
    private ServletUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Default character encoding used throughout the application.
     * <p>
     * This constant defines the standard UTF-8 encoding that should be used
     * for all text processing operations to ensure proper internationalization
     * support and character handling.
     */
    public static final String DEFAULT_CHARSET = StandardCharsets.UTF_8.name();

    /**
     * Common HTTP headers used for client IP address detection in proxy environments.
     * <p>
     * This list contains the most commonly used headers by load balancers, reverse proxies,
     * and CDNs to forward the original client IP address. The headers are checked in order
     * of preference, with X-Forwarded-For being the most standard.
     *
     * <p>Header descriptions:
     * <ul>
     *   <li><strong>X-Forwarded-For:</strong> Standard header for forwarded client IP</li>
     *   <li><strong>Proxy-Client-IP:</strong> Used by some proxy servers</li>
     *   <li><strong>WL-Proxy-Client-IP:</strong> WebLogic proxy header</li>
     *   <li><strong>HTTP_CLIENT_IP:</strong> Alternative client IP header</li>
     *   <li><strong>HTTP_X_FORWARDED_FOR:</strong> Alternative forwarded header</li>
     *   <li><strong>X-Real-IP:</strong> Nginx real IP header</li>
     * </ul>
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
     * Ajax request identification constants.
     * <p>
     * These constants are used to detect Ajax/XHR requests through various mechanisms:
     * - Standard X-Requested-With header
     * - Custom Ajax parameter
     */
    private static final String AJAX_HEADER = "X-Requested-With";
    private static final String AJAX_VALUE = "XMLHttpRequest";
    private static final String AJAX_PARAM = "__ajax";

    /**
     * Performance optimization caches for URL encoding and decoding operations.
     * <p>
     * These concurrent hash maps cache the results of URL encoding and decoding operations
     * to improve performance for frequently processed URLs. The caches are thread-safe
     * and help reduce the overhead of repeated encoding/decoding operations.
     *
     * <p><strong>Cache Management:</strong>
     * - Automatic population on first access
     * - Thread-safe concurrent access
     * - Manual clearing available via {@link #clearCache()}
     * - No automatic expiration (manual management required)
     */
    private static final Map<String, String> URL_DECODE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> URL_ENCODE_CACHE = new ConcurrentHashMap<>();

    // ==================== Request Parameter Extraction Methods ====================

    /**
     * Retrieves a string parameter from the current HTTP request.
     * <p>
     * This method provides a convenient way to extract string parameters from the current
     * request context without explicitly passing the HttpServletRequest object. It uses
     * Spring's RequestContextHolder to access the current request.
     *
     * <p><strong>Behavior:</strong>
     * <ul>
     *   <li>Returns the parameter value if found</li>
     *   <li>Returns null if parameter doesn't exist</li>
     *   <li>Returns null if no current request context is available</li>
     *   <li>Handles multiple values by returning the first one</li>
     * </ul>
     *
     * @param name The name of the parameter to retrieve. Must not be null.
     * @return The parameter value as a string, or null if not found or no request context
     * @see #getParameter(String, String)
     * @see HttpServletRequest#getParameter(String)
     */
    public static String getParameter(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? request.getParameter(name) : null;
    }

    /**
     * Retrieves a string parameter from the current HTTP request with a default value.
     * <p>
     * This method extends the basic parameter retrieval by providing a fallback mechanism
     * when the parameter is not present or is blank. It's particularly useful for optional
     * parameters that should have sensible defaults.
     *
     * <p><strong>Default Value Logic:</strong>
     * <ul>
     *   <li>Returns parameter value if present and not blank</li>
     *   <li>Returns defaultValue if parameter is null, empty, or whitespace-only</li>
     *   <li>Returns defaultValue if no current request context is available</li>
     * </ul>
     *
     * @param name         The name of the parameter to retrieve. Must not be null.
     * @param defaultValue The default value to return if parameter is not found or blank.
     *                     Can be null.
     * @return The parameter value or the default value if parameter is blank or missing
     * @see #getParameter(String)
     * @see StringUtils#defaultIfBlank(CharSequence, CharSequence)
     */
    public static String getParameter(String name, String defaultValue) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return defaultValue;
        }
        return StringUtils.defaultIfBlank(request.getParameter(name), defaultValue);
    }

    /**
     * Retrieves all request parameters as string arrays from the specified request.
     *
     * @param request The servlet request to extract parameters from
     * @return Unmodifiable map of parameter names to string arrays, empty map if request is null
     */
    public static Map<String, String[]> getParams(ServletRequest request) {
        if (request == null) {
            return Collections.emptyMap();
        }
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    /**
     * Retrieves all request parameters as string arrays from the current request.
     *
     * @return Unmodifiable map of parameter names to string arrays
     */
    public static Map<String, String[]> getParams() {
        HttpServletRequest request = getRequest();
        return getParams(request);
    }

    /**
     * Retrieves all request parameters as strings with multiple values joined by commas.
     *
     * @param request The servlet request to extract parameters from
     * @return Map of parameter names to comma-separated string values
     */
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

    /**
     * Retrieves all request parameters as strings with multiple values joined by commas.
     *
     * @return Map of parameter names to comma-separated string values
     */
    public static Map<String, String> getParamMap() {
        HttpServletRequest request = getRequest();
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
        HttpServletRequest request = getRequest();
        return getParamListMap(request);
    }

    // ==================== Request Header Methods ====================

    /**
     * Retrieves a request header value by name.
     *
     * @param name The header name to retrieve
     * @return The header value, or null if not found or no request context
     */
    public static String getHeader(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? request.getHeader(name) : null;
    }

    /**
     * Retrieves a request header value by name with a default value.
     *
     * @param name         The header name to retrieve
     * @param defaultValue The default value to return if header is not found or blank
     * @return The header value or default value if not found
     */
    public static String getHeader(String name, String defaultValue) {
        String value = getHeader(name);
        return StringUtils.defaultIfBlank(value, defaultValue);
    }

    /**
     * Retrieves all request headers as a map.
     *
     * @return Map of header names to header values, empty map if no request context
     */
    public static Map<String, String> getHeaders() {
        HttpServletRequest request = getRequest();
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

    // ==================== Request/Response/Session Access Methods ====================

    /**
     * Retrieves the current HttpServletRequest from the request context.
     *
     * @return The current HttpServletRequest, or null if no request context is available
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return requestAttributes.getRequest();
    }

    /**
     * Retrieves the current HttpServletResponse from the request context.
     *
     * @return The current HttpServletResponse, or null if no request context is available
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return requestAttributes.getResponse();
    }

    /**
     * Retrieves the current HttpSession from the request context.
     *
     * @return The current HttpSession, or null if no request context is available
     */
    public static HttpSession getSession() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getSession() : null;
    }

    /**
     * Retrieves the current HttpSession with option to create if not exists.
     *
     * @param create Whether to create a new session if one doesn't exist
     * @return The current HttpSession, or null if no request context is available
     */
    public static HttpSession getSession(boolean create) {
        HttpServletRequest request = getRequest();
        return request != null ? request.getSession(create) : null;
    }

    /**
     * Retrieves the current ServletRequestAttributes from the request context.
     *
     * @return The current ServletRequestAttributes, or null if not available or not a servlet context
     */
    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes ? (ServletRequestAttributes) attributes : null;
    }

    /**
     * Renders a string to the client with JSON content type.
     *
     * @param response The HTTP response object
     * @param string   The string content to render
     */
    public static void renderString(HttpServletResponse response, String string) {
        renderString(response, string, MediaType.APPLICATION_JSON_VALUE);
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
     * 将XML字符串渲染到客户端
     *
     * @param response 响应对象
     * @param xml      XML字符串
     */
    public static void renderXml(HttpServletResponse response, String xml) {
        renderString(response, xml, MediaType.APPLICATION_XML_VALUE);
    }

    /**
     * 将HTML字符串渲染到客户端
     *
     * @param response 响应对象
     * @param html     HTML字符串
     */
    public static void renderHtml(HttpServletResponse response, String html) {
        renderString(response, html, MediaType.TEXT_HTML_VALUE);
    }

    /**
     * Determines if the request is an Ajax/XHR request.
     *
     * @param request The HTTP request to check
     * @return true if the request is identified as an Ajax request, false otherwise
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }

        String xRequestedWith = request.getHeader(AJAX_HEADER);
        if (AJAX_VALUE.equals(xRequestedWith)) {
            return true;
        }

        String uri = request.getRequestURI();
        if (StringUtils.endsWithIgnoreCase(uri, ".json") || StringUtils.endsWithIgnoreCase(uri, ".xml")) {
            return true;
        }

        String ajax = request.getParameter(AJAX_PARAM);
        return StringUtils.equalsAnyIgnoreCase(ajax, "json", "xml");
    }

    /**
     * Determines if the current request is an Ajax/XHR request.
     *
     * @return true if the current request is identified as an Ajax request, false otherwise
     */
    public static boolean isAjaxRequest() {
        HttpServletRequest request = getRequest();
        return isAjaxRequest(request);
    }

    /**
     * URL编码
     *
     * @param str 待编码的字符串
     * @return 编码后的字符串
     * @throws UnsupportedEncodingException 编码异常
     */
    public static String urlEncode(String str) throws UnsupportedEncodingException {
        if (StringUtils.isBlank(str)) {
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
        if (StringUtils.isBlank(str)) {
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

    /**
     * Extracts the real client IP address from an HTTP request, considering proxy headers.
     * <p>
     * This method implements a comprehensive strategy for determining the actual client IP address
     * in environments with load balancers, reverse proxies, and CDNs. It checks multiple common
     * headers used by different proxy implementations to forward the original client IP.
     *
     * <p><strong>IP Resolution Strategy:</strong>
     * <ol>
     *   <li>Check standard proxy headers (X-Forwarded-For, etc.)</li>
     *   <li>Check additional custom headers if provided</li>
     *   <li>Fall back to request.getRemoteAddr() if no proxy headers found</li>
     *   <li>Handle comma-separated IP lists from proxy chains</li>
     *   <li>Filter out "unknown" values commonly used by proxies</li>
     * </ol>
     *
     * <p><strong>Proxy Chain Handling:</strong>
     * When requests pass through multiple proxies, headers may contain comma-separated
     * IP addresses. This method extracts the first valid (non-"unknown") IP from such lists.
     *
     * <p><strong>Security Considerations:</strong>
     * <ul>
     *   <li>Proxy headers can be spoofed by malicious clients</li>
     *   <li>Only trust proxy headers in controlled environments</li>
     *   <li>Consider additional validation for security-critical operations</li>
     * </ul>
     *
     * @param request          The HTTP request to extract IP from. If null, returns null.
     * @param otherHeaderNames Additional header names to check for IP addresses.
     *                         These are checked after the standard headers.
     * @return The client IP address, or null if request is null or no valid IP found
     * @see #getClientIp()
     * @see #getReverseProxyIp(String)
     */
    public static String getClientIp(HttpServletRequest request, String... otherHeaderNames) {
        if (request == null) {
            return null;
        }

        // Combine default headers with additional ones, maintaining order
        Set<String> headerNames = new LinkedHashSet<>(DEFAULT_IP_HEADERS);
        headerNames.addAll(Arrays.asList(otherHeaderNames));

        // Check each header for IP address
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
                if (!StringUtils.isBlank(trimmedIp) && !"unknown".equalsIgnoreCase(trimmedIp)) {
                    return trimmedIp;
                }
            }
        }
        return ip;
    }

    /**
     * 获取客户端真实IP地址（使用当前请求）
     *
     * @return 客户端IP地址
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        return getClientIp(request);
    }

    /**
     * 获取Cookie值
     *
     * @param name Cookie名称
     * @return Cookie值
     */
    public static String getCookieValue(String name) {
        HttpServletRequest request = getRequest();
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
        HttpServletRequest request = getRequest();
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
     * 获取请求的完整URL
     *
     * @param request 请求对象
     * @return 完整URL
     */
    public static String getFullUrl(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String queryString = request.getQueryString();
        String requestURI = request.getRequestURI();

        if (StringUtils.isNotBlank(queryString)) {
            return requestURI + "?" + queryString;
        }
        return requestURI;
    }

    /**
     * 获取请求的完整URL（使用当前请求）
     *
     * @return 完整URL
     */
    public static String getFullUrl() {
        HttpServletRequest request = getRequest();
        return getFullUrl(request);
    }

    /**
     * 获取请求的User-Agent
     *
     * @param request 请求对象
     * @return User-Agent
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader(HttpHeaders.USER_AGENT) : null;
    }

    /**
     * 获取请求的User-Agent（使用当前请求）
     *
     * @return User-Agent
     */
    public static String getUserAgent() {
        HttpServletRequest request = getRequest();
        return getUserAgent(request);
    }
}