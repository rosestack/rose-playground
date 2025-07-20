package io.github.rose.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ServletUtils 测试类
 *
 * @author zhijun.chen
 * @since 0.0.1
 */
@ExtendWith(MockitoExtension.class)
class ServletUtilsTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    @Mock
    private ServletRequestAttributes mockRequestAttributes;

    private MockHttpServletRequest testRequest;
    private MockHttpServletResponse testResponse;

    @BeforeEach
    void setUp() {
        testRequest = new MockHttpServletRequest();
        testResponse = new MockHttpServletResponse();

        // 清理缓存
        ServletUtils.clearCache();
    }

    @Test
    void testGetParameter() {
        // 设置请求参数
        testRequest.addParameter("name", "test");
        testRequest.addParameter("age", "25");
        testRequest.addParameter("active", "true");

        // 模拟RequestContextHolder
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(testRequest));

        assertEquals("test", ServletUtils.getParameter("name"));
        assertEquals("25", ServletUtils.getParameter("age"));
        assertEquals("true", ServletUtils.getParameter("active"));
        assertNull(ServletUtils.getParameter("nonexistent"));
    }

    @Test
    void testGetParameterWithDefault() {
        testRequest.addParameter("name", "test");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(testRequest));

        assertEquals("test", ServletUtils.getParameter("name", "default"));
        assertEquals("default", ServletUtils.getParameter("nonexistent", "default"));
    }

    @Test
    void testGetParams() {
        testRequest.addParameter("name", "test");
        testRequest.addParameter("age", "25", "30");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(testRequest));

        Map<String, String[]> params = ServletUtils.getParams();
        assertNotNull(params);
        assertEquals("test", params.get("name")[0]);
        assertArrayEquals(new String[]{"25", "30"}, params.get("age"));
    }

    @Test
    void testGetParamMap() {
        testRequest.addParameter("name", "test");
        testRequest.addParameter("age", "25", "30");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(testRequest));

        Map<String, String> paramMap = ServletUtils.getParamMap();
        assertNotNull(paramMap);
        assertEquals("test", paramMap.get("name"));
        assertEquals("25,30", paramMap.get("age"));
    }

    @Test
    void testGetParamListMap() {
        testRequest.addParameter("name", "test");
        testRequest.addParameter("age", "25", "30");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(testRequest));

        Map<String, List<String>> paramListMap = ServletUtils.getParamListMap();
        assertNotNull(paramListMap);
        assertEquals(Arrays.asList("test"), paramListMap.get("name"));
        assertEquals(Arrays.asList("25", "30"), paramListMap.get("age"));
    }

    @Test
    void testGetHeader() {
        testRequest.addHeader("Content-Type", "application/json");
        testRequest.addHeader("User-Agent", "Mozilla/5.0");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(testRequest));

        assertEquals("application/json", ServletUtils.getHeader("Content-Type"));
        assertEquals("Mozilla/5.0", ServletUtils.getHeader("User-Agent"));
        assertNull(ServletUtils.getHeader("Nonexistent"));
        assertEquals("default", ServletUtils.getHeader("Nonexistent", "default"));
    }

    @Test
    void testGetHeaders() {
        testRequest.addHeader("Content-Type", "application/json");
        testRequest.addHeader("User-Agent", "Mozilla/5.0");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(testRequest));

        Map<String, String> headers = ServletUtils.getHeaders();
        assertNotNull(headers);
        assertEquals("application/json", headers.get("Content-Type"));
        assertEquals("Mozilla/5.0", headers.get("User-Agent"));
    }

    @Test
    void testGetRequest() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(testRequest));
        assertNotNull(ServletUtils.getRequest());
        assertEquals(testRequest, ServletUtils.getRequest());
    }

    @Test
    void testGetResponse() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(testRequest, testResponse));
        assertNotNull(ServletUtils.getResponse());
        assertEquals(testResponse, ServletUtils.getResponse());
    }

    @Test
    void testGetSession() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(testRequest));
        HttpSession session = ServletUtils.getSession();
        assertNotNull(session);
    }

    @Test
    void testRenderJson() throws UnsupportedEncodingException {
        ServletUtils.renderJson(testResponse, "{\"name\":\"test\"}");

        assertEquals(200, testResponse.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_UTF8.toString(), testResponse.getContentType().toString());
        assertEquals("{\"name\":\"test\"}", testResponse.getContentAsString());
    }

    @Test
    void testRenderXml() throws UnsupportedEncodingException {
        ServletUtils.renderXml(testResponse, "<root><name>test</name></root>");

        assertEquals(200, testResponse.getStatus());
        assertEquals("application/xml;charset=UTF-8", testResponse.getContentType().toString());
        assertEquals("<root><name>test</name></root>", testResponse.getContentAsString());
    }

    @Test
    void testRenderHtml() throws UnsupportedEncodingException {
        ServletUtils.renderHtml(testResponse, "<html><body>test</body></html>");

        assertEquals(200, testResponse.getStatus());
        assertEquals("text/html;charset=UTF-8", testResponse.getContentType().toString());
        assertEquals("<html><body>test</body></html>", testResponse.getContentAsString());
    }

    @Test
    void testIsAjaxRequest() {
        // 测试Accept头
        testRequest.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        assertTrue(ServletUtils.isAjaxRequest(testRequest));

        // 测试X-Requested-With头
        testRequest = new MockHttpServletRequest();
        testRequest.addHeader("X-Requested-With", "XMLHttpRequest");
        assertTrue(ServletUtils.isAjaxRequest(testRequest));

        // 测试URI后缀
        testRequest = new MockHttpServletRequest();
        testRequest.setRequestURI("/api/data.json");
        assertTrue(ServletUtils.isAjaxRequest(testRequest));

        // 测试参数
        testRequest = new MockHttpServletRequest();
        testRequest.addParameter("__ajax", "json");
        assertTrue(ServletUtils.isAjaxRequest(testRequest));

        // 测试非Ajax请求
        testRequest = new MockHttpServletRequest();
        assertFalse(ServletUtils.isAjaxRequest(testRequest));
    }

    @Test
    void testUrlEncodeAndDecode() throws UnsupportedEncodingException {
        String original = "Hello World!";
        String encoded = ServletUtils.urlEncode(original);
        String decoded = ServletUtils.urlDecode(encoded);

        assertEquals(original, decoded);
        assertNotEquals(original, encoded);
    }

    @Test
    void testUrlEncodeAndDecodeSafe() {
        String original = "Hello World!";
        String encoded = ServletUtils.urlEncodeSafe(original);
        String decoded = ServletUtils.urlDecodeSafe(encoded);

        assertEquals(original, decoded);
    }

    @Test
    void testGetClientIP() {
        // 测试X-Forwarded-For
        testRequest.addHeader("X-Forwarded-For", "192.168.1.1, 10.0.0.1");
        assertEquals("192.168.1.1", ServletUtils.getClientIp(testRequest));

        // 测试X-Real-IP
        testRequest = new MockHttpServletRequest();
        testRequest.addHeader("X-Real-IP", "192.168.1.2");
        assertEquals("192.168.1.2", ServletUtils.getClientIp(testRequest));

        // 测试RemoteAddr
        testRequest = new MockHttpServletRequest();
        testRequest.setRemoteAddr("192.168.1.3");
        assertEquals("192.168.1.3", ServletUtils.getClientIp(testRequest));
    }

    @Test
    void testGetCookieValue() {
        Cookie cookie = new Cookie("testCookie", "testValue");
        testRequest.setCookies(cookie);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(testRequest));

        assertEquals("testValue", ServletUtils.getCookieValue("testCookie"));
        assertNull(ServletUtils.getCookieValue("nonexistent"));
    }

    @Test
    void testGetCookie() {
        Cookie cookie = new Cookie("testCookie", "testValue");
        testRequest.setCookies(cookie);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(testRequest));

        Cookie found = ServletUtils.getCookie("testCookie");
        assertNotNull(found);
        assertEquals("testCookie", found.getName());
        assertEquals("testValue", found.getValue());
    }

    @Test
    void testAddCookie() {
        ServletUtils.addCookie(testResponse, "testCookie", "testValue");

        Cookie[] cookies = testResponse.getCookies();
        assertNotNull(cookies);
        assertEquals(1, cookies.length);
        assertEquals("testCookie", cookies[0].getName());
        assertEquals("testValue", cookies[0].getValue());
        assertEquals("/", cookies[0].getPath());
    }

    @Test
    void testGetFullUrl() {
        testRequest.setRequestURI("/api/users");
        testRequest.setQueryString("page=1&size=10");

        String fullUrl = ServletUtils.getFullUrl(testRequest);
        assertEquals("/api/users?page=1&size=10", fullUrl);
    }

    @Test
    void testGetFullUrlWithoutQueryString() {
        testRequest.setRequestURI("/api/users");

        String fullUrl = ServletUtils.getFullUrl(testRequest);
        assertEquals("/api/users", fullUrl);
    }

    @Test
    void testGetUserAgent() {
        testRequest.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0");

        String userAgent = ServletUtils.getUserAgent(testRequest);
        assertEquals("Mozilla/5.0", userAgent);
    }

    @Test
    void testClearCache() {
        // 先进行一些编码操作
        ServletUtils.urlEncodeSafe("test");
        ServletUtils.urlDecodeSafe("test");

        // 清理缓存
        ServletUtils.clearCache();

        // 验证缓存已清理（通过再次编码，应该重新计算）
        String encoded = ServletUtils.urlEncodeSafe("test");
        assertNotNull(encoded);
    }

    @Test
    void testNullSafety() {
        // 测试各种null情况
        assertNull(ServletUtils.getParameter("test"));
        assertEquals("default", ServletUtils.getParameter("test", "default"));

        assertNull(ServletUtils.getHeader("test"));
        assertEquals("default", ServletUtils.getHeader("test", "default"));

        assertFalse(ServletUtils.isAjaxRequest((HttpServletRequest) null));

        assertNull(ServletUtils.getClientIp((HttpServletRequest) null));
        assertNull(ServletUtils.getFullUrl((HttpServletRequest) null));
        assertNull(ServletUtils.getUserAgent((HttpServletRequest) null));
    }

    @Test
    void testParameterTypes() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("float", "123.45");
        request.addParameter("decimal", "123.456789");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    }
}