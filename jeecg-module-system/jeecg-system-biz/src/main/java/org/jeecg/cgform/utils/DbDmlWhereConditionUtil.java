//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.jeecg.cgform.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.system.query.MatchTypeEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.system.util.JeecgDataAutorUtils;
import org.jeecg.common.system.vo.SysPermissionDataRuleModel;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.online.cgform.entity.OnlCgformField;
import org.jeecg.modules.online.cgform.service.IOnlCgformFieldService;
import org.jeecg.modules.online.config.b.e;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.jeecg.common.constant.DataBaseConstant.DELETED_TABLE;
import static org.jeecg.common.constant.SymbolConstant.COLON;

@Data
public class DbDmlWhereConditionUtil {
    private String alias;
    private String aliasNoPoint;
    private String dataBaseType;
    private boolean dateStringSearch;
    private List<e> fieldList;
    private List<String> needList;
    private List<SysPermissionDataRuleModel> authDatalist;
    private Map<String, Object> reqParams;
    private StringBuffer sql;
    private StringBuffer superQuerySql;
    private Map<String, Object> sqlParams;
    private String daoType;
    private boolean superQuery;
    private String matchType;
    private int usePage;
    private boolean first;
    private String paramPrefix;
    private Map<String, String> duplicateSqlNameRecord;
    private Map<String, String> duplicateParamNameRecord;
    private String tableName;
    public String subTableStr;

    public DbDmlWhereConditionUtil() {
    }

    public DbDmlWhereConditionUtil(String var1, String var2) {
        this.alias = var1;
        this.aliasNoPoint = var1.replace(".", "");
        this.dataBaseType = var2;
        this.dateStringSearch = this.e(var2);
        this.sql = new StringBuffer();
        this.sqlParams = new HashMap<>(5);
        this.authDatalist = null;
        this.needList = null;
        this.matchType = " AND ";
        this.usePage = 1;
        this.first = true;
        this.paramPrefix = "";
        this.duplicateSqlNameRecord = new HashMap<>(5);
        this.duplicateParamNameRecord = new HashMap<>(5);
    }

    public DbDmlWhereConditionUtil(String var1) {
        this(var1, null);
        this.usePage = 2;
    }

    public DbDmlWhereConditionUtil(String var1, boolean var2, String var3) {
        this(var1, null);
        this.superQuery = var2;
        this.matchType = " " + var3 + " ";
        this.usePage = 2;
    }

    public String a(List<e> var1, Map<String, Object> var2) {
        this.c(var1, var2);
        this.d();
        return this.sql.toString();
    }

    public String generateWhereCondition(List<e> fieldConfigs, Map<String, Object> params, List<SysPermissionDataRuleModel> dataRules) {
        this.setAuthDatalist(dataRules);
        this.c(fieldConfigs, params);
        this.b(fieldConfigs);
        this.a(params);
        this.e();
        return this.sql.toString();
    }

    public String a(List<e> var1, Map<String, Object> var2, List<SysPermissionDataRuleModel> var3, String var4) {
        this.setAuthDatalist(var3);
        this.paramPrefix = var4;
        this.c(var1, var2);
        this.b(var1);
        this.e();
        return this.sql.toString();
    }

    public String b(List<e> var1, Map<String, Object> var2) {
        this.c(var1, var2);
        return this.sql.toString();
    }

    public String a(List<e> var1) {
        if (this.superQuery) {

            for (e var3 : var1) {
                String var4 = var3.getName();
                String var5 = var3.getVal();
                if (var5 != null) {
                    QueryRuleEnum var6 = QueryRuleEnum.getByValue(var3.getRule());
                    if (var6 == null) {
                        var6 = QueryRuleEnum.EQ;
                    }

                    this.a(var4, var3.getType(), var5, var6);
                }
            }
        }

        return this.sql.toString();
    }

