package com.darkidiot.redis.queue.impl;

import com.darkidiot.redis.exception.RedisException;
import com.darkidiot.redis.queue.Queue;
import com.darkidiot.redis.util.ByteObjectConvertUtil;
import com.darkidiot.redis.util.CommonUtil.Callback;
import com.darkidiot.redis.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

import java.io.Serializable;
import java.util.List;

import static com.darkidiot.redis.util.CommonUtil.invoke;

/**
 * 简单的优先级队列(采用高优先级队列与低优先级队列二级优先级实现, 支持同一优先级队列先进先出)
 *
 * @author darkidiot
 */
@Slf4j
public class SimplePriorityQueue<T extends Serializable> implements Queue<T> {

    private Pool pool;
    private String name;

    private static final String highlyPriorityQueue = "Highly Priority Queue:";
    private static final String lowlyPriorityQueue = "Lowly Priority Queue:";

    private static final String queueName = "Simple Priority Queue";

    public SimplePriorityQueue(String name, Pool pool) throws RedisException {
        if (pool == null) {
            throw new RedisException("Initialize SimplePriorityQueue failure, And pool can not be null.");
        }
        if (StringUtil.isEmpty(name)) {
            throw new RedisException("Initialize SimpleSingleQueueFifoQueue failure, And name can not be empty.");
        }
        this.pool = pool;
        this.name = name;
    }

    @Override
    public boolean enqueue(@SuppressWarnings("unchecked") final T... members) throws RedisException {
        return enqueue(-1, members);
    }

    /**
     * 双队列优先级队列,正数代表高优先级队列,负数或零代表低优先级队列.
     */
    @Override
    public boolean enqueue(final int priority, @SuppressWarnings("unchecked") final T... members) throws RedisException {
        if (members == null || members.length == 0) {
            return false;
        }
        try {
            return invoke(new Callback<Boolean>() {
                @Override
                public Boolean call(Jedis jedis) {
                    long end = System.currentTimeMillis() + Constants.defaultEnqueueTimeout;
                    long retNum;
                    if (priority > 0) {
                        retNum = jedis.lpush(Constants.createKey(highlyPriorityQueue + name), ByteObjectConvertUtil.getBytesFromObject(members));
                    } else {
                        retNum = jedis.lpush(Constants.createKey(lowlyPriorityQueue + name), ByteObjectConvertUtil.getBytesFromObject(members));
                    }
                    if (System.currentTimeMillis() > end) {
                        log.warn("Enqueue SimplePriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                    }
                    return retNum == members.length;
                }
            }, pool);
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
                    long end = System.currentTimeMillis() + Constants.defaultDequeueTimeout;
                    List<String> retList = jedis.brpop(Constants.createKey(highlyPriorityQueue + name), String.valueOf(0), Constants.createKey(lowlyPriorityQueue + name), String.valueOf(0));
                    if (System.currentTimeMillis() > end) {
                        log.warn("Dequeue SimplePriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                    }
                    String retStr = retList.get(1);
                    if (StringUtil.isNotEmpty(retStr)) {
                        Object[] objects = (Object[]) ByteObjectConvertUtil.getObjectFromBytes(retStr);
                        return (T) objects[0];
                    }
                    return null;
                }
            }, pool);
        } catch (JedisException jedisException) {
            return null;
        }
    }

    @Override
    public T top() throws RedisException {
        try {
            return invoke(new Callback<T>() {
                @Override
                @SuppressWarnings("unchecked")
                public T call(Jedis jedis) {
                    long end = System.currentTimeMillis() + Constants.defaultTopQueueTimeout;
                    String top = jedis.lindex(Constants.createKey(highlyPriorityQueue + name), -1);
                    if (top == null) {
                        top = jedis.lindex(Constants.createKey(lowlyPriorityQueue + name), -1);
                    }
                    if (System.currentTimeMillis() > end) {
                        log.warn("Top SimplePriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                    }
                    if (StringUtil.isNotEmpty(top)) {
                        Object[] objects = (Object[]) ByteObjectConvertUtil.getObjectFromBytes(top);
                        return (T) objects[0];
                    }
                    return null;
                }
            }, pool);
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
                    long end = System.currentTimeMillis() + Constants.defaultQueryQueueSzieTimeout;
                    Long size = jedis.llen(Constants.createKey(highlyPriorityQueue + name)) + jedis.llen(Constants.createKey(lowlyPriorityQueue + name));
                    if (System.currentTimeMillis() > end) {
                        log.warn("Query SimplePriorityQueue size time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                    }
                    return size;
                }
            }, pool);
        } catch (JedisException jedisException) {
            return -1L;
        }
    }

    @Override
    public String getName() throws RedisException {
        return SimplePriorityQueue.queueName;
    }

    @Override
    public boolean clear() throws RedisException {
        try {
            return invoke(new Callback<Boolean>() {
                @Override
                public Boolean call(Jedis jedis) {
                    long end = System.currentTimeMillis() + Constants.defaultClearQueueTimeout;
                    Long delNum = jedis.del(Constants.createKey(highlyPriorityQueue + name), Constants.createKey(lowlyPriorityQueue + name));
                    if (System.currentTimeMillis() > end) {
                        log.warn("Clear SimplePriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                    }
                    return delNum == 2 || delNum == 1 || delNum == 0;
                }
            }, pool);
        } catch (JedisException jedisException) {
            return false;
        }
    }

    @Override
    public boolean isEmpty() throws RedisException {
        return this.size() == 0 ? true : false;
    }
}