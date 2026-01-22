package com.exchangeapi.service;

import com.exchangeapi.exception.ApiException;
import com.exchangeapi.config.CurrencyTestHelper;
import com.exchangeapi.model.dto.ExchangeResultDTO;
import com.exchangeapi.model.entity.CurrencyRate;
import com.exchangeapi.repository.CurrencyRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

	@InjectMocks
	private CurrencyService classUnderTest;

	@Mock
	private CurrencyRateRepository currencyRateRepository;

	@Mock
	private CurrencyProducerService currencyProducerService;

	@Captor
	private ArgumentCaptor<ExchangeResultDTO> exchangeResultCaptor;

	@Test
	void getAllCurrencyRates_shouldReturnMappedDTOs() {
		// given
		CurrencyRate usd = CurrencyTestHelper.createUsdRate();
		CurrencyRate eur = CurrencyTestHelper.createEurRate();
		when(currencyRateRepository.findAll()).thenReturn(List.of(usd, eur));

		// when
		var result = classUnderTest.getAllCurrencyRates();

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getCode()).isEqualTo("USD");
		assertThat(result.get(0).getBuyRate()).isEqualTo(new BigDecimal("4.10"));
		assertThat(result.get(0).getSellRate()).isEqualTo(new BigDecimal("4.20"));

		verify(currencyRateRepository).findAll();
	}

	@Test
	void getAllCurrencyRates_shouldReturnEmptyListWhenNoCurrencies() {
		// given
		when(currencyRateRepository.findAll()).thenReturn(List.of());

		// when
		var result = classUnderTest.getAllCurrencyRates();

		// then
		assertThat(result).isEmpty();
		verify(currencyRateRepository).findAll();
	}

	@Test
	void getCurrencyByCode_shouldReturnCurrencyWhenFound() {
		// given
		CurrencyRate usd = CurrencyTestHelper.createUsdRate();
		when(currencyRateRepository.findByCode("USD")).thenReturn(Optional.of(usd));

		// when
		var result = classUnderTest.getCurrencyByCode("USD");

		// then
		assertThat(result.getCode()).isEqualTo("USD");
		assertThat(result.getBuyRate()).isEqualTo(new BigDecimal("4.10"));
		assertThat(result.getSellRate()).isEqualTo(new BigDecimal("4.20"));

		verify(currencyRateRepository).findByCode("USD");
	}

	@Test
	void getCurrencyByCode_shouldThrowWhenNotFound() {
		// given
		when(currencyRateRepository.findByCode("XXX")).thenReturn(Optional.empty());

		// when / then
		assertThatThrownBy(() -> classUnderTest.getCurrencyByCode("XXX"))
				.isInstanceOf(ApiException.class);

		verify(currencyRateRepository).findByCode("XXX");
	}

	@Test
	void exchangeCurrency_shouldExchangeFromUsdToPln_andPublishMessage() {
		// given
		CurrencyRate usd = CurrencyTestHelper.createUsdRate();
		when(currencyRateRepository.findByCode("USD")).thenReturn(Optional.of(usd));

		var request = new com.exchangeapi.model.dto.command.ExchangeRequestCommand("USD", "PLN", new BigDecimal("100.00"));
		Jwt jwt = createMockJwt();

		// when
		ExchangeResultDTO result = classUnderTest.exchangeCurrency(request, jwt);

		// then
		assertThat(result.getFrom()).isEqualTo("USD");
		assertThat(result.getTo()).isEqualTo("PLN");
		assertThat(result.getAmount()).isEqualTo(new BigDecimal("100.00"));
		assertThat(result.getConvertedAmount()).isEqualTo(new BigDecimal("410.00"));
		assertThat(result.getRate()).isEqualTo(new BigDecimal("4.10"));
		assertThat(result.getEmail()).isEqualTo("testuser@example.com");

		verify(currencyProducerService).publish(exchangeResultCaptor.capture());
		ExchangeResultDTO sent = exchangeResultCaptor.getValue();
		assertThat(sent.getEmail()).isEqualTo("testuser@example.com");
		assertThat(sent.getFrom()).isEqualTo("USD");
	}

	@Test
	void exchangeCurrency_shouldThrowWhenSameCurrency_andNotPublish() {
		// given
		var request = new com.exchangeapi.model.dto.command.ExchangeRequestCommand("USD", "USD", new BigDecimal("100.00"));
		Jwt jwt = createMockJwt();

		// when / then
		assertThatThrownBy(() -> classUnderTest.exchangeCurrency(request, jwt))
				.isInstanceOf(ApiException.class);

		verify(currencyProducerService, never()).publish(any());
	}

	@Test
	void exchangeCurrency_shouldThrowWhenCurrencyNotFound_andNotPublish() {
		// given
		when(currencyRateRepository.findByCode("XXX")).thenReturn(Optional.empty());

		var request = new com.exchangeapi.model.dto.command.ExchangeRequestCommand("XXX", "PLN", new BigDecimal("100.00"));
		Jwt jwt = createMockJwt();

		// when / then
		assertThatThrownBy(() -> classUnderTest.exchangeCurrency(request, jwt))
				.isInstanceOf(ApiException.class);

		verify(currencyProducerService, never()).publish(any());
	}

	private Jwt createMockJwt() {
		return Jwt.withTokenValue("token")
				.header("alg", "none")
				.claim("email", "testuser@example.com")
				.claim("sub", "testuser")
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plusSeconds(3600))
				.build();
	}
}
