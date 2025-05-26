package de.wolkenzentrale.operator.awx.controllers;

import de.wolkenzentrale.operator.awx.client.ClientFactory;
import de.wolkenzentrale.operator.awx.client.ClientRegistry;
import de.wolkenzentrale.operator.awx.interfaces.awx.client.AwxClient;
import de.wolkenzentrale.operator.awx.model.common.Connection;
import de.wolkenzentrale.operator.awx.model.crd.status.AwxConnectionStatus;
import de.wolkenzentrale.operator.awx.model.crd.kubernetes.StatusCondition;
import de.wolkenzentrale.operator.awx.model.api.VersionInfo;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.util.PatchUtils;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

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
    private final ObjectMapper objectMapper;
    private final CoreV1Api coreV1Api;

    // Metrics
    private final LongCounter reconciliationCounter;
    private final LongCounter reconciliationErrorCounter;
    private final LongCounter connectionSuccessCounter;
    private final LongCounter connectionErrorCounter;

    public ConnectionController(ClientFactory clientFactory, ClientRegistry clientRegistry, 
                              Tracer tracer, Meter meter, ApiClient apiClient, 
                              @Qualifier("kubernetesObjectMapper") ObjectMapper objectMapper) {
        this.clientFactory = clientFactory;
        this.clientRegistry = clientRegistry;
        this.tracer = tracer;
        this.meter = meter;
        this.apiClient = apiClient;
        this.customObjectsApi = new CustomObjectsApi(apiClient);
        this.coreV1Api = new CoreV1Api(apiClient);
        this.objectMapper = objectMapper;

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
                
                // Read password from Kubernetes secret
                String passwordSecretName = (String) spec.get("passwordSecretName");
                String passwordSecretKey = (String) spec.get("passwordSecretKey");
                String password = readPasswordFromSecret(namespace, passwordSecretName, passwordSecretKey);
                
                Connection connection = Connection.builder()
                    .name(name)
                    .namespace(namespace)
                    .url((String) spec.get("url"))
                    .username((String) spec.get("username"))
                    .password(password)
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

                // Update status with success and version info - matching documented format
                AwxConnectionStatus status = new AwxConnectionStatus();
                
                // Set base ResourceStatus fields
                status.setPhase("Succeeded");
                status.setMessage("Successfully connected to AWX instance");
                status.setLastUpdateTime(java.time.OffsetDateTime.now());
                status.setObservedGeneration(extractGeneration(resource));
                
                // Check if this is the first successful connection
                int currentFailedAttempts = getFailedAttemptCount(namespace, name);
                if (currentFailedAttempts > 0 || !hasFirstSuccessfulSync(namespace, name)) {
                    status.setFirstSuccessfulSync(java.time.OffsetDateTime.now());
                }
                
                // Set AwxConnection-specific fields
                status.setConnectionStatus("Connected");
                status.setAwxVersion(versionInfo.getVersion());
                status.setLastConnected(java.time.OffsetDateTime.now().toString());
                status.setFailedConnectionAttempts(0);
                
                // Set detailed condition
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
                log.info("✅ Successfully updated status for AWX Connection: {}/{} (version: {})", 
                    namespace, name, versionInfo.getVersion());

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

                // Update status with failure - matching documented format
                AwxConnectionStatus status = new AwxConnectionStatus();
                String errorMessage = e.getMessage();
                if (e.getCause() != null) {
                    errorMessage += " (caused by: " + e.getCause().getMessage() + ")";
                }

                // Set base ResourceStatus fields
                status.setPhase("Failed");
                status.setMessage("Failed to connect to AWX instance: " + errorMessage);
                status.setLastUpdateTime(java.time.OffsetDateTime.now());
                status.setObservedGeneration(extractGeneration(resource));
                
                // Preserve existing firstSuccessfulSync and awxVersion if they exist
                preserveExistingSuccessInfo(namespace, name, status);
                
                // Set AwxConnection-specific fields
                status.setConnectionStatus("Error");
                status.setFailedConnectionAttempts(getFailedAttemptCount(namespace, name) + 1);
                
                // Set detailed condition
                status.setCondition(StatusCondition.create(
                    StatusCondition.Types.READY,
                    StatusCondition.Statuses.FALSE,
                    "ConnectionFailed",
                    "Failed to connect to AWX instance: " + errorMessage
                ));

                // Update the resource with new status
                updateStatus(namespace, name, status);
                log.warn("⚠️ Updated failure status for AWX Connection: {}/{} (attempt: {})", 
                    namespace, name, status.getFailedConnectionAttempts());
            }
        } finally {
            span.end();
        }
    }

    private void updateStatus(String namespace, String name, AwxConnectionStatus status) {
        try {
            log.debug("🔄 Updating status for AWX Connection: {}/{}", namespace, name);
            
            // Create a merge patch with only the status field
            Map<String, Object> patchBody = new HashMap<>();
            patchBody.put("status", status);
            
            // Convert to JSON string for the patch
            String patchJson = objectMapper.writeValueAsString(patchBody);
            log.debug("📤 Sending patch JSON: {}", patchJson);
            
            // Use PatchUtils.patch() with buildCall pattern - working solution from GitHub issue
            PatchUtils.patch(
                Object.class,
                () -> customObjectsApi.patchNamespacedCustomObjectStatus(
                    GROUP, VERSION, namespace, PLURAL, name,
                    new V1Patch(patchJson)).buildCall(null),
                V1Patch.PATCH_FORMAT_JSON_MERGE_PATCH,
                apiClient
            );
                
            log.debug("✅ Status update completed for AWX Connection: {}/{}", namespace, name);
        } catch (Exception e) {
            log.error("❌ Failed to update status for AWX Connection: {}/{} - {}", namespace, name, e.getMessage(), e);
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

    /**
     * Safely extract generation from Kubernetes resource metadata.
     * Handles both Long and Double types that can come from JSON deserialization.
     */
    private Long extractGeneration(Map<String, Object> resource) {
        Object generationObj = ((Map<String, Object>) resource.get("metadata")).get("generation");
        if (generationObj instanceof Number) {
            return ((Number) generationObj).longValue();
        }
        return null;
    }

    /**
     * Get the current failed attempt count from the existing status.
     */
    private int getFailedAttemptCount(String namespace, String name) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resource = (Map<String, Object>) customObjectsApi.getNamespacedCustomObject(
                GROUP, VERSION, namespace, PLURAL, name).execute();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> status = (Map<String, Object>) resource.get("status");
            if (status != null && status.get("failedConnectionAttempts") != null) {
                Object attempts = status.get("failedConnectionAttempts");
                if (attempts instanceof Number) {
                    return ((Number) attempts).intValue();
                }
            }
        } catch (Exception e) {
            log.debug("Could not retrieve failed attempt count for {}/{}, defaulting to 0", namespace, name);
        }
        return 0;
    }

    /**
     * Check if the resource already has a firstSuccessfulSync timestamp.
     */
    private boolean hasFirstSuccessfulSync(String namespace, String name) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resource = (Map<String, Object>) customObjectsApi.getNamespacedCustomObject(
                GROUP, VERSION, namespace, PLURAL, name).execute();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> status = (Map<String, Object>) resource.get("status");
            if (status != null && status.get("firstSuccessfulSync") != null) {
                return true;
            }
        } catch (Exception e) {
            log.debug("Could not retrieve firstSuccessfulSync for {}/{}, assuming false", namespace, name);
        }
        return false;
    }

    private String readPasswordFromSecret(String namespace, String secretName, String secretKey) {
        try {
            V1Secret secret = new CoreV1Api(apiClient).readNamespacedSecret(secretName, namespace).execute();
            if (secret.getData() == null || secret.getData().get(secretKey) == null) {
                log.error("🔐 Secret key '{}' not found in secret '{}/{}'", secretKey, namespace, secretName);
                return null;
            }
            byte[] passwordBytes = secret.getData().get(secretKey);
            String password = new String(passwordBytes, StandardCharsets.UTF_8);
            log.info("🔐 Successfully read password from secret '{}/{}'", namespace, secretName);
            return password;
        } catch (Exception e) {
            log.error("❌ Failed to read password from secret: {}/{}", namespace, secretName, e);
            throw new RuntimeException("Failed to read password from secret: " + namespace + "/" + secretName, e);
        }
    }

    private void preserveExistingSuccessInfo(String namespace, String name, AwxConnectionStatus status) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resource = (Map<String, Object>) customObjectsApi.getNamespacedCustomObject(
                GROUP, VERSION, namespace, PLURAL, name).execute();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> existingStatus = (Map<String, Object>) resource.get("status");
            if (existingStatus != null) {
                if (existingStatus.get("firstSuccessfulSync") != null) {
                    status.setFirstSuccessfulSync(java.time.OffsetDateTime.parse(existingStatus.get("firstSuccessfulSync").toString()));
                }
                if (existingStatus.get("awxVersion") != null) {
                    status.setAwxVersion(existingStatus.get("awxVersion").toString());
                }
            }
        } catch (Exception e) {
            log.debug("Could not preserve existing success info for {}/{}, assuming no change", namespace, name);
        }
    }
} 