package de.wolkenzentrale.operator.awx.model.crd.status;

import de.wolkenzentrale.operator.awx.model.crd.kubernetes.ResourceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Status information for an AWX Connection resource.
 * Extends the base ResourceStatus with connection-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AwxConnectionStatus extends ResourceStatus {
    
    /**
     * The current connection status to the AWX instance
     * (Connected, Disconnected, Error)
     */
    private String connectionStatus;
    
    /**
     * Timestamp of the last successful connection to the AWX instance
     */
    private String lastConnected;
    
    /**
     * AWX version reported by the instance
     */
    private String awxVersion;
    
    /**
     * Number of failed connection attempts
     */
    private Integer failedConnectionAttempts;
    
    /**
     * Create a connection status with a disconnected state
     */
    public static AwxConnectionStatus disconnected(String message) {
        return AwxConnectionStatus.builder()
                .message(message)
                .phase("Pending")
                .connectionStatus("Disconnected")
                .build();
    }
    
    /**
     * Update the status to indicate successful connection
     */
    public AwxConnectionStatus withConnected(String version) {
        super.withSuccess("Successfully connected to AWX instance");
        setConnectionStatus("Connected");
        setAwxVersion(version);
        setFailedConnectionAttempts(0);
        return this;
    }
} 