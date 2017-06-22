package com.darkidiot.redis.lock;

import com.darkidiot.redis.config.JedisPoolFactory;
import com.darkidiot.redis.config.RedisInitParam;
import com.darkidiot.redis.exception.RedisException;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.jedis.imp.Jedis;
import com.darkidiot.redis.lock.imp.RigorousRedisLock;
import com.darkidiot.redis.lock.imp.SimpleRedisLock;
import com.darkidiot.redis.lock.imp.StrictRedisLock;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.darkidiot.redis.config.RedisPropertyConstants.DEFAULT_SERVICE_KEY;

/**
 * Redis Lock 工厂类<br>
 * <b>Notice:<b/>
 * <ul>
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

    private static final Map<String, IJedis> iJedisMap = Maps.newConcurrentMap();

    private static final Map<String, Multiset<String>> needReleaseKeyMap = Maps.newConcurrentMap();

    public static void overlayLockCount(IJedis jedis, String LockKey) {
        Multiset<String> multiset = needReleaseKeyMap.get(jedis.baseConfig().getServerName());
        multiset.add(LockKey);
    }

    public static void DepriveLockCount(IJedis jedis, String LockKey) {
        Multiset<String> multiset = needReleaseKeyMap.get(jedis.baseConfig().getServerName());
        multiset.remove(LockKey);
    }

    public static void releaseLockWhenShutdown() {
        for (Map.Entry<String, IJedis> entry : iJedisMap.entrySet()) {
            String key = entry.getKey();
            IJedis jedis = entry.getValue();
            Multiset<String> LockKeySet = needReleaseKeyMap.get(key);
            List<String> needReleaseKey = Lists.newArrayList();
            for (Multiset.Entry<String> lockKey : LockKeySet.entrySet()) {

            }
        }
    }


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
        IJedis jedis = iJedisMap.get(service);
        if (jedis == null) {
            RedisInitParam initParam = JedisPoolFactory.getInitParam(service);
            if (!initParam.getR$WSeparated()) {
                throw new IllegalStateException("Can not create RedisLock cause by don't separate write and read, should be configuration [{}.read&write.separated=true] to use RedisLock.");
            }
            jedis = new Jedis(JedisPoolFactory.getWritePool(service), JedisPoolFactory.getReadPool(service), initParam);
            iJedisMap.put(service, jedis);
            needReleaseKeyMap.put(service, HashMultiset.<String>create());
        }
        return callback.call(jedis);
    }

}
