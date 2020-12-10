package com.self.learnjava.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.self.learnjava.service.StorageService;

/*
 * 注意到LocalStorageService使用了条件装配@Profile("default")，即默认启用LocalStorageService，而CloudStorageService使用了条件装配@Profile("!default")，即非default环境时，自动启用CloudStorageService。这样，一套代码，就实现了不同环境启用不同的配置。
 * 我们以最常用的@ConditionalOnProperty为例，把上一节的StorageService改写如下。首先，定义配置storage.type=xxx，用来判断条件，默认为local：
 * storage:
  	type: ${STORAGE_TYPE:local}
 * 设定为local时，启用LocalStorageService：
 * 注意到LocalStorageService的注解，当指定配置为local，或者配置不存在，均启用LocalStorageService。
 * 可见，Spring Boot提供的条件装配使得应用程序更加具有灵活性。
 * 小结
 * Spring Boot提供了几个非常有用的条件装配注解，可实现灵活的条件装配。
 */
@Component
//@Profile("default")
@ConditionalOnProperty(value="storage.type", havingValue="local", matchIfMissing=true)
public class LocalStorageService implements StorageService {
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${storage.local:/var/static}")
	String localStorageRootDir;
	
	private File localStorageRoot;
	
	@PostConstruct
	public void init() {
		logger.info("Intializing local storage with root dir: {}", this.localStorageRootDir);
		this.localStorageRoot = new File(this.localStorageRootDir);
	}
	
	/*
	 * 根据URI打开InputStream:(non-Javadoc)
	 */
	@Override
	public InputStream openInputStream(String uri) throws IOException {
		File targetFile = new File(this.localStorageRoot, uri);
		return new BufferedInputStream(new FileInputStream(targetFile));
	}
	
	/*
	 * 根据扩展名+InputStream保存并返回URI:
	 */
	@Override
	public String store(String extName, InputStream input) throws FileNotFoundException, IOException {
		String fileName = UUID.randomUUID().toString() + "." + extName;
		File targetFile = new File(this.localStorageRoot, fileName);
		try(OutputStream output = new BufferedOutputStream(new FileOutputStream(targetFile))){
			//input.transerTo(output);
		}
		return fileName;
	}
}
