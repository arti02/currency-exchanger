package com.exchangeapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRateDTO {

	public CurrencyRateDTO(String code){
		this.code = code;
	}

	private String code;
	private BigDecimal buyRate;
	private BigDecimal sellRate;
}
