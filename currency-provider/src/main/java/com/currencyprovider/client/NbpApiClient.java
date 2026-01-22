package com.currencyprovider.client;

import com.currencyprovider.model.NbpApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "nbp-api", url = "${app.external-api-url}")
public interface NbpApiClient {

	@GetMapping
	NbpApiResponse[] getCurrencyRates();
}
