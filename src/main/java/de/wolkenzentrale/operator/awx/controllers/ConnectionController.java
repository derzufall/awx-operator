package de.wolkenzentrale.operator.awx.controllers;

import de.wolkenzentrale.operator.awx.client.ClientFactory;
import de.wolkenzentrale.operator.awx.client.ClientRegistry;
import de.wolkenzentrale.operator.awx.interfaces.awx.client.AwxClient;
import de.wolkenzentrale.operator.awx.model.common.Connection;
import de.wolkenzentrale.operator.awx.model.crd.status.AwxConnectionStatus;
import de.wolkenzentrale.operator.awx.model.crd.kubernetes.StatusCondition;
import de.wolkenzentrale.operator.awx.model.api.VersionInfo;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Controller for reconciling AWX Connection resources.
 * Manages the lifecycle of AWX clients based on Connection CRDs.
 */
@Slf4j
@Component
public class ConnectionController {

    private static final String GROUP = "wolkenzentrale.de";
    private static final String VERSION = "v1alpha1";
    private static final String PLURAL = "awxconnections";
    private static final String FINALIZER = "awx-connection.finalizers.wolkenzentrale.de";

    private final ClientFactory clientFactory;
    private final ClientRegistry clientRegistry;
    private final Tracer tracer;
    private final Meter meter;
    private final ApiClient apiClient;
    private final CustomObjectsApi customObjectsApi;

    // Metrics
    private final LongCounter reconciliationCounter;
    private final LongCounter reconciliationErrorCounter;
    private final LongCounter connectionSuccessCounter;
    private final LongCounter connectionErrorCounter;

    public ConnectionController(ClientFactory clientFactory, ClientRegistry clientRegistry, 
                              Tracer tracer, Meter meter, ApiClient apiClient) {
        this.clientFactory = clientFactory;
        this.clientRegistry = clientRegistry;
        this.tracer = tracer;
        this.meter = meter;
        this.apiClient = apiClient;
        this.customObjectsApi = new CustomObjectsApi(apiClient);

        // Initialize metrics
        this.reconciliationCounter = meter.counterBuilder("awx.connection.reconciliations")
            .setDescription("Total number of AWX connection reconciliations")
            .build();
        this.reconciliationErrorCounter = meter.counterBuilder("awx.connection.reconciliation.errors")
            .setDescription("Total number of AWX connection reconciliation errors")
            .build();
        this.connectionSuccessCounter = meter.counterBuilder("awx.connection.success")
            .setDescription("Total number of successful AWX connections")
            .build();
        this.connectionErrorCounter = meter.counterBuilder("awx.connection.errors")
            .setDescription("Total number of AWX connection errors")
            .build();
    }

