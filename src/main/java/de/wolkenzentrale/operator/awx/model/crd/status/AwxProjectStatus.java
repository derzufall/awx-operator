package de.wolkenzentrale.operator.awx.model.crd.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

import de.wolkenzentrale.operator.awx.model.crd.kubernetes.ResourceStatus;

/**
 * Status information for an AWX Project resource.
 * Extends the base ResourceStatus with AWX-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AwxProjectStatus extends ResourceStatus {
    
    /**
     * The ID of the project in AWX
     */
    private Integer awxId;
    
    /**
     * When the project was created in AWX
     */
    private OffsetDateTime created;
    
    /**
     * When the project was last modified in AWX
     */
    private OffsetDateTime modified;
    
    /**
     * The project status as reported by AWX
     */
    private String status;
    
    /**
     * Last time a project update was initiated
     */
    private OffsetDateTime lastUpdateJobTime;
    
    /**
     * ID of the last update job in AWX
     */
    private Integer lastUpdateJobId;
} 