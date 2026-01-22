package com.exchangeapi.controller;

import com.exchangeapi.config.CurrencyTestHelper;
import com.exchangeapi.model.dto.command.ExchangeRequestCommand;
import com.exchangeapi.model.entity.CurrencyRate;
import com.exchangeapi.repository.CurrencyRateRepository;
import com.exchangeapi.config.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class CurrencyExchangeControllerIntegrationTest extends BaseIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CurrencyRateRepository currencyRateRepository;

	@BeforeEach
	void cleanup() {
		currencyRateRepository.deleteAll();
	}

	@Test
	void getAllCurrencies_returnsAllCurrencyRates() throws Exception {
		// given
		CurrencyRate usd = CurrencyTestHelper.createUsdRate();
		CurrencyRate eur = CurrencyTestHelper.createEurRate();
		CurrencyRate gbp = CurrencyTestHelper.createGbpRate();

		currencyRateRepository.save(usd);
		currencyRateRepository.save(eur);
		currencyRateRepository.save(gbp);

		// when / then
		mockMvc.perform(get("/api/v1/currencies"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[0].code").exists())
				.andExpect(jsonPath("$[0].buyRate").exists())
				.andExpect(jsonPath("$[0].sellRate").exists());
	}

	@Test
	void getCurrencyRates_returnsSpecificCurrency() throws Exception {
		// given
		CurrencyRate usd = CurrencyTestHelper.createUsdRate();
		CurrencyRate savedUsd = currencyRateRepository.save(usd);

		// when / then
		mockMvc.perform(get("/api/v1/currencies/{currencyCode}", "USD"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.code").value("USD"))
				.andExpect(jsonPath("$.buyRate").value(savedUsd.getBuyRate().doubleValue()))
				.andExpect(jsonPath("$.sellRate").value(savedUsd.getSellRate().doubleValue()));
	}

	@Test
	@WithMockUser(username = "testuser", authorities = {"TRADER"})
	void exchangeCurrency_performsExchangeAndReturnsResult() throws Exception {
		// given
		CurrencyRate usd = CurrencyTestHelper.createUsdRate();
		CurrencyRate eur = CurrencyTestHelper.createEurRate();
		currencyRateRepository.save(usd);
		currencyRateRepository.save(eur);

		ExchangeRequestCommand request = new ExchangeRequestCommand(
				"USD",
				"EUR",
				new BigDecimal("100.00")
		);
		String body = CurrencyTestHelper.toJson(request);

		// when / then
		mockMvc.perform(post("/api/v1/currencies/exchange")
						.with(jwt().jwt(jwt -> jwt.claim("email", "test@example.com")))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.from").value("USD"))
				.andExpect(jsonPath("$.to").value("EUR"))
				.andExpect(jsonPath("$.amount").value(100.00))
				.andExpect(jsonPath("$.convertedAmount").exists())
				.andExpect(jsonPath("$.rate").exists())
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	@WithMockUser(username = "testuser", authorities = {"TRADER"})
	void exchangeCurrency_exchangeToPlnUsesOnlyBuyRate() throws Exception {
		// given
		CurrencyRate usd = CurrencyTestHelper.createUsdRate();
		currencyRateRepository.save(usd);

		ExchangeRequestCommand request = new ExchangeRequestCommand(
				"USD",
				"PLN",
				new BigDecimal("100.00")
		);
		String body = CurrencyTestHelper.toJson(request);

		// when / then
		mockMvc.perform(post("/api/v1/currencies/exchange")
						.with(jwt().jwt(jwt -> jwt.claim("email", "test@example.com")))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.from").value("USD"))
				.andExpect(jsonPath("$.to").value("PLN"))
				.andExpect(jsonPath("$.amount").value(100.00))
				.andExpect(jsonPath("$.rate").value(usd.getBuyRate().doubleValue()))
				.andExpect(jsonPath("$.convertedAmount").value(410.00)); // 100 * 4.10
	}

	@Test
	@WithMockUser(username = "testuser", authorities = {"TRADER"})
	void exchangeCurrency_exchangeFromPlnUsesSellRate() throws Exception {
		// given
		CurrencyRate eur = CurrencyTestHelper.createEurRate();
		currencyRateRepository.save(eur);

		ExchangeRequestCommand request = new ExchangeRequestCommand(
				"PLN",
				"EUR",
				new BigDecimal("100.00")
		);
		String body = CurrencyTestHelper.toJson(request);

		// when / then
		mockMvc.perform(post("/api/v1/currencies/exchange")
						.contentType(MediaType.APPLICATION_JSON)
						.with(jwt().jwt(jwt -> jwt.claim("email", "test@example.com")))
						.content(body))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.from").value("PLN"))
				.andExpect(jsonPath("$.to").value("EUR"))
				.andExpect(jsonPath("$.amount").value(100.00))
				.andExpect(jsonPath("$.rate").value(eur.getSellRate().doubleValue()));
	}

	@Test
	@WithMockUser(username = "testuser", authorities = {"TRADER"})
	void exchangeCurrency_returnsBadRequestForSameCurrency() throws Exception {
		// given
		CurrencyRate usd = CurrencyTestHelper.createUsdRate();
		currencyRateRepository.save(usd);

		ExchangeRequestCommand request = new ExchangeRequestCommand(
				"USD",
				"USD",
				new BigDecimal("100.00")
		);
		String body = CurrencyTestHelper.toJson(request);

		// when / then
		mockMvc.perform(post("/api/v1/currencies/exchange")
						.contentType(MediaType.APPLICATION_JSON)
						.with(jwt().jwt(jwt -> jwt.claim("email", "test@example.com")))
						.content(body))
				.andExpect(status().isBadRequest());
	}
}
