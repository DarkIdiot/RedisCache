package com.darkidiot.base;

import com.darkidiot.redis.queue.Queue;
import com.darkidiot.redis.queue.impl.PerfectPriorityQueue;
import com.darkidiot.redis.queue.impl.RoughPriorityQueue;
import com.darkidiot.redis.queue.impl.SimpleFifoQueue;
import com.darkidiot.redis.queue.impl.SimplePriorityQueue;
import com.darkidiot.redis.util.ByteObjectConvertUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;

@FixMethodOrder(MethodSorters.JVM)
@Slf4j
public class QueueTest {
    private static JedisPool pool;

    private int testCount = 2; // 2000、5000、10000

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        pool = new JedisPool("127.0.0.1", 6379);
    }

    /**
     * ================= testSimpleFifoQueue =================
     */
    @Test
    public void testSimpleFifoQueue() {
        Jedis resource = pool.getResource();
        resource.del("Queue:Simple Fifo Queue");
        resource.close();
        int n = testCount;
        Queue<Person> queue = new SimpleFifoQueue<>("Simple Fifo Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            int count = i;
            new Thread(() -> {
                queue.enqueue(new Person(count + ""));
                log.info(Thread.currentThread() + ": enqueue " + count);
                countDownLatch.countDown();
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
        Queue<Person> queue = new SimpleFifoQueue<>("Simple Fifo Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                Person dequeue = queue.dequeue();
                log.info(Thread.currentThread() + ": dequeue " + dequeue);
                countDownLatch.countDown();
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
        Queue<Person> queue = new SimpleFifoQueue<>("Simple Fifo Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                Person top = queue.top();
                log.info(Thread.currentThread() + ": Top " + top);
                countDownLatch.countDown();
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
        Queue<String> queue = new SimpleFifoQueue<>("Simple Fifo Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                long size = queue.size();
                log.info(Thread.currentThread() + ": queue size " + size);
                countDownLatch.countDown();
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
        Queue<String> queue = new SimpleFifoQueue<>("Simple Fifo Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                boolean empty = queue.isEmpty();
                log.info(Thread.currentThread() + ": is empty " + empty);
                countDownLatch.countDown();
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
        Queue<String> queue = new SimpleFifoQueue<>("Simple Fifo Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                boolean empty = queue.clear();
                log.info(Thread.currentThread() + ": is clear " + empty);
                countDownLatch.countDown();
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
        Jedis resource = pool.getResource();
        resource.del("Queue:Lowly Priority Queue:Simple Priority Queue");
        resource.del("Queue:Highly Priority Queue:Simple Priority Queue");
        resource.close();
        int n = testCount;
        Queue<Person> queue = new SimplePriorityQueue<>("Simple Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(2 * n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            int count = i;
            new Thread(() -> {
                queue.enqueue(new Person(count + ""));
                log.info(Thread.currentThread() + ": lowly enqueue " + count);
                countDownLatch.countDown();
            }).start();

            new Thread(() -> {
                queue.enqueue(1, new Person(count + ""));
                log.info(Thread.currentThread() + ": highly enqueue " + count);
                countDownLatch.countDown();
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
        Queue<Person> queue = new SimplePriorityQueue<>("Simple Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                Person dequeue = queue.dequeue();
                log.info(Thread.currentThread() + ": dequeue " + dequeue);
                countDownLatch.countDown();
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
        Queue<Person> queue = new SimplePriorityQueue<>("Simple Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                Person top = queue.top();
                log.info(Thread.currentThread() + ": Top " + top);
                countDownLatch.countDown();
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
        Queue<Person> queue = new SimplePriorityQueue<>("Simple Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                long size = queue.size();
                log.info(Thread.currentThread() + ": queue size " + size);
                countDownLatch.countDown();
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
        Queue<Person> queue = new SimplePriorityQueue<>("Simple Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                boolean empty = queue.isEmpty();
                log.info(Thread.currentThread() + ": is empty " + empty);
                countDownLatch.countDown();
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
        Queue<Person> queue = new SimplePriorityQueue<>("Simple Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                boolean empty = queue.clear();
                log.info(Thread.currentThread() + ": is clear " + empty);
                countDownLatch.countDown();
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
        Jedis resource = pool.getResource();
        ArrayList<String> list = Lists.newArrayList();
        for (int i = 0; i < n; i++) {
            list.add("Queue:Rough Priority Queue:" + i);
        }
        resource.del(list.toArray(new String[list.size()]));
        resource.close();
        Queue<Person> queue = new RoughPriorityQueue<>("Rough Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            int count = i;
            new Thread(() -> {
                queue.enqueue(count, new Person(count + ""));
                log.info(Thread.currentThread() + ":enqueue " + count);
                countDownLatch.countDown();
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
        Queue<Person> queue = new RoughPriorityQueue<>("Rough Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                Person dequeue = queue.dequeue();
                log.info(Thread.currentThread() + ": dequeue " + dequeue);
                countDownLatch.countDown();
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
        Queue<Person> queue = new RoughPriorityQueue<>("Rough Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                Person top = queue.top();
                log.info(Thread.currentThread() + ": Top " + top);
                countDownLatch.countDown();
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
        Queue<Person> queue = new RoughPriorityQueue<>("Rough Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                long size = queue.size();
                log.info(Thread.currentThread() + ": queue size " + size);
                countDownLatch.countDown();
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
        Queue<Person> queue = new RoughPriorityQueue<>("Rough Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                boolean empty = queue.isEmpty();
                log.info(Thread.currentThread() + ": is empty " + empty);
                countDownLatch.countDown();
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
        Queue<Person> queue = new RoughPriorityQueue<>("Rough Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                boolean empty = queue.clear();
                log.info(Thread.currentThread() + ": is clear " + empty);
                countDownLatch.countDown();
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
        Jedis resource = pool.getResource();
        resource.del("Queue:Perfect Priority Queue");
        resource.close();
        Queue<String> queue = new PerfectPriorityQueue<>("Perfect Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            int count = i;
            new Thread(() -> {
                queue.enqueue(count, count + "");
                log.info(Thread.currentThread() + ":enqueue " + count);
                countDownLatch.countDown();
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
        Jedis resource = pool.getResource();
        resource.del("Queue:Perfect Priority Queue");
        resource.close();
        Queue<Person> queue = new PerfectPriorityQueue<>("Perfect Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            int count = i;
            new Thread(() -> {
                queue.enqueue(count, new Person("Tom_" + count));
                log.info(Thread.currentThread() + ":enqueue Tom_" + count);
                countDownLatch.countDown();
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
        Queue<Person> queue = new PerfectPriorityQueue<>("Perfect Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                Person dequeue = queue.dequeue();
                log.info(Thread.currentThread() + ": dequeue " + dequeue);
                countDownLatch.countDown();
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
        Queue<String> queue = new PerfectPriorityQueue<>("Perfect Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                Object top = queue.top();
                log.info(Thread.currentThread() + ": Top " + top);
                countDownLatch.countDown();
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
        Queue<String> queue = new PerfectPriorityQueue<>("Perfect Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                long size = queue.size();
                log.info(Thread.currentThread() + ": queue size " + size);
                countDownLatch.countDown();
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
        Queue<String> queue = new PerfectPriorityQueue<>("Perfect Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                boolean empty = queue.isEmpty();
                log.info(Thread.currentThread() + ": is empty " + empty);
                countDownLatch.countDown();
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
        Queue<String> queue = new PerfectPriorityQueue<>("Perfect Priority Queue", pool);
        CountDownLatch countDownLatch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                boolean empty = queue.clear();
                log.info(Thread.currentThread() + ": is clear " + empty);
                countDownLatch.countDown();
            }).start();
        }
        try {
            countDownLatch.await();
            shutdownHook("Perfect Priority Queue", "enqueue", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void shutdownHook(String name, String method, long spendTime) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> log.info(name + " " + method + " spend time " + spendTime + "ms for " + testCount + " Thread.")));
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        pool.destroy();
    }

    static class Person implements Serializable {
        private static final long serialVersionUID = 1L;
        String name;

        public Person(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Person [name=" + name + "]";
        }

    }
}

