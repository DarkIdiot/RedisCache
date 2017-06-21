package com.darkidiot.redis.lock.imp;

import com.darkidiot.redis.config.IPorServerConfig;
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
 * 简单的分布式锁的实现,效率较高(极端情况下，会出现多个实例同时获取到锁的 情况)
 * 堵塞式
 *
 * @author darkidiot
 */
@Slf4j
public class SimpleRedisLock implements Lock {

    private final IJedis jedis;

    private final String name;

    private String identifier;

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
    public void lock(final long acquireTimeout, final long lockTimeout) throws RedisException {
        if (acquireTimeout < 0 || lockTimeout < -1) {
            throw new RedisException("acquireTimeout can not be negative Or LockTimeout can not be less than -1.");
        }
        final String lockKey = Constants.createKey(name);
        final String value = IPorServerConfig.getThreadId();
        final int lockExpire = (int) (lockTimeout);
        final long end = System.currentTimeMillis() + acquireTimeout;

        jedis.callOriginalJedis(new Callback<String>() {
            @Override
            public String call(Jedis jedis) {
                int i = 1;
                log.debug("entrance lock");
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
                        Transaction multi = jedis.multi();
                        multi.expire(lockKey, lockExpire);
                        multi.set(lockKey, value);   //将锁占为己用，并改变value;
                        multi.exec();
                        identifier = value;
                        break;
                    }

                    try {
                        long sleepMillis = Constants.defaultWaitIntervalInMSUnit * new Random().nextInt(FibonacciUtil.circulationFibonacciNormal(++i > 15 ? 15 : i));
                        if (System.currentTimeMillis() > end) {
                            log.warn("Acquire SimpleRedisLock time out. spend[ {}ms ] and await[ {}ms]", System.currentTimeMillis() - end, sleepMillis);
                        }
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                log.debug("lock");
                return identifier;
            }
        }, WRITE);
    }

    @Override
    public boolean unlock() throws RedisException {
        if (StringUtil.isEmpty(identifier)) {
            throw new RedisException("identifier can not be empty.");
        }
        final String lockKey = Constants.createKey(this.name);
        final long end = System.currentTimeMillis() + Constants.defaultReleaseLockTimeout;
        log.debug("unlock");
        return jedis.callOriginalJedis(new Callback<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                String o1 = jedis.get(lockKey);
                log.debug("identifier:{},value:{}",identifier, o1);
                if (identifier.equals(o1)) {
                    jedis.del(lockKey);
                    if (System.currentTimeMillis() > end) {
                        log.warn("Release SimpleRedisLock time out. spend[ {}ms ]", System.currentTimeMillis() - end);
                    }
                    return true;
                }
                throw new RedisException("Release the SimpleRedisLock error, the lock was robbed.");
            }
        }, READ);
    }

    @Override
    public void lock() throws RedisException {
        lock(Constants.defaultAcquireLockTimeout, Constants.defaultLockTimeout);
    }

    @Override
    public boolean isLocking() throws RedisException {
        if (StringUtil.isEmpty(identifier)) {
            throw new RedisException("identifier can not be empty.");
        }
        final String lockKey = Constants.createKey(this.name);
        long end = System.currentTimeMillis() + Constants.defaultCheckLockTimeout;
        String retStr = jedis.get(lockKey);
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