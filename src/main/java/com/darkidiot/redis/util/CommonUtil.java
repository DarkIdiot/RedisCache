package com.darkidiot.redis.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

/**
 * Redis 公用Util
 *
 * @author darkidiot
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonUtil {

    public static <T> T invoke(Callback<T> call, Pool pool) {
        try (Jedis jedis = (Jedis) pool.getResource()) {
            return call.call(jedis);
        }
    }

    public interface Callback<T> {
        T call(Jedis jedis);
    }
}
