package com.currencyprovider.service;

import com.currencyprovider.config.AppConfig;
import com.currencyprovider.helper.CurrencyTestHelper;
import com.currencyprovider.model.NbpRateDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyPublisherServiceTest {

	@InjectMocks
	private CurrencyPublisherService currencyPublisherService;

	@Mock
	private AppConfig appConfig;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@Captor
	private ArgumentCaptor<NbpRateDTO> nbpRateCaptor;

	@Captor
	private ArgumentCaptor<String> queueNameCaptor;

	@Test
	void publish_shouldSendMessageToQueue() {
		// given
		NbpRateDTO nbpRateDTO = CurrencyTestHelper.createMockRates().get(0);
	  when(appConfig.getQueryName()).thenReturn("currency.queue");

		// when
		currencyPublisherService.publish(nbpRateDTO);

		// then
		verify(rabbitTemplate).convertAndSend(queueNameCaptor.capture(), nbpRateCaptor.capture());

		// Verify queue name
		assertThat(queueNameCaptor.getValue()).isNotNull();

		// Verify published rate
		NbpRateDTO publishedRate = nbpRateCaptor.getValue();
		assertThat(publishedRate.getCode()).isEqualTo("USD");
		assertThat(publishedRate.getCurrency()).isEqualTo("dolar amerykański");
		assertThat(publishedRate.getBid()).isNotNull();
		assertThat(publishedRate.getAsk()).isNotNull();
	}
}
