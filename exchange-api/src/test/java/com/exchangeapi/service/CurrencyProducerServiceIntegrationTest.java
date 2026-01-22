package com.exchangeapi.service;

import com.exchangeapi.config.AppConfig;
import com.exchangeapi.config.BaseIntegrationTest;
import com.exchangeapi.model.dto.ExchangeResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyProducerServiceIntegrationTest extends BaseIntegrationTest {

	@Autowired
	private CurrencyProducerService currencyProducerService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private AppConfig appConfig;

	@BeforeEach
	void purgeQueue() {
		rabbitTemplate.execute(channel -> {
			channel.queuePurge(appConfig.getQueryName());
			return null;
		});
	}

	@Test
	void publish_shouldSendMessageToConfiguredQueue() {
		// given
		ExchangeResultDTO dto = new ExchangeResultDTO(
				"USD",
				"PLN",
				new BigDecimal("100.00"),
				new BigDecimal("410.00"),
				new BigDecimal("4.10"),
				LocalDateTime.now(),
				"it-" + UUID.randomUUID() + "@example.com"
		);

		// when
		currencyProducerService.publish(dto);

		// then
		Object received = rabbitTemplate.receiveAndConvert(appConfig.getQueryName(), 5_000);
		assertThat(received).isNotNull();
		assertThat(received).isInstanceOf(ExchangeResultDTO.class);
		ExchangeResultDTO receivedDto = (ExchangeResultDTO) received;
		assertThat(receivedDto.getFrom()).isEqualTo("USD");
		assertThat(receivedDto.getTo()).isEqualTo("PLN");
		assertThat(receivedDto.getAmount()).isEqualByComparingTo("100.00");
		assertThat(receivedDto.getConvertedAmount()).isEqualByComparingTo("410.00");
		assertThat(receivedDto.getRate()).isEqualByComparingTo("4.10");
		assertThat(receivedDto.getEmail()).isEqualTo(dto.getEmail());
	}
}
