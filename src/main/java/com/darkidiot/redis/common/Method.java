package com.darkidiot.redis.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * 订阅消费消息方法名枚举类型
 *
 * @author darkidiot
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public enum Method {
    update("update"),
    put("put"),
    remove("remove"),
    clear("clear");

    private String methodName;
}
