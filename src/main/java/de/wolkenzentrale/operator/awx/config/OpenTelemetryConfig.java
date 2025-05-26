package de.wolkenzentrale.operator.awx.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * OpenTelemetry configuration for providing tracing and metrics functionality.
 * 🔍 This configuration provides the essential OpenTelemetry beans required by the application.
 */
@Slf4j
@Configuration
@Profile("!test") // Don't load this config in test profiles (tests use mocks)
public class OpenTelemetryConfig {

    private static final String INSTRUMENTATION_NAME = "awx-resource-operator";

    @Bean
    public Tracer tracer() {
        log.info("🔍 Setting up OpenTelemetry Tracer for instrumentation: {}", INSTRUMENTATION_NAME);
        
        return GlobalOpenTelemetry.getTracer(INSTRUMENTATION_NAME);
    }

    @Bean
    public Meter meter() {
        log.info("📊 Setting up OpenTelemetry Meter for instrumentation: {}", INSTRUMENTATION_NAME);
        
        return GlobalOpenTelemetry.getMeter(INSTRUMENTATION_NAME);
    }
} 