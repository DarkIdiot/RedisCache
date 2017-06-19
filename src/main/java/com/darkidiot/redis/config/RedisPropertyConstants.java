package com.darkidiot.redis.config;

/**
 * Redis-properties配置文件相关常量
 *
 * @author darkidiot
 */
public interface RedisPropertyConstants {

    String CONFIG_PROPERTY_NAME = "redis.properties";

    String PKEY_SERVICES = "service.names";
    /** redis 默认服务 */
    String DEFAULT_SERVICE_KEY = "redis";
    String DEFAULT_GROUP_KEY = "common";

    /** properties文件中，与redis配置匹配的代码 */

    // 单机配置

    String PKEY_IP = "%s.ip";
    String PKEY_PORT = "%s.port";

    String PKEY_IPPORTPWD = "%s.ip-port-password";

    // 集群配置

    /** 集群master的名字 */
    String PKEY_SENTINEL_MASTER_NAME = "%s.sentinel.master.name";
    /** 集群监控服务器地址(多个以逗号分隔)如 127.0.0.1:6379,192.1.1.1:6379 */
    String PKEY_SENTINEL_HOSTS = "%s.sentinel.hosts";

    /** 读写分离配置标识 */
    String PKEY_R$WSEPARATED = "%s.read&write.separated";
    /** redis数据库序号 */
    String PKEY_DB_INDEX = "%s.db.index";

    /** 是否是集群配置 */
    String PKEY_ISCLUSTER = "%s.redis.cluster";

    /** 密码 */
    String PKEY_PASSWORD = "%s.password";
    String PKEY_OPEN_LOCAL_CACHE = "%s.open.local.cache";

    /** JedisPool链接超时时间 */
    String PKEY_TIMEOUT_IN_MILLIS = "%s.timeoutInMillis";
    /** 最大活跃jedis实例数量 */
    String PKEY_MAX_TOTAL = "%s.maxTotal";
    /** 控制一个pool最多有多少个状态为idle的jedis实例 */
    String PKEY_MAX_IDLE = "%s.maxIdle";
    /** 表示当borrow一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException */
    String PKEY_MAX_WAIT = "%s.maxWait";
    /** 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的 */
    String PKEY_TEST_ON_BORROW = "%s.testOnBorrow";
    /** 在return一个jedis实例时，是否提前进行validate操作； */
    String PKEY_TEST_ON_RETURN = "%s.testOnReturn";

    /** 文件中ip:port?password正则表达式 */
    String IP_PORT_PASSWORD = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\:(\\d+)\\?([a-zA-Z0-9]*)";



    //默认属性配置
    String DEFAULT_IP = "127.0.0.1";
    int DEFAULT_PORT = 6379;

    int DEFAULT_DB_INDEX = 0;

    boolean DEFAULT_IS_CLUSTER = false;
    boolean DEFAULT_R$W_SEPARATED =true;
    int DEFAULT_TIMEOUT = 10000;
    int DEFAULT_MAX_TOTAL = 8;
    int DEFAULT_MAX_IDLE = 8;
    int DEFAULT_MAX_WAIT = 10000;
    boolean DEFAULT_TEST_ON_BORROW = true;
    boolean DEFAULT_TEST_ON_RETURN = false;
}