    public void c(List<e> fieldConfigs, Map<String, Object> params) {
        fieldConfigs.forEach(fieldConfig->{
            String fieldName = fieldConfig.getName();
            var fieldType = fieldConfig.getType();
            if (this.needList != null && this.needList.contains(fieldName)) {
                fieldConfig.setIsSearch(1);
                fieldConfig.setMode("single");
            }

            if (oConvertUtils.isNotEmpty(fieldConfig.getMainField()) && oConvertUtils.isNotEmpty(fieldConfig.getMainTable())) {
                fieldConfig.setIsSearch(1);
                fieldConfig.setMode("single");
            }

            if(fieldConfig.getName().equals(DELETED_TABLE)){
                fieldConfig.setIsSearch(1);
                fieldConfig.setMode("single");
                params.put(DELETED_TABLE, 0);
            }

            if (1 == fieldConfig.getIsSearch()) {
                if ("time".equals(fieldConfig.getView()) && "group".equals(fieldConfig.getMode())) {
                    fieldConfig.setMode("single");
                }

                if ("group".equals(fieldConfig.getMode())) {
                    String var7 = fieldName + "_begin";
                    Object var8 = params.get(this.paramPrefix + var7);
                    if (null != var8) {
                        this.b(fieldName, " >= ");
                        this.b(var7, fieldType, var8);
                    }

                    String var9 = fieldName + "_end";
                    Object var10 = params.get(this.paramPrefix + var9);
                    if (null != var10) {
                        this.b(fieldName, " <= ");
                        this.a(var9, fieldType, var10, "end");
                    }
                } else {
                    Object var13 = params.get(this.paramPrefix + fieldName);
                    if (var13 != null) {
                        String var14 = fieldConfig.getView();
                        if ("list_multi".equals(var14)) {
                            this.e(fieldName, var13);
                        } else if ("popup".equals(var14)) {
                            this.f(fieldName, var13);
                        } else {
                            this.a(fieldName, fieldType, var13);
                        }
                    }
                }
            }
        });


        params.keySet().stream().filter(var11 -> var11.startsWith("popup_param_pre__")).forEach(var11 -> {
            Object var12 = params.get(var11);
            if (var12 != null) {
                var var6 = var11.replace("popup_param_pre__", "");
                this.a(var6, "", var12);
            }
        });

    }

    public void setAuthList(List<SysPermissionDataRuleModel> authDatalist) {
        this.authDatalist = authDatalist;
    }

    private void d() {
        var var1 = JeecgDataAutorUtils.loadDataSearchConditon();
        if (var1 != null && var1.size() > 0) {
            for (SysPermissionDataRuleModel o : var1) {
                if (o == null) {
                    break;
                }

                String var4 = o.getRuleValue();
                if (!oConvertUtils.isEmpty(var4)) {
                    String var5 = o.getRuleConditions();
                    if (QueryRuleEnum.SQL_RULES.getValue().equals(var5)) {
                        this.b("", QueryGenerator.getSqlRuleValue(var4));
                    } else {
                        QueryRuleEnum var6 = QueryRuleEnum.getByValue(o.getRuleConditions());
                        String var7 = "Integer";
                        var4 = var4.trim();
                        if (var4.startsWith("'") && var4.endsWith("'")) {
                            var7 = "string";
                            var4 = var4.substring(1, var4.length() - 1);
                        } else if (var4.startsWith("#{") && var4.endsWith("}")) {
                            var7 = "string";
                        }

                        String var8 = QueryGenerator.converRuleValue(var4);
                        this.a(o.getRuleColumn(), var7, var8, var6);
                    }
                }
            }
        }

    }

    private e a(String var1, List<e> var2) {
        if (var2 != null && var1 != null) {
            String var3 = oConvertUtils.camelToUnderline(var1);

            for (org.jeecg.modules.online.config.b.e e : var2) {
                String var6 = e.getName();
                if (var1.equals(var6) || var3.equals(var6)) {
                    return e;
                }
            }
        }

        return null;
    }

