package com.self.learnjava.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.self.learnjava.config.RoutingWithSlave;
import com.self.learnjava.entity.User;
import com.self.learnjava.service.MessaginService;
import com.self.learnjava.service.RedisService;
import com.self.learnjava.service.StorageService;
import com.self.learnjava.service.UserService;
import com.self.learnjava.service.ValueService;

@Controller
public class UserController {
	public static final String KEY_USER = "__user__";
	
	public static final String KEY_USERS = "__users__";
	
	public static final String KEY_USER_ID = "__userid__";
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	UserService userService;
	
	@Autowired
	StorageService storageService;
	
	@Autowired
	ValueService valueService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	MessaginService messaginService;
	
	@ExceptionHandler(RuntimeException.class)
	public ModelAndView handleUnknowException(Exception ex) {
		Map<String, String> info = new HashMap<String, String>();
		info.put("error", ex.getClass().getSimpleName());
		info.put("message", ex.getMessage());
		return new ModelAndView("500.html", info);
	}
	
    // 把User写入Redis:
	private void putUserInToRedis(User user) throws JsonProcessingException {
		redisService.hset(KEY_USERS, user.getId().toString(), objectMapper.writeValueAsString(user));
	}
	
    // 从Redis读取User:
	private User getUserFromRedis(HttpSession session) throws JsonMappingException, JsonProcessingException {
		Long id = (Long)session.getAttribute(KEY_USER_ID);
		if (id != null) {
			String s = redisService.hget(KEY_USERS, id.toString());
			if (s != null) {
				return objectMapper.readValue(s, User.class);
			}
		}
		logger.info("get User from redis" + id);
		return null;
	}
	
	@GetMapping("/")
	public ModelAndView index(HttpSession session) {
		logger.info("Spring Boot提供了一个开发阶段非常有用的spring-boot-devtools，能自动检测classpath路径上文件修改并自动重启。");
		User user = (User)session.getAttribute(KEY_USER);
		Map<String, Object> model = new HashMap<>();
		if (user != null) {
			model.put("user", model);
		}
		return new ModelAndView("index.html", model);
	}
	
	@GetMapping("/register")
	public ModelAndView register() {
		return new ModelAndView("register.html");
	}
	
	//用户登录成功后，把ID放入Session，把User实例放入Redis：
	@PostMapping("/register")
	public ModelAndView doRegister(@RequestParam("email")String email, @RequestParam("password")String password,
			@RequestParam("name")String name) {
		try {
			User user = userService.register(email, password, name);
			logger.info("user registered: {}", user.getEmail());
			messaginService.sendMailMessage(MailMessage.registration(user.getEmail(), user.getName()));
		} catch (Exception e) {
			e.printStackTrace();
			Map<String, String> info = new HashMap<String, String>();
			info.put("email", email);
			info.put("error", "Register failed");
			return new ModelAndView("register.html", info);
		}
		return new ModelAndView("redirect:/signin");
	}
	
	@GetMapping("/signin")
	public ModelAndView signin(HttpSession session) {
		User user = (User)session.getAttribute(KEY_USER);
		if (user != null) {
			return new ModelAndView("redirect:profile");
		}
		return new ModelAndView("signin.html");
	}
	
	@PostMapping("/signin")
	public ModelAndView doSignin(@RequestParam("email")String email, @RequestParam("password")String password, HttpSession session) throws JsonProcessingException {
		try {
			User user = userService.signin(email, password);
			session.setAttribute(KEY_USER, user);
			putUserInToRedis(user);
		} catch (Exception e) {
			e.printStackTrace();
			Map<String, String> info = new HashMap<>();
			info.put("email", email);
			info.put("error", "Signin failed");
			return new ModelAndView("signin.html", info);
		}
		return new ModelAndView("redirect:/profile");
	}
	
	/*
	 * @RoutingWithSlave//<-- 指示在此方法中使用slave数据库
	 * 实现上述功能需要编写一个@RoutingWithSlave注解，一个AOP织入和一个ThreadLocal来保存key。由于代码比较简单，这里我们不再详述。
	 * 如果我们想要确认是否真的切换了DataSource，可以覆写determineTargetDataSource()方法并打印出DataSource的名称：
	 */
	@GetMapping("/profile")
	@RoutingWithSlave//<-- 指示在此方法中使用slave数据库
	public ModelAndView profile(HttpSession session) throws JsonMappingException, JsonProcessingException {
		User user = (User)session.getAttribute(KEY_USER);
		User userRedis = getUserFromRedis(session);//需要获取User时，从Redis取出：
		if (userRedis != null) {
			logger.info("get User from redis" + userRedis);
		}
		if (user == null) {
			return new ModelAndView("redirect:/signin");
		}
		Map<String, Object> info = new HashMap<>();
		user = userService.getUserByEmail(user.getEmail());
		info.put("user", user);
		return new ModelAndView("profile.html", info);
	}
	
	@GetMapping("/signout")
	public String signout(HttpSession session) {
		session.removeAttribute(KEY_USER);
		return "redirect:/signin";
	}
	
	@GetMapping("/resetPassword")
	public ModelAndView resetPassword() {
		throw new UnsupportedOperationException("Not supported yet!");
	}
}
