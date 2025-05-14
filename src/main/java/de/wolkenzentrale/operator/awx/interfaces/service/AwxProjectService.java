package de.wolkenzentrale.operator.awx.interfaces.service;

import de.wolkenzentrale.operator.awx.interfaces.client.AwxClient;
import de.wolkenzentrale.operator.awx.interfaces.dto.Project;
import de.wolkenzentrale.operator.awx.interfaces.dto.ProjectInfo;
import de.wolkenzentrale.operator.awx.util.RetryMono;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwxProjectService {

    private final AwxClient awxClient;

    public List<ProjectInfo> listProjects() {
        log.info("Requesting projects from AWX");
        return awxClient.listProjects()
                .doOnNext(response -> log.info("Retrieved {} projects from AWX", response.getCount()))
                .flatMapIterable(response -> response.getResults())
                .doOnError(error -> log.error("Error retrieving projects from AWX", error))
                .collectList()
                .block();
    }
    
    public Optional<ProjectInfo> getProject(Integer id) {
        log.info("Requesting project with ID {} from AWX", id);
        try {
            return Optional.ofNullable(
                awxClient.getProject(id)
                    .doOnNext(project -> log.info("Retrieved project: {}", project.getName()))
                    .doOnError(error -> log.error("Error retrieving project with ID {}", id, error))
                    .block()
            );
        } catch (Exception e) {
            log.error("Failed to retrieve project with ID {}", id, e);
            return Optional.empty();
        }
    }
    
    public ProjectInfo createProject(Project project) {
        log.info("Creating project in AWX: {}", project.getName());
             
        return awxClient.createProject(project)
                .doOnNext(createdProject -> log.info("Successfully created project with ID: {}", createdProject.getId()))
                .doOnError(error -> log.error("Error creating project: {}", project.getName(), error))
                .block();
    }
    
    public boolean deleteProject(Integer id) {
        log.info("Deleting project with ID {} from AWX", id);
        try {
            awxClient.deleteProject(id)
                .doOnSuccess(ignore -> log.info("Successfully deleted project with ID: {}", id))
                .doOnError(error -> log.error("Error deleting project with ID {}", id, error))
                .block();
            return true;
        } catch (Exception e) {
            log.error("Failed to delete project with ID {}", id, e);
            return false;
        }
    }

    public boolean deleteProjectWithRetry(Integer id) {
        log.info("Deleting project with ID {} from AWX", id);
        try {
            RetryMono.of(
                awxClient.deleteProject(id)
                    .doOnSuccess(ignore -> log.info("Successfully deleted project with ID: {}", id))
                    .doOnError(error -> log.error("Error deleting project with ID {}", id, error)),
                "delete project " + id
            ).run().block();
            return true;
        } catch (Exception e) {
            log.error("Failed to delete project with ID {}", id, e);
            return false;
        }
    }
}