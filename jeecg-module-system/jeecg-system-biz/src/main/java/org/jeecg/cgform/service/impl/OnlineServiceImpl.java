package org.jeecg.cgform.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.cgform.database.domain.CgformFieldDO;
import org.jeecg.cgform.infrastructure.enums.TableTypeEnum;
import org.jeecg.cgform.service.CgformFieldService;
import org.jeecg.cgform.service.CgformHeadService;
import org.jeecg.cgform.utils.DbDmlGenerator;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.online.auth.service.IOnlAuthPageService;
import org.jeecg.modules.online.cgform.a.a;
import org.jeecg.modules.online.cgform.entity.OnlCgformEnhanceJs;
import org.jeecg.modules.online.cgform.entity.OnlCgformField;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;
import org.jeecg.modules.online.cgform.model.*;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Stream;

import static org.jeecg.common.constant.CommonConstant.CHAR_BIG_Y;
import static org.jeecg.common.constant.DataBaseConstant.ID;
import static org.jeecg.common.constant.SymbolConstant.COMMA;

@Slf4j
@Primary
@Service("OnlineServiceImpl_n")
public class OnlineServiceImpl extends org.jeecg.modules.online.cgform.service.a.i{

    private final OnlCgformHeadServiceImpl onlCgformHeadService;

    private final CgformFieldService cgformFieldService;

    private final CgformHeadService cgformHeadService;

    @Lazy
    private final SysBaseApiImpl sysBaseAPI;

    private final IOnlAuthPageService onlAuthPageService;

    public OnlineServiceImpl(OnlCgformHeadServiceImpl onlCgformHeadService, CgformFieldService cgformFieldService, CgformHeadService cgformHeadService, SysBaseApiImpl sysBaseAPI, IOnlAuthPageService onlAuthPageService) {
        this.onlCgformHeadService = onlCgformHeadService;
        this.cgformFieldService = cgformFieldService;
        this.cgformHeadService = cgformHeadService;
        this.sysBaseAPI = sysBaseAPI;
        this.onlAuthPageService = onlAuthPageService;
    }

    @Override
    public b queryOnlineConfig(OnlCgformHead head, String username) {
        String headId = head.getId();
        boolean isJoinQuery = DbDmlGenerator.a(head);
        var fieldList = cgformFieldService.listFieldByHead(head.getId());
        var hideCode = this.onlAuthPageService.queryHideCode(headId, true);
        List<OnlColumn> var7 = new ArrayList<>();
        HashMap<String,List<DictModel>> var8 = new HashMap<>(5);
        List<HrefSlots> var9 = new ArrayList<>();
        List<c> var10 = new ArrayList<>();
        List<String> var11 = new ArrayList<>();
        Map<String,Integer> var12 = new HashMap<>(5);
        var var13 = head.getSelectFieldList();
        var var14 = fieldList.iterator();

        String mainTable;
        String mainField;



        while(var14.hasNext()) {
            var cgformField = var14.next();
            String var16 = cgformField.getDbFieldName();
            mainTable = cgformField.getMainTable();
            mainField = cgformField.getMainField();
            if (oConvertUtils.isNotEmpty(mainField) && oConvertUtils.isNotEmpty(mainTable)) {
                c var19 = new c(var16, mainField);
                var10.add(var19);
            }

            if (cgformField.getIsShowList() != null && 1 == cgformField.getIsShowList() && ! ID.equals(var16) && !hideCode.contains(var16) && !var11.contains(var16) && (var13 == null || var13.size() == 0 || var13.contains(var16))) {
                OnlColumn var31 = this.generateOnlColumn(cgformField, var8, var9);
                var12.put(cgformField.getDbFieldName(), 1);
                var7.add(var31);
                String var20 = var31.getLinkField();
                if (var20 != null && !var20.isEmpty()) {
                    this.generateOnlColumn(new ArrayList<>(fieldList), var11, var7, var16, var20);
                }
            }
        }

        this.generateOnlColumn(var7, var11);
        if (isJoinQuery) {
            List<OnlColumn> columnList = this.generateOnlColumn(head, var8, var9, var12);
            if (!columnList.isEmpty()) {
                List<String> var23 = var12.keySet().stream().filter(o -> var12.get(o) > 1).toList();

                OnlColumn var27;
                for(var var25 = columnList.iterator(); var25.hasNext(); var7.add(var27)) {
                    var27 = var25.next();
                    mainField = var27.getDataIndex();
                    if (var23.contains(mainField)) {
                        var27.setDataIndex(var27.getTableName() + "_" + mainField);
                    }
                }
            }
        }

        b var22 = new b();
        var22.setCode(headId);
        var22.setTableType(head.getTableType());
        var22.setFormTemplate(head.getFormTemplate());
        var22.setDescription(head.getTableTxt());
        var22.setCurrentTableName(head.getTableName());
        var22.setPaginationFlag(head.getIsPage());
        var22.setCheckboxFlag(head.getIsCheckbox());
        var22.setScrollFlag(head.getScroll());
        var22.setRelationType(head.getRelationType());
        var22.setColumns(var7);
        var22.setDictOptions(var8);
        var22.setFieldHrefSlots(var9);
        var22.setForeignKeys(var10);
        var22.setHideColumns(hideCode);
        var var24 = this.onlCgformHeadService.queryButtonList(headId, true);
        var var26 = var24.stream().filter(var30 -> !hideCode.contains(var30.getButtonCode())).toList();

        var22.setCgButtonList(var26);
        OnlCgformEnhanceJs var29 = this.onlCgformHeadService.queryEnhanceJs(headId, "list");
        if (var29 != null && oConvertUtils.isNotEmpty(var29.getCgJs())) {
            mainField = org.jeecg.modules.online.cgform.d.c.b(var29.getCgJs(), var24);
            var22.setEnhanceJs(mainField);
        }

        if (CHAR_BIG_Y.equals(head.getIsTree())) {
            var22.setPidField(head.getTreeParentIdField());
            var22.setHasChildrenField(head.getTreeIdField());
            var22.setTextField(head.getTreeFieldname());
        }

        return var22;
    }

