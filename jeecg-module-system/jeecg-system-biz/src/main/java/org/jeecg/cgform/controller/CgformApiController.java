package org.jeecg.cgform.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.touchbiz.common.entity.exception.BizException;
import com.touchbiz.common.entity.exception.ParamException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.cgform.entity.CgFormEntity;
import org.jeecg.cgform.infrastructure.enums.TableTypeEnum;
import org.jeecg.cgform.service.CgformFieldService;
import org.jeecg.cgform.service.CgformHeadService;
import org.jeecg.cgform.service.impl.OnlCgformFieldServiceImpl;
import org.jeecg.cgform.service.impl.OnlCgformHeadServiceImpl;
import org.jeecg.cgform.service.impl.OnlineJoinQueryServiceImpl;
import org.jeecg.cgform.utils.DbConfigUtil;
import org.jeecg.cgform.utils.DbDmlGenerator;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.aspect.annotation.OnlineAuth;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.ModuleType;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;
import org.jeecg.modules.online.cgform.model.b;
import org.jeecg.modules.online.cgform.service.IOnlCgformHeadService;
import org.jeecg.modules.online.cgform.service.IOnlCgformSqlService;
import org.jeecg.modules.online.cgform.service.IOnlineService;
import org.jeecg.modules.online.config.d.e;
import org.jeecg.modules.online.config.exception.BusinessException;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.OracleSequenceMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.PostgresSequenceMaxValueIncrementer;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.jeecg.common.constant.CacheConstant.ONLINE_FORM;
import static org.jeecg.common.constant.CacheConstant.ONLINE_LIST;
import static org.jeecg.common.constant.CommonConstant.CHAR_BIG_N;
import static org.jeecg.common.constant.CommonConstant.CHAR_BIG_Y;
import static org.jeecg.common.constant.DataBaseConstant.DELETED_TABLE;
import static org.jeecg.common.constant.DataBaseConstant.ID;
import static org.jeecg.common.constant.SymbolConstant.COMMA;
import static org.jeecg.common.constant.SymbolConstant.SINGLE_QUOTATION_MARK;
import static org.jeecg.modules.data.constant.SqlConstant.EQUAL;

/**
 * @author jiangyan
 */
@Slf4j
@RestController
@RequestMapping({"/api/online/cgform/api"})
public class CgformApiController extends JeecgController<OnlCgformHead, IOnlCgformHeadService> {

    private final OnlCgformHeadServiceImpl  cgformHeadService;

    private final IOnlineService onlineService;

    private final CgformHeadService headService;

    private final CgformFieldService cgformFieldService;

    private final IOnlCgformSqlService onlCgformSqlService;

    private final OnlCgformFieldServiceImpl onlCgformFieldService;

    private final OnlineJoinQueryServiceImpl onlineJoinQueryService;

    @Value("${jeecg.path.upload}")
    private String upLoadPath;

    @Value("${jeecg.uploadType}")
    private String uploadType;


    public CgformApiController(OnlCgformHeadServiceImpl cgformHeadService, IOnlineService onlineService, CgformHeadService headService, CgformFieldService cgformFieldService, IOnlCgformSqlService onlCgformSqlService, OnlCgformFieldServiceImpl onlCgformFieldService, OnlineJoinQueryServiceImpl onlineJoinQueryService) {
        this.cgformHeadService = cgformHeadService;
        this.onlineService = onlineService;
        this.headService = headService;
        this.cgformFieldService = cgformFieldService;
        this.onlCgformSqlService = onlCgformSqlService;
        this.onlCgformFieldService = onlCgformFieldService;
        this.onlineJoinQueryService = onlineJoinQueryService;
    }

    @PostMapping({"/addAll"})
    public Result<?> addAll(@RequestBody CgFormEntity entity) {
        try {
            String tableName = entity.getHead().getTableName();
            if (DbConfigUtil.tableIsExist(tableName)) {
                return Result.error("数据库表[" + tableName + "]已存在,请从数据库导入表单");
            } else {
                if (TableTypeEnum.SLAVE.getType().equals(entity.getHead().getTableType())) {
                    if (oConvertUtils.isEmpty(entity.getHead().getRelationType())) {
                        return Result.error("附表必须选择映射关系！");
                    }

                    if (oConvertUtils.isEmpty(entity.getHead().getTabOrderNum())) {
                        return Result.error("附表必须填写排序序号！");
                    }
                }
                return cgformHeadService.addAllNew(entity);
            }
        }
        catch (BizException | ParamException err){
            return Result.error(err.getMessage());
        }
        catch (Exception var3) {
            log.error("CgformApiController.addAll发生异常：" + var3.getMessage(), var3);
            return Result.error("操作失败");
        }
    }


