package com.gome.supermain.redis.lock.imp;

import static com.gome.supermain.redis.lock.imp.Constants.LOCK_UNLOCK;
import static com.gome.supermain.redis.lock.imp.Constants.createKey;
import static com.gome.supermain.redis.lock.imp.Constants.defaultAcquireLockTimeout;
import static com.gome.supermain.redis.lock.imp.Constants.defaultCheckLockTimeout;
import static com.gome.supermain.redis.lock.imp.Constants.defaultLockTimeout;
import static com.gome.supermain.redis.lock.imp.Constants.defaultReleaseLockTimeout;
import static com.gome.supermain.redis.lock.imp.Constants.defaultWaitIntervalInMSUnit;
import static com.gome.supermain.redis.util.CommonUtil.invoke;

import java.util.concurrent.Semaphore;

import com.gome.supermain.redis.exception.RedisException;
import com.gome.supermain.redis.lock.Lock;
import com.gome.supermain.redis.util.CommonUtil.Callback;
import com.gome.supermain.redis.util.FibonacciUtil;
import com.gome.supermain.redis.util.StringUtil;
import com.gome.supermain.redis.util.UUIDUtil;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;
/**
 *  严格的分布式锁的实现(超级超级严格)
 * @author darkidiot
 */
@Slf4j
public class RigorousRedisLock implements Lock {
	
	private static int MAX_SUPPORT_THREAD_COUNT = 5;
	
	JedisPool jedisPool;
	String name;
	Semaphore semaphore;
	public RigorousRedisLock(JedisPool jedisPool,String name) throws RedisException {
		if (jedisPool == null) {
			throw new RedisException("Initialize RigorousRedisLock failure, And jedisPool can not be null.");
		}
		if (StringUtil.isEmpty(name)) {
			throw new RedisException("Initialize RigorousRedisLock failure, And name can not be empty.");
		}
		this.jedisPool = jedisPool;
		this.name = name;
		this.semaphore = new Semaphore(MAX_SUPPORT_THREAD_COUNT,true);
	}

	@Override
	public String lock(final long acquireTimeout,final long lockTimeout) throws RedisException {
		if (acquireTimeout < 0 || lockTimeout < -1) {
			throw new RedisException("acquireTimeout can not be  negative Or LockTimeout can not be less than -1.");
		}
		final String value = UUIDUtil.generateShortUUID();
		final String lockKey = createKey(this.name);
		final int lockExpire = (int) (lockTimeout);
        try {
        	semaphore.acquire();
        	return invoke(new Callback<String>() {
        		@Override
        		public String call(Jedis jedis) {
                    long end = System.currentTimeMillis() + acquireTimeout;
                    int i = 1;
                    while (true) {
                        jedis.watch(lockKey);
                        // 开启watch之后，如果key的值被修改，则事务失败，exec方法返回null
                        String retStr = jedis.get(lockKey);
                        // 多个进程同时获取到未上锁状态时,进入事务上锁,第一个事务执行成功之后所有操作都会被取消并进入等待.
                        if (retStr == null || retStr.equals(LOCK_UNLOCK)) {
                            Transaction t = jedis.multi();
                            t.setex(lockKey, lockExpire, value);
                            if (t.exec() != null) {
                                return value;
                            }
                        }
                        jedis.unwatch();
                        try {
                            Thread.sleep(defaultWaitIntervalInMSUnit * FibonacciUtil.circulationFibonacciNormal(i++));
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        if (System.currentTimeMillis() > end) {
                        	log.warn("Acquire RigorousRedisLock time out. spend[ {}ms ]", System.currentTimeMillis() - end);
        				}
                    }
        		}
        	}, jedisPool);
        } catch (JedisException e) {
        	return null;
        } catch (InterruptedException e) {
        	return null;
		}finally {
			semaphore.release();
		}
	}

	@Override
	public String lock() throws RedisException {
		return lock(defaultAcquireLockTimeout, defaultLockTimeout);
	}

	@Override
	public boolean unlock(final String identifier) throws RedisException {
		if (StringUtil.isEmpty(identifier)) {
			throw new RedisException("identifier can not be empty.");
		}
		final String lockKey = createKey(this.name);
		try{
			return invoke(new Callback<Boolean>() {
				@Override
				public Boolean call(Jedis jedis) {
					long end = System.currentTimeMillis() + defaultReleaseLockTimeout;
					if (identifier.equals(jedis.getSet(lockKey,LOCK_UNLOCK))) {
						if (System.currentTimeMillis() > end) {
							log.warn("Release RigorousRedisLock time out. spend[ {}ms ]", System.currentTimeMillis() - end);
						}
						return true;
					}
					return false;
				}
			}, jedisPool);
        } catch (JedisException je) {
        	return false;
        }
	}

	@Override
	public boolean isLocking(String identifier) throws RedisException {
		if (StringUtil.isEmpty(identifier)) {
			throw new RedisException("identifier can not be empty.");
		}
		final String lockKey = createKey(this.name);
		try{
			return invoke(new Callback<Boolean>() {
				@Override
				public Boolean call(Jedis jedis) {
					long end = System.currentTimeMillis() + defaultCheckLockTimeout;
					String retStr = null;
					retStr = jedis.get(lockKey);
					if (System.currentTimeMillis() > end) {
						log.warn("Checking RigorousRedisLock time out. spend[ {}ms ]", System.currentTimeMillis() - end);
					}
					return retStr != null && !retStr.equals(LOCK_UNLOCK);
				}
			}, jedisPool);
		} catch (JedisException je) {
	    	return false;
	    }
	}
	
	@Override
	public String getName() throws RedisException {
		return name;
	}
}