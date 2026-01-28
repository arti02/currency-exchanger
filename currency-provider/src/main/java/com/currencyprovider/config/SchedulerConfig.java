package com.currencyprovider.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "60m")
public class SchedulerConfig {

	@Bean
	public LockProvider lockProvider(MongoTemplate mongoTemplate, AppConfig appConfig) {
		return new MongoLockProvider(mongoTemplate.getCollection(appConfig.getShedlockDbName()));
	}

}