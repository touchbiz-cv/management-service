package org.jeecg.modules.alarmrecord.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.alarmrecord.dto.DeviceDto;
import org.jeecg.modules.alarmrecord.dto.DeviceUpdateDto;
import org.jeecg.modules.alarmrecord.entity.Device;
import org.jeecg.modules.alarmrecord.enums.StatusEnum;
import org.jeecg.modules.alarmrecord.service.IDeviceService;
import org.jeecg.modules.infrastructure.converter.basic.DeviceConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * @Description: t_device
 * @Author: jeecg-boot
 * @Date: 2024-03-09
 * @Version: V1.0
 */
@Api(tags = "设备")
@RestController
@RequestMapping("/api/device")
@Slf4j
public class DeviceController extends JeecgController<Device, IDeviceService> {
    @Autowired
    private IDeviceService deviceService;

    /**
     * 分页列表查询
     *
     * @param device
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "t_device-分页列表查询", notes = "t_device-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<Device>> queryPageList(Device device,
                                               @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                               HttpServletRequest req) {
        LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjectUtils.isNotEmpty(device.getName()), Device::getName, device.getName());
        queryWrapper.eq(ObjectUtils.isNotEmpty(device.getStatus()), Device::getStatus, device.getStatus());
        queryWrapper.orderByDesc(Device::getGmtCreate);
        Page<Device> page = new Page<Device>(pageNo, pageSize);
        IPage<Device> pageList = deviceService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param deviceDto
     * @return
     */
    @AutoLog(value = "t_device-添加")
    @ApiOperation(value = "t_device-添加", notes = "t_device-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody @Validated DeviceDto deviceDto) {
        if (!Pattern.matches("^[a-zA-Z0-9]*$", deviceDto.getSeriesNumber())) {
            return Result.error("设备序列号格式有误，只能输入字母+数字");
        }
        LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Device::getSeriesNumber, deviceDto.getSeriesNumber());
        if (ObjectUtils.isNotEmpty(deviceService.getOne(queryWrapper))) {
            return Result.error("设备序列号已存在,请查证");
        }
        Device device = DeviceConverter.INSTANCE.transformIn(deviceDto);
        deviceService.save(device);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param deviceDto
     * @return
     */
    @AutoLog(value = "t_device-编辑")
    @ApiOperation(value = "t_device-编辑", notes = "t_device-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody @Validated DeviceUpdateDto deviceDto) {
        if (!Pattern.matches("^[a-zA-Z0-9]*$", deviceDto.getSeriesNumber())) {
            return Result.error("设备序列号格式有误，只能输入字母+数字");
        }
        LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Device::getSeriesNumber, deviceDto.getSeriesNumber());
        Device deviceServiceOne = deviceService.getOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(deviceServiceOne) && !deviceServiceOne.getId().equals(deviceDto.getId())) {
            return Result.error("设备序列号已存在,请查证");
        }
        Device device = DeviceConverter.INSTANCE.transformUpdateIn(deviceDto);
        deviceService.updateById(device);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "t_device-通过id删除")
    @ApiOperation(value = "t_device-通过id删除", notes = "t_device-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        Device device = deviceService.getById(id);
        if (ObjectUtils.isEmpty(device)) {
            return Result.error("设备不存在,请查证");
        }
        if (StatusEnum.ON.getValue().equals(device.getStatus())) {
            return Result.error("设备启用中,不能删除");
        }
        deviceService.removeById(id);
        return Result.OK("删除成功!");
    }


    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "t_device-通过id查询", notes = "t_device-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<Device> queryById(@RequestParam(name = "id", required = true) String id) {
        Device device = deviceService.getById(id);
        if (device == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(device);
    }

}
