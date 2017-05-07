package com.darkidiot.redis.queue.impl;

import com.darkidiot.redis.exception.RedisException;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.queue.Queue;
import com.darkidiot.redis.util.ByteObjectConvertUtil;
import com.darkidiot.redis.util.FibonacciUtil;
import com.darkidiot.redis.util.StringUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisException;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.darkidiot.redis.common.JedisType.WRITE;
import static com.darkidiot.redis.util.CommonUtil.Callback;

/**
 * 完美的多优先级支持队列(采用单个队列，每个元素赋予一个Score并按照Score排序得到一个优先级队列)
 * Notice: 由于Sorted Set本身是一个set，因此消息队列中的消息不能重复，否则新加入的消息会覆盖以前加入的任务.
 *
 * @author darkidiot
 */
@Slf4j
public class PerfectPriorityQueue<T extends Serializable> implements Queue<T> {

    private String name;
    private IJedis jedis;

    public PerfectPriorityQueue(String name, IJedis jedis) throws RedisException {
        if (jedis == null) {
            throw new RedisException("Initialize PerfectPriorityQueue failure, And jedis can not be null.");
        }
        if (StringUtil.isEmpty(name)) {
            throw new RedisException("Initialize PerfectPriorityQueue failure, And name can not be empty.");
        }
        this.name = name;
        this.jedis = jedis;
    }

    @SafeVarargs
    @Override
    public final boolean enqueue(@SuppressWarnings("unchecked") final T... members) throws RedisException {
        return enqueue(Integer.MIN_VALUE, members);
    }

    @SafeVarargs
    @Override
    public final boolean enqueue(final int priority, @SuppressWarnings("unchecked") final T... members) throws RedisException {
        if (members == null || members.length == 0) {
            return false;
        }
        long end = System.currentTimeMillis() + Constants.defaultEnqueueTimeout;
        Map<String, Double> tempMap = Maps.newHashMap();
        for (T member : members) {
            tempMap.put(ByteObjectConvertUtil.getBytesFromObject(member), (double) priority);
        }
        boolean ret = jedis.zadd(Constants.createKey(name), tempMap);
        if (System.currentTimeMillis() > end) {
            log.warn("Enqueue PerfectPriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T dequeue() throws RedisException {
        try {
            return jedis.callOriginalJedis(new Callback<T>() {
                @Override
                public T call(Jedis jedis) {
                    long end = System.currentTimeMillis() + Constants.defaultDequeueTimeout;
                    int i = 1;
                    while (true) {
                        Transaction trans = jedis.multi();
                        trans.zrevrangeWithScores(Constants.createKey(name), 0, 0);
                        trans.zremrangeByRank(Constants.createKey(name), -1, -1);
                        List<Object> exec = trans.exec();
                        Set<Tuple> retTupleSet = (Set<Tuple>) exec.get(0);
                        if (retTupleSet != null && retTupleSet.size() == 1) {
                            Tuple tuple = retTupleSet.iterator().next();
                            String element = tuple.getElement();
                            return (T) ByteObjectConvertUtil.getObjectFromBytes(element);
                        }
                        try {
                            Thread.sleep(Constants.defaultWaitIntervalInMSUnit * FibonacciUtil.circulationFibonacciNormal(i++));
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        if (System.currentTimeMillis() > end) {
                            log.warn("Dequeue PerfectPriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                        }
                    }

                }
            }, WRITE);
        } catch (JedisException jedisException) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T top() throws RedisException {
        long end = System.currentTimeMillis() + Constants.defaultTopQueueTimeout;
        Set<Tuple> retTupleSet = jedis.zrevrangeWithScores(Constants.createKey(name), 0, 0);
        if (retTupleSet != null && retTupleSet.size() == 1) {
            Tuple tuple = retTupleSet.iterator().next();
            return (T) ByteObjectConvertUtil.getObjectFromBytes(tuple.getElement());
        }
        if (System.currentTimeMillis() > end) {
            log.warn("Dequeue PerfectPriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
        }
        return null;
    }

    @Override
    public long size() throws RedisException {
        long end = System.currentTimeMillis() + Constants.defaultTopQueueTimeout;
        long retNum = jedis.zlexcount(Constants.createKey(name), "-", "+");
        if (System.currentTimeMillis() > end) {
            log.warn("Query PerfectPriorityQueue size time out. spend[ {}ms ]", System.currentTimeMillis() - end);
        }
        return retNum;
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
        long end = System.currentTimeMillis() + Constants.defaultClearQueueTimeout;
        Long delNum = jedis.del(Constants.createKey(name));
        if (System.currentTimeMillis() > end) {
            log.warn("Clear PerfectPriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
        }
        return delNum == 1 || delNum == 0;
    }
}