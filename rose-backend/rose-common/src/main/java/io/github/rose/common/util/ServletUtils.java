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


@Slf4j
public abstract class ServletUtils {

    private ServletUtils() {
    }

    // ==================== Constants and Configuration ====================

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
     * HTTP method constants for request type detection.
     * <p>
     * These constants define the standard HTTP methods supported by the utility class.
     * They are used for method comparison and request type analysis.
     */
    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String HTTP_METHOD_PUT = "PUT";
    private static final String HTTP_METHOD_DELETE = "DELETE";
    private static final String HTTP_METHOD_PATCH = "PATCH";
    private static final String HTTP_METHOD_HEAD = "HEAD";
    private static final String HTTP_METHOD_OPTIONS = "OPTIONS";

    /**
     * File extension constants for content type detection and static resource identification.
     * <p>
     * These constants are used to identify different types of web resources and
     * determine appropriate content types for responses.
     */
    private static final String EXT_JSON = ".json";
    private static final String EXT_XML = ".xml";
    private static final String EXT_HTML = ".html";
    private static final String EXT_HTM = ".htm";
    private static final String EXT_JS = ".js";
    private static final String EXT_CSS = ".css";
    private static final String EXT_PNG = ".png";
    private static final String EXT_JPG = ".jpg";
    private static final String EXT_JPEG = ".jpeg";
    private static final String EXT_GIF = ".gif";
    private static final String EXT_PDF = ".pdf";

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
     * Retrieves an integer parameter from the current HTTP request.
     * <p>
     * This method provides type-safe integer parameter extraction with automatic
     * string-to-integer conversion. It handles conversion errors gracefully by
     * returning null instead of throwing exceptions.
     *
     * <p><strong>Conversion Behavior:</strong>
     * <ul>
     *   <li>Returns Integer object if parameter exists and is valid number</li>
     *   <li>Returns null if parameter doesn't exist</li>
     *   <li>Returns null if parameter cannot be parsed as integer</li>
     *   <li>Logs warning for parsing failures</li>
     * </ul>
     *
     * @param name The name of the parameter to retrieve and convert. Must not be null.
     * @return The parameter value as Integer, or null if not found or conversion fails
     * @see #getParameterToInt(String, Integer)
     * @see Integer#valueOf(String)
     */
    public static Integer getParameterToInt(String name) {
        return getParameterToInt(name, null);
    }

