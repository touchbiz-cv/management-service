package org.jeecg.cgform.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.touchbiz.common.utils.date.DateTimeFormat;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.cgform.infrastructure.enums.TableTypeEnum;
import org.jeecg.cgform.utils.DbDmlWhereConditionUtil;
import org.jeecg.common.api.dto.DataLogDTO;
import org.jeecg.common.system.util.JeecgDataAutorUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.FillRuleUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.online.auth.service.IOnlAuthDataService;
import org.jeecg.modules.online.auth.service.IOnlAuthPageService;
import org.jeecg.modules.online.cgform.entity.OnlCgformField;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;
import org.jeecg.modules.online.cgform.mapper.OnlCgformHeadMapper;
import org.jeecg.cgform.database.domain.CgformFieldDO;
import org.jeecg.cgform.service.CgformFieldService;
import org.jeecg.cgform.utils.DbDmlGenerator;
import org.jeecg.modules.online.cgform.mapper.OnlineMapper;
import org.jeecg.modules.online.cgform.model.e;
import org.jeecg.modules.online.cgform.model.h;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.jeecg.common.constant.CommonConstant.DATA_LOG_TYPE_COMMENT;
import static org.jeecg.common.constant.DataBaseConstant.*;
import static org.jeecg.common.constant.SymbolConstant.*;


/**
 * @author jiangyan
 */
@Slf4j
@Primary
@Service("OnlCgformFieldServiceImpl_n")
public class OnlCgformFieldServiceImpl extends org.jeecg.modules.online.cgform.service.a.c{

    private final  String[] IGNORED_Fields = {ID, SYS_ORG_CODE_TABLE.toUpperCase(), CREATE_BY_TABLE.toUpperCase(),UPDATE_BY_TABLE.toUpperCase(), CREATE_TIME_TABLE.toUpperCase(), UPDATE_TIME_TABLE.toUpperCase(), DELETED_TABLE.toUpperCase()};

    private final OnlCgformHeadMapper cgformHeadMapper;

    private final CgformFieldService cgformFieldService;

    private final DbServiceImpl dbService;

    private final SysBaseApiImpl sysBaseApi;

    private final OnlineMapper onlineMapper;

    private final IOnlAuthDataService onlAuthDataService;
    private final IOnlAuthPageService onlAuthPageService;
    public OnlCgformFieldServiceImpl(OnlCgformHeadMapper cgformHeadMapper, CgformFieldService cgformFieldService, DbServiceImpl dbService, SysBaseApiImpl sysBaseApi, OnlineMapper onlineMapper, IOnlAuthDataService onlAuthDataService, IOnlAuthPageService onlAuthPageService) {
        this.cgformHeadMapper = cgformHeadMapper;
        this.cgformFieldService = cgformFieldService;
        this.dbService = dbService;
        this.sysBaseApi = sysBaseApi;
        this.onlineMapper = onlineMapper;
        this.onlAuthDataService = onlAuthDataService;
        this.onlAuthPageService = onlAuthPageService;
    }

    @Override
    public void saveFormData(String code, String tbName, JSONObject json, boolean isCrazy) {
        var head = cgformHeadMapper.selectById(code);
        saveFormData(head, tbName, json, isCrazy);
    }

    @Override
    @Transactional(rollbackFor= Exception.class)
    public void saveFormData(List<OnlCgformField> fieldList, String tbname, JSONObject json) {
        var queryWrapper = new LambdaQueryWrapper<OnlCgformHead>();
        queryWrapper.eq(OnlCgformHead::getTableName, tbname);
        queryWrapper.isNull(OnlCgformHead::getPhysicId);
        var head = cgformHeadMapper.selectOne(queryWrapper);
        saveFormData(head, tbname, json, false);
    }

    public String saveFormData(OnlCgformHead head, String tbName, JSONObject json, boolean isCrazy) {
        var fieldList = cgformFieldService.listFieldByHead(head.getId());
        applyFullRule(fieldList, json);
        List<OnlCgformField> onlCgformFields = new ArrayList<>(fieldList);
        Map<String,Object> map;
        if (isCrazy) {
            map = DbDmlGenerator.c(tbName, onlCgformFields, json);
        } else {
            boolean isIdentity = "NATIVE".equalsIgnoreCase(head.getIdType());
            if(isIdentity){
                fieldList = fieldList.stream().filter(x->!ID.equals(x.getDbFieldName())).toList();
            }
            map = DbDmlGenerator.generatorInsertData(isIdentity, tbName, fieldList, json);
        }

        var id = dbService.executeInsert(tbName,map);
        addOnlineDataLog(head, tbName, id, map);
        return id;
    }

