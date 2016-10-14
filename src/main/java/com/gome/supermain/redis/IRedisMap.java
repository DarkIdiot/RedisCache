package com.gome.supermain.redis;

import java.io.Serializable;
import java.util.List;

import com.gome.supermain.redis.validate.KeyValidation;


/**
 * Redis-map类型缓存抽象接口
 * @author darkidiot
 */
@SuppressWarnings("unchecked")
public interface IRedisMap<K extends Serializable, V extends Serializable> {

	/**
	 * 简单key，value存储
	 * @param key
	 * @param value
	 * @return
	 */
	void put(K key, V value);
	
	/**
	 * 简单key，value存储
	 * @param key
	 * @param value
	 * @param validations key的验证策略
	 * @return
	 */
	void put(K key, V value, KeyValidation<K>... validations);
	
	/**
	 * 获取缓存中的值
	 * @param key
	 * @return
	 */
	V get(K key);
	
	/**
	 * 获取缓存中的值
	 * @param key
	 * @param validations key的验证策略
	 * @return
	 */
	V get(K key, KeyValidation<K>... validations);

	/**
	 * 批量获取缓存中的值
	 * @param keys
	 * @return
	 */
	List<V> getList(List<K> keys);
	
	/**
	 * 批量获取缓存中的值
	 * @param keys
	 * @param validations key的验证策略
	 * @return
	 */
	List<V> getList(List<K> keys, KeyValidation<K>... validations);

	/**
	 * 从缓存中移除指定key值
	 * @param key
	 */
	void remove(K key);
	
	/**
	 * 从缓存中移除指定key值
	 * @param key
	 * @param validations key的验证策略
	 */
	void remove(K key, KeyValidation<K>... validations);

	/**
	 * 是否包含某个指定的key值
	 * @param key
	 * @return
	 */
	boolean contains(K key);
	
	/**
	 * 是否包含某个指定的key值
	 * @param key
	 * @param validations key的验证策略
	 * @return
	 */
	boolean contains(K key, KeyValidation<K>... validations);

	/**
	 * 当前缓存大小
	 * @return
	 */
	int size();

	/**
	 * 获取当前map的名称
	 * @return
	 */
	String getName();

	/**
	 * 清空map
	 */
	void clear();
}
