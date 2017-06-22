package com.darkidiot.redis.lock.imp;

import com.darkidiot.redis.config.IPorServerConfig;
import com.darkidiot.redis.exception.RedisException;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.lock.Lock;
import com.darkidiot.redis.util.FibonacciUtil;
import com.darkidiot.redis.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.Random;

import static com.darkidiot.redis.common.JedisType.READ;
import static com.darkidiot.redis.common.JedisType.WRITE;
import static com.darkidiot.redis.lock.imp.Constants.defaultAcquireLockTimeout;
import static com.darkidiot.redis.lock.imp.Constants.defaultLockTimeout;
import static com.darkidiot.redis.util.CommonUtil.Callback;

/**
 * <font color="red"><b>Commend:</b></font>
 * 严格的分布式锁的实现(超级超级严格)
 * <b>Notice:<b/>
 * <ul>
 * <li>可重入锁(重入锁必须先于外部锁释放)</li>
 * @author darkidiot
 */
@Slf4j
public class RigorousRedisLock implements Lock {

    private final IJedis jedis;
    private final String name;

    private String identifier;

    public RigorousRedisLock(IJedis jedis, String name) throws RedisException {
        if (jedis == null) {
            throw new RedisException("Initialize RigorousRedisLock failure, And jedis can not be null.");
        }
        if (StringUtil.isEmpty(name)) {
            throw new RedisException("Initialize RigorousRedisLock failure, And name can not be empty.");
        }
        this.jedis = jedis;
        this.name = name;
    }

    @Override
    public void lock(final long acquireTimeout, final long lockTimeout) throws RedisException {
        if (acquireTimeout < 0 || lockTimeout < -1) {
            throw new RedisException("acquireTimeout can not be negative Or LockTimeout can not be less than -1.");
        }
        final String value = IPorServerConfig.getThreadId();
        final String lockKey = Constants.createKey(this.name);
        final int lockExpire = (int) (lockTimeout);
        final long end = System.currentTimeMillis() + acquireTimeout;
        jedis.callOriginalJedis(new Callback<String>() {
            @Override
            public String call(Jedis jedis) {
                int i = 1;
                boolean flag = false;
                boolean innerJudge = true;
                String lockValue = value;
                while (true) {
                    jedis.watch(lockKey);
                    // 开启watch之后，如果key的值被修改，则事务失败，exec方法返回null
                    String retStr = jedis.get(lockKey);
                    // 多个进程同时获取到未上锁状态时,进入事务上锁,第一个事务执行成功之后所有操作都会被取消并进入等待.
                    if (retStr == null || retStr.equals(Constants.LOCK_UNLOCK) || flag) {
                        Transaction t = jedis.multi();
                        t.setex(lockKey, lockExpire, lockValue);
                        if (t.exec() != null) {
                            identifier = lockValue;
                            return identifier;
                        }
                    }
                    jedis.unwatch();
                    if (innerJudge && retStr != null && retStr.contains(value)) {
                        lockValue = Constants.autoOverlayValue(retStr);
                        flag = true;
                    } else if (retStr != null){
                        innerJudge = false;
                    }
                    try {
                        long sleepMillis = Constants.defaultWaitIntervalInMSUnit * new Random().nextInt(FibonacciUtil.circulationFibonacciNormal(++i > 15 ? 15 : i));
                        if (System.currentTimeMillis() > end) {
                            log.warn("Acquire RigorousRedisLock time out. spend[ {}ms ] and await[ {}ms]", System.currentTimeMillis() - end, sleepMillis);
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
    public void lock() throws RedisException {
        lock(defaultAcquireLockTimeout, defaultLockTimeout);
    }

    @Override
    public boolean unlock() throws RedisException {
        if (StringUtil.isEmpty(identifier)) {
            throw new RedisException("identifier can not be empty.");
        }
        final String lockKey = Constants.createKey(this.name);
        return jedis.callOriginalJedis(new Callback<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                long end = System.currentTimeMillis() + Constants.defaultReleaseLockTimeout;
                if (identifier.equals(jedis.getSet(lockKey, Constants.autoDepriveValue(identifier)))) {
                    if (System.currentTimeMillis() > end) {
                        log.warn("Release RigorousRedisLock time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                    }
                    return true;
                }
                throw new RedisException("Release the RigorousRedisLock error, the lock was robbed.");
            }
        }, READ);
    }

    @Override
    public boolean isLocking() throws RedisException {
        if (StringUtil.isEmpty(identifier)) {
            throw new RedisException("identifier can not be empty.");
        }
        String lockKey = Constants.createKey(this.name);
        long end = System.currentTimeMillis() + Constants.defaultCheckLockTimeout;
        String retStr = jedis.get(lockKey);
        if (System.currentTimeMillis() > end) {
            log.warn("Checking RigorousRedisLock time out. spend[ {}ms ]", System.currentTimeMillis() - end);
        }
        return retStr != null && !retStr.equals(Constants.LOCK_UNLOCK);
    }

    @Override
    public String getName() throws RedisException {
        return name;
    }
}