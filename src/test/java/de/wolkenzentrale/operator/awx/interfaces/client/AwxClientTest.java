package de.wolkenzentrale.operator.awx.interfaces.client;

import de.wolkenzentrale.operator.awx.interfaces.awx.client.AwxClient;
import de.wolkenzentrale.operator.awx.model.api.ProjectInfo;
import de.wolkenzentrale.operator.awx.model.api.ProjectListResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

/*
 * 
 */

@ExtendWith(MockitoExtension.class)
class AwxClientTest {

    @Mock
    private AwxClient awxClient;

    private ProjectListResponse mockProjectResponse;

    @BeforeEach
    void setUp() {
        // Create test data
        ProjectInfo project1 = new ProjectInfo();
        project1.setId(1);
        project1.setName("Test Project 1");
        project1.setDescription("First test project");
        project1.setScmType("git");
        project1.setStatus("successful");
        project1.setCreated(OffsetDateTime.now());

        ProjectInfo project2 = new ProjectInfo();
        project2.setId(2);
        project2.setName("Test Project 2");
        project2.setDescription("Second test project");
        project2.setScmType("git");
        project2.setStatus("successful");
        project2.setCreated(OffsetDateTime.now());

        mockProjectResponse = new ProjectListResponse();
        mockProjectResponse.setCount(2);
        mockProjectResponse.setResults(List.of(project1, project2));
    }

    @Test
    void listProjects_shouldReturnProjectList() {
        // Arrange
        when(awxClient.listProjects()).thenReturn(Mono.just(mockProjectResponse));

        // Act & Assert
        StepVerifier.create(awxClient.listProjects())
                .expectNextMatches(response -> {
                    // Verify the response count
                    boolean countMatches = response.getCount() == 2;
                    
                    // Verify projects are returned
                    boolean hasProjects = response.getResults() != null && 
                                         response.getResults().size() == 2;
                    
                    // Verify project names
                    boolean namesMatch = response.getResults().get(0).getName().equals("Test Project 1") &&
                                       response.getResults().get(1).getName().equals("Test Project 2");
                    
                    return countMatches && hasProjects && namesMatch;
                })
                .verifyComplete();
    }

    @Test
    void listProjects_whenEmpty_shouldReturnEmptyList() {
        // Arrange
        ProjectListResponse emptyResponse = new ProjectListResponse();
        emptyResponse.setCount(0);
        emptyResponse.setResults(List.of());
        
        when(awxClient.listProjects()).thenReturn(Mono.just(emptyResponse));

        // Act & Assert
        StepVerifier.create(awxClient.listProjects())
                .expectNextMatches(response -> 
                    response.getCount() == 0 && 
                    response.getResults() != null && 
                    response.getResults().isEmpty())
                .verifyComplete();
    }

    @Test
    void listProjects_whenError_shouldPropagateError() {
        // Arrange
        RuntimeException testException = new RuntimeException("Test exception");
        when(awxClient.listProjects()).thenReturn(Mono.error(testException));

        // Act & Assert
        StepVerifier.create(awxClient.listProjects())
                .expectErrorMatches(error -> 
                    error instanceof RuntimeException && 
                    "Test exception".equals(error.getMessage()))
                .verify();
    }
} 