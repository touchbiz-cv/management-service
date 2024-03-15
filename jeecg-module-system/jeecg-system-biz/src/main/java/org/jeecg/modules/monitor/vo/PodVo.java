package org.jeecg.modules.monitor.vo;

import io.fabric8.kubernetes.api.model.Pod;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PodVo extends Pod {

    public PodVo(){
        this.canDelete = false;
        this.canUpgrade = false;
    }

    private boolean canUpgrade;

    private boolean canDelete;
}
