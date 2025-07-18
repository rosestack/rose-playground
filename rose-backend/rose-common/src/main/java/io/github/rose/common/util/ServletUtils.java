package io.github.rose.common.util;

import io.github.rose.core.util.NetUtils;
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
 * Servlet工具类
 * 提供HTTP请求/响应处理、参数获取、客户端IP获取等功能
 *
 * @author zhijun.chen
 * @since 0.0.1
 */
@Slf4j
public abstract class ServletUtils {

    private ServletUtils() {
        // 工具类，禁止实例化
    }

    // ==================== 常量定义 ====================

    /**
     * 默认字符编码
     */
    public static final String DEFAULT_CHARSET = StandardCharsets.UTF_8.name();

    /**
     * 常见的客户端IP请求头
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
     * 常见的HTTP方法
     */
    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String HTTP_METHOD_PUT = "PUT";
    private static final String HTTP_METHOD_DELETE = "DELETE";
    private static final String HTTP_METHOD_PATCH = "PATCH";
    private static final String HTTP_METHOD_HEAD = "HEAD";
    private static final String HTTP_METHOD_OPTIONS = "OPTIONS";

    /**
     * 常见的文件扩展名
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
     * Ajax请求标识
     */
    private static final String AJAX_HEADER = "X-Requested-With";
    private static final String AJAX_VALUE = "XMLHttpRequest";
    private static final String AJAX_PARAM = "__ajax";

    /**
     * 缓存
     */
    private static final Map<String, String> URL_DECODE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> URL_ENCODE_CACHE = new ConcurrentHashMap<>();

    // ==================== 请求参数获取方法 ====================

    /**
     * 获取String参数
     *
     * @param name 参数名
     * @return 参数值，如果不存在返回null
     */
    public static String getParameter(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? request.getParameter(name) : null;
    }

    /**
     * 获取String参数（带默认值）
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static String getParameter(String name, String defaultValue) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return defaultValue;
        }
        return StringUtils.defaultIfBlank(request.getParameter(name), defaultValue);
    }

    /**
     * 获取Integer参数
     *
     * @param name 参数名
     * @return 参数值，如果不存在或转换失败返回null
     */
    public static Integer getParameterToInt(String name) {
        return getParameterToInt(name, null);
    }

    /**
     * 获取Integer参数（带默认值）
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值
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
     * 获取Long参数
     *
     * @param name 参数名
     * @return 参数值，如果不存在或转换失败返回null
     */
    public static Long getParameterToLong(String name) {
        return getParameterToLong(name, null);
    }

    /**
     * 获取Long参数（带默认值）
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值
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
     * 获取Boolean参数
     *
     * @param name 参数名
     * @return 参数值，如果不存在返回null
     */
    public static Boolean getParameterToBool(String name) {
        return getParameterToBool(name, null);
    }

    /**
     * 获取Boolean参数（带默认值）
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static Boolean getParameterToBool(String name, Boolean defaultValue) {
        String value = getParameter(name);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return Boolean.valueOf(value);
    }

    /**
     * 获取Double参数
     *
     * @param name 参数名
     * @return 参数值，如果不存在或转换失败返回null
     */
    public static Double getParameterToDouble(String name) {
        return getParameterToDouble(name, null);
    }

    /**
     * 获取Double参数（带默认值）
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值
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
     * 获取Float参数
     *
     * @param name 参数名
     * @return 参数值，如果不存在或转换失败返回null
     */
    public static Float getParameterToFloat(String name) {
        return getParameterToFloat(name, null);
    }

    /**
     * 获取Float参数（带默认值）
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值
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
     * 获取BigDecimal参数
     *
     * @param name 参数名
     * @return 参数值，如果不存在或转换失败返回null
     */
    public static java.math.BigDecimal getParameterToBigDecimal(String name) {
        return getParameterToBigDecimal(name, null);
    }

