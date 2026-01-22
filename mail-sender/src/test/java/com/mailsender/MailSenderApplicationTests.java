package com.mailsender;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.mailsender.config.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MailSenderApplicationTests {

	@Test
	void contextLoads() {
	}

}
