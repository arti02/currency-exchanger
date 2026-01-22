package com.mailsender.service;

import com.mailsender.model.dto.ExchangeResultDTO;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@UtilityClass
public class MailBuilderService {

	private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public String buildConfirmationEmailBody(ExchangeResultDTO result) {
		Objects.requireNonNull(result, "result must not be null");

		String from = nullSafe(result.getFrom());
		String to = nullSafe(result.getTo());
		String amount = nullSafe(result.getAmount());
		String convertedAmount = nullSafe(result.getConvertedAmount());
		String rate = nullSafe(result.getRate());
		String email = nullSafe(result.getEmail());

		LocalDateTime ts = result.getTimestamp();
		String timestamp = (ts == null) ? "" : ts.format(DTF);

		return """
			Dear Customer,

			Thank you for using our currency exchange service. Here are the details of your recent transaction:

			Email: %s
			Exchanged From: %s %s
			Exchanged To: %s %s
			Applied Rate: %s
			Date: %s

			If you have any questions, please do not hesitate to contact our support.

			Best regards,
			The Currency Exchange Team
			""".formatted(email, amount, from, convertedAmount, to, rate, timestamp);
	}

	private static String nullSafe(Object value) {
		return value == null ? "" : value.toString();
	}
}