    /**
     * 获取BigDecimal参数（带默认值）
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值
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
     * 获得所有请求参数（数组形式）
     *
     * @param request 请求对象
     * @return 参数Map（不可修改）
     */
    public static Map<String, String[]> getParams(ServletRequest request) {
        if (request == null) {
            return Collections.emptyMap();
        }
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    /**
     * 获得所有请求参数（数组形式）
     *
     * @return 参数Map（不可修改）
     */
    public static Map<String, String[]> getParams() {
        HttpServletRequest request = getRequest();
        return getParams(request);
    }

    /**
     * 获得所有请求参数（字符串形式，多值用逗号分隔）
     *
     * @param request 请求对象
     * @return 参数Map
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
     * 获得所有请求参数（字符串形式，多值用逗号分隔）
     *
     * @return 参数Map
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

    // ==================== 请求头相关方法 ====================

    /**
     * 获取请求头
     *
     * @param name 请求头名称
     * @return 请求头值
     */
    public static String getHeader(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? request.getHeader(name) : null;
    }

    /**
     * 获取请求头（带默认值）
     *
     * @param name         请求头名称
     * @param defaultValue 默认值
     * @return 请求头值
     */
    public static String getHeader(String name, String defaultValue) {
        String value = getHeader(name);
        return StringUtils.defaultIfBlank(value, defaultValue);
    }

    /**
     * 获取所有请求头
     *
     * @return 请求头Map
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

    // ==================== 请求/响应/会话获取方法 ====================

    /**
     * 获取HttpServletRequest
     *
     * @return HttpServletRequest对象
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return requestAttributes.getRequest();
    }

    /**
     * 获取HttpServletResponse
     *
     * @return HttpServletResponse对象
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return requestAttributes.getResponse();
    }

    /**
     * 获取HttpSession
     *
     * @return HttpSession对象
     */
    public static HttpSession getSession() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getSession() : null;
    }

    /**
     * 获取HttpSession（可选择是否创建）
     *
     * @param create 是否创建新会话
     * @return HttpSession对象
     */
    public static HttpSession getSession(boolean create) {
        HttpServletRequest request = getRequest();
        return request != null ? request.getSession(create) : null;
    }

    /**
     * 获取ServletRequestAttributes
     *
     * @return ServletRequestAttributes对象
     */
    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes ? (ServletRequestAttributes) attributes : null;
    }

    /**
     * 将字符串渲染到客户端
     *
     * @param response 响应对象
     * @param string   待渲染的字符串
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
     * 是否是Ajax异步请求
     *
     * @param request 请求对象
     * @return 是否是Ajax请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        // 检查Accept头
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }

        // 检查X-Requested-With头
        String xRequestedWith = request.getHeader(AJAX_HEADER);
        if (AJAX_VALUE.equals(xRequestedWith)) {
            return true;
        }

        // 检查URI后缀
        String uri = request.getRequestURI();
        if (StringUtils.endsWithIgnoreCase(uri, ".json") || StringUtils.endsWithIgnoreCase(uri, ".xml")) {
            return true;
        }

        // 检查参数
        String ajax = request.getParameter(AJAX_PARAM);
        return StringUtils.equalsAnyIgnoreCase(ajax, "json", "xml");
    }

    /**
     * 是否是Ajax异步请求（使用当前请求）
     *
     * @return 是否是Ajax请求
     */
    public static boolean isAjaxRequest() {
        HttpServletRequest request = getRequest();
        return isAjaxRequest(request);
    }

    /**
     * 是否是GET请求
     *
     * @param request 请求对象
     * @return 是否是GET请求
     */
    public static boolean isGetRequest(HttpServletRequest request) {
        return request != null && HTTP_METHOD_GET.equalsIgnoreCase(request.getMethod());
    }

    /**
     * 是否是POST请求
     *
     * @param request 请求对象
     * @return 是否是POST请求
     */
    public static boolean isPostRequest(HttpServletRequest request) {
        return request != null && HTTP_METHOD_POST.equalsIgnoreCase(request.getMethod());
    }

    /**
     * 是否是PUT请求
     *
     * @param request 请求对象
     * @return 是否是PUT请求
     */
    public static boolean isPutRequest(HttpServletRequest request) {
        return request != null && HTTP_METHOD_PUT.equalsIgnoreCase(request.getMethod());
    }

    /**
     * 是否是DELETE请求
     *
     * @param request 请求对象
     * @return 是否是DELETE请求
     */
    public static boolean isDeleteRequest(HttpServletRequest request) {
        return request != null && HTTP_METHOD_DELETE.equalsIgnoreCase(request.getMethod());
    }

    /**
     * 是否是PATCH请求
     *
     * @param request 请求对象
     * @return 是否是PATCH请求
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
     * 获取客户端真实IP地址
     *
     * @param request          请求对象
     * @param otherHeaderNames 额外的IP请求头名称
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request, String... otherHeaderNames) {
        if (request == null) {
            return null;
        }

        Set<String> headerNames = new LinkedHashSet<>(DEFAULT_IP_HEADERS);
        headerNames.addAll(Arrays.asList(otherHeaderNames));

        String ip;
        for (String header : headerNames) {
            ip = request.getHeader(header);
            if (!NetUtils.isUnknown(ip)) {
                return NetUtils.getReverseProxyIp(ip);
            }
        }

        ip = request.getRemoteAddr();
        return NetUtils.getReverseProxyIp(ip);
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

    // ==================== 工具方法 ====================

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
     * @param charset   字符编码
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

    // ==================== 调试和日志方法 ====================

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