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
 * @Description: t_camera
 * @Author: jeecg-boot
 * @Date:   2024-03-09
 * @Version: V1.0
 */
@Data
@TableName("t_camera")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="t_camera对象", description="t_camera")
public class Camera implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private Integer id;
	/**所属场站等信息，要分配以后才会有该部分信息*/
	@Excel(name = "所属场站等信息，要分配以后才会有该部分信息", width = 15)
    @ApiModelProperty(value = "所属场站等信息，要分配以后才会有该部分信息")
    private Integer stationId;
	/**相机名称*/
	@Excel(name = "相机名称", width = 15)
    @ApiModelProperty(value = "相机名称")
    private String cameraName;
	/**相机类型（1：NVR，2：DVR，3：IPC,4:虚拟相机）*/
	@Excel(name = "相机类型（1：NVR，2：DVR，3：IPC,4:虚拟相机）", width = 15)
    @ApiModelProperty(value = "相机类型（1：NVR，2：DVR，3：IPC,4:虚拟相机）")
    private String cameraType;
	/**ip地址*/
	@Excel(name = "ip地址", width = 15)
    @ApiModelProperty(value = "ip地址")
    private String ip;
	/**控制端口*/
	@Excel(name = "控制端口", width = 15)
    @ApiModelProperty(value = "控制端口")
    private String controlPort;
	/**视频端口*/
	@Excel(name = "视频端口", width = 15)
    @ApiModelProperty(value = "视频端口")
    private String videoPort;
	/**协议*/
	@Excel(name = "协议", width = 15)
    @ApiModelProperty(value = "协议")
    private String agreement;
	/**通道名称*/
	@Excel(name = "通道名称", width = 15)
    @ApiModelProperty(value = "通道名称")
    private String channelName;
	/**用户名*/
	@Excel(name = "用户名", width = 15)
    @ApiModelProperty(value = "用户名")
    private String cameraUsername;
	/**密码*/
	@Excel(name = "密码", width = 15)
    @ApiModelProperty(value = "密码")
    private String cameraPassword;
	/**子码流地址*/
	@Excel(name = "子码流地址", width = 15)
    @ApiModelProperty(value = "子码流地址")
    private String subCodeStream;
	/**所属平台id*/
	@Excel(name = "所属平台id", width = 15)
    @ApiModelProperty(value = "所属平台id")
    private Integer platformId;
	/**所属平台中的相机唯一标识*/
	@Excel(name = "所属平台中的相机唯一标识", width = 15)
    @ApiModelProperty(value = "所属平台中的相机唯一标识")
    private String platformUnicode;
	/**rtsp地址*/
	@Excel(name = "rtsp地址", width = 15)
    @ApiModelProperty(value = "rtsp地址")
    private String rtspUrl;
	/**是否在线 1 在线 2未在线*/
	@Excel(name = "是否在线 1 在线 2未在线", width = 15)
    @ApiModelProperty(value = "是否在线 1 在线 2未在线")
    private Integer online;
	/**萤石相机验证码*/
	@Excel(name = "萤石相机验证码", width = 15)
    @ApiModelProperty(value = "萤石相机验证码")
    private String devVcode;
	/**萤石相机直播视频流*/
	@Excel(name = "萤石相机直播视频流", width = 15)
    @ApiModelProperty(value = "萤石相机直播视频流")
    private String rtmpAddress;
	/**萤石相机序列号*/
	@Excel(name = "萤石相机序列号", width = 15)
    @ApiModelProperty(value = "萤石相机序列号")
    private String devSerial;
	/**物理位置属性（1：固定，2：移动）*/
	@Excel(name = "物理位置属性（1：固定，2：移动）", width = 15)
    @ApiModelProperty(value = "物理位置属性（1：固定，2：移动）")
    private Integer locationType;
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
	/**截帧命令*/
	@Excel(name = "截帧命令", width = 15)
    @ApiModelProperty(value = "截帧命令")
    private String frameCommand;
	/**lastOnlineTime*/
	@Excel(name = "lastOnlineTime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "lastOnlineTime")
    private Date lastOnlineTime;
}
