package com.darkidiot.redis.exception;

public class TimeOutException extends RedisException {

	private static final long serialVersionUID = 1L;

	public TimeOutException() {
		super();
	}

	public TimeOutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TimeOutException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimeOutException(String message) {
		super(message);
	}

	public TimeOutException(Throwable cause) {
		super(cause);
	}

}
