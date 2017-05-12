package com.darkidiot.redis.jedis.imp;

import com.darkidiot.redis.common.JedisType;
import com.darkidiot.redis.config.RedisInitParam;
import com.darkidiot.redis.jedis.IJedis;
import com.darkidiot.redis.util.ByteObjectConvertUtil;
import com.google.common.collect.Lists;
import lombok.Data;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Tuple;
import redis.clients.util.Pool;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import static com.darkidiot.redis.common.JedisType.READ;
import static com.darkidiot.redis.common.JedisType.WRITE;
import static com.darkidiot.redis.util.CommonUtil.Callback;
import static com.darkidiot.redis.util.CommonUtil.invoke;

/**
 * Redis缓存实现类，支持读写分离(链接自动关闭)
 *
 * @author darkidiot
 */
@Data
public class Jedis implements IJedis {

    /**
     * 读jedis连接池
     */
    private Pool writeJedisPool;
    /**
     * 写jedis连接池
     */
    private Pool readJedisPool;

    /**
     * 读线程控制器
     */
    private Semaphore readSemaphore;
    /**
     * 写线程控制器
     */
    private Semaphore writeSemaphore;

    private RedisInitParam baseConfig;

    public Jedis(Pool writeJedisPool, Pool readJedisPool, RedisInitParam baseConfig, Semaphore readSemaphore, Semaphore writeSemaphore) {
        if (writeJedisPool == null && readJedisPool == null) {
            throw new IllegalArgumentException("writeRedisPool and readRedisPool can not both null.");
        }
        this.readJedisPool = readJedisPool;
        this.writeJedisPool = writeJedisPool;
        this.readSemaphore = readSemaphore;
        this.writeSemaphore = writeSemaphore;
        this.baseConfig = baseConfig;
    }

    /**
     * 根据读写类型获取不同的JedisPool
     *
     * @param type
     * @return
     */
    private Pool getPoolByType(JedisType type) {
        switch (type) {
            case READ:
                if (readJedisPool != null) {
                    return readJedisPool;
                }
                if (writeJedisPool != null) {
                    return writeJedisPool;
                }
                throw new IllegalStateException("please set read redis params before you use it!");
            case WRITE:
                if (writeJedisPool == null) {
                    throw new IllegalStateException("please set write redis params before you use it!");
                }
                return writeJedisPool;
            default:
                throw new IllegalArgumentException("JedisType parameter error, use as: READ|WRITE.");
        }
    }

    /**
     * 根据读写类型获取不同的Semaphore
     *
     * @param type
     * @return
     */
    private Semaphore getSemaphoreByType(JedisType type) {
        switch (type) {
            case READ:
                if (readSemaphore != null) {
                    return readSemaphore;
                }
                throw new IllegalStateException("please set read redis params [maxTotal] before you use it!");
            case WRITE:
                if (writeSemaphore != null) {
                    return writeSemaphore;
                }
                throw new IllegalStateException("please set write redis params [maxTotal] before you use it!");
            default:
                throw new IllegalArgumentException("JedisType parameter error, use as: READ|WRITE.");
        }
    }

    private <T> T handle(Callback<T> call, JedisType type) {
        return invoke(call, getPoolByType(type), getSemaphoreByType(type));
    }

