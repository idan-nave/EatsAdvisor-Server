package com.eatsadvisor.eatsadvisor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

//@SpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // Ensures a test-specific configuration is used
class EatsadvisorApplicationTests {

	@Test
	void contextLoads() {
		// This simply checks if the Spring Boot context can load successfully.
	}
}
