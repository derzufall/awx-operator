package de.wolkenzentrale.operator.awx.model.crd.status;

import org.junit.jupiter.api.Test;

import de.wolkenzentrale.operator.awx.model.crd.kubernetes.ResourceStatus;
import de.wolkenzentrale.operator.awx.model.crd.kubernetes.StatusCondition;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ResourceStatusTest {

    @Test
    void testBaseResourceStatus() {
        // Given
        ResourceStatus status = ResourceStatus.pending("Initializing resource");
        
        // Then
        assertEquals("Initializing resource", status.getMessage());
        assertEquals("Pending", status.getPhase());
        assertNotNull(status.getLastUpdateTime());
        assertNull(status.getFirstSuccessfulSync());
        assertTrue(status.getConditions().isEmpty());
    }
    
    @Test
    void testAwxProjectStatus() {
        // Given
        AwxProjectStatus status = AwxProjectStatus.builder()
                .message("Project created")
                .phase("Succeeded")
                .awxId(123)
                .status("successful")
                .build();
        
        // Add a condition
        status.setCondition(StatusCondition.create(
                StatusCondition.Types.SYNCED,
                StatusCondition.Statuses.TRUE,
                "ProjectCreated",
                "Project was created in AWX"
        ));
        
        // Then
        assertEquals("Project created", status.getMessage());
        assertEquals("Succeeded", status.getPhase());
        assertEquals(Integer.valueOf(123), status.getAwxId());
        assertEquals("successful", status.getStatus());
        assertEquals(1, status.getConditions().size());
        assertEquals(StatusCondition.Types.SYNCED, status.getConditions().get(0).getType());
    }
    
    @Test
    void testAwxInstanceStatus() {
        // Given
        AwxInstanceStatus disconnected = AwxInstanceStatus.disconnected("Not connected yet");
        
        // When
        AwxInstanceStatus connected = disconnected.withConnected("2.5.0");
        
        // Then
        assertEquals("Disconnected", disconnected.getConnectionStatus());
        assertEquals("Pending", disconnected.getPhase());
        
        assertEquals("Connected", connected.getConnectionStatus());
        assertEquals("Succeeded", connected.getPhase());
        assertEquals("Successfully connected to AWX instance", connected.getMessage());
        assertEquals("2.5.0", connected.getAwxVersion());
        assertEquals(Integer.valueOf(0), connected.getFailedConnectionAttempts());
    }
} 