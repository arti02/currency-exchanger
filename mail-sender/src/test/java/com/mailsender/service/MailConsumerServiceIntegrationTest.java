package com.mailsender.service;

import com.mailsender.config.AppConfig;
import com.mailsender.config.BaseIntegrationTest;
import com.mailsender.model.dto.ExchangeResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MailConsumerServiceIntegrationTest extends BaseIntegrationTest {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private AppConfig appConfig;

	@MockitoBean
	private MailService mailService;

	@BeforeEach
	void ensureQueueExistsAndPurge() {
		rabbitTemplate.execute(channel -> {
			channel.queuePurge(appConfig.getQueryName());
			return null;
		});
	}

	@Test
	void shouldConsumeMessageAndCallMailService() {
		// given
		String email = "it-" + UUID.randomUUID() + "@example.com";
		ExchangeResultDTO dto = new ExchangeResultDTO(
				"USD",
				"PLN",
				new BigDecimal("100.00"),
				new BigDecimal("410.00"),
				new BigDecimal("4.10"),
				LocalDateTime.of(2026, 1, 22, 10, 0),
				email);

		// when
		rabbitTemplate.convertAndSend(appConfig.getQueryName(), dto);

		// then
		await().untilAsserted(() -> verify(mailService, times(1)).send(
				eq(email),
				eq("Currency Exchange Confirmation"),
				eq(MailBuilderService.buildConfirmationEmailBody(dto))));
	}
}
