package de.wolkenzentrale.operator.awx.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {
    private String name;
    private String description;
    
    private String scmType;
    private String scmUrl;
    private String scmBranch;
} 