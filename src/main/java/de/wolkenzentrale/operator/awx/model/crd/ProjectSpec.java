package de.wolkenzentrale.operator.awx.model.crd;

import de.wolkenzentrale.operator.awx.model.common.CrossResourceReference;
import de.wolkenzentrale.operator.awx.model.common.Project;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Specification for AWX Project custom resource.
 * Extends the base Project model with Kubernetes-specific fields.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProjectSpec extends Project {
    
    /**
     * Reference to the AwxConnection resource for this project
     */
    private CrossResourceReference awxConnectionRef;
    
} 