    private void b(List<e> var1) {
        List<SysPermissionDataRuleModel> var2 = this.authDatalist;
        if (var2 == null) {
            var2 = JeecgDataAutorUtils.loadDataSearchConditon();
        }

        if (var2 != null && var2.size() > 0) {
            for (SysPermissionDataRuleModel o : var2) {
                SysPermissionDataRuleModel var4 = o;
                if (var4 == null) {
                    break;
                }

                String var5 = var4.getRuleValue();
                if (!oConvertUtils.isEmpty(var5)) {
                    String var6 = var4.getRuleConditions();
                    if (QueryRuleEnum.SQL_RULES.getValue().equals(var6)) {
                        this.b("", QueryGenerator.getSqlRuleValue(var5));
                    } else {
                        String var7 = var4.getRuleColumn();
                        e var8 = this.a(var7, var1);
                        if (var8 != null) {
                            String var9 = QueryGenerator.converRuleValue(var5);
                            QueryRuleEnum var10 = QueryRuleEnum.getByValue(var4.getRuleConditions());
                            this.a(var8.getName(), var8.getType(), var9, var10);
                        }
                    }
                }
            }
        }

    }

    private void e() {
        String var1 = org.jeecg.modules.online.cgform.d.b.f(this.tableName);
        boolean var2 = org.jeecg.modules.online.cgform.d.b.j(var1);
        if (var2) {
            String var3 = SpringContextUtils.getHttpServletRequest().getHeader("X-Tenant-Id");
            this.a("tenant_id", "int", var3, QueryRuleEnum.EQ);
        }

    }

    private void a(String var1, String var2, Object var3) {
        this.a(var1, var2, var3, (QueryRuleEnum)null);
    }

    private void a(String var1, String var2, Object var3, QueryRuleEnum var4) {
        if (var3 != null) {
            String var5 = var3.toString();
            boolean var6 = false;
            if (var4 == null) {
                var6 = true;
                var4 = QueryGenerator.convert2Rule(var3);
            }

            if (var6) {
                var5 = var5.trim();
            }

            switch (var4) {
                case GT, LT -> {
                    this.b(var1, var4.getValue());
                    if (var6) {
                        var5 = var5.substring(1);
                    }
                    this.b(var1, var2, (Object) var5);
                }
                case GE, LE -> {
                    this.b(var1, var4.getValue());
                    if (var6) {
                        var5 = var5.substring(2);
                    }
                    this.b(var1, var2, (Object) var5);
                }
                case EQ -> {
                    this.b(var1, var4.getValue());
                    this.b(var1, var2, (Object) var5);
                }
                case EQ_WITH_ADD -> {
                    this.b(var1, var4.getValue());
                    if (var6) {
                        var5 = var5.replaceAll("\\+\\+", ",");
                    }
                    this.b(var1, var2, (Object) var5);
                }
                case NE -> {
                    this.b(var1, " <> ");
                    if (var6) {
                        var5 = var5.substring(1);
                    }
                    this.b(var1, var2, (Object) var5);
                }
                case IN -> {
                    this.b(var1, " in ");
                    this.a(var1, var2, var5);
                }
                case LIKE, RIGHT_LIKE, LEFT_LIKE -> {
                    this.b(var1, " like ");
                    if (var6) {
                        this.a(var1, var5);
                    } else {
                        this.a(var1, var5, var4);
                    }
                }
                default -> {
                    this.b(var1, " = ");
                    this.b(var1, var2, (Object) var5);
                }
            }
        }

    }

