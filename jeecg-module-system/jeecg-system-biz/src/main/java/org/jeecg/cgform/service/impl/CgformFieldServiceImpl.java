package org.jeecg.cgform.service.impl;

import org.jeecg.cgform.database.domain.CgformFieldDO;
import org.jeecg.cgform.database.mapper.CgformFieldMapper;
import org.jeecg.cgform.service.CgformFieldService;
import org.jeecg.modules.base.service.BizServiceImpl;
import org.jeecg.modules.online.cgform.entity.OnlCgformField;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author jiangyan
 */
@Service
public class CgformFieldServiceImpl extends BizServiceImpl<CgformFieldMapper, CgformFieldDO> implements CgformFieldService {

    @Override
    public List<CgformFieldDO> listFieldByHead(String headId) {
        return listFieldByHead(headId, null);
    }

    @Override
    public List<CgformFieldDO> listFieldByHeadAndMainTable(String headId, String mainTable) {
        var queryWrapper = createBizQueryWrapper().eq(CgformFieldDO::getCgformHeadId, headId);
        queryWrapper.eq(CgformFieldDO::getMainTable, mainTable);
        queryWrapper.orderByAsc(OnlCgformField::getOrderNum);
        return list(queryWrapper);
    }

    @Override
    public List<CgformFieldDO> listFieldByHead(String headId, Boolean isDbPersist) {
        var queryWrapper = createBizQueryWrapper().eq(CgformFieldDO::getCgformHeadId, headId);
        if(isDbPersist != null){
            queryWrapper.eq(CgformFieldDO::getDbIsPersist, isDbPersist? 1: 0 );
        }
        queryWrapper.orderByAsc(OnlCgformField::getOrderNum);
        return list(queryWrapper);
    }

    @Override
    public void deleteByHeadId(String headId) {
        var wrapper = createBizQueryWrapper();
        wrapper.eq(CgformFieldDO::getCgformHeadId, headId);
        getBaseMapper().delete(wrapper);
    }
}
