package org.jeecg.cgform.service.impl;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.touchbiz.common.entity.exception.ParamException;
import com.touchbiz.common.utils.date.DateTimeFormat;
import com.touchbiz.common.utils.date.LocalDateTimeUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.jeecg.cgform.database.domain.CgformFieldDO;
import org.jeecg.cgform.database.domain.CgformHeadDO;
import org.jeecg.cgform.entity.CgFormEntity;
import org.jeecg.cgform.entity.CgFormSaveEntity;
import org.jeecg.cgform.entity.CgHeadConfigEntity;
import org.jeecg.cgform.infrastructure.converter.OnlCgformHeadConverter;
import org.jeecg.cgform.infrastructure.enums.TableTypeEnum;
import org.jeecg.cgform.service.CgformFieldService;
import org.jeecg.cgform.service.CgformHeadService;
import org.jeecg.cgform.utils.DbDmlGenerator;
import org.jeecg.cgform.utils.DbGenerator;
import org.jeecg.cgform.utils.DbReadTableUtil;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.online.cgform.entity.OnlCgformField;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;
import org.jeecg.modules.online.cgform.entity.OnlCgformIndex;
import org.jeecg.modules.online.cgform.service.IOnlCgformIndexService;
import org.jeecg.modules.online.config.b.b;
import org.jeecg.modules.online.config.d.e;
import org.jeecg.modules.online.config.exception.BusinessException;
import org.jeecg.modules.online.config.service.DbTableHandleI;
import org.jeecg.modules.system.service.ISysFillRuleService;
import org.jeecgframework.codegenerate.generate.pojo.ColumnVo;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.baomidou.dynamic.datasource.support.DdConstants.MASTER;
import static org.jeecg.common.constant.CommonConstant.CHAR_BIG_N;
import static org.jeecg.common.constant.CommonConstant.CHAR_BIG_Y;
import static org.jeecg.common.constant.DataBaseConstant.*;
import static org.jeecg.common.constant.SymbolConstant.COMMA;
import static org.jeecg.common.util.CommonUtils.getDatabaseTypeEnum;

/**
 * @author jiangyan
 */
@Slf4j
@Primary
@Service("OnlCgformHeadServiceImpl_n")
public class OnlCgformHeadServiceImpl extends org.jeecg.modules.online.cgform.service.a.d {

    private final OnlCgformFieldServiceImpl fieldService;
    private final CgformFieldService cgformFieldService;
    private final IOnlCgformIndexService indexService;
    private final ISysFillRuleService fillRuleService;

    private final CgformHeadService cgformHeadService;

    public OnlCgformHeadServiceImpl(OnlCgformFieldServiceImpl fieldService, CgformFieldService cgformFieldService, IOnlCgformIndexService indexService, ISysFillRuleService fillRuleService, CgformHeadService cgformHeadService) {
        this.fieldService = fieldService;
        this.cgformFieldService = cgformFieldService;
        this.indexService = indexService;
        this.fillRuleService = fillRuleService;
        this.cgformHeadService = cgformHeadService;
    }

    @Override
    public void doDbSynch(String code, String syncMethod) throws HibernateException, SQLException, org.jeecg.modules.online.config.exception.a {
        OnlCgformHead cgformHead = this.getById(code);
        if (cgformHead == null) {
            throw new ParamException("实体配置不存在");
        } else {
            var tableName = cgformHead.getTableName();
            var fieldList = this.cgformFieldService.listFieldByHead(code, true);
            CgHeadConfigEntity configEntity = new CgHeadConfigEntity();
            configEntity.setTableName(tableName);
            configEntity.setJformPkType(cgformHead.getIdType());
            configEntity.setJformPkSequence(cgformHead.getIdSequence());
            configEntity.setContent(cgformHead.getTableTxt());
            configEntity.setFields(fieldList);
            configEntity.setColumns(new ArrayList<>(fieldList));

            DbType dbType = getDatabaseTypeEnum();

            if ("normal".equals(syncMethod) && !DbType.SQLITE.equals(dbType)) {
                if (e.a(tableName, (b)null)) {
                    DbGenerator dbGenerator = new DbGenerator();
                    List<String> alterSql = dbGenerator.generateAlterSql(getDataSource(), configEntity);

                    alterSql.stream().filter(sql -> !oConvertUtils.isEmpty(sql) && !oConvertUtils.isEmpty(sql.trim()))
                            .forEach(sql -> getBaseMapper().executeDDL(sql));

                    List<OnlCgformIndex> indexList = this.indexService.list(
                            new LambdaQueryWrapper<OnlCgformIndex>().eq(OnlCgformIndex::getCgformHeadId, code)
                    );

                    indexList.stream().filter(cgformIndex -> CHAR_BIG_N.equals(cgformIndex.getIsDbSynch()) || CommonConstant.DEL_FLAG_1.equals(cgformIndex.getDelFlag())).forEach(cgformIndex -> {
                        String indexName = dbGenerator.b(cgformIndex.getIndexName(), tableName);
                        if (indexService.isExistIndex(indexName)) {
                            String dropIndexSql = dbGenerator.a(cgformIndex.getIndexName(), tableName);
                            try {
                                getBaseMapper().executeDDL(dropIndexSql);
                            } catch (Exception ex) {
                                log.error("删除表【{}】索引({})失败!", tableName, cgformIndex.getIndexName());

                            }
                        }
                        if (CommonConstant.DEL_FLAG_1.equals(cgformIndex.getDelFlag())) {
                            this.indexService.removeById(cgformIndex.getId());
                        }
                    });
                } else {
                    DbGenerator.generateDdlSql(getDataSource(), configEntity);
                }
            } else if ("force".equals(syncMethod) || DbType.SQLITE.equals(dbType)) {
                DbTableHandleI var10 = e.getTableHandle();
                String dropTableSQL = var10.dropTableSQL(tableName);
                getBaseMapper().executeDDL(dropTableSQL);
                DbGenerator.generateDdlSql(getDataSource(), configEntity);
            }

            this.indexService.createIndex(code, e.getDatabaseType(), tableName);
            cgformHead.setIsDbSynch(CHAR_BIG_Y);
            if (cgformHead.getTableVersion() == 1) {
                cgformHead.setTableVersion(2);
            }

            this.updateById(cgformHead);
        }
    }

