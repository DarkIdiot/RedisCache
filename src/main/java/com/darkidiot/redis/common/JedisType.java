package com.darkidiot.redis.common;

/**
 * 控制JedisPool分为读写线程池
 * @author darkidiot
 *
 */
public enum JedisType {
	//写类型
	WRITE(0),
	//读类型
	READ(1);

	private int type;

	private JedisType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
