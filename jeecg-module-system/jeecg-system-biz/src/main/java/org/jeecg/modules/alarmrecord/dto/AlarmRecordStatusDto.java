package org.jeecg.modules.alarmrecord.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Description: t_alarm_record
 * @Author: jeecg-boot
 * @Date: 2024-03-09
 * @Version: V1.0
 */
@Data
public class AlarmRecordStatusDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @NotNull(message = "参数id不能为空")
    @ApiModelProperty(value = "id")
    private Integer id;

    /**
     * 报警状态
     */
    @NotNull(message = "参数报警状态不能为空")
    @ApiModelProperty(value = "报警状态")
    private Integer alarmState;
}
