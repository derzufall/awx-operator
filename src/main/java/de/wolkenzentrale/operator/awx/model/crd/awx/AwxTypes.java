package de.wolkenzentrale.operator.awx.model.crd.awx;

import de.wolkenzentrale.operator.awx.model.common.Instance;
import de.wolkenzentrale.operator.awx.model.common.Project;
import de.wolkenzentrale.operator.awx.model.crd.kubernetes.KubernetesResource;
import de.wolkenzentrale.operator.awx.model.crd.kubernetes.ResourceList;
import de.wolkenzentrale.operator.awx.model.crd.status.AwxInstanceStatus;
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
    public static class AwxProject extends KubernetesResource<Project, AwxProjectStatus> {
    }
    
    /**
     * Type alias for AwxInstance
     */
    public static class AwxInstance extends KubernetesResource<Instance, AwxInstanceStatus> {
    }
    
    /**
     * Type alias for AwxProjectList
     */
    public static class AwxProjectList extends ResourceList<AwxProject> {
    }
    
    /**
     * Type alias for AwxInstanceList
     */
    public static class AwxInstanceList extends ResourceList<AwxInstance> {
    }
} 