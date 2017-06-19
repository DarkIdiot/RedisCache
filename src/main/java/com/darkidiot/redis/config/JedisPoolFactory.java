package com.darkidiot.redis.config;

import com.darkidiot.redis.common.JedisType;
import com.darkidiot.redis.exception.RedisException;
import com.darkidiot.redis.util.StringUtil;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.darkidiot.redis.config.RedisPropertyConstants.*;

@Slf4j
public class JedisPoolFactory {

    private static Map<String, RedisInitParam> redisParamMap = Maps.newHashMap();

    private static HashMap<String, Pool> poolMap = Maps.newHashMap();

    private static Splitter commaSplitter = Splitter.on(",").omitEmptyStrings().trimResults();

    private JedisPoolFactory() {
    }

    static {
        initParam();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("The JVM Hook is execute.");
                for (Entry<String, Pool> entry : poolMap.entrySet()) {
                    Pool pool = entry.getValue();
                    log.info("The JedisPool: {} will be destroyed.", pool);
                    pool.destroy();
                }
            }
        });
    }

    /**
     * 初始化配置文件
     */
    private static void initParam() {
        Properties conf = new Properties();
        try (InputStream input = Resources.asByteSource(Resources.getResource(CONFIG_PROPERTY_NAME)).openStream()) {
            conf.load(input);
        } catch (Exception e) {
            log.error("failed to load {}, cause by:{}", CONFIG_PROPERTY_NAME, Throwables.getStackTraceAsString(e));
        }

        Set<String> serviceNames = Sets.newHashSet();
        String services = conf.getProperty(PKEY_SERVICES);
        if (!StringUtil.isNotEmpty(services)) {
            log.info("RedisCache get configuration[service.names] -> {}", services);
            serviceNames.addAll(commaSplitter.splitToList(services));
        }
        serviceNames.add(DEFAULT_SERVICE_KEY);
        log.info("RedisCache load default configuration[serviceName] -> {}", DEFAULT_SERVICE_KEY);

        final String read_prefix = ".read";
        final String write_prefix = ".write";
        for (String serviceName : serviceNames) {
            RedisInitParam redisInitParam = new RedisInitParam();

            redisInitParam.setServerName(serviceName);

            String format = String.format(PKEY_IP, serviceName);
            String ip = conf.getProperty(format);
            if (!StringUtil.isEmpty(ip)) {
                log.info("RedisCache set configuration[{}] -> {}", format, ip);
                redisInitParam.setIp(ip);
            }

            format = String.format(PKEY_PORT, serviceName);
            String port = conf.getProperty(format);
            if (!StringUtil.isEmpty(port)) {
                log.info("RedisCache set configuration[{}] -> {}", format, port);
                redisInitParam.setPort(Integer.valueOf(port));
            }

            format = String.format(PKEY_PASSWORD, serviceName);
            String password = conf.getProperty(format);
            if (!StringUtil.isEmpty(password)) {
                log.info("RedisCache set configuration[{}] -> {}", format, password);
                redisInitParam.setPassword(password);
            }

            format = String.format(PKEY_IPPORTPWD, serviceName);
            String ipPortPwd = conf.getProperty(format);
            if (!StringUtil.isEmpty(ipPortPwd)) {
                log.info("RedisCache set configuration[{}] -> {}", format, ipPortPwd);
                redisInitParam.setIpPortPwd(ipPortPwd);
            }

            format = String.format(PKEY_SENTINEL_MASTER_NAME, serviceName);
            String sentinelMasterName = conf.getProperty(format);
            if (!StringUtil.isEmpty(sentinelMasterName)) {
                log.info("RedisCache set configuration[{}] -> {}", format, sentinelMasterName);
                redisInitParam.setSentinelMasterName(sentinelMasterName);
            }

            format = String.format(PKEY_SENTINEL_HOSTS, serviceName);
            String sentinelHosts = conf.getProperty(format);
            if (!StringUtil.isEmpty(sentinelHosts)) {
                log.info("RedisCache set configuration[{}] -> {}", format, sentinelHosts);
                redisInitParam.setSentinelHosts(sentinelHosts);
            }

            format = String.format(PKEY_ISCLUSTER, serviceName);
            String isCluster = conf.getProperty(format);
            if (!StringUtil.isEmpty(isCluster)) {
                log.info("RedisCache set configuration[{}] -> {}", format, isCluster);
                redisInitParam.setIsCluster(Boolean.valueOf(isCluster));
            }

            format = String.format(PKEY_R$WSEPARATED, serviceName);
            String read$write = conf.getProperty(format);
            if (!StringUtil.isEmpty(read$write)) {
                log.info("RedisCache set configuration[{}] -> {}", format, read$write);
                redisInitParam.setR$WSeparated(Boolean.valueOf(read$write));
            }

            format = String.format(PKEY_OPEN_LOCAL_CACHE, serviceName);
            String openLocalCache = conf.getProperty(format);
            if (!StringUtil.isEmpty(openLocalCache)) {
                log.info("RedisCache set configuration[{}] -> {}", format, openLocalCache);
                redisInitParam.setOpenLocalCache(Boolean.valueOf(openLocalCache));
            }

            format = String.format(PKEY_DB_INDEX, serviceName);
            String dbIndex = conf.getProperty(format);
            if (!StringUtil.isEmpty(dbIndex)) {
                log.info("RedisCache set configuration[{}] -> {}", format, dbIndex);
                redisInitParam.setDbIndex(Integer.valueOf(dbIndex));
            }

            format = String.format(PKEY_TIMEOUT_IN_MILLIS, serviceName + read_prefix);
            String timeoutR = conf.getProperty(format);
            if (!StringUtil.isEmpty(timeoutR)) {
                Integer timeout = Integer.valueOf(timeoutR);
                timeout = timeout < DEFAULT_MAX_WAIT ? DEFAULT_MAX_WAIT : timeout;
                log.info("RedisCache set configuration[{}] -> {}", format, timeout);
                redisInitParam.setTimeoutR(timeout);
            } else {
                format = String.format(PKEY_TIMEOUT_IN_MILLIS, serviceName);
                timeoutR = conf.getProperty(format);
                if (!StringUtil.isEmpty(timeoutR)) {
                    Integer timeout = Integer.valueOf(timeoutR);
                    timeout = timeout < DEFAULT_MAX_WAIT ? DEFAULT_MAX_WAIT : timeout;
                    log.info("RedisCache set configuration[{}] -> {}", format, timeout);
                    redisInitParam.setTimeoutR(timeout);
                }
            }

            format = String.format(PKEY_TEST_ON_BORROW, serviceName + read_prefix);
            String testOnBorrowR = conf.getProperty(format);
            if (!StringUtil.isEmpty(testOnBorrowR)) {
                log.info("RedisCache set configuration[read.{}] -> {}", format, testOnBorrowR);
                redisInitParam.setTestOnBorrowR(Boolean.valueOf(testOnBorrowR));
            } else {
                format = String.format(PKEY_TEST_ON_BORROW, serviceName);
                testOnBorrowR = conf.getProperty(format);
                if (!StringUtil.isEmpty(testOnBorrowR)) {
                    log.info("RedisCache set configuration[read.{}] -> {}", format, testOnBorrowR);
                    redisInitParam.setTestOnBorrowR(Boolean.valueOf(testOnBorrowR));
                }
            }

            format = String.format(PKEY_TEST_ON_RETURN, serviceName + read_prefix);
            String testOnReturnR = conf.getProperty(format);
            if (!StringUtil.isEmpty(testOnReturnR)) {
                log.info("RedisCache set configuration[read.{}] -> {}", format, testOnReturnR);
                redisInitParam.setTestOnReturnR(Boolean.valueOf(testOnReturnR));
            } else {
                format = String.format(PKEY_TEST_ON_RETURN, serviceName);
                testOnReturnR = conf.getProperty(format);
                if (!StringUtil.isEmpty(testOnReturnR)) {
                    log.info("RedisCache set configuration[read.{}] -> {}", format, testOnReturnR);
                    redisInitParam.setTestOnReturnR(Boolean.valueOf(testOnReturnR));
                }
            }

            format = String.format(PKEY_MAX_WAIT, serviceName + read_prefix);
            String maxWaitMillisR = conf.getProperty(format);
            if (!StringUtil.isEmpty(maxWaitMillisR)) {
                log.info("RedisCache set configuration[read.{}] -> {}ms", format, maxWaitMillisR);
                redisInitParam.setMaxWaitMillisR(Long.valueOf(maxWaitMillisR));
            } else {
                format = String.format(PKEY_MAX_WAIT, serviceName);
                maxWaitMillisR = conf.getProperty(format);
                if (!StringUtil.isEmpty(maxWaitMillisR)) {
                    log.info("RedisCache set configuration[read.{}] -> {}ms", format, maxWaitMillisR);
                    redisInitParam.setMaxWaitMillisR(Long.valueOf(maxWaitMillisR));
                }
            }

            format = String.format(PKEY_MAX_IDLE, serviceName + read_prefix);
            String maxIdleR = conf.getProperty(format);
            if (!StringUtil.isEmpty(maxIdleR)) {
                log.info("RedisCache set configuration[read.{}] -> {}", format, maxIdleR);
                redisInitParam.setMaxIdleR(Integer.valueOf(maxIdleR));
            } else {
                format = String.format(PKEY_MAX_IDLE, serviceName);
                maxIdleR = conf.getProperty(format);
                if (!StringUtil.isEmpty(maxIdleR)) {
                    log.info("RedisCache set configuration[read.{}] -> {}", format, maxIdleR);
                    redisInitParam.setMaxIdleR(Integer.valueOf(maxIdleR));
                }
            }

            format = String.format(PKEY_MAX_TOTAL, serviceName + read_prefix);
            String maxTotalR = conf.getProperty(format);
            if (!StringUtil.isEmpty(maxTotalR)) {
                log.info("RedisCache set configuration[read.{}] -> {}", format, maxTotalR);
                redisInitParam.setMaxTotalR(Integer.valueOf(maxTotalR));
            } else {
                format = String.format(PKEY_MAX_TOTAL, serviceName);
                maxTotalR = conf.getProperty(format);
                if (!StringUtil.isEmpty(maxTotalR)) {
                    log.info("RedisCache set configuration[read.{}] -> {}", format, maxTotalR);
                    redisInitParam.setMaxTotalR(Integer.valueOf(maxTotalR));
                }
            }

            format = String.format(PKEY_TIMEOUT_IN_MILLIS, serviceName + write_prefix);
            String timeoutW = conf.getProperty(format);
            if (!StringUtil.isEmpty(timeoutW)) {
                Integer timeout = Integer.valueOf(timeoutW);
                timeout = timeout < DEFAULT_MAX_WAIT ? DEFAULT_MAX_WAIT : timeout;
                log.info("RedisCache set configuration[read.{}] -> {}", format, timeout);
                redisInitParam.setTimeoutW(timeout);
            } else {
                format = String.format(PKEY_TIMEOUT_IN_MILLIS, serviceName);
                timeoutW = conf.getProperty(format);
                if (!StringUtil.isEmpty(timeoutW)) {
                    Integer timeout = Integer.valueOf(timeoutW);
                    timeout = timeout < DEFAULT_MAX_WAIT ? DEFAULT_MAX_WAIT : timeout;
                    log.info("RedisCache set configuration[read.{}] -> {}", format, timeout);
                    redisInitParam.setTimeoutW(timeout);
                }
            }

            format = String.format(PKEY_TEST_ON_BORROW, serviceName + write_prefix);
            String testOnBorrowW = conf.getProperty(format);
            if (!StringUtil.isEmpty(testOnBorrowW)) {
                log.info("RedisCache set configuration[write.{}] -> {}", format, testOnBorrowW);
                redisInitParam.setTestOnBorrowW(Boolean.valueOf(testOnBorrowW));
            } else {
                format = String.format(PKEY_TEST_ON_BORROW, serviceName);
                testOnBorrowW = conf.getProperty(format);
                if (!StringUtil.isEmpty(testOnBorrowW)) {
                    log.info("RedisCache set configuration[write.{}] -> {}", format, testOnBorrowW);
                    redisInitParam.setTestOnBorrowW(Boolean.valueOf(testOnBorrowW));
                }
            }

            format = String.format(PKEY_TEST_ON_RETURN, serviceName + write_prefix);
            String testOnReturnW = conf.getProperty(format);
            if (!StringUtil.isEmpty(testOnReturnW)) {
                log.info("RedisCache set configuration[write.{}] -> {}", format, testOnReturnW);
                redisInitParam.setTestOnReturnW(Boolean.valueOf(testOnReturnW));
            } else {
                format = String.format(PKEY_TEST_ON_RETURN, serviceName);
                testOnReturnW = conf.getProperty(format);
                if (!StringUtil.isEmpty(testOnReturnW)) {
                    log.info("RedisCache set configuration[write.{}] -> {}", format, testOnReturnW);
                    redisInitParam.setTestOnReturnW(Boolean.valueOf(testOnReturnW));
                }
            }

            format = String.format(PKEY_MAX_WAIT, serviceName + write_prefix);
            String maxWaitMillisW = conf.getProperty(format);
            if (!StringUtil.isEmpty(maxWaitMillisW)) {
                log.info("RedisCache set configuration[write.{}] -> {}ms", format, maxWaitMillisW);
                redisInitParam.setMaxWaitMillisW(Long.valueOf(maxWaitMillisW));
            } else {
                format = String.format(PKEY_MAX_WAIT, serviceName);
                maxWaitMillisW = conf.getProperty(format);
                if (!StringUtil.isEmpty(maxWaitMillisW)) {
                    log.info("RedisCache set configuration[write.{}] -> {}ms", format, maxWaitMillisW);
                    redisInitParam.setMaxWaitMillisW(Long.valueOf(maxWaitMillisW));
                }
            }

            format = String.format(PKEY_MAX_IDLE, serviceName + write_prefix);
            String maxIdleW = conf.getProperty(format);
            if (!StringUtil.isEmpty(maxIdleW)) {
                log.info("RedisCache set configuration[write.{}] -> {}", format, maxIdleW);
                redisInitParam.setMaxIdleW(Integer.valueOf(maxIdleW));
            } else {
                format = String.format(PKEY_MAX_IDLE, serviceName);
                maxIdleW = conf.getProperty(format);
                if (!StringUtil.isEmpty(maxIdleW)) {
                    log.info("RedisCache set configuration[write.{}] -> {}", format, maxIdleW);
                    redisInitParam.setMaxIdleW(Integer.valueOf(maxIdleW));
                }
            }

            format = String.format(PKEY_MAX_TOTAL, serviceName + write_prefix);
            String maxTotalW = conf.getProperty(format);
            if (!StringUtil.isEmpty(maxTotalW)) {
                log.info("RedisCache set configuration[write.{}] -> {}", format, maxTotalW);
                redisInitParam.setMaxTotalW(Integer.valueOf(maxTotalW));
            } else {
                format = String.format(PKEY_MAX_TOTAL, serviceName);
                maxTotalW = conf.getProperty(format);
                if (!StringUtil.isEmpty(maxTotalW)) {
                    log.info("RedisCache set configuration[write.{}] -> {}", format, maxTotalW);
                    redisInitParam.setMaxTotalW(Integer.valueOf(maxTotalW));
                }
            }
            redisParamMap.put(serviceName, redisInitParam);
        }
    }

    private final static String READ_SUFFIX = "-read";
    private final static String WRITE_SUFFIX = "-write";

    @SuppressWarnings("unchecked")
    public static Pool getReadPool(String service) {
        RedisInitParam config = redisParamMap.get(service);
        Boolean R$W = getBooleanWithDefault(config.getR$WSeparated(), DEFAULT_R$W_SEPARATED);
        String serviceName = R$W ? service + READ_SUFFIX : service;
        Pool readPool = poolMap.get(serviceName);
        if (readPool == null) {
            readPool = getPool(config, JedisType.READ);
            poolMap.put(serviceName, readPool);
            log.debug("store the Jedis Pool Instance [name={}] to HashMap.", serviceName);
        }
        return readPool;
    }

    @SuppressWarnings("unchecked")
    public static Pool getWritePool(String service) {
        RedisInitParam config = redisParamMap.get(service);
        Boolean R$W = getBooleanWithDefault(config.getR$WSeparated(), DEFAULT_R$W_SEPARATED);
        String serviceName = R$W ? service + WRITE_SUFFIX : service;
        Pool writePool = poolMap.get(serviceName);
        if (writePool == null) {
            writePool = getPool(config, JedisType.WRITE);
            poolMap.put(serviceName, writePool);
            log.debug("store the Jedis Pool Instance [name={}] to HashMap.", serviceName);
        }
        return writePool;
    }

    private static Pool getPool(RedisInitParam initParam, JedisType mode) {
        if (getBooleanWithDefault(initParam.getIsCluster(), DEFAULT_IS_CLUSTER)) {
            String sentinelProps = initParam.getSentinelHosts();
            Iterable<String> parts = commaSplitter.split(sentinelProps);
            Set<String> sentinelHosts = Sets.newHashSet(parts);
            if (sentinelHosts.size() == 0) {
                throw new RedisException("Redis Cluster configure failure. Cause by not find SentinelHosts.");
            }
            String masterName = initParam.getSentinelMasterName();
            if (StringUtil.isEmpty(masterName)) {
                throw new RedisException("Redis Cluster configure failure. Cause by not find SentinelMasterName.");
            }
            String password = initParam.getPassword();
            int dbIndex = getIntWithDefault(initParam.getDbIndex(), DEFAULT_DB_INDEX);
            boolean testOnBorrow = getBooleanWithDefault(isRead(mode) ? initParam.getTestOnBorrowR() : initParam.getTestOnBorrowW(), DEFAULT_TEST_ON_BORROW);
            boolean testOnReturn = getBooleanWithDefault(isRead(mode) ? initParam.getTestOnReturnR() : initParam.getTestOnReturnW(), DEFAULT_TEST_ON_RETURN);
            long maxWaitMillis = getLongWithDefault(isRead(mode) ? initParam.getMaxWaitMillisR() : initParam.getMaxWaitMillisW(), DEFAULT_MAX_WAIT);
            int maxIdle = getIntWithDefault(isRead(mode) ? initParam.getMaxIdleR() : initParam.getMaxIdleW(), DEFAULT_MAX_IDLE);
            int maxTotal = getIntWithDefault(isRead(mode) ? initParam.getMaxTotalR() : initParam.getMaxTotalW(), DEFAULT_MAX_TOTAL);
            int timeout = getIntWithDefault(isRead(mode) ? initParam.getTimeoutR() : initParam.getTimeoutW(), DEFAULT_TIMEOUT);
            JedisPoolConfig config = new JedisPoolConfig();
            config.setTestOnBorrow(testOnBorrow);
            config.setTestOnReturn(testOnReturn);
            config.setMaxWaitMillis(maxWaitMillis);
            config.setMaxIdle(maxIdle);
            config.setMaxTotal(maxTotal);
            return new JedisSentinelPool(masterName, sentinelHosts, config, timeout, password, dbIndex);
        } else {
            String ipPortPwd = initParam.getIpPortPwd();
            String redisHost = initParam.getIp();
            Integer redisPort = initParam.getPort();
            String password = initParam.getPassword();

            if (StringUtil.isNotEmpty(ipPortPwd)) {
                Pattern pattern = Pattern.compile(IP_PORT_PASSWORD);
                Matcher matcher = pattern.matcher(ipPortPwd);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Your redis configuration of " + mode + " is not format as: ip:port?password.");
                }
                redisHost = matcher.group(1);
                redisPort = Integer.parseInt(matcher.group(2));
                password = matcher.group(3);
            }
            if (StringUtil.isEmpty(redisHost)) {
                log.warn("Redis standalone configure failure. Cause by not find ip, and use the default value:[{}].", DEFAULT_IP);
                redisHost = DEFAULT_IP;
            }
            if (redisPort == null) {
                log.warn("Redis standalone configure failure. Cause by not find port, and use the default value:[{}]", DEFAULT_PORT);
                redisPort = DEFAULT_PORT;
            }

            int dbIndex = getIntWithDefault(initParam.getDbIndex(), DEFAULT_DB_INDEX);
            boolean testOnBorrow = getBooleanWithDefault(isRead(mode) ? initParam.getTestOnBorrowR() : initParam.getTestOnBorrowW(), DEFAULT_TEST_ON_BORROW);
            boolean testOnReturn = getBooleanWithDefault(isRead(mode) ? initParam.getTestOnReturnR() : initParam.getTestOnReturnW(), DEFAULT_TEST_ON_RETURN);
            long maxWaitMillis = getLongWithDefault(isRead(mode) ? initParam.getMaxWaitMillisR() : initParam.getMaxWaitMillisW(), DEFAULT_MAX_WAIT);
            int maxIdle = getIntWithDefault(isRead(mode) ? initParam.getMaxIdleR() : initParam.getMaxIdleW(), DEFAULT_MAX_IDLE);
            int maxTotal = getIntWithDefault(isRead(mode) ? initParam.getMaxTotalR() : initParam.getMaxTotalW(), DEFAULT_MAX_TOTAL);
            int timeout = getIntWithDefault(isRead(mode) ? initParam.getTimeoutR() : initParam.getTimeoutW(), DEFAULT_MAX_IDLE);
            JedisPoolConfig config = new JedisPoolConfig();
            config.setTestOnBorrow(testOnBorrow);
            config.setTestOnReturn(testOnReturn);
            config.setMaxWaitMillis(maxWaitMillis);
            config.setMaxIdle(maxIdle);
            config.setMaxTotal(maxTotal);
            return new JedisPool(config, redisHost, redisPort, timeout, password, dbIndex);
        }
    }

    private static Boolean isRead(JedisType type) {
        return JedisType.READ.equals(type);
    }

    public static RedisInitParam getInitParam(String service) {
        return redisParamMap.get(service);
    }

    private static long getLongWithDefault(Long value, long defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static int getIntWithDefault(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static boolean getBooleanWithDefault(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }
}
