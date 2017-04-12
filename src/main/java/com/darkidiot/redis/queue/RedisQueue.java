package com.darkidiot.redis.queue;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.darkidiot.redis.config.JedisPoolFactory;
import com.darkidiot.redis.exception.RedisException;
import com.darkidiot.redis.queue.impl.PerfectPriorityQueue;
import com.darkidiot.redis.queue.impl.RoughPriorityQueue;
import com.darkidiot.redis.queue.impl.SimpleFifoQueue;
import com.darkidiot.redis.queue.impl.SimplePriorityQueue;

import redis.clients.jedis.JedisPool;

/**
 * Redis Queue 工厂类
 * <ul>
 * <li>Notice: 采用享元模式， 同一个进程在多线程环境下去获取同名的锁总是返回单一实例.</li>
 * </ul>
 * 
 * @author darkidiot
 */
public class RedisQueue {
	private static final String PERFECT_PRIORITY_QUEUE_PREFIX = "Perfect Priority Queue:";
	private static final String ROUGH_PRIORITY_QUEUE_PREFIX = "Rough Priority Queue:";
	private static final String SIMPLE_PRIORITY_QUEUE_PREFIX = "Simple Priority Queue:";
	private static final String SIMPLE_FIFO_QUEUE_PREFIX = "Simple Fifo Queue:";

	private static final Map<String, Queue<? extends Serializable>> QueueMap = new ConcurrentHashMap<>();

	private RedisQueue() {}

	public static <T extends Serializable> Queue<T> usePerfectPriorityQueue(final String queuename) throws RedisException {
		return invoke(new Callback<T>(){
			@Override
			public Queue<T> call(JedisPool jedisPool) throws RedisException {
				return new PerfectPriorityQueue<T>(queuename, jedisPool);
			}
		}, PERFECT_PRIORITY_QUEUE_PREFIX, queuename);
	}

	public static <T extends Serializable> Queue<T> useRoughPriorityQueue(final String queuename) throws RedisException {
		return invoke(new Callback<T>() {
			@Override
			public Queue<T> call(JedisPool jedisPool) throws RedisException {
				return new RoughPriorityQueue<T>(queuename, jedisPool);
			}
		}, ROUGH_PRIORITY_QUEUE_PREFIX, queuename);
	}

	public static <T extends Serializable> Queue<T> useSimplePriorityQueue(final String queuename) throws RedisException {
		return invoke(new Callback<T>() {
			@Override
			public Queue<T> call(JedisPool jedisPool) throws RedisException {
				return new SimplePriorityQueue<T>(queuename, jedisPool);
			}
		}, SIMPLE_PRIORITY_QUEUE_PREFIX, queuename);
	}

	public static <T extends Serializable> Queue<T> useSimpleFifoQueue(final String queuename) throws RedisException {
		return invoke(new Callback<T>() {
			@Override
			public Queue<T> call(JedisPool jedisPool) throws RedisException {
				return new SimpleFifoQueue<T>(queuename, jedisPool);
			}
		}, SIMPLE_FIFO_QUEUE_PREFIX, queuename);
	}

	private static interface Callback<T extends Serializable>{
		 Queue<T> call(JedisPool jedisPool) throws RedisException;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Serializable> Queue<T> invoke(Callback<T> callback, String prefix, String lockname) throws RedisException {
		String key = createKey(lockname, prefix);
		Queue<T> queue = (Queue<T>) QueueMap.get(key);
		if (queue == null) {
			queue = callback.call(JedisPoolFactory.getWritePool());
			QueueMap.put(key, queue);
		}
		return queue;
	}

	private static String createKey(String queuename, String prefix) {
		return prefix + queuename;
	}
}
