package com.darkidiot.redis.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.darkidiot.redis.config.JedisPoolFactory;
import com.darkidiot.redis.lock.imp.RigorousRedisLock;
import com.darkidiot.redis.exception.RedisException;
import com.darkidiot.redis.lock.imp.SimpleRedisLock;
import com.darkidiot.redis.lock.imp.StrictRedisLock;

import redis.clients.jedis.JedisPool;

/**
 * Redis Lock 工厂类 
 * <ul>
 * <li>Notice: 采用享元模式， 同一个进程在多线程环境下去获取同名的锁总是返回单一实例.</li>
 * </ul>
 * @author darkidiot
 */
public class RedisLock {
	
	private static final String RIGOROUS_LOCK_PREFIX = "Rigorous Lock:";
	private static final String SIMPLE_LOCK_PREFIX = "Simple Lock:";
	private static final String STRICT_LOCK_PREFIX = "Strict Lock:";
	
	private static final Map<String,Lock> LockMap = new ConcurrentHashMap<>();
	
	private RedisLock(){}
	
	public static Lock useRigorousRedisLock(final String lockname) throws RedisException{
		return invoke(new Callback() {
			@Override
			public Lock call(JedisPool jedisPool) throws RedisException {
				return new RigorousRedisLock(jedisPool, lockname);
			}
		}, RIGOROUS_LOCK_PREFIX, lockname);
	}
	
	public static Lock useSimpleRedisLock(final String lockname) throws RedisException{
		return invoke(new Callback() {
			@Override
			public Lock call(JedisPool jedisPool) throws RedisException {
				return new SimpleRedisLock(jedisPool, lockname);
			}
		}, SIMPLE_LOCK_PREFIX, lockname);
	}
	
	public static Lock useStrictRedisLock(final String lockname) throws RedisException{
		return invoke(new Callback() {
			@Override
			public Lock call(JedisPool jedisPool) throws RedisException {
				return new StrictRedisLock(jedisPool, lockname);
			}
		}, STRICT_LOCK_PREFIX, lockname);
	}
	
	private static interface Callback {
		Lock call(JedisPool jedisPool) throws RedisException ;
	}
	
	private static Lock invoke(Callback callback,String prefix,String lockname) throws RedisException{
		String key = createKey(lockname, prefix);
		Lock lock = LockMap.get(key);
		if (lock == null) {
			lock = callback.call(JedisPoolFactory.getWritePool());
			LockMap.put(key, lock);
		}
		return lock;
	}
	
	private static String createKey(String lockname ,String prefix){
		return prefix + lockname;
	}
}
