Redis Cache
=
Tips：
-
     required: JDK1.7+
Overview
-
#### `RedisCache`是基于`Jedis`的SDK。
+ ①实现了防穿透`缓存`，`分布式锁`，`分布式队列`，本地缓存同步更新<br>
+ ②个性化的redis源配置支持<br>

# Usage:

### RedisCache
`Normal Style`
```Java
	IRedisMap<String, String> cache = Redis.use("redisSourceName");
	cache.put("redisKey","redisValue");
	cache.get("redisKey");
```
`Fluent Style`
```Java
	IRedisMap<String, String> cache = Redis.create().setServiceName("redisSourceName").build();
	cache.put("redisKey","redisValue");
	cache.get("redisKey");
```
### RedisLock
#### Normal Style
```Java
    Lock lock = RedisLock.useSimpleRedisLock("simpleLock", "redisSourceName");
    lock.lock();
    lock.isLocking();
    lock.unlock();
```
#### Fluent Style
```Java
    Lock lock = RedisLock.create().setService("redisSourceName").setLockName("simpleLock").useSimpleRedisLock();
    lock.lock();
    lock.isLocking();
    lock.unlock();
```
### RedisQueue
#### Normal Style
```Java
    Queue<Task> queue = RedisQueue.useSimpleFifoQueue("simpleQueue","redisSourceName");
    queue.enqueue(task1,task2,task3);
    Task task = queue.dequeue();
```
#### Fluent Style
```Java
    Queue<Task> queue = RedisQueue.create().setService("redisSourceName").setQueueName("simpleQueue").useSimpleFifoQueue();
    queue.enqueue(task1,task2,task3);
    Task task = queue.dequeue();
```

# Configuration
## pattern I @for connection@
>>> service.names=<b>redisSourceName</b>,<b>redisSourceName1</b>,...

### Stand-Alone
>>> <b>redisSourceName</b>.ip = <font color="grey ">127.0.0.1</font><br>
>>> <b>redisSourceName</b>.port = <font color="grey ">6379</font><br>
>>> <b>redisSourceName</b>.password=<font color="grey ">password</font><br>

#### <font color="green">Simplified Way

>>> <b>redisSourceName</b>.ip-port-password = <font color="grey ">127.0.0.1:6379?password</font></font>

### Sentinel
>>> <b>redisSourceName1</b>.is.cluster = <font color="grey ">true</font><br>
>>> <b>redisSourceName1</b>.sentinel.master.name = <font color="grey ">masterName</font><br>
>>> <b>redisSourceName1</b>.sentinel.hosts = <font color="grey ">127.0.0.1:6379;127.0.0.1:6389;127.0.0.1:6379;127.0.0.1:6399</font><br>
>>> <b>redisSourceName1</b>.password = <font color="grey ">password</font><br>

## pattern II @for basic@

>>> <b>redisSourceName</b>.open.local.cache = <font color="grey ">true</font><br>
>>> <b>redisSourceName</b>[.read].timeoutInMillis = <font color="grey ">10000</font><br>
>>> <b>redisSourceName</b>[.write].timeoutInMillis = <font color="grey ">10000</font><br>
>>> <b>redisSourceName</b>[.read].maxTotal = <font color="grey ">8</font><br>
>>> <b>redisSourceName</b>[.write].maxTotal = <font color="grey ">8</font><br>
>>> <b>redisSourceName</b>[.read].maxIdle = <font color="grey ">20</font><br>
>>> <b>redisSourceName</b>[.write].maxIdle = <font color="grey ">20</font><br>
>>> <b>redisSourceName</b>[.read].maxWait = <font color="grey ">2000</font><br>
>>> <b>redisSourceName</b>[.write].maxWait = <font color="grey ">2000</font><br>
>>> <b>redisSourceName</b>[.read].testOnBorrow = <font color="grey ">true</font><br>
>>> <b>redisSourceName</b>[.write].testOnBorrow = <font color="grey ">true</font><br>
>>> <b>redisSourceName</b>[.read].testOnReturn = <font color="grey ">false</font><br>
>>> <b>redisSourceName</b>[.write].testOnReturn = <font color="grey ">false</font><br>

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