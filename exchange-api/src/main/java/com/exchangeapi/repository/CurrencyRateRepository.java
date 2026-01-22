package com.exchangeapi.repository;

import com.exchangeapi.model.entity.CurrencyRate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CurrencyRateRepository extends MongoRepository<CurrencyRate, String> {

	Optional<CurrencyRate> findByCode(String code);
}
