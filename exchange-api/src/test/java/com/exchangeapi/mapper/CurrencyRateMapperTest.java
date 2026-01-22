package com.exchangeapi.mapper;

import com.exchangeapi.config.CurrencyTestHelper;
import com.exchangeapi.model.dto.CurrencyRateDTO;
import com.exchangeapi.model.entity.CurrencyRate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyRateMapperTest extends CurrencyTestHelper {

	@Test
	void toDTO_shouldMapAllFieldsCorrectly() {
		// given
		CurrencyRate rate = createCurrencyRate("USD", new BigDecimal("4.10"), new BigDecimal("4.20"));

		// when
		CurrencyRateDTO dto = CurrencyRateMapper.toDTO(rate);

		// then
		assertThat(dto).isNotNull();
		assertThat(dto.getCode()).isEqualTo("USD");
		assertThat(dto.getBuyRate()).isEqualTo(new BigDecimal("4.10"));
		assertThat(dto.getSellRate()).isEqualTo(new BigDecimal("4.20"));
	}

	@Test
	void toDTO_shouldHandleNullRates() {
		// given
		CurrencyRate rate = new CurrencyRate();
		rate.setCode("EUR");
		rate.setBuyRate(null);
		rate.setSellRate(null);

		// when
		CurrencyRateDTO dto = CurrencyRateMapper.toDTO(rate);

		// then
		assertThat(dto).isNotNull();
		assertThat(dto.getCode()).isEqualTo("EUR");
		assertThat(dto.getBuyRate()).isNull();
		assertThat(dto.getSellRate()).isNull();
	}
}
