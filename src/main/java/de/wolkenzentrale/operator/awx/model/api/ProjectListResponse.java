package de.wolkenzentrale.operator.awx.model.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectListResponse {
    private Integer count;
    private String next;
    private String previous;
    private List<ProjectInfo> results;
} 