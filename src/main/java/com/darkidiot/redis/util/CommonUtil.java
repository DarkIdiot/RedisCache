package com.darkidiot.redis.util;

import redis.clients.jedis.JedisPool;

/**
 * Redis 公用Util
 * 
 * @author darkidiot
 */
public class CommonUtil {
	
	private CommonUtil() {}
	
	public static <T> T invoke(Callback<T> call,JedisPool jedisPool) {
		redis.clients.jedis.Jedis jedis = jedisPool.getResource();
		try {
			return call.call(jedis);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}

	public static interface Callback<T> {
		T call(redis.clients.jedis.Jedis jedis);
	}
}
