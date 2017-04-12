package com.darkidiot.redis.exception;

public class AcquireLockTimeoutException extends TimeOutException {

	private static final long serialVersionUID = 1L;

	public AcquireLockTimeoutException() {
		super();
	}

	public AcquireLockTimeoutException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AcquireLockTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public AcquireLockTimeoutException(String message) {
		super(message);
	}

	public AcquireLockTimeoutException(Throwable cause) {
		super(cause);
	}

}
