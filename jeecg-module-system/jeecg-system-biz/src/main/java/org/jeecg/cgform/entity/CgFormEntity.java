package org.jeecg.cgform.entity;

import lombok.Data;
import org.jeecg.cgform.database.domain.CgformHeadDO;
import org.jeecg.modules.online.cgform.entity.OnlCgformIndex;
import org.jeecg.cgform.database.domain.CgformFieldDO;

import java.util.List;

/**
 * @author jiangyan
 */
@Data
public class CgFormEntity {

    private CgformHeadDO head;
    private List<CgformFieldDO> fields;
    private List<String> deleteFieldIds;
    private List<OnlCgformIndex> indexs;
    private List<String> deleteIndexIds;
}