    @Override
    public JSONObject getOnlineVue3QueryInfo(String headId) {
        OnlCgformHead var2 = this.onlCgformHeadService.getById(headId);
        boolean var3 = DbDmlGenerator.a(var2);
        List<String> var4 = new ArrayList<>();
        JSONObject var5 = this.generateOnlColumn(headId, var4, true, null);
        JSONObject var6 = var5.getJSONObject("properties");
        var5.put("title", var2.getTableTxt());
        var5.put("table", var2.getTableName());
        var5.put("joinQuery", var3);
        var5.put("searchFieldList", var4);
        if (DbDmlGenerator.aE.equals(var2.getTableType())) {
            String var7 = var2.getSubTableStr();
            if (var7 != null && !var7.isEmpty()) {
                String[] var8 = var7.split(COMMA);

                for (String var12 : var8) {
                    OnlCgformHead head = this.cgformHeadService.getByTableName(var12);
                    if (head != null) {
                        JSONObject var14 = this.generateOnlColumn(head.getId(), var4, false, var12);
                        var14.put("title", head.getTableTxt());
                        var14.put("view", "table");
                        var6.put(var12, var14);
                    }
                }
            }
        }

        return var5;
    }


    private OnlColumn generateOnlColumn(OnlCgformField var1, Map<String, List<DictModel>> var2, List<HrefSlots> var3) {
        String var4 = var1.getDbFieldName();
        OnlColumn var5 = new OnlColumn(var1.getDbFieldTxt(), var4);
        var5.setDbType(var1.getDbType());
        String var6 = var1.getDictField();
        String var7 = var1.getFieldShowType();
        if (var7 != null) {
            if (oConvertUtils.isNotEmpty(var6) && !"popup".equals(var7) && !"link_table".equals(var7)) {
                List<DictModel> dictModels = new ArrayList<>();
                if (oConvertUtils.isNotEmpty(var1.getDictTable())) {
                    dictModels = this.sysBaseAPI.queryTableDictItemsByCode(var1.getDictTable(), var1.getDictText(), var6);
                } else if (oConvertUtils.isNotEmpty(var1.getDictField())) {
                    dictModels = this.sysBaseAPI.queryDictItemsByCode(var6);
                }

                var2.put(var4, dictModels);
                var5.setCustomRender(var4);
            }

            if ("switch".equals(var7)) {
                var var13 = DbDmlGenerator.b(var1);
                var2.put(var4, var13);
                var5.setCustomRender(var4);
            }

            if ("link_table_field".equals(var7)) {
                var5.setFieldType(var7);
            }

            if ("link_table".equals(var7)) {
                var5.setFieldType(var7);
                var5.setHrefSlotName(var1.getDictTable());
            }

            List<DictModel> var10;
            String var15;
            if ("link_down".equals(var7)) {
                var15 = var1.getDictTable();
                org.jeecg.modules.online.cgform.a.a var9 = JSONObject.parseObject(var15, a.class);

                try {
                    var10 = this.sysBaseAPI.queryTableDictItemsByCode(var9.getTable(), var9.getTxt(), var9.getKey());
                    var2.put(var4, var10);
                    var5.setCustomRender(var4);
                    var5.setLinkField(var9.getLinkField());
                } catch (Exception var12) {
                    log.warn("联动组件配置错误!:{}", var12.getMessage());
                }
            }

            String[] var17;
            if ("sel_tree".equals(var7)) {
                var17 = var1.getDictText().split(COMMA);
                var var14 = this.sysBaseAPI.queryTableDictItemsByCode(var1.getDictTable(), var17[2], var17[0]);
                var2.put(var4, var14);
                var5.setCustomRender(var4);
            }

            String var16;
            if ("cat_tree".equals(var7)) {
                var15 = var1.getDictText();
                if (oConvertUtils.isEmpty(var15)) {
                    var16 = DbDmlGenerator.e(var1.getDictField());
                    var10 = this.sysBaseAPI.queryFilterTableDictInfo("SYS_CATEGORY", "NAME", "ID", var16);
                    var2.put(var4, var10);
                    var5.setCustomRender(var4);
                } else {
                    var5.setCustomRender("_replace_text_" + var15);
                }
            }

            String var18;
            if ("sel_depart".equals(var7)) {
                var17 = this.generateOnlColumn(var1.getFieldExtendJson());
                var16 = !var17[0].isEmpty() ? var17[0] : "ID";
                var18 = !var17[1].isEmpty() ? var17[1] : "DEPART_NAME";
                var var11 = this.sysBaseAPI.queryTableDictItemsByCode("SYS_DEPART", var18, var16);
                var2.put(var4, var11);
                var5.setCustomRender(var4);
            }

            if ("sel_user".equals(var1.getFieldShowType())) {
                var17 = this.generateOnlColumn(var1.getFieldExtendJson());
                var16 = !var17[0].isEmpty() ? var17[0] : "USERNAME";
                var18 = !var17[1].isEmpty() ? var17[1] : "REALNAME";
                var var11 = this.sysBaseAPI.queryTableDictItemsByCode("SYS_USER", var18, var16);
                var2.put(var4, var11);
                var5.setCustomRender(var4);
            }

            if (var7.contains("file")) {
                var5.setScopedSlots(new g("fileSlot"));
            } else if (var7.contains("image")) {
                var5.setScopedSlots(new g("imgSlot"));
            } else if (var7.contains("editor")) {
                var5.setScopedSlots(new g("htmlSlot"));
            } else if ("date".equals(var7)) {
                var5.setScopedSlots(new g("dateSlot"));
            } else if ("pca".equals(var7)) {
                var5.setScopedSlots(new g("pcaSlot"));
            }

            if (StringUtils.isNotBlank(var1.getFieldHref())) {
                var15 = "fieldHref_" + var4;
                var5.setHrefSlotName(var15);
                var3.add(new HrefSlots(var15, var1.getFieldHref()));
            }

            if ("1".equals(var1.getSortFlag())) {
                var5.setSorter(true);
            }

            var15 = var1.getFieldExtendJson();
            if (oConvertUtils.isNotEmpty(var15) && var15.indexOf("showLength") > 0) {
                JSONObject var19 = JSON.parseObject(var15);
                if (var19 != null && var19.get("showLength") != null) {
                    var5.setShowLength(oConvertUtils.getInt(var19.get("showLength")));
                }
            }

        }
        return var5;
    }

