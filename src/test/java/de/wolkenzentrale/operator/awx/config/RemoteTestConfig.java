package de.wolkenzentrale.operator.awx.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for remote integration tests.
 * Logs connection details and validates configuration.
 */
@Configuration
@Profile("remotetest")
public class RemoteTestConfig {
    
    private static final Logger log = LoggerFactory.getLogger(RemoteTestConfig.class);
    
    @Value("${awx.baseUrl}")
    private String baseUrl;
    
    @Value("${awx.username}")
    private String username;

    /**
     * Log connection details on startup
     */
    @Bean
    public CommandLineRunner logRemoteTestConfig() {
        return args -> {
            log.info("Remote Test Configuration:");
            log.info("AWX Base URL: {}", baseUrl);
            log.info("AWX Username: {}", username);
            log.info("AWX Password: {}", username != null ? "[PROVIDED]" : "[MISSING]");
            
            if (baseUrl == null || baseUrl.isBlank()) {
                log.error("AWX Base URL is not configured properly!");
            }
            
            if (username == null || username.isBlank()) {
                log.warn("AWX Username is not configured! Tests may fail if authentication is required.");
            }
        };
    }
} 