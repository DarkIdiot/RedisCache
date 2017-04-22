package com.darkidiot.base;

import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * SubPubTest 测试类
 * Copyright (c) for darkidiot
 * Date:2017/4/22
 * Author: <a href="darkidiot@icloud.com">darkidiot</a>
 * School: CUIT
 * Desc:
 */
@Slf4j
public class SubPubTest {

    private static Jedis jedis;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        jedis = new Jedis("127.0.0.1", 6379);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        jedis.disconnect();
    }

    @Test
    public void testSubscribe() {
        RedisMsgPubSubListener listener = new RedisMsgPubSubListener();
        /**
         * 注意：subscribe是一个阻塞的方法，在取消订阅该频道前，会一直阻塞在这，只有当取消了订阅才会执行下面的other code，
         * 参考上面代码，我在onMessage里面收到消息后，调用了this.unsubscribe(); 来取消订阅，这样才会执行后面的other code
         */
        jedis.subscribe(listener, "redisChat");
        //如果没有取消订阅,方法将一直堵塞在此处不会向下执行
    }

    @Test
    public void testPublish() throws InterruptedException {
        jedis.publish("redisChat", "Redis is a great caching technique");
        Thread.sleep(5000);
        jedis.publish("redisChat", "build your dream");
        Thread.sleep(5000);
        jedis.publish("redisChat", "over");
    }

    private static class RedisMsgPubSubListener extends JedisPubSub {

        @Override
        public void onMessage(String channel, String message) {
            log.info("channel: {} receives message :{}", channel, message);
            //this.unsubscribe();//取消订阅
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
            log.info("channel: {} is been subscribed", subscribedChannels);
        }

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
            log.info("channel: {} is been unSubscribed:" + subscribedChannels);
        }
    }
}
