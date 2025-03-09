package com.eatsadvisor.eatsadvisor;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EatsadvisorApplication {
	public static void main(String[] args) {
		// ✅ Load environment variables before starting Spring Boot
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		System.out.println("✅ Environment variables loaded!");

		// Start Spring Boot
		SpringApplication.run(EatsadvisorApplication.class, args);
	}
}
