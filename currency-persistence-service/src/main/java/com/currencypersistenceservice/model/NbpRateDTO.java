package com.currencypersistenceservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NbpRateDTO {

	private String currency;
	private String code;
	private BigDecimal bid;
	private BigDecimal ask;
}
