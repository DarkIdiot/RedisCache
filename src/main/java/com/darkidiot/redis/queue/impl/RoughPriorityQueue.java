package com.darkidiot.redis.queue.impl;

import com.darkidiot.redis.common.JedisType;
import com.darkidiot.redis.exception.RedisException;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.queue.Queue;
import com.darkidiot.redis.util.ByteObjectConvertUtil;
import com.darkidiot.redis.util.CommonUtil;
import com.darkidiot.redis.util.NumberUtil;
import com.darkidiot.redis.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 粗糙的多优先级支持队列(采用多个队列,每个队列容纳2个优先级;优先级为偶数,先进后出;优先级为奇数,先进先出;同一队列,优先级为偶数总是比奇数先出)
 *
 * @author darkidiot
 */
@Slf4j
public class RoughPriorityQueue<T extends Serializable> implements Queue<T> {

    private String name;
    private IJedis jedis;

    private TreeSet<String> queueNames = new TreeSet<>(new Comparator<String>() {
        @Override
        public int compare(String s1, String s2) {
            Integer s1Number = Integer.parseInt(s1.substring(s1.lastIndexOf(Constants.queueNameSequenceSeparator) + 1, s1.length()));
            Integer s2Number = Integer.parseInt(s2.substring(s1.lastIndexOf(Constants.queueNameSequenceSeparator) + 1, s1.length()));
            if (s1Number > s2Number) {
                return -1;
            } else if (s1Number < s2Number) {
                return 1;
            } else {
                return 0;
            }
        }
    });

    public RoughPriorityQueue(String name, IJedis jedis) throws RedisException {
        if (jedis == null) {
            throw new RedisException("Initialize RoughPriorityQueue failure, And jedis can not be null.");
        }
        if (StringUtil.isEmpty(name)) {
            throw new RedisException("Initialize RoughPriorityQueue failure, And name can not be empty.");
        }
        this.jedis = jedis;
        this.name = name;
        queueNames.add(Constants.createKey(name, Integer.MIN_VALUE * 2));
    }

    @SafeVarargs
    @Override
    public final boolean enqueue(final T... members) throws RedisException {
        return enqueue(Integer.MIN_VALUE, members);
    }

    @SafeVarargs
    @Override
    public final boolean enqueue(final int priority, final T... members) throws RedisException {
        if (members == null || members.length == 0) {
            return false;
        }

        return jedis.callOriginalJedis(new CommonUtil.Callback<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                long end = System.currentTimeMillis() + Constants.defaultEnqueueTimeout;
                long retNum;
                if (NumberUtil.isOdd(priority)) {
                    retNum = jedis.rpush(Constants.createKey(name, priority), ByteObjectConvertUtil.getBytesFromObject(members));
                } else {
                    retNum = jedis.lpush(Constants.createKey(name, priority), ByteObjectConvertUtil.getBytesFromObject(members));
                }
                queueNames.add(Constants.createKey(name, priority));
                if (System.currentTimeMillis() > end) {
                    log.warn("Enqueue RoughPriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                }
                return retNum == members.length;
            }
        }, JedisType.WRITE);
    }

    @Override
    public T dequeue() throws RedisException {
        return jedis.callOriginalJedis(new CommonUtil.Callback<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T call(Jedis jedis) {
                long end = System.currentTimeMillis() + Constants.defaultDequeueTimeout;
                Set<String> keys = jedis.keys(Constants.createKeyByAsterisk(name));
                queueNames.addAll(keys);
                String[] keyArr = queueNames.toArray(new String[queueNames.size() * 2]);
                for (int i = keyArr.length / 2 - 1; i != -1; i--) {
                    keyArr[2 * i] = keyArr[i];
                }
                for (int i = 1; i < keyArr.length; i += 2) {
                    keyArr[i] = String.valueOf(0);
                }
                List<String> retList = jedis.brpop(keyArr);
                if (System.currentTimeMillis() > end) {
                    log.warn("Dequeue RoughPriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
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

    @SuppressWarnings("unchecked")
    @Override
    public T top() throws RedisException {
        return jedis.callOriginalJedis(new CommonUtil.Callback<T>() {
            @Override
            public T call(Jedis jedis) {
                long end = System.currentTimeMillis() + Constants.defaultTopQueueTimeout;
                Set<String> keys = jedis.keys(Constants.createKeyByAsterisk(name));
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
                    Object[] objects = (Object[]) ByteObjectConvertUtil.getObjectFromBytes(retStr);
                    return (T) objects[0];
                }
                return null;
            }
        }, JedisType.WRITE);
    }

    @Override
    public long size() throws RedisException {
        return jedis.callOriginalJedis(new CommonUtil.Callback<Long>() {
            @Override
            public Long call(Jedis jedis) {
                long end = System.currentTimeMillis() + Constants.defaultQueryQueueSzieTimeout;
                Set<String> keys = jedis.keys(Constants.createKeyByAsterisk(name));
                if (keys == null || keys.size() == 0) {
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
        }, JedisType.WRITE);
    }

    @Override
    public String getName() throws RedisException {
        return this.name;
    }

    @Override
    public boolean clear() throws RedisException {
        return jedis.callOriginalJedis(new CommonUtil.Callback<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                long end = System.currentTimeMillis() + Constants.defaultClearQueueTimeout;
                Set<String> keys = jedis.keys(Constants.createKeyByAsterisk(name));
                String[] keyArr = keys.toArray(new String[0]);
                Long delNum = 0L;
                if (keyArr.length != 0) {
                    delNum = jedis.del(keyArr);
                }
                if (System.currentTimeMillis() > end) {
                    log.warn("Clear RoughPriorityQueue time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                }
                return delNum == 0 || delNum == keys.size();
            }
        }, JedisType.WRITE);
    }

    @Override
    public boolean isEmpty() throws RedisException {
        return this.size() == 0;
    }
}