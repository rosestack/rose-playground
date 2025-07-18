package io.github.rose.core.util;

import org.apache.commons.net.util.SubnetUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Commons Net 使用示例测试
 * 展示如何使用 Commons Net 进行网络操作
 */
class CommonsNetUsageTest {
    
    private static final Logger log = LoggerFactory.getLogger(CommonsNetUsageTest.class);
    
    // IPv4 地址验证正则表达式
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$"
    );
    
    // IPv6 地址验证正则表达式
    private static final Pattern IPV6_PATTERN = Pattern.compile(
        "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^::1$|^::$"
    );
    
    // ==================== IP 地址验证 ====================
    
    @Test
    void testIPv4Validation() {
        // 有效的 IPv4 地址
        assertTrue(isValidIPv4("192.168.1.1"));
        assertTrue(isValidIPv4("10.0.0.1"));
        assertTrue(isValidIPv4("172.16.0.1"));
        assertTrue(isValidIPv4("127.0.0.1"));
        assertTrue(isValidIPv4("0.0.0.0"));
        assertTrue(isValidIPv4("255.255.255.255"));
        
        // 无效的 IPv4 地址
        assertFalse(isValidIPv4("256.1.2.3"));
        assertFalse(isValidIPv4("1.2.3.256"));
        assertFalse(isValidIPv4("192.168.1"));
        assertFalse(isValidIPv4("192.168.1.1.1"));
        assertFalse(isValidIPv4("192.168.1.abc"));
        assertFalse(isValidIPv4(""));
        assertFalse(isValidIPv4(null));
    }
    
    @Test
    void testIPv6Validation() {
        // 有效的 IPv6 地址
        assertTrue(isValidIPv6("2001:db8::1"));
        assertTrue(isValidIPv6("::1"));
        assertTrue(isValidIPv6("::"));
        assertTrue(isValidIPv6("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        
        // 无效的 IPv6 地址
        assertFalse(isValidIPv6("2001:db8::1::"));
        assertFalse(isValidIPv6("2001:db8:1"));
        assertFalse(isValidIPv6("192.168.1.1"));
        assertFalse(isValidIPv6(""));
        assertFalse(isValidIPv6(null));
    }
    
    @Test
    void testIPValidation() {
        // 测试通用 IP 验证
        assertTrue(isValidIP("192.168.1.1"));
        assertTrue(isValidIP("2001:db8::1"));
        assertFalse(isValidIP("invalid"));
        assertFalse(isValidIP(""));
        assertFalse(isValidIP(null));
    }
    
    // ==================== CIDR 范围检查 ====================
    
    @Test
    void testCIDRRangeCheck() {
        // 测试 CIDR 范围检查
        assertTrue(isInCIDR("192.168.1.5", "192.168.1.0/24"));
        assertTrue(isInCIDR("192.168.1.1", "192.168.1.0/24"));
        assertTrue(isInCIDR("192.168.1.254", "192.168.1.0/24"));
        
        assertFalse(isInCIDR("192.168.2.1", "192.168.1.0/24"));
        assertFalse(isInCIDR("10.0.0.1", "192.168.1.0/24"));
    }
    
    @Test
    void testCIDRWithDifferentMasks() {
        // 测试不同的子网掩码
        assertTrue(isInCIDR("192.168.1.1", "192.168.0.0/16"));
        assertTrue(isInCIDR("192.168.2.1", "192.168.0.0/16"));
        assertFalse(isInCIDR("192.169.1.1", "192.168.0.0/16"));
        
        assertTrue(isInCIDR("10.0.0.1", "10.0.0.0/8"));
        assertTrue(isInCIDR("10.255.255.255", "10.0.0.0/8"));
        assertFalse(isInCIDR("11.0.0.1", "10.0.0.0/8"));
    }
    
    @Test
    void testCIDRAddresses() {
        // 获取 CIDR 范围内的所有地址
        String[] addresses = getCIDRAddresses("192.168.1.0/30");
        
        assertEquals(4, addresses.length);
        assertArrayEquals(new String[]{"192.168.1.0", "192.168.1.1", "192.168.1.2", "192.168.1.3"}, addresses);
    }
    
    @Test
    void testCIDRInfo() {
        // 获取 CIDR 信息
        SubnetUtils subnetUtils = new SubnetUtils("192.168.1.0/24");
        subnetUtils.setInclusiveHostCount(true);
        
        SubnetUtils.SubnetInfo info = subnetUtils.getInfo();
        
        assertEquals("192.168.1.0", info.getNetworkAddress());
        assertEquals("192.168.1.255", info.getBroadcastAddress());
        assertEquals("255.255.255.0", info.getNetmask());
        assertEquals(256, info.getAddressCount());
        assertEquals("192.168.1.1", info.getLowAddress());
        assertEquals("192.168.1.254", info.getHighAddress());
    }
    
    // ==================== 端口验证 ====================
    
    @Test
    void testPortValidation() {
        // 有效的端口
        assertTrue(isValidPort(0));
        assertTrue(isValidPort(80));
        assertTrue(isValidPort(8080));
        assertTrue(isValidPort(65535));
        
        // 无效的端口
        assertFalse(isValidPort(-1));
        assertFalse(isValidPort(65536));
        assertFalse(isValidPort(100000));
    }
    
    @Test
    void testUserPortRange() {
        // 用户端口范围 (1024-65535)
        assertFalse(isUserPort(0));
        assertFalse(isUserPort(80));
        assertFalse(isUserPort(1023));
        assertTrue(isUserPort(1024));
        assertTrue(isUserPort(8080));
        assertTrue(isUserPort(65535));
        assertFalse(isUserPort(65536));
    }
    
    // ==================== 网络地址处理 ====================
    
    @Test
    void testHostnameResolution() {
        // 测试主机名解析
        String ip = getIpByHost("localhost");
        assertNotNull(ip);
        assertTrue(isValidIP(ip));
        
        // 测试无效主机名
        String invalidIp = getIpByHost("invalid-hostname-that-does-not-exist");
        assertEquals("invalid-hostname-that-does-not-exist", invalidIp);
    }
    
    @Test
    void testReverseLookup() {
        // 测试反向查找
        String hostname = getHostByIp("127.0.0.1");
        assertNotNull(hostname);
        
        // 测试无效IP
        String invalidHostname = getHostByIp("999.999.999.999");
        assertEquals("999.999.999.999", invalidHostname);
    }
    
    // ==================== 实际应用示例 ====================
    
    @Test
    void testNetworkConfigValidation() {
        // 模拟网络配置验证
        NetworkConfig config = new NetworkConfig("192.168.1.100", "192.168.1.0/24", 8080);
        
        assertTrue(isValidNetworkConfig(config));
    }
    
    @Test
    void testInvalidNetworkConfig() {
        // 测试无效的网络配置
        NetworkConfig config1 = new NetworkConfig("192.168.2.100", "192.168.1.0/24", 8080);
        assertFalse(isValidNetworkConfig(config1));
        
        NetworkConfig config2 = new NetworkConfig("192.168.1.100", "192.168.1.0/24", 70000);
        assertFalse(isValidNetworkConfig(config2));
    }
    
    @Test
    void testPrivateIPRanges() {
        // 测试私有IP范围
        assertTrue(isPrivateIP("10.0.0.1"));
        assertTrue(isPrivateIP("172.16.0.1"));
        assertTrue(isPrivateIP("192.168.1.1"));
        assertTrue(isPrivateIP("127.0.0.1"));
        
        assertFalse(isPrivateIP("8.8.8.8"));
        assertFalse(isPrivateIP("1.1.1.1"));
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 验证 IPv4 地址
     */
    private boolean isValidIPv4(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return false;
        }
        return IPV4_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * 验证 IPv6 地址
     */
    private boolean isValidIPv6(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return false;
        }
        return IPV6_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * 验证 IP 地址（IPv4 或 IPv6）
     */
    private boolean isValidIP(String ipAddress) {
        return isValidIPv4(ipAddress) || isValidIPv6(ipAddress);
    }
    
    /**
     * 检查 IP 是否在 CIDR 范围内
     */
    private boolean isInCIDR(String ipAddress, String cidr) {
        try {
            SubnetUtils subnetUtils = new SubnetUtils(cidr);
            subnetUtils.setInclusiveHostCount(true);
            return subnetUtils.getInfo().isInRange(ipAddress);
        } catch (Exception e) {
            log.warn("Failed to check CIDR range: {} for IP: {}", cidr, ipAddress, e);
            return false;
        }
    }
    
    /**
     * 获取 CIDR 范围内的所有地址
     */
    private String[] getCIDRAddresses(String cidr) {
        try {
            SubnetUtils subnetUtils = new SubnetUtils(cidr);
            subnetUtils.setInclusiveHostCount(true);
            return subnetUtils.getInfo().getAllAddresses();
        } catch (Exception e) {
            log.warn("Failed to get addresses for CIDR: {}", cidr, e);
            return new String[0];
        }
    }
    
    /**
     * 验证端口号
     */
    private boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }
    
    /**
     * 检查是否为用户端口
     */
    private boolean isUserPort(int port) {
        return port >= 1024 && port <= 65535;
    }
    
    /**
     * 通过主机名获取IP地址
     */
    private String getIpByHost(String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (UnknownHostException e) {
            log.debug("Failed to resolve hostname: {}", hostName, e);
            return hostName;
        }
    }
    
    /**
     * 通过IP地址获取主机名
     */
    private String getHostByIp(String ipAddress) {
        if (!isValidIP(ipAddress)) {
            return ipAddress;
        }
        
        try {
            return InetAddress.getByName(ipAddress).getHostName();
        } catch (UnknownHostException e) {
            log.debug("Failed to resolve IP address: {}", ipAddress, e);
            return ipAddress;
        }
    }
    
    /**
     * 检查是否为私有IP地址
     */
    private boolean isPrivateIP(String ipAddress) {
        if (!isValidIPv4(ipAddress)) {
            return false;
        }
        
        return isInCIDR(ipAddress, "10.0.0.0/8") ||
               isInCIDR(ipAddress, "172.16.0.0/12") ||
               isInCIDR(ipAddress, "192.168.0.0/16") ||
               isInCIDR(ipAddress, "127.0.0.0/8");
    }
    
    /**
     * 验证网络配置
     */
    private boolean isValidNetworkConfig(NetworkConfig config) {
        return isValidIP(config.getIp()) &&
               isInCIDR(config.getIp(), config.getCidr()) &&
               isValidPort(config.getPort());
    }
    
    /**
     * 网络配置类
     */
    private static class NetworkConfig {
        private final String ip;
        private final String cidr;
        private final int port;
        
        public NetworkConfig(String ip, String cidr, int port) {
            this.ip = ip;
            this.cidr = cidr;
            this.port = port;
        }
        
        public String getIp() { return ip; }
        public String getCidr() { return cidr; }
        public int getPort() { return port; }
    }
} 