    private void generateOnlColumn(List<OnlColumn> var1, List<String> var2) {
        Iterator<OnlColumn> var3 = var1.iterator();

        while(var3.hasNext()) {
            OnlColumn var4 = var3.next();
            String var5 = var4.getDataIndex();
            if (var2 != null && var2.contains(var5) && oConvertUtils.isEmpty(var4.getCustomRender())) {
                var3.remove();
            }
        }

    }

    private void generateOnlColumn(List<OnlCgformField> fieldList, List<String> fieldNameList, List<OnlColumn> columnList, String customRender, String fieldNames) {
        if (!oConvertUtils.isNotEmpty(fieldNames)) {
           return;
        }
        Arrays.stream(fieldNames.split(COMMA))
                .filter(fieldName -> fieldList.stream()
                        .anyMatch(field -> 1 == field.getIsShowList() && fieldName.equals(field.getDbFieldName())))
                .forEach(fieldName -> {
                    var field = fieldList.stream()
                            .filter(f -> 1 == f.getIsShowList() && fieldName.equals(f.getDbFieldName()))
                            .findFirst()
                            .orElse(null);

                    if (field != null) {
                        fieldNameList.add(fieldName);
                        OnlColumn column = new OnlColumn(field.getDbFieldTxt(), field.getDbFieldName());
                        column.setCustomRender(customRender);
                        columnList.add(column);
                    }
                });

    }

