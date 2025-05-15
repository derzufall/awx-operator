package de.wolkenzentrale.operator.awx.model.crd;

import io.kubernetes.client.openapi.models.V1ListMeta;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.junit.jupiter.api.Test;

import de.wolkenzentrale.operator.awx.model.common.Instance;
import de.wolkenzentrale.operator.awx.model.common.Project;
import de.wolkenzentrale.operator.awx.model.crd.kubernetes.KubernetesResource;
import de.wolkenzentrale.operator.awx.model.crd.kubernetes.ResourceList;
import de.wolkenzentrale.operator.awx.model.crd.status.AwxInstanceStatus;
import de.wolkenzentrale.operator.awx.model.crd.status.AwxProjectStatus;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ResourceListTest {

    @Test
    void testResourceListWithProjects() {
        // Given
        KubernetesResource<Project, AwxProjectStatus> project1 = new KubernetesResource<>();
        project1.setApiVersion("wolkenzentrale.de/v1alpha1");
        project1.setKind("AwxProject");
        project1.setMetadata(new V1ObjectMeta().name("project1"));
        
        KubernetesResource<Project, AwxProjectStatus> project2 = new KubernetesResource<>();
        project2.setApiVersion("wolkenzentrale.de/v1alpha1");
        project2.setKind("AwxProject");
        project2.setMetadata(new V1ObjectMeta().name("project2"));
        
        List<KubernetesResource<Project, AwxProjectStatus>> projectList = Arrays.asList(project1, project2);
        
        // When
        ResourceList<KubernetesResource<Project, AwxProjectStatus>> resourceList = new ResourceList<>();
        resourceList.setApiVersion("wolkenzentrale.de/v1alpha1");
        resourceList.setKind("AwxProjectList");
        resourceList.setMetadata(new V1ListMeta().resourceVersion("1"));
        resourceList.setItems(projectList);
        
        // Then
        assertEquals("wolkenzentrale.de/v1alpha1", resourceList.getApiVersion());
        assertEquals("AwxProjectList", resourceList.getKind());
        assertNotNull(resourceList.getMetadata());
        assertEquals("1", resourceList.getMetadata().getResourceVersion());
        assertEquals(2, resourceList.getItems().size());
        assertEquals("project1", resourceList.getItems().get(0).getMetadata().getName());
        assertEquals("project2", resourceList.getItems().get(1).getMetadata().getName());
    }
    
    @Test
    void testResourceListWithInstances() {
        // Given
        KubernetesResource<Instance, AwxInstanceStatus> instance1 = new KubernetesResource<>();
        instance1.setApiVersion("wolkenzentrale.de/v1alpha1");
        instance1.setKind("AwxInstance");
        instance1.setMetadata(new V1ObjectMeta().name("instance1"));
        
        KubernetesResource<Instance, AwxInstanceStatus> instance2 = new KubernetesResource<>();
        instance2.setApiVersion("wolkenzentrale.de/v1alpha1");
        instance2.setKind("AwxInstance");
        instance2.setMetadata(new V1ObjectMeta().name("instance2"));
        
        List<KubernetesResource<Instance, AwxInstanceStatus>> instanceList = Arrays.asList(instance1, instance2);
        
        // When
        ResourceList<KubernetesResource<Instance, AwxInstanceStatus>> resourceList = new ResourceList<>();
        resourceList.setApiVersion("wolkenzentrale.de/v1alpha1");
        resourceList.setKind("AwxInstanceList");
        resourceList.setMetadata(new V1ListMeta().resourceVersion("1"));
        resourceList.setItems(instanceList);
        
        // Then
        assertEquals("wolkenzentrale.de/v1alpha1", resourceList.getApiVersion());
        assertEquals("AwxInstanceList", resourceList.getKind());
        assertNotNull(resourceList.getMetadata());
        assertEquals("1", resourceList.getMetadata().getResourceVersion());
        assertEquals(2, resourceList.getItems().size());
        assertEquals("instance1", resourceList.getItems().get(0).getMetadata().getName());
        assertEquals("instance2", resourceList.getItems().get(1).getMetadata().getName());
    }
} 