    //TODO 此接口耗时需要800ms，如何优化效率
    @PutMapping({"/editAll"})
    @CacheEvict(value = {ONLINE_LIST, ONLINE_FORM}, allEntries = true, beforeInvocation = true)
    public Result<?> editAll(@RequestBody CgFormEntity entity) {
        try {
            if (entity.getHead().getTableType().equals(TableTypeEnum.SLAVE.getType())) {
                if (oConvertUtils.isEmpty(entity.getHead().getRelationType())) {
                    return Result.error("附表必须选择映射关系！");
                }

                if (oConvertUtils.isEmpty(entity.getHead().getTabOrderNum())) {
                    return Result.error("附表必须填写排序序号！");
                }
            }
            return cgformHeadService.editAllNew(entity);
        }
        catch (BizException | ParamException err){
            return Result.error(err.getMessage());
        }
        catch (Exception var3) {
            log.error("CgformApiController.editAll发生异常：" + var3.getMessage(), var3);
            return Result.error("操作失败");
        }
    }

    @AutoLog(
            value = "online表单加载",
            module = ModuleType.ONLINE
    )
    @OnlineAuth("getFormItem")
    @GetMapping({"/getFormItem/{code}"})
    public Result<JSONObject> getFormItem(@PathVariable("code") String code) {
        OnlCgformHead head;

        try {
            head = getService().getTable(code);
        } catch (org.jeecg.modules.online.config.exception.a var8) {
            return Result.error("表不存在");
        }

        Result<JSONObject> var4 = new Result<>();
        LoginUser var5 = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        JSONObject var9 = this.onlineService.queryOnlineFormItem(head, var5.getUsername());
        var4.setResult(DbDmlGenerator.b(var9));
        var4.setOnlTable(head.getTableName());
        return var4;
    }

    @AutoLog(
            operateType = CommonConstant.OPERATE_TYPE_2,
            value = "online新增数据",
            module = ModuleType.ONLINE
    )
    @OnlineAuth("form")
    @PostMapping({"/form/{code}"})
    @CacheEvict(
            value = {"sys:cache:online:linkTable"},
            allEntries = true
    )
    public Result<String> formAdd(@PathVariable("code") String tableCode, @RequestBody JSONObject data) {
        Result<String> result = Result.OK();

        try {
            var saveEntity = cgformHeadService.saveManyFormData(tableCode, data);
            result.setResult(saveEntity.getId());
            result.setOnlTable(saveEntity.getTableName());
            result.setMessage("添加成功!");
        } catch (Exception var8) {
            log.error("CgformApiController.formAdd()发生异常：", var8);
            result.setSuccess(false);
            result.setMessage("保存失败，" + DbDmlGenerator.a(var8));
        }

        return result;
    }


    @AutoLog(operateType = CommonConstant.OPERATE_TYPE_3,
            value = "online修改数据",
            module = ModuleType.ONLINE )
    @OnlineAuth("form")
    @PutMapping({"/form/{code}"})
    @CacheEvict(value = {"sys:cache:online:linkTable"},allEntries = true)
    public Result<?> formEdit(@PathVariable("code") String code, @RequestBody JSONObject jsonObject) {
        try {
            String tableName = cgformHeadService.editManyFormData(code, jsonObject);
            Result<Object> result = Result.ok("修改成功！");
            result.setOnlTable(tableName);
            return result;
        } catch (Exception err) {
            log.error("CgformApiController.formEdit()发生异常：" + err.getMessage(), err);
            return Result.error("修改失败，" + DbDmlGenerator.a(err));
        }
    }

