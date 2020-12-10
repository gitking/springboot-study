package com.self.learnjava.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(20)
@Component
public class ApiFilterRegistrationBean extends FilterRegistrationBean<Filter>{
	
	@PostConstruct
	public void init(){
		setFilter(new ApiFilter());
		List<String> urlList = new ArrayList<>();
		urlList.add("/api/*");//过滤指定Url
		setUrlPatterns(urlList);
	}
	
	class ApiFilter implements Filter {
		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException{
			HttpServletResponse resp = (HttpServletResponse)response;
			resp.setHeader("X-Api-Version", "1.0");
			chain.doFilter(request, response);
		}
	}
}
