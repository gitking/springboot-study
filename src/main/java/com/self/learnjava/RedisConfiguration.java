package com.self.learnjava;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

@ConfigurationProperties("spring.redis")
public class RedisConfiguration {
	private String host;
	private int port;
	private String password;
	private int database;
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		System.out.println("spring设置进来的host为:" + host);
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		System.out.println("spring设置进来的端口为:" + port);
		this.port = port;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		System.out.println("spring设置进来的host为:" + host);
		this.password = password;
	}
	public int getDatabase() {
		return database;
	}
	public void setDatabase(int database) {
		this.database = database;
	}
	
	@Bean
	RedisClient redisClient() {
		RedisURI uri = RedisURI.Builder.redis(this.host, this.port).withPassword(this.password)
				.withDatabase(this.database).build();
		return RedisClient.create(uri);
	}
}
