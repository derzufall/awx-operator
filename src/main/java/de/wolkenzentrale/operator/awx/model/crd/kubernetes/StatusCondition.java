package de.wolkenzentrale.operator.awx.model.crd.kubernetes;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Represents a standard Kubernetes condition for CRD statuses.
 * Follows Kubernetes conventions for status reporting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusCondition {
    
    /**
     * Type of the condition (e.g., Ready, Available, Progressing)
     */
    private String type;
    
    /**
     * Status of the condition (True, False, Unknown)
     */
    private String status;
    
    /**
     * Last time the condition status changed
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime lastTransitionTime;
    
    /**
     * Last time the condition was updated, even without status change
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime lastUpdateTime;
    
    /**
     * Machine-readable reason for the condition's status
     */
    private String reason;
    
    /**
     * Human-readable message about the condition
     */
    private String message;
    
    /**
     * Create a new condition with the current timestamp
     */
    public static StatusCondition create(String type, String status, String reason, String message) {
        OffsetDateTime now = OffsetDateTime.now();
        return StatusCondition.builder()
                .type(type)
                .status(status)
                .reason(reason)
                .message(message)
                .lastTransitionTime(now)
                .lastUpdateTime(now)
                .build();
    }
    
    /**
     * Common condition types
     */
    public static class Types {
        public static final String READY = "Ready";
        public static final String AVAILABLE = "Available";
        public static final String PROGRESSING = "Progressing";
        public static final String FAILED = "Failed";
        public static final String SYNCED = "Synced";
    }
    
    /**
     * Common condition statuses
     */
    public static class Statuses {
        public static final String TRUE = "True";
        public static final String FALSE = "False";
        public static final String UNKNOWN = "Unknown";
    }
} 