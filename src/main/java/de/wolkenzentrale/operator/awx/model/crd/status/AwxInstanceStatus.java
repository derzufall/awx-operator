package de.wolkenzentrale.operator.awx.model.crd.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AwxInstanceStatus {
    private String connectionStatus;
    private String message;
    private String lastConnected;
} 