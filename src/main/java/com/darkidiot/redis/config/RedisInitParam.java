package com.darkidiot.redis.config;

import lombok.Data;

/**
 * Redis初始化参数列表
 *
 * @author darkidiot
 */
@Data
public class RedisInitParam {
    /**
     * redis服务器信息配置
     */
    private String ip;
    private Integer port;
    private String password;

    /**
     * ip:port?password  redis服务器信息配置
     */
    private String ipPortPwd;

    /**
     * 集群master的名字
     */
    private String sentinelMasterName;
    /**
     * 集群监控服务器地址(多个以逗号分隔)如 127.0.0.1:6379,192.1.1.1:6379
     */
    private String sentinelHosts;

    /**
     * 是否配置集群
     */
    private Boolean isCluster;
    /**
     * 是否读写分离
     */
    private Boolean R$WSeparated;
    /**
     * 服务的标识
     */
    private String serverName;
    /**
     * 是否开启本地缓存
     */
    private Boolean openLocalCache;
    /**
     * 本地缓存更新订阅线程数，开启本地缓存时候有效
     */
    private Integer subscribeThreadNum;

    /**
     * redis数据库序号
     */
    private Integer dbIndex;

    //读基础配置

    /**
     * JedisPool链接超时时间
     */
    private Integer timeoutR;
    /**
     * 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的
     */
    private Boolean testOnBorrowR;
    /**
     * 在return一个jedis实例时，是否提前进行validate操作；
     */
    private Boolean testOnReturnR;
    /**
     * 表示当borrow一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException
     */
    private Integer maxWaitMillisR;
    /**
     * 控制一个pool最多有多少个状态为idle的jedis实例
     */
    private Integer maxIdleR;

    //写基础配置

    /**
     * JedisPool链接超时时间
     */
    private Integer timeoutW;
    /**
     * 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的
     */
    private Boolean testOnBorrowW;
    /**
     * 在return一个jedis实例时，是否提前进行validate操作；
     */
    private Boolean testOnReturnW;
    /**
     * 表示当borrow一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException
     */
    private Integer maxWaitMillisW;
    /**
     * 控制一个pool最多有多少个状态为idle的jedis实例
     */
    private Integer maxIdleW;
}
