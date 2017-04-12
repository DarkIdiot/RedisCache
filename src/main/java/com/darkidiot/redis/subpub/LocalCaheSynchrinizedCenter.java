package com.darkidiot.redis.subpub;

import static com.darkidiot.redis.common.Method.valueOf;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONObject;
import com.darkidiot.redis.common.Method;
import com.darkidiot.redis.config.JedisPoolFactory;
import com.darkidiot.redis.LocalMap;
import com.darkidiot.redis.config.IPorServerConfig;
import com.darkidiot.redis.util.ByteObjectConvertUtil;
import com.darkidiot.redis.util.UUIDUtil;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
/**
 * Redis 消息订阅发布中心
 * @author darkidiot
 */
@Slf4j
public class LocalCaheSynchrinizedCenter {
	/** 发布订阅主题 */
	public static final String TOPIC_SYNCHRONIZED_LOCAL_CACHE = "TOPIC_SYNCHRONIZED_LOCAL_CACHE";
	/** 当前环境Map列表 */
	private static final Map<String, LocalMap<String, ? extends Serializable>> LOCAL_CACHES = new ConcurrentHashMap<String,LocalMap<String, ? extends Serializable>>();
	
	/** 发布客户端ID (自己发布的消息自己不消费) */
	private static final String CLIENT_ID = UUIDUtil.generateShortUUID();
	
	/** 服务开启就开启本地缓存同步策略  */
	static{
		LocalCaheSynchrinizedCenterThread caheSynchrinizedThread = new LocalCaheSynchrinizedCenterThread();
		caheSynchrinizedThread.start();
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				// 销毁订阅线程
				LocalCaheSynchrinizedCenterThread.flag = false;
				try {
					caheSynchrinizedThread.interrupt();
					caheSynchrinizedThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	/**
	 * 订阅消息
	 * @param name 配置名称
	 * @param jedis IJedis
	 * @param localCache 本地缓存
	 */
	public synchronized static void subscribe(LocalMap<String, ? extends Serializable> localCache){
		LocalMap<String, ? extends Serializable > cache = LOCAL_CACHES.get(localCache.getName());
		if( cache == null ){
			LOCAL_CACHES.put(localCache.getName(), localCache);
			log.info("add LocalCache [" + localCache.getName() + "] to cache maps. And cache maps's size is" + LOCAL_CACHES.size());
		}
	}
	
	/**
	 * 消费消息
	 * @param maps Redis相关的Map
	 * @param message 消息
	 */
	public static void consume(String message){
		if( LOCAL_CACHES == null || LOCAL_CACHES == null || message.isEmpty() ){
			log.error("RedisMap consume error message=" + message);
			return;
		}
		JSONObject obj = (JSONObject)JSONObject.parse(message);
		String clientId = obj.getString("clientId");
		if( CLIENT_ID.equals(clientId) ){
			return;//不消费自己发布的消息
		}
		String method = obj.getString("method");
		String key = obj.getString("key");
		Object keyObject = ByteObjectConvertUtil.getObjectFromBytes(key.getBytes());
		String name = obj.getString("name");
		LocalMap<String, ? extends Serializable> localCache = LOCAL_CACHES.get(name);
		if( localCache != null ){
			if(Method.put.equals(Method.valueOf(method)) || Method.remove.equals(Method.valueOf(method)) ){
				localCache.remove(keyObject.toString());
				log.info("RedisMap consume " + name + ".remove(" + key + ") " + message);
			}else if( Method.clear.equals(Method.valueOf(method)) ){
				localCache.clear();
				log.info("RedisMap consume " + name + ".clear() " + message);
			}else{
				log.info("RedisMap consume " + name + "." + method + "() " + message);
			}
		}else{
			//log.info("RedisMap can't find LocalMap " + name);
		}
	}
	
	/**
	 * 发布消息
	 * @param name Map名称
	 * @param method 方法名称
	 * @param key 键值
	 */
	public static <K extends Serializable> void publish(String name, Method method, K key){
			Jedis jedis = JedisPoolFactory.getWritePool().getResource();
		try {
			JSONObject obj = new JSONObject();
			obj.put("clientId", CLIENT_ID);
			obj.put("serverId", IPorServerConfig.getServerId());
			obj.put("name", name);
			obj.put("method", method);
			obj.put("key", new String(ByteObjectConvertUtil.getBytesFromObject(key)));
			String message = obj.toJSONString();
			jedis.publish(TOPIC_SYNCHRONIZED_LOCAL_CACHE, message);
			log.info("RedisMap publish " + message);
		} catch (Exception e) {
			log.error("RedisMap publish error " + e.getMessage());
			e.printStackTrace();
		}finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
	
	/**订阅线程*/
	public static class LocalCaheSynchrinizedCenterThread extends Thread{
		
		public static volatile boolean flag = true;
		
		public LocalCaheSynchrinizedCenterThread() {
			this.setName("RedisMap subscribe " + TOPIC_SYNCHRONIZED_LOCAL_CACHE + " " + LOCAL_CACHES.size());
			this.setDaemon(true);
		}

		@Override
		public void run() {
			while(flag){//订阅错误，重新订阅
				subscribe();
			}
		}
		
		/**
		 * 订阅消息
		 */
		private void subscribe(){
			Jedis jedis = JedisPoolFactory.getWritePool().getResource();
			try{
				log.info("RedisMap subscribe start... " + TOPIC_SYNCHRONIZED_LOCAL_CACHE + "("  + LOCAL_CACHES.size() + ")");
				jedis.subscribe(new AbstractJedisPubSub() {
					@Override
					public void onMessage(String channel, String message) {
						try {
							if(TOPIC_SYNCHRONIZED_LOCAL_CACHE.equals(channel)){
								consume(message);
							}
						} catch (Exception e) {
							log.error("RedisMap consume error " + message, e);
						}
					}
				}, TOPIC_SYNCHRONIZED_LOCAL_CACHE);
			}catch(Exception e){
				log.error("RedisMap subscribe error " + e.getMessage() + ".");
			}finally{
				if (jedis != null) {
					jedis.close();
				}
				log.error("RedisMap subscribe stop... resubscribe 5s later.");
				try{Thread.sleep(5000);} catch (Exception e) {}
			}
		}
	}
}
