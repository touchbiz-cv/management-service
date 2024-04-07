package org.jeecg.cgform.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.online.cgform.d.b;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;
import org.jeecg.modules.online.cgform.mapper.OnlineMapper;
import org.jeecg.modules.online.cgform.model.e;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.jeecg.common.constant.DataBaseConstant.ID;

/**
 * @author jiangyan
 */
@Primary
@Service("OnlineJoinQueryService_n")
public class OnlineJoinQueryServiceImpl extends org.jeecg.modules.online.cgform.service.a.h{

    private final OnlineMapper onlineMapper;

    public OnlineJoinQueryServiceImpl(OnlineMapper onlineMapper) {
        this.onlineMapper = onlineMapper;
    }

    @Override
    public Map<String, Object> pageList(OnlCgformHead head, Map<String, Object> params) {
        return this.pageList(head, params, false);
    }

    @Override
    public Map<String, Object> pageList(OnlCgformHead head, Map<String, Object> params, boolean ignoreSelectSubField) {
        e queryInfo = this.getQueryInfo(head, params, ignoreSelectSubField);
        String sql = queryInfo.getSql();
        var queryInfoParams = queryInfo.getParams();
        var var7 = queryInfo.getTableAliasMap();
        HashMap<String,Object> var8 = new HashMap<>(5);
        var pageSize = params.get("pageSize") == null ? 10 : Integer.parseInt(params.get("pageSize").toString());
        if (pageSize == -521) {
            var records = this.onlineMapper.selectByCondition(sql, queryInfoParams);
            if (records != null && records.size() != 0) {
                var8.put("total", records.size());
                if (ignoreSelectSubField) {
                    records = this.b(records);
                }

                var8.put("records", b.a(records, var7.values()));
            } else {
                var8.put("total", 0);
            }

            if (ignoreSelectSubField) {
                var8.put("fieldList", queryInfo.getFieldList());
            }
        } else {
            int pageNo = params.get("pageNo") == null ? 1 : Integer.parseInt(params.get("pageNo").toString());
            var page = new Page<Map<String,Object>>(pageNo, pageSize);
            page.setOptimizeCountSql(false);
            var var12 = this.onlineMapper.selectPageByCondition(page, sql, queryInfoParams);
            var8.put("total", var12.getTotal());
            var var13 = var12.getRecords();
            if (ignoreSelectSubField) {
                var13 = this.b(var13);
            }
            var8.put("records", b.a(var13, var7.values()));
        }

        return var8;
    }

    private List<Map<String, Object>> b(List<Map<String, Object>> var1) {
        Map<String, Object> var2 = new HashMap<>(5);
        for (Map<String, Object> stringObjectMap : var1) {
            String var5 = stringObjectMap.get(ID).toString();
            var2.putIfAbsent(var5, stringObjectMap);
        }

        return new ArrayList(var2.values());
    }
}
