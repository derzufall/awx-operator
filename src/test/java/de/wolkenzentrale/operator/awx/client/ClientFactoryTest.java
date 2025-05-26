package de.wolkenzentrale.operator.awx.client;

import de.wolkenzentrale.operator.awx.model.common.Connection;
import de.wolkenzentrale.operator.awx.model.common.ConnectionKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for ClientFactory
 */
@ExtendWith(MockitoExtension.class)
class ClientFactoryTest {
    
    @Mock
    private ClientRegistry registry;
    
    @Mock
    private ExchangeStrategies exchangeStrategies;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private ClientFactory clientFactory;
    
    private Connection testConnection;
    private ConnectionKey testKey;
    
    @BeforeEach
    void setUp() {
        testConnection = Connection.builder()
            .name("test-awx")
            .namespace("default")
            .url("https://awx.example.com")
            .username("admin")
            .password("secret-password")
            .insecureSkipTlsVerify(false)
            .build();
        testKey = ConnectionKey.fromConnection(testConnection);
    }
    
    @Test
    void updateClients_CreatesNewClient() {
        // When
        clientFactory.updateClients(Collections.singletonList(testConnection));
        
        // Then
        verify(registry).put(any(RawClient.class));
        verify(registry, never()).remove(any(ConnectionKey.class));
    }
    
    @Test
    void updateClients_RemovesObsoleteClient() {
        // Given
        Set<ConnectionKey> existingKeys = new HashSet<>(Arrays.asList(testKey));
        when(registry.getKeys()).thenReturn(existingKeys);
        
        // When
        clientFactory.updateClients(Collections.emptyList());
        
        // Then
        verify(registry).remove(testKey);
    }
    
    @Test
    void updateClients_RecreatesUpdatedClient() {
        // Given
        Set<ConnectionKey> existingKeys = new HashSet<>(Arrays.asList(testKey));
        when(registry.getKeys()).thenReturn(existingKeys);
        when(registry.getConnection(testKey)).thenReturn(testConnection);
        
        // Create a modified connection
        Connection modifiedConnection = Connection.builder()
            .name(testConnection.getName())
            .namespace(testConnection.getNamespace())
            .url(testConnection.getUrl())
            .username(testConnection.getUsername())
            .password("new-password") // Changed password
            .insecureSkipTlsVerify(testConnection.isInsecureSkipTlsVerify())
            .build();
        
        // When
        clientFactory.updateClients(Collections.singletonList(modifiedConnection));
        
        // Then
        verify(registry).remove(testKey);
        verify(registry).put(any(RawClient.class));
    }
    
    @Test
    void updateClients_KeepsUnchangedClient() {
        // Given
        Set<ConnectionKey> existingKeys = new HashSet<>(Arrays.asList(testKey));
        when(registry.getKeys()).thenReturn(existingKeys);
        when(registry.getConnection(testKey)).thenReturn(testConnection);
        
        // When
        clientFactory.updateClients(Collections.singletonList(testConnection));
        
        // Then
        verify(registry, never()).remove(testKey);
        verify(registry, never()).put(any(RawClient.class));
    }
} 