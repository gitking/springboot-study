package com.self.learnjava.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.self.learnjava.entity.User;
import com.self.learnjava.service.UserService;

/*
 * 添加Filter
 * 我们在Spring中已经学过了集成Filter，本质上就是通过代理，把Spring管理的Bean注册到Servlet容器中，不过步骤比较繁琐，需要配置web.xml。
 * 在Spring Boot中，添加一个Filter更简单了，可以做到零配置。我们来看看在Spring Boot中如何添加Filter。
 * Spring Boot会自动扫描所有的FilterRegistrationBean类型的Bean，然后，将它们返回的Filter自动注册到Servlet容器中，无需任何配置。
 * 我们还是以AuthFilter为例，首先编写一个AuthFilterRegistrationBean，它继承自FilterRegistrationBean：
 * FilterRegistrationBean本身不是Filter，它实际上是Filter的工厂。Spring Boot会调用getFilter()，把返回的Filter注册到Servlet容器中。
 * 因为我们可以在FilterRegistrationBean中注入需要的资源，然后，在返回的AuthFilter中，这个内部类可以引用外部类的所有字段，自然也包括注入的UserService，所以，整个过程完全基于Spring的IoC容器完成。
 * 再注意到AuthFilterRegistrationBean标记了一个@Order(10)，因为Spring Boot支持给多个Filter排序，数字小的在前面，所以，多个Filter的顺序是可以固定的。
 * 我们再编写一个ApiFilter，专门过滤/api/*这样的URL。首先编写一个ApiFilterRegistrationBean
 */
@Order(10)
@Component
public class AuthFilterRegistrationBean extends FilterRegistrationBean<Filter>{
	
	@Autowired
	UserService userService;
	
	@Override
	public Filter getFilter() {
		/*
		 * 从FilterRegistrationBean的父类AbstractFilterRegistrationBean可以看到,如果不设置
		 * Filter的filter-mapping默认过滤/*
		 */
		return new AuthFilter();
	}
	
	class AuthFilter implements Filter {
		final Logger logger = LoggerFactory.getLogger(getClass());
		
		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			HttpServletRequest req = (HttpServletRequest)request;
			try {
				authenticateByHeader(req);
			} catch (RuntimeException e) {
				logger.warn("login by authorization header failed.", e);
			}
			chain.doFilter(request, response);
		}
		
		private void authenticateByHeader(HttpServletRequest req) throws UnsupportedEncodingException {
			String authHeader = req.getHeader("Authorization");
			if (authHeader != null && authHeader.startsWith("Basic ")) {
				logger.info("try authenticate by authorization header...");
				String up = new String(Base64.getDecoder().decode(authHeader.substring(6)), StandardCharsets.UTF_8);
				int pos = up.indexOf(':');
				if (pos > 0) {
					String email = URLDecoder.decode(up.substring(0, pos), "UTF-8");
					String password = URLDecoder.decode(up.substring(pos + 1), "UTF-8");
					User user = userService.signin(email, password);
					req.getSession().setAttribute(UserController.KEY_USER, user);
					logger.info("user {} login by authorization header ok.", email);
				}
			}
		}
	}
}
