package com.eatsadvisor.eatsadvisor.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

/**
 * Configuration class to customize Spring Data REST behavior
 * and prevent conflicts with our custom controllers
 */
@Configuration
@SpringBootApplication(exclude = RepositoryRestMvcAutoConfiguration.class)
public class RestConfig {

    /**
     * Configure Spring Data REST
     * This bean won't be used since we're excluding RepositoryRestMvcAutoConfiguration,
     * but we're keeping it in case we need to re-enable Spring Data REST in the future
     */
    @Bean
    @Primary
    public RepositoryRestConfigurer repositoryRestConfigurer() {
        return new RepositoryRestConfigurer() {
            @Override
            public void configureRepositoryRestConfiguration(
                    org.springframework.data.rest.core.config.RepositoryRestConfiguration config, 
                    CorsRegistry cors) {
                // Move Spring Data REST to a different base path
                config.setBasePath("/api/data-rest");
                // Disable automatic exposure of repositories
                config.disableDefaultExposure();
            }
        };
    }
}
