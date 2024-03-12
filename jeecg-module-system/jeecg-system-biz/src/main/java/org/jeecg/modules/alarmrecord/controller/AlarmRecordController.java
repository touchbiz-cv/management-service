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
import org.jeecg.modules.alarmrecord.dto.AlarmRecordStatusDto;
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
import java.net.URLEncoder;
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
@RequestMapping("/api/alarmRecord")
@Slf4j
public class AlarmRecordController extends JeecgController<AlarmRecord, IAlarmRecordService> {

    @Autowired
    private HttpServletRequest request;


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
    //@AutoLog(value = "告警分页列表查询")
    @ApiOperation(value = "告警分页列表查询", notes = "告警分页列表查询")
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

        String imageUrl = request.getScheme() + "://" + request.getHeader("Host") + "/api/images/getImage?filename=";
        List<AlarmRecordDto> responseList = pageList.getRecords().stream().map(m -> {
                    try {
                        AlarmRecordDto alarmRecordDto = AlarmRecordConverter.INSTANCE.transformOut(m);
                        alarmRecordDto.setSceneName("");
                        Camera camera = cameraService.getById(m.getCameraId());
                        alarmRecordDto.setCameraName(ObjectUtils.isEmpty(camera) ? "" : camera.getCameraName());
                        Algorithm algorithm = algorithmService.getById(m.getAlgoId());
                        alarmRecordDto.setAlgoName(ObjectUtils.isEmpty(algorithm) ? "" : algorithm.getName());
                        if (ObjectUtils.isNotEmpty(m.getAlarmImageDraw())) {
                            alarmRecordDto.setAlarmImageDraw(imageUrl + URLEncoder.encode(m.getAlarmImageDraw()));
                        }
                        if (ObjectUtils.isNotEmpty(m.getOriginalImage())) {
                            alarmRecordDto.setOriginalImage(imageUrl + URLEncoder.encode(m.getOriginalImage()));
                        }
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
     * 编辑
     *
     * @param dto
     * @return
     */
    @AutoLog(value = "告警标记处理")
    @ApiOperation(value = "告警标记处理", notes = "告警标记处理")
    @RequiresPermissions("org.jeecg.modules.alarmrecord:t_alarm_record:edit")
    @RequestMapping(value = "/editStatus", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> editStatus(@RequestBody AlarmRecordStatusDto dto) {
        AlarmRecord alarmRecord = new AlarmRecord();
        alarmRecord.setId(dto.getId());
        alarmRecord.setAlarmState(dto.getAlarmState());
        alarmRecordService.updateById(alarmRecord);
        return Result.OK("告警标记处理成功!");
    }


    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "t_alarm_record-通过id查询")
    @ApiOperation(value = "通过id查询告警详情", notes = "通过id查询告警详情")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id, HttpServletRequest req) {
        AlarmRecord alarmRecord = alarmRecordService.getById(id);
        if (alarmRecord == null) {
            return Result.error("未找到对应数据");
        }
        try {
            String imageUrl = request.getScheme() + "://" + request.getHeader("Host") + "/api/images/getImage?filename=";
            AlarmRecordDto alarmRecordDto = AlarmRecordConverter.INSTANCE.transformOut(alarmRecord);
            alarmRecordDto.setSceneName("");
            Camera camera = cameraService.getById(alarmRecord.getCameraId());
            alarmRecordDto.setCameraName(ObjectUtils.isEmpty(camera) ? "" : camera.getCameraName());
            Algorithm algorithm = algorithmService.getById(alarmRecord.getAlgoId());
            alarmRecordDto.setAlgoName(ObjectUtils.isEmpty(algorithm) ? "" : algorithm.getName());
            if (ObjectUtils.isNotEmpty(alarmRecord.getAlarmImageDraw())) {
                alarmRecordDto.setAlarmImageDraw(imageUrl + URLEncoder.encode(alarmRecord.getAlarmImageDraw()));
            }
            if (ObjectUtils.isNotEmpty(alarmRecord.getOriginalImage())) {
                alarmRecordDto.setOriginalImage(imageUrl + URLEncoder.encode(alarmRecord.getOriginalImage()));
            }
            return Result.OK(alarmRecordDto);
        } catch (Exception e) {
            throw new JeecgBootException(e);
        }
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
