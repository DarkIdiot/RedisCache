package com.gome.supermain.redis.config;

/**
 * Redis-properties配置文件相关常量
 * @author darkidiot
 */
interface RedisPropertyConstants {
	String DEFAULT_CONFIG_KEY = "default";

	//properties文件中，与redis配置匹配的代码
	String PKEY_IP = "%s.ip";
	String PKEY_PORT = "%s.port";
	String PKEY_TIMEOUTINMILLIS = "%s.timeoutInMillis";
	String PKEY_PASSWORD = "%s.password";
	String PKEY_MAXACTIVE = "%s.maxActive";
	String PKEY_MAXIDLE = "%s.maxIdle";
	String PKEY_MAXWAIT = "%s.maxWait";
	String PKEY_TESTONBORROW = "%s.testOnBorrow";
	String PKEY_TESTONRETURN = "%s.testOnReturn";
	
	/** 读写分离配置标识 */
	String WRITE = "%s.write";
	String READ = "%s.read";
	/** .yml文件中ip:port?password正则表达式 */
	String IP_PORT_PASSWORD = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\:(\\d+)\\?([a-zA-Z0-9]+)";

	//默认属性配置
	int DEFAULT_TIMEOUT = 10;
	int DEFAULT_MAXIDLE = 100;
	int DEFAULT_MAXACTIVE = 200;
	int DEFAULT_MAXWAIT = 10000;
	/** testOnReturn:在进行returnObject对返回的connection进行validateObject校验. */ 
	boolean DEFAULT_TESTONBORROW = true;
	/** testOnBorrow:在进行borrowObject进行处理时，对拿到的connection进行validateObject校验. */
	boolean DEFAULT_TESTONRETURN = true;
}
