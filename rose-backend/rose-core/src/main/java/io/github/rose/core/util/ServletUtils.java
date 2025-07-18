package io.github.rose.core.util;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
import java.util.stream.Collectors;

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
    private static final String[] DEFAULT_IP_HEADERS = {
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_CLIENT_IP",
        "HTTP_X_FORWARDED_FOR"
    };

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
     * @return 参数值
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
     * @return 参数值
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
     * @return 参数值
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
     * @return 参数值
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
     * 获取String参数
     */
    public static String getHeader(String name) {
        return getRequest().getHeader(name);
    }

    /**
     * 获取request
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return requestAttributes.getRequest();
    }

    /**
     * 获取response
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return requestAttributes.getResponse();
    }

    /**
     * 获取session
     */
    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    /**
     * 将字符串渲染到客户端
     *
     * @param response 渲染对象
     * @param string   待渲染的字符串
     */
    public static void renderString(HttpServletResponse response, String string) {
        try {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.getWriter().print(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否是Ajax异步请求
     *
     * @param request
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String accept = request.getHeader("accept");
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }

        String xRequestedWith = request.getHeader("X-Requested-With");
        if (xRequestedWith != null && xRequestedWith.contains("XMLHttpRequest")) {
            return true;
        }

        String uri = request.getRequestURI();
        if (StringUtils.equalsAnyIgnoreCase(uri, ".json", ".xml")) {
            return true;
        }

        String ajax = request.getParameter("__ajax");
        return StringUtils.equalsAnyIgnoreCase(ajax, "json", "xml");
    }

    /**
     * 内容编码
     *
     * @param str 内容
     * @return 编码后的内容
     */
    public static String urlEncode(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, String.valueOf(StandardCharsets.UTF_8));
    }

    /**
     * 内容解码
     *
     * @param str 内容
     * @return 解码后的内容
     */
    public static String urlDecode(String str) throws UnsupportedEncodingException {
        return URLDecoder.decode(str, String.valueOf(StandardCharsets.UTF_8));
    }

    public static String getClientIP(HttpServletRequest request, String... otherHeaderNames) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        if (ObjectUtils.isNotEmpty(otherHeaderNames)) {
            headers = Arrays.copyOf(headers, headers.length + otherHeaderNames.length);
            System.arraycopy(otherHeaderNames, 0, headers, headers.length - otherHeaderNames.length, otherHeaderNames.length);
        }

        return getClientIPByHeader(request, headers);
    }

    public static String getClientIPByHeader(HttpServletRequest request, String... headerNames) {
        String ip;
        for (String header : headerNames) {
            ip = request.getHeader(header);
            if (!NetUtils.isUnknown(ip)) {
                return NetUtils.getMultistageReverseProxyIp(ip);
            }
        }

        ip = request.getRemoteAddr();
        return NetUtils.getMultistageReverseProxyIp(ip);
    }

}