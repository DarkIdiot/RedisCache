package com.darkidiot.redis.lock.imp;

import com.darkidiot.redis.exception.RedisException;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.lock.Lock;
import com.darkidiot.redis.util.FibonacciUtil;
import com.darkidiot.redis.util.StringUtil;
import com.darkidiot.redis.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.Random;

import static com.darkidiot.redis.common.JedisType.READ;
import static com.darkidiot.redis.common.JedisType.WRITE;
import static com.darkidiot.redis.util.CommonUtil.Callback;

/**
 * 严格的分布式锁的实现(事务锁定)<br>
 * <br>
 * 可能会出现2种极端情况:
 * <ul>
 * <li>锁已过过期时效但是并未失效.</li>
 * <li>成功解锁并返回false,加锁期间其他进程进入并修改了锁信息并再次延长锁过期时间,但并未获得锁.</li>
 * </ul>
 *
 * @author darkidiot
 */
@Slf4j
public class StrictRedisLock implements Lock {

    private IJedis jedis;
    private String name;


    public StrictRedisLock(IJedis jedis, String name) throws RedisException {
        if (jedis == null) {
            throw new RedisException("Initialize StrictRedisLock failure, And jedis can not be null.");
        }
        if (StringUtil.isEmpty(name)) {
            throw new RedisException("Initialize StrictRedisLock failure, And name can not be empty.");
        }
        this.jedis = jedis;
        this.name = name;
    }

    @Override
    public String lock(final long acquireTimeout, final long lockTimeout) throws RedisException {
        if (acquireTimeout < 0 || lockTimeout < -1) {
            throw new RedisException("acquireTimeout can not be  negative Or LockTimeout can not be less than -1.");
        }
        final String lockKey = Constants.createKey(this.name);
        final String value = UUIDUtil.generateShortUUID();
        return jedis.callOriginalJedis(new Callback<String>() {
            @Override
            public String call(Jedis jedis) {
                int lockExpire = (int) (lockTimeout);

                long end = System.currentTimeMillis() + acquireTimeout;
                int i = 1;
                while (true) {
                    String retStr = jedis.get(lockKey);
                    if (retStr == null || retStr.equals(Constants.LOCK_UNLOCK)) {
                        Transaction t = jedis.multi();
                        t.getSet(lockKey, value);
                        t.expire(lockKey, lockExpire);
                        String ret = (String) t.exec().get(0);
                        if (ret == null || ret.equals(Constants.LOCK_UNLOCK)) {
                            return value;
                        }
                    }
                    try {
                        long sleepMillis = Constants.defaultWaitIntervalInMSUnit * new Random().nextInt(FibonacciUtil.circulationFibonacciNormal(++i > 15 ? 15 : i));
                        if (System.currentTimeMillis() > end) {
                            log.warn("Acquire StrictRedisLock time out. spend[ {}ms ] and await[ {}ms]", System.currentTimeMillis() - end, sleepMillis);
                        }
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }, WRITE);
    }

    @Override
    public String lock() throws RedisException {
        return lock(Constants.defaultAcquireLockTimeout, Constants.defaultLockTimeout);
    }

    @Override
    public boolean unlock(final String identifier) throws RedisException {
        if (StringUtil.isEmpty(identifier)) {
            throw new RedisException("identifier can not be empty.");
        }
        final String lockKey = Constants.createKey(this.name);
        return jedis.callOriginalJedis(new Callback<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                long end = System.currentTimeMillis() + Constants.defaultReleaseLockTimeout;
                if (identifier.equals(jedis.getSet(lockKey, Constants.LOCK_UNLOCK))) {
                    if (System.currentTimeMillis() > end) {
                        log.warn("Release StrictRedisLock time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                    }
                    return true;
                }
                throw new RedisException("Release the StrictRedisLock error, the lock was robbed.");
            }
        }, READ);
    }

    @Override
    public boolean isLocking(String identifier) throws RedisException {
        if (StringUtil.isEmpty(identifier)) {
            throw new RedisException("identifier can not be empty.");
        }
        String lockKey = Constants.createKey(this.name);
        long end = System.currentTimeMillis() + Constants.defaultCheckLockTimeout;
        String retStr = jedis.get(lockKey);
        if (System.currentTimeMillis() > end) {
            log.warn("Checking StrictRedisLock time out. spend[ {}ms ]", System.currentTimeMillis() - end);
        }
        return retStr != null && !retStr.equals(Constants.LOCK_UNLOCK);
    }

    @Override
    public String getName() throws RedisException {
        return this.name;
    }
}