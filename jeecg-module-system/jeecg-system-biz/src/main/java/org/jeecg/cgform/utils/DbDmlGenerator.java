package org.jeecg.cgform.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.cgform.database.domain.CgformFieldDO;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.online.cgform.entity.OnlCgformField;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.jeecg.common.constant.DataBaseConstant.ID;
import static org.jeecg.common.constant.SymbolConstant.COMMA;
import static org.jeecg.modules.data.constant.SqlConstant.EQUAL;

/**
 * @author jiangyan
 */
@Slf4j
public class DbDmlGenerator extends org.jeecg.modules.online.cgform.d.b{

    private static final String PROPERTIES = "properties";

    public static Map<String, Object> generatorInsertData(boolean isIdentity, String tableName, List<CgformFieldDO> fieldList, JSONObject jsonObject) {
        String dataBaseType = "";
        try {
            dataBaseType = org.jeecg.modules.online.config.d.e.getDatabaseType();
        } catch (SQLException | org.jeecg.modules.online.config.exception.a err) {
            log.error("",err);
        }

        HashMap<String,Object> dataMap = new HashMap<>(5);
        LoginUser var9 = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (var9 == null) {
            throw new SecurityException("online保存表单数据异常:系统未找到当前登陆用户信息");
        }

        Set<String> var10 = filterTreeField(new ArrayList<>(fieldList));
        String tenantIdField = "tenant_id";
        String var12 = f(tableName);
        boolean isTenantTable = j(var12);


        if (isTenantTable) {
            var sql = SpringContextUtils.getHttpServletRequest().getHeader("X-Tenant-Id");
            dataMap.put(tenantIdField, sql);
        }
        if(!isIdentity){
            String id = jsonObject.getString(ID);
            if (oConvertUtils.isEmpty(id)) {
                id = a();
            }
            dataMap.put(ID, id);
        }

        String finalDataBaseType = dataBaseType;
        fieldList.stream()
                .filter(field->field.getDbIsPersist() == 1)
                .filter(field->field.getDbFieldName() != null)
                .filter(field->!tenantIdField.equalsIgnoreCase(field.getDbFieldName()))
                .filter(field->!isTenantTable)
                .forEach(field->{
                    var fieldName = field.getDbFieldName();
                    if ("bpm_status".equalsIgnoreCase(fieldName)) {
                        dataMap.put("bpm_status", 1);
                    }
                    else{
                        a(field, var9, jsonObject, "CREATE_BY", "CREATE_TIME", "SYS_ORG_CODE");
                        String var17;
                        if (var10.contains(fieldName)) {
                            var17 = org.jeecg.modules.online.cgform.d.g.a(finalDataBaseType, field, jsonObject, dataMap);
                            dataMap.put(fieldName, var17);
                        } else if (field.getIsShowForm() == 1 || ObjectUtils.isNotEmpty(field.getFillRule()) || !oConvertUtils.isEmpty(field.getMainField()) || !oConvertUtils.isEmpty(field.getDbDefaultVal())) {
                            if (oConvertUtils.isEmpty(jsonObject.get(fieldName))
                                    && !oConvertUtils.isEmpty(field.getDbDefaultVal())) {
                                jsonObject.put(fieldName, field.getDbDefaultVal());
                            }

                            if ("".equals(jsonObject.get(fieldName))) {
                                var dbType = field.getDbType();
                                if (!(org.jeecg.modules.online.cgform.d.g.a(dbType)
                                        || org.jeecg.modules.online.cgform.d.g.b(dbType))) {
                                    var value = org.jeecg.modules.online.cgform.d.g.a(finalDataBaseType, field, jsonObject, dataMap);
                                    dataMap.put(fieldName, value);
                                }
                            }
                            else{
                                dataMap.put(fieldName, jsonObject.get(fieldName));
                            }
                        }
                    }
                });

        return dataMap;

    }

