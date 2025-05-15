package de.wolkenzentrale.operator.awx.model.crd.awx;

import de.wolkenzentrale.operator.awx.model.common.Connection;
import de.wolkenzentrale.operator.awx.model.crd.ProjectSpec;
import de.wolkenzentrale.operator.awx.model.crd.kubernetes.KubernetesResource;
import de.wolkenzentrale.operator.awx.model.crd.kubernetes.ResourceList;
import de.wolkenzentrale.operator.awx.model.crd.status.AwxConnectionStatus;
import de.wolkenzentrale.operator.awx.model.crd.status.AwxProjectStatus;

/**
 * Type aliases for improved readability when using generic types
 */
public final class AwxTypes {
    
    private AwxTypes() {
        // Utility class, no instantiation
    }
    
    /**
     * Type alias for AwxProject
     */
    public static class AwxProject extends KubernetesResource<ProjectSpec, AwxProjectStatus> {
    }
    
    /**
     * Type alias for AwxConnection
     */
    public static class AwxConnection extends KubernetesResource<Connection, AwxConnectionStatus> {
    }
    
    /**
     * Type alias for AwxProjectList
     */
    public static class AwxProjectList extends ResourceList<AwxProject> {
    }
    
    /**
     * Type alias for AwxConnectionList
     */
    public static class AwxConnectionList extends ResourceList<AwxConnection> {
    }
} 