package io.github.rosestack.core.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet 工具类
 * <p>
 * 提供 HTTP 请求和响应的常用操作
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
public final class ServletUtils {

    private ServletUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 获取当前请求对象
     *
     * @return 当前请求对象
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取当前响应对象
     *
     * @return 当前响应对象
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getResponse() : null;
    }

    /**
     * 获取请求头信息
     *
     * @param name 请求头名称
     * @return 请求头值
     */
    public static String getHeader(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? request.getHeader(name) : null;
    }

    /**
     * 获取请求参数
     *
     * @param name 参数名称
     * @return 参数值
     */
    public static String getParameter(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? request.getParameter(name) : null;
    }

    /**
     * 获取请求参数（带默认值）
     *
     * @param name 参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static String getParameter(String name, String defaultValue) {
        String value = getParameter(name);
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    /**
     * 获取请求参数（整数类型）
     *
     * @param name 参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static Integer getParameterInt(String name, Integer defaultValue) {
        String value = getParameter(name);
        if (StringUtils.hasText(value)) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 获取请求参数（长整型）
     *
     * @param name 参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static Long getParameterLong(String name, Long defaultValue) {
        String value = getParameter(name);
        if (StringUtils.hasText(value)) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 获取请求参数（布尔类型）
     *
     * @param name 参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static Boolean getParameterBoolean(String name, Boolean defaultValue) {
        String value = getParameter(name);
        if (StringUtils.hasText(value)) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    /**
     * 获取所有请求参数
     *
     * @return 参数映射
     */
    public static Map<String, String> getAllParameters() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return new HashMap<>();
        }

        Map<String, String> params = new HashMap<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            params.put(name, request.getParameter(name));
        }
        return params;
    }

    /**
     * 获取请求URL
     *
     * @return 请求URL
     */
    public static String getRequestUrl() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getRequestURL().toString() : null;
    }

    /**
     * 获取请求URI
     *
     * @return 请求URI
     */
    public static String getRequestUri() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getRequestURI() : null;
    }

    /**
     * 获取请求方法
     *
     * @return 请求方法
     */
    public static String getRequestMethod() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getMethod() : null;
    }

    /**
     * 获取客户端IP地址
     *
     * @return 客户端IP地址
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }

        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_CLIENT_IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    /**
     * 获取服务器IP地址
     *
     * @return 服务器IP地址
     */
    public static String getServerIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    /**
     * 获取用户代理
     *
     * @return 用户代理
     */
    public static String getUserAgent() {
        return getHeader("User-Agent");
    }

    /**
     * 获取请求ID
     *
     * @return 请求ID
     */
    public static String getRequestId() {
        return getHeader("X-Request-ID");
    }

    /**
     * 获取请求来源
     *
     * @return 请求来源
     */
    public static String getReferer() {
        return getHeader("Referer");
    }

    /**
     * 获取请求来源域名
     *
     * @return 请求来源域名
     */
    public static String getRefererDomain() {
        String referer = getReferer();
        if (StringUtils.hasText(referer)) {
            try {
                java.net.URL url = new java.net.URL(referer);
                return url.getHost();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 判断是否为AJAX请求
     *
     * @return 是否为AJAX请求
     */
    public static boolean isAjaxRequest() {
        String xRequestedWith = getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(xRequestedWith);
    }

    /**
     * 判断是否为移动设备
     *
     * @return 是否为移动设备
     */
    public static boolean isMobileDevice() {
        String userAgent = getUserAgent();
        if (!StringUtils.hasText(userAgent)) {
            return false;
        }

        String lowerUserAgent = userAgent.toLowerCase();
        return lowerUserAgent.contains("mobile") ||
                lowerUserAgent.contains("android") ||
                lowerUserAgent.contains("iphone") ||
                lowerUserAgent.contains("ipad") ||
                lowerUserAgent.contains("windows phone");
    }

    /**
     * 写入响应内容
     *
     * @param response 响应对象
     * @param content 响应内容
     * @param contentType 内容类型
     * @throws IOException IO异常
     */
    public static void writeResponse(HttpServletResponse response, String content, String contentType) throws IOException {
        response.setContentType(contentType);
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(content);
            writer.flush();
        }
    }

    /**
     * 写入JSON响应
     *
     * @param response 响应对象
     * @param json JSON字符串
     * @throws IOException IO异常
     */
    public static void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
        writeResponse(response, json, "application/json;charset=UTF-8");
    }

    /**
     * 写入HTML响应
     *
     * @param response 响应对象
     * @param html HTML字符串
     * @throws IOException IO异常
     */
    public static void writeHtmlResponse(HttpServletResponse response, String html) throws IOException {
        writeResponse(response, html, "text/html;charset=UTF-8");
    }

    /**
     * 写入文本响应
     *
     * @param response 响应对象
     * @param text 文本内容
     * @throws IOException IO异常
     */
    public static void writeTextResponse(HttpServletResponse response, String text) throws IOException {
        writeResponse(response, text, "text/plain;charset=UTF-8");
    }
} 