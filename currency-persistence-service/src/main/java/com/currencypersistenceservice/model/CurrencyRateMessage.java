package com.currencypersistenceservice.model;

//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
//@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyRateMessage {
	private String code;
	private BigDecimal bid;
	private BigDecimal ask;
	private LocalDateTime timestamp;
}