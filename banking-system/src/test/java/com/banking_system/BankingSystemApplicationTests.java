package com.banking_system;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires MongoDB connection; enable when using embedded Mongo/Testcontainers")
class BankingSystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
