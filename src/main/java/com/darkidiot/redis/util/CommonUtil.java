package com.darkidiot.redis.util;

import com.google.common.base.Throwables;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

import java.util.concurrent.Semaphore;

/**
 * Redis 公用Util
 *
 * @author darkidiot
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonUtil {

    public static <T> T invoke(Callback<T> call, Pool pool, Semaphore semaphore) {
        try {
            semaphore.acquire();
            try (Jedis jedis = (Jedis) pool.getResource()) {
                return call.call(jedis);
            }
        } catch (InterruptedException e) {
            log.error("Thread has been interrupted, cause by {}", Throwables.getStackTraceAsString(e));
            return null;
        } finally {
            semaphore.release();
        }
    }

    public interface Callback<T> {
        T call(Jedis jedis);
    }
}