    private void a(String var1, String var2, String var3) {
        String[] var4 = var3.split(",");
        if (var4.length == 0) {
            this.a("('')");
        } else {
            String var5 = "foreach_%s_%s";
            StringBuilder var6 = new StringBuilder();

            for(int var7 = 0; var7 < var4.length; ++var7) {
                String var8 = var4[var7].trim();
                String var9 = String.format(var5, var1, var7);
                if (var7 > 0) {
                    var6.append(",");
                }

                String var10 = this.f(var9);
                if ("jdbcTemplate".equals(this.daoType)) {
                    var6.append(COLON).append(var10);
                } else {
                    var6.append("#{").append(this.b(var10)).append("}");
                }

                if (!"Long".equals(var2) && !"Integer".equals(var2)) {
                    this.a(var9, (Object)var8);
                } else {
                    this.a(var9, (Object)Integer.parseInt(var8));
                }
            }

            this.a("(" + var6 + ")");
        }

    }

    private void a(String var1, String var2) {
        String var3 = this.c(var1, "VARCHAR");
        this.a(var3);
        String var4;
        if ((!var2.startsWith("*") || !var2.endsWith("*")) && (!var2.startsWith("%") || !var2.endsWith("%"))) {
            if (!var2.startsWith("*") && !var2.startsWith("%")) {
                if (!var2.endsWith("*") && !var2.endsWith("%")) {
                    var4 = "%" + var2 + "%";
                } else {
                    var4 = var2.substring(0, var2.length() - 1) + "%";
                }
            } else {
                var4 = "%" + var2.substring(1);
            }
        } else {
            var4 = "%" + var2.substring(1, var2.length() - 1) + "%";
        }

        this.a(var1, (Object)var4);
    }

    private void a(String var1, String var2, QueryRuleEnum var3) {
        String var4 = this.c(var1, "VARCHAR");
        this.a(var4);
        if (var3 == QueryRuleEnum.LEFT_LIKE) {
            this.a(var1, (Object)("%" + var2));
        } else if (var3 == QueryRuleEnum.RIGHT_LIKE) {
            this.a(var1, (Object)(var2 + "%"));
        } else {
            this.a(var1, (Object)("%" + var2 + "%"));
        }

    }

    private void b(String var1, String var2, Object var3) {
        this.a(var1, var2, var3, (String)null);
    }

    private void a(String var1, String var2, Object var3, String var4) {
        String var5 = var2.toLowerCase();
        if (this.d(var2)) {
            if (org.jeecg.modules.online.cgform.d.b.g(var3.toString())) {
                this.a(var3.toString());
            } else {
                this.a("''");
            }
        } else {
            String var6;
            Date var7;
            if ("datetime".equals(var5)) {
                var6 = var3.toString().trim();
                if (var6.length() <= 10) {
                    if ("end".equals(var4)) {
                        var6 = var6 + " 23:59:59";
                    } else {
                        var6 = var6 + " 00:00:00";
                    }
                }

                var7 = DateUtils.str2Date(var6, DateUtils.datetimeFormat.get());
                this.b(var1, var7);
            } else if ("date".equals(var5)) {
                var6 = var3.toString().trim();
                if (var6.length() > 10) {
                    var6 = var6.substring(0, 10);
                }

                var7 = DateUtils.str2Date(var6, DateUtils.date_sdf.get());
                this.c(var1, var7);
            } else {
                var6 = var3.toString().trim();
                if (var6.startsWith("'") && var6.endsWith("'") && this.usePage == 1) {
                    this.a(var6);
                } else {
                    this.d(var1, var6);
                }
            }
        }

    }

    private void b(String var1, String var2) {
        this.b(var1, var2, this.matchType);
    }

    private void b(String var1, String var2, String var3) {
        if (this.first) {
            this.first = false;
        } else {
            this.sql.append(var3);
        }

        if (!var1.isEmpty()) {
            this.sql.append(this.alias).append(var1).append(var2);
        } else {
            this.sql.append(" ").append(var2).append(" ");
        }

    }

    private void a(String var1) {
        this.sql.append(var1);
    }

    private String c(String var1, String var2) {
        var1 = this.f(var1);
        if ("jdbcTemplate".equals(this.daoType)) {
            return COLON + var1;
        } else {
            String var3 = this.b(var1);
            return var2 == null ? String.format("#{%s}", var3) : String.format("#{%s, jdbcType=%s}", var3, var2);
        }
    }

