package de.wolkenzentrale.operator.awx.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.context.annotation.Primary;

/**
 * Jackson configuration with separate ObjectMappers for different APIs.
 */
@Configuration
public class JacksonConfig {

    /**
     * ObjectMapper for Kubernetes API - uses camelCase naming strategy
     * Used for status updates and CRD serialization
     */
    @Bean
    @Primary
    public ObjectMapper kubernetesObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // Kubernetes uses camelCase (default)
        return mapper;
    }

    /**
     * ObjectMapper for AWX API - uses snake_case naming strategy  
     * Used for AWX REST API communication
     */
    @Bean
    public ObjectMapper awxObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // AWX API uses snake_case
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }

    /**
     * ExchangeStrategies for reactive WebClient using AWX ObjectMapper
     * This is used by ClientFactory for AWX API communication
     */
    @Bean
    public ExchangeStrategies customExchangeStrategies(ObjectMapper awxObjectMapper) {
        return ExchangeStrategies.builder()
            .codecs(configurer -> {
                configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(awxObjectMapper));
                configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(awxObjectMapper));
            })
            .build();
    }
}
