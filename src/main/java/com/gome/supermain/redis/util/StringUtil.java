package com.gome.supermain.redis.util;

/**
 * 字符串工具类
 * @author: darkidiot
 */
public final class StringUtil {

	private StringUtil(){}
	
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
	
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}
	
	public static String nullToEmpty(String str) {
		if(str == null)
			return "";
		return str;
	}
}
