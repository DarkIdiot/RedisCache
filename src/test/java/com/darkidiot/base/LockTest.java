package com.darkidiot.base;

import com.darkidiot.redis.lock.Lock;
import com.darkidiot.redis.lock.imp.RigorousRedisLock;
import com.darkidiot.redis.lock.imp.SimpleRedisLock;
import com.darkidiot.redis.lock.imp.StrictRedisLock;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class LockTest {
    private static JedisPool pool;

    private int testCount = 4000; // 2000、5000、10000

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(4000);
        pool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379);
        Jedis resource = pool.getResource();
        resource.set("Count", "10000");
        resource.close();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        pool.destroy();
    }

    @Test
    public void testSimpleLock() {
        final Lock lock = new SimpleRedisLock(pool, "Simple RedisLock");
        int n = testCount;
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String identifier = lock.lock();
                    log.info(Thread.currentThread() + ":" + identifier);
                    boolean unlockFlag = lock.unlock(identifier);
                    log.info(Thread.currentThread() + ":" + unlockFlag);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            final long spendTime = System.currentTimeMillis() - start;
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("Simple RedisLock spend time " + spendTime + "ms for " + testCount + " Thread.");
                }
            }
            ));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testStrictLock() {
        final Lock lock = new StrictRedisLock(pool, "Strict RedisLock");
        int n = testCount;
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String identifier = lock.lock();
                    Jedis resource = pool.getResource();
                    log.info(Thread.currentThread() + ":" + identifier);
                    String count = resource.get("Count");
                    log.info(Thread.currentThread() + ":" + count);
                    resource.set("Count", Integer.valueOf(count) + 1 + "");
                    boolean unlockFlag = lock.unlock(identifier);
                    log.info(Thread.currentThread() + ":" + unlockFlag);
                    resource.close();
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            final long spendTime = System.currentTimeMillis() - start;
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("Strict RedisLock spend time " + spendTime + "ms for " + testCount + " Thread.");
                }
            }
            ));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRigorousLock() {
        final Lock lock = new RigorousRedisLock(pool, "Rigorous RedisLock");
        int n = testCount;
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String identifier = lock.lock();
                    Jedis resource = pool.getResource();
                    log.info(Thread.currentThread() + ":" + identifier);
                    String count = resource.get("Count");
                    log.info(Thread.currentThread() + ":" + count);
                    resource.set("Count", Integer.valueOf(count) + 1 + "");
                    boolean unlockFlag = lock.unlock(identifier);
                    log.info(Thread.currentThread() + ":" + unlockFlag);
                    resource.close();
                    countDownLatch.countDown();
                }
            }).start();
        }

        try {
            countDownLatch.await();
            final long spendTime = System.currentTimeMillis() - start;
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("Rigorous RedisLock spend time " + spendTime + "ms for " + testCount + " Thread.");
                }
            }
            ));
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }
}
