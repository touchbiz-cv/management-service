package org.jeecg.modules.alarmrecord.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Description: t_device
 * @Author: jeecg-boot
 * @Date: 2024-03-09
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "t_device对象", description = "t_device")
public class DeviceUpdateDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @NotNull(message = "设备名称不能为空")
    @ApiModelProperty(value = "id")
    private Integer id;
    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    @ApiModelProperty(value = "设备名称")
    private String name;
    /**
     * 设备序列号
     */
    @NotBlank(message = "设备序列号不能为空")
    @ApiModelProperty(value = "设备序列号")
    private String seriesNumber;
    /**
     * 设备类型,默认为0, 0为拉流截图设备
     */
    @NotNull(message = "设备类型不能为空")
    @ApiModelProperty(value = "设备类型,默认为0, 0为拉流截图设备")
    private Integer aiType;
    /**
     * 设备状态字段，0为未启用状态，1为启用状态
     */
    @NotNull(message = "设备状态字段不能为空")
    @ApiModelProperty(value = "设备状态字段，0为未启用状态，1为启用状态")
    private Integer status;

    /**
     * 最大相机数量
     */
//    @NotNull(message = "最大相机数量不能为空")
    @ApiModelProperty(value = "最大相机数量")
    private Integer maxCameraNum;

}
