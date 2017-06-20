package com.darkidiot.redis.lock;

import com.darkidiot.redis.config.JedisPoolFactory;
import com.darkidiot.redis.exception.RedisException;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.jedis.imp.Jedis;
import com.darkidiot.redis.lock.imp.RigorousRedisLock;
import com.darkidiot.redis.lock.imp.SimpleRedisLock;
import com.darkidiot.redis.lock.imp.StrictRedisLock;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.darkidiot.redis.config.RedisPropertyConstants.DEFAULT_SERVICE_KEY;

/**
 * Redis Lock 工厂类<br>
 * <b>Notice:<b/>
 * <ul>
 * <li>采用享元模式， 同一个进程在多线程环境下去获取同名的锁总是返回单一实例.</li>
 * <li>redis锁支持2000级别并发,超出需要进行redis参数调优完成支持</li>
 * </ul>
 *
 * @author darkidiot
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisLock {

    private static final String RIGOROUS_LOCK_PREFIX = "Rigorous Lock:";
    private static final String SIMPLE_LOCK_PREFIX = "Simple Lock:";
    private static final String STRICT_LOCK_PREFIX = "Strict Lock:";

    private static final Map<String, IJedis> IjedisMap = new ConcurrentHashMap<>();

    public static Lock useRigorousRedisLock(final String lockname) throws RedisException {
        return useRigorousRedisLock(lockname, DEFAULT_SERVICE_KEY);
    }

    public static Lock useRigorousRedisLock(final String lockname, final String service) throws RedisException {
        return invoke(new Callback() {
            @Override
            public Lock call(IJedis jedis) throws RedisException {
                return new RigorousRedisLock(jedis, lockname);
            }
        }, RIGOROUS_LOCK_PREFIX, lockname, service);
    }

    public static Lock useSimpleRedisLock(final String lockname) throws RedisException {
        return useSimpleRedisLock(lockname, DEFAULT_SERVICE_KEY);
    }

    public static Lock useSimpleRedisLock(final String lockname, final String service) throws RedisException {
        return invoke(new Callback() {
            @Override
            public Lock call(IJedis jedis) throws RedisException {
                return new SimpleRedisLock(jedis, lockname);
            }
        }, SIMPLE_LOCK_PREFIX, lockname, service);
    }

    public static Lock useStrictRedisLock(final String lockname) throws RedisException {
        return useStrictRedisLock(lockname, DEFAULT_SERVICE_KEY);
    }

    public static Lock useStrictRedisLock(final String lockname, final String service) throws RedisException {
        return invoke(new Callback() {
            @Override
            public Lock call(IJedis jedis) throws RedisException {
                return new StrictRedisLock(jedis, lockname);
            }
        }, STRICT_LOCK_PREFIX, lockname, service);
    }

    private interface Callback {
        Lock call(IJedis jedis) throws RedisException;
    }

    private static Lock invoke(Callback callback, String prefix, String lockname, String service) throws RedisException {
        String key = createKey(lockname, prefix);
        IJedis jedis = IjedisMap.get(key);
        if (jedis == null) {
            jedis = new Jedis(JedisPoolFactory.getWritePool(service), JedisPoolFactory.getReadPool(service), JedisPoolFactory.getInitParam(service));
            IjedisMap.put(key, jedis);
        }
        return callback.call(jedis);
    }

    private static String createKey(String lockname, String prefix) {
        return prefix + lockname;
    }
}
