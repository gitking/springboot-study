package com.self.learnjava;

import javax.sql.DataSource;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.self.learnjava.config.MasterDataSourceConfiguration;
import com.self.learnjava.config.RoutingDataSourceConfiguration;
import com.self.learnjava.config.SlaveDataSourceConfiguration;

/**
 * Spring Boot开发
 * 我们已经在前面详细介绍了Spring框架，它的主要功能包括IoC容器、AOP支持、事务支持、MVC开发以及强大的第三方集成功能等。
 * 那么，Spring Boot又是什么？它和Spring是什么关系？
 * Spring Boot是一个基于Spring的套件，它帮我们预组装了Spring的一系列组件，以便以尽可能少的代码和配置来开发基于Spring的Java应用程序。
 * 以汽车为例，如果我们想组装一辆汽车，我们需要发动机、传动、轮胎、底盘、外壳、座椅、内饰等各种部件，然后把它们装配起来。Spring就相当于提供了一系列这样的部件，但是要装好汽车上路，还需要我们自己动手。而Spring Boot则相当于已经帮我们预装好了一辆可以上路的汽车，如果有特殊的要求，例如把发动机从普通款换成涡轮增压款，可以通过修改配置或编写少量代码完成。
 * 因此，Spring Boot和Spring的关系就是整车和零部件的关系，它们不是取代关系，试图跳过Spring直接学习Spring Boot是不可能的。
 * Spring Boot的目标就是提供一个开箱即用的应用程序架构，我们基于Spring Boot的预置结构继续开发，省时省力。
 * 本章我们将详细介绍如何使用Spring Boot。
 * 第一个Spring Boot应用
 * 要了解Spring Boot，我们先来编写第一个Spring Boot应用程序，看看与前面我们编写的Spring应用程序有何异同。
 * 我们新建一个springboot-hello的工程，创建标准的Maven目录结构如下：
 * springboot-hello
	├── pom.xml
	├── src
	│   └── main
	│       ├── java
	│       └── resources
	│           ├── application.yml
	│           ├── logback-spring.xml
	│           ├── static
	│           └── templates
	└── target
 * 其中，在src/main/resources目录下，注意到几个文件：
 * application.yml
 * 这是Spring Boot默认的配置文件，它采用YAML(https://yaml.org/)格式而不是.properties格式，文件名必须是application.yml而不是其他名称。
 * YAML格式比key=value格式的.properties文件更易读。比较一下两者的写法：
 * 使用.properties格式：
 * # application.properties
	spring.application.name=${APP_NAME:unnamed}
	spring.datasource.url=jdbc:hsqldb:file:testdb
	spring.datasource.username=sa
	spring.datasource.password=
	spring.datasource.dirver-class-name=org.hsqldb.jdbc.JDBCDriver
	spring.datasource.hikari.auto-commit=false
	spring.datasource.hikari.connection-timeout=3000
	spring.datasource.hikari.validation-timeout=3000
	spring.datasource.hikari.max-lifetime=60000
	spring.datasource.hikari.maximum-pool-size=20
	spring.datasource.hikari.minimum-idle=1
 * 使用YAML格式：
 * # application.yml
	spring:
	  application:
	    name: ${APP_NAME:unnamed}
	  datasource:
	    url: jdbc:hsqldb:file:testdb
	    username: sa
	    password:
	    dirver-class-name: org.hsqldb.jdbc.JDBCDriver
	    hikari:
	      auto-commit: false
	      connection-timeout: 3000
	      validation-timeout: 3000
	      max-lifetime: 60000
	      maximum-pool-size: 20
	      minimum-idle: 1
 * 可见，YAML是一种层级格式，它和.properties很容易互相转换，它的优点是去掉了大量重复的前缀，并且更加易读。
 * 也可以使用application.properties作为配置文件，但不如YAML格式简单。 
 * 使用环境变量	      
 * 在配置文件中，我们经常使用如下的格式对某个key进行配置：
 * app:
	  db:
	    host: ${DB_HOST:localhost}
	    user: ${DB_USER:root}
	    password: ${DB_PASSWORD:password}
 * 这种${DB_HOST:localhost}意思是，首先从环境变量查找DB_HOST，如果环境变量定义了，那么使用环境变量的值，否则，使用默认值localhost。
 * 这使得我们在开发和部署时更加方便，因为开发时无需设定任何环境变量，直接使用默认值即本地数据库，而实际线上运行的时候，只需要传入环境变量即可：
 * $ DB_HOST=10.0.1.123 DB_USER=prod DB_PASSWORD=xxxx java -jar xxx.jar
 * logback-spring.xml
 * 这是Spring Boot的logback配置文件名称（也可以使用logback.xml），一个标准的写法如下：
 * 它主要通过<include resource="..." />引入了Spring Boot的一个缺省配置，这样我们就可以引用类似${CONSOLE_LOG_PATTERN}这样的变量。上述配置定义了一个控制台输出和文件输出，可根据需要修改。
 * static是静态文件目录，templates是模板文件目录，注意它们不再存放在src/main/webapp下，而是直接放到src/main/resources这个classpath目录，因为在Spring Boot中已经不需要专门的webapp目录了。
 * 以上就是Spring Boot的标准目录结构，它完全是一个基于Java应用的普通Maven项目。
 * 我们再来看源码目录结构：
 * src/main/java
	└── com
	    └── itranswarp
	        └── learnjava
	            ├── Application.java
	            ├── entity
	            │   └── User.java
	            ├── service
	            │   └── UserService.java
	            └── web
	                └── UserController.java
 * 在存放源码的src/main/java目录中，Spring Boot对Java包的层级结构有一个要求。注意到我们的根package是com.itranswarp.learnjava，
 * 下面还有entity、service、web等子package。Spring Boot要求main()方法所在的启动类必须放到根package下，命名不做要求，这里我们以Application.java命名，它的内容如下：	               
 * 启动Spring Boot应用程序只需要一行代码加上一个注解@SpringBootApplication，该注解实际上又包含了：
 * @SpringBootConfiguration 
 * 	@Configuration
 * @EnableAutoConfiguration
 * 	@AutoConfigurationPackage
 * @ComponentScan
 * 这样一个注解就相当于启动了自动配置和自动扫描。
 * 使用Spring Boot时，强烈推荐从spring-boot-starter-parent继承，因为这样就可以引入Spring Boot的预置配置。
 * 紧接着，我们引入了依赖spring-boot-starter-web和spring-boot-starter-jdbc，它们分别引入了Spring MVC相关依赖和Spring JDBC相关依赖，无需指定版本号，
 * 因为引入的<parent>内已经指定了，只有我们自己引入的某些第三方jar包需要指定版本号。这里我们引入pebble-spring-boot-starter作为View，以及hsqldb作为嵌入式数据库。
 * hsqldb已在spring-boot-starter-jdbc中预置了版本号2.5.0，因此此处无需指定版本号。
 * 根据pebble-spring-boot-starter的文档(https://pebbletemplates.io/wiki/guide/spring-boot-integration/)，加入如下配置到application.yml：
 * 对Application稍作改动，添加WebMvcConfigurer这个Bean：
 * 现在就可以直接运行Application，启动后观察Spring Boot的日志：
 * Spring Boot自动启动了嵌入式Tomcat，当看到Started Application in xxx seconds时，Spring Boot应用启动成功。
 * 现在，我们在浏览器输入localhost:8080就可以直接访问页面。那么问题来了：
 * 前面我们定义的数据源、声明式事务、JdbcTemplate在哪创建的？怎么就可以直接注入到自己编写的UserService中呢？
 * 这些自动创建的Bean就是Spring Boot的特色：AutoConfiguration。
 * 当我们引入spring-boot-starter-jdbc时，启动时会自动扫描所有的XxxAutoConfiguration：
 * 1.DataSourceAutoConfiguration：自动创建一个DataSource，其中配置项从application.yml的spring.datasource读取；
 * 2.DataSourceTransactionManagerAutoConfiguration：自动创建了一个基于JDBC的事务管理器；
 * 3.JdbcTemplateAutoConfiguration：自动创建了一个JdbcTemplate。
 * 因此，我们自动得到了一个DataSource、一个DataSourceTransactionManager和一个JdbcTemplate。
 * 类似的，当我们引入spring-boot-starter-web时，自动创建了：
 * 1.ServletWebServerFactoryAutoConfiguration：自动创建一个嵌入式Web服务器，默认是Tomcat；
 * 2.DispatcherServletAutoConfiguration：自动创建一个DispatcherServlet；
 * 3.HttpEncodingAutoConfiguration：自动创建一个CharacterEncodingFilter；
 * 4.WebMvcAutoConfiguration：自动创建若干与MVC相关的Bean。
 * 引入第三方pebble-spring-boot-starter时，自动创建了：
 * 1.PebbleAutoConfiguration：自动创建了一个PebbleViewResolver。
 * Spring Boot大量使用XxxAutoConfiguration来使得许多组件被自动化配置并创建，而这些创建过程又大量使用了Spring的Conditional功能。例如，我们观察JdbcTemplateAutoConfiguration，它的代码如下：
 * @Configuration(proxyBeanMethods = false)
	@ConditionalOnClass({ DataSource.class, JdbcTemplate.class })
	@ConditionalOnSingleCandidate(DataSource.class)
	@AutoConfigureAfter(DataSourceAutoConfiguration.class)
	@EnableConfigurationProperties(JdbcProperties.class)
	@Import({ JdbcTemplateConfiguration.class, NamedParameterJdbcTemplateConfiguration.class })
	public class JdbcTemplateAutoConfiguration {
	}
 * 当满足条件：
 * @ConditionalOnClass：在classpath中能找到DataSource和JdbcTemplate；
 * @ConditionalOnSingleCandidate(DataSource.class)：在当前Bean的定义中能找到唯一的DataSource；
 * 该JdbcTemplateAutoConfiguration就会起作用。实际创建由导入的JdbcTemplateConfiguration完成：
 * @Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingBean(JdbcOperations.class)
	class JdbcTemplateConfiguration {
	    @Bean
	    @Primary
	    JdbcTemplate jdbcTemplate(DataSource dataSource, JdbcProperties properties) {
	        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	        JdbcProperties.Template template = properties.getTemplate();
	        jdbcTemplate.setFetchSize(template.getFetchSize());
	        jdbcTemplate.setMaxRows(template.getMaxRows());
	        if (template.getQueryTimeout() != null) {
	            jdbcTemplate.setQueryTimeout((int) template.getQueryTimeout().getSeconds());
	        }
	        return jdbcTemplate;
	    }
	}
 * 创建JdbcTemplate之前，要满足@ConditionalOnMissingBean(JdbcOperations.class)，即不存在JdbcOperations的Bean。
 * 如果我们自己创建了一个JdbcTemplate，例如，在Application中自己写个方法：
 * @SpringBootApplication
	public class Application {
	    ...
	    @Bean
	    JdbcTemplate createJdbcTemplate(@Autowired DataSource dataSource) {
	        return new JdbcTemplate(dataSource);
	    }
	}
 * 那么根据条件@ConditionalOnMissingBean(JdbcOperations.class)，Spring Boot就不会再创建一个重复的JdbcTemplate（因为JdbcOperations是JdbcTemplate的父类）。
 * 可见，Spring Boot自动装配功能是通过自动扫描+条件装配实现的，这一套机制在默认情况下工作得很好，但是，如果我们要手动控制某个Bean的创建，就需要详细地了解Spring Boot自动创建的原理，很多时候还要跟踪XxxAutoConfiguration，以便设定条件使得某个Bean不会被自动创建。
 * 小结
 * Spring Boot是一个基于Spring提供了开箱即用的一组套件，它可以让我们基于很少的配置和代码快速搭建出一个完整的应用程序。
 * Spring Boot有非常强大的AutoConfiguration功能，它是通过自动扫描+条件装配实现的。
 * 薄荷糖与红茶 ：
	1.目前廖老师的项目是每一节都创建一个hsqldb。
	其实可以创建一个之后复用。
	把hsqldb-2.5.1.jar加到图形化工具比如squirrel的classpath 建一个driver，然后URL 写比如 jdbc:hsqldb:file:d:\testdb 可以连接并且查询
	但是我没有试过怎么用命令行建起这个DB。我是在起了项目之后的那个test重用的。命令行肯定有不过我没有兴趣尝试了。
	有点意外的是java -classpath d:\hsqldb-2.5.1.jar org.hsqldb.util.DatabaseManager 这个启动图形工具它居然还有若干种driver 不止hsqldb这种
	2.在springboot里面仅仅加了application.java之后 可以启动。然后浏览器打开http://localhost:8080 可以看到dispatcherSevelet启动，然后抛404错误（因为现在没有任何component可以处理被sevelet丢过来的request）
	3.springboot真是开箱即用，好用。。。
 * 使用application.yml配置文件需要注意的地方：冒号后面要有空格，如果不加空格会导致yml配置读取失败
 * SNH48-刘慈欣 
 * PebbleTemplates也有能将自己集成进Spring Boot的spring-boot-starter包
 * 参见Pebble Templates 官方文档(https://pebbletemplates.io/wiki/guide/spring-boot-integration/)
 * 第一步，我们将POM中对pebbleTemplates的导入
 * <dependency>
		<groupId>io.pebbletemplates</groupId>
		<artifactId>pebble-spring5</artifactId>
		<version>${pebble.version}</version>
	</dependency>
	改为
	<dependency>
		<groupId>io.pebbletemplates</groupId>
		<artifactId>pebble-spring-boot-starter</artifactId>
		<version>${pebble.version}</version>
	</dependency>
 * // PebbleTemplates最新版已是3.1.4，我们也可以将properties中pebble.version的值改为3.1.4。
 * 廖雪峰：改成starter，更简单
 * eclipse报这个错误,重启一下就好了
 * The declared package "com.slef.learnjava" does not match the expected package ""
 * 
 * 使用开发者工具
 * 在开发阶段，我们经常要修改代码，然后重启Spring Boot应用。经常手动停止再启动，比较麻烦。
 * Spring Boot提供了一个开发者工具，可以监控classpath路径上的文件。只要源码或配置文件发生修改，Spring Boot应用可以自动重启。在开发阶段，这个功能比较有用。
 * 要使用这一开发者功能，我们只需添加如下依赖到pom.xml：
 * <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
</dependency>
 * 然后，没有然后了。直接启动应用程序，然后试着修改源码，保存，观察日志输出，Spring Boot会自动重新加载。
 * 默认配置下，针对/static、/public和/templates目录中的文件修改，不会自动重启，因为禁用缓存后，这些文件的修改可以实时更新。
 * 小结
 * Spring Boot提供了一个开发阶段非常有用的spring-boot-devtools，能自动检测classpath路径上文件修改并自动重启。
 * 
 * IDEA想要触发运行中修改自动重启，需要Ctrl + F9在运行中手动构建项目
 * 或者对IDEA配置作一些更改，参见https://stackoverflow.com/questions/53569745/spring-boot-developer-tools-auto-restart-doesnt-work-in-intellij
 * 推荐热部署插件jRebel
 * 打包Spring Boot应用
 * 我们在Maven的使用插件一节中介绍了如何使用maven-shade-plugin打包一个可执行的jar包。在Spring Boot应用中，打包更加简单，因为Spring Boot自带一个更简单的spring-boot-maven-plugin插件用来打包，我们只需要在pom.xml中加入以下配置：
 * <project ...>
    ...
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
 * 无需任何配置，Spring Boot的这款插件会自动定位应用程序的入口Class，我们执行以下Maven命令即可打包：
 * mvn clean package
 * 以springboot-exec-jar项目为例，打包后我们在target目录下可以看到两个jar文件：
 * 其中，springboot-exec-jar-1.0-SNAPSHOT.jar.original是Maven标准打包插件打的jar包，它只包含我们自己的Class，不包含依赖，而springboot-exec-jar-1.0-SNAPSHOT.jar是Spring Boot打包插件创建的包含依赖的jar，可以直接运行
 * java -jar springboot-exec-jar-1.0-SNAPSHOT.jar
 * 这样，部署一个Spring Boot应用就非常简单，无需预装任何服务器，只需要上传jar包即可。
 * 在打包的时候，因为打包后的Spring Boot应用不会被修改，因此，默认情况下，spring-boot-devtools这个依赖不会被打包进去。但是要注意，使用早期的Spring Boot版本时，需要配置一下才能排除spring-boot-devtools这个依赖：
 * <plugin>
	    <groupId>org.springframework.boot</groupId>
	    <artifactId>spring-boot-maven-plugin</artifactId>
	    <configuration>
	        <excludeDevtools>true</excludeDevtools>
	    </configuration>
	</plugin>
 * 如果不喜欢默认的项目名+版本号作为文件名，可以加一个配置指定文件名：
 * <project ...>
	    ...
	    <build>
	        <finalName>awesome-app</finalName>
	        ...
	    </build>
	</project>
 * 这样打包后的文件名就是awesome-app.jar。
 * 小结
 * Spring Boot提供了一个Maven插件用于打包所有依赖到单一jar文件，此插件十分易用，无需配置。
 * 
 * 使用Actuator
 * 在生产环境中，需要对应用程序的状态进行监控。前面我们已经介绍了使用JMX对Java应用程序包括JVM进行监控，使用JMX需要把一些监控信息以MBean的形式暴露给JMX Server，而Spring Boot已经内置了一个监控功能，它叫Actuator。
 * 使用Actuator非常简单，只需添加如下依赖：
 * <dependency>
	    <groupId>org.springframework.boot</groupId>
	    <artifactId>spring-boot-starter-actuator</artifactId>
	</dependency>
 * 然后正常启动应用程序，Actuator会把它能收集到的所有信息都暴露给JMX。此外，Actuator还可以通过URL/actuator/挂载一些监控点，例如，输入http://localhost:8080/actuator/health，我们可以查看应用程序当前状态：
 * {
    "status": "UP"
}
 * 许多网关作为反向代理需要一个URL来探测后端集群应用是否存活，这个URL就可以提供给网关使用。
 * Actuator默认把所有访问点暴露给JMX，但处于安全原因，只有health和info会暴露给Web。Actuator提供的所有访问点均在官方文档列出，要暴露更多的访问点给Web，需要在application.yml中加上配置：
 * management:
  endpoints:
    web:
      exposure:
        include: info, health, beans, env, metrics
 * 要特别注意暴露的URL的安全性，例如，/actuator/env可以获取当前机器的所有环境变量，不可暴露给公网。
 * 小结
 * Spring Boot提供了一个Actuator，可以方便地实现监控，并可通过Web访问特定类型的监控。  
 * 使用Profiles
 * Profile本身是Spring提供的功能，我们在使用条件装配中已经讲到了，Profile表示一个环境的概念，如开发、测试和生产这3个环境：
 * native,test,production
 * 或者按git分支定义master、dev这些环境：master,dev
 * 在启动一个Spring应用程序的时候，可以传入一个或多个环境，例如：
 * -Dspring.profiles.active=test,master
 * 大多数情况下，使用一个环境就足够了。
 * Spring Boot对Profiles的支持在于，可以在application.yml中为每个环境进行配置。下面是一个示例配置：
 * 注意到分隔符---，最前面的配置是默认配置，不需要指定Profile，后面的每段配置都必须以spring.profiles: xxx开头，表示一个Profile。上述配置默认使用8080端口，但是在test环境下，使用8000端口，在production环境下，使用80端口，并且启用Pebble的缓存。
 * 如果我们不指定任何Profile，直接启动应用程序，那么Profile实际上就是default，可以从Spring Boot启动日志看出：
 * 要以test环境启动，可输入如下命令：
 * java -Dspring.profiles.active=test -jar springboot-profiles-1.0-SNAPSHOT.jar
 * 从日志看到活动的Profile是test，Tomcat的监听端口是8000。
 * 通过Profile可以实现一套代码在不同环境启用不同的配置和功能。假设我们需要一个存储服务，在本地开发时，直接使用文件存储即可，但是，在测试和生产环境，需要存储到云端如S3上，如何通过Profile实现该功能？
 * 首先，我们要定义存储接口StorageService：
 * 
 * 使用Conditional
 * 使用Profile能根据不同的Profile进行条件装配，但是Profile控制比较糙，如果想要精细控制，例如，配置本地存储，AWS存储和阿里云存储，将来很可能会增加Azure存储等，用Profile就很难实现。
 * Spring本身提供了条件装配@Conditional，但是要自己编写比较复杂的Condition来做判断，比较麻烦。Spring Boot则为我们准备好了几个非常有用的条件：
 * 1.@ConditionalOnProperty：如果有指定的配置，条件生效；
 * 2.@ConditionalOnBean：如果有指定的Bean，条件生效；
 * 3.@ConditionalOnMissingBean：如果没有指定的Bean，条件生效；
 * 4.@ConditionalOnMissingClass：如果没有指定的Class，条件生效；
 * 5.@ConditionalOnWebApplication：在Web环境中条件生效；
 * 6.@ConditionalOnExpression：根据表达式判断条件是否生效。
 * 我们以最常用的@ConditionalOnProperty为例，把上一节的StorageService改写如下。首先，定义配置storage.type=xxx，用来判断条件，默认为local：
 * storage:
  	type: ${STORAGE_TYPE:local}
 * 设定为local时，启用LocalStorageService：
 * @Component
@ConditionalOnProperty(value = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {
    ...
}
 * 加载配置文件
 * 加载配置文件可以直接使用注解@Value，例如，我们定义了一个最大允许上传的文件大小配置：
 * storage:
	  local:
	    max-size: 102400
 * 在某个FileUploader里，需要获取该配置，可使用@Value注入：
 * @Component
	public class FileUploader {
	    @Value("{storage.local.max-size:102400}")
	    int maxSize;
	
	    ...
	}
 * 在另一个UploadFilter中，因为要检查文件的MD5，同时也要检查输入流的大小，因此，也需要该配置：
 * @Component
	public class UploadFilter implements Filter {
	    @Value("{storage.local.max-size:100000}")
	    int maxSize;
	
	    ...
	}
 * 多次引用同一个@Value不但麻烦，而且@Value使用字符串，缺少编译器检查，容易造成多处引用不一致（例如，UploadFilter把缺省值误写为100000）。
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
 * 可以首先定义一个Java Bean，持有该组配置：
 * 保证Java Bean的属性名称与配置一致即可。然后，我们添加两个注解：
 * @Configuration
	@ConfigurationProperties("storage.local")
	public class StorageConfiguration {
	    ...
	}
 * 注意到@ConfigurationProperties("storage.local")表示将从配置项storage.local读取该项的所有子项配置，并且，@Configuration表示StorageConfiguration也是一个Spring管理的Bean，可直接注入到其他Bean中：
 * 这样一来，引入storage.local的相关配置就很容易了，因为只需要注入StorageConfiguration这个Bean，这样可以由编译器检查类型，无需编写重复的@Value注解。
 * 小结
 * Spring Boot提供了@ConfigurationProperties注解，可以非常方便地把一段配置加载到一个Bean中。
 * 禁用自动配置
 * Spring Boot大量使用自动配置和默认配置，极大地减少了代码，通常只需要加上几个注解，并按照默认规则设定一下必要的配置即可。例如，配置JDBC，默认情况下，只需要配置一个spring.datasource：
 * spring:
	  datasource:
	    url: jdbc:hsqldb:file:testdb
	    username: sa
	    password:
	    dirver-class-name: org.hsqldb.jdbc.JDBCDriver
 * Spring Boot就会自动创建出DataSource、JdbcTemplate、DataSourceTransactionManager，非常方便。
 * 但是，有时候，我们又必须要禁用某些自动配置。例如，系统有主从两个数据库，而Spring Boot的自动配置只能配一个，怎么办？
 * 这个时候，针对DataSource相关的自动配置，就必须关掉。我们需要用exclude指定需要关掉的自动配置：
 * // 启动自动配置，但排除指定的自动配置:
 * @EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
 * 现在，Spring Boot不再给我们自动创建DataSource、JdbcTemplate和DataSourceTransactionManager了，要实现主从数据库支持，怎么办？
 * 让我们一步一步开始编写支持主从数据库的功能。首先，我们需要把主从数据库配置写到application.yml中，仍然按照Spring Boot默认的格式写，但datasource改为datasource-master和datasource-slave：
 * spring:
	  datasource-master:
	    url: jdbc:hsqldb:file:testdb
	    username: sa
	    password:
	    dirver-class-name: org.hsqldb.jdbc.JDBCDriver
	  datasource-slave:
	    url: jdbc:hsqldb:file:testdb
	    username: sa
	    password:
	    dirver-class-name: org.hsqldb.jdbc.JDBCDriver
 * 注意到两个数据库实际上是同一个库。如果使用MySQL，可以创建一个只读用户，作为datasource-slave的用户来模拟一个从库。
 * 下一步，我们分别创建两个HikariCP的DataSource：
 * 小结
 * 可以通过@EnableAutoConfiguration(exclude = {...})指定禁用的自动配置；
 * 可以通过@Import({...})导入自定义配置。
 * 
 * 添加Filter
 * 我们在Spring中已经学过了集成Filter，本质上就是通过代理，把Spring管理的Bean注册到Servlet容器中，不过步骤比较繁琐，需要配置web.xml。
 * 在Spring Boot中，添加一个Filter更简单了，可以做到零配置。我们来看看在Spring Boot中如何添加Filter。
 * Spring Boot会自动扫描所有的FilterRegistrationBean类型的Bean，然后，将它们返回的Filter自动注册到Servlet容器中，无需任何配置。
 * 我们还是以AuthFilter为例，首先编写一个AuthFilterRegistrationBean，它继承自FilterRegistrationBean：
 * @Order(10)
	@Component
	public class AuthFilterRegistrationBean extends FilterRegistrationBean<Filter> {
	    @Autowired
	    UserService userService;
	
	    @Override
	    public Filter getFilter() {
	        return new AuthFilter();
	    }
	
	    class AuthFilter implements Filter {
	        ...
	    }
	}
 * FilterRegistrationBean本身不是Filter，它实际上是Filter的工厂。Spring Boot会调用getFilter()，把返回的Filter注册到Servlet容器中。
 * 因为我们可以在FilterRegistrationBean中注入需要的资源，然后，在返回的AuthFilter中，这个内部类可以引用外部类的所有字段，自然也包括注入的UserService，
 * 所以，整个过程完全基于Spring的IoC容器完成。
 * 再注意到AuthFilterRegistrationBean标记了一个@Order(10)，因为Spring Boot支持给多个Filter排序，数字小的在前面，所以，多个Filter的顺序是可以固定的。
 * 我们再编写一个ApiFilter，专门过滤/api/*这样的URL。首先编写一个ApiFilterRegistrationBean
 * @Order(20)
	@Component
	public class ApiFilterRegistrationBean extends FilterRegistrationBean<Filter> {
	    @PostConstruct
	    public void init() {
	        setFilter(new ApiFilter());
	        setUrlPatterns(List.of("/api/*"));
	    }
	
	    class ApiFilter implements Filter {
	        ...
	    }
	}
 * 这个ApiFilterRegistrationBean和AuthFilterRegistrationBean又有所不同。因为我们要过滤URL，而不是针对所有URL生效，因此，在@PostConstruct方法中，通过setFilter()设置一个Filter实例后，再调用setUrlPatterns()传入要过滤的URL列表。
 * 小结
 * 在Spring Boot中添加Filter更加方便，并且支持对多个Filter进行排序。
 */
