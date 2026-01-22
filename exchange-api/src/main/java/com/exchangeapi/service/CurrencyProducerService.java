package com.exchangeapi.service;

import com.exchangeapi.config.AppConfig;
import com.exchangeapi.model.dto.ExchangeResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrencyProducerService {

	private final AppConfig appConfig;

	private final RabbitTemplate rabbitTemplate;

	public void publish(ExchangeResultDTO resultDTO) {
		rabbitTemplate.convertAndSend(appConfig.getQueryName(), resultDTO);
	}

}
