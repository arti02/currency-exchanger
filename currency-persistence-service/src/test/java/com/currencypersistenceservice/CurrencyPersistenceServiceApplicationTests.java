package com.currencypersistenceservice;

import com.currencypersistenceservice.configuration.TestContainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfiguration.class)
class CurrencyPersistenceServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