    @Scheduled(fixedDelayString = "${awx.reconciliation.interval:30000}")
    public void reconcileAll() {
        try {
            // List all AWX connections across all namespaces
            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) customObjectsApi.listClusterCustomObject(
                GROUP, VERSION, PLURAL).execute();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            for (Map<String, Object> item : items) {
                String namespace = (String) ((Map<String, Object>) item.get("metadata")).get("namespace");
                String name = (String) ((Map<String, Object>) item.get("metadata")).get("name");
                reconcile(namespace, name, item);
            }
        } catch (Exception e) {
            log.error("Failed to reconcile AWX connections", e);
        }
    }

    private void reconcile(String namespace, String name, Map<String, Object> resource) {
        Span span = tracer.spanBuilder("reconcile")
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute("awx.connection.namespace", namespace)
            .setAttribute("awx.connection.name", name)
            .startSpan();
            
        try (Scope scope = span.makeCurrent()) {
            log.info("🔄 Reconciling AWX Connection: {}/{}", namespace, name);
            reconciliationCounter.add(1, Attributes.builder()
                .put("namespace", namespace)
                .put("name", name)
                .build());

            try {
                // Convert resource to Connection model
                Map<String, Object> spec = (Map<String, Object>) resource.get("spec");
                Connection connection = Connection.builder()
                    .name(name)
                    .namespace(namespace)
                    .url((String) spec.get("url"))
                    .username((String) spec.get("username"))
                    .password((String) spec.get("password"))
                    .insecureSkipTlsVerify((Boolean) spec.get("insecureSkipTlsVerify"))
                    .build();

                // Update client factory with this connection
                clientFactory.updateClients(Collections.singletonList(connection));

                // Get the client and verify connection by fetching version info
                AwxClient client = clientRegistry.getClient(namespace, name);
                if (client == null) {
                    throw new IllegalStateException("Client not found after creation");
                }

                // Get version info with retry
                VersionInfo versionInfo = client.getVersion()
                    .doOnNext(info -> {
                        log.info("📊 AWX version info: {}", info.getVersion());
                        span.setAttribute("awx.version", info.getVersion());
                        span.setAttribute("awx.instance_name", info.getInstanceName());
                    })
                    .doOnError(error -> {
                        log.error("❌ Error getting AWX version", error);
                        span.recordException(error);
                    })
                    .retry(3)
                    .block();

                if (versionInfo == null) {
                    throw new IllegalStateException("Failed to get AWX version info after retries");
                }

                // Update status with success and version info
                AwxConnectionStatus status = new AwxConnectionStatus();
                status.withConnected(versionInfo.getVersion());
                status.setObservedGeneration((Long) ((Map<String, Object>) resource.get("metadata")).get("generation"));
                status.setCondition(StatusCondition.create(
                    StatusCondition.Types.READY,
                    StatusCondition.Statuses.TRUE,
                    "Connected",
                    String.format("Successfully connected to AWX instance %s (version %s)", 
                        versionInfo.getInstanceName(), 
                        versionInfo.getVersion())
                ));

                // Update metrics
                connectionSuccessCounter.add(1, Attributes.builder()
                    .put("namespace", namespace)
                    .put("name", name)
                    .put("version", versionInfo.getVersion())
                    .build());

                // Update the resource with new status
                updateStatus(namespace, name, status);

            } catch (Exception e) {
                log.error("❌ Failed to reconcile AWX Connection: {}/{}", namespace, name, e);
                span.recordException(e);
                span.setStatus(StatusCode.ERROR, e.getMessage());
                
                reconciliationErrorCounter.add(1, Attributes.builder()
                    .put("namespace", namespace)
                    .put("name", name)
                    .put("error_type", e.getClass().getSimpleName())
                    .build());
                connectionErrorCounter.add(1, Attributes.builder()
                    .put("namespace", namespace)
                    .put("name", name)
                    .put("error_type", e.getClass().getSimpleName())
                    .build());

                // Update status with failure
                AwxConnectionStatus status = new AwxConnectionStatus();
                String errorMessage = e.getMessage();
                if (e.getCause() != null) {
                    errorMessage += " (caused by: " + e.getCause().getMessage() + ")";
                }

                status.withFailure("Failed to connect to AWX instance: " + errorMessage);
                status.setObservedGeneration((Long) ((Map<String, Object>) resource.get("metadata")).get("generation"));
                status.setCondition(StatusCondition.create(
                    StatusCondition.Types.READY,
                    StatusCondition.Statuses.FALSE,
                    "ConnectionFailed",
                    "Failed to connect to AWX instance: " + errorMessage
                ));

                // Update the resource with new status
                updateStatus(namespace, name, status);
            }
        } finally {
            span.end();
        }
    }

    private void updateStatus(String namespace, String name, AwxConnectionStatus status) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resource = (Map<String, Object>) customObjectsApi.getNamespacedCustomObject(
                GROUP, VERSION, namespace, PLURAL, name).execute();
            
            resource.put("status", status);
            
            customObjectsApi.replaceNamespacedCustomObject(
                GROUP, VERSION, namespace, PLURAL, name, resource).execute();
        } catch (Exception e) {
            log.error("Failed to update status for AWX Connection: {}/{}", namespace, name, e);
        }
    }

    public void cleanup(String namespace, String name) {
        Span span = tracer.spanBuilder("cleanup")
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute("awx.connection.namespace", namespace)
            .setAttribute("awx.connection.name", name)
            .startSpan();
            
        try (Scope scope = span.makeCurrent()) {
            log.info("🧹 Cleaning up AWX Connection: {}/{}", namespace, name);

            try {
                // The ClientFactory will handle removing the client when we call updateClients
                // with an empty list for this namespace
                clientFactory.updateClients(Collections.emptyList());
            } catch (Exception e) {
                log.error("❌ Failed to cleanup AWX Connection: {}/{}", namespace, name, e);
                span.recordException(e);
                span.setStatus(StatusCode.ERROR, e.getMessage());
            }
        } finally {
            span.end();
        }
    }
} 