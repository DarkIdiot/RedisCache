package com.gome.supermain.redis.common;

/**
 * 订阅消费消息方法名枚举类型
 * @author darkidiot
 */
public enum Method {
	update("update"),
	put("put"),
	remove("remove"),
	clear("clear");
	
	private String methodName;

	private Method(String methodName) {
		this.methodName = methodName;
	}
	
	@Override
    public String toString(){  
        return this.methodName;  
    }  
}
