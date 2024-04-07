package org.jeecg.cgform.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.online.cgform.entity.OnlCgformField;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jeecg.common.constant.DataBaseConstant.ID;
import static org.jeecg.common.constant.SymbolConstant.COMMA;

/**
 * @author jiangyan
 */ // extends org.jeecg.modules.online.cgform.d.h
public class DbFiledValidater{
    private final Map<String, OnlCgformField> d= new HashMap<>(5);
    private final Map<String, OnlCgformField> notEmptyMap = new HashMap<>(5);

    public DbFiledValidater(List<OnlCgformField> fields) {
        for (OnlCgformField field : fields) {
            String fieldValidType = field.getFieldValidType();
            int dbIsNull = field.getDbIsNull();
            String dbFieldName = field.getDbFieldName();

            if (!ObjectUtils.isEmpty(fieldValidType) && !org.jeecg.modules.online.cgform.enums.b.a.getType().equals(fieldValidType)) {
                if (org.jeecg.modules.online.cgform.enums.b.k.getType().equals(fieldValidType)) {
                    notEmptyMap.put(dbFieldName, field);
                } else {
                    d.put(dbFieldName, field);
                }
            }

            if (dbIsNull == 0 || "1".equals(field.getFieldMustInput())) {
                if (oConvertUtils.isEmpty(field.getDbDefaultVal()) && !ID.equals(dbFieldName)) {
                    notEmptyMap.put(dbFieldName, field);
                }
            }
        }
    }

    public String valid(String var1, int var2) {
        StringBuilder var3 = new StringBuilder();
        JSONObject var4 = JSON.parseObject(var1);

        for (Map<String, OnlCgformField> fieldMap : Arrays.asList(this.notEmptyMap, this.d)) {
            for (Map.Entry<String, OnlCgformField> entry : fieldMap.entrySet()) {
                String var6 = entry.getKey();
                OnlCgformField var8 = entry.getValue();
                String var7 = var4.getString(var6);
                String var9 = var8.getFieldValidType();

                if (var7 != null && !var7.isEmpty()) {
                    String var10;
                    String var11;
                    if (org.jeecg.modules.online.cgform.enums.b.j.getType().equals(var9)) {
                        var10 = "^-?[1-9]\\d*$";
                        var11 = "请输入整数";
                    } else {
                        org.jeecg.modules.online.cgform.enums.b validEnum = org.jeecg.modules.online.cgform.enums.b.b(var9);
                        if (validEnum == null) {
                            var10 = var9;
                            var11 = "校验【" + var9 + "】未通过";
                        } else {
                            var10 = validEnum.getPattern();
                            var11 = validEnum.getMsg();
                        }
                    }
                    Pattern var14 = Pattern.compile(var10);
                    Matcher var13 = var14.matcher(var7);

                    if (!var13.find()) {
                        var3.append(var8.getDbFieldTxt()).append(var11).append(COMMA);
                    }

                } else {
                    var3.append(var8.getDbFieldTxt()).append(org.jeecg.modules.online.cgform.enums.b.k.getMsg()).append(COMMA);
                }
            }
        }

        if (!var3.isEmpty()) {
            return validMessage(var3.toString(), var2);
        }
        return null;
    }

    public static String validMessage(String var0, int var1) {
        return String.format("第%s行校验信息:", var1) + var0 + "\r\n";
    }

    public static String errorMessage(int var0, int var1) {
        int var2 = var0 - var1;
        return String.format("总上传行数:%s,已导入行数:%s,错误行数:%s", var0, var2, var1);
    }
}
