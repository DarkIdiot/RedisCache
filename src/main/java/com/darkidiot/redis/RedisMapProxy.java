package com.darkidiot.redis;

import java.io.Serializable;
import java.util.List;

import com.darkidiot.redis.common.Method;
import com.darkidiot.redis.config.JedisPoolFactory;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.subpub.LocalCaheSynchrinizedCenter;
import com.darkidiot.redis.validate.KeyValidation;
import com.darkidiot.redis.validate.NopValidation;

/**
 * Redis 缓存代理类(封装本地缓存操作)
 * @author darkidiot
 */
@SuppressWarnings("unchecked")
public class RedisMapProxy<K extends Serializable,V extends Serializable> implements IRedisMap<K, V>{
	
	IRedisMap<K, V> redisCache;
	IRedisMap<K, V> localCache;
	/** 开启本地缓存标示 */
	private boolean openLocalCacheFlag;
	/** 本地缓存过期时间(单位:秒) */
	private static final int expire = 3600; 
	
	private final KeyValidation<K> nop_validation = new NopValidation<K>();
	
	public RedisMapProxy(String name, IJedis jedis, int localCacheExpire) {
		this.redisCache = new RedisMap<K,V>(name, jedis);
		openLocalCacheFlag = JedisPoolFactory.getInitParam().getOpenLocalCache();
		if (openLocalCacheFlag) {
			this.localCache = new LocalMap<K,V>(name, localCacheExpire);
			LocalCaheSynchrinizedCenter.subscribe((LocalMap<String, ? extends Serializable>) localCache);
		}
	}
	
	public RedisMapProxy(String name, IJedis jedis) {
		this.redisCache = new RedisMap<K,V>(name, jedis);
		openLocalCacheFlag = JedisPoolFactory.getInitParam().getOpenLocalCache();
		if (openLocalCacheFlag) {
			this.localCache = new LocalMap<K,V>(name, expire);
			LocalCaheSynchrinizedCenter.subscribe((LocalMap<String, ? extends Serializable>) localCache);
		}
	}

	@Override
	public void put(K key, V value) {
		this.put(key, value, nop_validation);
	}

	@Override
	public void put(K key, V value, KeyValidation<K>... validations) {
		if (openLocalCacheFlag) {
			localCache.put(key, value, validations);
			LocalCaheSynchrinizedCenter.publish(redisCache.getName(), Method.put, key);
		}
		redisCache.put(key, value, validations);
	}

	@Override
	public V get(K key) {
		return get(key,nop_validation);
	}

	@Override
	public V get(K key, KeyValidation<K>... validations) {
		if (openLocalCacheFlag) {
			V v = localCache.get(key,validations);
			if (v != null) {
				return v;
			}
		}
		return redisCache.get(key,validations);
	}

	@Override
	public List<V> getList(List<K> keys) {
		return getList(keys,nop_validation);
	}

	@Override
	public List<V> getList(List<K> keys, KeyValidation<K>... validations) {
		if (openLocalCacheFlag) {
			List<V> list = localCache.getList(keys,validations);
			if (list != null && list.size() != 0) {
				return list;
			}
		}
		return redisCache.getList(keys, validations);
	}

	@Override
	public void remove(K key) {
		remove(key, nop_validation);
	}

	@Override
	public void remove(K key, KeyValidation<K>... validations) {
		if (openLocalCacheFlag) {
			localCache.remove(key,validations);
			LocalCaheSynchrinizedCenter.publish(getName(), Method.remove, key);
		}
		redisCache.remove(key, validations);
	}

	@Override
	public boolean contains(K key) {
		return contains(key, nop_validation);
	}

	@Override
	public boolean contains(K key, KeyValidation<K>... validations) {
		if (openLocalCacheFlag) {
			boolean contains = localCache.contains(key, validations);
			if (contains) {
				return contains;
			}
		}
		return redisCache.contains(key,validations);
	}

	@Override
	public int size() {
		return redisCache.size();
	}

	@Override
	public String getName() {
		return this.redisCache.getName();
	}

	@Override
	public void clear() {
		if (openLocalCacheFlag) {
			localCache.clear();
			LocalCaheSynchrinizedCenter.publish(getName(), Method.remove, "");
		}
		redisCache.clear();
	}
}
