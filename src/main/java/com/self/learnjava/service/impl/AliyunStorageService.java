package com.self.learnjava.service.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.self.learnjava.service.StorageService;

/*
 * 设定为aliyun时，启用AliyunStorageService：
 */
@Component
@ConditionalOnProperty(value="storage.type", havingValue="aliyun")
public class AliyunStorageService implements StorageService{
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	@PostConstruct
	public void init() {
		logger.info("Initializing Aliyun storage...");
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
