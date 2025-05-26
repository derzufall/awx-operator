package de.wolkenzentrale.operator.awx.client;

import de.wolkenzentrale.operator.awx.interfaces.awx.client.AwxClient;
import de.wolkenzentrale.operator.awx.model.common.Connection;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Immutable class that stores connection information and the AWX client object after it is built.
 * The connection info field is used to build the client object it contains.
 * All fields are final and must be set at construction time.
 */
@Slf4j
@Value
public class RawClient {
    /**
     * The connection information used to build the AWX client
     */
    private Connection connection;
    
    /**
     * The AWX client object built using the connection information
     */
    private AwxClient client;

    /**
     * The exchange strategies used for the WebClient
     */
    private ExchangeStrategies exchangeStrategies;

    /**
     * Creates a new RawClient with the given connection and exchange strategies, and builds the AWX client
     * 
     * @param connection The connection information to use
     * @param exchangeStrategies The exchange strategies to use for the WebClient
     */
    public RawClient(Connection connection, ExchangeStrategies exchangeStrategies) {
        this.connection = connection;
        this.exchangeStrategies = exchangeStrategies;
        this.client = buildClient();
    }

    /**
     * Builds the AWX client using the connection information
     */
    private AwxClient buildClient() {
        log.info("🌟 Building AWX client for connection to {}", connection.getUrl());
        
        WebClient webClient = buildWebClient();
        return createAwxClientFromWebClient(webClient);
    }
    
    private WebClient buildWebClient() {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(connection.getUrl())
                .exchangeStrategies(exchangeStrategies);
                
        configureSsl(builder);
        configureAuthentication(builder);
        configureCommonHeaders(builder);
        
        return builder.build();
    }
    
    private void configureSsl(WebClient.Builder builder) {
        if (connection.isInsecureSkipTlsVerify()) {
            log.warn("⚠️ TLS verification disabled for client to {}", connection.getUrl());
            builder.clientConnector(createInsecureConnector());
        }
    }
    
    private void configureAuthentication(WebClient.Builder builder) {
        String username = connection.getUsername();
        String password = connection.getPassword();
        
        if (username != null && password != null) {
            String credentials = username + ":" + password;
            String encodedCredentials = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials);
        } else {
            log.warn("⚠️ Missing authentication credentials for connection to {}", connection.getUrl());
        }
    }
    
    private void configureCommonHeaders(WebClient.Builder builder) {
        builder.defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        builder.defaultHeader(HttpHeaders.ACCEPT, "application/json");
    }
    
    private ReactorClientHttpConnector createInsecureConnector() {
        try {
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
                    
            HttpClient httpClient = HttpClient.create()
                    .secure(spec -> spec.sslContext(sslContext));
                    
            return new ReactorClientHttpConnector(httpClient);
        } catch (Exception e) {
            log.error("❌ Failed to create insecure SSL context", e);
            throw new RuntimeException("Failed to create insecure SSL context", e);
        }
    }
    
    private AwxClient createAwxClientFromWebClient(WebClient webClient) {
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(AwxClient.class);
    }
}
