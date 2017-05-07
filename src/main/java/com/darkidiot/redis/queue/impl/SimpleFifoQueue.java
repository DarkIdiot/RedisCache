package com.darkidiot.redis.queue.impl;

import com.darkidiot.redis.common.JedisType;
import com.darkidiot.redis.exception.RedisException;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.queue.Queue;
import com.darkidiot.redis.util.ByteObjectConvertUtil;
import com.darkidiot.redis.util.CommonUtil.Callback;
import com.darkidiot.redis.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.List;

import static com.darkidiot.redis.queue.impl.Constants.*;

/**
 * 简单先进先出队列(采用单队列模式实现,不支持优先级)
 *
 * @author darkidiot
 */
@Slf4j
@SuppressWarnings("unchecked")
public class SimpleFifoQueue<T extends Serializable> implements Queue<T> {

    private String name;
    private IJedis jedis;

    public SimpleFifoQueue(String name, IJedis jedis) throws RedisException {
        if (jedis == null) {
            throw new RedisException("Initialize SimpleFifoQueue failure, And jedis can not be null.");
        }
        if (StringUtil.isEmpty(name)) {
            throw new RedisException("Initialize SimpleFifoQueue failure, And name can not be empty.");
        }
        this.jedis = jedis;
        this.name = name;
    }

    @Override
    public boolean enqueue(final T... members) throws RedisException {
        if (members == null || members.length == 0) {
            return false;
        }
        return jedis.callOriginalJedis(new Callback<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                long end = System.currentTimeMillis() + defaultEnqueueTimeout;
                long retNum = jedis.lpush(createKey(name), ByteObjectConvertUtil.getBytesFromObject(members));
                if (System.currentTimeMillis() > end) {
                    log.warn("Enqueue SimpleFifoQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                }
                return retNum > 0;
            }
        }, JedisType.WRITE);
    }

    @Override
    @Deprecated
    public boolean enqueue(int priority, T... members) throws RedisException {
        throw new RedisException("Ohh,the instance of Queue is SimpleFifoQueue this moment, and can not support this method.");
    }

    @Override
    public T dequeue() throws RedisException {
        return jedis.callOriginalJedis(new Callback<T>() {
            @Override
            public T call(Jedis jedis) {
                long end = System.currentTimeMillis() + defaultDequeueTimeout;
                List<String> retList = jedis.brpop(createKey(name), String.valueOf(0));
                if (System.currentTimeMillis() > end) {
                    log.warn("Dequeue SimpleFifoQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                }
                String retStr = retList.get(1);
                if (StringUtil.isNotEmpty(retStr)) {
                    Object[] objects = (Object[]) ByteObjectConvertUtil.getObjectFromBytes(retStr);
                    return (T) objects[0];
                }
                return null;
            }
        }, JedisType.WRITE);
    }

    @Override
    public T top() throws RedisException {
        return jedis.callOriginalJedis(new Callback<T>() {
            @Override
            public T call(Jedis jedis) {
                long end = System.currentTimeMillis() + defaultTopQueueTimeout;
                String top = jedis.lindex(createKey(name), -1);
                if (System.currentTimeMillis() > end) {
                    log.warn("Top SimpleFifoQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                }
                if (StringUtil.isNotEmpty(top)) {
                    Object[] objectFromBytes = (Object[]) ByteObjectConvertUtil.getObjectFromBytes(top);
                    return (T) objectFromBytes[0];
                }
                return null;
            }
        }, JedisType.WRITE);
    }

    @Override
    public long size() throws RedisException {
        return jedis.callOriginalJedis(new Callback<Long>() {
            @Override
            public Long call(Jedis jedis) {
                long end = System.currentTimeMillis() + defaultQueryQueueSzieTimeout;
                long size = jedis.llen(createKey(name));
                if (System.currentTimeMillis() > end) {
                    log.warn("Query SimpleFifoQueue size time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                }
                return size;
            }
        }, JedisType.WRITE);
    }

    @Override
    public boolean isEmpty() throws RedisException {
        return this.size() == 0;
    }

    @Override
    public String getName() throws RedisException {
        return this.name;
    }

    @Override
    public boolean clear() throws RedisException {
        return jedis.callOriginalJedis(new Callback<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                long end = System.currentTimeMillis() + defaultClearQueueTimeout;
                Long delNum = jedis.del(createKey(name));
                if (System.currentTimeMillis() > end) {
                    log.warn("Clear SimpleFifoQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                }
                return delNum == 1 || delNum == 0;
            }
        }, JedisType.READ);
    }
}