package com.darkidiot.base;

import com.darkidiot.redis.config.JedisPoolFactory;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.queue.Queue;
import com.darkidiot.redis.queue.impl.PerfectPriorityQueue;
import com.darkidiot.redis.queue.impl.RoughPriorityQueue;
import com.darkidiot.redis.queue.impl.SimpleFifoQueue;
import com.darkidiot.redis.queue.impl.SimplePriorityQueue;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;

@FixMethodOrder(MethodSorters.JVM)
@Slf4j
public class QueueTest {
    private static IJedis jedis;
    private static String service = "redis";
    private int testCount = 2000;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        jedis = new com.darkidiot.redis.jedis.imp.Jedis(JedisPoolFactory.getWritePool(service), JedisPoolFactory.getReadPool(service), JedisPoolFactory.getInitParam(service));
    }

    /**
     * ================= testSimpleFifoQueue =================
     */
    @Test
    public void testSimpleFifoQueue() {
        int n = testCount;
        final Queue<Person> queue = new SimpleFifoQueue<>("Simple Fifo Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            final int count = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    queue.enqueue(new Person(count + ""));
                    log.info(Thread.currentThread() + ": enqueue " + count);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Simple Fifo Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimpleFifoQueue2() {
        int n = testCount;
        final Queue<Person> queue = new SimpleFifoQueue<>("Simple Fifo Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Person dequeue = queue.dequeue();
                    log.info(Thread.currentThread() + ": dequeue " + dequeue);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Simple Fifo Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimpleFifoQueue3() {
        int n = testCount;
        final Queue<Person> queue = new SimpleFifoQueue<>("Simple Fifo Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Person top = queue.top();
                    log.info(Thread.currentThread() + ": Top " + top);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Simple Fifo Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimpleFifoQueue4() {
        int n = testCount;
        final Queue<String> queue = new SimpleFifoQueue<>("Simple Fifo Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long size = queue.size();
                    log.info(Thread.currentThread() + ": queue size " + size);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Simple Fifo Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimpleFifoQueue5() {
        int n = testCount;
        final Queue<String> queue = new SimpleFifoQueue<>("Simple Fifo Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean empty = queue.isEmpty();
                    log.info(Thread.currentThread() + ": is empty " + empty);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Simple Fifo Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimpleFifoQueue6() {
        int n = testCount;
        final Queue<String> queue = new SimpleFifoQueue<>("Simple Fifo Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean empty = queue.clear();
                    log.info(Thread.currentThread() + ": is clear " + empty);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Simple Fifo Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * ================= testSimplePriorityQueue =================
     */

    @Test
    public void testSimplePriorityQueue() {
        int n = testCount;
        final Queue<Person> queue = new SimplePriorityQueue<>("Simple Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(2 * n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            final int count = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    queue.enqueue(new Person(count + ""));
                    log.info(Thread.currentThread() + ": lowly enqueue " + count);
                    countDownLatch.countDown();
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    queue.enqueue(1, new Person(count + ""));
                    log.info(Thread.currentThread() + ": highly enqueue " + count);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Simple Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimplePriorityQueue2() {
        int n = testCount;
        final Queue<Person> queue = new SimplePriorityQueue<>("Simple Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Person dequeue = queue.dequeue();
                    log.info(Thread.currentThread() + ": dequeue " + dequeue);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Simple Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimplePriorityQueue3() {
        int n = testCount;
        final Queue<Person> queue = new SimplePriorityQueue<>("Simple Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Person top = queue.top();
                    log.info(Thread.currentThread() + ": Top " + top);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Simple Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimplePriorityQueue4() {
        int n = testCount;
        final Queue<Person> queue = new SimplePriorityQueue<>("Simple Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long size = queue.size();
                    log.info(Thread.currentThread() + ": queue size " + size);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Simple Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimplePriorityQueue5() {
        int n = testCount;
        final Queue<Person> queue = new SimplePriorityQueue<>("Simple Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean empty = queue.isEmpty();
                    log.info(Thread.currentThread() + ": is empty " + empty);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Simple Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimplePriorityQueue6() {
        int n = testCount;
        final Queue<Person> queue = new SimplePriorityQueue<>("Simple Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean empty = queue.clear();
                    log.info(Thread.currentThread() + ": is clear " + empty);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Simple Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * ================= testRoughPriorityQueue =================
     */

    @Test
    public void testRoughPriorityQueue() {
        int n = testCount;
        final Queue<Person> queue = new RoughPriorityQueue<>("Rough Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            final int count = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    queue.enqueue(count, new Person(count + ""));
                    log.info(Thread.currentThread() + ":enqueue " + count);
                    countDownLatch.countDown();
                }
            }).start();

        }
        try {
            countDownLatch.await();
            shutdownHook("Rough Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRoughPriorityQueue2() {
        int n = testCount;
        final Queue<Person> queue = new RoughPriorityQueue<>("Rough Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Person dequeue = queue.dequeue();
                    log.info(Thread.currentThread() + ": dequeue " + dequeue);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Rough Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRoughPriorityQueue3() {
        int n = testCount;
        final Queue<Person> queue = new RoughPriorityQueue<>("Rough Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Person top = queue.top();
                    log.info(Thread.currentThread() + ": Top " + top);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Rough Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRoughPriorityQueue4() {
        int n = testCount;
        final Queue<Person> queue = new RoughPriorityQueue<>("Rough Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long size = queue.size();
                    log.info(Thread.currentThread() + ": queue size " + size);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Rough Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRoughPriorityQueue5() {
        int n = testCount;
        final Queue<Person> queue = new RoughPriorityQueue<>("Rough Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean empty = queue.isEmpty();
                    log.info(Thread.currentThread() + ": is empty " + empty);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Rough Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRoughPriorityQueue6() {
        int n = testCount;
        final Queue<Person> queue = new RoughPriorityQueue<>("Rough Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean empty = queue.clear();
                    log.info(Thread.currentThread() + ": is clear " + empty);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Rough Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * ================= testPerfectPriorityQueue =================
     */

    @Test
    public void testPerfectPriorityQueue() {
        int n = testCount;
        final Queue<String> queue = new PerfectPriorityQueue<>("Perfect Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            final int count = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    queue.enqueue(count, count + "");
                    log.info(Thread.currentThread() + ":enqueue " + count);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Perfect Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPerfectPriorityQueue1() {
        int n = testCount;
        final Queue<Person> queue = new PerfectPriorityQueue<>("Perfect Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            final int count = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    queue.enqueue(count, new Person("Tom_" + count));
                    log.info(Thread.currentThread() + ":enqueue Tom_" + count);
                    countDownLatch.countDown();
                }

                ;
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Perfect Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPerfectPriorityQueue2() {
        int n = testCount;
        final Queue<Person> queue = new PerfectPriorityQueue<>("Perfect Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Person dequeue = queue.dequeue();
                    log.info(Thread.currentThread() + ": dequeue " + dequeue);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Perfect Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPerfectPriorityQueue3() {
        int n = testCount;
        final Queue<String> queue = new PerfectPriorityQueue<>("Perfect Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Object top = queue.top();
                    log.info(Thread.currentThread() + ": Top " + top);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Perfect Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPerfectPriorityQueue4() {
        int n = testCount;
        final Queue<String> queue = new PerfectPriorityQueue<>("Perfect Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long size = queue.size();
                    log.info(Thread.currentThread() + ": queue size " + size);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Perfect Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPerfectPriorityQueue5() {
        int n = testCount;
        final Queue<String> queue = new PerfectPriorityQueue<>("Perfect Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean empty = queue.isEmpty();
                    log.info(Thread.currentThread() + ": is empty " + empty);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Perfect Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPerfectPriorityQueue6() {
        int n = testCount;
        final Queue<String> queue = new PerfectPriorityQueue<>("Perfect Priority Queue", jedis);
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean empty = queue.clear();
                    log.info(Thread.currentThread() + ": is clear " + empty);
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Perfect Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void shutdownHook(final String name, final String method, final long spendTime) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                log.info(name + " " + method + " spend time " + spendTime + "ms for " + testCount + " Thread.");
            }
        }));
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    static class Person implements Serializable {
        private static final long serialVersionUID = 1L;
        String name;

        Person(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Person [name=" + name + "]";
        }

    }
}

