package com.darkidiot.redis.lock;

import com.darkidiot.redis.exception.RedisException;

/**
 * Redis分布式锁<非阻塞式></><br>
 * <b>Notice(暂不实现):<b/><br/>
 * <ul>
 * <li>依赖jedisPool,使得每一个锁的操作都新从jedisPool中去获取jedis,消除了从加锁到释放锁之间长期占用jedis连接</li>
 * <li><strong>Notice:</strong> 对于同一个锁的实例而言,进程并发量支持数最大值为6,线程数支持MAX级别, 处理时间也会相应的延长 .</li>
 * </ul>
 * @author darkidiot
 */
public interface NonBlockingLock {

	/**
	 * 加锁
	 * @param lockTimeout 锁的超时时间(秒)(-1: for never expired)
	 * @return identifier
	 */
    void lock(long acquireTimeout, long lockTimeout) throws RedisException;

	/**
	 * 加锁(acquireTimeout:默认10秒,lockTimeout:默认50秒)
	 * @return identifier
	 */
    void lock() throws RedisException;

	/**
	 * 释放锁
	 * @return true|false : false is for unlock operation to release lock.
	 */
    boolean unlock() throws RedisException;

	/**
	 * 判断是否加锁成功
	 */
    boolean isLocking() throws RedisException;

	/**
	 * 获取当前锁的名称
	 */
    String getName() throws RedisException;
}
