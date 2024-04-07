package org.jeecg.cgform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.cgform.database.domain.CgformHeadDO;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;

import java.util.List;

/**
 * @author jiangyan
 */
public interface CgformHeadService extends IService<CgformHeadDO> {

    void initCopyState(List<CgformHeadDO> headList);

    List<String> getAllBaseTable();

    void updateIsBase(String isBase, String tableName);

    OnlCgformHead getByTableName(String tableName);
}
