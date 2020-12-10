package com.self.learnjava.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/*
 * 此外，上述两个DataSource的Bean名称分别为masterDataSource和slaveDataSource，
 * 我们还需要一个最终的@Primary标注的DataSource，它采用Spring提供的AbstractRoutingDataSource，代码实现如下：
 */
public class RoutingDataSourceConfiguration {
	
	/*
	 * RoutingDataSource本身并不是真正的DataSource，它通过Map关联一组DataSource，下面的代码创建了包含两个DataSource的RoutingDataSource，
	 * 关联的key分别为masterDataSource和slaveDataSource：
	 */
	@Primary
	@Bean
	DataSource dataSource(@Autowired @Qualifier(RoutingDataSourceContext.MASTER_DATASOURCE)DataSource masterDataSource,
			@Autowired @Qualifier(RoutingDataSourceContext.SLAVE_DATASOURCE)DataSource slaveDataSource) {
		RoutingDataSource ds = new RoutingDataSource();
		Map<Object, Object> info = new HashMap<>();// 关联两个DataSource:
		info.put(RoutingDataSourceContext.MASTER_DATASOURCE, masterDataSource);
		info.put(RoutingDataSourceContext.SLAVE_DATASOURCE, slaveDataSource);
		ds.setTargetDataSources(info);
		ds.setDefaultTargetDataSource(masterDataSource);// 默认使用masterDataSource:
		return ds;
	}
	
	/*
	 * 仍然需要自己创建JdbcTemplate和PlatformTransactionManager，注入的是标记为@Primary的RoutingDataSource。
	 * 这样，我们通过如下的代码就可以切换RoutingDataSource底层使用的真正的DataSource：
	 * RoutingDataSourceContext.setDataSourceRoutingKey("slaveDataSource");
     * jdbcTemplate.query(...);
     * 只不过写代码切换DataSource即麻烦又容易出错，更好的方式是通过注解配合AOP实现自动切换，这样，客户端代码实现如下：
     * @Controller
		public class UserController {
			@RoutingWithSlave // <-- 指示在此方法中使用slave数据库
			@GetMapping("/profile")
			public ModelAndView profile(HttpSession session) {
		        ...
		    }
		}
	 */
	@Bean
	JdbcTemplate jdbcTemplate(@Autowired DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
	
	@Bean
	DataSourceTransactionManager dataSourceTransactionManager(@Autowired DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
}

/*
 * RoutingDataSource本身并不是真正的DataSource，它通过Map关联一组DataSource，下面的代码创建了包含两个DataSource的RoutingDataSource，关联的key分别为masterDataSource和slaveDataSource：
 */
class RoutingDataSource extends AbstractRoutingDataSource {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	protected Object determineCurrentLookupKey() {
        // 从ThreadLocal中取出key:
		return RoutingDataSourceContext.getDataSourceRoutingKey();
	}
	
	/*
	 * 如果我们想要确认是否真的切换了DataSource，可以覆写determineTargetDataSource()方法并打印出DataSource的名称
	 * 访问不同的URL，可以在日志中看到两个DataSource，分别是HikariPool-1和hikariPool-2：
	 * 2020-06-14 17:55:21.676  INFO 91561 --- [nio-8080-exec-7] c.i.learnjava.config.RoutingDataSource   : determin target datasource: HikariDataSource (HikariPool-1)
     * 2020-06-14 17:57:08.992  INFO 91561 --- [io-8080-exec-10] c.i.learnjava.config.RoutingDataSource   : determin target datasource: HikariDataSource (HikariPool-2)
     * 我们用一个图来表示创建的DataSource以及相关Bean的关系：
     *  ┌────────────────────┐       ┌──────────────────┐
		│@Primary            │<──────│   JdbcTemplate   │
		│RoutingDataSource   │       └──────────────────┘
		│ ┌────────────────┐ │       ┌──────────────────┐
		│ │MasterDataSource│ │<──────│DataSource        │
		│ └────────────────┘ │       │TransactionManager│
		│ ┌────────────────┐ │       └──────────────────┘
		│ │SlaveDataSource │ │
		│ └────────────────┘ │
	    └────────────────────┘
	 * 注意到DataSourceTransactionManager和JdbcTemplate引用的都是RoutingDataSource，所以，这种设计的一个限制就是：在一个请求中，一旦切换了内部数据源，在同一个事务中，不能再切到另一个，否则，DataSourceTransactionManager和JdbcTemplate操作的就不是同一个数据库连接。
	 */
	@Override
	protected DataSource determineTargetDataSource() {
		DataSource ds = super.determineTargetDataSource();
		logger.info("determin target datasource: {}", ds);
		return ds;
	}
}