@SpringBootApplication
//启动自动配置，但排除指定的自动配置:
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@Import({MasterDataSourceConfiguration.class, SlaveDataSourceConfiguration.class, RoutingDataSourceConfiguration.class
	,RedisConfiguration.class})
public class Application 
{
    public static void main( String[] args )
    {
    	/*
    	 * Whitelabel Error Page
    	 * This application has no explicit mapping for /error, so you are seeing this as a fallback.
		 * Tue Nov 10 08:46:41 CST 2020
		 * There was an unexpected error (type=Not Found, status=404).
		 * 启动成功之后访问http://localhost:8080/,会报上面的404错误,这是正常的,是因为我们还没写处理请求的代码
    	 */
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    WebMvcConfigurer createWebMvcConfigurer(@Autowired HandlerInterceptor[] interceptors) {
    	return new WebMvcConfigurer() {
    		@Override
    		public void addResourceHandlers(ResourceHandlerRegistry registry){
    			//映射路径：/static/到classpath路径
    			registry.addResourceHandler("/static/**")
    			.addResourceLocations("classpath:/static/");
    		}
    	};
    }
    
    /*
     * 创建JdbcTemplate之前，要满足@ConditionalOnMissingBean(JdbcOperations.class)，即不存在JdbcOperations的Bean。
     * 如果我们自己创建了一个JdbcTemplate，例如，在Application中自己写个方法：
     * 那么根据条件@ConditionalOnMissingBean(JdbcOperations.class)，Spring Boot就不会再创建一个重复的JdbcTemplate（因为JdbcOperations是JdbcTemplate的父类）。
     */
    @Bean
    JdbcTemplate createJdbcTemplate(@Autowired DataSource dataSource) {
    	return new JdbcTemplate(dataSource);
    }
    
    @Bean
    MessageConverter createMessageConverter() {
    	return new Jackson2JsonMessageConverter();
    }
}
