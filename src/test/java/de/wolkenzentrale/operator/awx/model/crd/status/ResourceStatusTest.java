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
    void testAwxConnectionStatus() {
        // Given
        AwxConnectionStatus status = AwxConnectionStatus.disconnected("Not connected yet");
        
        // Then - check initial disconnected state
        assertEquals("Disconnected", status.getConnectionStatus());
        assertEquals("Pending", status.getPhase());
        assertEquals("Not connected yet", status.getMessage());
        
        // When - connect to AWX
        AwxConnectionStatus connectedStatus = status.withConnected("2.5.0");
        
        // Then - both variables reference the same object that's now connected
        assertSame(status, connectedStatus); // Same object reference
        assertEquals("Connected", status.getConnectionStatus());
        assertEquals("Succeeded", status.getPhase());
        assertEquals("Successfully connected to AWX instance", status.getMessage());
        assertEquals("2.5.0", status.getAwxVersion());
        assertEquals(Integer.valueOf(0), status.getFailedConnectionAttempts());
    }
} 