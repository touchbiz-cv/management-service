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
import org.jeecg.modules.alarmrecord.entity.CameraAi;
import org.jeecg.modules.alarmrecord.service.ICameraAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: t_camera_ai
 * @Author: jeecg-boot
 * @Date: 2024-03-09
 * @Version: V1.0
 */
@Api(tags = "相机ai")
@RestController
@RequestMapping("/cameraAi")
@Slf4j
public class CameraAiController extends JeecgController<CameraAi, ICameraAiService> {
    @Autowired
    private ICameraAiService cameraAiService;

    /**
     * 分页列表查询
     *
     * @param cameraAi
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "t_camera_ai-分页列表查询")
    @ApiOperation(value = "t_camera_ai-分页列表查询", notes = "t_camera_ai-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<CameraAi>> queryPageList(CameraAi cameraAi,
                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                 HttpServletRequest req) {
        QueryWrapper<CameraAi> queryWrapper = QueryGenerator.initQueryWrapper(cameraAi, req.getParameterMap());
        Page<CameraAi> page = new Page<CameraAi>(pageNo, pageSize);
        IPage<CameraAi> pageList = cameraAiService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param cameraAi
     * @return
     */
    @AutoLog(value = "t_camera_ai-添加")
    @ApiOperation(value = "t_camera_ai-添加", notes = "t_camera_ai-添加")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera_ai:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody CameraAi cameraAi) {
        cameraAiService.save(cameraAi);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param cameraAi
     * @return
     */
    @AutoLog(value = "t_camera_ai-编辑")
    @ApiOperation(value = "t_camera_ai-编辑", notes = "t_camera_ai-编辑")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera_ai:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody CameraAi cameraAi) {
        cameraAiService.updateById(cameraAi);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "t_camera_ai-通过id删除")
    @ApiOperation(value = "t_camera_ai-通过id删除", notes = "t_camera_ai-通过id删除")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera_ai:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        cameraAiService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "t_camera_ai-批量删除")
    @ApiOperation(value = "t_camera_ai-批量删除", notes = "t_camera_ai-批量删除")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera_ai:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.cameraAiService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "t_camera_ai-通过id查询")
    @ApiOperation(value = "t_camera_ai-通过id查询", notes = "t_camera_ai-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<CameraAi> queryById(@RequestParam(name = "id", required = true) String id) {
        CameraAi cameraAi = cameraAiService.getById(id);
        if (cameraAi == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(cameraAi);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param cameraAi
     */
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera_ai:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CameraAi cameraAi) {
        return super.exportXls(request, cameraAi, CameraAi.class, "t_camera_ai");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_camera_ai:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CameraAi.class);
    }

}
