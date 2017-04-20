package com.darkidiot.redis;


import java.io.Serializable;

import com.darkidiot.redis.config.JedisPoolFactory;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.jedis.imp.Jedis;
import org.springframework.stereotype.Component;

/**
 * Redis工厂类
 * @author darkidiot
 */
public class Redis {
	
	private Redis() {}
	
	/**
	 * 获取缓存接口.
	 * @param name(必传,每一块缓存的唯一标志)
	 * @return
	 */
	public static <K extends Serializable, V extends Serializable> IRedisMap<K, V> use(String name){
		IJedis jedis = new Jedis(JedisPoolFactory.getWritePool(), JedisPoolFactory.getReadPool());
		return new RedisMapProxy<>(name, jedis);
	}
	
}
