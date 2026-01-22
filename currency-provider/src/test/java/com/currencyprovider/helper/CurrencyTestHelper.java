package com.currencyprovider.helper;

import com.currencyprovider.model.NbpApiResponse;
import com.currencyprovider.model.NbpRateDTO;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.List;

@UtilityClass
public class CurrencyTestHelper {

	public NbpApiResponse createMockNbpResponse() {
		NbpApiResponse response = new NbpApiResponse();
		response.setTable("C");
		response.setNo("015/C/NBP/2026");
		response.setTradingDate("2026-01-17");
		response.setEffectiveDate("2026-01-17");
		response.setRates(createMockRates());
		return response;
	}

	public List<NbpRateDTO> createMockRates() {
		return List.of(
				createNbpRate("dolar amerykański", "USD", new BigDecimal("4.0500"), new BigDecimal("4.1500")),
				createNbpRate("euro", "EUR", new BigDecimal("4.4500"), new BigDecimal("4.5500")),
				createNbpRate("funt szterling", "GBP", new BigDecimal("5.1500"), new BigDecimal("5.2500")));
	}

	private NbpRateDTO createNbpRate(String currency, String code, BigDecimal bid, BigDecimal ask) {
		NbpRateDTO rate = new NbpRateDTO();
		rate.setCurrency(currency);
		rate.setCode(code);
		rate.setBid(bid);
		rate.setAsk(ask);
		return rate;
	}
}
