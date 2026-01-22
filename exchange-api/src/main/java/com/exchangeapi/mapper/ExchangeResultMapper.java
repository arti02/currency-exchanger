package com.exchangeapi.mapper;

import com.exchangeapi.model.dto.ExchangeResultDTO;
import com.exchangeapi.model.dto.command.ExchangeRequestCommand;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@UtilityClass
public class ExchangeResultMapper {

	public ExchangeResultDTO toDTO(
			ExchangeRequestCommand request,
			BigDecimal converted,
			BigDecimal toSellRate) {
		ExchangeResultDTO resultDTO = new ExchangeResultDTO();
		resultDTO.setFrom(request.from());
		resultDTO.setTo(request.to());
		resultDTO.setAmount(request.amount());
		resultDTO.setConvertedAmount(converted);
		resultDTO.setRate(toSellRate);
		resultDTO.setTimestamp(LocalDateTime.now());
		return resultDTO;
	}
}