package org.jeecg.modules.alarmrecord.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: t_algorithm
 * @Author: jeecg-boot
 * @Date:   2024-03-09
 * @Version: V1.0
 */
@Data
@TableName("t_algorithm")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="t_algorithm对象", description="t_algorithm")
public class Algorithm implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private Integer id;
	/**算法名称*/
	@Excel(name = "算法名称", width = 15)
    @ApiModelProperty(value = "算法名称")
    private String name;
	/**算法英文名称*/
	@Excel(name = "算法英文名称", width = 15)
    @ApiModelProperty(value = "算法英文名称")
    private String enName;
	/**算法位置*/
	@Excel(name = "算法位置", width = 15)
    @ApiModelProperty(value = "算法位置")
    private String code;
	/**算法类型，0为通用算法，1为流程作业算法，2为事件类型*/
	@Excel(name = "算法类型，0为通用算法，1为流程作业算法，2为事件类型", width = 15)
    @ApiModelProperty(value = "算法类型，0为通用算法，1为流程作业算法，2为事件类型")
    private Integer type;
	/**算法每秒需要的频次*/
	@Excel(name = "算法每秒需要的频次", width = 15)
    @ApiModelProperty(value = "算法每秒需要的频次")
    private BigDecimal alarmInterval;
	/**算法发送的mq指定routingkey*/
	@Excel(name = "算法发送的mq指定routingkey", width = 15)
    @ApiModelProperty(value = "算法发送的mq指定routingkey")
    private String routingKey;
	/**以服务方式调用的接口地址*/
	@Excel(name = "以服务方式调用的接口地址", width = 15)
    @ApiModelProperty(value = "以服务方式调用的接口地址")
    private String url;
	/**0*/
	@Excel(name = "0", width = 15)
    @ApiModelProperty(value = "0")
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
	/**帧数，取多少张*/
	@Excel(name = "帧数，取多少张", width = 15)
    @ApiModelProperty(value = "帧数，取多少张")
    private Integer frameNumber;
	/**检测频率,间隔多少秒*/
	@Excel(name = "检测频率,间隔多少秒", width = 15)
    @ApiModelProperty(value = "检测频率,间隔多少秒")
    private Integer frequency;

	/**扩展数据，采用json格式*/
	@Excel(name = "扩展数据，采用json格式", width = 15)
    @ApiModelProperty(value = "扩展数据，采用json格式")
    private String extend;
}
