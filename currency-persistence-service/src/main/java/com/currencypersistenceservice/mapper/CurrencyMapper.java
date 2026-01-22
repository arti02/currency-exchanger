package com.currencypersistenceservice.mapper;

import com.currencypersistenceservice.model.CurrencyRate;
import com.currencypersistenceservice.model.NbpRateDTO;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CurrencyMapper {

	public CurrencyRate toEntity(NbpRateDTO nbpRateDTO, CurrencyRate currencyRate) {
		currencyRate.setCode(nbpRateDTO.getCode());
		currencyRate.setBuyRate(nbpRateDTO.getBid());
		currencyRate.setSellRate(nbpRateDTO.getAsk());
		return currencyRate;
	}
}
