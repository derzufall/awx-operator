package de.wolkenzentrale.operator.awx.interfaces.awx.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import de.wolkenzentrale.operator.awx.model.api.ProjectInfo;
import de.wolkenzentrale.operator.awx.model.api.ProjectListResponse;
import de.wolkenzentrale.operator.awx.model.common.Project;
import reactor.core.publisher.Mono;

@HttpExchange("/api/v2")
public interface AwxClient {

    @GetExchange("/projects/")
    Mono<ProjectListResponse> listProjects();
    
    @GetExchange("/projects/{id}/")
    Mono<ProjectInfo> getProject(@PathVariable("id") Integer id);
    
    @PostExchange("/projects/")
    Mono<ProjectInfo> createProject(@RequestBody Project project);
    
    @DeleteExchange("/projects/{id}/")
    Mono<Void> deleteProject(@PathVariable("id") Integer id);
} 