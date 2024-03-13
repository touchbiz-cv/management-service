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
import org.jeecg.modules.alarmrecord.entity.CameraAlgoAssign;
import org.jeecg.modules.alarmrecord.service.ICameraAlgoAssignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: t_camera_algo_assign
 * @Author: jeecg-boot
 * @Date: 2024-03-09
 * @Version: V1.0
 */
@Api(tags = "相机算法")
@RestController
@RequestMapping("/api/cameraAlgoAssign")
@Slf4j
public class CameraAlgoAssignController extends JeecgController<CameraAlgoAssign, ICameraAlgoAssignService> {
    @Autowired
    private ICameraAlgoAssignService cameraAlgoAssignService;

    /**
     * 分页列表查询
     *
     * @param cameraAlgoAssign
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "t_camera_algo_assign-分页列表查询")
    @ApiOperation(value = "t_camera_algo_assign-分页列表查询", notes = "t_camera_algo_assign-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<CameraAlgoAssign>> queryPageList(CameraAlgoAssign cameraAlgoAssign,
                                                         @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                         HttpServletRequest req) {
        QueryWrapper<CameraAlgoAssign> queryWrapper = QueryGenerator.initQueryWrapper(cameraAlgoAssign, req.getParameterMap());
        Page<CameraAlgoAssign> page = new Page<CameraAlgoAssign>(pageNo, pageSize);
        IPage<CameraAlgoAssign> pageList = cameraAlgoAssignService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param cameraAlgoAssign
     * @return
     */
    @AutoLog(value = "t_camera_algo_assign-添加")
    @ApiOperation(value = "t_camera_algo_assign-添加", notes = "t_camera_algo_assign-添加")
//    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera_algo_assign:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody CameraAlgoAssign cameraAlgoAssign) {
        cameraAlgoAssignService.save(cameraAlgoAssign);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param cameraAlgoAssign
     * @return
     */
    @AutoLog(value = "t_camera_algo_assign-编辑")
    @ApiOperation(value = "t_camera_algo_assign-编辑", notes = "t_camera_algo_assign-编辑")
//    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera_algo_assign:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody CameraAlgoAssign cameraAlgoAssign) {
        cameraAlgoAssignService.updateById(cameraAlgoAssign);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "t_camera_algo_assign-通过id删除")
    @ApiOperation(value = "t_camera_algo_assign-通过id删除", notes = "t_camera_algo_assign-通过id删除")
//    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera_algo_assign:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        cameraAlgoAssignService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "t_camera_algo_assign-批量删除")
    @ApiOperation(value = "t_camera_algo_assign-批量删除", notes = "t_camera_algo_assign-批量删除")
//    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera_algo_assign:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.cameraAlgoAssignService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "t_camera_algo_assign-通过id查询")
    @ApiOperation(value = "t_camera_algo_assign-通过id查询", notes = "t_camera_algo_assign-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<CameraAlgoAssign> queryById(@RequestParam(name = "id", required = true) String id) {
        CameraAlgoAssign cameraAlgoAssign = cameraAlgoAssignService.getById(id);
        if (cameraAlgoAssign == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(cameraAlgoAssign);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param cameraAlgoAssign
     */
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera_algo_assign:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CameraAlgoAssign cameraAlgoAssign) {
        return super.exportXls(request, cameraAlgoAssign, CameraAlgoAssign.class, "t_camera_algo_assign");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera_algo_assign:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CameraAlgoAssign.class);
    }

}
