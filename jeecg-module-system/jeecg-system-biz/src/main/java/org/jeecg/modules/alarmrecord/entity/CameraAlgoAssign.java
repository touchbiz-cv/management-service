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
 * @Description: t_camera_algo_assign
 * @Author: jeecg-boot
 * @Date: 2024-03-09
 * @Version: V1.0
 */
@Data
@TableName("t_camera_algo_assign")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "t_camera_algo_assign对象", description = "t_camera_algo_assign")
public class CameraAlgoAssign implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "id")
    private Integer id;
    /**
     * 相机id
     */
    @Excel(name = "相机id", width = 15)
    @ApiModelProperty(value = "相机id")
    private Integer cameraId;
    /**
     * 算法id
     */
    @Excel(name = "算法id", width = 15)
    @ApiModelProperty(value = "算法id")
    private Integer algoId;
    /**
     * 场景id
     */
    @Excel(name = "场景id", width = 15)
    @ApiModelProperty(value = "场景id")
    private Integer sceneId;
    /**
     * 检测帧率，最小是1帧，最大是5000
     */
    @Excel(name = "检测帧率，最小是1帧，最大是5000", width = 15)
    @ApiModelProperty(value = "检测帧率，最小是1帧，最大是5000")
    private Integer frameRate;
    /**
     * 检测区域的设置
     */
    @Excel(name = "检测区域的设置", width = 15)
    @ApiModelProperty(value = "检测区域的设置")
    private String region;
    /**
     * 是否启用, 0是停用，1是启用
     */
    @Excel(name = "是否启用, 0是停用，1是启用", width = 15)
    @ApiModelProperty(value = "是否启用, 0是停用，1是启用")
    private String status;
    /**
     * 是否标记删除
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
}
