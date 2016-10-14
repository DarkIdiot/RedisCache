package com.gome.supermain.redis;


import java.io.Serializable;

import com.gome.supermain.redis.config.JedisPoolFactory;
import com.gome.supermain.redis.jedis.IJedis;
import com.gome.supermain.redis.jedis.imp.Jedis;

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
		return new RedisMapProxy<K,V>(name,jedis);
	}
	
}
