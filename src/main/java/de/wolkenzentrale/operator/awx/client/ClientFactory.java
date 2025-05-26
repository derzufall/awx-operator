package de.wolkenzentrale.operator.awx.client;

import de.wolkenzentrale.operator.awx.interfaces.awx.client.AwxClient;
import de.wolkenzentrale.operator.awx.model.common.Connection;
import de.wolkenzentrale.operator.awx.model.common.ConnectionKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for creating and managing AWX clients based on Connection objects.
 * Maintains a cache of clients that matches the desired state from provided connections.
 */
@Slf4j
@Component
public class ClientFactory {
    private final ClientCache cache;
    
    public ClientFactory(ClientCache cache) {
        this.cache = cache;
    }
    
    /**
     * Updates the client cache to match the provided list of connections.
     * Creates new clients when needed and removes obsolete ones.
     *
     * @param desiredConnections Collection of connections representing the desired state
     */
    public void updateClients(Collection<Connection> desiredConnections) {
        log.info("🔄 Updating clients for {} connections", desiredConnections.size());
        
        // Convert to map of connection keys for easier lookup
        Map<ConnectionKey, Connection> desiredConnectionMap = desiredConnections.stream()
                .collect(Collectors.toMap(ConnectionKey::fromConnection, Function.identity()));
        
        // Remove obsolete connections
        int removed = removeObsoleteClients(desiredConnectionMap);
        
        // Recreate clients that need updating
        int updated = recreateUpdatedClients(desiredConnectionMap);
        
        // Add new clients
        int created = addNewClients(desiredConnectionMap);
        
        log.info("✅ Client update complete. Created: {}, Updated: {}, Removed: {}, Total: {}", 
                created, updated, removed, cache.size());
    }

    /**
     * Finds and removes clients that are no longer in the desired connection map.
     * First finds which clients need to be removed, then removes them.
     * 
     * @param desiredConnectionMap Map of desired connections
     * @return Number of connections removed
     */
    private int removeObsoleteClients(Map<ConnectionKey, Connection> desiredConnectionMap) {
        // First find which clients need to be removed
        Set<ConnectionKey> connectionsToRemove = new HashSet<>(cache.getKeys());
        connectionsToRemove.removeAll(desiredConnectionMap.keySet());
        
        // Then remove them
        connectionsToRemove.forEach(cache::remove);
        return connectionsToRemove.size();
    }

    /**
     * Finds and recreates clients that exist in both maps but have different connection details.
     * This is done in a single pass for efficiency.
     * 
     * @param desiredConnectionMap Map of desired connections
     * @return Number of connections recreated
     */
    private int recreateUpdatedClients(Map<ConnectionKey, Connection> desiredConnectionMap) {
        int updated = 0;
        for (ConnectionKey key : cache.getKeys()) {
            Connection desiredConnection = desiredConnectionMap.get(key);
            
            // Skip if not in desired map (handled by removeObsoleteClients)
            if (desiredConnection == null) {
                continue;
            }
            
            Connection currentConnection = cache.get(key).getConnection();
            
            // Check if connection details have changed
            if (currentConnection.hasConnectionDetailsChanged(desiredConnection)) {
                // Remove old client and create new one
                cache.remove(key);
                createClient(desiredConnection);
                log.debug("🔄 Recreated updated client: {}", key);
                updated++;
            }
        }
        return updated;
    }

    /**
     * Finds and adds new clients that exist in the desired map but not in the current map.
     * First finds which clients need to be added, then adds them.
     * 
     * @param desiredConnectionMap Map of desired connections
     * @return Number of connections created
     */
    private int addNewClients(Map<ConnectionKey, Connection> desiredConnectionMap) {
        // First find which clients need to be added
        Set<ConnectionKey> connectionsToAdd = new HashSet<>(desiredConnectionMap.keySet());
        connectionsToAdd.removeAll(cache.getKeys());
        
        // Then add them
        connectionsToAdd.forEach(key -> createClient(desiredConnectionMap.get(key)));
        return connectionsToAdd.size();
    }

    private AwxClient createClient(Connection connection) {
        log.info("🌟 Creating new AWX client for {}", connection);
        
        RawClient rawClient = new RawClient(connection);
        cache.put(rawClient);
        
        return rawClient.getClient();
    }
} 