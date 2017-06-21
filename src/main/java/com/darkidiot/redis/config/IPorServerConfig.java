package com.darkidiot.redis.config;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;

import static com.darkidiot.redis.config.RedisPropertyConstants.PKEY_TIMEOUT_IN_MILLIS;

@Slf4j
public class IPorServerConfig {

    private static String getIP() {
        String ip = "";
        try {
            InetAddress address = InetAddress.getLocalHost();
            ip = address.getHostAddress();
        } catch (Exception e) {
            log.error("get the IP error, cause by:{}", Throwables.getStackTraceAsString(e));
        }
        return ip;
    }

    //获取MAC地址
    private static String getMac() {
        byte[] mac = new byte[0];
        try {
            InetAddress ia = InetAddress.getLocalHost();
            mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        } catch (Exception e) {
            log.error("get the mac address error, cause by:{}", Throwables.getStackTraceAsString(e));
        }
        StringBuffer sb = new StringBuffer(32);
        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            //字节转换为整数
            int temp = mac[i] & 0xff;
            String str = Integer.toHexString(temp);
            if (str.length() == 1) {
                sb.append("0").append(str);
            } else {
                sb.append(str);
            }
        }
        return sb.toString().toUpperCase();
    }

    //获取进程号
    private static String getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return name.split("@")[0];
    }

    //获取线程号
    private static String getTid() {
        return Thread.currentThread().getName();
    }

    private static String getServerName(String service) {
        return JedisPoolFactory.getInitParam(service).getServerName();
    }

    public static String getServerId(String service) {
        String serverId = getServerName(service) + "-" + getIP();
        log.debug("the server id is: [{}]", serverId);
        return serverId;
    }

    public static String getThreadId() {
        String template  = "%s:%s:%s";
        return String.format(template, getMac(), getPid(), getTid());
    }

    public static void main(String[] args) {
        log.info("My computer's ip is:{}" , getIP());
        log.warn("My computer's pid is:{}" , getPid());
        log.error("My computer's tid is:{}" , getTid());
        log.info("My computer's mac is:{}" , getMac());
    }
}