    /**
     * Retrieves an integer parameter from the current HTTP request with a default value.
     * <p>
     * This method combines type-safe integer conversion with fallback value support.
     * It's ideal for numeric parameters that should have reasonable defaults when
     * not provided or when conversion fails.
     *
     * <p><strong>Conversion and Fallback Logic:</strong>
     * <ol>
     *   <li>Retrieve string parameter value</li>
     *   <li>Return defaultValue if parameter is blank or missing</li>
     *   <li>Attempt integer conversion</li>
     *   <li>Return converted value on success</li>
     *   <li>Log warning and return defaultValue on conversion failure</li>
     * </ol>
     *
     * <p><strong>Error Handling:</strong>
     * Conversion failures are logged at WARN level with parameter name and value
     * for debugging purposes, but do not throw exceptions.
     *
     * @param name         The name of the parameter to retrieve and convert. Must not be null.
     * @param defaultValue The default value to return if parameter is missing or
     *                     conversion fails. Can be null.
     * @return The parameter value as Integer, or defaultValue if conversion fails
     * @see #getParameterToInt(String)
     * @see #getParameter(String)
     */
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        String value = getParameter(name);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse parameter '{}' to Integer: {}", name, value);
            return defaultValue;
        }
    }

    /**
     * Retrieves a long parameter from the current HTTP request.
     *
     * @param name The name of the parameter to retrieve and convert
     * @return The parameter value as Long, or null if not found or conversion fails
     */
    public static Long getParameterToLong(String name) {
        return getParameterToLong(name, null);
    }

    /**
     * Retrieves a long parameter from the current HTTP request with a default value.
     *
     * @param name         The name of the parameter to retrieve and convert
     * @param defaultValue The default value to return if parameter is missing or conversion fails
     * @return The parameter value as Long, or defaultValue if conversion fails
     */
    public static Long getParameterToLong(String name, Long defaultValue) {
        String value = getParameter(name);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse parameter '{}' to Long: {}", name, value);
            return defaultValue;
        }
    }

    /**
     * Retrieves a boolean parameter from the current HTTP request.
     *
     * @param name The name of the parameter to retrieve and convert
     * @return The parameter value as Boolean, or null if not found
     */
    public static Boolean getParameterToBool(String name) {
        return getParameterToBool(name, null);
    }

    /**
     * Retrieves a boolean parameter from the current HTTP request with a default value.
     *
     * @param name         The name of the parameter to retrieve and convert
     * @param defaultValue The default value to return if parameter is missing or blank
     * @return The parameter value as Boolean, or defaultValue if not found
     */
    public static Boolean getParameterToBool(String name, Boolean defaultValue) {
        String value = getParameter(name);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return Boolean.valueOf(value);
    }

    /**
     * Retrieves a double parameter from the current HTTP request.
     *
     * @param name The name of the parameter to retrieve and convert
     * @return The parameter value as Double, or null if not found or conversion fails
     */
    public static Double getParameterToDouble(String name) {
        return getParameterToDouble(name, null);
    }

    /**
     * Retrieves a double parameter from the current HTTP request with a default value.
     *
     * @param name         The name of the parameter to retrieve and convert
     * @param defaultValue The default value to return if parameter is missing or conversion fails
     * @return The parameter value as Double, or defaultValue if conversion fails
     */
    public static Double getParameterToDouble(String name, Double defaultValue) {
        String value = getParameter(name);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse parameter '{}' to Double: {}", name, value);
            return defaultValue;
        }
    }

    /**
     * Retrieves a float parameter from the current HTTP request.
     *
     * @param name The name of the parameter to retrieve and convert
     * @return The parameter value as Float, or null if not found or conversion fails
     */
    public static Float getParameterToFloat(String name) {
        return getParameterToFloat(name, null);
    }

    /**
     * Retrieves a float parameter from the current HTTP request with a default value.
     *
     * @param name         The name of the parameter to retrieve and convert
     * @param defaultValue The default value to return if parameter is missing or conversion fails
     * @return The parameter value as Float, or defaultValue if conversion fails
     */
    public static Float getParameterToFloat(String name, Float defaultValue) {
        String value = getParameter(name);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Float.valueOf(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse parameter '{}' to Float: {}", name, value);
            return defaultValue;
        }
    }

    /**
     * Retrieves a BigDecimal parameter from the current HTTP request.
     *
     * @param name The name of the parameter to retrieve and convert
     * @return The parameter value as BigDecimal, or null if not found or conversion fails
     */
    public static java.math.BigDecimal getParameterToBigDecimal(String name) {
        return getParameterToBigDecimal(name, null);
    }

    /**
     * Retrieves a BigDecimal parameter from the current HTTP request with a default value.
     *
     * @param name         The name of the parameter to retrieve and convert
     * @param defaultValue The default value to return if parameter is missing or conversion fails
     * @return The parameter value as BigDecimal, or defaultValue if conversion fails
     */
    public static java.math.BigDecimal getParameterToBigDecimal(String name, java.math.BigDecimal defaultValue) {
        String value = getParameter(name);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return new java.math.BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse parameter '{}' to BigDecimal: {}", name, value);
            return defaultValue;
        }
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
     * @param name The header name to retrieve
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
     * @param string The string content to render
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
     * Determines if the request is a GET request.
     *
     * @param request The HTTP request to check
     * @return true if the request method is GET, false otherwise
     */
    public static boolean isGetRequest(HttpServletRequest request) {
        return request != null && HTTP_METHOD_GET.equalsIgnoreCase(request.getMethod());
    }

    /**
     * Determines if the request is a POST request.
     *
     * @param request The HTTP request to check
     * @return true if the request method is POST, false otherwise
     */
    public static boolean isPostRequest(HttpServletRequest request) {
        return request != null && HTTP_METHOD_POST.equalsIgnoreCase(request.getMethod());
    }

    /**
     * Determines if the request is a PUT request.
     *
     * @param request The HTTP request to check
     * @return true if the request method is PUT, false otherwise
     */
    public static boolean isPutRequest(HttpServletRequest request) {
        return request != null && HTTP_METHOD_PUT.equalsIgnoreCase(request.getMethod());
    }

    /**
     * Determines if the request is a DELETE request.
     *
     * @param request The HTTP request to check
     * @return true if the request method is DELETE, false otherwise
     */
    public static boolean isDeleteRequest(HttpServletRequest request) {
        return request != null && HTTP_METHOD_DELETE.equalsIgnoreCase(request.getMethod());
    }

    /**
     * Determines if the request is a PATCH request.
     *
     * @param request The HTTP request to check
     * @return true if the request method is PATCH, false otherwise
     */
    public static boolean isPatchRequest(HttpServletRequest request) {
        return request != null && HTTP_METHOD_PATCH.equalsIgnoreCase(request.getMethod());
    }

    /**
     * 是否是HEAD请求
     *
     * @param request 请求对象
     * @return 是否是HEAD请求
     */
    public static boolean isHeadRequest(HttpServletRequest request) {
        return request != null && HTTP_METHOD_HEAD.equalsIgnoreCase(request.getMethod());
    }

    /**
     * 是否是OPTIONS请求
     *
     * @param request 请求对象
     * @return 是否是OPTIONS请求
     */
    public static boolean isOptionsRequest(HttpServletRequest request) {
        return request != null && HTTP_METHOD_OPTIONS.equalsIgnoreCase(request.getMethod());
    }

    /**
     * 获取HTTP方法
     *
     * @param request 请求对象
     * @return HTTP方法，如果请求为null返回null
     */
    public static String getHttpMethod(HttpServletRequest request) {
        return request != null ? request.getMethod() : null;
    }

    /**
     * 获取HTTP方法（使用当前请求）
     *
     * @return HTTP方法
     */
    public static String getHttpMethod() {
        HttpServletRequest request = getRequest();
        return getHttpMethod(request);
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

    // ==================== Cookie操作方法 ====================

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

    public static String getReferer(HttpServletRequest request) {
        return request != null ? request.getHeader(HttpHeaders.REFERER) : null;
    }

    public static String getReferer() {
        HttpServletRequest request = getRequest();
        return getReferer(request);
    }

    // ==================== 文件类型检测方法 ====================

    /**
     * 是否是JSON请求
     *
     * @param request 请求对象
     * @return 是否是JSON请求
     */
    public static boolean isJsonRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        // 检查Accept头
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }

        // 检查Content-Type头
        String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }

        // 检查URI后缀
        String uri = request.getRequestURI();
        return StringUtils.endsWithIgnoreCase(uri, EXT_JSON);
    }

    /**
     * 是否是XML请求
     *
     * @param request 请求对象
     * @return 是否是XML请求
     */
    public static boolean isXmlRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        // 检查Accept头
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept != null && accept.contains(MediaType.APPLICATION_XML_VALUE)) {
            return true;
        }

        // 检查Content-Type头
        String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType != null && contentType.contains(MediaType.APPLICATION_XML_VALUE)) {
            return true;
        }

        // 检查URI后缀
        String uri = request.getRequestURI();
        return StringUtils.endsWithIgnoreCase(uri, EXT_XML);
    }

    /**
     * 是否是HTML请求
     *
     * @param request 请求对象
     * @return 是否是HTML请求
     */
    public static boolean isHtmlRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        // 检查Accept头
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept != null && accept.contains(MediaType.TEXT_HTML_VALUE)) {
            return true;
        }

        // 检查URI后缀
        String uri = request.getRequestURI();
        return StringUtils.endsWithIgnoreCase(uri, EXT_HTML) || StringUtils.endsWithIgnoreCase(uri, EXT_HTM);
    }

    /**
     * 是否是静态资源请求
     *
     * @param request 请求对象
     * @return 是否是静态资源请求
     */
    public static boolean isStaticResourceRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        String uri = request.getRequestURI();
        return StringUtils.endsWithIgnoreCase(uri, EXT_JS) ||
                StringUtils.endsWithIgnoreCase(uri, EXT_CSS) ||
                StringUtils.endsWithIgnoreCase(uri, EXT_PNG) ||
                StringUtils.endsWithIgnoreCase(uri, EXT_JPG) ||
                StringUtils.endsWithIgnoreCase(uri, EXT_JPEG) ||
                StringUtils.endsWithIgnoreCase(uri, EXT_GIF) ||
                StringUtils.endsWithIgnoreCase(uri, EXT_PDF);
    }

    // ==================== 请求信息获取方法 ====================

    /**
     * 获取请求的协议
     *
     * @param request 请求对象
     * @return 协议（http或https）
     */
    public static String getProtocol(HttpServletRequest request) {
        return request != null ? request.getScheme() : null;
    }

    /**
     * 获取请求的协议（使用当前请求）
     *
     * @return 协议
     */
    public static String getProtocol() {
        HttpServletRequest request = getRequest();
        return getProtocol(request);
    }

    /**
     * 获取请求的服务器名称
     *
     * @param request 请求对象
     * @return 服务器名称
     */
    public static String getServerName(HttpServletRequest request) {
        return request != null ? request.getServerName() : null;
    }

    /**
     * 获取请求的服务器名称（使用当前请求）
     *
     * @return 服务器名称
     */
    public static String getServerName() {
        HttpServletRequest request = getRequest();
        return getServerName(request);
    }

    /**
     * 获取请求的服务器端口
     *
     * @param request 请求对象
     * @return 服务器端口
     */
    public static int getServerPort(HttpServletRequest request) {
        return request != null ? request.getServerPort() : -1;
    }

    /**
     * 获取请求的服务器端口（使用当前请求）
     *
     * @return 服务器端口
     */
    public static int getServerPort() {
        HttpServletRequest request = getRequest();
        return getServerPort(request);
    }

    /**
     * 获取请求的上下文路径
     *
     * @param request 请求对象
     * @return 上下文路径
     */
    public static String getContextPath(HttpServletRequest request) {
        return request != null ? request.getContextPath() : null;
    }

    /**
     * 获取请求的上下文路径（使用当前请求）
     *
     * @return 上下文路径
     */
    public static String getContextPath() {
        HttpServletRequest request = getRequest();
        return getContextPath(request);
    }

    /**
     * 获取请求的Servlet路径
     *
     * @param request 请求对象
     * @return Servlet路径
     */
    public static String getServletPath(HttpServletRequest request) {
        return request != null ? request.getServletPath() : null;
    }

    /**
     * 获取请求的Servlet路径（使用当前请求）
     *
     * @return Servlet路径
     */
    public static String getServletPath() {
        HttpServletRequest request = getRequest();
        return getServletPath(request);
    }

    /**
     * 获取请求的路径信息
     *
     * @param request 请求对象
     * @return 路径信息
     */
    public static String getPathInfo(HttpServletRequest request) {
        return request != null ? request.getPathInfo() : null;
    }

    /**
     * 获取请求的路径信息（使用当前请求）
     *
     * @return 路径信息
     */
    public static String getPathInfo() {
        HttpServletRequest request = getRequest();
        return getPathInfo(request);
    }

    // ==================== 响应操作方法 ====================

    /**
     * 设置响应状态码
     *
     * @param response 响应对象
     * @param status   状态码
     */
    public static void setStatus(HttpServletResponse response, int status) {
        if (response != null) {
            response.setStatus(status);
        }
    }

    /**
     * 设置响应状态码
     *
     * @param response 响应对象
     * @param status   状态码
     */
    public static void setStatus(HttpServletResponse response, HttpStatus status) {
        if (response != null && status != null) {
            response.setStatus(status.value());
        }
    }

    /**
     * 设置响应头
     *
     * @param response 响应对象
     * @param name     头名称
     * @param value    头值
     */
    public static void setHeader(HttpServletResponse response, String name, String value) {
        if (response != null && StringUtils.isNotBlank(name)) {
            response.setHeader(name, value);
        }
    }

    /**
     * 添加响应头
     *
     * @param response 响应对象
     * @param name     头名称
     * @param value    头值
     */
    public static void addHeader(HttpServletResponse response, String name, String value) {
        if (response != null && StringUtils.isNotBlank(name)) {
            response.addHeader(name, value);
        }
    }

    /**
     * 设置响应内容类型
     *
     * @param response    响应对象
     * @param contentType 内容类型
     */
    public static void setContentType(HttpServletResponse response, String contentType) {
        if (response != null && StringUtils.isNotBlank(contentType)) {
            response.setContentType(contentType);
        }
    }

    /**
     * 设置响应字符编码
     *
     * @param response 响应对象
     * @param charset  字符编码
     */
    public static void setCharacterEncoding(HttpServletResponse response, String charset) {
        if (response != null && StringUtils.isNotBlank(charset)) {
            response.setCharacterEncoding(charset);
        }
    }

    // ==================== 安全相关方法 ====================

    /**
     * 是否是HTTPS请求
     *
     * @param request 请求对象
     * @return 是否是HTTPS请求
     */
    public static boolean isHttpsRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        return "https".equalsIgnoreCase(request.getScheme());
    }

    /**
     * 是否是HTTPS请求（使用当前请求）
     *
     * @return 是否是HTTPS请求
     */
    public static boolean isHttpsRequest() {
        HttpServletRequest request = getRequest();
        return isHttpsRequest(request);
    }

    /**
     * 获取请求的真实IP（考虑代理）
     *
     * @param request 请求对象
     * @return 真实IP地址
     */
    public static String getRealIp(HttpServletRequest request) {
        return getClientIp(request);
    }

    /**
     * 获取请求的真实IP（使用当前请求）
     *
     * @return 真实IP地址
     */
    public static String getRealIp() {
        return getClientIp();
    }

    /**
     * 获取请求的详细信息（用于调试）
     *
     * @param request 请求对象
     * @return 请求详细信息
     */
    public static String getRequestDetails(HttpServletRequest request) {
        if (request == null) {
            return "Request is null";
        }

        StringBuilder details = new StringBuilder();
        details.append("Method: ").append(request.getMethod()).append("\n");
        details.append("URL: ").append(getFullUrl(request)).append("\n");
        details.append("Protocol: ").append(request.getScheme()).append("\n");
        details.append("Server: ").append(request.getServerName()).append(":").append(request.getServerPort()).append("\n");
        details.append("Context Path: ").append(request.getContextPath()).append("\n");
        details.append("Servlet Path: ").append(request.getServletPath()).append("\n");
        details.append("Path Info: ").append(request.getPathInfo()).append("\n");
        details.append("Query String: ").append(request.getQueryString()).append("\n");
        details.append("Remote Address: ").append(request.getRemoteAddr()).append("\n");
        details.append("Remote Host: ").append(request.getRemoteHost()).append("\n");
        details.append("Remote Port: ").append(request.getRemotePort()).append("\n");
        details.append("User Agent: ").append(getUserAgent(request)).append("\n");
        details.append("Referer: ").append(getReferer(request)).append("\n");

        return details.toString();
    }

    /**
     * 获取请求的详细信息（使用当前请求）
     *
     * @return 请求详细信息
     */
    public static String getRequestDetails() {
        HttpServletRequest request = getRequest();
        return getRequestDetails(request);
    }
}