    @AutoLog(
            value = "online根据表名查询表单数据",
            module = ModuleType.ONLINE
    )
    @GetMapping({"/form/table_name/{tableName}/{dataId}"})
    public Result<?> formDataByName(@PathVariable("tableName") String tableName, @PathVariable("dataId") String dataId) {
        try {
            var head = headService.getByTableName(tableName);
            if (head == null) {
                return Result.error("OnlCgform tableName: " + tableName + " 不存在！");
            }
            SqlInjectionUtil.filterContent(dataId, SINGLE_QUOTATION_MARK);
            Result<?> result = formDataByCode(head.getId(), dataId);
            result.setOnlTable(tableName);
            return result;
        } catch (Exception err) {
            log.error("Online表单查询异常，" + err.getMessage(), err);
            return Result.error("查询失败，" + err.getMessage());
        }
    }

    @AutoLog(
            value = "online表单数据查询"
    )
    @GetMapping({"/form/{code}/{id}"})
    public Result<?> formDataByCode(@PathVariable("code") String code, @PathVariable(ID) String id) {
        try {
            SqlInjectionUtil.filterContent(id, SINGLE_QUOTATION_MARK);
            Map<String, Object> data = cgformHeadService.queryManyFormData(code, id);
            return Result.ok(DbDmlGenerator.a(data));
        } catch (Exception var4) {
            log.error("Online表单查询异常：" + var4.getMessage(), var4);
            return Result.error("查询失败，" + var4.getMessage());
        }
    }

    @PermissionData
    @OnlineAuth("getData")
    @GetMapping({"/getData/{code}"})
    public Result<Map<String, Object>> getData(@PathVariable("code") String code, HttpServletRequest var2) {
        Result<Map<String, Object>> result = new Result<>();
        OnlCgformHead cgformHead;
        try {
            cgformHead = cgformHeadService.getTable(code);
        } catch (org.jeecg.modules.online.config.exception.a var10) {
            result.error500("实体不存在");
            return result;
        }

        if (oConvertUtils.isEmpty(cgformHead.getPhysicId()) && CHAR_BIG_N.equals(cgformHead.getIsDbSynch())) {
            result.error500("NO_DB_SYNC");
            return result;
        }
        String var5 = var2.getParameter("linkTableSelectFields");
        if (oConvertUtils.isNotEmpty(var5)) {
            cgformHead.setSelectFieldString(var5);
        }
        Map<String,Object> var6;
        try {
            var params = DbDmlGenerator.a(var2);
            boolean isJoinQuery = DbDmlGenerator.a(cgformHead);
            if (isJoinQuery) {
                var6 = this.onlineJoinQueryService.pageList(cgformHead, params);
            } else {
                var6 = this.onlCgformFieldService.queryAutolistPage(cgformHead, params, null);
            }

            this.executeEnhanceList(cgformHead, var6);
            result.setResult(var6);
        } catch (Exception var9) {
            log.error(var9.getMessage(), var9);
            result.error500("数据库查询失败，" + var9.getMessage());
        }

        result.setOnlTable(cgformHead.getTableName());
        return result;
    }