    public static Map<String, Object> generatorUpdateData(List<OnlCgformField> fieldList, JSONObject jsonObject) {
        HashMap<String,Object> paramMap = new HashMap<>(5);
        LoginUser loginUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        if (loginUser == null) {
            throw new JeecgBootException("online修改表单数据异常:系统未找到当前登陆用户信息");
        }
        var var7 = filterTreeField(fieldList);

        for (OnlCgformField cgformField : fieldList) {
            if (!org.jeecg.modules.online.cgform.b.b.b.equals(cgformField.getDbIsPersist())) {
                continue;
            }
            String dbFiledName = cgformField.getDbFieldName();
            if (dbFiledName == null) {
                continue;
            }
            a(cgformField, loginUser, jsonObject, "UPDATE_BY", "UPDATE_TIME");


            if (var7.contains(dbFiledName) && jsonObject.get(dbFiledName) != null && !"".equals(jsonObject.getString(dbFiledName))) {
//                String dbType = org.jeecg.modules.online.cgform.d.g.a(dataBaseType, cgformField, jsonObject, paramMap);
                paramMap.put(dbFiledName, getActualValue(cgformField, jsonObject));
            } else if (cgformField.getIsShowForm() == 1 && !ID.equals(dbFiledName)) {
                if ("".equals(jsonObject.get(dbFiledName))) {
                    String dbType = cgformField.getDbType();
                    if (org.jeecg.modules.online.cgform.d.g.a(dbType) || org.jeecg.modules.online.cgform.d.g.b(dbType)) {
                        continue;
                    }
                }
                //针对是主表的情况
                if (!oConvertUtils.isNotEmpty(cgformField.getMainTable()) || !oConvertUtils.isNotEmpty(cgformField.getMainField()) || oConvertUtils.isNotEmpty(jsonObject.get(dbFiledName))) {
//                    String dbType = org.jeecg.modules.online.cgform.d.g.a(dataBaseType, cgformField, jsonObject, paramMap);
                    paramMap.put(dbFiledName, getActualValue(cgformField, jsonObject));
                } else if (!jsonObject.containsKey(dbFiledName) && cgformField.getDbIsNull() == 1 && !ObjectUtils.isEmpty(cgformField.getDbDefaultVal())) {
                    paramMap.put(dbFiledName, cgformField.getDbDefaultVal());
                }
            }
        }

        paramMap.put(ID, jsonObject.getString(ID));

        return paramMap;
    }

    public static Set<String> filterTreeField(List<OnlCgformField> fieldList) {
        HashSet<String> var1 = new HashSet<>();

        fieldList.forEach(field->{
            if ("popup".equals(field.getFieldShowType())) {
                var var4 = field.getDictText();
                if (var4 != null && !var4.isEmpty()) {
                    var1.addAll(Arrays.stream(var4.split(COMMA)).collect(Collectors.toSet()));
                }
            }

            if ("cat_tree".equals(field.getFieldShowType())) {
                var var4 = field.getDictText();
                if (oConvertUtils.isNotEmpty(var4)) {
                    var1.add(var4);
                }
            }
        });


//        fieldList.forEach(field->{
//            var  var4 = field.getDbFieldName();
//            if (field.getIsShowForm() == 1 && var1.contains(var4)) {
//                var1.remove(var4);
//            }
//        });
        return var1;
    }

    public static JSONObject b(JSONObject jsonObject) {
        JSONObject var1;
        if (jsonObject.containsKey(PROPERTIES)) {
            var1 = jsonObject.getJSONObject(PROPERTIES);
        } else {
            JSONObject var2 = jsonObject.getJSONObject("schema");
            var1 = var2.getJSONObject(PROPERTIES);
        }

        SysBaseApiImpl sysBaseApi = SpringContextUtils.getBean(SysBaseApiImpl.class);
        for (String var4 : var1.keySet()) {
            JSONObject var5 = var1.getJSONObject(var4);
            String var6 = var5.getString("view");
            String var7;
            if (isMultiValue(var6)) {
                var7 = var5.getString("dictCode");
                String var16 = var5.getString("dictText");
                String var17 = var5.getString("dictTable");
                List<DictModel> var18 = new ArrayList<>();
                if (oConvertUtils.isNotEmpty(var17)) {
                    var18 = sysBaseApi.queryTableDictItemsByCode(var17, var16, var7);
                } else if (oConvertUtils.isNotEmpty(var7)) {
                    var18 = sysBaseApi.queryEnableDictItemsByCode(var7);
                }

                if (var18 != null && var18.size() > 0) {
                    var5.put("enum", var18);
                }
            } else if ("tab".equals(var6)) {
                var7 = var5.getString("relationType");
                if ("1".equals(var7)) {
                    b(var5);
                } else {
                    JSONArray var8 = var5.getJSONArray("columns");
                    for (int var9 = 0; var9 < var8.size(); ++var9) {
                        JSONObject json = var8.getJSONObject(var9);
                        if (isMultiValue(json)) {
                            String var11 = json.getString("dictCode");
                            String var12 = json.getString("dictText");
                            String var13 = json.getString("dictTable");
                            List<DictModel> var14 = new ArrayList<>();
                            if (oConvertUtils.isNotEmpty(var13)) {
                                var14 = sysBaseApi.queryTableDictItemsByCode(var13, var12, var11);
                            } else if (oConvertUtils.isNotEmpty(var11)) {
                                var14 = sysBaseApi.queryEnableDictItemsByCode(var11);
                            }

                            if (var14 != null && var14.size() > 0) {
                                json.put("options", var14);
                            }
                        }
                    }
                }
            }
        }
        return jsonObject;
    }

