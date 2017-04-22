package com.darkidiot.redis.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * 控制JedisPool分为读写线程池
 *
 * @author darkidiot
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public enum JedisType {
    //写类型
    WRITE(0),
    //读类型
    READ(1);

    private int type;
}
