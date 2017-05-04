package com.darkidiot.redis.lock.imp;

import com.darkidiot.redis.exception.RedisException;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.lock.Lock;
import com.darkidiot.redis.util.FibonacciUtil;
import com.darkidiot.redis.util.StringUtil;
import com.darkidiot.redis.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * 简单的分布式锁的实现,效率较高(极端情况下，会出现多个实例同时获取到锁的 情况)
 * 堵塞式
 *
 * @author darkidiot
 */
@Slf4j
public class SimpleRedisLock implements Lock {

    private final IJedis jedis;

    private final String name;

    public SimpleRedisLock(IJedis jedis, String name) throws RedisException {
        if (jedis == null) {
            throw new RedisException("Initialize SimpleRedisLock failure, And jedis can not be null.");
        }
        if (StringUtil.isEmpty(name)) {
            throw new RedisException("Initialize SimpleRedisLock failure, And name can not be empty.");
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
        String value = UUIDUtil.generateShortUUID();
        int lockExpire = (int) (lockTimeout);
        long end = System.currentTimeMillis() + acquireTimeout;
        int i = 1;
        String identifier;
        while (true) {
            // 将rediskey的最大生存时刻存到redis里，过了这个时刻该锁会被自动释放
            if (jedis.setnx(lockKey, value) == 1) {
                //判断是否被其他实例拿到并改变value
                String lockValue = jedis.get(lockKey);
                if (lockValue != null && lockValue.equals(value)) {
                    //进程crash在这里，然后再继续执行会导致多个实例同时获取 到锁的混乱情况
                    jedis.expire(lockKey, lockExpire);
                    identifier = value;
                    break;
                }
            }

            /** ttl为 -1 表示key上没有设置生存时间（key是不会不存在的，因为前面setnx自动创建）
             *  如果出现这种状况,那就是进程的某个实例setnx成功后 crash 导致紧跟着的expire没有被调用,这时可以直接设置expire并把锁纳为己用
             */
            if (jedis.ttl(lockKey) == -1) {
                jedis.expire(lockKey, lockExpire);
                jedis.set(lockKey, value);   //将锁占为己用，并改变value;
                identifier = value;
                break;
            }

            try {
                Thread.sleep(Constants.defaultWaitIntervalInMSUnit * new Random().nextInt(FibonacciUtil.circulationFibonacciNormal(i++)));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            if (System.currentTimeMillis() > end) {
                log.warn("Acquire SimpleRedisLock time out. spend[ {}ms ]", System.currentTimeMillis() - end);
            }
        }
        return identifier;
    }

    @Override
    public boolean unlock(final String identifier) throws RedisException {
        if (StringUtil.isEmpty(identifier)) {
            throw new RedisException("identifier can not be empty.");
        }
        final String lockKey = Constants.createKey(this.name);
        long end = System.currentTimeMillis() + Constants.defaultReleaseLockTimeout;
        if (identifier.equals(jedis.get(lockKey))) {
            jedis.del(lockKey);
            if (System.currentTimeMillis() > end) {
                log.warn("Release SimpleRedisLock time out. spend[ {}ms ]", System.currentTimeMillis() - end);
            }
            return true;
        }
        return false;
    }

    @Override
    public String lock() throws RedisException {
        return lock(Constants.defaultAcquireLockTimeout, Constants.defaultLockTimeout);
    }

    @Override
    public boolean isLocking(final String identifier) throws RedisException {
        if (StringUtil.isEmpty(identifier)) {
            throw new RedisException("identifier can not be empty.");
        }
        final String lockKey = Constants.createKey(this.name);
        String retStr;
        long end = System.currentTimeMillis() + Constants.defaultCheckLockTimeout;
        retStr = jedis.get(lockKey);
        if (System.currentTimeMillis() > end) {
            log.warn("Checking SimpleRedisLock time out. spend[ {}ms ]", System.currentTimeMillis() - end);
        }
        return retStr != null && retStr.equals(identifier);
    }

    @Override
    public String getName() throws RedisException {
        return this.name;
    }
}