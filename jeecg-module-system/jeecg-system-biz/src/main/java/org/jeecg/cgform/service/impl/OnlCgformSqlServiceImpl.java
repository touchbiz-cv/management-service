package org.jeecg.cgform.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.val;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.jeecg.cgform.utils.DbDmlGenerator;
import org.jeecg.cgform.utils.DbFiledValidater;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.online.cgform.converter.b;
import org.jeecg.modules.online.cgform.entity.OnlCgformField;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;
import org.jeecg.modules.online.cgform.enums.EnhanceDataEnum;
import org.jeecg.modules.online.cgform.mapper.OnlCgformFieldMapper;
import org.jeecg.modules.online.cgform.service.IOnlCgformHeadService;
import org.jeecg.modules.online.config.exception.BusinessException;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jiangyan
 */
@Primary
@Service("OnlCgformSqlServiceImpl_n")
public class OnlCgformSqlServiceImpl extends org.jeecg.modules.online.cgform.service.a.f{

    private final SqlSessionTemplate sqlSessionTemplate;

    private final IOnlCgformHeadService onlCgformHeadService;

    private final OnlCgformFieldServiceImpl fieldService;

    public OnlCgformSqlServiceImpl(IOnlCgformHeadService onlCgformHeadService, OnlCgformFieldServiceImpl fieldService, SqlSessionTemplate sqlSessionTemplate) {
        this.onlCgformHeadService = onlCgformHeadService;
        this.fieldService = fieldService;
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    @Override
    public Map<String, String> saveOnlineImportDataWithValidate(OnlCgformHead head, List<OnlCgformField> fieldList, List<Map<String, Object>> dataList) {
        StringBuilder var4 = new StringBuilder();
        DbFiledValidater var5 = new DbFiledValidater(fieldList);
        OnlCgformFieldMapper fieldMapper = SpringContextUtils.getBean(OnlCgformFieldMapper.class);
        int var7 = 0;
        int errorCount = 0;

        for (val stringObjectMap : dataList) {
            String var11 = JSON.toJSONString(stringObjectMap);
            ++var7;
            String validErrorResult = var5.valid(var11, var7);
            if (validErrorResult == null) {
                try {
                    this.a(var11, head, fieldList, fieldMapper);
                } catch (Exception var16) {
                    ++errorCount;
                    String var14 = this.generateDuplicateErrorInfo(var16.getCause().getMessage());
                    String var15 = DbFiledValidater.validMessage(var14, var7);
                    var4.append(var15);
                }
            } else {
                ++errorCount;
                var4.append(validErrorResult);
            }
        }

        HashMap<String, String> var17 = new HashMap<>(5);
        var17.put("error", var4.toString());
        var17.put("tip", DbFiledValidater.errorMessage(dataList.size(), errorCount));
        return var17;
    }

    @Override
    public void saveBatchOnlineTable(OnlCgformHead head, List<OnlCgformField> fieldList, List<Map<String, Object>> dataList) throws BusinessException {
        SqlSession sqlSession = null;

        try {
            b.a(2, dataList, fieldList);
            sqlSession = this.sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
            OnlCgformFieldMapper mapper = sqlSession.getMapper(OnlCgformFieldMapper.class);
            short batchSize = 1000;
            int index=0;
            for (Object data : dataList) {
                String var8 = JSON.toJSONString(data);
                this.a(var8, head, fieldList, mapper);
                index++;

                if (index % batchSize == 0) {
                    sqlSession.commit();
                    sqlSession.clearCache();
                }
            }
            sqlSession.commit();
        } catch (Exception var12) {
            if (sqlSession != null) {
                sqlSession.rollback();
            }
            throw new BusinessException(var12.getMessage());
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }

        }

    }


    private String generateDuplicateErrorInfo(String var1) {
        String var2 = "^Duplicate entry \\'(.*)\\' for key .*$";
        Pattern var3 = Pattern.compile(var2);
        Matcher var4 = var3.matcher(var1);
        return var4.find() ? "重复数据" + var4.group(1) : var1;
    }

    private void a(String var1, OnlCgformHead head, List<OnlCgformField> fieldList, OnlCgformFieldMapper fieldMapper) throws BusinessException {
        JSONObject data = JSONObject.parseObject(var1);
        EnhanceDataEnum type = this.onlCgformHeadService.executeEnhanceImport(head, data);
        String tableName = head.getTableName();
        Map<String,Object> var8;
        if (EnhanceDataEnum.INSERT == type) {
            fieldService.saveFormData(head, tableName, data, false);
        } else if (EnhanceDataEnum.UPDATE == type) {
            var8 = DbDmlGenerator.b(tableName, fieldList, data);
            fieldMapper.executeUpdatetSQL(var8);
        }
    }

}
