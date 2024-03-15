package org.jeecg.modules.license.dto;


import lombok.Data;
import org.jeecg.modules.license.application.LicenseExtraModel;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * License生成类需要的参数
 */
@Data
public class License implements Serializable {

    @Serial
    private static final long serialVersionUID = -7793154252684580872L;
    /**
     * 证书subject
     */
    private String subject;

    /**
     * 证书生效时间
     */
    private LocalDate issuedTime = LocalDate.now();

    /**
     * 证书失效时间
     */
    private LocalDate expiryTime;

    /**
     * 描述信息
     */
    private String description = "";

    /**
     * 额外的服务器硬件校验信息
     */
    private LicenseExtraModel licenseExtraModel;
}
