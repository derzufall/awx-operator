package de.wolkenzentrale.operator.awx.interfaces.service;

import de.wolkenzentrale.operator.awx.interfaces.awx.client.AwxClient;
import de.wolkenzentrale.operator.awx.interfaces.awx.service.AwxProjectService;
import de.wolkenzentrale.operator.awx.model.api.ProjectInfo;
import de.wolkenzentrale.operator.awx.model.common.Project;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Remote integration test for AWX Project Service.
 * This test requires a running AWX instance configured in application-remotetest.yml.
 * 
 * Run with: mvn test -Dtest=AwxProjectServiceRemoteIT -Dspring.profiles.active=remotetest
 * Set credentials with: -DAWX_USERNAME=username -DAWX_PASSWORD=password
 */
@SpringBootTest
@ActiveProfiles("remotetest")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Share state between test methods
class AwxProjectServiceRemoteIT {

    private static final Logger log = LoggerFactory.getLogger(AwxProjectServiceRemoteIT.class);
    
    @Autowired
    private AwxClient awxClient;
    
    // Shared test state
    private String testProjectName;
    private Integer createdProjectId;
    private List<ProjectInfo> initialProjects;
    
    @BeforeAll
    void setup() {
        // Generate a unique project name for this test run
        testProjectName = "integration-test-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Starting remote integration tests with project name: {}", testProjectName);
    }
    
    @AfterAll
    void cleanup() {
        log.info("Cleaning up after remote integration tests");
        // Final cleanup in case the delete test failed or was skipped
        if (createdProjectId != null) {
            try {
                Optional<ProjectInfo> projectExists = AwxProjectService.getProject(awxClient, createdProjectId);
                if (projectExists.isPresent()) {
                    log.warn("Final cleanup: Removing project that wasn't deleted during tests: {}", createdProjectId);
                    AwxProjectService.deleteProjectWithRetry(awxClient, createdProjectId);
                }
            } catch (Exception e) {
                log.error("Failed to clean up project: {}", createdProjectId, e);
            }
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("List existing projects")
    void shouldListProjects() {
        // When
        initialProjects = AwxProjectService.listProjects(awxClient);
        
        // Then
        assertThat(initialProjects).isNotNull();
        log.info("Found {} projects on remote AWX", initialProjects.size());
        initialProjects.forEach(p -> log.info("Project: {} (ID: {})", p.getName(), p.getId()));
    }
    
    @Test
    @Order(2)
    @DisplayName("Create a new project")
    void shouldCreateProject() {
        // Given
        Project project = new Project();
        project.setName(testProjectName);
        project.setDescription("Test project created by integration test at " + LocalDateTime.now());
        project.setScmType("git");
        project.setScmUrl("https://github.com/test/project.git");
        project.setScmBranch("main");
        
        // When
        ProjectInfo createdProject = AwxProjectService.createProject(awxClient, project);
        
        // Then
        assertThat(createdProject).isNotNull();
        assertThat(createdProject.getId()).isNotNull();
        assertThat(createdProject.getName()).isEqualTo(testProjectName);
        
        // Store the ID for later tests
        createdProjectId = createdProject.getId();
        log.info("Created test project with ID: {}", createdProjectId);
    }
    
    @Test
    @Order(3)
    @DisplayName("Get the created project")
    void shouldGetProject() {
        // Given
        assertThat(createdProjectId).isNotNull();
        
        // When
        Optional<ProjectInfo> project = AwxProjectService.getProject(awxClient, createdProjectId);
        
        // Then
        assertThat(project).isPresent();
        assertThat(project.get().getId()).isEqualTo(createdProjectId);
        assertThat(project.get().getName()).isEqualTo(testProjectName);
    }
    
    @Test
    @Order(4)
    @DisplayName("List projects after creation")
    void shouldListProjectsAfterCreation() {
        // When
        List<ProjectInfo> projects = AwxProjectService.listProjects(awxClient);
        
        // Then
        assertThat(projects).isNotNull();
        assertThat(projects.size()).isGreaterThanOrEqualTo(initialProjects.size() + 1);
        
        // Verify our project is in the list
        boolean found = projects.stream()
            .anyMatch(p -> p.getId().equals(createdProjectId) && p.getName().equals(testProjectName));
        assertThat(found).isTrue();
    }
    
    @Test
    @Order(5)
    @DisplayName("Delete the created project")
    void shouldDeleteProject() {
        // Given
        assertThat(createdProjectId).isNotNull();
        
        // When
        boolean deleted = AwxProjectService.deleteProjectWithRetry(awxClient, createdProjectId);
        
        // Then
        assertThat(deleted).isTrue();
        
        // Verify project is gone
        Optional<ProjectInfo> project = AwxProjectService.getProject(awxClient, createdProjectId);
        assertThat(project).isEmpty();
    }
    
    @Test
    @Order(6)
    @DisplayName("List projects after deletion")
    void shouldListProjectsAfterDeletion() {
        // When
        List<ProjectInfo> projects = AwxProjectService.listProjects(awxClient);
        
        // Then
        assertThat(projects).isNotNull();
        assertThat(projects.size()).isEqualTo(initialProjects.size());
        
        // Verify our project is not in the list
        boolean found = projects.stream()
            .anyMatch(p -> p.getId().equals(createdProjectId) && p.getName().equals(testProjectName));
        assertThat(found).isFalse();
    }
    
    @Test
    void contextLoads() {
        // This test simply checks that the client can be autowired
        // and the application context loads successfully
        assertThat(awxClient).isNotNull();
    }
}
