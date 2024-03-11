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
import org.jeecg.modules.alarmrecord.entity.Camera;
import org.jeecg.modules.alarmrecord.service.ICameraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: t_camera
 * @Author: jeecg-boot
 * @Date: 2024-03-09
 * @Version: V1.0
 */
@Api(tags = "相机")
@RestController
@RequestMapping("/api/camera")
@Slf4j
public class CameraController extends JeecgController<Camera, ICameraService> {
    @Autowired
    private ICameraService cameraService;

    /**
     * 分页列表查询
     *
     * @param camera
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "t_camera-分页列表查询")
    @ApiOperation(value = "t_camera-分页列表查询", notes = "t_camera-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<Camera>> queryPageList(Camera camera,
                                               @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                               HttpServletRequest req) {
        QueryWrapper<Camera> queryWrapper = QueryGenerator.initQueryWrapper(camera, req.getParameterMap());
        Page<Camera> page = new Page<Camera>(pageNo, pageSize);
        IPage<Camera> pageList = cameraService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param camera
     * @return
     */
    @AutoLog(value = "t_camera-添加")
    @ApiOperation(value = "t_camera-添加", notes = "t_camera-添加")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody Camera camera) {
        cameraService.save(camera);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param camera
     * @return
     */
    @AutoLog(value = "t_camera-编辑")
    @ApiOperation(value = "t_camera-编辑", notes = "t_camera-编辑")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody Camera camera) {
        cameraService.updateById(camera);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "t_camera-通过id删除")
    @ApiOperation(value = "t_camera-通过id删除", notes = "t_camera-通过id删除")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        cameraService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "t_camera-批量删除")
    @ApiOperation(value = "t_camera-批量删除", notes = "t_camera-批量删除")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.cameraService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "t_camera-通过id查询")
    @ApiOperation(value = "t_camera-通过id查询", notes = "t_camera-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<Camera> queryById(@RequestParam(name = "id", required = true) String id) {
        Camera camera = cameraService.getById(id);
        if (camera == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(camera);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param camera
     */
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Camera camera) {
        return super.exportXls(request, camera, Camera.class, "t_camera");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Camera.class);
    }

}
