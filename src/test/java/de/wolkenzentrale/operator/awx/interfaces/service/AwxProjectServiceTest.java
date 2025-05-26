package de.wolkenzentrale.operator.awx.interfaces.service;

import de.wolkenzentrale.operator.awx.interfaces.awx.client.AwxClient;
import de.wolkenzentrale.operator.awx.interfaces.awx.service.AwxProjectService;
import de.wolkenzentrale.operator.awx.model.api.ProjectInfo;
import de.wolkenzentrale.operator.awx.model.api.ProjectListResponse;
import de.wolkenzentrale.operator.awx.model.common.Project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AwxProjectServiceTest {

    @Mock
    private AwxClient awxClient;

    private ProjectListResponse mockResponse;
    private ProjectInfo project1;
    private ProjectInfo project2;

    @BeforeEach
    void setUp() {
        // Create test data
        project1 = new ProjectInfo();
        project1.setId(1);
        project1.setName("Test Project 1");
        project1.setDescription("First test project");
        project1.setScmType("git");
        project1.setStatus("successful");
        project1.setCreated(OffsetDateTime.now());

        project2 = new ProjectInfo();
        project2.setId(2);
        project2.setName("Test Project 2");
        project2.setDescription("Second test project");
        project2.setScmType("git");
        project2.setStatus("successful");
        project2.setCreated(OffsetDateTime.now());

        mockResponse = new ProjectListResponse();
        mockResponse.setCount(2);
        mockResponse.setResults(List.of(project1, project2));
    }

    @Test
    void listProjects_shouldReturnListOfProjects() {
        // Arrange
        when(awxClient.listProjects()).thenReturn(Mono.just(mockResponse));

        // Act
        List<ProjectInfo> result = AwxProjectService.listProjects(awxClient);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Project 1");
        assertThat(result.get(1).getId()).isEqualTo(2);
        assertThat(result.get(1).getName()).isEqualTo("Test Project 2");
        
        // Verify the client was called exactly once
        verify(awxClient, times(1)).listProjects();
    }

    @Test
    void listProjects_whenEmpty_shouldReturnEmptyList() {
        // Arrange
        ProjectListResponse emptyResponse = new ProjectListResponse();
        emptyResponse.setCount(0);
        emptyResponse.setResults(Collections.emptyList());
        
        when(awxClient.listProjects()).thenReturn(Mono.just(emptyResponse));

        // Act
        List<ProjectInfo> result = AwxProjectService.listProjects(awxClient);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        // Verify the client was called exactly once
        verify(awxClient, times(1)).listProjects();
    }

    @Test
    void listProjects_whenError_shouldPropagateException() {
        // Arrange
        RuntimeException testException = new RuntimeException("Test error");
        when(awxClient.listProjects()).thenReturn(Mono.error(testException));

        // Act & Assert
        RuntimeException thrown = assertThrows(
            RuntimeException.class,
            () -> AwxProjectService.listProjects(awxClient),
            "Expected listProjects() to throw RuntimeException"
        );
        
        // Verify the exception message matches
        assertThat(thrown.getMessage()).isEqualTo("Test error");
        
        // Verify the client was called exactly once
        verify(awxClient, times(1)).listProjects();
    }
    
    @Test
    void getProject_shouldReturnProject() {
        // Arrange
        when(awxClient.getProject(1)).thenReturn(Mono.just(project1));

        // Act
        Optional<ProjectInfo> result = AwxProjectService.getProject(awxClient, 1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
        assertThat(result.get().getName()).isEqualTo("Test Project 1");
        
        // Verify the client was called exactly once
        verify(awxClient, times(1)).getProject(1);
    }
    
    @Test
    void getProject_whenNotFound_shouldReturnEmpty() {
        // Arrange
        when(awxClient.getProject(999)).thenReturn(Mono.empty());

        // Act
        Optional<ProjectInfo> result = AwxProjectService.getProject(awxClient, 999);

        // Assert
        assertThat(result).isEmpty();
        
        // Verify the client was called exactly once
        verify(awxClient, times(1)).getProject(999);
    }
    
    @Test
    void getProject_whenError_shouldReturnEmpty() {
        // Arrange
        RuntimeException testException = new RuntimeException("Test error");
        when(awxClient.getProject(1)).thenReturn(Mono.error(testException));

        // Act
        Optional<ProjectInfo> result = AwxProjectService.getProject(awxClient, 1);

        // Assert
        assertThat(result).isEmpty();
        
        // Verify the client was called exactly once
        verify(awxClient, times(1)).getProject(1);
    }
    
    @Test
    void createProject_shouldReturnCreatedProject() {
        // Arrange
        Project project = new Project();
        project.setName("New Project");
        project.setDescription("A new test project");
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

        // Act
        ProjectInfo result = AwxProjectService.createProject(awxClient, project);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getName()).isEqualTo("New Project");
        
        // Verify the client was called exactly once
        verify(awxClient, times(1)).createProject(project);
    }
    
    @Test
    void createProject_whenError_shouldPropagateException() {
        // Arrange
        Project project = new Project();
        project.setName("New Project");
        project.setDescription("A new test project");
        project.setScmType("git");
        project.setScmUrl("https://github.com/test/project.git");
        
        RuntimeException testException = new RuntimeException("Test error");
        when(awxClient.createProject(project)).thenReturn(Mono.error(testException));

        // Act & Assert
        RuntimeException thrown = assertThrows(
            RuntimeException.class,
            () -> AwxProjectService.createProject(awxClient, project),
            "Expected createProject() to throw RuntimeException"
        );
        
        // Verify the exception message matches
        assertThat(thrown.getMessage()).isEqualTo("Test error");
        
        // Verify the client was called exactly once
        verify(awxClient, times(1)).createProject(project);
    }
    
    @Test
    void deleteProject_shouldReturnTrue() {
        // Arrange
        when(awxClient.deleteProject(1)).thenReturn(Mono.empty());

        // Act
        boolean result = AwxProjectService.deleteProject(awxClient, 1);

        // Assert
        assertThat(result).isTrue();
        
        // Verify the client was called exactly once
        verify(awxClient, times(1)).deleteProject(1);
    }
    
    @Test
    void deleteProject_whenError_shouldReturnFalse() {
        // Arrange
        RuntimeException testException = new RuntimeException("Test error");
        when(awxClient.deleteProject(1)).thenReturn(Mono.error(testException));

        // Act
        boolean result = AwxProjectService.deleteProject(awxClient, 1);

        // Assert
        assertThat(result).isFalse();
        
        // Verify the client was called exactly once
        verify(awxClient, times(1)).deleteProject(1);
    }
    
    @Test
    void deleteProjectWithRetry_shouldReturnTrue() {
        // Arrange
        when(awxClient.deleteProject(1)).thenReturn(Mono.empty());

        // Act
        boolean result = AwxProjectService.deleteProjectWithRetry(awxClient, 1);

        // Assert
        assertThat(result).isTrue();
        
        // Verify the client was called exactly once
        verify(awxClient, times(1)).deleteProject(1);
    }
    
    @Test
    void deleteProjectWithRetry_whenError_shouldReturnFalse() {
        // Arrange
        RuntimeException testException = new RuntimeException("Test error");
        when(awxClient.deleteProject(1)).thenReturn(Mono.error(testException));

        // Act
        boolean result = AwxProjectService.deleteProjectWithRetry(awxClient, 1);

        // Assert
        assertThat(result).isFalse();
        
        // Verify the client was called exactly once
        verify(awxClient, times(1)).deleteProject(1);
    }
} 