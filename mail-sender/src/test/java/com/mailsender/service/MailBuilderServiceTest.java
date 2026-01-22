package com.mailsender.service;

import com.mailsender.model.dto.ExchangeResultDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MailBuilderServiceTest {

	@Test
	void buildConfirmationEmailBody_shouldContainAllFields() {
		// given
		ExchangeResultDTO dto = new ExchangeResultDTO(
				"USD",
				"PLN",
				new BigDecimal("100.00"),
				new BigDecimal("410.00"),
				new BigDecimal("4.10"),
				LocalDateTime.of(2026, 1, 22, 12, 30, 15),
				"test@example.com"
		);

		// when
		String body = MailBuilderService.buildConfirmationEmailBody(dto);

		// then
		assertThat(body).contains("Dear Customer");
		assertThat(body).contains("Email: test@example.com");
		assertThat(body).contains("Exchanged From: 100.00 USD");
		assertThat(body).contains("Exchanged To: 410.00 PLN");
		assertThat(body).contains("Applied Rate: 4.10");
		assertThat(body).contains("Date: 2026-01-22 12:30:15");
	}

	@Test
	void buildConfirmationEmailBody_shouldThrowOnNull() {
		assertThatThrownBy(() -> MailBuilderService.buildConfirmationEmailBody(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("result must not be null");
	}

	@Test
	void buildConfirmationEmailBody_shouldNotThrowWhenOptionalFieldsAreNull() {
		// given
		ExchangeResultDTO dto = new ExchangeResultDTO();

		// when
		String body = MailBuilderService.buildConfirmationEmailBody(dto);

		// then
		assertThat(body).contains("Dear Customer");
		assertThat(body).contains("Email:");
		assertThat(body).contains("Exchanged From:");
		assertThat(body).contains("Exchanged To:");
		assertThat(body).contains("Applied Rate:");
		assertThat(body).contains("Date:");
	}
}
