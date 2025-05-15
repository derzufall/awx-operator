package de.wolkenzentrale.operator.awx.model.crd.kubernetes;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic Kubernetes resource for AWX operator
 * @param <S> The spec type
 * @param <T> The status type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KubernetesResource<S, T> implements KubernetesObject {
    private String apiVersion;
    private String kind;
    private V1ObjectMeta metadata;
    private S spec;
    private T status;
} 