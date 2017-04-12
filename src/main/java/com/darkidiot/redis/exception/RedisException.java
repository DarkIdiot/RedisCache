package com.darkidiot.redis.exception;

public class RedisException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RedisException() {
		super();
	}

	public RedisException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RedisException(String message, Throwable cause) {
		super(message, cause);
	}

	public RedisException(String message) {
		super(message);
	}

	public RedisException(Throwable cause) {
		super(cause);
	}
}
