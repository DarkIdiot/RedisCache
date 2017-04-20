package com.darkidiot.redis.config;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

import com.google.common.collect.Lists;

public class IPorServerConfig {

	private static String getIP() {
		try {
			ArrayList<String> addressList = Lists.newArrayList();
			for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements();) {
				NetworkInterface networkInterface = interfaces.nextElement();
				if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
					continue;
				}
				Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
				if (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (addr != null && addr.getHostAddress() != null) {
                        addressList.add(addr.getHostAddress());
					}
				}
			}
			return addressList.toString();
		} catch (Exception e) {
			return null;
		}
	}
	
	private static String getServerName() {
		return JedisPoolFactory.getInitParam().getServerName();
	}
	
	public static String getServerId(){
		return getIP()+"-"+getServerName();
	}
}
