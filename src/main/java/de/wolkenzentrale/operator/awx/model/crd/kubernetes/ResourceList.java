package de.wolkenzentrale.operator.awx.model.crd.kubernetes;

import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1ListMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic Kubernetes resource list for AWX operator resources.
 * @param <T> The type of resource in the list
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceList<T extends KubernetesObject> implements KubernetesListObject {
    private String apiVersion;
    private String kind;
    private V1ListMeta metadata;
    private List<T> items = new ArrayList<>();
} 