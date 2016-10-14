package com.gome.supermain.redis.jedis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Tuple;

/**
 * Redis缓存公共接口
 * 
 * @author darkidiot
 */
public interface IJedis {
	/**
	 * 发布
	 * 
	 * @param topic
	 *            主题
	 * @param message
	 *            消息
	 */
	void publish(String topic, String message);

	/**
	 * 订阅
	 * 
	 * @param jedisPubSub
	 * @param topic
	 */
	void subscribe(JedisPubSub jedisPubSub, String... topic);

	Set<Tuple> zrangeWithScores(byte[] key, int offset, int len);

	byte[] get(byte[] key);

	String get(String key);

	boolean zadd(byte[] key, double score, byte[] value);

	boolean zadd(String key, double score, String value);

	boolean zadd(byte[] key, Map<byte[], Double> scoreMembers);

	boolean zrem(byte[] key, byte[] value);

	Set<Tuple> zrevrangeWithScores(byte[] key, int start, int end);

	double zscore(byte[] key, byte[] value);

	// include min and max
	Set<byte[]> zrangeByScore(byte[] key, double min, double max);

	String set(byte[] key, byte[] value);

	String set(String key, long value);

	long incr(byte[] key);

	long incr(String key);

	long incrBy(byte[] key, long value);

	long incrBy(String key, long value);

	long decr(byte[] key);

	long decr(String key);

	long decrBy(byte[] key, long value);

	long decrBy(String key, long value);

	Set<byte[]> keys(byte[] pattern);

	long zcard(byte[] key);

	long zremrangeByRank(byte[] key, int offset, int len);

	long zremrangeByRankV2(byte[] key, int start, int end);

	long zremrangeByScore(byte[] key, int start, int end);

	String info();

	String hget(String key, String field);

	byte[] hget(byte[] key, byte[] field);

	long hset(String key, String field, String value);

	long hset(byte[] key, byte[] field, byte[] value);

	long hdel(String key, String field);

	long hdel(byte[] key, byte[] field);

	Map<String, String> hgetAll(String key);

	long expire(String key, int seconds);

	long del(String key);

	long del(byte[] key);

	long expire(byte[] key, int seconds);

	long linsert(String key, boolean where, String pivot, String value);

	long lpush(String key, String value);

	long rpush(String key, String value);

	String ltrim(String key, long start, long end);

	List<String> lrange(String key, long start, long end);

	String lindex(String key, int index);

	long llen(String key);

	long lrem(String key, int count, String value);

	String rpop(String key);

	byte[] rpop(byte[] key);

	long lpush(byte[] key, byte[] string);

	List<byte[]> lrange(byte[] key, int start, int end);

	byte[] lpop(byte[] key);

	String lpop(String key);

	long llen(byte[] key);

	long lrem(byte[] key, int count, byte[] value);

	String ltrim(byte[] key, int start, int end);

	long rpush(byte[] key, byte[] string);

	long sadd(final String key, final String... members);

	long sadd(final byte[] key, final byte[]... members);

	long srem(final byte[] key, final byte[]... members);

	long srem(final String key, final String... members);

	Set<byte[]> smembers(byte[] key);

	Set<String> smembers(String key);

	long scard(byte[] key);

	long scard(String key);

	boolean sismember(String key, String member);

	boolean sismember(byte[] key, byte[] member);

	Map<byte[], byte[]> hgetAll(byte[] key);

	List<byte[]> hmget(byte[] key, byte[]... fields);

	List<? extends Object> hmget(Object key, List<? extends Object> fields);

	long hincrBy(String key, String field, long value);

	boolean exists(byte[] key);

	String rename(byte[] oldkey, byte[] newkey);

	long renamenx(byte[] oldkey, byte[] newkey);

	long zunion(String dstkey, String... sets);

	Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max);

	boolean hexists(String key, String field);

	boolean hexists(byte[] key, byte[] field);

	boolean exists(String key);

	String set(String key, String value);

	long lpush(String key, String... values);

	double zincrby(String key, double score, String member);

	long zrevrank(String key, String member);

	Set<Tuple> zrevrangeWithScores(String key, long start, long end);

	double zscore(String key, String member);

	List<String> blpop(int timeout, String... channels);

	List<byte[]> blpop(int timeout, byte[]... channels);

	long setnx(final String key, final String value);

	long setnx(final byte[] key, final byte[] value);

	long ttl(final byte[] key);

	long ttl(final String key);
}
