package com.eatsadvisor.eatsadvisor;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EatsadvisorApplication {
	static {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

		// Database properties
		String dbUrl = dotenv.get("DB_URL");
		if (dbUrl != null && !dbUrl.isEmpty()) {
			System.setProperty("DB_URL", dbUrl);
		} else {
			System.setProperty("DB_URL", "jdbc:h2:mem:testdb");
		}

		String dbUsername = dotenv.get("DB_USERNAME");
		if (dbUsername != null && !dbUsername.isEmpty()) {
			System.setProperty("DB_USERNAME", dbUsername);
		} else {
			System.setProperty("DB_USERNAME", "sa");
		}

		String dbPassword = dotenv.get("DB_PASSWORD");
		if (dbPassword != null && !dbPassword.isEmpty()) {
			System.setProperty("DB_PASSWORD", dbPassword);
		} else {
			System.setProperty("DB_PASSWORD", "");
		}

		// Google OAuth properties
		String clientId = dotenv.get("GOOGLE_OAUTH_CLIENT_ID");
		if (clientId != null && !clientId.isEmpty()) {
			System.setProperty("GOOGLE_OAUTH_CLIENT_ID", clientId);
		}

		String clientSecret = dotenv.get("GOOGLE_OAUTH_CLIENT_SECRET");
		if (clientSecret != null && !clientSecret.isEmpty()) {
			System.setProperty("GOOGLE_OAUTH_CLIENT_SECRET", clientSecret);
		}

		// Other properties
		String jwtSecret = dotenv.get("JWT_SECRET");
		if (jwtSecret != null && !jwtSecret.isEmpty()) {
			System.setProperty("JWT_SECRET", jwtSecret);
		} else {
			System.setProperty("JWT_SECRET", "default-secret");
		}

		String openaiApiKey = dotenv.get("OPENAI_API_KEY");
		if (openaiApiKey != null && !openaiApiKey.isEmpty()) {
			System.setProperty("OPENAI_API_KEY", openaiApiKey);
		} else {
			System.setProperty("OPENAI_API_KEY", "");
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(EatsadvisorApplication.class, args);
	}
}