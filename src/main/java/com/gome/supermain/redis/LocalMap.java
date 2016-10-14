package com.gome.supermain.redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.gome.supermain.redis.util.ByteObjectConvertUtil;
import com.gome.supermain.redis.validate.KeyValidation;
import com.gome.supermain.redis.validate.NopValidation;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
public class LocalMap<K extends Serializable, V extends Serializable> implements IRedisMap<K, V> {
	
	//本地缓存
	private static Map<Object, CacheEntry> cacheMap = new ConcurrentHashMap<Object, CacheEntry>();
	private static final String GROUP_SEP = "#";
	/** 缓存定时清除器 */
	private static Timer timer = new Timer("LocalCacheTimer", true);
	//缓存名称
	private String name;
	//缓存失效时间，单位秒
	private long expire = 0;
	//key验证:空操作
	private final KeyValidation<K> nop_validation = new NopValidation<K>();
	
	private static final boolean VALIDATION_FOR_LOACL_CACHE = false;
	
	/**最多容忍50MS延迟*/
	private static final int WARN_TIME_LIMIT = 50;
	static {
		// 每10秒清除过期的缓存
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				clearExpiredCache();
			}
		}, 1000, 1000 * 10);
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				timer.cancel();
			}
		});
	}

	/**
	 * 默认永不失效
	 * @param name map缓存名称，必填，不能为空，用于区分各个缓存
	 */
	public LocalMap(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @param name map缓存名称，必填，不能为空，用于区分各个缓存
	 * @param expire 过期时间
	 */
	public LocalMap(String name, int expire) {
		this.name = name;
		this.expire = expire;
	}

	/**
	 * 清除过期的缓存
	 */
	private static void clearExpiredCache() {
		if (cacheMap != null && cacheMap.size() > 0) {
			Iterator<Entry<Object, CacheEntry>> iterator = cacheMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Object, CacheEntry> entry = iterator.next();
				if (entry == null) {
					continue;
				}
				CacheEntry cacheEntry = entry.getValue();
				// 缓存已经过期
				if (cacheEntry == null || cacheEntry.isExpired()) {
					iterator.remove();
				}
			}
		}
	}
	
	@Override
	public void put(K key, V value) {
		put(key, value, nop_validation);
	}

	@Override
	public void put(K key, V value, KeyValidation<K>... validations) {
		if (key == null) {
			return;
		}
		
		if (VALIDATION_FOR_LOACL_CACHE) {
			if(keyValidations(key, validations)){
				return;
			}
		}
		
		long time = System.currentTimeMillis();
		try {
		long deadline = expire == 0 ? 0 : System.currentTimeMillis() + 1000 * expire;
		cacheMap.put(getUniqueKey(key), new CacheEntry(value,deadline));
		} finally {
			long spend = System.currentTimeMillis() - time;
			if (spend > WARN_TIME_LIMIT) {
				log.warn("LocalMap[ {} ] put[ {} - {} ] spend[ {}ms ].", new Object[] { name, key, value, spend });
			}
		}
	}

	@Override
	public V get(K key) {
		return get(key,nop_validation);
	}

	@Override
	public V get(K key, KeyValidation<K>... validations) {
		if (key == null) {
			return null;
		}
		
		if (VALIDATION_FOR_LOACL_CACHE) {
			if (keyValidations(key, validations)) {
				return null;
			}
		}
		
		long time = System.currentTimeMillis();
		try {
			CacheEntry cacheEntry = cacheMap.get(getUniqueKey(key));
			if (cacheEntry == null) {
				return null;
			}
			return (V) cacheEntry.getValue();
		} finally {
			long spend = System.currentTimeMillis() - time;
			if (spend > WARN_TIME_LIMIT) {
				log.warn("LocalMap[ {} ] get[ {} ] spend[ {}ms ].", new Object[] { name, key, spend });
			}
		}
	}

	@Override
	public List<V> getList(List<K> keys) {
		return getList(keys, nop_validation);
	}

	@Override
	public List<V> getList(List<K> keys, KeyValidation<K>... validations) {
		if (keys == null || keys.size() == 0) {
			return null;
		}
		
		if (VALIDATION_FOR_LOACL_CACHE) {
			for(int i =0;i< keys.size();i++){
				if (keyValidations(keys.get(i), validations)) {
					keys.remove(i);
				}
			}
		}
		
		if (keys.size() == 0) {
			return null;
		}
		
		long time = System.currentTimeMillis();
		try {
			List<V> list = new ArrayList<V>();
			for (K k : keys) {
				list.add((V) cacheMap.get(getUniqueKey(k)));
			}
			return list;
		} finally {
			long spend = System.currentTimeMillis() - time;
			if (spend > WARN_TIME_LIMIT) {
				log.warn("LocalMap[ {} ] getList[ {}... ] spend[ {}ms ].", new Object[] { name, keys.get(0), spend });
			}
		}
	}

	@Override
	public void remove(K key) {
		remove(key,nop_validation);
	}

	@Override
	public void remove(K key, KeyValidation<K>... validations) {
		if (key == null) {
			return;
		}
		
		if (VALIDATION_FOR_LOACL_CACHE) {
			if(keyValidations(key, validations)){
				return;
			}
		}
		
		cacheMap.remove(getUniqueKey(key));
	}

	@Override
	public boolean contains(K key) {
		return contains(key,nop_validation);
	}

	@Override
	public boolean contains(K key, KeyValidation<K>... validations) {
		if (key == null) {
			return false;
		}
		
		if (VALIDATION_FOR_LOACL_CACHE) {
			if(keyValidations(key, validations)){
				return false; 
			}
		}
		
		return cacheMap.containsKey(getUniqueKey(key));
	}

	@Override
	public int size() {
		return getAllKeys().size();
	}

	@Override
	public void clear() {
		List<K> keys = getAllKeys();
		for (int i = 0; keys != null && i < keys.size(); i++) {
			cacheMap.remove(keys.get(i));
		}
	}
	
	private List<K> getAllKeys(){
		Set<Object> keySet = cacheMap.keySet();
		List<K> keys = new ArrayList<K>();
		Iterator<Object> iterator = keySet.iterator();
		while(iterator.hasNext()){
			K k = (K)iterator.next();
			if (k.toString().contains(name+GROUP_SEP)) {
				keys.add(k);
			}
		}
		return keys;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	private String getUniqueKey(Object key) {
		return new StringBuilder(this.name).append(GROUP_SEP).append(ByteObjectConvertUtil.getBytesFromObject(key)).toString();
	}
	
	private boolean keyValidations(K key, KeyValidation<K>... validations) {
		if (validations != null && validations.length != 0) {
			for (KeyValidation<K> keyValidation : validations) {
				if (!keyValidation.validate(key)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/** 本地缓存实体 */
	private static class CacheEntry {
		private Object value;
		private long expire = 0L;
		
		public CacheEntry(Object value, long expire) {
			this.value = value;
			this.expire = expire;
		}

		/**
		 * 是否已经过期
		 * @return
		 */
		public boolean isExpired() {
			if (expire <= 0) {
				return false;
			}
			return System.currentTimeMillis() > expire;
		}

		public Object getValue() {
			return value;
		}

		@Override
		public String toString() {
			return value == null ? "" : value.toString();
		}
	}
}
