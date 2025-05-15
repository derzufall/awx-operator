package de.wolkenzentrale.operator.awx.model.crd;

import io.kubernetes.client.openapi.models.V1ListMeta;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.junit.jupiter.api.Test;

import de.wolkenzentrale.operator.awx.model.common.Connection;
import de.wolkenzentrale.operator.awx.model.crd.kubernetes.KubernetesResource;
import de.wolkenzentrale.operator.awx.model.crd.kubernetes.ResourceList;
import de.wolkenzentrale.operator.awx.model.crd.status.AwxConnectionStatus;
import de.wolkenzentrale.operator.awx.model.crd.status.AwxProjectStatus;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ResourceListTest {

    @Test
    void testResourceListWithProjects() {
        // Given
        KubernetesResource<ProjectSpec, AwxProjectStatus> project1 = new KubernetesResource<>();
        project1.setApiVersion("wolkenzentrale.de/v1alpha1");
        project1.setKind("AwxProject");
        project1.setMetadata(new V1ObjectMeta().name("project1"));
        
        KubernetesResource<ProjectSpec, AwxProjectStatus> project2 = new KubernetesResource<>();
        project2.setApiVersion("wolkenzentrale.de/v1alpha1");
        project2.setKind("AwxProject");
        project2.setMetadata(new V1ObjectMeta().name("project2"));
        
        List<KubernetesResource<ProjectSpec, AwxProjectStatus>> projectList = Arrays.asList(project1, project2);
        
        // When
        ResourceList<KubernetesResource<ProjectSpec, AwxProjectStatus>> resourceList = new ResourceList<>();
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
    void testResourceListWithConnections() {
        // Given
        KubernetesResource<Connection, AwxConnectionStatus> connection1 = new KubernetesResource<>();
        connection1.setApiVersion("wolkenzentrale.de/v1alpha1");
        connection1.setKind("AwxConnection");
        connection1.setMetadata(new V1ObjectMeta().name("connection1"));
        
        KubernetesResource<Connection, AwxConnectionStatus> connection2 = new KubernetesResource<>();
        connection2.setApiVersion("wolkenzentrale.de/v1alpha1");
        connection2.setKind("AwxConnection");
        connection2.setMetadata(new V1ObjectMeta().name("connection2"));
        
        List<KubernetesResource<Connection, AwxConnectionStatus>> connectionList = Arrays.asList(connection1, connection2);
        
        // When
        ResourceList<KubernetesResource<Connection, AwxConnectionStatus>> resourceList = new ResourceList<>();
        resourceList.setApiVersion("wolkenzentrale.de/v1alpha1");
        resourceList.setKind("AwxConnectionList");
        resourceList.setMetadata(new V1ListMeta().resourceVersion("1"));
        resourceList.setItems(connectionList);
        
        // Then
        assertEquals("wolkenzentrale.de/v1alpha1", resourceList.getApiVersion());
        assertEquals("AwxConnectionList", resourceList.getKind());
        assertNotNull(resourceList.getMetadata());
        assertEquals("1", resourceList.getMetadata().getResourceVersion());
        assertEquals(2, resourceList.getItems().size());
        assertEquals("connection1", resourceList.getItems().get(0).getMetadata().getName());
        assertEquals("connection2", resourceList.getItems().get(1).getMetadata().getName());
    }
} 