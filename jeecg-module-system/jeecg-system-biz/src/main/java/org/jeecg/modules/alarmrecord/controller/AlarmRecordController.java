package org.jeecg.modules.alarmrecord.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.alarmrecord.converter.AlarmRecordConverter;
import org.jeecg.modules.alarmrecord.dto.AlarmRecordDto;
import org.jeecg.modules.alarmrecord.entity.AlarmRecord;
import org.jeecg.modules.alarmrecord.entity.Algorithm;
import org.jeecg.modules.alarmrecord.entity.Camera;
import org.jeecg.modules.alarmrecord.service.IAlarmRecordService;
import org.jeecg.modules.alarmrecord.service.IAlgorithmService;
import org.jeecg.modules.alarmrecord.service.ICameraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: t_alarm_record
 * @Author: jeecg-boot
 * @Date: 2024-03-09
 * @Version: V1.0
 */
@Api(tags = "报警列表")
@RestController
@RequestMapping("/alarmRecord")
@Slf4j
public class AlarmRecordController extends JeecgController<AlarmRecord, IAlarmRecordService> {
    @Autowired
    private IAlarmRecordService alarmRecordService;

    @Autowired
    private ICameraService cameraService;

    @Autowired
    private IAlgorithmService algorithmService;


    /**
     * 分页列表查询
     *
     * @param alarmRecord
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "t_alarm_record-分页列表查询")
    @ApiOperation(value = "t_alarm_record-分页列表查询", notes = "t_alarm_record-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<?>> queryPageList(AlarmRecord alarmRecord,
                                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                          HttpServletRequest req) {
        QueryWrapper<AlarmRecord> queryWrapper = QueryGenerator.initQueryWrapper(alarmRecord, req.getParameterMap());
        queryWrapper.orderByDesc("gmt_create");
        Page<AlarmRecord> page = new Page<>(pageNo, pageSize);
        IPage<AlarmRecord> pageList = alarmRecordService.page(page, queryWrapper);
        if (ObjectUtils.isEmpty(pageList.getRecords())) {
            return Result.OK(pageList);
        }
        List<AlarmRecordDto> responseList = pageList.getRecords().stream().map(m -> {
                    try {
                        AlarmRecordDto alarmRecordDto = AlarmRecordConverter.INSTANCE.transformOut(m);
                        alarmRecordDto.setSceneName("");
                        Camera camera = cameraService.getById(m.getCameraId());
                        alarmRecordDto.setCameraName(ObjectUtils.isEmpty(camera) ? "" : camera.getCameraName());
                        Algorithm algorithm = algorithmService.getById(m.getAlgoId());
                        alarmRecordDto.setAlgoName(ObjectUtils.isEmpty(algorithm) ? "" : algorithm.getName());
                        return alarmRecordDto;
                    } catch (Exception e) {
                        throw new JeecgBootException(e);
                    }
                }
        ).collect(Collectors.toList());
        IPage<AlarmRecordDto> responsePage = new Page<>(pageNo, pageSize);
        responsePage.setPages(pageList.getPages());
        responsePage.setTotal(pageList.getTotal());
        responsePage.setSize(pageList.getTotal());
        responsePage.setRecords(responseList);
        return Result.OK(responsePage);
    }

    /**
     * 添加
     *
     * @param alarmRecord
     * @return
     */
    @AutoLog(value = "t_alarm_record-添加")
    @ApiOperation(value = "t_alarm_record-添加", notes = "t_alarm_record-添加")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_alarm_record:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AlarmRecord alarmRecord) {
        alarmRecordService.save(alarmRecord);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param alarmRecord
     * @return
     */
    @AutoLog(value = "t_alarm_record-编辑")
    @ApiOperation(value = "t_alarm_record-编辑", notes = "t_alarm_record-编辑")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_alarm_record:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AlarmRecord alarmRecord) {
        alarmRecordService.updateById(alarmRecord);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "t_alarm_record-通过id删除")
    @ApiOperation(value = "t_alarm_record-通过id删除", notes = "t_alarm_record-通过id删除")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_alarm_record:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        alarmRecordService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "t_alarm_record-批量删除")
    @ApiOperation(value = "t_alarm_record-批量删除", notes = "t_alarm_record-批量删除")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_alarm_record:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.alarmRecordService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "t_alarm_record-通过id查询")
    @ApiOperation(value = "t_alarm_record-通过id查询", notes = "t_alarm_record-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AlarmRecord> queryById(@RequestParam(name = "id", required = true) String id) {
        AlarmRecord alarmRecord = alarmRecordService.getById(id);
        if (alarmRecord == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(alarmRecord);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param alarmRecord
     */
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_alarm_record:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AlarmRecord alarmRecord) {
        return super.exportXls(request, alarmRecord, AlarmRecord.class, "t_alarm_record");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_alarm_record:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AlarmRecord.class);
    }

}
