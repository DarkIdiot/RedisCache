package com.darkidiot.redis.config;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.darkidiot.redis.config.RedisPropertyConstants.*;

@Slf4j
public class JedisPoolFactory {

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
        initParam();
	}

    /**
     * 初始化配置文件
     */
    private static void initParam() {



    }

    public static JedisPool getReadPool(){
		JedisPool readPool = jedisPoolMap.get(READ);
		if (readPool == null) {
			readPool = getPool(initParam.getWrite(), READ);
			jedisPoolMap.put(READ, readPool);
		}
		return readPool;
	}

	public static JedisPool getWritePool(){
		JedisPool writePool = jedisPoolMap.get(WRITE);
        if (writePool == null) {
			writePool = getPool(initParam.getWrite(), WRITE);
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
		boolean testOnBorrow = getBooleanWithDefault(initParam.getTestOnBorrow(), DEFAULT_TEST_ON_BORROW);
		boolean testOnReturn = getBooleanWithDefault(initParam.getTestOnReturn(), DEFAULT_TEST_ON_RETURN);
		long maxWaitMillis = getLongWithDefault(initParam.getMaxWaitMillis(), DEFAULT_MAX_WAIT);
		int maxIdle = getIntWithDefault(initParam.getMaxIdle(), DEFAULT_MAX_IDLE);
		int timeout = getIntWithDefault(initParam.getTimeout(), DEFAULT_TIMEOUT);
		String serverId = initParam.getServerName();
		param.config(timeout,maxWaitMillis, maxIdle, testOnBorrow, testOnReturn, serverId);
		JedisPoolConfig config = new JedisPoolConfig();
		config.setTestOnBorrow(testOnBorrow);
		config.setTestOnReturn(testOnReturn);
		config.setMaxWaitMillis(maxWaitMillis);
		config.setMaxIdle(maxIdle);
        return new JedisPool(config,ip,port,timeout,password);
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
