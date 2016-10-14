package com.gome.supermain.redis.queue.impl;

import static com.gome.supermain.redis.queue.impl.Constants.createKey;
import static com.gome.supermain.redis.queue.impl.Constants.createKeyByAsterisk;
import static com.gome.supermain.redis.queue.impl.Constants.defaultClearQueueTimeout;
import static com.gome.supermain.redis.queue.impl.Constants.defaultDequeueTimeout;
import static com.gome.supermain.redis.queue.impl.Constants.defaultEnqueueTimeout;
import static com.gome.supermain.redis.queue.impl.Constants.defaultQueryQueueSzieTimeout;
import static com.gome.supermain.redis.queue.impl.Constants.defaultTopQueueTimeout;
import static com.gome.supermain.redis.queue.impl.Constants.queueNameSequenceSeparator;
import static com.gome.supermain.redis.util.CommonUtil.invoke;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.gome.supermain.redis.exception.RedisException;
import com.gome.supermain.redis.queue.Queue;
import com.gome.supermain.redis.util.ByteObjectConvertUtil;
import com.gome.supermain.redis.util.CommonUtil.Callback;
import com.gome.supermain.redis.util.NumberUtil;
import com.gome.supermain.redis.util.StringUtil;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;
/**
 * 粗糙的多优先级支持队列(采用多个队列,每个队列容纳2个优先级;优先级为偶数,先进后出;优先级为奇数,先进先出;同一队列,优先级为偶数总是比奇数先出)
 * @author darkidiot
 */
@Slf4j
public class RoughPriorityQueue<T extends Serializable> implements Queue<T> {

	String name;
	JedisPool jedisPool;
	
	private TreeSet<String> queueNames = new TreeSet<String>(new Comparator<String>(){
		@Override
		public int compare(String s1, String s2) {
			Integer s1Number = Integer.parseInt(s1.substring(s1.lastIndexOf(queueNameSequenceSeparator) + 1, s1.length()));
			Integer s2Number = Integer.parseInt(s2.substring(s1.lastIndexOf(queueNameSequenceSeparator) + 1, s1.length()));
			if (s1Number > s2Number) {
				return -1;
			} else if (s1Number < s2Number){
				return 1;
			}else{
				return 0;	
			}
		}
	});

	public RoughPriorityQueue(String name, JedisPool jedisPool) throws RedisException {
		if (jedisPool == null) {
			throw new RedisException("Initialize RoughPriorityQueue failure, And jedisPool can not be null.");
		}
		if (StringUtil.isEmpty(name)) {
			throw new RedisException("Initialize RoughPriorityQueue failure, And name can not be empty.");
		}
		this.name = name;
		this.jedisPool = jedisPool;
		queueNames.add(createKey(name, Integer.MIN_VALUE * 2));
	}