    @Override
    public Set<Tuple> zrangeWithScores(final byte[] key, final int offset, final int len) {
        return handle(new Callback<Set<Tuple>>() {
            @Override
            public Set<Tuple> call(redis.clients.jedis.Jedis jedis) {
                return jedis.zrangeWithScores(key, offset, offset + len);
            }
        }, WRITE);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(final byte[] key, final int start, final int end) {
        return handle(new Callback<Set<Tuple>>() {
            @Override
            public Set<Tuple> call(redis.clients.jedis.Jedis jedis) {
                return jedis.zrevrangeWithScores(key, start, end);
            }
        }, WRITE);
    }

    /*
     * add one member in sorted set, user question totalsize and sourceid
     * question +1
     */
    @Override
    public boolean zadd(final byte[] key, final double score, final byte[] value) {
        return handle(new Callback<Boolean>() {
            @Override
            public Boolean call(redis.clients.jedis.Jedis jedis) {
                return jedis.zadd(key, score, value) == 1;
            }
        }, WRITE);
    }

    /*
     * add one member in sorted set, user question totalsize and sourceid
     * question +1
     */
    @Override
    public boolean zadd(final String key, final double score, final String value) {
        return handle(new Callback<Boolean>() {
            @Override
            public Boolean call(redis.clients.jedis.Jedis jedis) {
                return jedis.zadd(key, score, value) == 1;
            }
        }, WRITE);
    }

    /*
     * add list member in sorted set question +1
     */
    @Override
    public boolean zadd(final byte[] key, final Map<byte[], Double> scoreMembers) {
        return handle(new Callback<Boolean>() {
            @Override
            public Boolean call(redis.clients.jedis.Jedis jedis) {
                return jedis.zadd(key, scoreMembers) == 1;
            }
        }, WRITE);
    }

    @Override
    public boolean zadd(final String key, final Map<String, Double> scoreMembers) {
        return handle(new Callback<Boolean>() {
            @Override
            public Boolean call(redis.clients.jedis.Jedis jedis) {
                return jedis.zadd(key, scoreMembers) == 1;
            }
        }, WRITE);
    }

    @Override
    public double zscore(final byte[] key, final byte[] value) {
        return handle(new Callback<Double>() {
            @Override
            public Double call(redis.clients.jedis.Jedis jedis) {
                return jedis.zscore(key, value);
            }
        }, WRITE);
    }

    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final double min, final double max) {
        return handle(new Callback<Set<byte[]>>() {
            @Override
            public Set<byte[]> call(redis.clients.jedis.Jedis jedis) {
                return jedis.zrangeByScore(key, min, max);
            }
        }, WRITE);
    }

    /*
     * remove one member in sorted set, user question totalsize and sourceid
     * question -1
     */
    @Override
    public boolean zrem(final byte[] key, final byte[] value) {
        return handle(new Callback<Boolean>() {
            @Override
            public Boolean call(redis.clients.jedis.Jedis jedis) {
                return jedis.zrem(key, value) == 1;
            }
        }, WRITE);
    }

    @Override
    public boolean exists(final byte[] key) {
        return handle(new Callback<Boolean>() {
            @Override
            public Boolean call(redis.clients.jedis.Jedis jedis) {
                return jedis.exists(key);
            }
        }, READ);
    }

    @Override
    public String rename(final byte[] oldkey, final byte[] newkey) {
        return handle(new Callback<String>() {
            @Override
            public String call(redis.clients.jedis.Jedis jedis) {
                return jedis.rename(oldkey, newkey);
            }
        }, WRITE);
    }

