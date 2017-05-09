package com.darkidiot.base;

import com.darkidiot.redis.config.JedisPoolFactory;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.lock.Lock;
import com.darkidiot.redis.lock.imp.RigorousRedisLock;
import com.darkidiot.redis.lock.imp.SimpleRedisLock;
import com.darkidiot.redis.lock.imp.StrictRedisLock;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class LockTest {
    private static IJedis jedis;
    private static String service = "redis";
    private int testCount = 2000;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        jedis = new com.darkidiot.redis.jedis.imp.Jedis(JedisPoolFactory.getWritePool(service), JedisPoolFactory.getReadPool(service), JedisPoolFactory.getInitParam(service), JedisPoolFactory.getReadSemaphore(service), JedisPoolFactory.getWriteSemaphore(service));
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testSimpleLock() {
        final Lock lock = new SimpleRedisLock(jedis, "Simple RedisLock");
        int n = testCount;
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String identifier = lock.lock();
                        log.info(Thread.currentThread() + ":" + identifier);
                        boolean unlockFlag = lock.unlock(identifier);
                        log.info(Thread.currentThread() + ":" + unlockFlag);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            }).start();
        }
        try {
            countDownLatch.await();
            final long spendTime = System.currentTimeMillis() - start;
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("Simple RedisLock spend time " + spendTime + "ms.");
                }
            }
            ));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testStrictLock() {
        final Lock lock = new StrictRedisLock(jedis, "Strict RedisLock");
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
                    log.info("Strict RedisLock spend time " + spendTime + "ms.");
                }
            }
            ));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRigorousLock() {
        final Lock lock = new RigorousRedisLock(jedis, "Rigorous RedisLock");
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
                    log.info("Rigorous RedisLock spend time " + spendTime + "ms.");
                }
            }
            ));
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }
}
