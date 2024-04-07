package org.jeecg.modules.monitor.application.impl;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.config.K8sConfig;
import org.jeecg.modules.monitor.application.K8sApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author jiangyan
 */
@Slf4j
@Service
public class K8sApplicationImpl implements K8sApplication {

    @Value("${spring.profiles.active}")
    private String profile;

    private final KubernetesClient client;

    private final K8sConfig k8sConfig;

    public K8sApplicationImpl(KubernetesClient client, K8sConfig k8sConfig) {
        this.client = client;
        this.k8sConfig = k8sConfig;
    }

    private Optional<Deployment> findDeploymentByServiceName(String serviceName) {
        return client.apps().deployments().inNamespace(profile).list().getItems()
                .stream().filter(x -> x.getMetadata().getName().equals(serviceName)).findAny();
    }


    @Override
    public String getNodeUuid(String nodeName) {
        for (Node item : client.nodes().list().getItems()) {
            if (item.getMetadata().getName().equals(nodeName)) {
                return item.getMetadata().getUid();
            }
        }
        return null;
    }

    @Override
    public String getUuidByDefaultNode() {
        return getNodeUuid(k8sConfig.getNodeName());
    }


}
