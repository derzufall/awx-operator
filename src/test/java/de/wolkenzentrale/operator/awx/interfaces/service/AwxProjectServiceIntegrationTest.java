package de.wolkenzentrale.operator.awx.interfaces.service;

import de.wolkenzentrale.operator.awx.interfaces.awx.client.AwxClient;
import de.wolkenzentrale.operator.awx.interfaces.awx.service.AwxProjectService;
import de.wolkenzentrale.operator.awx.model.api.ProjectInfo;
import de.wolkenzentrale.operator.awx.model.api.ProjectListResponse;
import de.wolkenzentrale.operator.awx.model.common.Project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class AwxProjectServiceIntegrationTest {

    @MockBean
    private AwxClient awxClient;

    @Test
    void shouldListProjects() {
        // Given
        ProjectInfo project1 = new ProjectInfo();
        project1.setId(1);
        project1.setName("Test Project 1");
        project1.setDescription("Test Project 1 Description");
        project1.setScmType("git");
        project1.setStatus("successful");
        project1.setCreated(OffsetDateTime.now());

        ProjectInfo project2 = new ProjectInfo();
        project2.setId(2);
        project2.setName("Test Project 2");
        project2.setDescription("Test Project 2 Description");
        project2.setScmType("git");
        project2.setStatus("successful");
        project2.setCreated(OffsetDateTime.now());

        ProjectListResponse response = new ProjectListResponse();
        response.setCount(2);
        response.setResults(List.of(project1, project2));

        when(awxClient.listProjects()).thenReturn(Mono.just(response));

        // When
        List<ProjectInfo> projects = AwxProjectService.listProjects(awxClient);

        // Then
        assertThat(projects).hasSize(2);
        assertThat(projects.get(0).getId()).isEqualTo(1);
        assertThat(projects.get(0).getName()).isEqualTo("Test Project 1");
        assertThat(projects.get(1).getId()).isEqualTo(2);
        assertThat(projects.get(1).getName()).isEqualTo("Test Project 2");
    }
    
    @Test
    void shouldGetProject() {
        // Given
        ProjectInfo project = new ProjectInfo();
        project.setId(1);
        project.setName("Test Project");
        project.setDescription("Test Project Description");
        project.setScmType("git");
        project.setStatus("successful");
        project.setCreated(OffsetDateTime.now());

        when(awxClient.getProject(1)).thenReturn(Mono.just(project));

        // When
        Optional<ProjectInfo> result = AwxProjectService.getProject(awxClient, 1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
        assertThat(result.get().getName()).isEqualTo("Test Project");
    }
    
    @Test
    void shouldHandleProjectNotFound() {
        // Given
        when(awxClient.getProject(999)).thenReturn(Mono.empty());

        // When
        Optional<ProjectInfo> result = AwxProjectService.getProject(awxClient, 999);

        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void shouldCreateProject() {
        // Given
        Project project = new Project();
        project.setName("New Integration Project");
        project.setDescription("A new project for integration testing");
        project.setScmType("git");
        project.setScmUrl("https://github.com/test/project.git");
        
        ProjectInfo createdProject = new ProjectInfo();
        createdProject.setId(3);
        createdProject.setName(project.getName());
        createdProject.setDescription(project.getDescription());
        createdProject.setScmType(project.getScmType());
        createdProject.setScmUrl(project.getScmUrl());
        createdProject.setStatus("pending");
        createdProject.setCreated(OffsetDateTime.now());
        
        when(awxClient.createProject(project)).thenReturn(Mono.just(createdProject));

        // When
        ProjectInfo result = AwxProjectService.createProject(awxClient, project);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getName()).isEqualTo("New Integration Project");
        verify(awxClient).createProject(project);
    }
    
    @Test
    void shouldDeleteProject() {
        // Given
        when(awxClient.deleteProject(1)).thenReturn(Mono.empty());

        // When
        boolean result = AwxProjectService.deleteProject(awxClient, 1);

        // Then
        assertThat(result).isTrue();
        verify(awxClient).deleteProject(1);
    }
    
    @Test
    void shouldHandleDeleteError() {
        // Given
        when(awxClient.deleteProject(999)).thenReturn(Mono.error(new RuntimeException("Not found")));

        // When
        boolean result = AwxProjectService.deleteProject(awxClient, 999);

        // Then
        assertThat(result).isFalse();
        verify(awxClient).deleteProject(999);
    }
    
    @Test
    void shouldDeleteProjectWithRetry() {
        // Given
        when(awxClient.deleteProject(1)).thenReturn(Mono.empty());

        // When
        boolean result = AwxProjectService.deleteProjectWithRetry(awxClient, 1);

        // Then
        assertThat(result).isTrue();
        verify(awxClient).deleteProject(1);
    }
    
    @Test
    void shouldHandleDeleteWithRetryError() {
        // Given
        when(awxClient.deleteProject(999)).thenReturn(Mono.error(new RuntimeException("Not found")));

        // When
        boolean result = AwxProjectService.deleteProjectWithRetry(awxClient, 999);

        // Then
        assertThat(result).isFalse();
        verify(awxClient).deleteProject(999);
    }
    
    @Test
    void contextLoads() {
        // This test simply checks that the client can be autowired
        // and the application context loads successfully
        assertThat(awxClient).isNotNull();
    }
} 