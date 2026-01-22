package com.exchangeapi.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "currency_rate")
public class CurrencyRate {

	@Id
	private String code;
	private BigDecimal buyRate;
	private BigDecimal sellRate;
	private LocalDateTime timestamp;
}
