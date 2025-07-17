package io.github.rose.core.util;

import io.github.rose.core.exception.BusinessException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网络相关工具
 *
 * @author zhijun.chen
 * @since 0.0.1
 */
public class NetUtils {
    /**
     * 默认最小端口，1024
     */
    public static final int PORT_RANGE_MIN = 1024;
    /**
     * 默认最大端口，65535
     */
    public static final int PORT_RANGE_MAX = 0xFFFF;

    public static final String LOCAL_IP = "127.0.0.1";
    private static final Pattern ip4RegExp = Pattern.compile("^((?:1?[1-9]?\\d|2(?:[0-4]\\d|5[0-5]))\\.){4}$");
    //本地主机名称
    private static String LOCALHOST_NAME;

    /**
     * 检测本地端口可用性<br>
     * 来自org.springframework.util.SocketUtils
     *
     * @param port 被检测的端口
     * @return 是否可用
     */
    public static boolean isUsableLocalPort(int port) {
        if (false == isValidPort(port)) {
            // 给定的IP未在指定端口范围中
            return false;
        }

        // issue#765@Github, 某些绑定非127.0.0.1的端口无法被检测到
        try (ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
        } catch (IOException ignored) {
            return false;
        }

        try (DatagramSocket ds = new DatagramSocket(port)) {
            ds.setReuseAddress(true);
        } catch (IOException ignored) {
            return false;
        }

        return true;
    }

    public static boolean isValidPort(int port) {
        // 有效端口是0～65535
        return port >= 0 && port <= PORT_RANGE_MAX;
    }

    /**
     * 查找1024~65535范围内的可用端口<br>
     * 此方法只检测给定范围内的随机一个端口，检测65535-1024次<br>
     * 来自org.springframework.util.SocketUtils
     *
     * @return 可用的端口
     */
    public static int getUsableLocalPort() {
        return getUsableLocalPort(PORT_RANGE_MIN);
    }

    /**
     * 查找指定范围内的可用端口，最大值为65535<br>
     * 此方法只检测给定范围内的随机一个端口，检测65535-minPort次<br>
     * 来自org.springframework.util.SocketUtils
     *
     * @param minPort 端口最小值（包含）
     * @return 可用的端口
     */
    public static int getUsableLocalPort(int minPort) {
        return getUsableLocalPort(minPort, PORT_RANGE_MAX);
    }

    /**
     * 查找指定范围内的可用端口<br>
     * 此方法只检测给定范围内的随机一个端口，检测maxPort-minPort次<br>
     * 来自org.springframework.util.SocketUtils
     *
     * @param minPort 端口最小值（包含）
     * @param maxPort 端口最大值（包含）
     * @return 可用的端口
     */
    public static int getUsableLocalPort(int minPort, int maxPort) {
        final int maxPortExclude = maxPort + 1;
        int randomPort;
        for (int i = minPort; i < maxPortExclude; i++) {
            randomPort = RandomUtils.nextInt(minPort, maxPortExclude);
            if (isUsableLocalPort(randomPort)) {
                return randomPort;
            }
        }

        throw new BusinessException("Could not find an available port in the range [{}, {}] after {} attempts", minPort, maxPort, maxPort - minPort);
    }

    /**
     * 获取多个本地可用端口<br>
     * 来自org.springframework.util.SocketUtils
     *
     * @param numRequested 尝试次数
     * @param minPort      端口最小值（包含）
     * @param maxPort      端口最大值（包含）
     * @return 可用的端口
     * @since 4.5.4
     */
    public static LinkedHashSet<Integer> getUsableLocalPorts(int numRequested, int minPort, int maxPort) {
        final LinkedHashSet<Integer> availablePorts = new LinkedHashSet<>();
        int attemptCount = 0;
        while ((++attemptCount <= numRequested + 100) && availablePorts.size() < numRequested) {
            availablePorts.add(getUsableLocalPort(minPort, maxPort));
        }

        if (availablePorts.size() != numRequested) {
            throw new BusinessException("Could not find {} available  ports in the range [{}, {}]", numRequested, minPort, maxPort);
        }

        return availablePorts;
    }

    public static boolean isUnknown(String ipAddress) {
        return StringUtils.isBlank(ipAddress) || "unknown".equalsIgnoreCase(ipAddress);
    }

