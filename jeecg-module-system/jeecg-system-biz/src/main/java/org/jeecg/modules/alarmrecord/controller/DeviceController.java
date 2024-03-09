package org.jeecg.modules.alarmrecord.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.alarmrecord.entity.Device;
import org.jeecg.modules.alarmrecord.service.IDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: t_device
 * @Author: jeecg-boot
 * @Date: 2024-03-09
 * @Version: V1.0
 */
@Api(tags = "设备")
@RestController
@RequestMapping("/device")
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
    //@AutoLog(value = "t_device-分页列表查询")
    @ApiOperation(value = "t_device-分页列表查询", notes = "t_device-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<Device>> queryPageList(Device device,
                                               @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                               HttpServletRequest req) {
        QueryWrapper<Device> queryWrapper = QueryGenerator.initQueryWrapper(device, req.getParameterMap());
        Page<Device> page = new Page<Device>(pageNo, pageSize);
        IPage<Device> pageList = deviceService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param device
     * @return
     */
    @AutoLog(value = "t_device-添加")
    @ApiOperation(value = "t_device-添加", notes = "t_device-添加")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_device:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody Device device) {
        deviceService.save(device);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param device
     * @return
     */
    @AutoLog(value = "t_device-编辑")
    @ApiOperation(value = "t_device-编辑", notes = "t_device-编辑")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_device:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody Device device) {
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
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_device:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        deviceService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "t_device-批量删除")
    @ApiOperation(value = "t_device-批量删除", notes = "t_device-批量删除")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_device:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.deviceService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "t_device-通过id查询")
    @ApiOperation(value = "t_device-通过id查询", notes = "t_device-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<Device> queryById(@RequestParam(name = "id", required = true) String id) {
        Device device = deviceService.getById(id);
        if (device == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(device);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param device
     */
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_device:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Device device) {
        return super.exportXls(request, device, Device.class, "t_device");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_device:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Device.class);
    }

}
