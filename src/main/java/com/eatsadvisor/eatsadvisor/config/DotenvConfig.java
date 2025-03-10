package com.eatsadvisor.eatsadvisor.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {

    @PostConstruct
    public void loadEnvVariables() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        // ✅ Database Properties
        setSystemProperty("DB_URL", dotenv, null);
        setSystemProperty("DB_USERNAME", dotenv, null);
        setSystemProperty("DB_PASSWORD", dotenv, null);

        // ✅ OAuth Google Properties
        setSystemProperty("GOOGLE_OAUTH_CLIENT_ID", dotenv, null);
        setSystemProperty("GOOGLE_OAUTH_CLIENT_SECRET", dotenv, null);

        // ✅ JWT Secret
        setSystemProperty("JWT_SECRET", dotenv, null);

        // ✅ OpenAI API Key
        setSystemProperty("OPENAI_API_KEY", dotenv, null);

        // ✅ Base URLs
        setSystemProperty("BACKEND_BASE_URL", dotenv, null);
        setSystemProperty("FRONTEND_BASE_URL", dotenv, null);

        System.out.println("✅ DotenvConfig: Environment variables loaded successfully!");
    }

    private void setSystemProperty(String key, Dotenv dotenv, String defaultValue) {
        String value = dotenv.get(key);
        if (value != null && !value.isEmpty()) {
            System.setProperty(key, value);
        } else if (defaultValue != null) {
            System.setProperty(key, defaultValue);
        }
    }
}
