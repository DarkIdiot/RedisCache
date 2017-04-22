package com.darkidiot.redis.config;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IPorServerConfig {

    private static String getIP() {
        ArrayList<String> addressList = Lists.newArrayList();
        try {
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                if (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (inetAddress != null && inetAddress.getHostAddress() != null) {
                        addressList.add(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            log.error("get the IP error, cause by:{}", Throwables.getStackTraceAsString(e));
        }
        return addressList.toString();
    }

    private static String getServerName() {
        return JedisPoolFactory.getInitParam().getServerName();
    }

    public static String getServerId() {
        return getServerName() + "-" + getIP();
    }

    public static void main(String[] args) {
        log.info("My computer's ip is:{}", getIP());
    }
}
