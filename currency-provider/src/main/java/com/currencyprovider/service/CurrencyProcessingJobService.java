package com.currencyprovider.service;

import com.currencyprovider.client.NbpApiClient;
import com.currencyprovider.config.AppConfig;
import com.currencyprovider.model.NbpApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyProcessingJobService {

	private final NbpApiClient nbpApiClient;
	private final CurrencyPublisherService currencyPublisherService;

	@Scheduled(cron = "${currency.provider.schedule-cron}")
	public void fetchAndPublishCurrencyRates() {
		log.info("Fetching currency rates from NBP API");

		try {
			NbpApiResponse[] responses = nbpApiClient.getCurrencyRates();

			if (responses != null && responses.length > 0) {
				NbpApiResponse response = responses[0];
				response.getRates().forEach(currencyPublisherService::publish);

				log.info("Successfully published {} currency rates", response.getRates().size());
			}
		} catch (Exception e) {
			log.error("Error fetching or publishing currency rates", e);
		}
	}

}
