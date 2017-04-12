package com.darkidiot.redis;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.util.ByteObjectConvertUtil;
import com.darkidiot.redis.util.StringUtil;
import com.darkidiot.redis.validate.KeyValidation;
import com.darkidiot.redis.validate.NopValidation;

import lombok.extern.slf4j.Slf4j;


/**
 * RedisMap缓存实现类
 * @author darkidiot
 */
@SuppressWarnings({"unchecked","unused"})
@Slf4j
public class RedisMap<K extends Serializable, V extends Serializable> implements IRedisMap<K, V> {
	private String name;
	private IJedis jedis;
	/**最多容忍50MS延迟*/
	private static final int WARN_TIME_LIMIT = 50;

	private final KeyValidation<K> nop_validation = new NopValidation<K>();
	
	private static boolean VALIDATION_FOR_REDIS_CACHE = true;
	
	public RedisMap(String name, IJedis jedis) {
		if (StringUtil.isEmpty(name)) {
			throw new IllegalArgumentException("the 'name' of RedisMap can not be empty.");
		}
		if (jedis == null) {
			throw new IllegalArgumentException("the 'jedis' of RedisMap can not be null.");
		}
		this.name = name;
		this.jedis = jedis;
	}

	@Override
	public void put(K key, V value) {
		put(key, value, nop_validation);
	}

	@Override
	public V get(K key) {
		return get(key, nop_validation);
	}
	
	@Override
	public List<V> getList(List<K> keys) {
		return getList(keys,nop_validation);
	}
	
	@Override
	public boolean contains(K key) {
		return contains(key,nop_validation);
	}
	
	@Override
	public void remove(K key) {
		remove(key,nop_validation);
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public int size() {
		try {
			Map<byte[], byte[]> map = jedis.hgetAll(ByteObjectConvertUtil.getBytesFromObject(getName()));
			return map == null ? 0 : map.size();
		} catch (Exception e) {
			return 0;
		}
	}
	
	@Override
	public void put(K key, V value, KeyValidation<K>... validations) {
		if (key == null) {
			return;
		}
		if(VALIDATION_FOR_REDIS_CACHE && keyValidations(key, validations)){
			return;
		}
		long time = System.currentTimeMillis();
		try {
			Long result = jedis.hset(ByteObjectConvertUtil.getBytesFromObject(getName()), ByteObjectConvertUtil.getBytesFromObject(key),
					ByteObjectConvertUtil.getBytesFromObject(value));
		} finally {
			long spend = System.currentTimeMillis() - time;
			if (spend > WARN_TIME_LIMIT) {
				log.warn("RedisMap[ {} ] put[ {} - {} ] spend[ {}ms ].", new Object[] { name, key, value, spend });
			}
		}
	}
	
	@Override
	public V get(K key, KeyValidation<K>... validations) {
		if (key == null) {
			return null;
		}
		
		if(VALIDATION_FOR_REDIS_CACHE && keyValidations(key, validations)){
			return null;
		}
		
		long time = System.currentTimeMillis();
		try {
			byte[] valueByte = jedis.hget(ByteObjectConvertUtil.getBytesFromObject(getName()), ByteObjectConvertUtil.getBytesFromObject(key));
			if (valueByte == null) {
				return null;
			}
			return (V) ByteObjectConvertUtil.getObjectFromBytes(valueByte);
		} finally {
			long spend = System.currentTimeMillis() - time;
			if (spend > WARN_TIME_LIMIT) {
				log.warn("RedisMap[ {} ] get[ {} ] spend[ {}ms ].", new Object[] { name, key, spend });
			}
		}
	}
	
	@Override
	public List<V> getList(List<K> keys, KeyValidation<K>... validations) {
		if (keys == null || keys.size() == 0) {
			return null;
		}
		
		if (VALIDATION_FOR_REDIS_CACHE) {
			for(int i =0;i< keys.size();i++){
				if (keyValidations(keys.get(i), validations)) {
					keys.remove(i);
				}
			}
		}
		
		if (keys.size() == 0) {
			return null;
		}
		
		long time = System.currentTimeMillis();
		try {
			return (List<V>) jedis.hmget(getName(), keys);
		} finally {
			long spend = System.currentTimeMillis() - time;
			if (spend > WARN_TIME_LIMIT) {
				log.warn("RedisMap[ {} ] getList[ {}... ] spend[ {}ms ].", new Object[] { name, keys.get(0), spend });
			}
		}
	}
	
	@Override
	public boolean contains(K key,  KeyValidation<K>... validations) {
		if (key == null) {
			return false;
		}
		
		if(VALIDATION_FOR_REDIS_CACHE && keyValidations(key, validations)){
			return false;
		}
		
		Boolean result = jedis.hexists(ByteObjectConvertUtil.getBytesFromObject(getName()), ByteObjectConvertUtil.getBytesFromObject(key));
		return result == null ? false : result;
	}
	
	@Override
	public void remove(K key, KeyValidation<K>... validations) {
		if (key == null) {
			return;
		}
		if(VALIDATION_FOR_REDIS_CACHE && keyValidations(key, validations)){
			return;
		}
		Long result = jedis.hdel(ByteObjectConvertUtil.getBytesFromObject(getName()), ByteObjectConvertUtil.getBytesFromObject(key));
	}
	
	@Override
	public void clear() {
		Long result = jedis.del(ByteObjectConvertUtil.getBytesFromObject(getName()));
	}
	
	private boolean keyValidations(K key, KeyValidation<K>... validations) {
		if (validations != null && validations.length != 0) {
			for (KeyValidation<K> keyValidation : validations) {
				if (!keyValidation.validate(key)) {
					return true;
				}
			}
		}
		return false;
	}
}
