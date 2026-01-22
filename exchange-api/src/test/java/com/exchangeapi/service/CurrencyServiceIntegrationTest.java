package com.exchangeapi.service;

import com.exchangeapi.config.AppConfig;
import com.exchangeapi.config.BaseIntegrationTest;
import com.exchangeapi.model.dto.ExchangeResultDTO;
import com.exchangeapi.model.dto.command.ExchangeRequestCommand;
import com.exchangeapi.model.entity.CurrencyRate;
import com.exchangeapi.repository.CurrencyRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyServiceIntegrationTest extends BaseIntegrationTest {

	@Autowired
	private CurrencyService currencyService;

	@Autowired
	private CurrencyRateRepository currencyRateRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private AppConfig appConfig;

	@BeforeEach
	void setup() {
		currencyRateRepository.deleteAll();
		rabbitTemplate.execute(channel -> {
			channel.queuePurge(appConfig.getQueryName());
			return null;
		});
	}

	@Test
	void exchangeCurrency_shouldCalculateResult_andPublishMessage() {
		// given
		CurrencyRate usd = new CurrencyRate();
		usd.setCode("USD");
		usd.setBuyRate(new BigDecimal("4.10"));
		usd.setSellRate(new BigDecimal("4.20"));
		currencyRateRepository.save(usd);

		CurrencyRate eur = new CurrencyRate();
		eur.setCode("EUR");
		eur.setBuyRate(new BigDecimal("4.50"));
		eur.setSellRate(new BigDecimal("4.60"));
		currencyRateRepository.save(eur);

		ExchangeRequestCommand request = new ExchangeRequestCommand("USD", "EUR", new BigDecimal("100.00"));
		Jwt jwt = Jwt.withTokenValue("token")
				.header("alg", "none")
				.claim("email", "it@example.com")
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plusSeconds(3600))
				.build();

		// when
		ExchangeResultDTO result = currencyService.exchangeCurrency(request, jwt);

		// then - returned result
		assertThat(result.getFrom()).isEqualTo("USD");
		assertThat(result.getTo()).isEqualTo("EUR");
		assertThat(result.getAmount()).isEqualByComparingTo("100.00");
		assertThat(result.getRate()).isEqualByComparingTo("4.60");
		assertThat(result.getConvertedAmount()).isEqualByComparingTo("89.13"); // 100 * 4.10 / 4.60
		assertThat(result.getEmail()).isEqualTo("it@example.com");
		assertThat(result.getTimestamp()).isNotNull();

		// then - published message
		Object received = rabbitTemplate.receiveAndConvert(appConfig.getQueryName(), 5_000);
		assertThat(received).isNotNull();
		assertThat(received).isInstanceOf(ExchangeResultDTO.class);
		ExchangeResultDTO msg = (ExchangeResultDTO) received;
		assertThat(msg.getFrom()).isEqualTo("USD");
		assertThat(msg.getTo()).isEqualTo("EUR");
		assertThat(msg.getAmount()).isEqualByComparingTo("100.00");
		assertThat(msg.getRate()).isEqualByComparingTo("4.60");
		assertThat(msg.getConvertedAmount()).isEqualByComparingTo("89.13");
		assertThat(msg.getEmail()).isEqualTo("it@example.com");
		assertThat(msg.getTimestamp()).isNotNull();
	}
}
