package com.currencypersistenceservice.service;

import com.currencypersistenceservice.config.AppConfig;
import com.currencypersistenceservice.configuration.BaseIntegrationTest;
import com.currencypersistenceservice.model.CurrencyRate;
import com.currencypersistenceservice.model.NbpRateDTO;
import com.currencypersistenceservice.repository.CurrencyRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class CurrencyConsumerServiceIntegrationTest extends BaseIntegrationTest {

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private CurrencyRateRepository currencyRepository;

	@Autowired
	private CurrencyPersistenceService currencyPersistenceService;

	@BeforeEach
	void cleanup() {
		currencyRepository.deleteAll();
	}

	@Test
	void shouldConsumeMessageAndUpsertCurrencyRate() {
		// given
		currencyPersistenceService.save(new NbpRateDTO("Euro", "EUR", BigDecimal.valueOf(4.00), BigDecimal.valueOf(4.10)));
		assertThat(currencyRepository.count()).isEqualTo(1);

		// when
		NbpRateDTO incoming = new NbpRateDTO("Euro", "EUR", BigDecimal.valueOf(4.50), BigDecimal.valueOf(4.60));
		rabbitTemplate.convertAndSend(appConfig.getQueryName(), incoming);

		// then
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			Optional<CurrencyRate> maybe = currencyRepository.findById("EUR");
			assertThat(maybe).isPresent();
			CurrencyRate saved = maybe.get();
			assertThat(saved.getCode()).isEqualTo("EUR");
			assertThat(saved.getBuyRate()).isEqualByComparingTo("4.50");
			assertThat(saved.getSellRate()).isEqualByComparingTo("4.60");
		});

		assertThat(currencyRepository.count()).isEqualTo(1);
	}
}
