package org.jeecg.modules.license.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author jiangyan
 */
@Data
public class LicenseActivationRequest {

    @ApiModelProperty(value = "license文件的base64编码")
    private String file;

}
