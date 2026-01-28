package com.mailsender.config;

import org.springframework.amqp.core.Queue;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	RabbitMQContainer rabbitMQContainer() {
		return new RabbitMQContainer(DockerImageName.parse("rabbitmq:3-management"));
	}

	@Bean
	Queue currencyQueue(AppConfig appConfig) {
		return new Queue(appConfig.getQueryName(), true);
	}


}
