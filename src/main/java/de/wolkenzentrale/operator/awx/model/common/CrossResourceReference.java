package de.wolkenzentrale.operator.awx.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reference to another Kubernetes resource
 * Following Kubernetes conventions for cross-resource references
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrossResourceReference {
    /**
     * Name of the referenced resource
     */
    private String name;
    
    /**
     * Namespace of the referenced resource
     * If not specified, defaults to the same namespace as the referrer
     */
    private String namespace;
} 