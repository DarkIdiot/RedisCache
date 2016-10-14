package com.gome.supermain.redis.queue.impl;

import static com.gome.supermain.redis.queue.impl.Constants.createKey;
import static com.gome.supermain.redis.queue.impl.Constants.defaultClearQueueTimeout;
import static com.gome.supermain.redis.queue.impl.Constants.defaultDequeueTimeout;
import static com.gome.supermain.redis.queue.impl.Constants.defaultEnqueueTimeout;
import static com.gome.supermain.redis.queue.impl.Constants.defaultQueryQueueSzieTimeout;
import static com.gome.supermain.redis.queue.impl.Constants.defaultTopQueueTimeout;
import static com.gome.supermain.redis.util.CommonUtil.invoke;

import java.io.Serializable;
import java.util.List;

import com.gome.supermain.redis.exception.RedisException;
import com.gome.supermain.redis.queue.Queue;
import com.gome.supermain.redis.util.ByteObjectConvertUtil;
import com.gome.supermain.redis.util.CommonUtil.Callback;
import com.gome.supermain.redis.util.StringUtil;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

/**
 * 简单先进先出队列(采用单队列模式实现,不支持优先级)
 * 
 * @author darkidiot
 */
@Slf4j
@SuppressWarnings("unchecked")
public class SimpleFifoQueue<T extends Serializable> implements Queue<T> {

	String name;
	JedisPool jedisPool;

	public SimpleFifoQueue(String name, JedisPool jedisPool) throws RedisException {
		if (jedisPool == null) {
			throw new RedisException("Initialize SimpleFifoQueue failure, And jedisPool can not be null.");
		}
		if (StringUtil.isEmpty(name)) {
			throw new RedisException("Initialize SimpleFifoQueue failure, And name can not be empty.");
		}
		this.name = name;
		this.jedisPool = jedisPool;
	}

	@Override
	public boolean enqueue(final T... members) throws RedisException {
		if (members == null || members.length == 0) {
			return false;
		}
		try {
			return invoke(new Callback<Boolean>() {
				@Override
				public Boolean call(Jedis jedis) {
					long end = System.currentTimeMillis() + defaultEnqueueTimeout;
					long retNum = jedis.lpush(createKey(name).getBytes(), ByteObjectConvertUtil.getBytesFromObject(members));
					if (System.currentTimeMillis() > end) {
						log.warn("Enqueue SimpleFifoQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
					}
					return retNum > 0;	
			}}, jedisPool);
		} catch (JedisException jedisException) {
			return false;
		}
	}

	@Override
	@Deprecated
	public boolean enqueue(int priority, T... members) throws RedisException {
		throw new RedisException("Ohh,the instance of Queue is SimpleFifoQueue this moment, and can not support this method.");
	}

	@Override
	public T dequeue() throws RedisException {
		try {
			return invoke(new Callback<T>() {
				@Override
				public T call(Jedis jedis) {
					long end = System.currentTimeMillis() + defaultDequeueTimeout;
					List<String> retList = jedis.brpop(createKey(name),String.valueOf(0));
					if (System.currentTimeMillis() > end) {
						log.warn("Dequeue SimpleFifoQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
					}
					String retStr = retList.get(1);
					if (StringUtil.isNotEmpty(retStr)) {
						Object[] objects = (Object[]) ByteObjectConvertUtil.getObjectFromBytes(retStr.getBytes());
						return (T) objects[0];
					}
					return null;
			}}, jedisPool);
		} catch (JedisException jedisException) {
			return null;
		} 
	}

	@Override
	public T top() throws RedisException {
		try {
			return invoke(new Callback<T>() {
				@Override
				public T call(Jedis jedis) {
					long end = System.currentTimeMillis() + defaultTopQueueTimeout;
					String top = jedis.lindex(createKey(name), -1);
					if (System.currentTimeMillis() > end) {
						log.warn("Top SimpleFifoQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
					}
					if (StringUtil.isNotEmpty(top)) {
						Object[] objectFromBytes = (Object[]) ByteObjectConvertUtil.getObjectFromBytes(top.getBytes());
						return (T) objectFromBytes[0];
					}
					return null;
			}}, jedisPool);
		} catch (JedisException jedisException) {
			return null;
		}
	}

	@Override
	public long size() throws RedisException {
		try {
			return invoke(new Callback<Long>() {
				@Override
				public Long call(Jedis jedis) {
					long end = System.currentTimeMillis() + defaultQueryQueueSzieTimeout;
					long size = jedis.llen(createKey(name));
					if (System.currentTimeMillis() > end) {
						log.warn("Query SimpleFifoQueue size time out. spend[ {}ms ]", System.currentTimeMillis() - end);
					}
					return size;
			}}, jedisPool);
		} catch (JedisException jedisException) {
			return -1L;
		}
	}

	@Override
	public boolean isEmpty() throws RedisException {
		return this.size() == 0 ? true : false;
	}

	@Override
	public String getName() throws RedisException {
		return this.name;
	}

	@Override
	public boolean clear() throws RedisException {
		try{
			return invoke(new Callback<Boolean>() {
				@Override
				public Boolean call(Jedis jedis) {
					long end = System.currentTimeMillis() + defaultClearQueueTimeout;
					Long delNum = jedis.del(createKey(name));
					if (System.currentTimeMillis() > end) {
						log.warn("Clear SimpleFifoQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
					}
					return delNum == 1 || delNum == 0;
			}}, jedisPool);
		} catch (JedisException jedisException) {
			return false;
		}
	}
}