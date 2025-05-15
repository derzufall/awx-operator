package de.wolkenzentrale.operator.awx.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {
    private String name;
    private String description;
    
    private String scmType;
    private String scmUrl;
    private String scmBranch;
} 