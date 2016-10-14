package com.gome.superman.redis.base;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gome.supermain.redis.lock.Lock;
import com.gome.supermain.redis.lock.imp.RigorousRedisLock;
import com.gome.supermain.redis.lock.imp.StrictRedisLock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class InventoryImitationTest {

	private static JedisPool pool;
	
	private int ThreadCount = 10000; //20000、5000、1000
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		pool = new JedisPool("127.0.0.1", 6379);
		Jedis resource = pool.getResource();
		resource.set("Count", "10000");
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		pool.destroy();
	}
	
	@Test
	public void testStrictLock4Inventory(){
		Lock lock = new StrictRedisLock(pool, "Strict RedisLock");
		int n = ThreadCount;
		CountDownLatch countDownLatch = new CountDownLatch(n);
		long start = System.currentTimeMillis();
		for (int i = 0; i < n; i++) {
			new Thread(()->{
				String identifier = lock.lock();
				Jedis resource = pool.getResource();
				System.out.println(Thread.currentThread() + ":" +identifier);
				String count = resource.get("Count");
				int minus = new Random().nextInt(15);
				System.out.println(Thread.currentThread() + ":" + count + " minus by "+minus);
				if (minus < Integer.valueOf(count)) {
					resource.set("Count", Integer.valueOf(count) - minus + "");
				}else{
					System.err.println(Thread.currentThread() + ":" + "low stocks! Inventory has " + Integer.valueOf(count) + ". can not minus "+minus+".");
				}
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
						System.out.println("Strict RedisLock spend time "+spendTime+"ms for "+ThreadCount+" Thread.");
					}
					));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRigorousLock4Inventory(){
		Lock lock = new RigorousRedisLock(pool, "Rigorous RedisLock");
		int n = ThreadCount;
		CountDownLatch countDownLatch = new CountDownLatch(n);
		long start = System.currentTimeMillis();
		for (int i = 0; i < n; i++) {
			new Thread(()->{
				String identifier = lock.lock();
				Jedis resource = pool.getResource();
				System.out.println(Thread.currentThread() + ":" +identifier);
				String count = resource.get("Count");
				int minus = new Random().nextInt(15);
				System.out.println(Thread.currentThread() + ":" + count + " minus by "+minus);
				if (minus < Integer.valueOf(count)) {
					resource.set("Count", Integer.valueOf(count) - minus + "");
				}else{
					System.err.println(Thread.currentThread() + ":" + "low stocks! Inventory has " + Integer.valueOf(count) + ". can not minus "+minus+".");
				}
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
						System.out.println("Rigorous RedisLock spend time "+spendTime+"ms for "+ThreadCount+" Thread.");
					}
					));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
