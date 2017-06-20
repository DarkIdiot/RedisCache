package com.darkidiot.base;

import com.darkidiot.redis.config.JedisPoolFactory;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.jedis.imp.Jedis;
import com.darkidiot.redis.lock.Lock;
import com.darkidiot.redis.lock.imp.RigorousRedisLock;
import com.darkidiot.redis.lock.imp.StrictRedisLock;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class InventoryImitationTest {

    private static IJedis jedis;
    private static String service = "redis";

    private int ThreadCount = 10000; //20000、5000、1000

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        jedis = new Jedis(JedisPoolFactory.getWritePool(service), JedisPoolFactory.getReadPool(service), JedisPoolFactory.getInitParam(service));
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testStrictLock4Inventory() {
        final Lock lock = new StrictRedisLock(jedis, "Strict RedisLock");
        int n = ThreadCount;
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    lock.lock();
                    boolean unlockFlag = lock.unlock();
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
                    log.info("Strict RedisLock spend time " + spendTime + "ms for " + ThreadCount + " Thread.");
                }
            }
            ));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRigorousLock4Inventory() {
        final Lock lock = new RigorousRedisLock(jedis, "Rigorous RedisLock");
        int n = ThreadCount;
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    lock.lock();
                    boolean unlockFlag = lock.unlock();
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
                    log.info("Rigorous RedisLock spend time " + spendTime + "ms for " + ThreadCount + " Thread.");
                }
            }
            ));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
