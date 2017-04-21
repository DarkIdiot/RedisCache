package com.darkidiot.redis.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPool;

/**
 * Redis 公用Util
 *
 * @author darkidiot
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonUtil {

    public static <T> T invoke(Callback<T> call, JedisPool jedisPool) {
        redis.clients.jedis.Jedis jedis = jedisPool.getResource();
        try {
            return call.call(jedis);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public interface Callback<T> {
        T call(redis.clients.jedis.Jedis jedis);
    }
}
