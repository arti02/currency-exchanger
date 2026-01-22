package com.currencypersistenceservice.service;

import com.currencypersistenceservice.model.NbpRateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyConsumerService {

	private final CurrencyPersistenceService currencyPersistenceService;

	@RabbitListener(queues = "${app.query-name}")
	public void handleRateMessage(NbpRateDTO message) {
		log.info("Received rate for {}: Buy = {}, Sell = {}", message.getCode(), message.getBid(), message.getAsk());
		currencyPersistenceService.save(message);
	}
}
