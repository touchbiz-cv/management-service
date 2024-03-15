package org.jeecg.modules.monitor.application;


import io.fabric8.kubernetes.api.model.Pod;
import org.jeecg.modules.monitor.vo.PodVersion;
import org.jeecg.modules.monitor.vo.PodVo;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author jiangyan
 */
public interface K8sApplication {

    List<Pod> podList();

    /**
     * 根据容器name获取Pod
     *
     * @param name
     * @return
     */
    Optional<Pod> getPodByName(String name);

    boolean deletePod(String name);

    boolean resetPod(String name);

    /**
     * 读取pod的最新n行日志
     *
     * @param namespace
     * @param podName
     * @param lines
     * @return
     */
    String readLogByPod(String namespace, String podName, int lines);

    /**
     * 判断pod是否可以删除
     *
     * @param pod
     * @return
     */
    boolean canDelete(Pod pod);

    /**
     * 判断pod是否可以升级
     *
     * @param pod
     * @return
     */
    boolean canUpgrade(Pod pod);

    /**
     * @return
     */
    Consumer<PodVo> fillConsumer();

    /**
     * @param nodeName
     * @return
     */
    String getNodeUuid(String nodeName);

    /**
     * 获取节点的uuid
     *
     * @return
     */
    String getUuidByDefaultNode();

    /**
     * 获取pod版本
     *
     * @param pod
     * @return
     */
    PodVersion getVersion(Pod pod);

    /**
     * 对实例进行伸缩处理
     *
     * @param serviceName
     * @param replicas
     */
    void flexble(String serviceName, int replicas);

    Integer replicas(String serviceNam);
}
