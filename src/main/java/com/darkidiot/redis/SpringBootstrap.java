package com.darkidiot.redis;


import com.darkidiot.redis.config.RedisInitParam;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Spring boot 启动类
 * @Author darkidiot
 */

@SpringBootApplication
@EnableConfigurationProperties(RedisInitParam.class)
public class SpringBootstrap {
	
    public static void main(String[] args) {
        SpringApplication.run(SpringBootstrap.class,args);
    }
}
