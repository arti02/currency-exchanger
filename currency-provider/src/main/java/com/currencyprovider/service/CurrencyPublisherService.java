package com.currencyprovider.service;

import com.currencyprovider.config.AppConfig;
import com.currencyprovider.model.NbpRateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyPublisherService {

	private final RabbitTemplate rabbitTemplate;
	private final AppConfig appConfig;

	public void publish(NbpRateDTO nbpRateDTO) {
		rabbitTemplate.convertAndSend(appConfig.getQueryName(), nbpRateDTO);
		log.info("Published currency rate: {}", nbpRateDTO.getCode());
	}

}
