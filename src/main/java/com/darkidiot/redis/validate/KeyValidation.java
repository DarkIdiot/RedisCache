package com.darkidiot.redis.validate;

/**
 *  Redis缓存Key校验接口
 * @author darkidiot
 */
public interface KeyValidation<K> {
	public boolean validate(K key);
}
