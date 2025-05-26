package de.wolkenzentrale.operator.awx.model.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionInfo {
    private String version;

    @JsonProperty("active_node")
    private String instanceName;
} 