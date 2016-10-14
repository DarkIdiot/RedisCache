package com.gome.supermain.redis.validate;

/**
 *  Redis缓存Key校验接口
 * @author darkidiot
 */
public interface KeyValidation<K> {
	public boolean validate(K key);
}
