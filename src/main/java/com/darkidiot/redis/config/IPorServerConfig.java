package com.darkidiot.redis.config;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;

import com.google.common.base.Throwables;

import lombok.extern.slf4j.Slf4j;

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
        String macStr = sb.toString().toUpperCase();
        log.debug("the mac address is : {}");
        return macStr;
    }

    //获取进程号
    private static String getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];
        log.debug("the pid is : {}", pid);
        return pid;
    }

    //获取线程号
    private static String getTid() {
        return Thread.currentThread().getId() + "";
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
        return getMac() + ":" + getPid() + ":" + getTid();
    }

    public static void main(String[] args) {
        System.out.println("My computer's ip is:{}"+ getIP());
        System.out.println("My computer's pid is:{}"+ getPid());
        System.out.println("My computer's tid is:{}"+ getTid());
        System.out.println("My computer's mac is:{}"+ getMac());
    }
}
