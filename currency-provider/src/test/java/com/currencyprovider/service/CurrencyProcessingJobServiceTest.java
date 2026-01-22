package com.currencyprovider.service;

import com.currencyprovider.client.NbpApiClient;
import com.currencyprovider.helper.CurrencyTestHelper;
import com.currencyprovider.model.NbpApiResponse;
import com.currencyprovider.model.NbpRateDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyProcessingJobServiceTest {

	@InjectMocks
	private CurrencyProcessingJobService currencyProcessingJobService;

	@Mock
	private NbpApiClient nbpApiClient;

	@Mock
	private CurrencyPublisherService currencyPublisherService;

	@Captor
	private ArgumentCaptor<NbpRateDTO> nbpRateCaptor;

	@Test
	void fetchAndPublishCurrencyRates_shouldPublishAllRates() {
		// given
		NbpApiResponse mockResponse = CurrencyTestHelper.createMockNbpResponse();
		NbpApiResponse[] responses = new NbpApiResponse[]{mockResponse};

		when(nbpApiClient.getCurrencyRates()).thenReturn(responses);

		// when
		currencyProcessingJobService.fetchAndPublishCurrencyRates();

		// then
		verify(nbpApiClient).getCurrencyRates();
		verify(currencyPublisherService, times(3)).publish(nbpRateCaptor.capture());

		List<NbpRateDTO> publishedRates = nbpRateCaptor.getAllValues();
		assertThat(publishedRates).hasSize(3);

		// Verify USD
		NbpRateDTO usdRate = publishedRates.stream()
				.filter(r -> r.getCode().equals("USD"))
				.findFirst()
				.orElseThrow();
		assertThat(usdRate.getCurrency()).isEqualTo("dolar amerykański");

		// Verify EUR
		NbpRateDTO eurRate = publishedRates.stream()
				.filter(r -> r.getCode().equals("EUR"))
				.findFirst()
				.orElseThrow();
		assertThat(eurRate.getCurrency()).isEqualTo("euro");

		// Verify GBP
		NbpRateDTO gbpRate = publishedRates.stream()
				.filter(r -> r.getCode().equals("GBP"))
				.findFirst()
				.orElseThrow();
		assertThat(gbpRate.getCurrency()).isEqualTo("funt szterling");
	}

	@Test
	void fetchAndPublishCurrencyRates_shouldHandleEmptyResponse() {
		// given
		when(nbpApiClient.getCurrencyRates()).thenReturn(new NbpApiResponse[0]);

		// when
		currencyProcessingJobService.fetchAndPublishCurrencyRates();

		// then
		verify(nbpApiClient).getCurrencyRates();
		verify(currencyPublisherService, never()).publish(any(NbpRateDTO.class));
	}

	@Test
	void fetchAndPublishCurrencyRates_shouldHandleNullResponse() {
		// given
		when(nbpApiClient.getCurrencyRates()).thenReturn(null);

		// when
		currencyProcessingJobService.fetchAndPublishCurrencyRates();

		// then
		verify(nbpApiClient).getCurrencyRates();
		verify(currencyPublisherService, never()).publish(any(NbpRateDTO.class));
	}

	@Test
	void fetchAndPublishCurrencyRates_shouldHandleException() {
		// given
		when(nbpApiClient.getCurrencyRates()).thenThrow(new RuntimeException("API Error"));

		// when - should not throw exception
		currencyProcessingJobService.fetchAndPublishCurrencyRates();

		// then
		verify(nbpApiClient).getCurrencyRates();
		verify(currencyPublisherService, never()).publish(any(NbpRateDTO.class));
	}

	@Test
	void fetchAndPublishCurrencyRates_shouldPublishAllRatesFromFirstResponse() {
		// given
		NbpApiResponse mockResponse1 = CurrencyTestHelper.createMockNbpResponse();
		NbpApiResponse mockResponse2 = new NbpApiResponse();
		mockResponse2.setRates(List.of());
		NbpApiResponse[] responses = new NbpApiResponse[]{mockResponse1, mockResponse2};

		when(nbpApiClient.getCurrencyRates()).thenReturn(responses);

		// when
		currencyProcessingJobService.fetchAndPublishCurrencyRates();

		// then
		verify(nbpApiClient).getCurrencyRates();
		verify(currencyPublisherService, times(3)).publish(any(NbpRateDTO.class));
	}
}
