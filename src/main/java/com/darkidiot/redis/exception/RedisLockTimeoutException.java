package com.darkidiot.redis.exception;

public class RedisLockTimeoutException extends TimeOutException {

	private static final long serialVersionUID = 1L;

	public RedisLockTimeoutException() {
		super();
	}

	public RedisLockTimeoutException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RedisLockTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public RedisLockTimeoutException(String message) {
		super(message);
	}

	public RedisLockTimeoutException(Throwable cause) {
		super(cause);
	}

}
