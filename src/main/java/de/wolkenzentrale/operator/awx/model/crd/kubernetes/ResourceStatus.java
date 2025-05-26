package de.wolkenzentrale.operator.awx.model.crd.kubernetes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all Kubernetes resource status types.
 * Contains common fields that should be present in all status objects.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceStatus {
    
    /**
     * Simple string message about the current status
     */
    private String message;
    
    /**
     * Time when the resource was first successfully synchronized
     */
    private OffsetDateTime firstSuccessfulSync;
    
    /**
     * Time of the last status update
     */
    private OffsetDateTime lastUpdateTime;
    
    /**
     * The resource generation that this status reflects
     * Used to track which version of the resource is represented by the status
     */
    private Long observedGeneration;
    
    /**
     * Current phase of the resource
     * Common values include: Pending, Running, Succeeded, Failed, Unknown
     */
    private String phase;
    
    /**
     * List of conditions representing the resource's state
     * Standard Kubernetes pattern for reporting detailed status
     */
    @Builder.Default
    private List<StatusCondition> conditions = new ArrayList<>();
    
    /**
     * Adds a condition to the list or updates an existing one of the same type
     */
    public void setCondition(StatusCondition condition) {
        // Remove existing condition of same type if present
        conditions.removeIf(c -> c.getType().equals(condition.getType()));
        // Add the new condition
        conditions.add(condition);
    }
    
    /**
     * Create a basic status with default values
     */
    public static ResourceStatus pending(String message) {
        return ResourceStatus.builder()
            .message(message)
            .phase("Pending")
            .lastUpdateTime(OffsetDateTime.now())
            .build();
    }
    
    /**
     * Update the status to indicate success
     */
    public ResourceStatus withSuccess(String message) {
        setMessage(message);
        setPhase("Succeeded");
        setLastUpdateTime(OffsetDateTime.now());
        if (firstSuccessfulSync == null) {
            setFirstSuccessfulSync(OffsetDateTime.now());
        }
        return this;
    }
    
    /**
     * Update the status to indicate failure
     */
    public ResourceStatus withFailure(String message) {
        setMessage(message);
        setPhase("Failed");
        setLastUpdateTime(OffsetDateTime.now());
        return this;
    }
} 