package com.self.learnjava.service.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.self.learnjava.service.StorageService;

/*
 * 注意到LocalStorageService使用了条件装配@Profile("default")，即默认启用LocalStorageService，而CloudStorageService使用了条件装配@Profile("!default")，即非default环境时，自动启用CloudStorageService。这样，一套代码，就实现了不同环境启用不同的配置。
 * 小结
 * Spring Boot允许在一个配置文件中针对不同Profile进行配置；
 * Spring Boot在未指定Profile时默认为default。
 */
@Component
@Profile("!default")
public class CloudStorageService implements StorageService{
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${storage.cloud.bucket:}")
	String bucket;
	
	@Value("${storage.cloud.access-key:}")
	String accessKey;
	
	@Value("${storage.cloud.access-secret:}")
	String accessSecret;
	
	@PostConstruct
	public void init() {
		logger.info("Initializing cloud storage...");
	}
	
	@Override
	public InputStream openInputStream(String uri) throws IOException {
		throw new IOException("File not found: " + uri);
	}
	
	@Override
	public String store(String extName, InputStream input) throws IOException {
		throw new IOException("Unable to access cloud storage.");
	}
}
