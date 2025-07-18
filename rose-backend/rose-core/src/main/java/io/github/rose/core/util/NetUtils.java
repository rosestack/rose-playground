package io.github.rose.core.util;

import io.github.rose.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 优化版网络相关工具类
 * 提供IP地址处理、端口检测、网络接口操作等功能
 *
 * @author zhijun.chen
 * @since 0.0.1
 */
@Slf4j
public abstract class NetUtils {
    
    private NetUtils() {
        // 工具类，禁止实例化
    }

    // ==================== 常量定义 ====================
    
    /**
     * 默认最小端口，1024
     */
    public static final int PORT_RANGE_MIN = 1024;
    
    /**
     * 默认最大端口，65535
     */
    public static final int PORT_RANGE_MAX = 0xFFFF;
    
    /**
     * 本地回环地址
     */
    public static final String LOCAL_IP = "127.0.0.1";
    
    /**
     * 本地主机地址
     */
    public static final String LOCALHOST = "localhost";
    
    /**
     * 任意地址
     */
    public static final String ANY_HOST = "0.0.0.0";
    
    /**
     * IPv4地址正则表达式
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$"
    );
    
    /**
     * IPv6地址正则表达式
     */
    private static final Pattern IPV6_PATTERN = Pattern.compile(
        "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^::1$|^::$"
    );
    
    /**
     * 端口范围正则表达式
     */
    private static final Pattern PORT_PATTERN = Pattern.compile(":(\\d+)$");
    
    // ==================== 缓存 ====================
    
    /**
     * 本地主机名称缓存
     */
    private static volatile String cachedLocalHostName;
    
    /**
     * 本地主机地址缓存
     */
    private static volatile InetAddress cachedLocalHost;
    
    /**
     * 网络接口缓存
     */
    private static final Map<String, NetworkInterface> NETWORK_INTERFACE_CACHE = new ConcurrentHashMap<>();
    
    /**
     * IP地址缓存
     */
    private static final Map<String, String> IP_CACHE = new ConcurrentHashMap<>();
    
    // ==================== 端口相关方法 ====================
    
    /**
     * 检测本地端口可用性
     * 同时检测TCP和UDP端口
     *
     * @param port 被检测的端口
     * @return 是否可用
     */
    public static boolean isUsableLocalPort(int port) {
        if (!isValidPort(port)) {
            log.debug("Invalid port: {}", port);
            return false;
        }

        return isUsableTcpPort(port) && isUsableUdpPort(port);
    }
    
