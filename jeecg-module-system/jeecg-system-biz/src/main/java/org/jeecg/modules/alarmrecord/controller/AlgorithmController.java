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
import org.jeecg.modules.alarmrecord.entity.Algorithm;
import org.jeecg.modules.alarmrecord.service.IAlgorithmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: t_algorithm
 * @Author: jeecg-boot
 * @Date: 2024-03-09
 * @Version: V1.0
 */
@Api(tags = "算法")
@RestController
@RequestMapping("/algorithm")
@Slf4j
public class AlgorithmController extends JeecgController<Algorithm, IAlgorithmService> {
    @Autowired
    private IAlgorithmService algorithmService;

    /**
     * 分页列表查询
     *
     * @param algorithm
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "t_algorithm-分页列表查询")
    @ApiOperation(value = "t_algorithm-分页列表查询", notes = "t_algorithm-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<Algorithm>> queryPageList(Algorithm algorithm,
                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                  HttpServletRequest req) {
        QueryWrapper<Algorithm> queryWrapper = QueryGenerator.initQueryWrapper(algorithm, req.getParameterMap());
        Page<Algorithm> page = new Page<Algorithm>(pageNo, pageSize);
        IPage<Algorithm> pageList = algorithmService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param algorithm
     * @return
     */
    @AutoLog(value = "t_algorithm-添加")
    @ApiOperation(value = "t_algorithm-添加", notes = "t_algorithm-添加")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_algorithm:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody Algorithm algorithm) {
        algorithmService.save(algorithm);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param algorithm
     * @return
     */
    @AutoLog(value = "t_algorithm-编辑")
    @ApiOperation(value = "t_algorithm-编辑", notes = "t_algorithm-编辑")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_algorithm:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody Algorithm algorithm) {
        algorithmService.updateById(algorithm);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "t_algorithm-通过id删除")
    @ApiOperation(value = "t_algorithm-通过id删除", notes = "t_algorithm-通过id删除")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_algorithm:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        algorithmService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "t_algorithm-批量删除")
    @ApiOperation(value = "t_algorithm-批量删除", notes = "t_algorithm-批量删除")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_algorithm:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.algorithmService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "t_algorithm-通过id查询")
    @ApiOperation(value = "t_algorithm-通过id查询", notes = "t_algorithm-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<Algorithm> queryById(@RequestParam(name = "id", required = true) String id) {
        Algorithm algorithm = algorithmService.getById(id);
        if (algorithm == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(algorithm);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param algorithm
     */
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_algorithm:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Algorithm algorithm) {
        return super.exportXls(request, algorithm, Algorithm.class, "t_algorithm");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_algorithm:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Algorithm.class);
    }

}
