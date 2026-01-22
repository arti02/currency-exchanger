package com.currencypersistenceservice.service;

import com.currencypersistenceservice.mapper.CurrencyMapper;
import com.currencypersistenceservice.model.CurrencyRate;
import com.currencypersistenceservice.model.NbpRateDTO;
import com.currencypersistenceservice.repository.CurrencyRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyPersistenceService {

	private final CurrencyRateRepository currencyRateRepository;

	public void save(NbpRateDTO nbpRateDTO) {
		CurrencyRate rate = currencyRateRepository.findById(nbpRateDTO.getCode()).orElse(new CurrencyRate());
		CurrencyRate entity = CurrencyMapper.toEntity(nbpRateDTO, rate);
		currencyRateRepository.save(entity);
		log.info("Saved/Updated rate for {}", nbpRateDTO.getCode());
	}

}
