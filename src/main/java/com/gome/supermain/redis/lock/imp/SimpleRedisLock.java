package com.gome.supermain.redis.lock.imp;

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
import redis.clients.jedis.exceptions.JedisException;
/**
 *  简单的分布式锁的实现,效率较高(极端情况下，会出现多个实例同时获取到锁的 情况)
 *  堵塞式
 * @author darkidiot
 */
@Slf4j
public class SimpleRedisLock implements Lock{
	
	private static final int MAX_SUPPORT_THREAD_COUNT = 3;

	JedisPool jedisPool;
	
	String name;
	
	Semaphore semaphore;
	
	public SimpleRedisLock(JedisPool jedisPool, String name) throws RedisException {
		if (jedisPool == null) {
			throw new RedisException("Initialize SimpleRedisLock failure, And jedisPool can not be null.");
		}
		if (StringUtil.isEmpty(name)) {
			throw new RedisException("Initialize SimpleRedisLock failure, And name can not be empty.");
		}
		this.jedisPool = jedisPool;
		this.name = name;
		this.semaphore = new Semaphore(MAX_SUPPORT_THREAD_COUNT,true);
	}

	@Override
	public String lock(final long acquireTimeout, final long lockTimeout) throws RedisException {
		if (acquireTimeout < 0 || lockTimeout < -1) {
			throw new RedisException("acquireTimeout can not be  negative Or LockTimeout can not be less than -1.");
		}
        final String lockKey = createKey(this.name);
        try {
        	semaphore.acquire();
    		return invoke(new Callback<String>() {
    			@Override
    			public String call(Jedis jedis) {
    				String value = UUIDUtil.generateShortUUID();
    				int lockExpire = (int) (lockTimeout);
    				long end = System.currentTimeMillis() + acquireTimeout;
    				int i = 1;
    				String identifier = "";
    				while (true) {
    					// 将rediskey的最大生存时刻存到redis里，过了这个时刻该锁会被自动释放
    					if (jedis.setnx(lockKey, value) == 1) {
    						//判断是否被其他实例拿到并改变value
    						String lockValue = jedis.get(lockKey);
							if (lockValue != null && lockValue.equals(value)) {
    							//进程crash在这里，然后再继续执行会导致多个实例同时获取到锁的混乱情况
    							jedis.expire(lockKey, lockExpire); 
    							identifier = value;
    							break;
    						}
    					}
    					
    					/** ttl为 -1 表示key上没有设置生存时间（key是不会不存在的，因为前面setnx自动创建）
    					 *  如果出现这种状况,那就是进程的某个实例setnx成功后 crash 导致紧跟着的expire没有被调用,这时可以直接设置expire并把锁纳为己用
    					 */
    					if (jedis.ttl(lockKey) == -1) {
    						jedis.expire(lockKey, lockExpire);
    						jedis.set(lockKey, value);   //将锁占为己用，并改变value;
    						identifier = value;
    						break;
    					}
    					
    					try {
    						Thread.sleep(defaultWaitIntervalInMSUnit * FibonacciUtil.circulationFibonacciNormal(i++));
    					} catch (InterruptedException ie) {
    						Thread.currentThread().interrupt();
    					}
    					
    					if (System.currentTimeMillis() > end) {
    						log.warn("Acquire SimpleRedisLock time out. spend[ {}ms ]", System.currentTimeMillis() - end);
    					}
    				}
    				return identifier;
    			}
    		}, jedisPool);
        	
        } catch (JedisException je) {
        	return null;
        } catch (InterruptedException e) {
        	return null;
		}finally {
        	semaphore.release();
		}
	}

	@Override
	public boolean unlock(final String identifier) throws RedisException {
		if (StringUtil.isEmpty(identifier)) {
			throw new RedisException("identifier can not be empty.");
		}
        final String lockKey = createKey(this.name);
        try {
    		return invoke(new Callback<Boolean>() {
    			@Override
    			public Boolean call(Jedis jedis) {
    				long end = System.currentTimeMillis() + defaultReleaseLockTimeout;
    				if (identifier.equals(jedis.get(lockKey))) {
    					jedis.del(lockKey);
    					if (System.currentTimeMillis() > end) {
    						log.warn("Release SimpleRedisLock time out. spend[ {}ms ]", System.currentTimeMillis() - end);
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
	public String lock() throws RedisException{
		return lock(defaultAcquireLockTimeout, defaultLockTimeout);
	}

	@Override
	public boolean isLocking(final String identifier) throws RedisException {
		if (StringUtil.isEmpty(identifier)) {
			throw new RedisException("identifier can not be empty.");
		}
		final String lockKey = createKey(this.name);
		try{
			return invoke(new Callback<Boolean>() {
				@Override
				public Boolean call(Jedis jedis) {
					String retStr = null;
					long end = System.currentTimeMillis() + defaultCheckLockTimeout;
					retStr = jedis.get(lockKey);
					if (System.currentTimeMillis() > end) {
						log.warn("Checking SimpleRedisLock time out. spend[ {}ms ]", System.currentTimeMillis() - end);
					}
					return retStr != null && retStr.equals(identifier);
				}
			}, jedisPool);
		} catch (JedisException je) {
	    	return false;
	    }
	}

	@Override
	public String getName() throws RedisException {
		return this.name;
	}
}