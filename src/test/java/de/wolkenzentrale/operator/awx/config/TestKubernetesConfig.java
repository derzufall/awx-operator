package de.wolkenzentrale.operator.awx.config;

import io.kubernetes.client.openapi.ApiClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration for Kubernetes components.
 * 🧪 Provides mock implementations for testing without requiring a real Kubernetes cluster.
 */
@TestConfiguration
public class TestKubernetesConfig {

    @Bean
    @Primary
    public ApiClient apiClient() {
        return Mockito.mock(ApiClient.class);
    }
} 