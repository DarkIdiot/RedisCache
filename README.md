# Redis Cache
### Tips：
     required: JDK1.7+
# Overview
##### RedisCache是基于Redis的客户端，实现了防穿透缓存，分布式锁，分布式队列，以及本地缓存等功能。
# configuration:
## connection
### mode one

## basic
#### redis.[.read].pool.testOnReturn=false
#### redis.[.write].pool.testOnReturn=false
# Usage:

# Dependencies:
    <dependencies>
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
        </dependency>
        <dependency>
            <groupId>commons-logging</groupId>
            <artifactId>commons-logging</artifactId>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
        </dependency>
    </dependencies>