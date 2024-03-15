package org.jeecg.modules.license.application;


import org.jeecg.modules.license.dto.License;
import org.jeecg.modules.license.dto.LicenseActivationRequest;
import org.jeecg.modules.license.dto.LicenseStatusResponse;

import java.util.Optional;

/**
 * @author jiangyan
 */
public interface LicenseApplication {

    /**
     * 获取license状态以及详细信息的接口
     *
     * @return
     */

    LicenseStatusResponse getStatus();

    /**
     * 激活操作
     *
     * @param requst
     */
    void activation(LicenseActivationRequest requst);

    /**
     * 获取当前系统的license
     *
     * @return
     */
    Optional<License> getLicense();
}
