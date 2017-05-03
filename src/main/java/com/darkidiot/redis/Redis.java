package com.darkidiot.redis;


import com.darkidiot.redis.config.JedisPoolFactory;
import com.darkidiot.redis.config.RedisPropertyConstants;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.jedis.imp.Jedis;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Redis工厂类
 *
 * @author darkidiot
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Redis {

    /**
     * 获取缓存接口.
     *
     * @param service(必传,服务的唯一标志-对应配置文件的服务配置)
     * @param groupName(必传,每一块缓存的唯一标志)
     * @return
     */
    public static <K extends Serializable, V extends Serializable> IRedisMap<K, V> use(String service, String groupName) {
        IJedis jedis = new Jedis(JedisPoolFactory.getWritePool(service), JedisPoolFactory.getReadPool(service), JedisPoolFactory.getInitParam(service), JedisPoolFactory.getReadSemaphore(service), JedisPoolFactory.getWriteSemaphore(service));
        return new RedisMapProxy<>(groupName, jedis);
    }

    /**
     * 获取缓存接口.Note:需默认redis配置。
     *
     * @param groupName(必传,每一块缓存的唯一标志)
     * @return
     */
    public static <K extends Serializable, V extends Serializable> IRedisMap<K, V> use(String groupName) {
        String service = RedisPropertyConstants.DEFAULT_SERVICE_KEY;
        return use(service, groupName);
    }

    /**
     * 获取缓存接口.Note:需默认redis配置。
     *
     * @return
     */
    public static <K extends Serializable, V extends Serializable> IRedisMap<K, V> use() {
        String groupName = RedisPropertyConstants.DEFAULT_GROUP_KEY;
        return use(groupName);
    }

}
