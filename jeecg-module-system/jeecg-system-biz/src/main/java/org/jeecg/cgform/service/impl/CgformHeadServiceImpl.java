package org.jeecg.cgform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.cgform.database.domain.CgformHeadDO;
import org.jeecg.cgform.database.mapper.CgformHeadMapper;
import org.jeecg.cgform.service.CgformHeadService;
import org.jeecg.modules.base.service.BizServiceImpl;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CgformHeadServiceImpl extends BizServiceImpl<CgformHeadMapper, CgformHeadDO>  implements CgformHeadService {

    @Override
    public void initCopyState(List<CgformHeadDO> headList) {
        var physicIdList =getBaseMapper().queryCopyPhysicId();

        headList.forEach(head -> head.setHascopy(physicIdList.contains(head.getId()) ? 1: 0));

    }

    @Override
    public List<String> getAllBaseTable() {
        return getBaseMapper().getBaseTable();
    }

    @Override
    public void updateIsBase(String isBase, String tableName) {
        getBaseMapper().updateIsBase(isBase, tableName);
    }

    @Override
    public OnlCgformHead getByTableName(String tableName) {
        return getBaseMapper().selectOne(new LambdaQueryWrapper<CgformHeadDO>().eq(CgformHeadDO::getTableName, tableName));
    }

}
