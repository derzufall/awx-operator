package de.wolkenzentrale.operator.awx.model.crd.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AwxProjectStatus {
    private Integer awxId;
    private OffsetDateTime created;
    private OffsetDateTime modified;
    private String status;
    private String message;
} 