package org.jeecg.modules.monitor.application.impl;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.config.K8sConfig;
import org.jeecg.modules.monitor.application.K8sApplication;
import org.jeecg.modules.monitor.vo.PodVersion;
import org.jeecg.modules.monitor.vo.PodVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author jiangyan
 */
@Slf4j
@Service
public class K8sApplicationImpl implements K8sApplication {

    @Value("${spring.profiles.active}")
    private String profile;

    private final String serviceName = "management-service";

    private final KubernetesClient client;

    private final K8sConfig k8sConfig;

    public K8sApplicationImpl(KubernetesClient client, K8sConfig k8sConfig) {
        this.client = client;
        this.k8sConfig = k8sConfig;
    }

    @Override
    public List<Pod> podList() {
        return client.pods().inNamespace(profile).list().getItems();
    }

    @Override
    public Optional<Pod> getPodByName(String name) {
        var items = podList();
        return items.stream().filter(pod ->
        {
            // 由于是kubernetes的job创建的pod，所以pod名会在job名称后面自动加上一串字母，用"-"连接，所以比较时需要去掉后面一串
            var podName = pod.getMetadata().getName();
            return name.equals(podName);

        }).findAny();
    }

    @Override
    public boolean deletePod(String name) {
        var podOptional = getPodByName(name);
        if (podOptional.isEmpty()) {
            throw new JeecgBootException("不存在该Pod:" + name);
        }
        var pod = podOptional.get();
        var containers = pod.getSpec().getContainers();
        String serviceName = null;
        if (!CollectionUtils.isEmpty(containers)) {
            var container = containers.get(0);
            if (!canDelete(pod)) {
                throw new JeecgBootException("此Pod不允许进行删除操作。pod名称:" + name);
            }
            serviceName = container.getName();
        }
        assert serviceName != null;
        Integer replicas = replicas(serviceName);
        if (replicas > 1) {
            client.pods().delete(pod);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            flexble(serviceName, replicas - 1);
            return true;
        } else {
            throw new JeecgBootException("该服务实例数不足,无法删除" + name);
        }
    }

    private Optional<Deployment> findDeploymentByServiceName(String serviceName) {
        return client.apps().deployments().inNamespace(profile).list().getItems()
                .stream().filter(x -> x.getMetadata().getName().equals(serviceName)).findAny();
    }


    @Override
    public boolean resetPod(String name) {
        var podOptional = getPodByName(name);
        if (podOptional.isEmpty()) {
            throw new JeecgBootException("不存在该Pod:" + name);
        }
        client.pods().delete(podOptional.get());
        return true;
    }

    @Override
    public String readLogByPod(String namespace, String podName, int lines) {
        String podLogs = client.pods().inNamespace(namespace).withName(podName)
                .tailingLines(lines).getLog();
        log.info("podLogs:{}", podLogs);
        return podLogs;
    }

    @Override
    public boolean canDelete(Pod pod) {
        return true;
    }

    @Override
    public boolean canUpgrade(Pod pod) {
        var imageContainer = getCurrentImage();
        var containers = pod.getSpec().getContainers();
        if (!CollectionUtils.isEmpty(containers)) {
            var container = containers.get(0);
            var image = container.getImage();
            var params = image.split(":");
            var originParams = imageContainer.split(":");
            //只有业务服务才支持升级操作
            return params[0].equals(originParams[0]) && !params[1].equals(originParams[1])
                    && !container.getName().equals(serviceName);
        }
        return false;
    }

    @Override
    public Consumer<PodVo> fillConsumer() {
        return pod -> {

            pod.setCanDelete(canDelete(pod));
            pod.setCanUpgrade(canUpgrade(pod));
        };
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

    @Override
    public PodVersion getVersion(Pod pod) {
        var containers = pod.getSpec().getContainers();
        if (!CollectionUtils.isEmpty(containers)) {
            PodVersion podVersion = new PodVersion();
            var container = containers.get(0);
            var image = container.getImage();
            var params = image.split(":");
            podVersion.setName(container.getName());
            podVersion.setVersion(params[1]);
            return podVersion;
        }
        return null;
    }


    @Override
    public void flexble(String serviceName, int replicas) {
        var deploy = findDeploymentByServiceName(serviceName);

        deploy.ifPresent(x -> {
            x.getSpec().setReplicas(replicas);
            x = client.apps().deployments().replace(x);
            log.info("x:{}", x);
        });

//        var deploy = findReplicaSetByServiceName(serviceName);
//
//        deploy.ifPresent(x->{
//            x.getSpec().setReplicas(replicas);
//            x = client.apps().replicaSets().create(x);
//            log.info("x:{}", x);
//        });
    }

    @Override
    public Integer replicas(String serviceName) {
        var deploy = findDeploymentByServiceName(serviceName);
        log.info("serviceName:{}, deploy:{}", serviceName, deploy.get());
        return deploy.map(x -> x.getSpec().getReplicas()).orElse(0);
    }


    private String getCurrentImage() {
        var optional = findDeploymentByServiceName(serviceName);
        if (optional.isEmpty()) {
            log.warn("找不到该Deployment:" + serviceName);
            throw new JeecgBootException("找不到该Deployment:" + serviceName);
        }
        var container = optional.get().getSpec().getTemplate().getSpec().getContainers().get(0);
        return container.getImage();
    }

}
