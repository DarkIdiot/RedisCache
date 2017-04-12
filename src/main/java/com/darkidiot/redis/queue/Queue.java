package com.darkidiot.redis.queue;

import java.io.Serializable;

import com.darkidiot.redis.exception.RedisException;

/**
 * Redis分布式队列(支持优先级)
 * @author darkidiot
 */
public interface Queue<T extends Serializable> {
	
	/**
	 * 入队(默认最低优先级)
	 * @param members
	 * @return
	 * @throws RedisException
	 */
	public  boolean enqueue(@SuppressWarnings("unchecked") T... members) throws RedisException;
	
	/**
	 * 入队(所有成员必须都是指定优先级)
	 * @param priority
	 * @param members
	 * @return
	 * @throws RedisException
	 */
	public boolean enqueue(int priority, @SuppressWarnings("unchecked") T... members) throws RedisException;
	
	/**
	 * 出队(高优先级的先出 ,同一优先级的先进先出)
	 * @return
	 * @throws RedisException
	 */
	public T dequeue() throws RedisException;
	
	/**
	 * 返回队列第一位元素,但并不出队
	 * @return
	 * @throws RedisException
	 */
	public T top() throws RedisException;

	/**
	 * 返回队列的长度
	 * @return
	 * @throws RedisException
	 */
	public long size() throws RedisException;
	
	/**
	 * 判断队列是否为空
	 * @return
	 * @throws RedisException
	 */
	public boolean isEmpty() throws RedisException;
	
	/**
	 * 返回队列的名字
	 * @return
	 * @throws RedisException
	 */
	public String getName() throws RedisException;

	/**
	 * 清空队列
	 * @return
	 * @throws RedisException
	 */
	public boolean clear() throws RedisException;
}
