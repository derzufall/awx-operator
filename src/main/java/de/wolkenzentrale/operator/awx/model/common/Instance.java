package de.wolkenzentrale.operator.awx.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Instance {
    private String url;
    private String username;
    private String passwordSecretName;
    private String passwordSecretKey;
    private boolean insecureSkipTlsVerify;
} 