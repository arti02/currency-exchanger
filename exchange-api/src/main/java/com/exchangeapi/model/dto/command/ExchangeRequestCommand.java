package com.exchangeapi.model.dto.command;

import java.math.BigDecimal;

public record ExchangeRequestCommand(
		String from,
		String to,
		BigDecimal amount
) {}