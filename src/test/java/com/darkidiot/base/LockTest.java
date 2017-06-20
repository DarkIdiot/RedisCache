package com.darkidiot.base;

import com.darkidiot.redis.lock.Lock;
import com.darkidiot.redis.lock.RedisLock;
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
                        Thread.sleep(100);
                        boolean unlockFlag = lock.unlock();
                        log.info(Thread.currentThread() + ":" + unlockFlag);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