    /**
     * 查找原始字段配置中需要通过填充规则去进行填充的字段
     */
    private void applyFullRule(List<CgformFieldDO> fieldList, JSONObject json){
        fieldList.stream().filter(x-> !ObjectUtils.isEmpty(x.getFillRule())).forEach(field->{
            var fillRule = field.getFillRule();
            Object result = FillRuleUtil.executeRule(fillRule, json);
            json.put(field.getDbFieldName(), result);
        });
    }

    public void addOnlineDataLog(OnlCgformHead head, String tableName, String dataId, Map<String, Object> data) {
        if (checkAllowSaveLog(head)) {
            return;
        }
        //需要标记是第一条还是最后一条，然后根据这个值做差异比较
        String content = " 创建了记录";
        DataLogDTO var4 = new DataLogDTO(tableName, dataId, content, DATA_LOG_TYPE_COMMENT);
        sysBaseApi.saveDataLog(var4);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editFormData(String code, String tbName, JSONObject json, boolean isCrazy) {
        String dataId = json.containsKey(ID) ? json.get(ID).toString() : json.get("jeecg_row_key").toString();
        if(!json.containsKey(ID)){
            json.put(ID, dataId);
        }
        var fieldList  = new ArrayList<OnlCgformField>(cgformFieldService.listFieldByHead(code));
        Map<String,Object> map;
        if (isCrazy) {
            map = DbDmlGenerator.d(tbName, fieldList, json);
            this.getBaseMapper().executeUpdatetSQL(map);
        } else {
            map = DbDmlGenerator.generatorUpdateData(fieldList, json);
            dbService.executeUpdate(tbName,map);
        }
        log.info("json:{},map:{}",json, map);
        this.addOnlineUpdateDataLog(tbName, dataId, fieldList, json);

    }

    @Override
    public void addOnlineUpdateDataLog(String tableName, String dataId, List<OnlCgformField> fieldList, JSONObject json) {
        var queryWrapper = new LambdaQueryWrapper<OnlCgformHead>();
        queryWrapper.eq(OnlCgformHead::getTableName, tableName);
        var head = cgformHeadMapper.selectOne(queryWrapper);
        if(head == null){
            return;
        }
        if (checkAllowSaveLog(head)) {
            return;
        }

        String physicalTableName = DbDmlGenerator.f(tableName);
        var originData = this.queryFormData(physicalTableName, dataId);
        if (originData != null) {
            DataLogDTO dto = new DataLogDTO(physicalTableName, dataId, DATA_LOG_TYPE_COMMENT);
            String content = generateDataLogContent(fieldList, json, originData);
            dto.setContent(content);
            sysBaseApi.saveDataLog(dto);
        }

    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void deleteAutoListMainAndSub(OnlCgformHead head, String ids) {
        if (!TableTypeEnum.MASTER.getType().equals(head.getTableType())) {
            return;
        }
        String tableName = head.getTableName();
        String tableNameField = "tableName";
        String linkField = "linkField";
        String linkValueStr = "linkValueStr";
        String mainField = "mainField";
        //TODO 查找这张表是否存在deleted字段，以方便进行标记删除

        var mainTableFields = cgformFieldService.listFieldByHead(head.getId(), true);
        var existDeletedField = mainTableFields.stream().anyMatch(x-> DELETED_TABLE.equals(x.getDbFieldName()));
        var var9 = new ArrayList<Map<String,String>>();
        if (oConvertUtils.isNotEmpty(head.getSubTableStr())) {
            String[] subTableNames = head.getSubTableStr().split(COMMA);

            for (String subTableName : subTableNames) {
                var queryWrapper = new LambdaQueryWrapper<OnlCgformHead>();
                queryWrapper.eq(OnlCgformHead::getTableName, subTableName);
                OnlCgformHead cgformHead = this.cgformHeadMapper.selectOne(queryWrapper);
                if (cgformHead != null) {
                    cgformFieldService.listFieldByHeadAndMainTable(cgformHead.getId(), head.getTableName())
                            .forEach(fieldDO -> {
                                var var18 = new HashMap<String, String>(5);
                                var18.put(linkField, fieldDO.getDbFieldName());
                                var18.put(mainField, fieldDO.getMainField());
                                var18.put(tableNameField, subTableName);
                                var18.put(linkValueStr, "");
                                var9.add(var18);
                            });
                }
            }

            String[] mainIds = ids.split(COMMA);
            int index = 0;

            label56:
            while(true) {
                if (index >= mainIds.length) {
                    var var28 = var9.iterator();

                    while(true) {
                        if (!var28.hasNext()) {
                            break label56;
                        }

                        var map = var28.next();
                        //TODO 需要判断字表是否开启了标记删除，如果开启了标记删除，则需要分开处理
                        this.deleteAutoList(map.get(tableNameField), map.get(linkField), map.get(linkValueStr));
                    }
                }

                String mainId = mainIds[index];
                if (mainId.contains("@")) {
                    mainId = mainId.substring(0, mainId.indexOf("@"));
                }

                String selectSql = DbDmlGenerator.generatorSelectSql(tableName, mainTableFields, mainId);
                Map<String, Object> formData = this.baseMapper.queryFormData(selectSql);

                var9.forEach(map -> {
                    var mainFieldValue = map.get(mainField);
                    Object var22 = formData.getOrDefault(mainFieldValue.toLowerCase(),
                            formData.get(mainFieldValue.toUpperCase()));
                    if (var22 != null) {
                        map.put(linkValueStr, map.get(linkValueStr) + var22 + COMMA);
                    }
                });

                ++index;
            }
        }
        //TODO 需要判断字表是否开启了标记删除，如果开启了标记删除，则需要分开处理
        this.deleteAutoListById(head.getTableName(), ids, existDeletedField);
    }

    public void deleteAutoListById(String tbname, String ids, boolean existDeletedField) {
        this.deleteAutoList(tbname, "id", ids,existDeletedField);
    }


    @SneakyThrows
    public void deleteAutoList(String tbname, String linkField, String linkValue, boolean existDeletedField) {
        if (linkValue != null && !linkValue.isEmpty()) {
            String[] var4 = linkValue.split(COMMA);
            StringBuilder var5 = new StringBuilder();

            for (String s : var4) {
                String var9 = s;
                if (var9 != null && !var9.isEmpty()) {
                    if (var9.indexOf("@") > 0) {
                        var9 = var9.substring(0, var9.indexOf("@"));
                    }

                    var5.append("'").append(var9).append("',");
                }
            }

            String var10 = var5.toString();
            String sql;
            if(existDeletedField){
                sql = "UPDATE " + DbDmlGenerator.f(tbname) + " set deleted=1 where " + linkField + " in(" + var10.substring(0, var10.length() - 1) + ")";
                this.baseMapper.editFormData(sql);
            }
            else{
                sql = "DELETE FROM " + DbDmlGenerator.f(tbname) + " where " + linkField + " in(" + var10.substring(0, var10.length() - 1) + ")";
                this.baseMapper.deleteAutoList(sql);
            }
        }

    }

    @Override
    public Map<String, Object> queryAutolistPage(OnlCgformHead head, Map<String, Object> params, List<String> needList) {
        e queryInfo = this.getQueryInfo(head, params, needList);
        String sql = queryInfo.getSql();
        var queryParams = queryInfo.getParams();
        var fieldList = queryInfo.getFieldList();
        HashMap<String, Object> result = new HashMap<>(5);
        var pageSize = params.get("pageSize") == null ? 10 : Integer.parseInt(params.get("pageSize").toString());
        if (pageSize == -521) {
            var var10 = this.onlineMapper.selectByCondition(sql, queryParams);
            if (var10 != null && var10.size() != 0) {
                result.put("total", var10.size());
                result.put("fieldList", fieldList);
                result.put("records", DbDmlGenerator.d(var10));
            } else {
                result.put("total", 0);
                result.put("fieldList", fieldList);
            }
        } else {
            var pageNo = params.get("pageNo") == null ? 1 : Integer.parseInt(params.get("pageNo").toString());
            var page = new Page<Map<String, Object>>(pageNo, pageSize);
            var pageResult = this.onlineMapper.selectPageByCondition(page, sql, queryParams);
            result.put("total", pageResult.getTotal());
            var var13 = DbDmlGenerator.d(pageResult.getRecords());
            this.handleLinkTableDictData(head.getId(), var13);
            result.put("records", var13);
        }

        return result;
    }

    @Override
    public e getQueryInfo(OnlCgformHead head, Map<String, Object> params, List<String> needList) {
        String tableName = head.getTableName();
        String headId = head.getId();

        var fieldList = cgformFieldService.listFieldByHead(head.getId(), true);
        var selectFieldList = head.getSelectFieldList();
        List<CgformFieldDO> availableFields;
        if (selectFieldList != null && selectFieldList.size() > 0) {
            availableFields = this.a(headId, fieldList, selectFieldList, needList);
        } else {
            availableFields = this.queryAvailableFieldList(headId, true, fieldList, needList);
        }

        StringBuilder sb = new StringBuilder();
        DbDmlGenerator.generatorSelectSql(tableName, availableFields, sb);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = user.getId();
        var authData = this.onlAuthDataService.queryUserOnlineAuthData(userId, headId);
        if (!CollectionUtils.isEmpty(authData)) {
            HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
            JeecgDataAutorUtils.installUserInfo(request, this.sysBaseApi.getCacheUser(user.getUsername()));
        }

        DbDmlWhereConditionUtil conditionUtil = new DbDmlWhereConditionUtil("t.");
        conditionUtil.setTableName(tableName);
        conditionUtil.setNeedList(needList);
        conditionUtil.setSubTableStr(head.getSubTableStr());
        var persistFields = DbDmlGenerator.filterIsPersistFields(fieldList);
        String whereCondition = conditionUtil.generateWhereCondition(persistFields, params, authData);
        var sqlParams = conditionUtil.getSqlParams();
        if (!whereCondition.trim().isEmpty()) {
            sb.append(" t ").append(" where  ").append(whereCondition);
        }

        String sql = this.generateSelectFullSql(fieldList, params);
        sb.append(sql);
        e var19 = new e(sb.toString(), sqlParams);
        var19.setFieldList(Collections.unmodifiableList(availableFields));
        return var19;
    }



    public List<CgformFieldDO> queryAvailableFieldList(String cgformId, boolean isList, List<CgformFieldDO> List, List<String> needList) {
        LoginUser user = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        String userId = user.getId();
        var var8 = this.onlAuthPageService.queryListHideColumn(userId, cgformId);
        return this.a(var8, isList, List, needList);
    }

    private String generateSelectFullSql(List<CgformFieldDO> fieldList, Map<String, Object> var2) {
        Object var3 = var2.get("column");
        var var4 = new ArrayList<h>();
        String var7;
        h var8;
        if (var3 != null && !ID.equals(var3.toString())) {
            String var12 = var3.toString();
            Object var14 = var2.get("order");
            var7 = "desc";
            if (var14 != null) {
                var7 = var14.toString();
            }

            var8 = new h(var12, var7);
            var4.add(var8);
        } else {

            for (CgformFieldDO field : fieldList) {
                if ("1".equals(field.getSortFlag())) {
                    var7 = field.getFieldExtendJson();
                    var8 = new h(field.getDbFieldName());
                    if (var7 != null && !var7.isEmpty()) {
                        JSONObject jsonObject = JSON.parseObject(var7);
                        String orderRule = jsonObject.getString("orderRule");
                        if (orderRule != null && !orderRule.isEmpty()) {
                            var8.setRule(orderRule);
                            var4.add(var8);
                        }
                    }
                }
            }

            if (var4.size() == 0) {
                h var11 = h.a();
                var4.add(var11);
            }
        }

        var realSqlList = var4.stream().filter(x->isExistCamelToUnderline(x.getColumn(), fieldList))
                .map(h::getRealSql).toList();

        return " ORDER BY " + String.join(",", realSqlList);
    }

    private boolean isExistCamelToUnderline(String var1, List<CgformFieldDO> var2) {
        return var2.stream().anyMatch(var5->oConvertUtils.camelToUnderline(var1).equals(var5.getDbFieldName()));
    }


    private boolean checkAllowSaveLog(OnlCgformHead head) {
        if(ObjectUtils.isEmpty(head.getExtConfigJson())){
            return true;
        }
        JSONObject object;
        try{
            object = JSONObject.parseObject(head.getExtConfigJson());
        }
        catch(JSONException err){
            return true;
        }

        return !(object.containsKey("commentStatus") || object.get("commentStatus") == "1");
    }

    public Map<String, Object> queryFormData(String tableName, String dataId) {
        String var3 = "select * from " + tableName + " where id = '" + dataId + SINGLE_QUOTATION_MARK;
        return this.baseMapper.queryFormData(var3);
    }

    private String generateDataLogContent(List<OnlCgformField> fields, JSONObject originalData, Map<String, Object> newData) {
        StringBuilder logContent = new StringBuilder();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DateTimeFormat.DATE_FORMAT_FULL);
        for (OnlCgformField field : fields) {
            String fieldName = field.getDbFieldName();

            if (fieldName == null || isIgnoredField(fieldName)) {
                continue;
            }

            String fieldType = field.getDbType();
            Object originalValue = originalData.get(fieldName);
            Object newValue = newData.get(fieldName);

            String originalValueStr = formatValue(originalValue, fieldType, dateTimeFormatter);
            String newValueStr = formatValue(newValue, fieldType, dateTimeFormatter);

            if (!originalValueStr.equals(newValueStr)) {
                logContent.append("将名称为[").append(field.getDbFieldTxt()).append("]的字段内容:").append(originalValueStr).append(" 修改为:").append(newValueStr).append(";");
            }
        }

        return logContent.toString();
    }

    private boolean isIgnoredField(String fieldName) {
        return Arrays.stream(IGNORED_Fields).anyMatch(ignored -> ignored.equalsIgnoreCase(fieldName));
    }

    private String formatValue(Object value, String fieldType, DateTimeFormatter formatter) {
        if (value == null) {
            return "空";
        } else if ("Datetime".equalsIgnoreCase(fieldType) && value instanceof LocalDateTime) {
            return formatter.format((LocalDateTime) value);
        } else if ("BigDecimal".equalsIgnoreCase(fieldType) || "double".equalsIgnoreCase(fieldType) || value instanceof Number) {
            return value.toString();
        } else {
            return value.toString().isEmpty() ? "空" : value.toString();
        }
    }

    private List<CgformFieldDO> a(String var1, List<CgformFieldDO> var2, List<String> var3, List<String> var4) {
        LoginUser user = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        String userId = user.getId();
        var var7 = new ArrayList<CgformFieldDO>();

        var2.forEach(field -> {
            String var10 = field.getDbFieldName();
            if (var3.contains(var10)) {
                var7.add(field);
            }
        });

        var var11 = this.onlAuthPageService.queryListHideColumn(userId, var1);
        return this.a(var11, true, var7, var4);
    }

    private List<CgformFieldDO> a(List<String> var1, boolean var2, List<CgformFieldDO> var3, List<String> var4) {
        List<CgformFieldDO> var5 = new ArrayList<>();
        boolean var6 = var1 != null && var1.size() != 0 && var1.get(0) != null;

        for (CgformFieldDO field : var3) {
            String var9 = field.getDbFieldName();
            if (var4 != null && var4.contains(var9)) {
                field.setIsQuery(1);
                var5.add(field);
            } else {
                if (var2) {
                    if (field.getIsShowList() != 1) {
                        if (oConvertUtils.isNotEmpty(field.getMainTable()) && oConvertUtils.isNotEmpty(field.getMainField())) {
                            var5.add(field);
                        }
                        continue;
                    }
                } else if (field.getIsShowForm() != 1) {
                    continue;
                }

                if (var6) {
                    if (this.b(var9, var1)) {
                        var5.add(field);
                    }
                } else {
                    var5.add(field);
                }
            }
        }

        return var5;
    }

    private boolean b(String var1, List<String> var2) {
        boolean var3 = true;

        for (String var5 : var2) {
            if (!oConvertUtils.isEmpty(var5)) {
                String var6 = var5.substring(var5.lastIndexOf(COLON) + 1);
                if (!oConvertUtils.isEmpty(var6) && var6.equals(var1)) {
                    var3 = false;
                }
            }
        }

        return var3;
    }

}
