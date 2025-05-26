package de.wolkenzentrale.operator.awx.model.crd;

import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.junit.jupiter.api.Test;

import de.wolkenzentrale.operator.awx.model.common.Connection;
import de.wolkenzentrale.operator.awx.model.common.CrossResourceReference;
import de.wolkenzentrale.operator.awx.model.common.Project;
import de.wolkenzentrale.operator.awx.model.crd.kubernetes.KubernetesResource;
import de.wolkenzentrale.operator.awx.model.crd.status.AwxConnectionStatus;
import de.wolkenzentrale.operator.awx.model.crd.status.AwxProjectStatus;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class KubernetesResourceTest {

    @Test
    void testAwxProjectResource() {
        // Given
        CrossResourceReference connectionRef = new CrossResourceReference();
        connectionRef.setName("my-awx");
        
        ProjectSpec spec = new ProjectSpec();
        spec.setAwxConnectionRef(connectionRef);
        spec.setName("Test Project");
        spec.setDescription("A test project");
        spec.setScmType("git");
        spec.setScmUrl("https://github.com/test/repo.git");
        spec.setScmBranch("main");
        
        AwxProjectStatus status = new AwxProjectStatus();
        status.setAwxId(123);
        status.setStatus("successful");
        
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName("test-project");
        metadata.setNamespace("default");
        
        // When
        KubernetesResource<ProjectSpec, AwxProjectStatus> project = 
            new KubernetesResource<>();
        project.setApiVersion("wolkenzentrale.de/v1alpha1");
        project.setKind("AwxProject");
        project.setMetadata(metadata);
        project.setSpec(spec);
        project.setStatus(status);
        
        // Then
        assertEquals("wolkenzentrale.de/v1alpha1", project.getApiVersion());
        assertEquals("AwxProject", project.getKind());
        assertNotNull(project.getMetadata());
        assertEquals("test-project", project.getMetadata().getName());
        assertEquals("default", project.getMetadata().getNamespace());
        
        assertNotNull(project.getSpec());
        assertNotNull(project.getSpec().getAwxConnectionRef());
        assertEquals("my-awx", project.getSpec().getAwxConnectionRef().getName());
        assertEquals("Test Project", project.getSpec().getName());
        assertEquals("A test project", project.getSpec().getDescription());
        assertEquals("git", project.getSpec().getScmType());
        assertEquals("https://github.com/test/repo.git", project.getSpec().getScmUrl());
        assertEquals("main", project.getSpec().getScmBranch());
        
        // Test conversion to Project
        Project awxProject = project.getSpec().toProject();
        assertEquals("Test Project", awxProject.getName());
        assertEquals("A test project", awxProject.getDescription());
        assertEquals("git", awxProject.getScmType());
        assertEquals("https://github.com/test/repo.git", awxProject.getScmUrl());
        assertEquals("main", awxProject.getScmBranch());
        
        assertNotNull(project.getStatus());
        assertEquals(123, project.getStatus().getAwxId());
        assertEquals("successful", project.getStatus().getStatus());
    }
    
    @Test
    void testAwxConnectionResource() {
        // Given
        // Create a connection resource
        Connection spec = Connection.builder()
            .name("test-awx")
            .namespace("default")
            .url("https://awx.example.com")
            .username("admin")
            .password("secret-password")
            .insecureSkipTlsVerify(false)
            .build();

        AwxConnectionStatus status = new AwxConnectionStatus();
        status.setConnectionStatus("Connected");
        status.setMessage("Successfully connected to AWX instance");
        status.setLastConnected("2023-04-20T10:30:00Z");
        
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName("test-awx");
        metadata.setNamespace("default");
        
        // When
        KubernetesResource<Connection, AwxConnectionStatus> connection = 
            new KubernetesResource<>();
        connection.setApiVersion("wolkenzentrale.de/v1alpha1");
        connection.setKind("AwxConnection");
        connection.setMetadata(metadata);
        connection.setSpec(spec);
        connection.setStatus(status);
        
        // Then
        assertEquals("wolkenzentrale.de/v1alpha1", connection.getApiVersion());
        assertEquals("AwxConnection", connection.getKind());
        assertNotNull(connection.getMetadata());
        assertEquals("test-awx", connection.getMetadata().getName());
        assertEquals("default", connection.getMetadata().getNamespace());
        
        assertNotNull(connection.getSpec());
        assertEquals("https://awx.example.com", connection.getSpec().getUrl());
        assertEquals("admin", connection.getSpec().getUsername());
        assertEquals("secret-password", connection.getSpec().getPassword());
        assertFalse(connection.getSpec().isInsecureSkipTlsVerify());
        
        assertNotNull(connection.getStatus());
        assertEquals("Connected", connection.getStatus().getConnectionStatus());
        assertEquals("Successfully connected to AWX instance", connection.getStatus().getMessage());
        assertEquals("2023-04-20T10:30:00Z", connection.getStatus().getLastConnected());
    }
} 