    private String b(String var1) {
        return "param." + this.c(var1);
    }

    private void a(String var1, Object var2) {
        var1 = this.g(var1);
        this.sqlParams.put(this.c(var1), var2);
    }

    private String c(String var1) {
        return this.usePage == 1 ? var1 : this.aliasNoPoint + "_" + var1;
    }

    private void b(String var1, Object var2) {
        if (var2 != null) {
            String var3 = this.c(var1, "TIMESTAMP");
            this.a(var3);
            this.a(var1, var2);
        }

    }

    private void c(String var1, Object var2) {
        if (var2 != null) {
            String var3 = this.c(var1, "DATE");
            this.a(var3);
            this.a(var1, var2);
        }

    }

    private void d(String var1, Object var2) {
        if (var2 != null) {
            String var3 = this.c(var1, null);
            this.a(var3);
            this.a(var1, var2);
        }

    }

    private boolean d(String var1) {
        return "Long".equals(var1) || "Integer".equals(var1) || "int".equals(var1) || "double".equals(var1) || "BigDecimal".equals(var1) || "number".equals(var1);
    }

    private boolean e(String var1) {
        return !"ORACLE".equals(var1);
    }

    public static String a(String var0, long var1) {
        return var0.replaceFirst("\\?", String.valueOf(var1));
    }

    public static String a(String var0, long var1, long var3) {
        var0 = var0.replaceFirst("\\?", String.valueOf(var1));
        return var0.replaceFirst("\\?", String.valueOf(var3));
    }

    private void e(String var1, Object var2) {
        if (var2 != null) {
            String[] var3 = var2.toString().split(",");
            StringBuilder var4 = new StringBuilder();
            String var5 = this.alias + var1;

            for (String s : var3) {
                String var7 = var5 + " like '%" + s + ",%' or " + var5 + " like '%," + s + "%' or " + var5 + " = '" + s + "'";
                if (var4.isEmpty()) {
                    var4 = new StringBuilder(var7);
                } else {
                    var4.append(" or ").append(var7);
                }
            }

            if (!var4.isEmpty()) {
                String var8 = "(" + var4 + ")";
                this.b("", var8);
            }
        }

    }

    private void f(String var1, Object var2) {
        if (var2 != null) {
            String var3 = this.alias + var1;
            StringBuilder var4 = new StringBuilder();
            String var5 = "popup_%s_%s";
            String[] var6 = var2.toString().split(",");

            for(int var7 = 0; var7 < var6.length; ++var7) {
                String var8 = String.format(var5, var1, var7);
                String var9 = this.c(var8, "VARCHAR");
                String var10 = "%" + var6[var7] + "%";
                this.a(var8, (Object)var10);
                String var11 = var3 + " like " + var9;
                if (var4.isEmpty()) {
                    var4 = new StringBuilder(var11);
                } else {
                    var4.append(" and ").append(var11);
                }
            }

            if (!var4.isEmpty()) {
                String var12 = "(" + var4 + ")";
                this.b("", var12);
            }
        }

    }

