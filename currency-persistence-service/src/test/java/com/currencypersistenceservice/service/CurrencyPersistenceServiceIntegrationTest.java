package com.currencypersistenceservice.service;

import com.currencypersistenceservice.configuration.BaseIntegrationTest;
import com.currencypersistenceservice.model.NbpRateDTO;
import com.currencypersistenceservice.repository.CurrencyRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyPersistenceServiceIntegrationTest extends BaseIntegrationTest {

	@Autowired
	private CurrencyPersistenceService currencyPersistenceService;

	@Autowired
	private CurrencyRateRepository currencyRepository;

	@BeforeEach
	void cleanup() {
		currencyRepository.deleteAll();
	}

	@Test
	void shouldSaveCurrency() {
		// given
		NbpRateDTO nbpRateDTO = new NbpRateDTO("dollar", "USD", BigDecimal.valueOf(4.0), BigDecimal.valueOf(4.1));

		// when
		currencyPersistenceService.save(nbpRateDTO);

		// then
		assertThat(currencyRepository.findAll()).hasSize(1);
	}
}
