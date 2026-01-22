package com.exchangeapi.controller;

import com.exchangeapi.model.dto.CurrencyRateDTO;
import com.exchangeapi.model.dto.ExchangeResultDTO;
import com.exchangeapi.model.dto.command.ExchangeRequestCommand;
import com.exchangeapi.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/currencies")
@RequiredArgsConstructor
public class CurrencyExchangeController {

	private final CurrencyService currencyService;

	@GetMapping
	public ResponseEntity<List<CurrencyRateDTO>> getAllCurrencies() {
		List<CurrencyRateDTO> currencies = currencyService.getAllCurrencyRates();
		return ResponseEntity.ok(currencies);
	}

	@GetMapping("/{currencyCode}")
	public ResponseEntity<CurrencyRateDTO> getCurrencyRates(@PathVariable String currencyCode) {
		return ResponseEntity.ok(currencyService.getCurrencyByCode(currencyCode));
	}

	@PostMapping("/exchange")
	public ResponseEntity<ExchangeResultDTO> exchangeCurrency(@RequestBody ExchangeRequestCommand exchangeRequest, @AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(currencyService.exchangeCurrency(exchangeRequest, jwt));
	}

}
