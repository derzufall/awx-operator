package de.wolkenzentrale.operator.awx.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
public class KubernetesConfig {

    /**
     * Creates and configures the Kubernetes API client
     * 
     * @return configured ApiClient for Kubernetes
     */
    @Bean
    public ApiClient kubernetesApiClient() {
        try {
            log.info("🚀 Initializing connection to Kubernetes...");
            // Attempt to configure from within a pod first
            ApiClient client = Config.fromCluster();
            log.info("✅ Successfully connected to Kubernetes cluster!");
            return client;
        } catch (IOException e) {
            log.error("❌ Failed to initialize Kubernetes client from cluster: {}", e.getMessage());
            try {
                // Fallback to kubeconfig file
                log.info("🔄 Trying to connect using local kubeconfig...");
                ApiClient client = Config.defaultClient();
                log.info("✅ Successfully connected to Kubernetes using local config!");
                return client;
            } catch (IOException ex) {
                log.error("💥 Failed to initialize Kubernetes client from default config: {}", ex.getMessage());
                throw new RuntimeException("Could not initialize Kubernetes client", ex);
            }
        }
    }
} 