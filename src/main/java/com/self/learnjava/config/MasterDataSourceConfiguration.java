package com.self.learnjava.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/*
 * 注意到下述class并未添加@Configuration和@Component，要使之生效，可以使用@Import导入：
 * 此外，上述两个DataSource的Bean名称分别为masterDataSource和slaveDataSource，我们还需要一个最终的@Primary标注的DataSource，它采用Spring提供的AbstractRoutingDataSource，代码实现如下：
 */
public class MasterDataSourceConfiguration {
	
	@Bean("masterDataSourceProperties")
	@ConfigurationProperties("spring.datasource-master")
	DataSourceProperties dataSourceProperties() {
		return new DataSourceProperties();
	}
	
	@Bean(RoutingDataSourceContext.MASTER_DATASOURCE)
	DataSource dataSource(@Autowired @Qualifier("masterDataSourceProperties")DataSourceProperties props) {
		return props.initializeDataSourceBuilder().build();
	}
}