    @Override
    public long renamenx(final byte[] oldkey, final byte[] newkey) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.renamenx(oldkey, newkey);
            }
        }, WRITE);
    }

    @Override
    public byte[] get(final byte[] key) {
        return handle(new Callback<byte[]>() {
            @Override
            public byte[] call(redis.clients.jedis.Jedis jedis) {
                return jedis.get(key);
            }
        }, READ);
    }

    /*
     * if sucess, return OK, if return!=OK, something is wrong
     */
    @Override
    public String set(final byte[] key, final byte[] value) {
        return handle(new Callback<String>() {
            @Override
            public String call(redis.clients.jedis.Jedis jedis) {
                return jedis.set(key, value);
            }
        }, WRITE);
    }

    @Override
    public String set(final String key, final long value) {
        return handle(new Callback<String>() {
            @Override
            public String call(redis.clients.jedis.Jedis jedis) {
                return jedis.set(key, value + "");
            }
        }, WRITE);
    }

    @Override
    public long incr(final byte[] key) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.incr(key);
            }
        }, WRITE);
    }

    @Override
    public long incrBy(final byte[] key, final long value) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.incrBy(key, value);
            }
        }, WRITE);
    }

    @Override
    public long decr(final byte[] key) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.decr(key);
            }
        }, WRITE);
    }

    @Override
    public long decrBy(final byte[] key, final long value) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.decrBy(key, value);
            }
        }, WRITE);
    }

    @Override
    public Set<byte[]> keys(final byte[] pattern) {
        return handle(new Callback<Set<byte[]>>() {
            @Override
            public Set<byte[]> call(redis.clients.jedis.Jedis jedis) {
                return jedis.keys(pattern);
            }
        }, READ);
    }

    @Override
    public long zcard(final byte[] key) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.zcard(key);
            }
        }, WRITE);
    }

    @Override
    public long zremrangeByRank(final byte[] key, final int offset, final int len) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.zremrangeByRank(key, offset, offset + len);
            }
        }, WRITE);
    }

    @Override
    public long zremrangeByRankV2(final byte[] key, final int start, final int end) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.zremrangeByRank(key, start, end);
            }
        }, WRITE);
    }

    @Override
    public long zremrangeByScore(final byte[] key, final int start, final int end) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.zremrangeByScore(key, start, end);
            }
        }, WRITE);
    }

    /*
     * All the fields are in the form field:value (bytes) edis_version:0.07
     * connected_clients:1 connected_slaves:0 used_memory:3187
     * changes_since_last_save:0 last_save_time:1237655729
     * total_connections_received:1 total_commands_processed:1
     * uptime_in_seconds:25 uptime_in_days:0
     */
    @Override
    public String info() {
        return handle(new Callback<String>() {
            @Override
            public String call(redis.clients.jedis.Jedis jedis) {
                return jedis.info();
            }
        }, READ);
    }

    @Override
    public byte[] hget(final byte[] key, final byte[] field) {
        return handle(new Callback<byte[]>() {
            @Override
            public byte[] call(redis.clients.jedis.Jedis jedis) {
                return jedis.hget(key, field);
            }
        }, READ);
    }

    @Override
    public String hget(final String key, final String field) {
        return handle(new Callback<String>() {
            @Override
            public String call(redis.clients.jedis.Jedis jedis) {
                return jedis.hget(key, field);
            }
        }, READ);
    }

    @Override
    public long hset(final String key, final String field, final String value) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.hset(key, field, value);
            }
        }, WRITE);
    }

    @Override
    public long hset(final byte[] key, final byte[] field, final byte[] value) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.hset(key, field, value);
            }
        }, WRITE);
    }

    @Override
    public long hdel(final String key, final String field) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.hdel(key, field);
            }
        }, WRITE);
    }

    @Override
    public long hdel(final byte[] key, final byte[] field) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.hdel(key, field);
            }
        }, WRITE);
    }

    @Override
    public Map<String, String> hgetAll(final String key) {
        return handle(new Callback<Map<String, String>>() {
            @Override
            public Map<String, String> call(redis.clients.jedis.Jedis jedis) {
                return jedis.hgetAll(key);
            }
        }, READ);
    }

    @Override
    public Map<byte[], byte[]> hgetAll(final byte[] key) {
        return handle(new Callback<Map<byte[], byte[]>>() {
            @Override
            public Map<byte[], byte[]> call(redis.clients.jedis.Jedis jedis) {
                return jedis.hgetAll(key);
            }
        }, READ);
    }

    @Override
    public long hincrBy(final String key, final String field, final long value) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.hincrBy(key, field, value);
            }
        }, WRITE);
    }

    @Override
    public long expire(final String key, final int seconds) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.expire(key, seconds);
            }
        }, WRITE);
    }

    @Override
    public long del(final String key) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.del(key);
            }
        }, WRITE);
    }

    @Override
    public long del(final byte[] key) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.del(key);
            }
        }, WRITE);
    }

    @Override
    public long expire(final byte[] key, final int seconds) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.expire(key, seconds);
            }
        }, WRITE);
    }

    @Override
    public long linsert(final String key, final boolean where, final String pivot, final String value) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                if (where)
                    return jedis.linsert(key, BinaryClient.LIST_POSITION.BEFORE, pivot, value);
                else
                    return jedis.linsert(key, BinaryClient.LIST_POSITION.AFTER, pivot, value);
            }
        }, WRITE);
    }

    @Override
    public long lpush(final String key, final String value) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.lpush(key, value);
            }
        }, WRITE);
    }

    @Override
    public long rpush(final String key, final String value) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.rpush(key, value);
            }
        }, WRITE);
    }

    @Override
    public String ltrim(final String key, final long start, final long end) {
        return handle(new Callback<String>() {
            @Override
            public String call(redis.clients.jedis.Jedis jedis) {
                return jedis.ltrim(key, start, end);
            }
        }, WRITE);
    }

    @Override
    public List<String> lrange(final String key, final long start, final long end) {
        return handle(new Callback<List<String>>() {
            @Override
            public List<String> call(redis.clients.jedis.Jedis jedis) {
                return jedis.lrange(key, start, end);
            }
        }, WRITE);
    }

    @Override
    public String lindex(final String key, final int index) {
        return handle(new Callback<String>() {
            @Override
            public String call(redis.clients.jedis.Jedis jedis) {
                return jedis.lindex(key, index);
            }
        }, READ);
    }

    @Override
    public long llen(final String key) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.llen(key);
            }
        }, READ);
    }

    @Override
    public long lrem(final String key, final int count, final String value) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.lrem(key, count, value);
            }
        }, WRITE);
    }

    @Override
    public Set<String> smembers(final String key) {
        return handle(new Callback<Set<String>>() {
            @Override
            public Set<String> call(redis.clients.jedis.Jedis jedis) {
                return jedis.smembers(key);
            }
        }, READ);
    }

    @Override
    public Set<byte[]> smembers(final byte[] key) {
        return handle(new Callback<Set<byte[]>>() {
            @Override
            public Set<byte[]> call(redis.clients.jedis.Jedis jedis) {
                return jedis.smembers(key);
            }
        }, READ);
    }

    @Override
    public long scard(final String key) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.scard(key);
            }
        }, READ);
    }

    @Override
    public long scard(final byte[] key) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.scard(key);
            }
        }, READ);
    }

    @Override
    public long sadd(final byte[] key, final byte[]... members) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.sadd(key, members);
            }
        }, WRITE);
    }

    @Override
    public long sadd(final String key, final String... members) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.sadd(key, members);
            }
        }, WRITE);
    }

    @Override
    public long srem(final byte[] key, final byte[]... members) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.srem(key, members);
            }
        }, WRITE);
    }

    @Override
    public long srem(final String key, final String... members) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.srem(key, members);
            }
        }, WRITE);
    }

    @Override
    public boolean sismember(final String key, final String member) {
        return handle(new Callback<Boolean>() {
            @Override
            public Boolean call(redis.clients.jedis.Jedis jedis) {
                return jedis.sismember(key, member);
            }
        }, WRITE);
    }

    @Override
    public boolean sismember(final byte[] key, final byte[] member) {
        return handle(new Callback<Boolean>() {
            @Override
            public Boolean call(redis.clients.jedis.Jedis jedis) {
                return jedis.sismember(key, member);
            }
        }, WRITE);
    }

    @Override
    public String rpop(final String key) {
        return handle(new Callback<String>() {
            @Override
            public String call(redis.clients.jedis.Jedis jedis) {
                return jedis.rpop(key);
            }
        }, WRITE);
    }

    @Override
    public byte[] rpop(final byte[] key) {
        return handle(new Callback<byte[]>() {
            @Override
            public byte[] call(redis.clients.jedis.Jedis jedis) {
                return jedis.rpop(key);
            }
        }, WRITE);
    }

    @Override
    public long lpush(final byte[] key, final byte[] string) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.lpush(key, string);
            }
        }, WRITE);
    }

    @Override
    public List<byte[]> lrange(final byte[] key, final int start, final int end) {
        return handle(new Callback<List<byte[]>>() {
            @Override
            public List<byte[]> call(redis.clients.jedis.Jedis jedis) {
                return jedis.lrange(key, start, end);
            }
        }, WRITE);
    }

    @Override
    public byte[] lpop(final byte[] key) {
        return handle(new Callback<byte[]>() {
            @Override
            public byte[] call(redis.clients.jedis.Jedis jedis) {
                return jedis.lpop(key);
            }
        }, WRITE);
    }

    @Override
    public String lpop(final String key) {
        return handle(new Callback<String>() {
            @Override
            public String call(redis.clients.jedis.Jedis jedis) {
                return jedis.lpop(key);
            }
        }, WRITE);
    }

    @Override
    public long llen(final byte[] key) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.llen(key);
            }
        }, READ);
    }

    @Override
    public long lrem(final byte[] key, final int count, final byte[] value) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.lrem(key, count, value);
            }
        }, WRITE);
    }

    @Override
    public String ltrim(final byte[] key, final int start, final int end) {
        return handle(new Callback<String>() {
            @Override
            public String call(redis.clients.jedis.Jedis jedis) {
                return jedis.ltrim(key, start, end);
            }
        }, WRITE);
    }

    @Override
    public long rpush(final byte[] key, final byte[] string) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.rpush(key, string);
            }
        }, WRITE);
    }

    @Override
    public long zunion(final String dstkey, final String... sets) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.zunionstore(dstkey, sets);
            }
        }, WRITE);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final double min, final double max) {
        return handle(new Callback<Set<Tuple>>() {
            @Override
            public Set<Tuple> call(redis.clients.jedis.Jedis jedis) {
                return jedis.zrangeByScoreWithScores(key, min, max);
            }
        }, WRITE);
    }

    @Override
    public boolean hexists(final String key, final String field) {
        return handle(new Callback<Boolean>() {
            @Override
            public Boolean call(redis.clients.jedis.Jedis jedis) {
                return jedis.hexists(key, field);
            }
        }, READ);
    }

    @Override
    public boolean hexists(final byte[] key, final byte[] field) {
        return handle(new Callback<Boolean>() {
            @Override
            public Boolean call(redis.clients.jedis.Jedis jedis) {
                return jedis.hexists(key, field);
            }
        }, READ);
    }

    @Override
    public long incr(final String key) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.incr(key);
            }
        }, WRITE);
    }

    @Override
    public long incrBy(final String key, final long value) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.incrBy(key, value);
            }
        }, WRITE);
    }

    @Override
    public long decr(final String key) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.decr(key);
            }
        }, WRITE);
    }

    @Override
    public long decrBy(final String key, final long value) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.decrBy(key, value);
            }
        }, WRITE);
    }

    @Override
    public String get(final String key) {
        return handle(new Callback<String>() {
            @Override
            public String call(redis.clients.jedis.Jedis jedis) {
                return jedis.get(key);
            }
        }, READ);
    }

    @Override
    public boolean exists(final String key) {
        return handle(new Callback<Boolean>() {
            @Override
            public Boolean call(redis.clients.jedis.Jedis jedis) {
                return jedis.exists(key);
            }
        }, READ);
    }

    @Override
    public String set(final String key, final String value) {
        return handle(new Callback<String>() {
            @Override
            public String call(redis.clients.jedis.Jedis jedis) {
                return jedis.set(key, value);
            }
        }, WRITE);
    }

    @Override
    public long lpush(final String key, final String... values) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.lpush(key, values);
            }
        }, WRITE);
    }

    @Override
    public double zincrby(final String key, final double score, final String member) {
        return handle(new Callback<Double>() {
            @Override
            public Double call(redis.clients.jedis.Jedis jedis) {
                return jedis.zincrby(key, score, member);
            }
        }, WRITE);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(final String key, final long start, final long end) {
        return handle(new Callback<Set<Tuple>>() {
            @Override
            public Set<Tuple> call(redis.clients.jedis.Jedis jedis) {
                return jedis.zrevrangeWithScores(key, start, end);
            }
        }, READ);
    }

    @Override
    public long zrevrank(final String key, final String member) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.zrevrank(key, member);
            }
        }, READ);
    }

    @Override
    public double zscore(final String key, final String member) {
        return handle(new Callback<Double>() {
            @Override
            public Double call(redis.clients.jedis.Jedis jedis) {
                return jedis.zscore(key, member);
            }
        }, READ);
    }

    @Override
    public List<String> blpop(final int timeout, final String... channels) {
        return handle(new Callback<List<String>>() {
            @Override
            public List<String> call(redis.clients.jedis.Jedis jedis) {
                return jedis.blpop(timeout, channels);
            }
        }, WRITE);
    }

    @Override
    public List<byte[]> blpop(final int timeout, final byte[]... channels) {
        return handle(new Callback<List<byte[]>>() {
            @Override
            public List<byte[]> call(redis.clients.jedis.Jedis jedis) {
                return jedis.blpop(timeout, channels);
            }
        }, WRITE);
    }

    @Override
    public List<byte[]> hmget(final byte[] key, final byte[]... fields) {
        return handle(new Callback<List<byte[]>>() {
            @Override
            public List<byte[]> call(redis.clients.jedis.Jedis jedis) {
                return jedis.hmget(key, fields);
            }
        }, READ);
    }

    @Override
    public List<? extends Object> hmget(final Object key, final List<? extends Object> fields) {
        final String keyStr = ByteObjectConvertUtil.getBytesFromObject(key);
        final String[] fieldStr = new String[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i) != null) {
                fieldStr[i] = ByteObjectConvertUtil.getBytesFromObject(fields.get(i));
            }
        }
        List<String> retStr = handle(new Callback<List<String>>() {
            @Override
            public List<String> call(redis.clients.jedis.Jedis jedis) {
                return jedis.hmget(keyStr, fieldStr);
            }
        }, READ);
        if (retStr != null && retStr.size() > 0) {
            List<Object> rets = Lists.newArrayList();
            for (String valueStr : retStr) {
                if (valueStr != null && valueStr.length() > 0) {
                    rets.add(ByteObjectConvertUtil.getObjectFromBytes(valueStr));
                }
            }
            return rets;
        }
        return null;
    }

    @Override
    public RedisInitParam baseConfig() {
        return baseConfig;
    }

    @Override
    public <T> T callOriginalJedis(Callback<T> callback, JedisType type) {
        return invoke(callback, getPoolByType(type), getSemaphoreByType(type));
    }

    @Override
    public void publish(final String topic, final String message) {
        handle(new Callback<Void>() {
            @Override
            public Void call(redis.clients.jedis.Jedis jedis) {
                jedis.publish(topic, message);
                return null;
            }
        }, WRITE);
    }

    @Override
    public void subscribe(final JedisPubSub jedisPubSub, final String... topic) {
        handle(new Callback<Void>() {
            @Override
            public Void call(redis.clients.jedis.Jedis jedis) {
                jedis.subscribe(jedisPubSub, topic);
                return null;
            }
        }, WRITE);
    }

    @Override
    public long setnx(final String key, final String value) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.setnx(key, value);
            }
        }, WRITE);
    }

    @Override
    public long setnx(final byte[] key, final byte[] value) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.setnx(key, value);
            }
        }, WRITE);
    }

    @Override
    public long ttl(final byte[] key) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.ttl(key);
            }
        }, READ);
    }

    @Override
    public long ttl(final String key) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.ttl(key);
            }
        }, READ);
    }

    @Override
    public long zlexcount(final String key, final String s, final String s1) {
        return handle(new Callback<Long>() {
            @Override
            public Long call(redis.clients.jedis.Jedis jedis) {
                return jedis.zlexcount(key, s, s1);
            }
        }, READ);
    }
}
