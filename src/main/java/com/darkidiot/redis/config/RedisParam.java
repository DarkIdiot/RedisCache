package com.darkidiot.redis.config;

import lombok.Data;

/**
 * Redis初始化参数列表
 * @author darkidiot
 */
@Data
public class RedisParam {
	
	/** External State */
	
	//redis实例IP
	private String ip;
	//redis端口
	private int port;
	//redis密码
	private String password;
	
	/** Internal State */
	// JedisPool链接超时时间
	private int timeoutInMillis;
	// 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的
	private boolean testOnBorrow;
	// 在return一个jedis实例时，是否提前进行validate操作；
	private boolean testOnReturn;
	// 表示当borrow一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException
	private long maxWaitMillis;
	// 控制一个pool最多有多少个状态为idle的jedis实例
	private int maxIdle;
	// 服务的标识
	private String serverName;

	public RedisParam(String ip, int port, String password) {
		super();
		this.ip = ip;
		this.port = port;
		this.password = password;
	}

	public RedisParam config(int timeoutInMillis, long maxWaitMillis, int maxIdle,  boolean testOnBorrow, boolean testOnReturn, String serverName) {
		this.timeoutInMillis = timeoutInMillis;
		this.maxWaitMillis = maxWaitMillis;
		this.maxIdle = maxIdle;
		this.testOnBorrow = testOnBorrow;
		this.testOnReturn = testOnReturn;
		this.serverName = serverName;
		return this;
	}
}
