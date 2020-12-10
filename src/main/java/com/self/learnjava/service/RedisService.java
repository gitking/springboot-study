package com.self.learnjava.service;

import java.time.Duration;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.support.ConnectionPoolSupport;

/*
 * 访问Redis
 * 在Spring Boot中，要访问Redis，可以直接引入spring-boot-starter-data-redis依赖，它实际上是Spring Data的一个子项目——Spring Data Redis，主要用到了这几个组件：
 * Lettuce：一个基于Netty的高性能Redis客户端；
 * RedisTemplate：一个类似于JdbcTemplate的接口，用于简化Redis的操作。
 * 因为Spring Data Redis引入的依赖项很多，如果只是为了使用Redis，完全可以只引入Lettuce，剩下的操作都自己来完成。
 * 本节我们稍微深入一下Redis的客户端，看看怎么一步一步把一个第三方组件引入到Spring Boot中。
 * 首先，我们添加必要的几个依赖项：
 * io.lettuce:lettuce-core
 * org.apache.commons:commons-pool2
 * 注意我们并未指定版本号，因为在spring-boot-starter-parent中已经把常用组件的版本号确定下来了。
 * 第一步是在配置文件application.yml中添加Redis的相关配置：
 * spring:
	  redis:
	    host: ${REDIS_HOST:localhost}
	    port: ${REDIS_PORT:6379}
	    password: ${REDIS_PASSWORD:}
	    ssl: ${REDIS_SSL:false}
	    database: ${REDIS_DATABASE:0}
 * 然后，通过RedisConfiguration来加载它：
 * @ConfigurationProperties("spring.redis")
	public class RedisConfiguration {
		private String host;
		private int port;
		private String password;
		private int database;
	
	    // getters and setters...
	}
 * 再编写一个@Bean方法来创建RedisClient，可以直接放在RedisConfiguration中：
 * 在启动入口引入该配置：
 * @Import(RedisConfiguration.class) // 加载Redis配置
 * 注意：如果在RedisConfiguration中标注@Configuration，则可通过Spring Boot的自动扫描机制自动加载，否则，使用@Import手动加载。
 * 紧接着，我们用一个RedisService来封装所有的Redis操作。基础代码如下：
 * 注意到上述代码引入了Commons Pool的一个对象池，用于缓存Redis连接。因为Lettuce本身是基于Netty的异步驱动，在异步访问时并不需要创建连接池，但基于Servlet模型的同步访问时，连接池是有必要的。连接池在@PostConstruct方法中初始化，在@PreDestroy方法中关闭。
 * 下一步，是在RedisService中添加Redis访问方法。为了简化代码，我们仿照JdbcTemplate.execute(ConnectionCallback)方法，传入回调函数，可大幅减少样板代码。
 * 首先定义回调函数接口SyncCommandCallback：
 * 编写executeSync方法，在该方法中，获取Redis连接，利用callback操作Redis，最后释放连接，并返回操作结果：
 * public <T> T executeSync(SyncCommandCallback<T> callback) {
    try (StatefulRedisConnection<String, String> connection = redisConnectionPool.borrowObject()) {
        connection.setAutoFlushCommands(true);
        RedisCommands<String, String> commands = connection.sync();
        return callback.doInConnection(commands);
    } catch (Exception e) {
        logger.warn("executeSync redis failed.", e);
        throw new RuntimeException(e);
    }
}
 * 有的童鞋觉得这样访问Redis的代码太复杂了，实际上我们可以针对常用操作把它封装一下，例如set和get命令：
 * 类似的，hget和hset操作如下：
 * 常用命令可以提供方法接口，如果要执行任意复杂的操作，就可以通过executeSync(SyncCommandCallback<T>)来完成。
 * 完成了RedisService后，我们就可以使用Redis了。例如，在UserController中，我们在Session中只存放登录用户的ID，用户信息存放到Redis，提供两个方法用于读写：
 * 
 * 从Redis读写Java对象时，序列化和反序列化是应用程序的工作，上述代码使用JSON作为序列化方案，简单可靠。也可将相关序列化操作封装到RedisService中，这样可以提供更加通用的方法：
 * public <T> T get(String key, Class<T> clazz) {
	    ...
	}
	
	public <T> T set(String key, T value) {
	    ...
	}
 * 小结
 * Spring Boot默认使用Lettuce作为Redis客户端，同步使用时，应通过连接池提高效率。
 * 记得安装redis哦
 * redis在各大操作系统中的安装使用都非常简单，默认配置就是监听127.0.0.1:6379且无帐号密码
 * Java书籍推荐
 * 廖老师，本教程知识来源于哪本书呀？想再看看巩固巩固。
 * 廖雪峰
 * Java核心技术
 * Java编程思想
 * Effective Java
 * Expert One-to-One J2EE Development
 * Spring官网文档
 * 《Java编程思想》有新版了，叫《On Java 8》
 */
@Component
public class RedisService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	RedisClient redisClient;
	
	private GenericObjectPool<StatefulRedisConnection<String, String>> redisConnectionPool;
	
	@PostConstruct
	public void init() {
		GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig = new GenericObjectPoolConfig<>();
		poolConfig.setMaxTotal(20);
		poolConfig.setMaxIdle(5);
		poolConfig.setTestOnReturn(true);
		poolConfig.setTestWhileIdle(true);
		this.redisConnectionPool = ConnectionPoolSupport.createGenericObjectPool(()->redisClient.connect(), poolConfig);
	}
	
	@PreDestroy
	public void shutdown() {
		this.redisConnectionPool.close();
		this.redisClient.shutdown();
	}
	
	public <T> T executeSync(SyncCommandCallback<T> callback) {
		try(StatefulRedisConnection<String, String> connection = redisConnectionPool.borrowObject()) {
			connection.setAutoFlushCommands(true);
			RedisCommands<String, String> commands = connection.sync();
			return callback.doInConnection(commands);
		} catch (Exception e) {
			logger.warn("executeSync redis failed.", e);
			throw new RuntimeException(e);
		}
	}
	
	public String set(String key, String value) {
		return executeSync(commands -> commands.set(key, value));
	}
	
	public String set(String key, String value, Duration timeout) {
		return executeSync(commands -> commands.setex(key, timeout.toNanos(), value));
	}
	
	public String get(String key) {
		return executeSync(commands -> commands.get(key));
	}
	
	public boolean hset(String key, String field, String value) {
		return executeSync(commands -> commands.hset(key, field, value));
	}
	
	public String hget(String key, String field){
		return executeSync(commands -> commands.hget(key, field));
	}
	
	public Map<String, String> hgetall(String key) {
		return executeSync(commands -> commands.hgetall(key));
	}
}
