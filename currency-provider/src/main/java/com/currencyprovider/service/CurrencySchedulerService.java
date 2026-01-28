package com.currencyprovider.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencySchedulerService {

	private final CurrencyProcessingJobService currencyProcessingJobService;

	@Scheduled(cron = "${currency.provider.schedule-cron}")
	@SchedulerLock(name = "fetchAndPublishCurrencyRates", lockAtMostFor = "55m", lockAtLeastFor = "5m")
	public void execute() {
		currencyProcessingJobService.fetchAndPublishCurrencyRates();
	}
}
