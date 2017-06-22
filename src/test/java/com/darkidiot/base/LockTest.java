package com.darkidiot.base;

import com.darkidiot.redis.config.IPorServerConfig;
import com.darkidiot.redis.config.JedisPoolFactory;
import com.darkidiot.redis.jedis.imp.Jedis;
import com.darkidiot.redis.lock.Lock;
import com.darkidiot.redis.lock.RedisLock;
import com.darkidiot.redis.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class LockTest {
    private int testCount = 2000;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testCreateKey() {
        int n = testCount;
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info(IPorServerConfig.getThreadId());
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUUID() {
        int n = testCount;
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info(UUIDUtil.generateShortUUID());
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteSimpleLock() {
        String service = "redis";
        Jedis jedis = new Jedis(JedisPoolFactory.getWritePool(service), JedisPoolFactory.getReadPool(service), JedisPoolFactory.getInitParam(service));
        jedis.del("Lock:Simple RedisLock");
    }

    @Test
    public void testSimpleLock() {
        final Lock lock = RedisLock.useSimpleRedisLock("Simple RedisLock");
        int n = testCount;
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        lock.lock();
                        boolean unlockFlag = lock.unlock();
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
        final Lock lock = RedisLock.useStrictRedisLock("Strict RedisLock");
        final Lock lock1 = RedisLock.useStrictRedisLock("Strict RedisLock");
        int n = testCount;
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        lock.lock();
                        boolean unlockFlag = lock.unlock();
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
        final Lock lock = RedisLock.useRigorousRedisLock("Rigorous RedisLock");
        int n = testCount;
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        lock.lock();
                        boolean unlockFlag = lock.unlock();
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
                    log.info("Rigorous RedisLock spend time " + spendTime + "ms.");
                }
            }
            ));
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }
}