    @OnlineAuth("importXls")
    @PostMapping({"/importXls/{code}"})
    public Result<?> importXls(@PathVariable("code") String code, HttpServletRequest var2) {
        Result<String> result = new Result<>();
        String var7 = "";
        String validateStatus = var2.getParameter("validateStatus");
        StringBuffer var9 = new StringBuffer();

        try {
            OnlCgformHead cgformHead = cgformHeadService.getById(code);
            if (cgformHead == null) {
                return Result.error("数据库不存在该表记录");
            }

            var fieldDOList = cgformFieldService.listFieldByHead(code);
            String var13 = var2.getParameter("isSingleTableImport");
            var var14 = DbDmlGenerator.e(new ArrayList<>(fieldDOList));
            if (oConvertUtils.isEmpty(var13) && cgformHead.getTableType().equals(TableTypeEnum.MASTER.getType()) && oConvertUtils.isNotEmpty(cgformHead.getSubTableStr())) {

                for (String subTableName : cgformHead.getSubTableStr().split(COMMA)) {
                    OnlCgformHead var19 = cgformHeadService.getOne((new LambdaQueryWrapper<OnlCgformHead>()).eq(OnlCgformHead::getTableName, subTableName));
                    if (var19 != null) {
                        var var20 = cgformFieldService.listFieldByHead(var19.getId());
                        var var21 = DbDmlGenerator.b(new ArrayList<>(var20), var19.getTableTxt());
                        if (CollectionUtils.isNotEmpty(var21)) {
                            var14.addAll(var21);
                        }
                    }
                }
            }

            JSONObject var49 = null;
            String var50 = var2.getParameter("foreignKeys");
            if (oConvertUtils.isNotEmpty(var50)) {
                var49 = JSONObject.parseObject(var50);
            }

            MultipartHttpServletRequest var51 = (MultipartHttpServletRequest)var2;
            Map<String, MultipartFile> fileMap = var51.getFileMap();
            DataSource var53 = SpringContextUtils.getApplicationContext().getBean(DataSource.class);
            String var54 = e.a(var53);

            for (Map.Entry<String, MultipartFile> stringMultipartFileEntry : fileMap.entrySet()) {
                MultipartFile var23 = stringMultipartFileEntry.getValue();
                ImportParams var24 = new ImportParams();
                var24.setImageList(var14);
                var24.setDataHanlder(new org.jeecg.modules.online.cgform.d.a(new ArrayList<>(fieldDOList), this.upLoadPath, this.uploadType));
                List<Map> var25 = ExcelImportUtil.importExcel(var23.getInputStream(), Map.class, var24);
                if (var25 != null) {
                    if (TableTypeEnum.SLAVE.getType().equals(cgformHead.getTableType()) && cgformHead.getRelationType() == 1 && var25.size() > 1) {
                        return Result.error("一对一的表只能导入一条数据!");
                    }

                    Object var26 = "";
                    ArrayList<Map<String,Object>> dataList = new ArrayList<>();

                    Map var29;
                    for (var var28 = var25.iterator(); var28.hasNext(); var29.put("$mainTable$id", var26)) {
                        var29 = var28.next();
                        boolean var30 = false;
                        HashMap<String,Object> var32 = new HashMap<>(5);
                        var var33 = var29.keySet().iterator();

                        String var34;
                        while (var33.hasNext()) {
                            var34 = (String) var33.next();
                            if (!var34.contains("$subTable$")) {
                                if (var34.contains("$mainTable$") && oConvertUtils.isNotEmpty(var29.get(var34).toString())) {
                                    var30 = true;
                                    var26 = this.generateId(cgformHead, var53, var54);
                                }

                                var32.put(var34.replace("$mainTable$", ""), var29.get(var34));
                            }
                        }

                        if (CHAR_BIG_Y.equals(cgformHead.getIsTree())) {
                            if (oConvertUtils.isEmpty(var32.get(cgformHead.getTreeParentIdField()))) {
                                var32.put(cgformHead.getTreeParentIdField(), "0");
                            }

                            if (oConvertUtils.isEmpty(var32.get(cgformHead.getTreeIdField()))) {
                                var32.put(cgformHead.getTreeIdField(), "0");
                            }
                        }

                        if (var30) {
                            var32.put(ID, var26);
                            dataList.add(var32);
                            var26 = var32.get(ID);
                        }

                        if (var49 != null) {
                            var33 = var49.keySet().iterator();

                            while (var33.hasNext()) {
                                var34 = (String) var33.next();
                                System.out.println(var34 + EQUAL + var49.getString(var34));
                                var32.put(var34, var49.getString(var34));
                            }
                        }
                    }

                    if (dataList.isEmpty()) {
                        result.setSuccess(false);
                        result.setMessage("导入失败，匹配的数据条数为零!");
                        return result;
                    }

                    if ("1".equals(validateStatus)) {
                        var var57 = this.onlCgformSqlService.saveOnlineImportDataWithValidate(cgformHead, new ArrayList<>(fieldDOList), dataList);
                        String var59 = var57.get("error");
                        var7 = var57.get("tip");
                        if (var59 != null && !var59.isEmpty()) {
                            var9.append(cgformHead.getTableTxt()).append("导入校验,").append(var7).append(",详情如下:\r\n").append(var59);
                        }
                    } else {
                        this.onlCgformSqlService.saveBatchOnlineTable(cgformHead, new ArrayList<>(fieldDOList), dataList);
                    }

                    if (oConvertUtils.isEmpty(var13) && TableTypeEnum.MASTER.getType().equals(cgformHead.getTableType()) && oConvertUtils.isNotEmpty(cgformHead.getSubTableStr())) {
                        String[] var58 = cgformHead.getSubTableStr().split(COMMA);

                        for (String var62 : var58) {
                            var head = cgformHeadService.getOne((new LambdaQueryWrapper<OnlCgformHead>()).eq(OnlCgformHead::getTableName, var62));
                            if (head != null) {
                                var doList = cgformFieldService.listFieldByHead(head.getId());
                                ArrayList<Map<String, Object>> var35 = new ArrayList<>();
                                String var36 = head.getTableTxt();

                                for (var o : var25) {
                                    boolean var39 = false;
                                    HashMap<String, Object> var40 = new HashMap<>(5);

                                    for (val cgformFieldDO : doList) {
                                        String mainTable = cgformFieldDO.getMainTable();
                                        String mainField = cgformFieldDO.getMainField();
                                        boolean isMainTable = cgformHead.getTableName().equals(mainTable) && oConvertUtils.isNotEmpty(mainField);
                                        String var46 = var36 + "_" + cgformFieldDO.getDbFieldTxt();
                                        if (isMainTable) {
                                            var40.put(cgformFieldDO.getDbFieldName(), o.get("$mainTable$" + mainField));
                                        }

                                        Object var47 = o.get("$subTable$" + var46);
                                        if (null != var47 && oConvertUtils.isNotEmpty(var47.toString())) {
                                            var39 = true;
                                            var40.put(cgformFieldDO.getDbFieldName(), var47);
                                        }
                                    }

                                    if (var39) {
                                        var40.put(ID, this.generateId(head, var53, var54));
                                        var35.add(var40);
                                    }
                                }

                                if (!var35.isEmpty()) {
                                    if ("1".equals(validateStatus)) {
                                        var var66 = this.onlCgformSqlService.saveOnlineImportDataWithValidate(head, new ArrayList<>(doList), var35);
                                        String var67 = var66.get("error");
                                        String var68 = var66.get("tip");
                                        if (var67 != null && !var67.isEmpty()) {
                                            var9.append(head.getTableTxt()).append("导入校验,").append(var68).append(",详情如下:\r\n").append(var67);
                                        }
                                    } else {
                                        this.onlCgformSqlService.saveBatchOnlineTable(head, new ArrayList<>(doList), var35);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    var7 = "识别模版数据错误";
                    log.error(var7);
                }
            }

            result.setSuccess(true);
            if ("1".equals(validateStatus) && !var9.isEmpty()) {
                String var56 = DbDmlGenerator.a(this.upLoadPath, cgformHead.getTableTxt(), var9);
                result.setResult(var56);
                result.setMessage(var7);
                result.setCode(201);
            } else {
                result.setMessage("导入成功!");
            }
        } catch (Exception var48) {
            result.setSuccess(false);
            result.setMessage(var48.getMessage());
            log.error(var48.getMessage(), var48);
        }

        return result;
    }


    @AutoLog(value = "online列表加载",module = ModuleType.ONLINE)
    @OnlineAuth("getColumns")
    @GetMapping({"/getColumns/{code}"})
    public Result<b> getColumns(@PathVariable("code") String code, HttpServletRequest var2) {
        Result<b> result = new Result<>();
        OnlCgformHead head;
        try {
            head = cgformHeadService.getTable(code);
        } catch (org.jeecg.modules.online.config.exception.a var8) {
            result.error500("实体不存在");
            return result;
        }

        String var5 = var2.getParameter("linkTableSelectFields");
        if (oConvertUtils.isNotEmpty(var5)) {
            head.setSelectFieldString(var5);
        }

        LoginUser var6 = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        b config = this.onlineService.queryOnlineConfig(head, var6.getUsername());
        config.setIsDesForm(head.getIsDesForm());
        config.setDesFormCode(head.getDesFormCode());
        result.setResult(config);
        result.setOnlTable(head.getTableName());
        return result;
    }

    @GetMapping({"/getQueryInfoVue3/{code}"})
    public Result<?> getQueryInfoVue3(@PathVariable("code") String var1) {
        try {
            JSONObject var2 = this.onlineService.getOnlineVue3QueryInfo(var1);
            return Result.ok(var2);
        } catch (Exception var3) {
            log.error("OnlCgformApiController.getQueryInfoVue3()发生异常：" + var3.getMessage(), var3);
            return Result.error("查询失败");
        }
    }

    @AutoLog(
            operateType = 4,
            value = "online删除数据",
            module = ModuleType.ONLINE
    )
    @OnlineAuth("form")
    @DeleteMapping({"/form/{code}/{id}"})
    public Result<?> deleteFormById(@PathVariable("code") String code, @PathVariable("id") String id) {
        OnlCgformHead cgformHead = cgformHeadService.getById(code);
        if (cgformHead == null) {
            return Result.error("实体不存在");
        }
        try {
            String parentIds = "";
            if (CHAR_BIG_Y.equals(cgformHead.getIsTree())) {
                id = this.onlCgformFieldService.queryTreeChildIds(cgformHead, id);
                parentIds = this.onlCgformFieldService.queryTreePids(cgformHead, id);
            }

            if (id.indexOf(COMMA) > 0) {
                if (!TableTypeEnum.MASTER.getType().equals(cgformHead.getTableType())) {
                    var tableName = cgformHead.getTableName();
                    //TODO 查找这张表是否有标记删除字段
                    var mainTableFields = cgformFieldService.listFieldByHead(cgformHead.getId(), true);
                    var existDeletedField = mainTableFields.stream().anyMatch(x-> DELETED_TABLE.equals(x.getDbFieldName()));
                    this.onlCgformFieldService.deleteAutoListById(tableName, id, existDeletedField);
                } else {
                    this.onlCgformFieldService.deleteAutoListMainAndSub(cgformHead, id);
                }

                if (CHAR_BIG_Y.equals(cgformHead.getIsTree())) {
                    var tableName = cgformHead.getTableName();
                    String var6 = cgformHead.getTreeIdField();

                    for (String var11 : parentIds.split(COMMA)) {
                        this.onlCgformFieldService.updateTreeNodeNoChild(tableName, var6, var11);
                    }
                }
            } else {
                cgformHeadService.deleteOneTableInfo(code, id);
            }
        } catch (Exception e) {
            log.error("OnlCgformApiController.formEdit()发生异常：" + e.getMessage(), e);
            return Result.error("删除失败," + e.getMessage());
        }

        Result<Object> result = Result.ok("删除成功!");
        result.setOnlTable(cgformHead.getTableName());
        return result;
    }


    private Object generateId(OnlCgformHead config, DataSource dataSource, String dbType) {
        String idType = config.getIdType();

        if (idType != null) {
            if ("UUID".equalsIgnoreCase(idType)) {
                return DbDmlGenerator.a();
            } else {
                String sequenceName = config.getIdSequence();
                DataFieldMaxValueIncrementer incrementer = null;

                if ("NATIVE".equalsIgnoreCase(idType) && dbType != null) {
                    if ("oracle".equalsIgnoreCase(dbType)) {
                        incrementer = new OracleSequenceMaxValueIncrementer(dataSource, "HIBERNATE_SEQUENCE");
                    } else if ("postgres".equalsIgnoreCase(dbType)) {
                        incrementer = new PostgresSequenceMaxValueIncrementer(dataSource, "HIBERNATE_SEQUENCE");
                    }
                } else if ("SEQUENCE".equalsIgnoreCase(idType) && dbType != null) {
                    if ("oracle".equalsIgnoreCase(dbType)) {
                        incrementer = new OracleSequenceMaxValueIncrementer(dataSource, sequenceName);
                    } else if ("postgres".equalsIgnoreCase(dbType)) {
                        incrementer = new PostgresSequenceMaxValueIncrementer(dataSource, sequenceName);
                    }
                }

                if (incrementer != null) {
                    try {
                        return incrementer.nextLongValue();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }

        return null;
    }


    private void executeEnhanceList(OnlCgformHead head, Map<String, Object> result) throws BusinessException {
        var records = (List<Map<String,Object>>)result.get("records");
        this.cgformHeadService.executeEnhanceList(head, "query", records);
    }
}
