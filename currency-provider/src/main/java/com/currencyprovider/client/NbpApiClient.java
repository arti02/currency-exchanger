package com.currencyprovider.client;

import com.currencyprovider.model.NbpApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "NBPApiClient")
public interface NbpApiClient {

	@GetMapping
	NbpApiResponse[] getCurrencyRates();
}
