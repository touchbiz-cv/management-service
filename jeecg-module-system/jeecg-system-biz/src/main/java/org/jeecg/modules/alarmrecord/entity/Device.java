package org.jeecg.modules.alarmrecord.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: t_device
 * @Author: jeecg-boot
 * @Date: 2024-03-09
 * @Version: V1.0
 */
@Data
@TableName("t_device")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "t_device对象", description = "t_device")
public class Device implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "id")
    private Integer id;

    /**
     * 设备名称
     */
    @Excel(name = "设备名称", width = 15)
    @ApiModelProperty(value = "设备名称")
    private String name;
    /**
     * 设备序列号
     */
    @Excel(name = "设备序列号", width = 15)
    @ApiModelProperty(value = "设备序列号")
    private String seriesNumber;
    /**
     * 设备类型,默认为0, 0为拉流截图设备
     */
    @Excel(name = "设备类型,默认为0, 0为拉流截图设备", width = 15)
    @ApiModelProperty(value = "设备类型,默认为0, 0为拉流截图设备")
    private Integer aiType;
    /**
     * 设备状态字段，0为未启用状态，1为启用状态
     */
    @Excel(name = "设备状态字段，0为未启用状态，1为启用状态", width = 15)
    @ApiModelProperty(value = "设备状态字段，0为未启用状态，1为启用状态")
    private Integer status;
    /**
     * 所属场站等信息，要分配以后才会有该部分信息
     */
    @Excel(name = "所属场站等信息，要分配以后才会有该部分信息", width = 15)
    @ApiModelProperty(value = "所属场站等信息，要分配以后才会有该部分信息")
    private Integer stationId;
    /**
     * 设备型号
     */
    @Excel(name = "设备型号", width = 15)
    @ApiModelProperty(value = "设备型号")
    private String model;
    /**
     * 制造商
     */
    @Excel(name = "制造商", width = 15)
    @ApiModelProperty(value = "制造商")
    private String manufacturer;
    /**
     * 设备ip
     */
    @Excel(name = "设备ip", width = 15)
    @ApiModelProperty(value = "设备ip")
    private String ip;
    /**
     * 是否在线
     */
    @Excel(name = "是否在线", width = 15)
    @ApiModelProperty(value = "是否在线")
    private String online;
    /**
     * 最后在线时间
     */
    @Excel(name = "最后在线时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "最后在线时间")
    private Date lastOnlineTime;
    /**
     * 附加信息
     */
    @Excel(name = "附加信息", width = 15)
    @ApiModelProperty(value = "附加信息")
    private String info;

    /**
     * 最大相机数量
     */
    @Excel(name = "最大相机数量", width = 15)
    @ApiModelProperty(value = "最大相机数量")
    private Integer maxCameraNum;

    /**
     * 是否标记删除 （默认0正常，1已删除）
     */
    @TableLogic
    @Excel(name = "是否标记删除", width = 15)
    @ApiModelProperty(value = "是否标记删除")
    private Integer deleted;

    /**
     * 创建人
     */
    @Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
    private String createBy;
    /**
     * 更新人
     */
    @Excel(name = "修改人", width = 15)
    @ApiModelProperty(value = "更新人")
    private String updateBy;

    /**
     * 创建时间
     */
    @Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date gmtCreate;
    /**
     * 修改时间
     */
    @Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
    private Date gmtModify;

    /**
     * 公钥
     */
    @Excel(name = "公钥", width = 15)
    @ApiModelProperty(value = "公钥")
    private String publicKey;
    /**
     * 私钥
     */
    @Excel(name = "私钥", width = 15)
    @ApiModelProperty(value = "私钥")
    private String privateKey;
}