    public static boolean isMultiValue(String var0) {
        if ("list".equals(var0)) {
            return true;
        } else if ("radio".equals(var0)) {
            return true;
        } else if ("checkbox".equals(var0)) {
            return true;
        } else {
            return "list_multi".equals(var0);
        }
    }

    private static boolean isMultiValue(JSONObject var0) {
        Object var1 = var0.get("view");
        if (var1 != null) {
            String var2 = var1.toString();
            return "list".equals(var2) || "radio".equals(var2) || "checkbox_meta".equals(var2) || "list_multi".equals(var2) || "sel_search".equals(var2);
        }
        return false;
    }

    public static String generatorSelectSql(String var0, List<CgformFieldDO> var1, String var2) {
        return generatorSelectSql(var0, var1, "id", var2);
    }

    public static String generatorSelectSql(String tableName, List<CgformFieldDO> fieldList, String keyFieldName, String keyFieldValue) {
        StringBuilder stringBuilder = new StringBuilder("SELECT ");
        boolean existId = false;

        for(int index = 0; index < fieldList.size(); ++index) {
            var field = fieldList.get(index);
            if (field.getDbIsPersist() == 1) {
                String var9 = field.getDbFieldName();
                if (ID.equals(var9)) {
                    existId = true;
                }

                stringBuilder.append(var9);
                if (fieldList.size() > index + 1) {
                    stringBuilder.append(COMMA);
                }
            }
        }

        String var10 = stringBuilder.substring(stringBuilder.length() - 1);
        if (COMMA.equals(var10)) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }

        if (!existId) {
            stringBuilder.append(",id");
        }

        stringBuilder.append(" FROM ").append(f(tableName)).
                append(" where 1=1  ").append(" AND ").
                append(keyFieldName).append(EQUAL).append("'").append(keyFieldValue).append("'");
        return stringBuilder.toString();
    }

    public static void generatorSelectSql(String tableName, List<CgformFieldDO> fieldList, StringBuilder sqlBuilder) {
        if (!CollectionUtils.isEmpty(fieldList)) {
            sqlBuilder.append("SELECT ");
            int size = fieldList.size();
            boolean existId = false;

            for(int index = 0; index < size; ++index) {
                var field = fieldList.get(index);
                if (field.getDbIsPersist() == 1) {
                    if (ID.equals(field.getDbFieldName())) {
                        existId = true;
                    }

                    if ("cat_tree".equals(field.getFieldShowType()) && oConvertUtils.isNotEmpty(field.getDictText())) {
                        sqlBuilder.append(field.getDictText()).append(COMMA);
                    }

                    if (index == size - 1) {
                        sqlBuilder.append(field.getDbFieldName()).append(" ");
                    } else {
                        sqlBuilder.append(field.getDbFieldName()).append(COMMA);
                    }
                }
            }

            String var7 = sqlBuilder.substring(sqlBuilder.length() - 1);
            if (COMMA.equals(var7)) {
                sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
            }

            if (!existId) {
                sqlBuilder.append(",id");
            }
        } else {
            sqlBuilder.append("SELECT id");
        }

        sqlBuilder.append(" FROM ").append(f(tableName));
    }

    public static List<org.jeecg.modules.online.config.b.e> filterIsPersistFields(List<CgformFieldDO> fields) {
        return fields.stream().filter(field->field.getDbIsPersist() == 1)
                .map(org.jeecg.modules.online.config.b.e::new).toList();
    }

    public static Object getActualValue(OnlCgformField var1, JSONObject var2) {
        String var4 = var1.getDbType();
        String var5 = var1.getDbFieldName();
        if (var2.get(var5) == null) {
            return null;
        } else if ("int".equals(var4)) {
            return var2.getIntValue(var5);
        } else if ("double".equals(var4)) {
            return var2.getDoubleValue(var5);
        } else if ("BigDecimal".equals(var4)) {
            return new BigDecimal(var2.getString(var5));
        } else if ("Blob".equals(var4)) {
            return var2.getString(var5) != null ? var2.getString(var5).getBytes() : null;
        } else if (!"Date".equals(var4) && !"datetime".equalsIgnoreCase(var4)) {
            return var2.getString(var5);
        } else {
            return var2.getString(var5);
        }
    }

}
