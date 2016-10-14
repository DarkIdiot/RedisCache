package com.gome.superman.redis.base;

import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gome.supermain.redis.lock.Lock;
import com.gome.supermain.redis.lock.imp.RigorousRedisLock;
import com.gome.supermain.redis.lock.imp.SimpleRedisLock;
import com.gome.supermain.redis.lock.imp.StrictRedisLock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class LockTest {
	private static JedisPool pool;
	
	private int testCount = 2000; // 2000、5000、10000
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		pool = new JedisPool("127.0.0.1", 6379);
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
		Lock lock = new SimpleRedisLock(pool , "Simple RedisLock");
		int n = testCount;
		CountDownLatch countDownLatch = new CountDownLatch(n);
		long start = System.currentTimeMillis();
		for (int i = 0; i < n; i++) {
			new Thread(()->{
				String identifier = lock.lock();
				Jedis resource = pool.getResource();
				System.out.println(Thread.currentThread() + ":" +identifier);
				String count = resource.get("Count");
				System.out.println(Thread.currentThread() + ":" + count);
				resource.set("Count", Integer.valueOf(count) + 1 + "");
				boolean unlockFlag = lock.unlock(identifier);
				System.out.println(Thread.currentThread() + ":" +unlockFlag);
				resource.close();
				countDownLatch.countDown();
			}).start();
		}
		try {
			countDownLatch.await();
			long spendTime = System.currentTimeMillis() - start;
			Runtime.getRuntime().addShutdownHook(new Thread(
					()->{
						System.out.println("Simple RedisLock spend time "+spendTime+"ms for "+testCount+" Thread.");
					}
					));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testStrictLock() {
		Lock lock = new StrictRedisLock(pool , "Strict RedisLock");
		int n = testCount;
		CountDownLatch countDownLatch = new CountDownLatch(n);
		long start = System.currentTimeMillis();
		for (int i = 0; i < n; i++) {
			new Thread(()->{
				String identifier = lock.lock();
				Jedis resource = pool.getResource();
				System.out.println(Thread.currentThread() + ":" +identifier);
				String count = resource.get("Count");
				System.out.println(Thread.currentThread() + ":" + count);
				resource.set("Count", Integer.valueOf(count) + 1 + "");
				boolean unlockFlag = lock.unlock(identifier);
				System.out.println(Thread.currentThread() + ":" +unlockFlag);
				resource.close();
				countDownLatch.countDown();
			}).start();
		}
		try {
			countDownLatch.await();
			long spendTime = System.currentTimeMillis() - start;
			Runtime.getRuntime().addShutdownHook(new Thread(
					()->{
						System.out.println("Strict RedisLock spend time "+spendTime+"ms for "+testCount+" Thread.");
					}
					));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRigorousLock() {
		Lock lock = new RigorousRedisLock(pool , "Rigorous RedisLock");
		int n = testCount;
		CountDownLatch countDownLatch = new CountDownLatch(n);
		long start = System.currentTimeMillis();
		for (int i = 0; i < n; i++) {
			new Thread(()->{
				String identifier = lock.lock();
				Jedis resource = pool.getResource();
				System.out.println(Thread.currentThread() + ":" +identifier);
				String count = resource.get("Count");
				System.out.println(Thread.currentThread() + ":" + count);
				resource.set("Count", Integer.valueOf(count) + 1 + "");
				boolean unlockFlag = lock.unlock(identifier);
				System.out.println(Thread.currentThread() + ":" +unlockFlag);
				resource.close();
				countDownLatch.countDown();
			}).start();
		}
		
		try {
			countDownLatch.await();
			long spendTime = System.currentTimeMillis() - start;
			Runtime.getRuntime().addShutdownHook(new Thread(
					()->{
						System.out.println("Rigorous RedisLock spend time "+spendTime+"ms for "+testCount+" Thread.");
					}
					));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
}
