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

}
