package com.mailsender.service;

import com.mailsender.model.dto.ExchangeResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailConsumerService {

	private final MailService mailService;

	@RabbitListener(queues = "${app.query-name}")
	public void handleRateMessage(ExchangeResultDTO message) {
		log.info("Received exchange confirmation for user: {}", message.getEmail());

		try {
			mailService.send(message.getEmail(), "Currency Exchange Confirmation", MailBuilderService.buildConfirmationEmailBody(message));
			log.info("Confirmation email sent successfully to {}", message.getEmail());
		} catch (Exception e) {
			log.error("Failed to send confirmation email to {}", message.getEmail(), e);
		}

	}

}
