package de.wolkenzentrale.operator.awx.model.crd;

import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.junit.jupiter.api.Test;

import de.wolkenzentrale.operator.awx.model.common.Connection;
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
        Project spec = new Project();
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
        KubernetesResource<Project, AwxProjectStatus> project = 
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
        assertEquals("Test Project", project.getSpec().getName());
        assertEquals("A test project", project.getSpec().getDescription());
        assertEquals("git", project.getSpec().getScmType());
        assertEquals("https://github.com/test/repo.git", project.getSpec().getScmUrl());
        assertEquals("main", project.getSpec().getScmBranch());
        
        assertNotNull(project.getStatus());
        assertEquals(123, project.getStatus().getAwxId());
        assertEquals("successful", project.getStatus().getStatus());
    }
    
    @Test
    void testAwxConnectionResource() {
        // Given
        Connection spec = new Connection();
        spec.setUrl("https://awx.example.com");
        spec.setUsername("admin");
        spec.setPasswordSecretName("awx-creds");
        spec.setPasswordSecretKey("password");
        spec.setInsecureSkipTlsVerify(false);
        
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
        assertEquals("awx-creds", connection.getSpec().getPasswordSecretName());
        assertEquals("password", connection.getSpec().getPasswordSecretKey());
        assertFalse(connection.getSpec().isInsecureSkipTlsVerify());
        
        assertNotNull(connection.getStatus());
        assertEquals("Connected", connection.getStatus().getConnectionStatus());
        assertEquals("Successfully connected to AWX instance", connection.getStatus().getMessage());
        assertEquals("2023-04-20T10:30:00Z", connection.getStatus().getLastConnected());
    }
} 