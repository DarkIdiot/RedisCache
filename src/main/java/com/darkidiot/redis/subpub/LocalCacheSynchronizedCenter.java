package com.darkidiot.redis.subpub;

import com.darkidiot.redis.LocalMap;
import com.darkidiot.redis.common.Method;
import com.darkidiot.redis.config.IPorServerConfig;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.util.ByteObjectConvertUtil;
import com.darkidiot.redis.util.UUIDUtil;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
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
     * 发布订阅主题前缀:前缀+服务
     */
    private static final String TOPIC_SYNCHRONIZED_LOCAL_CACHE_PREFIX = "TOPIC_SYNCHRONIZED_LOCAL_CACHE:";
    /**
     * 当前环境Map列表
     */
    private static final Map<String, LocalMap<String, ? extends Serializable>> LOCAL_CACHES = new ConcurrentHashMap<>();

    /**
     * 当前环境已经订阅本地缓存服务集合
     */
    private static final Set<String> subscribeServiceSet = Sets.newConcurrentHashSet();

    /**
     * 发布客户端ID (自己发布的消息自己不消费)
     */
    private static final String CLIENT_ID = UUIDUtil.generateShortUUID();

    private static final Gson gson = new GsonBuilder().create();
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                    threadPool.shutdown();
                    log.info("Subscribe Thread Pool has been shutdown now.");
            }
        });
    }

    /**
     * 订阅消息
     *
     * @param localCache 本地缓存
     */
    public synchronized static void subscribe(final String service, IJedis jedis, LocalMap<String, ? extends Serializable> localCache) {
        /** 服务开启就开启本地缓存同步策略  */
        if (!subscribeServiceSet.contains(service)) {
            final LocalCacheSynchronizedCenterThread cacheSynchronizedCenterThread = new LocalCacheSynchronizedCenterThread(service, jedis);
            threadPool.execute(cacheSynchronizedCenterThread);
            subscribeServiceSet.add(service);
            // 销毁订阅线程钩子
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        cacheSynchronizedCenterThread.flag = false;
                        cacheSynchronizedCenterThread.interrupt();
                        cacheSynchronizedCenterThread.join();
                        log.info("{} was closed", cacheSynchronizedCenterThread.getName());
                    } catch (InterruptedException e) {
                        log.error("Thread was Interrupted, cause by:{}", Throwables.getStackTraceAsString(e));
                    }finally {
                        subscribeServiceSet.remove(service);
                    }
                }
            });
        }

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
     * @param service   服务名称
     * @param groupName Map名称
     * @param method    方法名称
     * @param key       键值
     */
    public static <K extends Serializable> void publish(IJedis jedis, String service, String groupName, Method method, K key) {
        try {
            MsgVo msg = new MsgVo(CLIENT_ID, IPorServerConfig.getServerId(service), method, groupName, ByteObjectConvertUtil.getBytesFromObject(key));
            String json = gson.toJson(msg);
            jedis.publish(buildSynchronizedTopic(service), json);
            log.info("local cache publish message:{} ", json);
        } catch (Exception e) {
            log.error("local cache publish error,cause by:{},stackTrace:{}", e.getMessage(), e.getStackTrace());
        }
    }

    private static String buildSynchronizedTopic(String service) {
        return TOPIC_SYNCHRONIZED_LOCAL_CACHE_PREFIX + service;
    }

    /**
     * 订阅线程
     */
    private static class LocalCacheSynchronizedCenterThread extends Thread {

        volatile boolean flag = true;
        private final String serviceName;
        private final ExecutorService threadPool;
        private final IJedis jedis;

        LocalCacheSynchronizedCenterThread(String serviceName, IJedis jedis) {
            this.setName("Thread-" + buildSynchronizedTopic(serviceName));
            this.setDaemon(true);
            this.serviceName = serviceName;
            this.jedis = jedis;
            threadPool = Executors.newCachedThreadPool();
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
            threadPool.shutdown();
            log.info("{}: Consumer Thread Pool has been shutdown now.", getName());
        }

        /**
         * 订阅消息
         */
        private void subscribe() throws InterruptedException {
            try {
                log.info("RedisMap subscribe start... ");
                jedis.subscribe(new AbstractJedisPubSub() {
                    @Override
                    public void onMessage(String channel, final String message) {
                        try {
                            threadPool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    consume(message);
                                }
                            });
                        } catch (Exception e) {
                            log.error("RedisMap consume error,cause by：{}", Throwables.getStackTraceAsString(e));
                        }
                    }
                }, buildSynchronizedTopic(serviceName));
            } catch (Exception e) {
                log.error("RedisMap subscribe error {}.", e.getMessage());
            } finally {
                log.error("RedisMap subscribe stop... resubscribe 5s later.");
                Thread.sleep(5000);
            }
        }
    }
}
