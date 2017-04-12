package com.darkidiot.redis.util;

/**
 *  数字工具类
 * @author darkidiot
 */
public class NumberUtil {

	private NumberUtil(){}
	
	/**
	 * 判断是否是奇数
	 * @param number
	 * @return
	 */
	public static boolean isOdd(Number number){
		return number.intValue() % 2 == 1;
	}
	
	/**
	 * 判断是否是偶数
	 * @param number
	 * @return
	 */
	public static boolean isEven(Number number){
		return !isOdd(number);
	}
}
