package de.wolkenzentrale.operator.awx.model.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionInfo {
    private String version;
    private String instanceName;
} 