    private void a(Map<String, Object> var1) {
        Object var2 = var1.get("superQueryMatchType");
        MatchTypeEnum var3 = MatchTypeEnum.getByValue(var2);
        if (var3 == null) {
            var3 = MatchTypeEnum.AND;
        }

        Object var4 = var1.get("superQueryParams");
        if (var4 != null && !StringUtils.isBlank(var4.toString())) {
            String var5;

            var5 = URLDecoder.decode(var4.toString(), StandardCharsets.UTF_8);

            JSONArray var6 = JSONArray.parseArray(var5);
            IOnlCgformFieldService var7 = SpringContextUtils.getBean(IOnlCgformFieldService.class);
            var var8 = new ArrayList<String>();
            var8.add("JEECG_SUPER_QUERY_MAIN_TABLE");
            int var11;
            if (this.subTableStr != null && !this.subTableStr.isEmpty()) {
                String[] var9 = this.subTableStr.split(",");
                var11 = var9.length;

                var8.addAll(Arrays.asList(var9).subList(0, var11));
            }

            var var25 = new HashMap<String, JSONObject>(5);
            StringBuilder var26 = new StringBuilder();

            for(var11 = 0; var11 < var8.size(); ++var11) {
                String var28 = var8.get(var11);
                var var29 = new ArrayList<e>();

                String var16;
                for(int var14 = 0; var14 < var6.size(); ++var14) {
                    JSONObject var15 = var6.getJSONObject(var14);
                    var16 = var15.getString("field");
                    if (!oConvertUtils.isEmpty(var16)) {
                        String[] var17 = var16.split(",");
                        e var18 = new e(var15);
                        if ("JEECG_SUPER_QUERY_MAIN_TABLE".equals(var28) && var17.length == 1) {
                            var29.add(var18);
                        } else if (var17.length == 2 && var17[0].equals(var28)) {
                            var29.add(var18);
                            JSONObject var19 = var25.get(var28);
                            if (var19 == null) {
                                var var20 = var7.queryFormFieldsByTableName(var28);
                                var19 = new JSONObject();

                                for (OnlCgformField o : var20) {
                                    if (StringUtils.isNotBlank(o.getMainTable())) {
                                        var19.put("subTableName", var28);
                                        var19.put("subField", o.getDbFieldName());
                                        var19.put("mainTable", o.getMainTable());
                                        var19.put("mainField", o.getMainField());
                                    }
                                }

                                var25.put(var28, var19);
                            }
                        }
                    }
                }

                if (!CollectionUtils.isEmpty(var29)) {
                    String var30 = var11 == 0 ? this.alias : this.aliasNoPoint + var11 + ".";
                    DbDmlWhereConditionUtil var31 = new DbDmlWhereConditionUtil(var30, true, var3.getValue());
                    var31.setDuplicateParamNameRecord(this.getDuplicateParamNameRecord());
                    var31.setDuplicateSqlNameRecord(this.getDuplicateSqlNameRecord());
                    var16 = var31.a(var29);
                    var var32 = var31.getSqlParams();
                    if (!ObjectUtils.isEmpty(var16)) {
                        if (var11 == 0) {
                            var26.append(" ").append(var16).append(" ");
                            this.sqlParams.putAll(var32);
                        } else {
                            JSONObject var33 = var25.get(var28);
                            String var34 = var33.getString("subTableName");
                            String var35 = var33.getString("subField");
                            String var36 = var33.getString("mainField");
                            String var37 = " %s in (select %s from %s %s where ";
                            String var23 = String.format(var37, var36, var35, var34, this.aliasNoPoint + var11);
                            this.sqlParams.putAll(var32);
                            var26.append(var3.getValue()).append(var23).append(var16).append(") ");
                        }
                    }
                }
            }

            String var27 = var26.toString();
            if (!var27.isEmpty()) {
                if (var27.startsWith("AND ")) {
                    var27 = var27.substring(3);
                } else if (var27.startsWith("OR ")) {
                    var27 = var27.substring(2);
                }

                this.b("", "(" + var27 + ")");
            }

        }
    }

    private String f(String var1) {
        return this.a(var1, this.duplicateSqlNameRecord);
    }

    private String g(String var1) {
        return this.a(var1, this.duplicateParamNameRecord);
    }

    private String a(String var1, Map<String, String> var2) {
        String var3 = var2.get(var1);
        if (var3 == null) {
            var3 = var1;
            var2.put(var1, var1 + "_1");
        } else {
            String var4 = var3.substring(var3.lastIndexOf("_") + 1);
            String var5 = var1 + "_" + (Integer.parseInt(var4) + 1);
            var2.put(var1, var5);
        }

        return var3;
    }
}
