package com.currencypersistenceservice.mapper;

import com.currencypersistenceservice.model.CurrencyRate;
import com.currencypersistenceservice.model.NbpRateDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyMapperTest {

	@Test
	void toEntity_shouldMapFieldsToNewEntity() {
		// given
		NbpRateDTO dto = new NbpRateDTO("Euro", "EUR", BigDecimal.valueOf(4.5), BigDecimal.valueOf(4.6));
		CurrencyRate entity = new CurrencyRate();

		// when
		CurrencyRate result = CurrencyMapper.toEntity(dto, entity);

		// then
		assertThat(result).isSameAs(entity);
		assertThat(result.getCode()).isEqualTo("EUR");
		assertThat(result.getBuyRate()).isEqualByComparingTo("4.5");
		assertThat(result.getSellRate()).isEqualByComparingTo("4.6");
	}

	@Test
	void toEntity_shouldUpdateExistingEntityAndNotTouchTimestamp() {
		// given
		CurrencyRate existing = new CurrencyRate();
		existing.setCode("EUR");
		existing.setBuyRate(BigDecimal.valueOf(4.0));
		existing.setSellRate(BigDecimal.valueOf(4.1));
		LocalDateTime ts = LocalDateTime.now().minusDays(1);
		existing.setTimestamp(ts);

		NbpRateDTO dto = new NbpRateDTO("Euro", "EUR", BigDecimal.valueOf(4.55), BigDecimal.valueOf(4.65));

		// when
		CurrencyRate result = CurrencyMapper.toEntity(dto, existing);

		// then
		assertThat(result).isSameAs(existing);
		assertThat(result.getCode()).isEqualTo("EUR");
		assertThat(result.getBuyRate()).isEqualByComparingTo("4.55");
		assertThat(result.getSellRate()).isEqualByComparingTo("4.65");
		assertThat(result.getTimestamp()).isEqualTo(ts);
	}
}
