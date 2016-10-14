package com.gome.supermain.redis.config;

import static com.gome.supermain.redis.config.RedisPropertyConstants.DEFAULT_MAXIDLE;
import static com.gome.supermain.redis.config.RedisPropertyConstants.DEFAULT_MAXWAIT;
import static com.gome.supermain.redis.config.RedisPropertyConstants.DEFAULT_TESTONBORROW;
import static com.gome.supermain.redis.config.RedisPropertyConstants.DEFAULT_TESTONRETURN;
import static com.gome.supermain.redis.config.RedisPropertyConstants.DEFAULT_TIMEOUT;
import static com.gome.supermain.redis.config.RedisPropertyConstants.IP_PORT_PASSWORD;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Component
@Slf4j
@EnableAutoConfiguration
public class JedisPoolFactory {
	@Autowired
	static RedisInitParam initParam;

	private static final String READ = "READ";
	private static final String WRITE = "WRITE";

	private static HashMap<String, JedisPool> jedisPoolMap = new HashMap<>();

	private JedisPoolFactory() {}

	static{
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run() {
				log.info("The JVM Hook is execute.");
				for (Entry<String, JedisPool> entry : jedisPoolMap.entrySet()) {
					JedisPool pool = entry.getValue();
					log.info("The JedisPool: {} will be destroyed.", pool);
					pool.destroy();
				}
			}
		});
	}
	
	public static JedisPool getReadPool(){
		JedisPool readPool = jedisPoolMap.get(READ);
		if (readPool == null) {
			readPool = getPool(initParam.getWirte(), READ);
			jedisPoolMap.put(READ, readPool);
		}
		return readPool;
	}

	public static JedisPool getWritePool(){
		JedisPool writePool = jedisPoolMap.get(WRITE);
		if (writePool == null) {
			writePool = getPool(initParam.getWirte(), WRITE);
			jedisPoolMap.put(WRITE, writePool);
		}
		return writePool;
	}

	private static JedisPool getPool(String ip_port_password, String mode){
		Pattern pattern = Pattern.compile(IP_PORT_PASSWORD);
		Matcher matcher = pattern.matcher(ip_port_password);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Your redis configuration of " + mode + " is not formated as: ip:port?password.");
		}
		String ip = matcher.group(1);
		int port = Integer.parseInt(matcher.group(2));
		String password = matcher.group(3);
		RedisParam param = new RedisParam(ip, port, password);
		boolean testOnBorrow = getBooleanWithDefault(initParam.getTestOnBorrow(), DEFAULT_TESTONBORROW);
		boolean testOnReturn = getBooleanWithDefault(initParam.getTestOnReturn(), DEFAULT_TESTONRETURN);
		long maxWaitMillis = getLongWithDefault(initParam.getMaxWaitMillis(), DEFAULT_MAXWAIT);
		int maxIdle = getIntWithDefault(initParam.getMaxIdle(), DEFAULT_MAXIDLE);
		int timeout = getIntWithDefault(initParam.getTimeout(), DEFAULT_TIMEOUT);
		String serverId = initParam.getServerName();
		param.config(timeout,maxWaitMillis, maxIdle, testOnBorrow, testOnReturn, serverId);
		JedisPoolConfig config = new JedisPoolConfig();
		config.setTestOnBorrow(testOnBorrow);
		config.setTestOnReturn(testOnReturn);
		config.setMaxWaitMillis(maxWaitMillis);
		config.setMaxIdle(maxIdle);
		JedisPool jedisPool = new JedisPool(config,ip,port,timeout,password);
		return jedisPool;
	}
	
	public static RedisInitParam getInitParam() {
		return initParam;
	}

	private static long getLongWithDefault(Long value, long defaultValue) {
		return value == null ?defaultValue:value;
	}

	private static int getIntWithDefault(Integer value, int defaultValue) {
		return value == null ?defaultValue:value;
	}

	private static boolean getBooleanWithDefault(Boolean value, boolean defaultValue) {
		return value == null ?defaultValue:value;
	}
}
