package com.darkidiot.redis.subpub;

import com.darkidiot.redis.LocalMap;
import com.darkidiot.redis.common.Method;
import com.darkidiot.redis.config.IPorServerConfig;
import com.darkidiot.redis.config.JedisPoolFactory;
import com.darkidiot.redis.util.ByteObjectConvertUtil;
import com.darkidiot.redis.util.UUIDUtil;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Redis 消息订阅发布中心
 *
 * @author darkidiot
 */
@Slf4j
public class LocalCacheSynchronizedCenter {
    /**
     * 发布订阅主题
     */
    private static final String TOPIC_SYNCHRONIZED_LOCAL_CACHE = "TOPIC_SYNCHRONIZED_LOCAL_CACHE";
    /**
     * 当前环境Map列表
     */
    private static final Map<String, LocalMap<String, ? extends Serializable>> LOCAL_CACHES = new ConcurrentHashMap<>();

    /**
     * 发布客户端ID (自己发布的消息自己不消费)
     */
    private static final String CLIENT_ID = UUIDUtil.generateShortUUID();

    private static final Gson gson = new GsonBuilder().create();

    /** 服务开启就开启本地缓存同步策略  */
    static {
        ExecutorService executorService = Executors.newFixedThreadPool(30);
        for (int i = 0; i < JedisPoolFactory.getInitParam().getSubscribeThreadNum(); i++) {
            LocalCacheSynchronizedCenterThread cacheSynchronizedCenterThread = new LocalCacheSynchronizedCenterThread();
            executorService.execute(cacheSynchronizedCenterThread);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    // 销毁订阅线程
                    LocalCacheSynchronizedCenterThread.flag = false;
                    try {
                        cacheSynchronizedCenterThread.interrupt();
                        cacheSynchronizedCenterThread.join();
                        log.error("Thread-{}-{}  was closed", cacheSynchronizedCenterThread.getName(), cacheSynchronizedCenterThread.getId());
                    } catch (InterruptedException e) {
                        log.error("Thread was Interrupted, cause by:{}", Throwables.getStackTraceAsString(e));
                    }
                }
            });
        }
    }


    /**
     * 订阅消息
     *
     * @param localCache 本地缓存
     */
    public synchronized static void subscribe(LocalMap<String, ? extends Serializable> localCache) {
        LocalMap<String, ? extends Serializable> cache = LOCAL_CACHES.get(localCache.getName());
        if (cache == null) {
            LOCAL_CACHES.put(localCache.getName(), localCache);
            log.info("add Local Cache [" + localCache.getName() + "] to cache maps. And cache maps's size is" + LOCAL_CACHES.size());
        }
    }

    /**
     * 消费消息
     *
     * @param message 消息
     */
    private static void consume(String message) {
        if (message.isEmpty()) {
            log.error("RedisMap consume error message:{}", message);
            return;
        }
        MsgVo msg = gson.fromJson(message, MsgVo.class);
        if (CLIENT_ID.equals(msg.getClientId())) {
            return;//不消费自己发布的消息
        }
        LocalMap<String, ? extends Serializable> localCache = LOCAL_CACHES.get(msg.getGroupName());
        if (localCache != null) {
            if (Method.put.equals(msg.getMethod()) || Method.remove.equals(msg.getMethod())) {
                localCache.remove(msg.getKey());
                log.info("local cache consume message:{}", message);
            } else if (Method.clear.equals(msg.getMethod())) {
                localCache.clear();
                log.info("local cache consume message:{}", message);
            } else {
                log.info("local cache can not consume message:{}", message);
            }
        }
    }

    /**
     * 发布消息
     *
     * @param groupName Map名称
     * @param method    方法名称
     * @param key       键值
     */
    public static <K extends Serializable> void publish(String groupName, Method method, K key) {
        try (Jedis jedis = JedisPoolFactory.getWritePool().getResource()) {
            MsgVo msg = new MsgVo(CLIENT_ID, IPorServerConfig.getServerId(), method, groupName, ByteObjectConvertUtil.getBytesFromObject2(key));
            String json = gson.toJson(msg);
            jedis.publish(TOPIC_SYNCHRONIZED_LOCAL_CACHE, json);
            log.info("local cache publish message:{} ", json);
        } catch (Exception e) {
            log.error("local cache publish error,cause by:{},stackTrace:{}", e.getMessage(), e.getStackTrace());
        }
    }

    /**
     * 订阅线程
     */
    private static class LocalCacheSynchronizedCenterThread extends Thread {

        static volatile boolean flag = true;

        LocalCacheSynchronizedCenterThread() {
            this.setName("RedisMap subscribe " + TOPIC_SYNCHRONIZED_LOCAL_CACHE + " " + LOCAL_CACHES.size());
            this.setDaemon(true);
        }

        @Override
        public void run() {
            while (flag) {//订阅错误，重新订阅
                try {
                    subscribe();
                } catch (InterruptedException e) {
                    log.error("Sleep subscribe thread was interrupted,cause by:{}", Throwables.getStackTraceAsString(e));
                }
            }
        }

        /**
         * 订阅消息
         */
        private void subscribe() throws InterruptedException {
            try (Jedis jedis = JedisPoolFactory.getWritePool().getResource()) {
                log.info("RedisMap subscribe start... ");
                log.info("TOPIC:{},local_cache size:{}", LOCAL_CACHES.size());
                jedis.subscribe(new AbstractJedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        try {
                            if (TOPIC_SYNCHRONIZED_LOCAL_CACHE.equals(channel)) {
                                consume(message);
                            }
                        } catch (Exception e) {
                            log.error("RedisMap consume error,cause by：{}", Throwables.getStackTraceAsString(e));
                        }
                    }
                }, TOPIC_SYNCHRONIZED_LOCAL_CACHE);
            } catch (Exception e) {
                log.error("RedisMap subscribe error {}.", e.getMessage());
            } finally {
                log.error("RedisMap subscribe stop... resubscribe 5s later.");
                Thread.sleep(5000);
            }
        }
    }
}