    private List<OnlColumn> generateOnlColumn(OnlCgformHead head, Map<String, List<DictModel>> var2, List<HrefSlots> var3, Map<String, Integer> var4) {
        List<OnlColumn> columnList = new ArrayList<>();
        if (TableTypeEnum.MASTER.getType().equals(head.getTableType())) {
            String subTableStr = head.getSubTableStr();
            if (!ObjectUtils.isEmpty(subTableStr)) {
                String[] subTables = subTableStr.split(COMMA);

                Stream.of(subTables).forEach(tableName -> {
                    var cgformHead = cgformHeadService.getByTableName(tableName);

                    if (cgformHead != null) {
                        var hideCode = onlAuthPageService.queryHideCode(cgformHead.getId(), true);
                        var fieldList = cgformFieldService.listFieldByHead(cgformHead.getId());

                        fieldList.stream()
                                .filter(field -> (1 == field.getIsShowList() || 1 == field.getIsQuery())
                                        && !hideCode.contains(field.getDbFieldName())
                                        && !ID.equals(field.getDbFieldName()))
                                .forEach(field -> {
                                    var4.merge(field.getDbFieldName(), 1, Integer::sum);
                                    var column = generateOnlColumn(field, var2, var3);
                                    if (1 == field.getIsShowList()) {
                                        column.setTableName(cgformHead.getTableName());
                                        columnList.add(column);
                                    }
                                });
                    }
                });
            }
        }

        return columnList;
    }

    private String[] generateOnlColumn(String var1) {
        String[] var2 = new String[]{"", ""};
        if (var1 != null && !var1.isEmpty()) {
            JSONObject var3 = JSON.parseObject(var1);
            if (var3.containsKey("store")) {
                var2[0] = oConvertUtils.camelToUnderline(var3.getString("store"));
            }

            if (var3.containsKey("text")) {
                var2[1] = oConvertUtils.camelToUnderline(var3.getString("text"));
            }
        }

        return var2;
    }

    private JSONObject generateOnlColumn(String var1, List<String> var2, boolean var3, String var4) {
        LambdaQueryWrapper<CgformFieldDO> var5 = new LambdaQueryWrapper<>();
        var5.eq(OnlCgformField::getCgformHeadId, var1);
        var5.and((var0) -> var0.eq(CgformFieldDO::getIsShowList, 1).or().eq(CgformFieldDO::getIsQuery, 1));
        var5.eq(OnlCgformField::getDbIsPersist, 1);
        var5.orderByAsc(OnlCgformField::getOrderNum);
        var var6 = this.cgformFieldService.list(var5);

        for (OnlCgformField var8 : var6) {
            var8.setFieldDefaultValue(null);
            if ("1".equals(var8.getQueryConfigFlag())) {
                var8.setFieldDefaultValue(var8.getQueryDefVal());
                var8.setDictField(var8.getQueryDictField());
                var8.setDictTable(var8.getQueryDictTable());
                var8.setDictText(var8.getQueryDictText());
                var8.setFieldShowType(var8.getQueryShowType());
            }

            if (1 == var8.getIsQuery()) {
                if (var3) {
                    var2.add(var8.getDbFieldName());
                } else {
                    var2.add(var4 + "@" + var8.getDbFieldName());
                }
            }
        }

        JSONObject var9 = DbDmlGenerator.a(new ArrayList<>(var6), null, (org.jeecg.modules.online.cgform.model.i)null);
        DbDmlGenerator.b(var9);
        return var9;
    }
}
