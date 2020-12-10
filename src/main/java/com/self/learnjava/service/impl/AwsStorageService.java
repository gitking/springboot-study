package com.self.learnjava.service.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.self.learnjava.service.StorageService;

/*
 * 设定为aws时，启用AwsStorageService：
 */
@Component
@ConditionalOnProperty(value="storage.type", havingValue="aws")
public class AwsStorageService implements StorageService{
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${storage.aws.bucket:}")
	String bucket;
	
	@Value("${storage.aws.access-key:}")
	String accessKey;
	
	@Value("${storage.aws.access-secret:}")
	String accessSecret;
	
	@PostConstruct
	public void init(){
		logger.info("Initializing AWS storage...");
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
