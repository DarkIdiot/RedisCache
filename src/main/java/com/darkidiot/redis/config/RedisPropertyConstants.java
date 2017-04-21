package com.darkidiot.redis.config;

/**
 * Redis-properties配置文件相关常量
 *
 * @author darkidiot
 */
interface RedisPropertyConstants {

    String CONFIG_PROPERTY_NAME = "redis.properties";

    String DEFAULT_CONFIG_KEY = "redis";

    //properties文件中，与redis配置匹配的代码
    String PKEY_IP = "%s.ip";
    String PKEY_PORT = "%s.port";
    /**
     * 读写分离配置标识
     */
    String PKEY_WRITE = "%s.write";
    String PKEY_READ = "%s.read";

    /**
     * 集群配置
     */
    //集群master的名字
    String PKEY_SENTINEL_MASTER_NAME = "%s.sentinel.master.name";
    //集群监控服务器地址(多个以逗号分隔)如 127.0.0.1:26379,192.1.1.1:26379
    String PKEY_SENTINEL_HOSTS = "%s.sentinel.hosts";

    String PKEY_PASSWORD = "%s.password";
    String PKEY_TIMEOUT_IN_MILLIS = "%s.timeoutInMillis";
    String PKEY_MAX_ACTIVE = "%s.maxActive";
    String PKEY_MAX_IDLE = "%s.maxIdle";
    String PKEY_MAX_WAIT = "%s.maxWait";
    String PKEY_TEST_ON_BORROW = "%s.testOnBorrow";
    String PKEY_TEST_ON_RETURN = "%s.testOnReturn";
    String PKEY_DB_INDEX = "%s.db.index";

    String PKEY_ISCLUSTER = "%s.redis.cluster";
    /**
     * 文件中ip:port?password正则表达式
     */
    String IP_PORT_PASSWORD = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\:(\\d+)\\?([a-zA-Z0-9]+)";

    //默认属性配置
    int DEFAULT_TIMEOUT = 10;
    int DEFAULT_MAX_IDLE = 100;
    int DEFAULT_MAX_ACTIVE = 200;
    int DEFAULT_MAX_WAIT = 10000;
    /**
     * testOnReturn:在进行returnObject对返回的connection进行validateObject校验.
     */
    boolean DEFAULT_TEST_ON_BORROW = false;
    /**
     * testOnBorrow:在进行borrowObject进行处理时，对拿到的connection进行validateObject校验.
     */
    boolean DEFAULT_TEST_ON_RETURN = false;
}
