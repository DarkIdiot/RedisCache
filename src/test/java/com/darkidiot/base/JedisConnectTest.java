package com.darkidiot.base;

import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.Transaction;

@Slf4j
public class JedisConnectTest {
	private static Jedis jedis;
    private static ShardedJedis sharding;
    private static ShardedJedisPool pool;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        List<JedisShardInfo> shards = Arrays.asList(
                new JedisShardInfo("127.0.0.1",6379),
                new JedisShardInfo("127.0.0.1",6379)); //使用相同的ip:port,仅作测试


        jedis = new Jedis("127.0.0.1",6379); 
        sharding = new ShardedJedis(shards);
        pool = new ShardedJedisPool(new JedisPoolConfig(), shards);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        jedis.disconnect();
        sharding.disconnect();
        pool.destroy();
    }

    @Test
    public void test1Normal() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            String result = jedis.set("n" + i, "n" + i);
        }
        long end = System.currentTimeMillis();
        log.info("Simple SET: " + ((end - start)/1000.0) + " seconds");
    }

    @Test
    public void test2Trans() {
        long start = System.currentTimeMillis();
        Transaction tx = jedis.multi();
        for (int i = 0; i < 100000; i++) {
            tx.set("t" + i, "t" + i);
        }
        //log.info(tx.get("t1000").get());

        List<Object> results = tx.exec();
        long end = System.currentTimeMillis();
        log.info("Transaction SET: " + ((end - start)/1000.0) + " seconds");
    }

    @Test
    public void test3Pipelined() {
        Pipeline pipeline = jedis.pipelined();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            pipeline.set("p" + i, "p" + i);
        }
        //log.info(pipeline.get("p1000").get());
        List<Object> results = pipeline.syncAndReturnAll();
        long end = System.currentTimeMillis();
        log.info("Pipelined SET: " + ((end - start)/1000.0) + " seconds");
    }

    @Test
    public void test4combPipelineTrans() {
        long start = System.currentTimeMillis();
        Pipeline pipeline = jedis.pipelined();
        pipeline.multi();
        for (int i = 0; i < 100000; i++) {
            pipeline.set("" + i, "" + i);
        }
        pipeline.exec();
        List<Object> results = pipeline.syncAndReturnAll();
        long end = System.currentTimeMillis();
        log.info("Pipelined transaction: " + ((end - start)/1000.0) + " seconds");
    }

    @Test
    public void test5shardNormal() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            String result = sharding.set("sn" + i, "n" + i);
        }
        long end = System.currentTimeMillis();
        log.info("Simple@Sharing SET: " + ((end - start)/1000.0) + " seconds");
    }

    @Test
    public void test6shardpipelined() {
        ShardedJedisPipeline pipeline = sharding.pipelined();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            pipeline.set("sp" + i, "p" + i);
        }
        List<Object> results = pipeline.syncAndReturnAll();
        long end = System.currentTimeMillis();
        log.info("Pipelined@Sharing SET: " + ((end - start)/1000.0) + " seconds");
    }

    @Test
    public void test7shardSimplePool() {
        ShardedJedis one = pool.getResource();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            String result = one.set("spn" + i, "n" + i);
        }
        long end = System.currentTimeMillis();
        one.close();
        log.info("Simple@Pool SET: " + ((end - start)/1000.0) + " seconds");
    }

    @Test
    public void test8shardPipelinedPool() {
        ShardedJedis one = pool.getResource();

        ShardedJedisPipeline pipeline = one.pipelined();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            pipeline.set("sppn" + i, "n" + i);
        }
        List<Object> results = pipeline.syncAndReturnAll();
        long end = System.currentTimeMillis();
        one.close();
        log.info("Pipelined@Pool SET: " + ((end - start)/1000.0) + " seconds");
    }
}