    private DruidDataSource getDataSource(){
        DataSource dataSource = SpringContextUtils.getApplicationContext().getBean(DataSource.class);
        if(dataSource instanceof DruidDataSource){
            return (DruidDataSource) dataSource;
        }
        var key = DynamicDataSourceContextHolder.peek();
        DataSource source;
        if(key == null){
            source =  ((DynamicRoutingDataSource)dataSource).getDataSources().get(MASTER);
        }
        else{
            source =  ((DynamicRoutingDataSource)dataSource).getDataSource(key);
        }
        if(source instanceof DruidDataSource){
            return (DruidDataSource) source;
        }
        if(source instanceof ItemDataSource){
            return (DruidDataSource) ((ItemDataSource) source).getDataSource();
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public String saveManyFormData(String code, JSONObject json, String xAccessToken) throws org.jeecg.modules.online.config.exception.a, BusinessException {
        return saveManyFormData(code, json).getTableName();
    }

    @Transactional(rollbackFor = {Exception.class})
    public CgFormSaveEntity saveManyFormData(String code, JSONObject json) throws org.jeecg.modules.online.config.exception.a, BusinessException {
        OnlCgformHead head = this.getTable(code);
        CgFormSaveEntity result = new CgFormSaveEntity();
        String var5 = "add";
        this.executeEnhanceJava(var5, "start", head, json);
        String physicalTableName = DbDmlGenerator.f(head.getTableName());

        if (CHAR_BIG_Y.equals(head.getIsTree())) {
            this.fieldService.saveTreeFormData(head.getId(), physicalTableName, json, head.getTreeIdField(), head.getTreeParentIdField());
        } else {
            var id = this.fieldService.saveFormData(head, physicalTableName, json, false);
            result.setId(id);
        }
        if (head.getTableType().equals(TableTypeEnum.MASTER.getType())
                && oConvertUtils.isNotEmpty(head.getSubTableStr())) {
            json.put(ID, result.getId());
            Arrays.stream(head.getSubTableStr().split(COMMA)).forEach(subTableName->{
                JSONArray subTableData = json.getJSONArray(subTableName);
                if (subTableData != null && !subTableData.isEmpty()) {
                    var subTableHead = cgformHeadService.getByTableName(subTableName);
                    if (subTableHead != null) {
                        var subTableFieldList = this.cgformFieldService.listFieldByHead(subTableHead.getId());
                        String dbFieldName = "";
                        String mainTableKey = null;
                        for (var subTableField: subTableFieldList) {
                            if (!oConvertUtils.isEmpty(subTableField.getMainField())) {
                                dbFieldName = subTableField.getDbFieldName();
                                String mainField = subTableField.getMainField();
                                if (json.get(mainField.toLowerCase()) != null) {
                                    mainTableKey = json.getString(mainField.toLowerCase());
                                    break;
                                }
                                else if (json.get(mainField.toUpperCase()) != null) {
                                    mainTableKey = json.getString(mainField.toUpperCase());
                                    break;
                                }
                            }
                        }
                        log.info("subTableHead:{}, dbFieldName:{}",subTableHead, dbFieldName );
                        for (int var21 = 0; var21 < subTableData.size(); ++var21) {
                            JSONObject jsonObject = subTableData.getJSONObject(var21);
                            if (mainTableKey != null) {
                                jsonObject.put(dbFieldName, mainTableKey);
                            }
                            this.fieldService.saveFormData(subTableHead, subTableHead.getTableName(), jsonObject, false);
                        }
                    }
                }
            });
        }

        this.executeEnhanceSql(var5, head.getId(), json);
        this.executeEnhanceJava(var5, "end", head, json);
        result.setTableName(head.getTableName());
        return result;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void saveDbTable2Online(String tbName) {

        CgformHeadDO cgformHead = new CgformHeadDO();
        cgformHead.setTableType(1);
        cgformHead.setIsCheckbox(CHAR_BIG_Y);
        cgformHead.setIsDbSynch(CHAR_BIG_Y);
        cgformHead.setIsTree(CHAR_BIG_N);
        cgformHead.setIsPage(CHAR_BIG_Y);
        cgformHead.setQueryMode("group");
        cgformHead.setTableName(tbName.toLowerCase());
        cgformHead.setTableTxt(tbName);
        cgformHead.setTableVersion(1);
        cgformHead.setFormTemplate("1");
        cgformHead.setCopyType(0);
        cgformHead.setIsDesForm(CHAR_BIG_N);
        cgformHead.setScroll(1);
        cgformHead.setThemeTemplate("normal");
        cgformHead.setId(String.valueOf(IdWorker.getId()));
        var fieldList = new ArrayList<CgformFieldDO>();

        try {
            DataSource dataSource = SpringContextUtils.getApplicationContext().getBean(DataSource.class);
            var isView = DbReadTableUtil.isView(dataSource.getConnection(), tbName);
            cgformHead.setIsView(isView? 1 : 0);
            List<ColumnVo> dbColumns = DbReadTableUtil.readOriginalTableColumn(dataSource.getConnection(), tbName);

            IntStream.range(0, dbColumns.size()).forEach(var6 -> {
                var columnVo = dbColumns.get(var6);
                String fieldDbName = columnVo.getFieldDbName();
                var cgformField = new CgformFieldDO();
                cgformField.setCgformHeadId(cgformHead.getId());
                cgformField.setDbFieldNameOld(columnVo.getFieldDbName().toLowerCase());
                cgformField.setDbFieldName(columnVo.getFieldDbName().toLowerCase());
                if (oConvertUtils.isNotEmpty(columnVo.getFiledComment())) {
                    cgformField.setDbFieldTxt(columnVo.getFiledComment());
                } else {
                    cgformField.setDbFieldTxt(columnVo.getFieldName());
                }
                cgformField.setDbIsKey(0);
                cgformField.setIsShowForm(1);
                cgformField.setIsQuery(0);
                cgformField.setFieldMustInput("0");
                cgformField.setIsShowList(1);
                cgformField.setOrderNum(var6 + 1);
                cgformField.setQueryMode("single");
                cgformField.setDbLength(oConvertUtils.getInt(columnVo.getPrecision()));
                cgformField.setFieldLength(120);
                cgformField.setDbPointLength(oConvertUtils.getInt(columnVo.getScale()));
                cgformField.setFieldShowType("text");
                cgformField.setDbIsNull(CHAR_BIG_Y.equals(columnVo.getNullable()) ? 1 : 0);
                cgformField.setIsReadOnly(0);
                if (ID.equalsIgnoreCase(fieldDbName)) {
                    String[] var10 = new String[]{"java.lang.Integer", "java.lang.Long"};
                    String var11 = columnVo.getFieldType();
                    if (Arrays.asList(var10).contains(var11)) {
                        cgformHead.setIdType("NATIVE");
                    } else {
                        cgformHead.setIdType("UUID");
                    }

                    cgformField.setDbIsKey(1);
                    cgformField.setIsShowForm(0);
                    cgformField.setIsShowList(0);
                    cgformField.setIsReadOnly(1);
                }
                if (CREATE_BY_TABLE.equalsIgnoreCase(fieldDbName) || CREATE_TIME_TABLE.equalsIgnoreCase(fieldDbName) || UPDATE_BY_TABLE.equalsIgnoreCase(fieldDbName) || UPDATE_TIME_TABLE.equalsIgnoreCase(fieldDbName) || SYS_ORG_CODE_TABLE.equalsIgnoreCase(fieldDbName)) {
                    cgformField.setIsShowForm(0);
                    cgformField.setIsShowList(0);
                }

                if (Integer.class.getName().equalsIgnoreCase(columnVo.getFieldType())) {
                    cgformField.setDbType("int");

                } else if (Timestamp.class.getName().equalsIgnoreCase(columnVo.getFieldType())) {
                    cgformField.setDbType("timestamp");
                } else if (Long.class.getName().equalsIgnoreCase(columnVo.getFieldType())) {
                    cgformField.setDbType("bigint");
                } else if (Date.class.getName().equalsIgnoreCase(columnVo.getFieldType())) {
                    if ("datetime".equals(columnVo.getFieldDbType())) {
                        cgformField.setDbType("Datetime");
                        cgformField.setFieldShowType("datetime");
                    } else {
                        cgformField.setDbType("Date");
                        cgformField.setFieldShowType("date");
                    }
                } else if (!Double.class.getName().equalsIgnoreCase(columnVo.getFieldType()) && !Float.class.getName().equalsIgnoreCase(columnVo.getFieldType())) {
                    if (!BigDecimal.class.getName().equalsIgnoreCase(columnVo.getFieldType()) && !"BigDecimal".equalsIgnoreCase(columnVo.getFieldType())) {
                        if (!"byte[]".equalsIgnoreCase(columnVo.getFieldType()) && !columnVo.getFieldType().contains("blob")) {
                            if (Object.class.getName().equals(columnVo.getFieldType()) && ("text".equalsIgnoreCase(columnVo.getFieldDbType()) || "ntext".equalsIgnoreCase(columnVo.getFieldDbType()))) {
                                cgformField.setDbType("Text");
                                cgformField.setFieldShowType("textarea");
                            } else if (Object.class.getName().equals(columnVo.getFieldType()) && "image".equalsIgnoreCase(columnVo.getFieldDbType())) {
                                cgformField.setDbType("Blob");
                            } else {
                                cgformField.setDbType("string");
                            }
                        } else {
                            cgformField.setDbType("Blob");
                            columnVo.setCharmaxLength(null);
                        }
                    } else {
                        cgformField.setDbType("BigDecimal");
                    }
                } else {
                    cgformField.setDbType("double");
                }
                if (oConvertUtils.isEmpty(columnVo.getPrecision()) && oConvertUtils.isNotEmpty(columnVo.getCharmaxLength())) {
                    if (Long.parseLong(columnVo.getCharmaxLength()) >= 3000L) {
                        cgformField.setDbType("Text");
                        cgformField.setFieldShowType("textarea");

                        try {
                            cgformField.setDbLength(Integer.valueOf(columnVo.getCharmaxLength()));
                        } catch (Exception var12) {
                            log.error(var12.getMessage(), var12);
                        }
                    } else {
                        cgformField.setDbLength(Integer.valueOf(columnVo.getCharmaxLength()));
                    }
                } else {
                    if (oConvertUtils.isNotEmpty(columnVo.getPrecision())) {
                        cgformField.setDbLength(Integer.valueOf(columnVo.getPrecision()));
                    } else if ("int".equals(cgformField.getDbType())) {
                        cgformField.setDbLength(10);
                    }

                    if (oConvertUtils.isNotEmpty(columnVo.getScale())) {
                        cgformField.setDbPointLength(Integer.valueOf(columnVo.getScale()));
                    }
                }
                if (oConvertUtils.getInt(columnVo.getPrecision()) == -1 && oConvertUtils.getInt(columnVo.getScale()) == 0) {
                    cgformField.setDbType("Text");
                }
                if (JDBCType.BLOB.getName().equalsIgnoreCase(cgformField.getDbType()) || "Text".equals(cgformField.getDbType()) ||  JDBCType.DATE.getName().equalsIgnoreCase(cgformField.getDbType())) {
                    cgformField.setDbLength(0);
                    cgformField.setDbPointLength(0);
                }
                cgformField.setDbIsPersist(1);
                fieldList.add(cgformField);
            });
        } catch (Exception var13) {
            log.error(var13.getMessage(), var13);
        }

        if (oConvertUtils.isEmpty(cgformHead.getFormCategory())) {
            cgformHead.setFormCategory("bdfl_include");
        }

        cgformHeadService.save(cgformHead);
        this.cgformFieldService.saveBatch(fieldList);
    }

    @Transactional(rollbackFor = {Exception.class})
    public Result<?> addAllNew(CgFormEntity model) {
        OnlCgformHead head = model.getHead();
        head.setId(LocalDateTimeUtils.getCurrentTimeStr(DateTimeFormat.DATE_FORMAT_YYYYMMDDHHMMSS));
        var var4 = model.getFields();
        var var5 = model.getIndexs();

        boolean var6 = false;

        for(int orderNum = 0; orderNum < var4.size(); ++orderNum) {
            var field = var4.get(orderNum);
            checkFillRule(field);
            field.setId(null);
            field.setCgformHeadId(head.getId());
            if (field.getOrderNum() == null) {
                field.setOrderNum(orderNum);
            }

            if (oConvertUtils.isNotEmpty(field.getMainTable()) && oConvertUtils.isNotEmpty(field.getMainField())) {
                var6 = true;
            }

            setTextFieldDefaultLength(field);
            if (field.getDbIsPersist() == null) {
                field.setDbIsPersist(1);
            }
        }

        var5.forEach(var10->{
            var10.setId(null);
            var10.setCgformHeadId(head.getId());
            var10.setIsDbSynch(CHAR_BIG_N);
            var10.setDelFlag(CommonConstant.DEL_FLAG_0);
        });


        head.setIsDbSynch(CHAR_BIG_N);
        head.setQueryMode("single");
        head.setTableVersion(1);
        head.setCopyType(0);
        if (TableTypeEnum.SLAVE.getType().equals(head.getTableType()) && head.getTabOrderNum() == null) {
            head.setTabOrderNum(1);
        }

        super.save(head);
        this.cgformFieldService.saveBatch(var4);
        this.indexService.saveBatch(var5);
        this.a(head, var4);
        if (TableTypeEnum.SLAVE.getType().equals(head.getTableType()) && var6) {
            fieldService.clearCacheOnlineConfig();
        }
        return Result.ok("添加成功");
    }


    @Transactional(rollbackFor = {Exception.class})
    public Result<?> editAllNew(CgFormEntity model){
        var head = model.getHead();
        var dbHead = super.getById(head.getId());
        if (dbHead == null) {
            return Result.error("未找到对应实体");
        }
        AtomicReference<String> isDbSynch = new AtomicReference<>(dbHead.getIsDbSynch());
        if (DbDmlGenerator.a(dbHead, head)) {
            isDbSynch.set(CHAR_BIG_N);
        }

        Integer tableVersion = dbHead.getTableVersion();
        if (tableVersion == null) {
            tableVersion = 0;
        }
        head.setTableVersion(tableVersion + 1);

        List<CgformFieldDO> saveFields = new ArrayList<>();
        List<CgformFieldDO> updateFields = new ArrayList<>();

        model.getFields().forEach(field->{
            String fieldId = String.valueOf(field.getId());
            checkFillRule(field);
            setTextFieldDefaultLength(field);
            if (fieldId.length() == 32) {
                updateFields.add(field);
            } else {
                String var13 = "_pk";
                if (!var13.equals(fieldId)) {
                    field.setId(null);
                    field.setCgformHeadId(head.getId());
                    saveFields.add(field);
                }
            }

            if (field.getDbIsPersist() == null) {
                field.setDbIsPersist(1);
            }
        });

        if (dbIsPersist(saveFields)) {
            isDbSynch.set(CHAR_BIG_N);
        }

        AtomicInteger orderNum = new AtomicInteger(0);

        List<CgformFieldDO> updateList = new ArrayList<>();
        updateFields.forEach(fieldDO->{
            var field = cgformFieldService.getById(fieldDO.getId());
            removeSubTable(field.getMainTable(), head.getTableName());
            if (DbDmlGenerator.a(field, fieldDO)) {
                isDbSynch.set(CHAR_BIG_N);
            }

            if ((field.getOrderNum() == null ? 0 : field.getOrderNum()) > orderNum.get()) {
                orderNum.set(field.getOrderNum());
            }

            if (CHAR_BIG_Y.equals(dbHead.getIsDbSynch()) && !fieldDO.getDbFieldName().equals(field.getDbFieldName())) {
                fieldDO.setDbFieldNameOld(field.getDbFieldName());
            }
            if (fieldDO.getFieldValidType() == null) {
                fieldDO.setFieldValidType("");
            }

            updateList.add(fieldDO);
        });
        this.cgformFieldService.updateBatchById(updateList);

        saveFields.forEach(field->{
            if (field.getOrderNum() == null) {
                orderNum.getAndIncrement();
                field.setOrderNum(orderNum.get());
            }
        });

        this.cgformFieldService.saveBatch(saveFields);

        saveIndex(model, head, isDbSynch);

        if (!CollectionUtils.isEmpty(model.getDeleteFieldIds())) {
            model.getDeleteFieldIds().forEach(item->{
                var field = this.fieldService.getById(item);
                if (field != null) {
                    if (field.getDbIsPersist() == 1) {
                        isDbSynch.set(CHAR_BIG_N);
                    }
                    removeSubTable(field.getMainTable(), head.getTableName());
//                        this.fieldService.removeById(item);
                }
            });
            this.fieldService.removeBatchByIds(model.getDeleteFieldIds());
        }

        head.setIsDbSynch(isDbSynch.get());
        super.updateById(head);
        this.a(head, model.getFields());
        updateCgformHeadFields(head, model.getFields());
        return Result.ok("全部修改成功");
    }

    public OnlCgformHead copyOnlineTableConfigReturnView(OnlCgformHead physicTable){
        String tableId = physicTable.getId();
        OnlCgformHead view = OnlCgformHeadConverter.INSTANCE.copy(physicTable);
        String id = UUIDGenerator.generate();
        view.setId(id);
        view.setTableName(generateViewName(tableId, physicTable.getTableName()));
        var fieldList = this.cgformFieldService.listFieldByHead(tableId);
        var saveList = fieldList.stream().map(field->{
            CgformFieldDO newField = new CgformFieldDO();
            newField.setCgformHeadId(id);
            this.copy(field, newField);
            return newField;
        }).toList();
        cgformFieldService.saveBatch(saveList);
        getBaseMapper().insert(view);
        return view;
    }

    @Override
    public void copyOnlineTableConfig(OnlCgformHead physicTable){
        copyOnlineTableConfigReturnView(physicTable);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public String editManyFormData(String code, JSONObject json) throws org.jeecg.modules.online.config.exception.a, BusinessException {
        OnlCgformHead cgformHead = this.getTable(code);
        String var4 = "edit";
        this.executeEnhanceJava(var4, "start", cgformHead, json);
        String tableName = cgformHead.getTableName();
        if (CHAR_BIG_Y.equals(cgformHead.getIsTree())) {
            this.fieldService.editTreeFormData(cgformHead.getId(), tableName, json, cgformHead.getTreeIdField(), cgformHead.getTreeParentIdField());
        } else {
            this.fieldService.editFormData(cgformHead.getId(), tableName, json, false);
        }

        if (TableTypeEnum.MASTER.getType().equals(cgformHead.getTableType())) {
            String subTableStr = cgformHead.getSubTableStr();
            if (oConvertUtils.isNotEmpty(subTableStr)) {
                for (String subTable : subTableStr.split(COMMA)) {
                    var var12 = cgformHeadService.getByTableName(subTable);
                    if (var12 != null) {
                        List<OnlCgformField> var13 = this.fieldService.list(new LambdaQueryWrapper<OnlCgformField>().eq(OnlCgformField::getCgformHeadId, var12.getId()));
                        String dbFiledName = "";
                        String mainFieldValue = null;

                        for (OnlCgformField cgformField : var13) {
                            if (!oConvertUtils.isEmpty(cgformField.getMainField())) {
                                dbFiledName = cgformField.getDbFieldName();
                                String mainField = cgformField.getMainField();
                                if (json.get(mainField.toLowerCase()) != null) {
                                    mainFieldValue = json.getString(mainField.toLowerCase());
                                }

                                if (json.get(mainField.toUpperCase()) != null) {
                                    mainFieldValue = json.getString(mainField.toUpperCase());
                                }
                            }
                        }

                        if (!oConvertUtils.isEmpty(mainFieldValue)) {
                            //TODO 需要判断字表是否开启了标记删除，如果开启了标记删除，则需要分开处理
                            this.fieldService.deleteAutoList(subTable, dbFiledName, mainFieldValue);
                            JSONArray jsonArray = json.getJSONArray(subTable);
                            if (jsonArray != null && jsonArray.size() != 0) {
                                for (int var20 = 0; var20 < jsonArray.size(); ++var20) {
                                    JSONObject var21 = jsonArray.getJSONObject(var20);
                                    var21.put(dbFiledName, mainFieldValue);
                                    this.fieldService.saveFormData(var13, subTable, var21);
                                }
                            }
                        }
                    }
                }
            }
        }

        this.executeEnhanceJava(var4, "end", cgformHead, json);
        this.executeEnhanceSql(var4, cgformHead.getId(), json);
        return tableName;
    }

    @Override
    public Map<String, Object> queryManyFormData(String code, String id) throws org.jeecg.modules.online.config.exception.a {
        OnlCgformHead cgformHead = this.getTable(code);
        var fields = this.fieldService.queryFormFields(cgformHead.getId(), true);
        if (CollectionUtils.isEmpty(fields)){
            throw new ParamException("找不到字段，请确认配置是否正确!");
        }
        var data = this.fieldService.queryFormData(fields, cgformHead.getTableName(), id);
        var filteredData = new HashMap<String, Object>(5);
        for (var field : fields) {
            var dbFieldName = field.getDbFieldName();
            if (data.containsKey(dbFieldName)) {
                filteredData.put(dbFieldName, data.get(dbFieldName));
            } else {
                // 如果找不到匹配的字段，可以抛出异常或执行其他处理
                throw new JeecgBootException("字段" + dbFieldName + "在数据中不存在!");
            }
        }
        if (TableTypeEnum.MASTER.getType().equals(cgformHead.getTableType())
                && oConvertUtils.isNotEmpty(cgformHead.getSubTableStr())) {
            for (String subTableName : cgformHead.getSubTableStr().split(COMMA)) {
                var subTableHead = cgformHeadService.getByTableName(subTableName);
                if (subTableHead != null) {
                    var subTableFields = this.fieldService.queryFormFields(subTableHead.getId(), false);

                    subTableFields.stream().filter(field->!oConvertUtils.isEmpty(field.getMainField())).findAny().ifPresent(field->{
                        var var14 = field.getDbFieldName();
                        String var18 = field.getMainField();
                        var var15 = DbDmlGenerator.a(filteredData, var18);
                        var subFormData = this.fieldService.querySubFormData(subTableFields, subTableName, var14, var15);
                        if (!CollectionUtils.isEmpty(subFormData)) {
                            filteredData.put(subTableName, DbDmlGenerator.d(subFormData));
                        } else {
                            filteredData.put(subTableName, new String[0]);
                        }
                    });
                }
            }
        }
        filteredData.put(ID, id);
        filteredData.put("jeecg_row_key", id);
        return filteredData;
    }

    @SneakyThrows
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void deleteOneTableInfo(String formId, String dataId) {
        OnlCgformHead cgformHead = this.getById(formId);
        if (cgformHead == null) {
            throw new BusinessException("未找到表配置信息");
        }
        String physicalTableName = DbDmlGenerator.f(cgformHead.getTableName());
        var dbData = this.baseMapper.queryOneByTableNameAndId(physicalTableName, dataId);
        if (dbData != null) {
            boolean existDeletedField = dbData.containsKey(DELETED_TABLE);
            dbData = DbDmlGenerator.a(dbData);
            String buttonCode = "delete";
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(dbData));
            this.executeEnhanceJava(buttonCode, "start", cgformHead, jsonObject);
            this.updateParentNode(cgformHead, dataId);
            if (TableTypeEnum.MASTER.getType().equals(cgformHead.getTableType())) {
                this.fieldService.deleteAutoListMainAndSub(cgformHead, dataId);
            } else {
                //如果存在逻辑删除字段，则进行标记删除操作，否则进行物理删除
                if(existDeletedField){
                    String updateSql = "update " + physicalTableName + " set deleted=1 where id = '" + dataId + "'";
                    fieldService.getBaseMapper().editFormData(updateSql);
                }
                else{
                    String deleteSql = "delete from " + physicalTableName + " where id = '" + dataId + "'";
                    fieldService.getBaseMapper().deleteAutoList(deleteSql);
                }
            }

            this.executeEnhanceSql(buttonCode, formId, jsonObject);
            this.executeEnhanceJava(buttonCode, "end", cgformHead, jsonObject);
        }
    }

    private void saveIndex(CgFormEntity model, CgformHeadDO head, AtomicReference<String> isDbSynch){
        var createIndexList = new ArrayList<OnlCgformIndex>();
        var updateIndexList = new ArrayList<OnlCgformIndex>();

        model.getIndexs().forEach(cgformIndex->{
            if (cgformIndex.getId() != null) {
                updateIndexList.add(cgformIndex);
            } else {
                cgformIndex.setId(null);
                cgformIndex.setIsDbSynch(CHAR_BIG_N);
                cgformIndex.setDelFlag(CommonConstant.DEL_FLAG_0);
                cgformIndex.setCgformHeadId(head.getId());
                createIndexList.add(cgformIndex);
            }
        });

        this.indexService.getCgformIndexsByCgformId(head.getId()).forEach(cgformIndex->{
            boolean exist = model.getIndexs().stream().anyMatch((var1) -> cgformIndex.getId().equals(var1.getId()));
            if (!exist) {
                cgformIndex.setDelFlag(CommonConstant.DEL_FLAG_1);
                updateIndexList.add(cgformIndex);
                isDbSynch.set(CHAR_BIG_N);
            }
        });


        if (createIndexList.size() > 0) {
            isDbSynch.set(CHAR_BIG_N);
            this.indexService.saveBatch(createIndexList);
        }


        updateIndexList.forEach(index->{
            OnlCgformIndex cgformIndex = this.indexService.getById(index.getId());
            if (DbDmlGenerator.a(cgformIndex, index)) {
                isDbSynch.set(CHAR_BIG_N);
                index.setIsDbSynch(CHAR_BIG_N);
            }

        });
        this.indexService.updateBatchById(updateIndexList);

    }
    private String generateViewName(String var1, String var2) {
        List<String> var3 = getBaseMapper().queryAllCopyTableName(var1);
        int var4 = 0;
        if (!CollectionUtils.isEmpty(var3)) {
            for (String var6 : var3) {
                int var7 = Integer.parseInt(var6.split("\\$")[1]);
                if (var7 > var4) {
                    var4 = var7;
                }
            }
        }
        return var2 + "$" + ++var4;
    }

    private void a(OnlCgformHead cgformHead, List<CgformFieldDO> fieldList) {
        if (TableTypeEnum.SLAVE.getType().equals(cgformHead.getTableType())) {
            cgformHead = getBaseMapper().selectById(cgformHead.getId());
            for (CgformFieldDO fieldDO : fieldList) {
                String mainTable = fieldDO.getMainTable();
                if (!oConvertUtils.isEmpty(mainTable)) {
                    var var6 = cgformHeadService.getByTableName(mainTable);
                    if (var6 != null) {
                        String subTableStr = var6.getSubTableStr();
                        if (oConvertUtils.isEmpty(subTableStr)) {
                            subTableStr = cgformHead.getTableName();
                        } else if (!this.b(cgformHead.getTableName(), subTableStr)) {
                            List<String> subTableList = new ArrayList<>(Arrays.asList(subTableStr.split(COMMA)));

                            for (int index = 0; index < subTableList.size(); ++index) {
                                String subTableName = subTableList.get(index);
                                OnlCgformHead subTableHead = cgformHeadService.getByTableName(subTableName);
                                if (subTableHead != null && cgformHead.getTabOrderNum() < oConvertUtils.getInt(subTableHead.getTabOrderNum(), 0)) {
                                    subTableList.add(index, cgformHead.getTableName());
                                    break;
                                }
                            }

                            if (!subTableList.contains(cgformHead.getTableName())) {
                                subTableList.add(cgformHead.getTableName());
                            }

                            subTableStr = String.join(COMMA, subTableList);
                        }

                        var6.setSubTableStr(subTableStr);
                        getBaseMapper().updateById(var6);
                        break;
                    }
                }
            }
        } else {
            List<OnlCgformHead> cgformHeadList = getBaseMapper().selectList(new LambdaQueryWrapper<OnlCgformHead>().like(OnlCgformHead::getSubTableStr, cgformHead.getTableName()));
            OnlCgformHead finalVar = cgformHead;
            cgformHeadList.forEach(head->{
                String subTableStr = head.getSubTableStr();
                List<String> var7 = Arrays.stream(subTableStr.split(COMMA)).filter(var11 -> !var11.equals(finalVar.getTableName())).toList();
                head.setSubTableStr(String.join(COMMA, var7));
            });
            updateBatchById(cgformHeadList);
        }

    }

    private void removeSubTable(String tableName, String subTableName) {
        if (oConvertUtils.isNotEmpty(tableName)) {
            var head = cgformHeadService.getByTableName(tableName);
            if (head != null && oConvertUtils.isNotEmpty(head.getSubTableStr())) {
                String subTableStr = head.getSubTableStr();
                List<String> var7 = Arrays.stream(subTableStr.split(COMMA)).filter(var11 -> !var11.equals(subTableName)).toList();
                head.setSubTableStr(String.join(COMMA, var7));
                getBaseMapper().updateById(head);
            }
        }

    }

    private void setTextFieldDefaultLength(OnlCgformField var1) {
        if ("Text".equals(var1.getDbType()) || "Blob".equals(var1.getDbType())) {
            var1.setDbLength(0);
            var1.setDbPointLength(0);
        }

    }

    private boolean dbIsPersist(List<CgformFieldDO> fieldList) {
        if (!CollectionUtils.isEmpty(fieldList)) {
            return fieldList.stream().anyMatch(field -> field.getDbIsPersist() == 1);
        } else {
            return false;
        }
    }

    private void updateCgformHeadFields(OnlCgformHead var1, List<CgformFieldDO> fieldDOList) {
        var cgformHeads = this.list((new LambdaQueryWrapper<OnlCgformHead>()).eq(OnlCgformHead::getPhysicId, var1.getId()));
        if (CollectionUtils.isEmpty(cgformHeads)) {
            return;
        }
        Iterator<OnlCgformHead> var4 = cgformHeads.iterator();
        List<String> dbFieldNameList = new ArrayList<>();
        List<CgformFieldDO> existFieldList;
        label108:
        do {
            while(var4.hasNext()) {
                OnlCgformHead head = var4.next();
                existFieldList = this.cgformFieldService.listFieldByHead(head.getId());
                CgformFieldDO var9;

                if (!CollectionUtils.isEmpty(existFieldList)) {
                    var var15 = new HashSet<String>();
                    for (CgformFieldDO cgformFieldDO : existFieldList) {
                        var9 = cgformFieldDO;
                        var15.add(var9.getDbFieldName());
                    }
                    var collect = fieldDOList.stream().collect(Collectors.toMap(OnlCgformField::getDbFieldName, var10 -> 1, (a, b) -> b, () -> new HashMap<>(5)));

                    var var20 = new ArrayList<String>();
                    collect.keySet().forEach(dbFieldName -> {
                        if (!var15.contains(dbFieldName)) {
                            var20.add(dbFieldName);
                        } else {
                            dbFieldNameList.add(dbFieldName);
                        }
                    });

                    var var21 = var15.stream().filter(var13 -> collect.get(var13) == null).toList();

                    if (var21.size() > 0) {
                        existFieldList.forEach(item->{
                            if (var21.contains(item.getDbFieldName())) {
                                this.fieldService.removeById(item.getId());
                            }
                        });

                    }

                    if (var20.size() > 0) {
                        var saveList = fieldDOList.stream().filter(field->var20.contains(field.getDbFieldName()))
                                .map(field->{
                                    CgformFieldDO fieldDO = new CgformFieldDO();
                                    fieldDO.setCgformHeadId(head.getId());
                                    copy(field, fieldDO);
                                    return fieldDO;
                                }).toList();
                        cgformFieldService.saveBatch(saveList);

                    }
                    continue label108;
                }

                this.cgformFieldService.saveBatch(fieldDOList.stream().map(field->{
                    var newField = new CgformFieldDO();
                    newField.setCgformHeadId(head.getId());
                    this.copy(field, newField);
                    return newField;
                }).toList());

            }

            return;
        } while(dbFieldNameList.size() == 0);

        for (String var13 : dbFieldNameList) {
            this.b(var13, fieldDOList, existFieldList);
        }
    }

    private boolean b(String var1, String var2) {
        if (oConvertUtils.isEmpty(var2)) {
            return false;
        } else {
            String[] var3 = var2.split(COMMA);
            for (String var7 : var3) {
                if (var7.equalsIgnoreCase(var1)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void copy(OnlCgformField source, OnlCgformField target) {
        if(source instanceof CgformFieldDO && target instanceof CgformFieldDO){
            ((CgformFieldDO)target).setFillRule(((CgformFieldDO)source).getFillRule());
        }
        target.setDbDefaultVal(source.getDbDefaultVal());
        target.setDbFieldName(source.getDbFieldName());
        target.setDbFieldNameOld(source.getDbFieldNameOld());
        target.setDbFieldTxt(source.getDbFieldTxt());
        target.setDbIsKey(source.getDbIsKey());
        target.setDbIsNull(source.getDbIsNull());
        target.setDbLength(source.getDbLength());
        target.setDbPointLength(source.getDbPointLength());
        target.setDbType(source.getDbType());
        target.setDictField(source.getDictField());
        target.setDictTable(source.getDictTable());
        target.setDictText(source.getDictText());
        target.setFieldExtendJson(source.getFieldExtendJson());
        target.setFieldHref(source.getFieldHref());
        target.setFieldLength(source.getFieldLength());
        target.setFieldMustInput(source.getFieldMustInput());
        target.setFieldShowType(source.getFieldShowType());
        target.setFieldValidType(source.getFieldValidType());
        target.setFieldDefaultValue(source.getFieldDefaultValue());
        target.setIsQuery(source.getIsQuery());
        target.setIsShowForm(source.getIsShowForm());
        target.setIsShowList(source.getIsShowList());
        target.setMainField(source.getMainField());
        target.setMainTable(source.getMainTable());
        target.setOrderNum(source.getOrderNum());
        target.setQueryMode(source.getQueryMode());
        target.setIsReadOnly(source.getIsReadOnly());
        target.setSortFlag(source.getSortFlag());
        target.setQueryDefVal(source.getQueryDefVal());
        target.setQueryConfigFlag(source.getQueryConfigFlag());
        target.setQueryDictField(source.getQueryDictField());
        target.setQueryDictTable(source.getQueryDictTable());
        target.setQueryDictText(source.getQueryDictText());
        target.setQueryMustInput(source.getQueryMustInput());
        target.setQueryShowType(source.getQueryShowType());
        target.setQueryValidType(source.getQueryValidType());
        target.setConverter(source.getConverter());
        target.setDbIsPersist(source.getDbIsPersist());
    }

    /***
     * 对编码规则进行校验
     */
    private void checkFillRule(CgformFieldDO field){
        //判断该校验规则是否存在
        if(field != null && !ObjectUtils.isEmpty(field.getFillRule())){
            //不能针对主键设置填充规则
            if(field.getDbIsKey() == 1){
                throw new ParamException("不能对主键设置填充规则");
            }
            if(field.getIsReadOnly() != 1){
                throw new ParamException("字段[" + field.getDbFieldName() + "]必须为只读才可以设置填充规则");
            }
//            if(fillRuleService.getByCode(field.getFillRule()).isEmpty()){
//                throw new ParamException("填充规则"+ field.getFillRule() + "规则不存在,请检查");
//            }

        }
    }

    private void b(String dbFieldName, List<CgformFieldDO> oraginFieldList, List<CgformFieldDO> fieldList) {
        CgformFieldDO foundField =  oraginFieldList.stream().filter(field->dbFieldName.equals(field.getDbFieldName()))
                .findAny().orElse(null);

        CgformFieldDO newFoundField =  fieldList.stream().filter(field->dbFieldName.equals(field.getDbFieldName()))
                .findAny().orElse(null);


        if (foundField != null && newFoundField != null) {
            boolean needToUpdate = false;
            if (!foundField.getDbType().equals(newFoundField.getDbType())) {
                newFoundField.setDbType(foundField.getDbType());
                needToUpdate = true;
            }

            if (foundField.getDbDefaultVal() != null && !foundField.getDbDefaultVal().equals(newFoundField.getDbDefaultVal())) {
                newFoundField.setDbDefaultVal(foundField.getDbDefaultVal());
                needToUpdate = true;
            }

            if (!foundField.getDbLength().equals(newFoundField.getDbLength())) {
                newFoundField.setDbLength(foundField.getDbLength());
                needToUpdate = true;
            }

            if (!foundField.getDbIsNull().equals(newFoundField.getDbIsNull())) {
                newFoundField.setDbIsNull(foundField.getDbIsNull());
                needToUpdate = true;
            }

            if (needToUpdate) {
                cgformFieldService.updateById(newFoundField);
            }
        }

    }


}
