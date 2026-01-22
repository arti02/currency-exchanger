package com.currencyprovider.model;

import lombok.Data;
import java.util.List;

@Data
public class NbpApiResponse {
	private String table;
	private String no;
	private String tradingDate;
	private String effectiveDate;
	private List<NbpRateDTO> rates;
}
