package com.gome.supermain.redis.lock;

import com.gome.supermain.redis.exception.RedisException;

/**
 * Redis分布式锁<br>
 * <li>依赖jedisPool,使得每一个锁的操作都新从jedisPool中去获取jedis,消除了从加锁到释放锁之间长期占用jedis连接</li>
 * <li><strong>Notice:</strong> 对于同一个锁的实例而言,进程并发量支持数最大值为6,线程数支持MAX级别, 处理时间也会相应的延长 .</li>
 * @author darkidiot
 */
public interface Lock {
	
	/**
	 * 加锁
	 * @param acquireTimeout 获取锁超时时间(秒)
	 * @param lockTimeout 锁的超时时间(秒)(-1: for never expired)
	 * @return identifier 
	 */
	public String lock(long acquireTimeout, long lockTimeout) throws RedisException;
	
	/**
	 * 加锁
	 * @param acquireTimeout 获取锁超时时间(秒)(默认10秒)
	 * @param lockTimeout 锁的超时时间(秒)(默认50秒)
	 * @return identifier 
	 */
	public String lock() throws RedisException;
	
	/**
	 * 释放锁
	 * @param identifier 释放锁标识
	 * @return true|false : false is for unlock operation to release lock. 
	 */
	public boolean unlock(String identifier) throws RedisException;
	
	/**
	 * 判断是否加锁成功 
	 * @param lockName
	 */
	public boolean isLocking(String identifier) throws RedisException;
	
	/**
	 * 获取当前锁的名称
	 */
	public String getName() throws RedisException;
}
