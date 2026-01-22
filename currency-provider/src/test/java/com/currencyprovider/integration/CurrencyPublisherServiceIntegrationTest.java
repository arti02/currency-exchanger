package com.currencyprovider.integration;

import com.currencyprovider.config.AppConfig;
import com.currencyprovider.configuration.TestcontainersConfiguration;
import com.currencyprovider.helper.CurrencyTestHelper;
import com.currencyprovider.model.NbpRateDTO;
import com.currencyprovider.service.CurrencyPublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.common.Json.getObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class CurrencyPublisherServiceIntegrationTest {

	@Autowired
	private CurrencyPublisherService currencyPublisherService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private AppConfig appConfig;


	@BeforeEach
	void setUp() {
		rabbitTemplate.execute(channel -> {
			channel.queuePurge(appConfig.getQueryName());
			return null;
		});
	}

	@Test
	void publish_shouldSendMessageToRabbitMQ() throws Exception {
		// given
		NbpRateDTO nbpRateDTO = CurrencyTestHelper.createMockRates().get(0); // USD

		// when
		currencyPublisherService.publish(nbpRateDTO);

		// then
		TimeUnit.MILLISECONDS.sleep(500);

		Message message = rabbitTemplate.receive(appConfig.getQueryName(), 5000);
		assertThat(message).isNotNull();

		NbpRateDTO receivedRate = getObjectMapper().readValue(message.getBody(), NbpRateDTO.class);
		assertThat(receivedRate.getCode()).isEqualTo("USD");
		assertThat(receivedRate.getCurrency()).isEqualTo("dolar amerykański");
		assertThat(receivedRate.getBid()).isNotNull();
		assertThat(receivedRate.getAsk()).isNotNull();
	}

	@Test
	void publish_shouldPublishMultipleRates() throws Exception {
		// given
		NbpRateDTO usdRate = CurrencyTestHelper.createMockRates().get(0);
		NbpRateDTO eurRate = CurrencyTestHelper.createMockRates().get(1);
		NbpRateDTO gbpRate = CurrencyTestHelper.createMockRates().get(2);

		// when
		currencyPublisherService.publish(usdRate);
		currencyPublisherService.publish(eurRate);
		currencyPublisherService.publish(gbpRate);

		// then
		TimeUnit.MILLISECONDS.sleep(500);

		Message message1 = rabbitTemplate.receive(appConfig.getQueryName(), 5000);
		assertThat(message1).isNotNull();
		NbpRateDTO rate1 = getObjectMapper().readValue(message1.getBody(), NbpRateDTO.class);
		assertThat(rate1.getCode()).isIn("USD", "EUR", "GBP");

		Message message2 = rabbitTemplate.receive(appConfig.getQueryName(), 5000);
		assertThat(message2).isNotNull();
		NbpRateDTO rate2 = getObjectMapper().readValue(message2.getBody(), NbpRateDTO.class);
		assertThat(rate2.getCode()).isIn("USD", "EUR", "GBP");

		Message message3 = rabbitTemplate.receive(appConfig.getQueryName(), 5000);
		assertThat(message3).isNotNull();
		NbpRateDTO rate3 = getObjectMapper().readValue(message3.getBody(), NbpRateDTO.class);
		assertThat(rate3.getCode()).isIn("USD", "EUR", "GBP");

		// Verify all rates are different
		assertThat(rate1.getCode()).isNotEqualTo(rate2.getCode());
		assertThat(rate1.getCode()).isNotEqualTo(rate3.getCode());
		assertThat(rate2.getCode()).isNotEqualTo(rate3.getCode());
	}

}
