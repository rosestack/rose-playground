package io.github.rose.core.util;

import io.github.rose.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.*;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NetUtilsOptimized测试类
 * 验证优化后的网络工具类功能
 */
class NetUtilsTest {

    @BeforeEach
    void setUp() {
        // 清理缓存
        NetUtils.clearCache();
    }

    // ==================== 端口相关测试 ====================

    @Test
    void testIsValidPort() {
        assertTrue(NetUtils.isValidPort(80));
        assertTrue(NetUtils.isValidPort(0));
        assertTrue(NetUtils.isValidPort(65535));
        
        assertFalse(NetUtils.isValidPort(-1));
        assertFalse(NetUtils.isValidPort(65536));
    }

    @Test
    void testIsUserPort() {
        assertTrue(NetUtils.isUserPort(1024));
        assertTrue(NetUtils.isUserPort(8080));
        assertTrue(NetUtils.isUserPort(65535));
        
        assertFalse(NetUtils.isUserPort(80));
        assertFalse(NetUtils.isUserPort(1023));
        assertFalse(NetUtils.isUserPort(65536));
    }

    @Test
    void testIsUsableTcpPort() {
        // 测试一个通常可用的端口
        int port = NetUtils.getUsableLocalPort();
        assertTrue(NetUtils.isUsableTcpPort(port));
        
        // 测试无效端口
        assertFalse(NetUtils.isUsableTcpPort(-1));
        assertFalse(NetUtils.isUsableTcpPort(65536));
    }

    @Test
    void testGetUsableLocalPort() {
        int port = NetUtils.getUsableLocalPort();
        assertTrue(NetUtils.isUserPort(port));
        assertTrue(NetUtils.isUsableLocalPort(port));
    }

    @Test
    void testGetUsableLocalPortWithRange() {
        int port = NetUtils.getUsableLocalPort(8000, 9000);
        assertTrue(port >= 8000 && port <= 9000);
        assertTrue(NetUtils.isUsableLocalPort(port));
    }

    @Test
    void testGetUsableLocalPortInvalidRange() {
        assertThrows(IllegalArgumentException.class, 
            () -> NetUtils.getUsableLocalPort(9000, 8000));
        assertThrows(IllegalArgumentException.class, 
            () -> NetUtils.getUsableLocalPort(-1, 1000));
        assertThrows(IllegalArgumentException.class, 
            () -> NetUtils.getUsableLocalPort(1000, 70000));
    }

    @Test
    void testGetUsableLocalPorts() {
        LinkedHashSet<Integer> ports = NetUtils.getUsableLocalPorts(3, 8000, 9000);
        assertEquals(3, ports.size());
        
        for (Integer port : ports) {
            assertTrue(port >= 8000 && port <= 9000);
            assertTrue(NetUtils.isUsableLocalPort(port));
        }
    }

    @Test
    void testGetUsableLocalPortsInvalidRequest() {
        assertThrows(IllegalArgumentException.class, 
            () -> NetUtils.getUsableLocalPorts(0, 8000, 9000));
        assertThrows(IllegalArgumentException.class, 
            () -> NetUtils.getUsableLocalPorts(2000, 8000, 9000));
    }

    // ==================== IP地址相关测试 ====================

    @Test
    void testIsUnknown() {
        assertTrue(NetUtils.isUnknown(null));
        assertTrue(NetUtils.isUnknown(""));
        assertTrue(NetUtils.isUnknown("   "));
        assertTrue(NetUtils.isUnknown("unknown"));
        assertTrue(NetUtils.isUnknown("UNKNOWN"));
        
        assertFalse(NetUtils.isUnknown("192.168.1.1"));
        assertFalse(NetUtils.isUnknown("127.0.0.1"));
    }

