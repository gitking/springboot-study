package com.self.learnjava.entity;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/*
 * 为了更好地管理配置，Spring Boot允许创建一个Bean，持有一组配置，并由Spring Boot自动注入。
 * 假设我们在application.yml中添加了如下配置：
 * storage:
	  local:
	    # 文件存储根目录:
	    root-dir: ${STORAGE_LOCAL_ROOT:/var/storage}
	    # 最大文件大小，默认100K:
	    max-size: ${STORAGE_LOCAL_MAX_SIZE:102400}
	    # 是否允许空文件:
	    allow-empty: false
	    # 允许的文件类型:
	    allow-types: jpg, png, gif
 * 可以首先定义一个Java Bean，StorageConfiguration持有该组配置：	    
 * 保证Java Bean的属性名称与配置一致即可。然后，我们添加两个注解：
 * 注意到@ConfigurationProperties("storage.local")表示将从配置项storage.local读取该项的所有子项配置，
 * 并且，@Configuration表示StorageConfiguration也是一个Spring管理的Bean，可直接注入到其他Bean中：
 */
@Configuration
@ConfigurationProperties("storage.local")
public class StorageConfiguration {
	private String rootDir;
	private int maxSize;
	private boolean allowEmpty;
	private List<String> allowTypes;
	public String getRootDir() {
		return rootDir;
	}
	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
	}
	public int getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	public boolean isAllowEmpty() {
		return allowEmpty;
	}
	public void setAllowEmpty(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
	}
	public List<String> getAllowTypes() {
		return allowTypes;
	}
	public void setAllowTypes(List<String> allowTypes) {
		this.allowTypes = allowTypes;
	}
}
