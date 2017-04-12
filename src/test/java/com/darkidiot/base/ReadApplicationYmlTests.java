package com.darkidiot.base;

import com.darkidiot.redis.IRedisMap;
import com.darkidiot.redis.Redis;
import com.darkidiot.redis.SpringBootstrap;
import com.darkidiot.redis.config.JedisPoolFactory;
import com.darkidiot.redis.config.RedisInitParam;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootstrap.class)
@Slf4j
public class ReadApplicationYmlTests {  
    @Autowired  
    private RedisInitParam param;
    
    @Autowired
    private JedisPoolFactory factory;
    
    @Autowired
    private Redis redis;
    
    @Test  
    public void testYmlValue() {  
        log.info(param.toString());
    }  
    
    @Test  
    public void testRedis() {
    	IRedisMap<String, String> use = redis.<String,String>use("Test");
		use.put("Test", "124");
		IRedisMap<String, String> use2 = redis.<String,String>use("Test");
    	log.info(use2.get("Test"));
    }  
    
    @Test 
    public void testJedisPoolFactory(){
    	log.info(factory.getReadPool().toString());
    	log.info(factory.getWritePool().toString());
    	log.info(factory.getInitParam().getOpenLocalCache().toString());
    }
    
}