package com.currencypersistenceservice.repository;

import com.currencypersistenceservice.model.CurrencyRate;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CurrencyRateRepository extends MongoRepository<CurrencyRate, String> {
}
