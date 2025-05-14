package de.wolkenzentrale.operator.awx.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.wolkenzentrale.operator.awx.interfaces.client.AwxClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Configuration for the AWX client
 * Works in all environments (dev, test, prod) based on property values
 */
@Slf4j
@Primary
@Configuration
public class AwxClientConfig {

    @Value("${awx.baseUrl:http://localhost:8052}")
    private String awxBaseUrl;
    
    @Value("${awx.username:admin}")
    private String username;

    @Value("${awx.password:password}")
    private String password;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public WebClient awxWebClient(ExchangeStrategies exchangeStrategies) {
        log.info("Configuring AWX WebClient with baseUrl: {}", awxBaseUrl);
        
        String basicAuth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(basicAuth.getBytes(StandardCharsets.UTF_8));
        String authHeaderValue = "Basic " + encodedAuth;
        
        // Log the ObjectMapper configuration
        log.info("Using ObjectMapper with property naming strategy: {}", 
                 objectMapper.getPropertyNamingStrategy() != null ? 
                 objectMapper.getPropertyNamingStrategy().getClass().getSimpleName() : "default");
        
        WebClient webClient = WebClient.builder()
                .baseUrl(awxBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, authHeaderValue)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(exchangeStrategies)
                .build();
        
        return webClient;
    }

    @Bean
    public AwxClient awxApiClient(WebClient awxWebClient) {
        // Configure HttpServiceProxyFactory with custom settings
        WebClientAdapter adapter = WebClientAdapter.create(awxWebClient);
        HttpServiceProxyFactory.Builder builder = HttpServiceProxyFactory.builderFor(adapter);
        
        // Create a factory with our custom adapter
        HttpServiceProxyFactory factory = builder
                .build();
                
        log.info("Creating AWX API client.");
        
        // Create the client interface proxy
        return factory.createClient(AwxClient.class);
    }
} 