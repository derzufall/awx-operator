package de.wolkenzentrale.operator.awx.interfaces.service;

import de.wolkenzentrale.operator.awx.interfaces.awx.client.AwxClient;
import de.wolkenzentrale.operator.awx.interfaces.awx.service.AwxProjectService;
import de.wolkenzentrale.operator.awx.interfaces.dto.ProjectInfo;
import de.wolkenzentrale.operator.awx.interfaces.dto.ProjectListResponse;
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

    @Autowired
    private AwxProjectService projectService;

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
        List<ProjectInfo> projects = projectService.listProjects();

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
        Optional<ProjectInfo> result = projectService.getProject(1);

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
        Optional<ProjectInfo> result = projectService.getProject(999);

        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void shouldCreateProject() {
        // Given
        ProjectInfo newProject = new ProjectInfo();
        newProject.setName("New Integration Project");
        newProject.setDescription("A new project for integration testing");
        newProject.setScmType("git");
        
        ProjectInfo createdProject = new ProjectInfo();
        createdProject.setId(3);
        createdProject.setName("New Integration Project");
        createdProject.setDescription("A new project for integration testing");
        createdProject.setScmType("git");
        createdProject.setStatus("pending");
        createdProject.setCreated(OffsetDateTime.now());
        
        when(awxClient.createProject(newProject)).thenReturn(Mono.just(createdProject));

        // When
        ProjectInfo result = projectService.createProject(newProject);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getName()).isEqualTo("New Integration Project");
        verify(awxClient).createProject(newProject);
    }
    
    @Test
    void shouldDeleteProject() {
        // Given
        when(awxClient.deleteProject(1)).thenReturn(Mono.empty());

        // When
        boolean result = projectService.deleteProject(1);

        // Then
        assertThat(result).isTrue();
        verify(awxClient).deleteProject(1);
    }
    
    @Test
    void shouldHandleDeleteError() {
        // Given
        when(awxClient.deleteProject(999)).thenReturn(Mono.error(new RuntimeException("Not found")));

        // When
        boolean result = projectService.deleteProject(999);

        // Then
        assertThat(result).isFalse();
        verify(awxClient).deleteProject(999);
    }
    
    @Test
    void contextLoads() {
        // This test simply checks that the service can be autowired
        // and the application context loads successfully
        assertThat(projectService).isNotNull();
        assertThat(awxClient).isNotNull();
    }
} 