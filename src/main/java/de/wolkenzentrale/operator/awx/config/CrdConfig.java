package de.wolkenzentrale.operator.awx.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.wolkenzentrale.operator.awx.model.crd.awx.AwxTypes;

@Slf4j
@Configuration
/**
 * Configuration class that registers Kubernetes Custom Resource Definition (CRD) API clients.
 * This class is responsible for creating and configuring the API clients that interact with
 * custom resources defined in the Kubernetes cluster.
 * 
 * It provides beans for:
 * 1. AwxProject CRD API client - for managing AWX Project custom resources
 * 2. AwxInstance CRD API client - for managing AWX Instance custom resources
 * 
 * These clients allow the operator to perform CRUD operations on the custom resources
 * using the Kubernetes API.
 */
public class CrdConfig {

    private static final String API_GROUP = "wolkenzentrale.de";
    private static final String API_VERSION = "v1alpha1";
    private static final String PLURAL_AWX_PROJECTS = "awxprojects";
    private static final String PLURAL_AWX_INSTANCES = "awxinstances";
    
    @Bean
    public GenericKubernetesApi<AwxTypes.AwxProject, AwxTypes.AwxProjectList> awxProjectApi(ApiClient apiClient) {
        log.info("🌟 Registering AwxProject CRD API client");
        return new GenericKubernetesApi<>(
                AwxTypes.AwxProject.class,
                AwxTypes.AwxProjectList.class,
                API_GROUP,
                API_VERSION,
                PLURAL_AWX_PROJECTS,
                apiClient
        );
    }
    
    @Bean
    public GenericKubernetesApi<AwxTypes.AwxInstance, AwxTypes.AwxInstanceList> awxInstanceApi(ApiClient apiClient) {
        log.info("🌟 Registering AwxInstance CRD API client");
        return new GenericKubernetesApi<>(
                AwxTypes.AwxInstance.class,
                AwxTypes.AwxInstanceList.class,
                API_GROUP,
                API_VERSION,
                PLURAL_AWX_INSTANCES,
                apiClient
        );
    }
} 