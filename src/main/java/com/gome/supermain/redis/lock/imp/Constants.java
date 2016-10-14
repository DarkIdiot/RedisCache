package com.gome.supermain.redis.lock.imp;


public class Constants {
	static final String LOCK_PREFIX = "Lock:";
	
	static final String LOCK_UNLOCK = "Lock:unlock";

	/** 默认获取锁超时时间 (超时会打印警告日志) */
	static final long defaultAcquireLockTimeout = 5*1000L;
	/** 默认释放锁超时时间 (超时会打印警告日志) */
	static final long defaultReleaseLockTimeout = 3*1000L;
	/** 默认检查锁超时时间 (超时会打印警告日志) */
	static final long defaultCheckLockTimeout = 3*1000L;
	
	
	/** 默认锁超时时间 (超时会自动释放锁) */
	static final long defaultLockTimeout = 5 * 60L;
	/** 获取锁失败后挂起再试的时间间隔单位(实际按斐波那契数列递增) */
	static final long defaultWaitIntervalInMSUnit = 5L;
	
	static String createKey(String lockName) {
		return LOCK_PREFIX + lockName;
	}
}
