package org.jeecg.modules.license.application;


import lombok.Data;

/**
 * 自定义需要校验的License参数，可以增加一些额外需要校验的参数，比如项目信息，ip地址信息等等，待完善
 *
 * @author jiangyan
 */
@Data
public class LicenseExtraModel {

    /**
     * 客户名称
     */
    private String customerName;
    /**
     * 用户数量
     */
    private Integer userAmount = 1;

    private Long usedUserAmount;

    /**
     * 产品数量
     */
    private Integer moduleAmount = 1;

    private Long uesdModuleAmount;

    /**
     * 数据容量，单位为TB
     */
    private Integer dataVolume = 1;

}


