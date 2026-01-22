package com.exchangeapi.service;

import com.exchangeapi.exception.ApiException;
import com.exchangeapi.mapper.CurrencyRateMapper;
import com.exchangeapi.mapper.ExchangeResultMapper;
import com.exchangeapi.model.dto.CurrencyRateDTO;
import com.exchangeapi.model.dto.ExchangeResultDTO;
import com.exchangeapi.model.dto.command.ExchangeRequestCommand;
import com.exchangeapi.repository.CurrencyRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static java.math.BigDecimal.ONE;

@Service
@RequiredArgsConstructor
public class CurrencyService {

	private final CurrencyRateRepository currencyRateRepository;

	private final CurrencyProducerService currencyProducerService;

	public List<CurrencyRateDTO> getAllCurrencyRates() {
		return currencyRateRepository.findAll().stream().map(CurrencyRateMapper::toDTO).toList();
	}

	public CurrencyRateDTO getCurrencyByCode(String code) {
		return currencyRateRepository.findByCode(code)
				.map(CurrencyRateMapper::toDTO)
				.orElseThrow(() -> ApiException.entityNotFound("Currency not found: " + code));
	}

	public ExchangeResultDTO exchangeCurrency(ExchangeRequestCommand request, Jwt jwt) {
		if (request.to().equals(request.from())) {
			throw ApiException.of("Same currency, exchange not needed", HttpStatus.BAD_REQUEST);
		}

		ExchangeResultDTO exchangeResult = getExchangeResult(request);
		exchangeResult.setEmail(jwt.getClaimAsString("email"));

		currencyProducerService.publish(exchangeResult);

		return exchangeResult;
	}

	private ExchangeResultDTO getExchangeResult(ExchangeRequestCommand request) {
		BigDecimal fromBuyRate = buyRateOrOne(request.from());
		BigDecimal toSellRate = sellRateOrOne(request.to());

		BigDecimal plnAmount = request.amount().multiply(fromBuyRate);
		BigDecimal converted = plnAmount.divide(toSellRate, 2, RoundingMode.HALF_UP);
		return ExchangeResultMapper.toDTO(request, converted, toSellRate);
	}

	private BigDecimal buyRateOrOne(String code) {
		if (code.equals("PLN")) {
			return ONE;
		}
		return getCurrencyByCode(code).getBuyRate();
	}

	private BigDecimal sellRateOrOne(String code) {
		if (code.equals("PLN")) {
			return ONE;
		}
		return getCurrencyByCode(code).getSellRate();
	}

}