	@Override
	public boolean enqueue(@SuppressWarnings("unchecked") final T... members) throws RedisException {
		if (members == null || members.length == 0) {
			return false;
		}
		try {
			return invoke(new Callback<Boolean>() {
				@Override
				public Boolean call(Jedis jedis) {
					long end = System.currentTimeMillis() + defaultEnqueueTimeout;
					long retNum = jedis.lpush(createKey(name, Integer.MIN_VALUE * 2).getBytes(), ByteObjectConvertUtil.getBytesFromObject(members));
					if (System.currentTimeMillis() > end) {
						log.warn("Enqueue RoughPriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
					}
					return retNum == members.length;
				}
			}, jedisPool);
		} catch (JedisException jedisException) {
			return false;
		}
	}

	@Override
	public boolean enqueue(final int priority, @SuppressWarnings("unchecked") final T... members) throws RedisException {
		if (members == null || members.length == 0) {
			return false;
		}
		try {
			return invoke(new Callback<Boolean>() {
				@Override
				public Boolean call(Jedis jedis) {
					long end = System.currentTimeMillis() + defaultEnqueueTimeout;
					long retNum;
					if(NumberUtil.isOdd(priority)){
						retNum = jedis.rpush(createKey(name, priority).getBytes(), ByteObjectConvertUtil.getBytesFromObject(members));
					}else{
						retNum = jedis.lpush(createKey(name, priority).getBytes(), ByteObjectConvertUtil.getBytesFromObject(members));
					}
					queueNames.add(createKey(name,priority));
					if (System.currentTimeMillis() > end) {
						log.warn("Enqueue RoughPriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
					}
					return retNum == members.length;
				}
			}, jedisPool);
		} catch (JedisException jedisException) {
			return false;
		}
	}

	@Override
	public T dequeue() throws RedisException {
		try {
			return invoke(new Callback<T>() {
				@SuppressWarnings("unchecked")
				@Override
				public T call(Jedis jedis) {
					long end = System.currentTimeMillis() + defaultDequeueTimeout;
					Set<String> keys = jedis.keys(createKeyByAsterisk(name));
					queueNames.addAll(keys);
					String[] keyArr = queueNames.toArray(new String[queueNames.size() * 2]);
					for (int i = keyArr.length / 2 -1; i != -1; i--) {
						keyArr[2*i] = keyArr[i]; 
					}
					for (int i = 1; i <keyArr.length; i += 2) {
						keyArr[i] = String.valueOf(0); 
					}
					List<String> retList = jedis.brpop(keyArr);
					if (System.currentTimeMillis() > end) {
						log.warn("Dequeue RoughPriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
					}
					String retStr = retList.get(1);
					if (StringUtil.isNotEmpty(retStr)) {
						Object[] objects = (Object[]) ByteObjectConvertUtil.getObjectFromBytes(retStr.getBytes());
						return (T) objects[0];
					}
					return null;
				}
			}, jedisPool);
		} catch (JedisException jedisException) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T top() throws RedisException {
		try {
			return invoke(new Callback<T>() {
				@Override
				public T call(Jedis jedis) {
					long end = System.currentTimeMillis() + defaultTopQueueTimeout;
					Set<String> keys = jedis.keys(createKeyByAsterisk(name));
					if (keys == null || keys.size() == 0) {
						return null;
					}
					queueNames.addAll(keys);
					String[] keyArr = queueNames.toArray(new String[0]);
					String retStr = null;
					for (String key : keyArr) {
						retStr = jedis.lindex(key, -1);
						if (retStr != null) {
							break;
						}
					}
					if (System.currentTimeMillis() > end) {
						log.warn("Top RoughPriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
					}
					if (StringUtil.isNotEmpty(retStr)) {
						Object[] objects = (Object[]) ByteObjectConvertUtil.getObjectFromBytes(retStr.getBytes());
						return (T) objects[0];
					}
					return null;
				}
			}, jedisPool);
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
					Set<String> keys = jedis.keys(createKeyByAsterisk(name));
					if (keys== null || keys.size() == 0) {
						return 0L;
					}
					long size = 0;
					for (String key : keys) {
						size += jedis.llen(key);
					}
					if (System.currentTimeMillis() > end) {
						log.warn("Query RoughPriorityQueue size time out. spend[ {}ms ]", System.currentTimeMillis() - end);
					}
					return size;
				}
			}, jedisPool);
		} catch (JedisException jedisException) {
			return -1L;
		}
	}

	@Override
	public String getName() throws RedisException {
		return this.name;
	}

	@Override
	public boolean clear() throws RedisException {
		try {
			return invoke(new Callback<Boolean>() {
				@Override
				public Boolean call(Jedis jedis) {
					long end = System.currentTimeMillis() + defaultClearQueueTimeout;
					Set<String> keys = jedis.keys(createKeyByAsterisk(name));
					String[] keyArr = keys.toArray(new String[0]);
					Long delNum = 0l ;
					if (keyArr.length != 0) {
						delNum = jedis.del(keyArr);
					}
					if (System.currentTimeMillis() > end) {
						log.warn("Clear RoughPriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
					}
					return delNum == 0 ||delNum == keys.size();
				}
			}, jedisPool);
		} catch (JedisException jedisException) {
			jedisException.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean isEmpty() throws RedisException {
		return this.size() == 0 ? true : false;
	}
}