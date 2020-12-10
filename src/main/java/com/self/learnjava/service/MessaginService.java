package com.self.learnjava.service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.self.learnjava.web.MailMessage;

@Component
public class MessaginService {
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	JmsTemplate jmsTemplate;
	
	/*
	 * 和Spring版本的JMS代码相比，使用Spring Boot集成JMS时，只要引入了spring-boot-starter-artemis，Spring Boot会自动创建JMS相关的ConnectionFactory、JmsListenerContainerFactory、JmsTemplate等，无需我们再手动配置了。
     * 发送消息时只需要引入JmsTemplate：
	 */
	public void sendMailMessage(MailMessage msg) throws Exception {
		System.out.println("发送jms/queue/mail");
		String text = objectMapper.writeValueAsString(msg);
		jmsTemplate.send("/jms/queue/mail", new MessageCreator(){
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(text);
			}
		});
	}
}
