package org.jeecg.cgform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.cgform.database.domain.CgformFieldDO;

import java.util.List;

/**
 * @author jiangyan
 */
public interface CgformFieldService extends IService<CgformFieldDO> {

    /**
     * 根据表id查询字段
     * @param headId
     * @return
     */
    List<CgformFieldDO> listFieldByHead(String headId);

    List<CgformFieldDO> listFieldByHeadAndMainTable(String headId, String mainTable);

    /**
     * 根据表id以及是否数据库同步筛选字段
     * @param headId
     * @param isDbPersist
     * @return
     */
    List<CgformFieldDO> listFieldByHead(String headId, Boolean isDbPersist);

    void deleteByHeadId(String headId);

}
