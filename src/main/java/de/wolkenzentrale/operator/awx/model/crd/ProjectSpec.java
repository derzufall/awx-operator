package de.wolkenzentrale.operator.awx.model.crd;

import de.wolkenzentrale.operator.awx.model.common.CrossResourceReference;
import de.wolkenzentrale.operator.awx.model.common.Project;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Specification for AWX Project custom resource.
 * Extends the base Project model with Kubernetes-specific fields.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProjectSpec extends Project {
    
    /**
     * Reference to the AwxConnection resource for this project
     */
    private CrossResourceReference awxConnectionRef;
    
    /**
     * Convert to AWX API-compatible Project
     * @return A clean Project instance suitable for AWX API calls
     */
    public Project toProject() {
        Project project = new Project();
        project.setName(getName());
        project.setDescription(getDescription());
        project.setScmType(getScmType());
        project.setScmUrl(getScmUrl());
        project.setScmBranch(getScmBranch());
        return project;
    }
} 