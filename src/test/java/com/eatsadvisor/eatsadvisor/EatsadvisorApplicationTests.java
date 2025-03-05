package com.eatsadvisor.eatsadvisor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // Ensures test profile is used
@TestPropertySource(locations = "classpath:application-test.yml") // Explicit test config
class EatsadvisorApplicationTests {

	@Test
	void contextLoads() {
		// This simply checks if the Spring Boot context can load successfully.
	}
}
