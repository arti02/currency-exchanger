package com.currencyprovider.client;

import com.currencyprovider.model.NbpApiResponse;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WireMockTest(httpPort = 8089)
@ActiveProfiles("test")
class NbpApiClientIntegrationTest {

	@Autowired
	private NbpApiClient nbpApiClient;

	@Test
	void getCurrencyRates_shouldReturnCurrencyRatesFromNbpApi() {
		// given
		String jsonResponse = """
				[
					{
						"table": "C",
						"no": "015/C/NBP/2026",
						"tradingDate": "2026-01-17",
						"effectiveDate": "2026-01-17",
						"rates": [
							{
								"currency": "dolar amerykański",
								"code": "USD",
								"bid": 4.0500,
								"ask": 4.1500
							},
							{
								"currency": "euro",
								"code": "EUR",
								"bid": 4.4500,
								"ask": 4.5500
							},
							{
								"currency": "funt szterling",
								"code": "GBP",
								"bid": 5.1500,
								"ask": 5.2500
							}
						]
					}
				]
				""";

		stubFor(get(urlEqualTo("/"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(jsonResponse)));

		// when
		NbpApiResponse[] responses = nbpApiClient.getCurrencyRates();

		// then
		assertThat(responses).isNotNull();
		assertThat(responses).hasSize(1);

		NbpApiResponse response = responses[0];
		assertThat(response.getTable()).isEqualTo("C");
		assertThat(response.getNo()).isEqualTo("015/C/NBP/2026");
		assertThat(response.getTradingDate()).isEqualTo("2026-01-17");
		assertThat(response.getEffectiveDate()).isEqualTo("2026-01-17");
		assertThat(response.getRates()).hasSize(3);

		// Verify USD
		assertThat(response.getRates().get(0).getCurrency()).isEqualTo("dolar amerykański");
		assertThat(response.getRates().get(0).getCode()).isEqualTo("USD");

		// Verify EUR
		assertThat(response.getRates().get(1).getCurrency()).isEqualTo("euro");
		assertThat(response.getRates().get(1).getCode()).isEqualTo("EUR");

		// Verify GBP
		assertThat(response.getRates().get(2).getCurrency()).isEqualTo("funt szterling");
		assertThat(response.getRates().get(2).getCode()).isEqualTo("GBP");

		verify(getRequestedFor(urlEqualTo("/")));
	}

	@Test
	void getCurrencyRates_shouldHandleEmptyRatesArray() {
		// given
		String jsonResponse = """
				[
					{
						"table": "C",
						"no": "015/C/NBP/2026",
						"tradingDate": "2026-01-17",
						"effectiveDate": "2026-01-17",
						"rates": []
					}
				]
				""";

		stubFor(get(urlEqualTo("/"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(jsonResponse)));

		// when
		NbpApiResponse[] responses = nbpApiClient.getCurrencyRates();

		// then
		assertThat(responses).isNotNull();
		assertThat(responses).hasSize(1);
		assertThat(responses[0].getRates()).isEmpty();

		verify(getRequestedFor(urlEqualTo("/")));
	}

	@Test
	void getCurrencyRates_shouldHandleMultipleTablesInResponse() {
		// given
		String jsonResponse = """
				[
					{
						"table": "C",
						"no": "015/C/NBP/2026",
						"tradingDate": "2026-01-17",
						"effectiveDate": "2026-01-17",
						"rates": [
							{
								"currency": "dolar amerykański",
								"code": "USD",
								"bid": 4.0500,
								"ask": 4.1500
							}
						]
					},
					{
						"table": "C",
						"no": "016/C/NBP/2026",
						"tradingDate": "2026-01-18",
						"effectiveDate": "2026-01-18",
						"rates": [
							{
								"currency": "euro",
								"code": "EUR",
								"bid": 4.4500,
								"ask": 4.5500
							}
						]
					}
				]
				""";

		stubFor(get(urlEqualTo("/"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(jsonResponse)));

		// when
		NbpApiResponse[] responses = nbpApiClient.getCurrencyRates();

		// then
		assertThat(responses).isNotNull();
		assertThat(responses).hasSize(2);
		assertThat(responses[0].getNo()).isEqualTo("015/C/NBP/2026");
		assertThat(responses[1].getNo()).isEqualTo("016/C/NBP/2026");

		verify(getRequestedFor(urlEqualTo("/")));
	}

}
