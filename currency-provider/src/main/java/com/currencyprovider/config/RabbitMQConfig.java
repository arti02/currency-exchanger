package com.currencyprovider.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

	@Bean
	public MessageConverter jsonMessageConverter() {
		log.info("Creating JacksonJsonMessageConverter");
		return new JacksonJsonMessageConverter();
	}

	@Bean
	public Queue currencyQueue(AppConfig appConfig, ConnectionFactory connectionFactory) {
		RabbitAdmin admin = new RabbitAdmin(connectionFactory);
		Queue queue = new Queue(appConfig.getQueryName(), true);
		admin.declareQueue(queue);
		return queue;
	}
}