    /**
     * 判定是否为内网IPv4<br>
     * 私有IP：
     * <pre>
     * A类 10.0.0.0-10.255.255.255
     * B类 172.16.0.0-172.31.255.255
     * C类 192.168.0.0-192.168.255.255
     * </pre>
     * 当然，还有127这个网段是环回地址
     *
     * @param ipAddress IP地址
     * @return 是否为内网IP
     * @since 5.7.18
     */
    public static boolean isInnerIP(String ipAddress) {
        boolean isInnerIp;
        long ipNum = ipv4ToLong(ipAddress);

        long aBegin = ipv4ToLong("10.0.0.0");
        long aEnd = ipv4ToLong("10.255.255.255");

        long bBegin = ipv4ToLong("172.16.0.0");
        long bEnd = ipv4ToLong("172.31.255.255");

        long cBegin = ipv4ToLong("192.168.0.0");
        long cEnd = ipv4ToLong("192.168.255.255");

        isInnerIp = isInner(ipNum, aBegin, aEnd) || isInner(ipNum, bBegin, bEnd) || isInner(ipNum, cBegin, cEnd) || LOCAL_IP.equals(ipAddress);
        return isInnerIp;
    }

    /**
     * 根据ip地址(xxx.xxx.xxx.xxx)计算出long型的数据
     * 方法别名：inet_aton
     *
     * @param strIP IP V4 地址
     * @return long值
     */
    public static long ipv4ToLong(String strIP) {
        final Matcher matcher = ip4RegExp.matcher(strIP);
        if (matcher.matches()) {
            return matchAddress(matcher);
        }
        throw new BusinessException("Invalid IPv4 address!");
    }

    /**
     * 将匹配到的Ipv4地址的4个分组分别处理
     *
     * @param matcher 匹配到的Ipv4正则
     * @return ipv4对应long
     */
    private static long matchAddress(Matcher matcher) {
        long addr = 0;
        for (int i = 1; i <= 4; ++i) {
            addr |= Long.parseLong(matcher.group(i)) << 8 * (4 - i);
        }
        return addr;
    }

    /**
     * 指定IP的long是否在指定范围内
     *
     * @param userIp 用户IP
     * @param begin  开始IP
     * @param end    结束IP
     * @return 是否在范围内
     */
    private static boolean isInner(long userIp, long begin, long end) {
        return (userIp >= begin) && (userIp <= end);
    }

    /**
     * 构建InetSocketAddress<br>
     * 当host中包含端口时（用“：”隔开），使用host中的端口，否则使用默认端口<br>
     * 给定host为空时使用本地host（127.0.0.1）
     *
     * @param host        Host
     * @param defaultPort 默认端口
     * @return InetSocketAddress
     */
    public static InetSocketAddress buildInetSocketAddress(String host, int defaultPort) {
        if (StringUtils.isBlank(host)) {
            host = LOCAL_IP;
        }

        String destHost;
        int port;
        int index = host.indexOf(":");
        if (index != -1) {
            // host:port形式
            destHost = host.substring(0, index);
            port = Integer.parseInt(host.substring(index + 1));
        } else {
            destHost = host;
            port = defaultPort;
        }

        return new InetSocketAddress(destHost, port);
    }

    /**
     * 通过域名得到IP
     *
     * @param hostName HOST
     * @return ip address or hostName if UnknownHostException
     */
    public static String getIpByHost(String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (UnknownHostException e) {
            return hostName;
        }
    }

    /**
     * 获取指定名称的网卡信息
     *
     * @param name 网络接口名，例如Linux下默认是eth0
     * @return 网卡，未找到返回{@code null}
     * @since 5.0.7
     */
    public static NetworkInterface getNetworkInterface(String name) {
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return null;
        }

        NetworkInterface netInterface;
        while (networkInterfaces.hasMoreElements()) {
            netInterface = networkInterfaces.nextElement();
            if (null != netInterface && name.equals(netInterface.getName())) {
                return netInterface;
            }
        }

