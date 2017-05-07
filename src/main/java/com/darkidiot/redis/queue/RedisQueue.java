package com.darkidiot.redis.queue;

import com.darkidiot.redis.config.JedisPoolFactory;
import com.darkidiot.redis.exception.RedisException;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.jedis.imp.Jedis;
import com.darkidiot.redis.queue.impl.PerfectPriorityQueue;
import com.darkidiot.redis.queue.impl.RoughPriorityQueue;
import com.darkidiot.redis.queue.impl.SimpleFifoQueue;
import com.darkidiot.redis.queue.impl.SimplePriorityQueue;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.darkidiot.redis.config.RedisPropertyConstants.DEFAULT_SERVICE_KEY;

/**
 * Redis Queue 工厂类
 * <ul>
 * <li>Notice: 采用享元模式， 同一个进程在多线程环境下去获取同名的锁总是返回单一实例.</li>
 * </ul>
 *
 * @author darkidiot
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisQueue {
    private static final String PERFECT_PRIORITY_QUEUE_PREFIX = "Perfect Priority Queue:";
    private static final String ROUGH_PRIORITY_QUEUE_PREFIX = "Rough Priority Queue:";
    private static final String SIMPLE_PRIORITY_QUEUE_PREFIX = "Simple Priority Queue:";
    private static final String SIMPLE_FIFO_QUEUE_PREFIX = "Simple Fifo Queue:";

    private static final Map<String, Queue<? extends Serializable>> QueueMap = new ConcurrentHashMap<>();

    public static <T extends Serializable> Queue<T> usePerfectPriorityQueue(final String queueName) throws RedisException {
        return usePerfectPriorityQueue(queueName, DEFAULT_SERVICE_KEY);
    }

    public static <T extends Serializable> Queue<T> usePerfectPriorityQueue(final String queueName, final String service) throws RedisException {
        return invoke(new Callback<T>() {
            @Override
            public Queue<T> call(IJedis jedis) throws RedisException {
                return new PerfectPriorityQueue<>(queueName, jedis);
            }
        }, PERFECT_PRIORITY_QUEUE_PREFIX, queueName, service);
    }

    public static <T extends Serializable> Queue<T> useRoughPriorityQueue(final String queueName) throws RedisException {
        return useRoughPriorityQueue(queueName, DEFAULT_SERVICE_KEY);
    }

    public static <T extends Serializable> Queue<T> useRoughPriorityQueue(final String queueName, final String service) throws RedisException {
        return invoke(new Callback<T>() {
            @Override
            public Queue<T> call(IJedis jedis) throws RedisException {
                return new RoughPriorityQueue<>(queueName, jedis);
            }
        }, ROUGH_PRIORITY_QUEUE_PREFIX, queueName, service);
    }

    public static <T extends Serializable> Queue<T> useSimplePriorityQueue(final String queueName) throws RedisException {
        return useSimplePriorityQueue(queueName, DEFAULT_SERVICE_KEY);
    }

    public static <T extends Serializable> Queue<T> useSimplePriorityQueue(final String queueName, final String service) throws RedisException {
        return invoke(new Callback<T>() {
            @Override
            public Queue<T> call(IJedis jedis) throws RedisException {
                return new SimplePriorityQueue<>(queueName, jedis);
            }
        }, SIMPLE_PRIORITY_QUEUE_PREFIX, queueName, service);
    }

    public static <T extends Serializable> Queue<T> useSimpleFifoQueue(final String queueName) throws RedisException {
        return useSimpleFifoQueue(queueName, DEFAULT_SERVICE_KEY);
    }

    public static <T extends Serializable> Queue<T> useSimpleFifoQueue(final String queueName, final String service) throws RedisException {
        return invoke(new Callback<T>() {
            @Override
            public Queue<T> call(IJedis jedis) throws RedisException {
                return new SimpleFifoQueue<>(queueName, jedis);
            }
        }, SIMPLE_FIFO_QUEUE_PREFIX, queueName, service);
    }

    private interface Callback<T extends Serializable> {
        Queue<T> call(IJedis jedis) throws RedisException;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Serializable> Queue<T> invoke(Callback<T> callback, String prefix, String queueName, String service) throws RedisException {
        String key = createKey(queueName, prefix);
        Queue<T> queue = (Queue<T>) QueueMap.get(key);
        if (queue == null) {
            queue = callback.call(new Jedis(JedisPoolFactory.getWritePool(service), JedisPoolFactory.getReadPool(service), JedisPoolFactory.getInitParam(service), JedisPoolFactory.getReadSemaphore(service), JedisPoolFactory.getWriteSemaphore(service)));
            QueueMap.put(key, queue);
        }
        return queue;
    }

    private static String createKey(String queueName, String prefix) {
        return prefix + queueName;
    }
}