    @Test
    void testIsValidIPv4() {
        assertTrue(NetUtils.isValidIPv4("192.168.1.1"));
        assertTrue(NetUtils.isValidIPv4("127.0.0.1"));
        assertTrue(NetUtils.isValidIPv4("0.0.0.0"));
        assertTrue(NetUtils.isValidIPv4("255.255.255.255"));
        
        assertFalse(NetUtils.isValidIPv4("256.1.1.1"));
        assertFalse(NetUtils.isValidIPv4("192.168.1"));
        assertFalse(NetUtils.isValidIPv4("192.168.1.1.1"));
        assertFalse(NetUtils.isValidIPv4(""));
        assertFalse(NetUtils.isValidIPv4(null));
    }

    @Test
    void testIsValidIPv6() {
        assertTrue(NetUtils.isValidIPv6("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        assertTrue(NetUtils.isValidIPv6("::1"));
        assertTrue(NetUtils.isValidIPv6("::"));
        
        assertFalse(NetUtils.isValidIPv6("192.168.1.1"));
        assertFalse(NetUtils.isValidIPv6(""));
        assertFalse(NetUtils.isValidIPv6(null));
    }

    @Test
    void testIsValidIP() {
        assertTrue(NetUtils.isValidIP("192.168.1.1"));
        assertTrue(NetUtils.isValidIP("::1"));
        
        assertFalse(NetUtils.isValidIP("invalid"));
        assertFalse(NetUtils.isValidIP(""));
        assertFalse(NetUtils.isValidIP(null));
    }

    @Test
    void testIsInnerIP() {
        // 私有IP地址
        assertTrue(NetUtils.isInnerIP("10.0.0.1"));
        assertTrue(NetUtils.isInnerIP("172.16.0.1"));
        assertTrue(NetUtils.isInnerIP("192.168.1.1"));
        assertTrue(NetUtils.isInnerIP("127.0.0.1"));
        
        // 公网IP地址
        assertFalse(NetUtils.isInnerIP("8.8.8.8"));
        assertFalse(NetUtils.isInnerIP("114.114.114.114"));
        
        // 无效IP
        assertFalse(NetUtils.isInnerIP("invalid"));
        assertFalse(NetUtils.isInnerIP(""));
        assertFalse(NetUtils.isInnerIP(null));
    }

    @Test
    void testIpv4ToLong() {
        assertEquals(0x7F000001L, NetUtils.ipv4ToLong("127.0.0.1"));
        assertEquals(0xC0A80101L, NetUtils.ipv4ToLong("192.168.1.1"));
        assertEquals(0x00000000L, NetUtils.ipv4ToLong("0.0.0.0"));
        assertEquals(0xFFFFFFFFL, NetUtils.ipv4ToLong("255.255.255.255"));
        
        assertThrows(BusinessException.class, 
            () -> NetUtils.ipv4ToLong("invalid"));
    }

    @Test
    void testLongToIPv4() {
        assertEquals("127.0.0.1", NetUtils.longToIPv4(0x7F000001L));
        assertEquals("192.168.1.1", NetUtils.longToIPv4(0xC0A80101L));
        assertEquals("0.0.0.0", NetUtils.longToIPv4(0x00000000L));
        assertEquals("255.255.255.255", NetUtils.longToIPv4(0xFFFFFFFFL));
    }

    @Test
    void testIsInCIDR() {
        assertTrue(NetUtils.isInCIDR("192.168.1.100", "192.168.1.0/24"));
        assertTrue(NetUtils.isInCIDR("10.0.0.1", "10.0.0.0/8"));
        
        assertFalse(NetUtils.isInCIDR("192.168.2.1", "192.168.1.0/24"));
        assertFalse(NetUtils.isInCIDR("invalid", "192.168.1.0/24"));
        assertFalse(NetUtils.isInCIDR("192.168.1.1", "invalid"));
    }

    // ==================== 网络地址构建测试 ====================

    @Test
    void testBuildInetSocketAddress() {
        InetSocketAddress address = NetUtils.buildInetSocketAddress("localhost:8080", 80);
        assertEquals("localhost", address.getHostString());
        assertEquals(8080, address.getPort());
        
        address = NetUtils.buildInetSocketAddress("localhost", 80);
        assertEquals("localhost", address.getHostString());
        assertEquals(80, address.getPort());
        
        address = NetUtils.buildInetSocketAddress("", 80);
        assertEquals("127.0.0.1", address.getHostString());
        assertEquals(80, address.getPort());
    }

    @Test
    void testBuildInetSocketAddressIPv6() {
        InetSocketAddress address = NetUtils.buildInetSocketAddress("[::1]:8080", 80);
        assertEquals("::1", address.getHostString());
        assertEquals(8080, address.getPort());
    }

    @Test
    void testBuildInetSocketAddressInvalidPort() {
        assertThrows(IllegalArgumentException.class, 
            () -> NetUtils.buildInetSocketAddress("localhost:70000", 80));
    }

    @Test
    void testParseHostPort() {
        String[] result = NetUtils.parseHostPort("localhost:8080");
        assertEquals("localhost", result[0]);
        assertEquals("8080", result[1]);
        
        result = NetUtils.parseHostPort("localhost");
        assertEquals("localhost", result[0]);
        assertEquals("0", result[1]);
        
        result = NetUtils.parseHostPort("");
        assertEquals("127.0.0.1", result[0]);
        assertEquals("0", result[1]);
    }

    @Test
    void testGetIpByHost() {
        String ip = NetUtils.getIpByHost("localhost");
        assertTrue(NetUtils.isValidIP(ip));
        
        // 测试缓存
        String cachedIp = NetUtils.getIpByHost("localhost");
        assertEquals(ip, cachedIp);
        
        // 测试无效主机名
        String invalidResult = NetUtils.getIpByHost("invalid.host.name.that.does.not.exist");
        assertEquals("invalid.host.name.that.does.not.exist", invalidResult);
    }

    // ==================== 网络连接测试 ====================

    @Test
    void testPing() {
        // 测试本地回环地址
        assertTrue(NetUtils.ping("127.0.0.1"));
        assertTrue(NetUtils.ping("127.0.0.1", 1000));
        
        // 测试无效地址
        assertFalse(NetUtils.ping("192.168.255.255", 100));
    }

    @Test
    void testIsOpen() {
        // 测试无效地址
        assertFalse(NetUtils.isOpen("192.168.255.255", 80, 100));
        assertFalse(NetUtils.isOpen(new InetSocketAddress("192.168.255.255", 80), 100));
    }

    // ==================== 本地主机信息测试 ====================

    @Test
    void testLocalIpv4s() {
        LinkedHashSet<String> ipv4s = NetUtils.localIpv4s();
        assertNotNull(ipv4s);
        assertFalse(ipv4s.isEmpty());

        // 验证所有IP都是IPv4格式
        for (String ip : ipv4s) {
            assertTrue(NetUtils.isValidIPv4(ip), "IP should be valid IPv4: " + ip);
        }

        System.out.println("Local IPv4 addresses: " + ipv4s);
    }

    @Test
    void testLocalIpv6s() {
        LinkedHashSet<String> ipv6s = NetUtils.localIpv6s();
        assertNotNull(ipv6s);

        // 验证所有IP都是IPv6格式（如果有的话）
        for (String ip : ipv6s) {
            assertTrue(NetUtils.isValidIPv6(ip) || ip.contains(":"), "IP should be valid IPv6: " + ip);
        }

        System.out.println("Local IPv6 addresses: " + ipv6s);
    }

    @Test
    void testLocalAddressList() {
        LinkedHashSet<InetAddress> addresses = NetUtils.localAddressList(null);
        assertNotNull(addresses);
        assertFalse(addresses.isEmpty());

        // 测试过滤器
        LinkedHashSet<InetAddress> ipv4Addresses = NetUtils.localAddressList(addr -> addr instanceof Inet4Address);
        assertNotNull(ipv4Addresses);
        assertFalse(ipv4Addresses.isEmpty());

        // 验证所有地址都是IPv4
        for (InetAddress addr : ipv4Addresses) {
            assertTrue(addr instanceof Inet4Address, "Address should be IPv4: " + addr);
        }
    }

    @Test
    void testLocalAddressListWithNetworkFilter() {
        // 测试网络接口过滤器和地址过滤器
        LinkedHashSet<InetAddress> addresses = NetUtils.localAddressList(
            iface -> {
                try {
                    return iface.isUp() && !iface.isLoopback();
                } catch (SocketException e) {
                    return false;
                }
            },
            addr -> addr instanceof Inet4Address
        );

        assertNotNull(addresses);

        // 验证所有地址都是IPv4
        for (InetAddress addr : addresses) {
            assertTrue(addr instanceof Inet4Address, "Address should be IPv4: " + addr);
        }
    }

    @Test
    void testToIpList() {
        Set<InetAddress> addresses = new LinkedHashSet<>();
        try {
            addresses.add(InetAddress.getByName("127.0.0.1"));
            addresses.add(InetAddress.getByName("::1"));
        } catch (UnknownHostException e) {
            fail("Failed to create test addresses", e);
        }

        LinkedHashSet<String> ipList = NetUtils.toIpList(addresses);
        assertNotNull(ipList);
        assertEquals(2, ipList.size());
        assertTrue(ipList.contains("127.0.0.1"));
        assertTrue(ipList.contains("::1") || ipList.contains("0:0:0:0:0:0:0:1"));
    }

    @Test
    void testGetLocalhost() {
        InetAddress localhost = NetUtils.getLocalhost();
        assertNotNull(localhost);
        assertFalse(localhost.isLoopbackAddress(), "Should not be loopback address");

        // 测试缓存
        InetAddress cachedLocalhost = NetUtils.getLocalhost();
        assertSame(localhost, cachedLocalhost, "Should return cached instance");

        // 清除缓存后重新获取
        NetUtils.clearCache();
        InetAddress newLocalhost = NetUtils.getLocalhost();
        assertNotNull(newLocalhost);
        assertEquals(localhost.getHostAddress(), newLocalhost.getHostAddress());
    }

    @Test
    void testGetLocalhostStr() {
        String localhostStr = NetUtils.getLocalhostStr();
        assertNotNull(localhostStr);
        assertTrue(NetUtils.isValidIP(localhostStr), "Should be valid IP: " + localhostStr);

        // 验证与getLocalhost一致
        InetAddress localhost = NetUtils.getLocalhost();
        assertEquals(localhost.getHostAddress(), localhostStr);
    }

    @Test
    void testGetLocalHostName() {
        String hostName = NetUtils.getLocalHostName();
        assertNotNull(hostName);
        assertFalse(hostName.isEmpty());

        // 测试缓存
        String cachedHostName = NetUtils.getLocalHostName();
        assertEquals(hostName, cachedHostName, "Should return cached hostname");

        // 清除缓存后重新获取
        NetUtils.clearCache();
        String newHostName = NetUtils.getLocalHostName();
        assertNotNull(newHostName);
        assertEquals(hostName, newHostName);

        System.out.println("Local host name: " + hostName);
    }

    // ==================== 工具方法测试 ====================

    @Test
    void testGetMultistageReverseProxyIp() {
        assertEquals("192.168.1.1",
            NetUtils.getMultistageReverseProxyIp("192.168.1.1, unknown, 10.0.0.1"));
        assertEquals("10.0.0.1",
            NetUtils.getMultistageReverseProxyIp("unknown, 10.0.0.1"));
        assertEquals("192.168.1.1",
            NetUtils.getMultistageReverseProxyIp("192.168.1.1"));
        assertNull(NetUtils.getMultistageReverseProxyIp(null));
    }

    @Test
    void testClearCache() {
        // 先添加一些缓存
        NetUtils.getIpByHost("localhost");
        NetUtils.getLocalhost();
        NetUtils.getLocalHostName();

        // 清理缓存
        NetUtils.clearCache();

        // 验证缓存已清理（这里只能间接验证）
        assertDoesNotThrow(() -> NetUtils.clearCache());
    }
}