    /**
     * 检测TCP端口可用性
     *
     * @param port 端口号
     * @return 是否可用
     */
    public static boolean isUsableTcpPort(int port) {
        if (!isValidPort(port)) {
            return false;
        }
        
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(port));
            return true;
        } catch (IOException e) {
            log.debug("TCP port {} is not available: {}", port, e.getMessage());
            return false;
        }
    }
    
    /**
     * 检测UDP端口可用性
     *
     * @param port 端口号
     * @return 是否可用
     */
    public static boolean isUsableUdpPort(int port) {
        if (!isValidPort(port)) {
            return false;
        }
        
        try (DatagramSocket datagramSocket = new DatagramSocket(port)) {
            datagramSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            log.debug("UDP port {} is not available: {}", port, e.getMessage());
            return false;
        }
    }

    /**
     * 验证端口号是否有效
     *
     * @param port 端口号
     * @return 是否有效
     */
    public static boolean isValidPort(int port) {
        return port >= 0 && port <= PORT_RANGE_MAX;
    }
    
    /**
     * 验证端口号是否在用户端口范围内
     *
     * @param port 端口号
     * @return 是否在用户端口范围内
     */
    public static boolean isUserPort(int port) {
        return port >= PORT_RANGE_MIN && port <= PORT_RANGE_MAX;
    }

    /**
     * 查找1024~65535范围内的可用端口
     *
     * @return 可用的端口
     */
    public static int getUsableLocalPort() {
        return getUsableLocalPort(PORT_RANGE_MIN);
    }

    /**
     * 查找指定范围内的可用端口，最大值为65535
     *
     * @param minPort 端口最小值（包含）
     * @return 可用的端口
     */
    public static int getUsableLocalPort(int minPort) {
        return getUsableLocalPort(minPort, PORT_RANGE_MAX);
    }

    /**
     * 查找指定范围内的可用端口
     * 优化：使用更高效的端口查找算法
     *
     * @param minPort 端口最小值（包含）
     * @param maxPort 端口最大值（包含）
     * @return 可用的端口
     * @throws BusinessException 如果找不到可用端口
     */
    public static int getUsableLocalPort(int minPort, int maxPort) {
        if (minPort < 0 || maxPort > PORT_RANGE_MAX || minPort > maxPort) {
            throw new IllegalArgumentException("Invalid port range: [" + minPort + ", " + maxPort + "]");
        }
        
        // 先尝试顺序查找
        for (int port = minPort; port <= maxPort; port++) {
            if (isUsableLocalPort(port)) {
                return port;
            }
        }
        
        // 如果顺序查找失败，尝试随机查找
        Set<Integer> tried = new HashSet<>();
        int maxAttempts = Math.min(maxPort - minPort + 1, 1000);
        
        for (int i = 0; i < maxAttempts; i++) {
            int randomPort = RandomUtils.nextInt(minPort, maxPort + 1);
            if (tried.contains(randomPort)) {
                continue;
            }
            tried.add(randomPort);
            
            if (isUsableLocalPort(randomPort)) {
                return randomPort;
            }
        }

        throw new BusinessException("Could not find an available port in the range [{}, {}] after {} attempts", 
            minPort, maxPort, maxAttempts);
    }

    /**
     * 获取多个本地可用端口
     * 优化：使用更高效的批量端口查找算法
     *
     * @param numRequested 需要的端口数量
     * @param minPort      端口最小值（包含）
     * @param maxPort      端口最大值（包含）
     * @return 可用的端口集合
     * @throws BusinessException 如果找不到足够的可用端口
     */
    public static LinkedHashSet<Integer> getUsableLocalPorts(int numRequested, int minPort, int maxPort) {
        if (numRequested <= 0) {
            throw new IllegalArgumentException("Number of requested ports must be positive");
        }
        if (minPort < 0 || maxPort > PORT_RANGE_MAX || minPort > maxPort) {
            throw new IllegalArgumentException("Invalid port range: [" + minPort + ", " + maxPort + "]");
        }
        
        int availableRange = maxPort - minPort + 1;
        if (numRequested > availableRange) {
            throw new IllegalArgumentException("Requested " + numRequested + 
                " ports but only " + availableRange + " ports available in range");
        }
        
        LinkedHashSet<Integer> availablePorts = new LinkedHashSet<>();
        Set<Integer> tried = new HashSet<>();
        int maxAttempts = Math.min(availableRange, numRequested * 10);
        
        // 先尝试顺序查找
        for (int port = minPort; port <= maxPort && availablePorts.size() < numRequested; port++) {
            if (isUsableLocalPort(port)) {
                availablePorts.add(port);
            }
        }
        
        // 如果顺序查找不够，使用随机查找
        while (availablePorts.size() < numRequested && tried.size() < maxAttempts) {
            int randomPort = RandomUtils.nextInt(minPort, maxPort + 1);
            if (tried.contains(randomPort)) {
                continue;
            }
            tried.add(randomPort);
            
            if (!availablePorts.contains(randomPort) && isUsableLocalPort(randomPort)) {
                availablePorts.add(randomPort);
            }
        }

        if (availablePorts.size() < numRequested) {
            throw new BusinessException("Could not find {} available ports in the range [{}, {}], only found {}", 
                numRequested, minPort, maxPort, availablePorts.size());
        }

        return availablePorts;
    }

    // ==================== IP地址相关方法 ====================
    
    /**
     * 检查IP地址是否为未知
     *
     * @param ipAddress IP地址
     * @return 是否为未知
     */
    public static boolean isUnknown(String ipAddress) {
        return StringUtils.isBlank(ipAddress) || "unknown".equalsIgnoreCase(ipAddress);
    }
    
    /**
     * 验证IPv4地址格式
     *
     * @param ipAddress IP地址
     * @return 是否为有效的IPv4地址
     */
    public static boolean isValidIPv4(String ipAddress) {
        if (StringUtils.isBlank(ipAddress)) {
            return false;
        }
        return IPV4_PATTERN.matcher(ipAddress.trim()).matches();
    }
    
    /**
     * 验证IPv6地址格式
     *
     * @param ipAddress IP地址
     * @return 是否为有效的IPv6地址
     */
    public static boolean isValidIPv6(String ipAddress) {
        if (StringUtils.isBlank(ipAddress)) {
            return false;
        }
        return IPV6_PATTERN.matcher(ipAddress.trim()).matches();
    }
    
    /**
     * 验证IP地址格式（IPv4或IPv6）
     *
     * @param ipAddress IP地址
     * @return 是否为有效的IP地址
     */
    public static boolean isValidIP(String ipAddress) {
        return isValidIPv4(ipAddress) || isValidIPv6(ipAddress);
    }

    /**
     * 判定是否为内网IPv4地址
     * 私有IP范围：
     * <pre>
     * A类 10.0.0.0-10.255.255.255
     * B类 172.16.0.0-172.31.255.255
     * C类 192.168.0.0-192.168.255.255
     * 环回 127.0.0.0-127.255.255.255
     * </pre>
     *
     * @param ipAddress IP地址
     * @return 是否为内网IP
     */
    public static boolean isInnerIP(String ipAddress) {
        if (!isValidIPv4(ipAddress)) {
            return false;
        }

        try {
            long ipNum = ipv4ToLong(ipAddress);

            // A类私有地址：10.0.0.0-10.255.255.255
            if (isInRange(ipNum, 0x0A000000L, 0x0AFFFFFFL)) {
                return true;
            }

            // B类私有地址：172.16.0.0-172.31.255.255
            if (isInRange(ipNum, 0xAC100000L, 0xAC1FFFFFL)) {
                return true;
            }

            // C类私有地址：192.168.0.0-192.168.255.255
            if (isInRange(ipNum, 0xC0A80000L, 0xC0A8FFFFL)) {
                return true;
            }

            // 环回地址：127.0.0.0-127.255.255.255
            if (isInRange(ipNum, 0x7F000000L, 0x7FFFFFFFL)) {
                return true;
            }

            return false;
        } catch (Exception e) {
            log.warn("Failed to check if IP is inner: {}", ipAddress, e);
            return false;
        }
    }

    /**
     * 将IPv4地址转换为long值
     * 方法别名：inet_aton
     *
     * @param ipAddress IPv4地址字符串
     * @return long值
     * @throws BusinessException 如果IP地址格式无效
     */
    public static long ipv4ToLong(String ipAddress) {
        if (!isValidIPv4(ipAddress)) {
            throw new BusinessException("Invalid IPv4 address: " + ipAddress);
        }

        try {
            String[] parts = ipAddress.split("\\.");
            long result = 0;
            for (int i = 0; i < 4; i++) {
                int part = Integer.parseInt(parts[i]);
                result = (result << 8) | part;
            }
            return result;
        } catch (Exception e) {
            throw new BusinessException("Failed to convert IPv4 to long: " + ipAddress, e);
        }
    }

    /**
     * 将long值转换为IPv4地址
     * 方法别名：inet_ntoa
     *
     * @param ip long值
     * @return IPv4地址字符串
     */
    public static String longToIPv4(long ip) {
        return String.format("%d.%d.%d.%d",
            (ip >> 24) & 0xFF,
            (ip >> 16) & 0xFF,
            (ip >> 8) & 0xFF,
            ip & 0xFF);
    }

    /**
     * 检查IP是否在指定范围内
     *
     * @param ip    要检查的IP（long格式）
     * @param begin 范围开始（long格式）
     * @param end   范围结束（long格式）
     * @return 是否在范围内
     */
    private static boolean isInRange(long ip, long begin, long end) {
        return ip >= begin && ip <= end;
    }

    /**
     * 检查IP地址是否在CIDR范围内
     *
     * @param ipAddress IP地址
     * @param cidr      CIDR表示法（如：192.168.1.0/24）
     * @return 是否在范围内
     */
    public static boolean isInCIDR(String ipAddress, String cidr) {
        if (!isValidIPv4(ipAddress) || StringUtils.isBlank(cidr)) {
            return false;
        }

        try {
            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                return false;
            }

            String networkAddress = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            if (prefixLength < 0 || prefixLength > 32) {
                return false;
            }

            long ipLong = ipv4ToLong(ipAddress);
            long networkLong = ipv4ToLong(networkAddress);
            long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;

            return (ipLong & mask) == (networkLong & mask);
        } catch (Exception e) {
            log.warn("Failed to check CIDR: {} in {}", ipAddress, cidr, e);
            return false;
        }
    }

    // ==================== 网络地址构建方法 ====================

    /**
     * 构建InetSocketAddress
     * 当host中包含端口时（用":"分隔），使用host中的端口，否则使用默认端口
     * 给定host为空时使用本地host（127.0.0.1）
     *
     * @param host        主机地址
     * @param defaultPort 默认端口
     * @return InetSocketAddress对象
     * @throws IllegalArgumentException 如果端口无效
     */
    public static InetSocketAddress buildInetSocketAddress(String host, int defaultPort) {
        if (StringUtils.isBlank(host)) {
            host = LOCAL_IP;
        }

        String destHost;
        int port;

        // 检查IPv6地址格式 [host]:port
        if (host.startsWith("[") && host.contains("]:")) {
            int bracketIndex = host.indexOf("]:");
            destHost = host.substring(1, bracketIndex);
            port = Integer.parseInt(host.substring(bracketIndex + 2));
        } else {
            // IPv4地址格式 host:port
            Matcher matcher = PORT_PATTERN.matcher(host);
            if (matcher.find()) {
                destHost = host.substring(0, matcher.start());
                port = Integer.parseInt(matcher.group(1));
            } else {
                destHost = host;
                port = defaultPort;
            }
        }

        if (!isValidPort(port)) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }

        return new InetSocketAddress(destHost, port);
    }

    /**
     * 解析主机和端口
     *
     * @param hostPort 主机:端口字符串
     * @return 包含主机和端口的数组 [host, port]
     */
    public static String[] parseHostPort(String hostPort) {
        if (StringUtils.isBlank(hostPort)) {
            return new String[]{LOCAL_IP, "0"};
        }

        InetSocketAddress address = buildInetSocketAddress(hostPort, 0);
        return new String[]{address.getHostString(), String.valueOf(address.getPort())};
    }

    /**
     * 通过域名获取IP地址（带缓存）
     *
     * @param hostName 主机名
     * @return IP地址，如果解析失败返回原主机名
     */
    public static String getIpByHost(String hostName) {
        if (StringUtils.isBlank(hostName)) {
            return LOCAL_IP;
        }

        // 检查缓存
        String cachedIp = IP_CACHE.get(hostName);
        if (cachedIp != null) {
            return cachedIp;
        }

        try {
            String ip = InetAddress.getByName(hostName).getHostAddress();
            // 缓存结果
            IP_CACHE.put(hostName, ip);
            return ip;
        } catch (UnknownHostException e) {
            log.debug("Failed to resolve hostname: {}", hostName, e);
            // 缓存失败结果，避免重复解析
            IP_CACHE.put(hostName, hostName);
            return hostName;
        }
    }

    /**
     * 通过IP地址获取主机名
     *
     * @param ipAddress IP地址
     * @return 主机名，如果解析失败返回原IP地址
     */
    public static String getHostByIp(String ipAddress) {
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

    // ==================== 网络连接测试方法 ====================

    /**
     * 测试指定地址和端口是否可连接
     *
     * @param address 网络地址
     * @param timeout 超时时间（毫秒）
     * @return 是否可连接
     */
    public static boolean isOpen(InetSocketAddress address, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(address, timeout);
            return true;
        } catch (Exception e) {
            log.debug("Connection test failed for {}: {}", address, e.getMessage());
            return false;
        }
    }

    /**
     * 测试指定主机和端口是否可连接
     *
     * @param host    主机地址
     * @param port    端口号
     * @param timeout 超时时间（毫秒）
     * @return 是否可连接
     */
    public static boolean isOpen(String host, int port, int timeout) {
        return isOpen(new InetSocketAddress(host, port), timeout);
    }

    /**
     * Ping指定IP地址
     *
     * @param ip      IP地址
     * @param timeout 超时时间（毫秒）
     * @return 是否可达
     */
    public static boolean ping(String ip, int timeout) {
        try {
            return InetAddress.getByName(ip).isReachable(timeout);
        } catch (Exception e) {
            log.debug("Ping failed for {}: {}", ip, e.getMessage());
            return false;
        }
    }

    /**
     * Ping指定IP地址（默认200ms超时）
     *
     * @param ip IP地址
     * @return 是否可达
     */
    public static boolean ping(String ip) {
        return ping(ip, 200);
    }

    // ==================== 本地主机信息获取方法 ====================

    /**
     * 获得本机的IPv4地址列表
     * 返回的IP列表有序，按照系统设备顺序
     *
     * @return IP地址列表
     */
    public static LinkedHashSet<String> localIpv4s() {
        final LinkedHashSet<InetAddress> localAddressList = localAddressList(t -> t instanceof Inet4Address);
        return toIpList(localAddressList);
    }

    /**
     * 获得本机的IPv6地址列表
     * 返回的IP列表有序，按照系统设备顺序
     *
     * @return IP地址列表
     */
    public static LinkedHashSet<String> localIpv6s() {
        final LinkedHashSet<InetAddress> localAddressList = localAddressList(t -> t instanceof Inet6Address);
        return toIpList(localAddressList);
    }

    /**
     * 获取所有满足过滤条件的本地IP地址对象
     *
     * @param addressFilter 过滤器，null表示不过滤，获取所有地址
     * @return 过滤后的地址对象列表
     */
    public static LinkedHashSet<InetAddress> localAddressList(Predicate<InetAddress> addressFilter) {
        return localAddressList(null, addressFilter);
    }

    /**
     * 获取所有满足过滤条件的本地IP地址对象
     *
     * @param networkInterfaceFilter 网络接口过滤器，null表示不过滤，获取所有网卡
     * @param addressFilter          地址过滤器，null表示不过滤，获取所有地址
     * @return 过滤后的地址对象列表
     */
    public static LinkedHashSet<InetAddress> localAddressList(Predicate<NetworkInterface> networkInterfaceFilter,
                                                              Predicate<InetAddress> addressFilter) {
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            log.error("Failed to get network interfaces", e);
            throw new BusinessException("Get network interface error!", e);
        }

        if (networkInterfaces == null) {
            throw new BusinessException("Get network interface error!");
        }

        final LinkedHashSet<InetAddress> ipSet = new LinkedHashSet<>();

        while (networkInterfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = networkInterfaces.nextElement();

            // 应用网络接口过滤器
            if (networkInterfaceFilter != null && !networkInterfaceFilter.test(networkInterface)) {
                continue;
            }

            final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                final InetAddress inetAddress = inetAddresses.nextElement();
                if (inetAddress != null && (addressFilter == null || addressFilter.test(inetAddress))) {
                    ipSet.add(inetAddress);
                }
            }
        }

        return ipSet;
    }

    /**
     * 将InetAddress集合转换为IP字符串集合
     *
     * @param addressList InetAddress集合
     * @return IP字符串集合
     */
    public static LinkedHashSet<String> toIpList(Set<InetAddress> addressList) {
        final LinkedHashSet<String> ipSet = new LinkedHashSet<>();
        for (InetAddress address : addressList) {
            ipSet.add(address.getHostAddress());
        }
        return ipSet;
    }

    /**
     * 获取本机地址（优先获取非内网地址）
     * 优化：添加缓存机制
     *
     * @return 本机InetAddress
     */
    public static InetAddress getLocalhost() {
        // 检查缓存
        if (cachedLocalHost != null) {
            return cachedLocalHost;
        }

        synchronized (NetUtils.class) {
            // 双重检查
            if (cachedLocalHost != null) {
                return cachedLocalHost;
            }

            final LinkedHashSet<InetAddress> localAddressList = localAddressList(address -> {
                // 非loopback地址，指127.*.*.*的地址
                return !address.isLoopbackAddress()
                        // 需为IPV4地址
                        && address instanceof Inet4Address;
            });

            if (ObjectUtils.isNotEmpty(localAddressList)) {
                InetAddress siteLocalAddress = null;
                for (InetAddress inetAddress : localAddressList) {
                    if (!inetAddress.isSiteLocalAddress()) {
                        // 非地区本地地址，指10.0.0.0 ~ 10.255.255.255、172.16.0.0 ~ 172.31.255.255、192.168.0.0 ~ 192.168.255.255
                        cachedLocalHost = inetAddress;
                        return inetAddress;
                    } else if (siteLocalAddress == null) {
                        siteLocalAddress = inetAddress;
                    }
                }

                if (siteLocalAddress != null) {
                    cachedLocalHost = siteLocalAddress;
                    return siteLocalAddress;
                }
            }

            try {
                InetAddress localhost = InetAddress.getLocalHost();
                cachedLocalHost = localhost;
                return localhost;
            } catch (UnknownHostException e) {
                log.warn("Failed to get localhost", e);
            }

            return null;
        }
    }

    /**
     * 获取本机IP地址字符串
     *
     * @return 本机IP地址字符串
     */
    public static String getLocalhostStr() {
        InetAddress localhost = getLocalhost();
        if (localhost != null) {
            return localhost.getHostAddress();
        }
        return null;
    }

    /**
     * 获取主机名称（带缓存）
     * 注意此方法会触发反向DNS解析，可能导致阻塞
     *
     * @return 主机名称
     */
    public static String getLocalHostName() {
        // 检查缓存
        if (cachedLocalHostName != null) {
            return cachedLocalHostName;
        }

        synchronized (NetUtils.class) {
            // 双重检查
            if (cachedLocalHostName != null) {
                return cachedLocalHostName;
            }

            final InetAddress localhost = getLocalhost();
            if (localhost != null) {
                String name = localhost.getHostName();
                if (StringUtils.isEmpty(name)) {
                    name = localhost.getHostAddress();
                }
                cachedLocalHostName = name;
                return name;
            }

            return null;
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 清理缓存
     */
    public static void clearCache() {
        IP_CACHE.clear();
        NETWORK_INTERFACE_CACHE.clear();
        cachedLocalHost = null;
        cachedLocalHostName = null;
    }

    /**
     * 获取多级反向代理的真实IP
     *
     * @param ip 可能包含多个IP的字符串
     * @return 第一个非unknown的IP
     */
    public static String getMultistageReverseProxyIp(String ip) {
        if (ip != null && ip.contains(",")) {
            for (String subIp : ip.split(",")) {
                String trimmedIp = subIp.trim();
                if (!isUnknown(trimmedIp)) {
                    return trimmedIp;
                }
            }
        }
        return ip;
    }
}