        return null;
    }

    /**
     * 获取本机所有网卡
     *
     * @return 所有网卡，异常返回{@code null}
     * @since 3.0.1
     */
    public static Collection<NetworkInterface> getNetworkInterfaces() {
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return null;
        }

        final List<NetworkInterface> ifCollection = new ArrayList<>();
        while (networkInterfaces != null && networkInterfaces.hasMoreElements()) {
            ifCollection.add(networkInterfaces.nextElement());
        }
        return ifCollection;
    }

    /**
     * 获得本机的IPv4地址列表<br>
     * 返回的IP列表有序，按照系统设备顺序
     *
     * @return IP地址列表 {@link LinkedHashSet}
     */
    public static LinkedHashSet<String> localIpv4s() {
        final LinkedHashSet<InetAddress> localAddressList = localAddressList(t -> t instanceof Inet4Address);

        return toIpList(localAddressList);
    }

    /**
     * 获得本机的IPv6地址列表<br>
     * 返回的IP列表有序，按照系统设备顺序
     *
     * @return IP地址列表 {@link LinkedHashSet}
     * @since 4.5.17
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
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        if (networkInterfaces == null) {
            throw new RuntimeException("Get network interface error!");
        }

        final LinkedHashSet<InetAddress> ipSet = new LinkedHashSet<>();

        while (networkInterfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = networkInterfaces.nextElement();
            final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                final InetAddress inetAddress = inetAddresses.nextElement();
                if (inetAddress != null && (null == addressFilter || addressFilter.test(inetAddress))) {
                    ipSet.add(inetAddress);
                }
            }
        }
        return ipSet;
    }

    public static LinkedHashSet<String> toIpList(Set<InetAddress> addressList) {
        final LinkedHashSet<String> ipSet = new LinkedHashSet<>();
        for (InetAddress address : addressList) {
            ipSet.add(address.getHostAddress());
        }

        return ipSet;
    }

    /**
     * 获取所有满足过滤条件的本地IP地址对象
     *
     * @param addressFilter          过滤器，null表示不过滤，获取所有地址
     * @param networkInterfaceFilter 过滤器，null表示不过滤，获取所有网卡
     * @return 过滤后的地址对象列表
     */
    public static LinkedHashSet<InetAddress> localAddressList(Predicate<NetworkInterface> networkInterfaceFilter, Predicate<InetAddress> addressFilter) {
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new BusinessException("Get network interface error!");
        }

        if (networkInterfaces == null) {
            throw new BusinessException("Get network interface error!");
        }

        final LinkedHashSet<InetAddress> ipSet = new LinkedHashSet<>();

        while (networkInterfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterfaceFilter != null && false == networkInterfaceFilter.test(networkInterface)) {
                continue;
            }
            final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                final InetAddress inetAddress = inetAddresses.nextElement();
                if (inetAddress != null && (null == addressFilter || addressFilter.test(inetAddress))) {
                    ipSet.add(inetAddress);
                }
            }
        }

        return ipSet;
    }

    public static String getLocalhostStr() {
        InetAddress localhost = getLocalhost();
        if (null != localhost) {
            return localhost.getHostAddress();
        }
        return null;
    }

    public static InetAddress getLocalhost() {
        final LinkedHashSet<InetAddress> localAddressList = localAddressList(address -> {
            // 非loopback地址，指127.*.*.*的地址
            return false == address.isLoopbackAddress()
                    // 需为IPV4地址
                    && address instanceof Inet4Address;
        });

        if (ObjectUtils.isNotEmpty(localAddressList)) {
            InetAddress address2 = null;
            for (InetAddress inetAddress : localAddressList) {
                if (false == inetAddress.isSiteLocalAddress()) {
                    // 非地区本地地址，指10.0.0.0 ~ 10.255.255.255、172.16.0.0 ~ 172.31.255.255、192.168.0.0 ~ 192.168.255.255
                    return inetAddress;
                } else if (null == address2) {
                    address2 = inetAddress;
                }
            }

            if (null != address2) {
                return address2;
            }
        }

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // ignore
        }

        return null;
    }

    /**
     * 获取主机名称，一次获取会缓存名称<br>
     * 注意此方法会触发反向DNS解析，导致阻塞，阻塞时间取决于网络！
     *
     * @return 主机名称
     */
    public static String getLocalHostName() {
        if (StringUtils.isNotBlank(LOCALHOST_NAME)) {
            return LOCALHOST_NAME;
        }

        final InetAddress localhost = getLocalhost();

        if (null != localhost) {
            String name = localhost.getHostName();
            if (StringUtils.isEmpty(name)) {
                name = localhost.getHostAddress();
            }
            LOCALHOST_NAME = name;
        }

        return LOCALHOST_NAME;
    }

    public static String getMultistageReverseProxyIp(String ip) {
        // 多级反向代理检测
        if (ip != null && StringUtils.indexOf(ip, ',') > 0) {
            for (final String subIp : StringUtils.split(ip, ",")) {
                if (false == isUnknown(subIp)) {
                    ip = subIp;
                    break;
                }
            }
        }
        return ip;
    }

    public static boolean isOpen(InetSocketAddress address, int timeout) {
        try (Socket sc = new Socket()) {
            sc.connect(address, timeout);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean ping(String ip, int timeout) {
        try {
            return InetAddress.getByName(ip).isReachable(timeout); // 当返回值是true时，说明host是可用的，false则不可。
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean ping(String ip) {
        return ping(ip, 200);
    }
}
