package com.gome.supermain.redis.queue.impl;

public class Constants {
	static final String QUEUE_PREFIX = "Queue:";
	
	/** 默认入队超时时间 (超时会打印警告日志) */
	static final long defaultEnqueueTimeout = 10*1000L;
	/** 默认出队超时时间 (超时会打印警告日志) */
	static final long defaultDequeueTimeout = 10*1000L;
	/** 默认检查队顶元素超时时间 (超时会打印警告日志) */
	static final long defaultTopQueueTimeout = 3*1000L;
	/** 默认查询队列长度超时时间 (超时会打印警告日志) */
	static final long defaultQueryQueueSzieTimeout = 3*1000L;
	/** 默认 清空队列长度超时时间 (超时会打印警告日志) */
	static final long defaultClearQueueTimeout = 3*1000L;

	/** 多队列情况下,用于分割队列名与队列编号的分隔符 */
	static final String queueNameSequenceSeparator = ":";
	/** redis通配符 */
	static final String asterisk = "*";
	/** 获取资源失败后重试的时间间隔单位(实际按斐波那契数列递增) */
	static final long defaultWaitIntervalInMSUnit = 5L;
	
	static String createKey(String queueKey){
		return  QUEUE_PREFIX + queueKey;
	}
	
	static String createKey(String queueName,int priority){
		return  QUEUE_PREFIX + queueName + queueNameSequenceSeparator + priority / 2;
	}
	
	static String createKeyByAsterisk(String queueName){
		return  QUEUE_PREFIX + queueName + queueNameSequenceSeparator + asterisk;
	}
}
