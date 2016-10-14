package com.gome.supermain.redis.validate;

public class NopValidation<K> implements KeyValidation<K> {

	@Override
	public boolean validate(K key) {
		return true;
	}
}
