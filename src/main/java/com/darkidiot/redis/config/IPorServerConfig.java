package com.darkidiot.redis.config;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

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
        log.debug("the IP List:{}", addressList);
        return addressList.toString();
    }

    private static String getServerName(String service) {
        return JedisPoolFactory.getInitParam(service).getServerName();
    }

    public static String getServerId(String service) {
        String serverId = getServerName(service) + "-" + getIP();
        log.debug("the server id is:[{}]", serverId);
        return serverId;
    }

    public static void main(String[] args) {
        log.info("My computer's ip is:{}", getIP());
    }
}
