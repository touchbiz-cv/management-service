package org.jeecg.modules.license.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author jiangyan
 */
@Data
public class LicenseStatusResponse {

    @ApiModelProperty(value = "序列号")
    private String seriesNo;

    @ApiModelProperty(value = "license状态, 0是待激活，1是激活状态,  -1是license过期, -2是license非法")
    private Integer status;

    @ApiModelProperty(value = "license详细信息")
    private License info;
}
