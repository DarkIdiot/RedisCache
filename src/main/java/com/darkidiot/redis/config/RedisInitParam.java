package com.darkidiot.redis.config;

import lombok.Data;

/**
 * Redis初始化参数列表
 * @author darkidiot
 */
@Data
public class RedisInitParam {
    /** redis服务器信息配置 */
    private String ip;
    private Integer port;
    private String password;

	/** ip:port?password  redis服务器信息配置 */
	private String ipPortPwd;



    private Boolean isCluster;
    /** 是否读写分离 */
    private Boolean R$WSeparated;
	// JedisPool链接超时时间
	private Integer timeout;
    // 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的
	private Boolean testOnBorrow;
	// 在return一个jedis实例时，是否提前进行validate操作；
	private Boolean testOnReturn;
	// 表示当borrow一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException
	private Long maxWaitMillis;
	// 控制一个pool最多有多少个状态为idle的jedis实例
	private Integer maxIdle;
	// 服务的标识
	private String serverName;
	// 是否开启本地缓存
	private Boolean openLocalCache;
}