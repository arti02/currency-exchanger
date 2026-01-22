package com.exchangeapi.config;

import com.exchangeapi.model.entity.CurrencyRate;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class CurrencyTestHelper {

	public static final LocalDateTime CURRENT_DATE = LocalDateTime.of(2026, 1, 17, 12, 0);

	public static CurrencyRate createCurrencyRate(String code, BigDecimal buyRate, BigDecimal sellRate) {
		CurrencyRate rate = new CurrencyRate();
		rate.setCode(code);
		rate.setBuyRate(buyRate);
		rate.setSellRate(sellRate);
		rate.setTimestamp(CURRENT_DATE);
		return rate;
	}

	public static CurrencyRate createUsdRate() {
		return createCurrencyRate("USD", new BigDecimal("4.10"), new BigDecimal("4.20"));
	}

	public static CurrencyRate createEurRate() {
		return createCurrencyRate("EUR", new BigDecimal("4.50"), new BigDecimal("4.60"));
	}

	public static CurrencyRate createGbpRate() {
		return createCurrencyRate("GBP", new BigDecimal("5.20"), new BigDecimal("5.30"));
	}

	public static String toJson(Object value) {
		if (value instanceof com.exchangeapi.model.dto.command.ExchangeRequestCommand cmd) {
			return "{\"from\":\"" + cmd.from() + "\",\"to\":\"" + cmd.to() + "\",\"amount\":" + cmd.amount() + "}";
		}
		throw new IllegalArgumentException("Unsupported type for toJson(): " + value.getClass());
	}

	public static String readJson(String path) throws Exception {
		return Files.readString(Path.of(path));
	}

	public static String createMockJwtString() {
		return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImVtYWlsIjoidGVzdHVzZXJAZXhhbXBsZS5jb20iLCJleHAiOjk5OTk5OTk5OTl9.mock";
	}
}
