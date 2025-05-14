package de.wolkenzentrale.operator.awx.interfaces.service;

import de.wolkenzentrale.operator.awx.interfaces.dto.Project;
import de.wolkenzentrale.operator.awx.interfaces.dto.ProjectInfo;
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
    private AwxProjectService projectService;
    
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
                Optional<ProjectInfo> projectExists = projectService.getProject(createdProjectId);
                if (projectExists.isPresent()) {
                    log.warn("Final cleanup: Removing project that wasn't deleted during tests: {}", createdProjectId);
                    projectService.deleteProjectWithRetry(createdProjectId);
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
        initialProjects = projectService.listProjects();
        
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
        Project newProject = new Project();
        newProject.setName(testProjectName);
        newProject.setDescription("Remote integration test project created at " + LocalDateTime.now());
        newProject.setScmType("git");
        newProject.setScmUrl("https://github.com/ansible/awx-operator.git");
        newProject.setScmBranch("devel");
        
        // When
        ProjectInfo createdProject = projectService.createProject(newProject);
        
        // Then
        assertThat(createdProject).isNotNull();
        assertThat(createdProject.getId()).isNotNull();
        createdProjectId = createdProject.getId();
        log.info("Created test project with ID: {}", createdProjectId);
        
        assertThat(createdProject.getName()).isEqualTo(testProjectName);
        assertThat(createdProject.getScmType()).isEqualTo("git");
        assertThat(createdProject.getScmUrl()).isEqualTo("https://github.com/ansible/awx-operator.git");
        assertThat(createdProject.getScmBranch()).isEqualTo("devel");
    }
    
    @Test
    @Order(3)
    @DisplayName("Retrieve the created project")
    void shouldGetCreatedProject() {
        // Precondition check
        assertThat(createdProjectId).isNotNull()
            .withFailMessage("Test sequence error: No project ID from previous test");
            
        // When
        Optional<ProjectInfo> retrievedProject = projectService.getProject(createdProjectId);
        
        // Then
        assertThat(retrievedProject).isPresent();
        assertThat(retrievedProject.get().getId()).isEqualTo(createdProjectId);
        assertThat(retrievedProject.get().getName()).isEqualTo(testProjectName);
        log.info("Retrieved project status: {}", retrievedProject.get().getStatus());
        
        // Verify that the project appears in the list of all projects
        List<ProjectInfo> currentProjects = projectService.listProjects();
        assertThat(currentProjects).isNotNull();
        assertThat(currentProjects)
            .filteredOn(p -> p.getId().equals(createdProjectId))
            .hasSize(1);
    }
    
    @Test
    @Order(4)
    @DisplayName("Delete the project")
    void shouldDeleteCreatedProject() {
        // Precondition check
        assertThat(createdProjectId).isNotNull()
            .withFailMessage("Test sequence error: No project ID from previous test");
            
        log.info("Deleting test project with ID: {}", createdProjectId);

        // When
        boolean deleted = projectService.deleteProjectWithRetry(createdProjectId);
        
        // Then
        assertThat(deleted).isTrue();
        log.info("Successfully deleted test project with ID: {}", createdProjectId);
        
        // Verify deletion
        Optional<ProjectInfo> deletedProject = projectService.getProject(createdProjectId);
        assertThat(deletedProject).isEmpty();
        
        // Verify project is removed from the list
        List<ProjectInfo> finalProjects = projectService.listProjects();
        assertThat(finalProjects).isNotNull();
        assertThat(finalProjects)
            .filteredOn(p -> p.getName().equals(testProjectName))
            .isEmpty();
        
        // Verify we have the expected project count
        assertThat(finalProjects.size()).isEqualTo(initialProjects.size());
    }
}
