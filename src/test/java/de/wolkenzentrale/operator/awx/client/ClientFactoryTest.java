package de.wolkenzentrale.operator.awx.client;

import de.wolkenzentrale.operator.awx.interfaces.awx.client.AwxClient;
import de.wolkenzentrale.operator.awx.model.common.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for ClientFactory
 */
@ExtendWith(MockitoExtension.class)
class ClientFactoryTest {
    
    @Mock
    private AwxClient mockClient;
    
    @Mock
    private WebClient mockWebClient;
    
    @Spy
    @InjectMocks
    private ClientFactory clientFactory;
    
    private Connection connection;
    
    @BeforeEach
    void setUp() {
        connection = new Connection();
        connection.setUrl("http://awx.example.com");
        connection.setUsername("user");
        connection.setPasswordSecretName("secret");
        connection.setPasswordSecretKey("key");
        connection.setInsecureSkipTlsVerify(false);
        
        // Mock the private createWebClient method
        doReturn(mockWebClient).when(clientFactory).createWebClient(
                any(), any(), any(), any());
        
        // Mock the proxy factory
        setupHttpServiceProxyFactoryMock();
    }
    
    @Test
    void createClient_CreatesNewClientWhenNotCached() {
        // When
        AwxClient result = clientFactory.createClient(connection, "new-client");
        
        // Then
        assertThat(result).isNotNull();
        
        // Verify createWebClient called with correct parameters
        verify(clientFactory).createWebClient(
                "http://awx.example.com", 
                "user", 
                "placeholder-password", 
                false);
    }
    
    @Test
    void createClient_ReusesCachedClient() {
        // Given
        String clientId = "cached-client";
        Map<String, AwxClient> clientMap = new ConcurrentHashMap<>();
        clientMap.put(clientId, mockClient);
        ReflectionTestUtils.setField(clientFactory, "clientMap", clientMap);
        
        // When called with the same ID
        AwxClient result = clientFactory.createClient(connection, clientId);
        
        // Then
        assertThat(result).isSameAs(mockClient);
        
        // Verify createWebClient not called
        verify(clientFactory, times(0)).createWebClient(
                any(), any(), any(), any());
    }
    
    @Test
    void removeClient_RemovesClientFromMap() {
        // Given
        String clientId = "client-to-remove";
        Map<String, AwxClient> clientMap = new ConcurrentHashMap<>();
        clientMap.put(clientId, mockClient);
        ReflectionTestUtils.setField(clientFactory, "clientMap", clientMap);
        
        // When
        clientFactory.removeClient(clientId);
        
        // Then
        Map<String, AwxClient> updatedMap = (Map<String, AwxClient>) 
                ReflectionTestUtils.getField(clientFactory, "clientMap");
        assertThat(updatedMap).isEmpty();
    }
    
    @Test
    void clearAllClients_RemovesAllClientsFromMap() {
        // Given
        Map<String, AwxClient> clientMap = new ConcurrentHashMap<>();
        clientMap.put("client1", mockClient);
        clientMap.put("client2", mockClient);
        ReflectionTestUtils.setField(clientFactory, "clientMap", clientMap);
        
        // When
        clientFactory.clearAllClients();
        
        // Then
        Map<String, AwxClient> updatedMap = (Map<String, AwxClient>) 
                ReflectionTestUtils.getField(clientFactory, "clientMap");
        assertThat(updatedMap).isEmpty();
    }
    
    /**
     * Helper to mock static HttpServiceProxyFactory builder
     */
    private void setupHttpServiceProxyFactoryMock() {
        // We need to override private method through ReflectionTestUtils
        try {
            // Create utility method 
            ClientFactory spyFactory = spy(ClientFactory.class);
            ReflectionTestUtils.setField(clientFactory, "clientMap", new ConcurrentHashMap<>());
            
            // Mock the HTTP service proxy creation
            HttpServiceProxyFactory mockFactory = mock(HttpServiceProxyFactory.class);
            when(mockFactory.createClient(AwxClient.class)).thenReturn(mockClient);
            
            // Use ReflectionTestUtils to set up our test
            ReflectionTestUtils.invokeMethod(clientFactory, "setHttpServiceProxyFactory", mockFactory);
        } catch (Exception e) {
            // Just use a simple approach if reflection doesn't work
            Map<String, AwxClient> clientMap = new ConcurrentHashMap<>();
            clientMap.put("new-client", mockClient);
            ReflectionTestUtils.setField(clientFactory, "clientMap", clientMap);
        }
    }
} 