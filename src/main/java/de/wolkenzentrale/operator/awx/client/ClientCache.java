package de.wolkenzentrale.operator.awx.client;

import de.wolkenzentrale.operator.awx.interfaces.awx.client.AwxClient;
import de.wolkenzentrale.operator.awx.model.common.ConnectionKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe cache for AWX clients.
 * Manages a map of connection keys to raw clients.
 */
@Slf4j
@Component
public class ClientCache {
    private final Map<ConnectionKey, RawClient> clients = new ConcurrentHashMap<>();
    
    /**
     * Gets a client by its connection key
     */
    public RawClient get(ConnectionKey key) {
        return clients.get(key);
    }

    /**
     * Gets a client by namespace and name.
     *
     * @param namespace Namespace
     * @param name Name
     * @return The AWX client, or null if not found
     */
    public AwxClient getClient(String namespace, String name) {
        ConnectionKey key = new ConnectionKey(namespace, name);
        RawClient rawClient = clients.get(key);
        
        if (rawClient == null) {
            log.warn("❓ Client not found for {}/{}", namespace, name);
            return null;
        }
        
        return rawClient.getClient();
    }
    
    /**
     * Adds a client to the cache
     */
    public void put(RawClient client) {
        clients.put(client.getConnection().getKey(), client);
        log.debug("💾 Cached client: {}", client.getConnection().getKey());
    }
    
    /**
     * Removes a client from the cache
     */
    public void remove(ConnectionKey key) {
        clients.remove(key);
        log.debug("🗑️ Removed client: {}", key);
    }
    
    /**
     * Gets all clients in a namespace
     */
    public Map<String, AwxClient> getClientsInNamespace(String namespace) {
        Map<String, AwxClient> result = new HashMap<>();
        clients.forEach((key, rawClient) -> {
            if (key.getNamespace().equals(namespace)) {
                result.put(key.getName(), rawClient.getClient());
            }
        });
        return result;
    }
    
    /**
     * Gets all connection keys in the cache
     */
    public Set<ConnectionKey> getKeys() {
        return new HashSet<>(clients.keySet());
    }
    
    /**
     * Checks if a key exists in the cache
     */
    public boolean containsKey(ConnectionKey key) {
        return clients.containsKey(key);
    }
    
    /**
     * Clears all clients from the cache
     */
    public void clear() {
        clients.clear();
        log.debug("🧹 Cleared all clients from cache");
    }
    
    /**
     * Gets the current size of the cache
     */
    public int size() {
        return clients.size();
    }
} 