package de.wolkenzentrale.operator.awx.model.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.wolkenzentrale.operator.awx.model.common.Project;
import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectInfo extends Project {
    private Integer id;
    private OffsetDateTime created;
    private OffsetDateTime modified;
    private String status;
} 