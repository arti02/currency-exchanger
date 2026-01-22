package com.exchangeapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeResultDTO {
	private String from;
	private String to;
	private BigDecimal amount;
	private BigDecimal convertedAmount;
	private BigDecimal rate;
	private LocalDateTime timestamp;
	private String email;
}