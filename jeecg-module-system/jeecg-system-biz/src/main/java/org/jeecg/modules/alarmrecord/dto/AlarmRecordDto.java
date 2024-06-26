package org.jeecg.modules.alarmrecord.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: t_alarm_record
 * @Author: jeecg-boot
 * @Date:   2024-03-09
 * @Version: V1.0
 */
@Data
public class AlarmRecordDto implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
    @ApiModelProperty(value = "id")
    private Integer id;
	/**场景id*/
	@Excel(name = "场景id", width = 15)
    @ApiModelProperty(value = "场景id")
    private Integer sceneId;

    /**场景名称*/
    @Excel(name = "场景名称", width = 15)
    @ApiModelProperty(value = "场景名称")
    private String sceneName;

	/**相机id*/
	@Excel(name = "相机id", width = 15)
    @ApiModelProperty(value = "相机id")
    private Integer cameraId;
	/**相机名称*/
	@Excel(name = "相机名称", width = 15)
    @ApiModelProperty(value = "相机名称")
    private String cameraName;
	/**算法id*/
	@Excel(name = "算法id", width = 15)
    @ApiModelProperty(value = "算法id")
    private Integer algoId;
    /**算法名称*/
    @Excel(name = "算法名称", width = 15)
    @ApiModelProperty(value = "算法名称")
    private String algoName;
	/**优先级*/
	@Excel(name = "优先级", width = 15)
    @ApiModelProperty(value = "优先级")
    private Integer priority;
	/**报警时间*/
	@Excel(name = "报警时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "报警时间")
    private Date alarmTime;
	/**告警标注画框的图片地址*/
	@Excel(name = "告警标注画框的图片地址", width = 15)
    @ApiModelProperty(value = "告警标注画框的图片地址")
    private String alarmImageDraw;
	/**告警原始图片地址*/
	@Excel(name = "告警原始图片地址", width = 15)
    @ApiModelProperty(value = "告警原始图片地址")
    private String originalImage;
	/**告警原始协议内容*/
	@Excel(name = "告警原始协议内容", width = 15)
    @ApiModelProperty(value = "告警原始协议内容")
    private String originalContent;
	/**报警状态*/
	@Excel(name = "报警状态", width = 15)
    @ApiModelProperty(value = "报警状态")
    private Integer alarmState;
	/**报警结果*/
	@Excel(name = "报警结果", width = 15)
    @ApiModelProperty(value = "报警结果")
    private String alarmResult;
	/**协议版本*/
	@Excel(name = "协议版本", width = 15)
    @ApiModelProperty(value = "协议版本")
    private String streamVersion;
	/**是否可用*/
	@Excel(name = "是否可用", width = 15)
    @ApiModelProperty(value = "是否可用")
    private String status;
	/**是否标记删除*/
	@Excel(name = "是否标记删除", width = 15)
    @ApiModelProperty(value = "是否标记删除")
    private String deleted;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
    private Integer creator;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
    @ApiModelProperty(value = "修改人")
    private Integer modifier;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
    private Date gmtCreate;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "修改时间")
    private Date gmtModify;
}
