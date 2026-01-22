package com.exchangeapi.mapper;

import com.exchangeapi.model.dto.CurrencyRateDTO;
import com.exchangeapi.model.entity.CurrencyRate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CurrencyRateMapper {

	public CurrencyRateDTO toDTO(CurrencyRate currencyRate) {
		return new CurrencyRateDTO(
				currencyRate.getCode(),
				currencyRate.getBuyRate(),
				currencyRate.getSellRate());
	}
}
