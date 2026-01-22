package com.currencyprovider.integration;

import com.currencyprovider.client.NbpApiClient;
import com.currencyprovider.config.AppConfig;
import com.currencyprovider.configuration.TestcontainersConfiguration;
import com.currencyprovider.helper.CurrencyTestHelper;
import com.currencyprovider.model.NbpApiResponse;
import com.currencyprovider.model.NbpRateDTO;
import com.currencyprovider.service.CurrencyProcessingJobService;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.common.Json.getObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@WireMockTest(httpPort = 8089)
@Import(TestcontainersConfiguration.class)
class CurrencyProcessingJobServiceIntegrationTest {

	@Autowired
	private CurrencyProcessingJobService currencyProcessingJobService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private AppConfig appConfig;

	@MockitoBean
	private NbpApiClient nbpApiClient;

	@BeforeEach
	void setUp() {
		rabbitTemplate.execute(channel -> {
			channel.queuePurge(appConfig.getQueryName());
			return null;
		});
	}

	@Test
	void fetchAndPublishCurrencyRates_shouldPublishMessagesToRabbitMQ() throws Exception {
		// given
		NbpApiResponse mockResponse = CurrencyTestHelper.createMockNbpResponse();
		NbpApiResponse[] responses = new NbpApiResponse[] { mockResponse };

		when(nbpApiClient.getCurrencyRates()).thenReturn(responses);

		// when
		currencyProcessingJobService.fetchAndPublishCurrencyRates();

		// then
		TimeUnit.MILLISECONDS.sleep(500);

		Message message1 = rabbitTemplate.receive(appConfig.getQueryName(), 5000);
		assertThat(message1).isNotNull();

		NbpRateDTO rate1 = getObjectMapper().readValue(message1.getBody(), NbpRateDTO.class);
		assertThat(rate1.getCode()).isIn("USD", "EUR", "GBP");
		assertThat(rate1.getBid()).isNotNull();
		assertThat(rate1.getAsk()).isNotNull();
		assertThat(rate1.getCurrency()).isNotNull();

		Message message2 = rabbitTemplate.receive(appConfig.getQueryName(), 5000);
		assertThat(message2).isNotNull();

		NbpRateDTO rate2 = getObjectMapper().readValue(message2.getBody(), NbpRateDTO.class);
		assertThat(rate2.getCode()).isIn("USD", "EUR", "GBP");

		Message message3 = rabbitTemplate.receive(appConfig.getQueryName(), 5000);
		assertThat(message3).isNotNull();

		NbpRateDTO rate3 = getObjectMapper().readValue(message3.getBody(), NbpRateDTO.class);
		assertThat(rate3.getCode()).isIn("USD", "EUR", "GBP");

		assertThat(rate1.getCode()).isNotEqualTo(rate2.getCode());
		assertThat(rate1.getCode()).isNotEqualTo(rate3.getCode());
		assertThat(rate2.getCode()).isNotEqualTo(rate3.getCode());

		verify(nbpApiClient, times(1)).getCurrencyRates();
	}

	@Test
	void fetchAndPublishCurrencyRates_shouldHandleEmptyResponse() throws Exception {
		// given
		when(nbpApiClient.getCurrencyRates()).thenReturn(new NbpApiResponse[0]);

		// when
		currencyProcessingJobService.fetchAndPublishCurrencyRates();

		// then
		TimeUnit.MILLISECONDS.sleep(500);

		Message message = rabbitTemplate.receive(appConfig.getQueryName(), 1000);
		assertThat(message).isNull();

		verify(nbpApiClient, times(1)).getCurrencyRates();
	}

	@Test
	void fetchAndPublishCurrencyRates_shouldHandleNullResponse() throws Exception {
		// given
		when(nbpApiClient.getCurrencyRates()).thenReturn(null);

		// when
		currencyProcessingJobService.fetchAndPublishCurrencyRates();

		// then
		TimeUnit.MILLISECONDS.sleep(500);

		Message message = rabbitTemplate.receive(appConfig.getQueryName(), 1000);
		assertThat(message).isNull();

		verify(nbpApiClient, times(1)).getCurrencyRates();
	}

}
