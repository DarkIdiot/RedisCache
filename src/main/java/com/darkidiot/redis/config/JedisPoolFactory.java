package com.darkidiot.redis.config;

import com.darkidiot.redis.util.StringUtil;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.darkidiot.redis.config.RedisPropertyConstants.*;

@Slf4j
public class JedisPoolFactory {

    private static Map<String, RedisInitParam> redisParamMap = Maps.newHashMap();

    private static final String READ = "READ";
    private static final String WRITE = "WRITE";
    private static HashMap<String, JedisPool> jedisPoolMap = Maps.newHashMap();

    private static Splitter commaSplitter = Splitter.on(",").omitEmptyStrings().trimResults();

    private JedisPoolFactory() {
    }

    static {
        initParam();
        Runtime.getRuntime().addShutdownHook(new Thread() {
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
        // TODO: 2017/4/22 配置
        String services = conf.getProperty(PKEY_SERVIES);
        if (!StringUtil.isNotEmpty(services)) {
            log.info("RedisCache get configuration[service.names] -> {}", services);
            List<String> serviceNames = commaSplitter.splitToList(services);
            for (String serviceName : serviceNames) {
                String.format(PKEY_IP, serviceName);
                String.format(PKEY_PORT, serviceName);
                String.format(PKEY_IPPORTPWD, serviceName);
                String.format(PKEY_R$WSEPARATED, serviceName);
                String.format(PKEY_SENTINEL_MASTER_NAME, serviceName);
                String.format(PKEY_SENTINEL_HOSTS, serviceName);
                String.format(PKEY_DB_INDEX, serviceName);
                String.format(PKEY_PASSWORD, serviceName);
                String.format(PKEY_TIMEOUT_IN_MILLIS, serviceName);
                String.format(PKEY_MAX_ACTIVE, serviceName);
            }

        }

    }

    public static JedisPool getReadPool(String service) {
        JedisPool readPool = jedisPoolMap.get(READ);
        if (readPool == null) {
            readPool = getPool(redisParamMap.get(service).getIpPortPwd(), READ);
            jedisPoolMap.put(READ, readPool);
        }
        return readPool;
    }

    public static JedisPool getWritePool(String service) {
        JedisPool writePool = jedisPoolMap.get(WRITE);
        if (writePool == null) {
            writePool = getPool(redisParamMap.get(service).getIpPortPwd(), WRITE);
            jedisPoolMap.put(WRITE, writePool);
        }
        return writePool;
    }

    private static JedisPool getPool(String ip_port_password, String mode) {
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
        param.config(timeout, maxWaitMillis, maxIdle, testOnBorrow, testOnReturn, serverId);
        JedisPoolConfig config = new JedisPoolConfig();
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        config.setMaxWaitMillis(maxWaitMillis);
        config.setMaxIdle(maxIdle);
        return new JedisPool(config, ip, port, timeout, password);
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
