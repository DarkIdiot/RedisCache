package com.darkidiot.base;

import com.darkidiot.redis.IRedisMap;
import com.darkidiot.redis.Redis;
import com.darkidiot.redis.lock.Lock;
import com.darkidiot.redis.lock.RedisLock;
import com.darkidiot.redis.queue.Queue;
import com.darkidiot.redis.queue.RedisQueue;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.Serializable;

@Slf4j
public class CacheTest {

    @Test
    public void pushToCache1() {
        IRedisMap<String, String> cache = Redis.use("redisSourceName");
        cache.put("redisKey","redisValue");
        cache.get("redisKey");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("==================================");
        Redis.use().put("123","11122");
    }

    @Test
    public void getCache() {
        System.out.println(Redis.<String, String>use().get("123"));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Redis.<String, String>use().get("123"));
    }
}
