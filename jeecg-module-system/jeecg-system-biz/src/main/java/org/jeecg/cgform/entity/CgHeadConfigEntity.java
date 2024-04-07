package org.jeecg.cgform.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.cgform.database.domain.CgformFieldDO;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CgHeadConfigEntity extends org.jeecg.modules.online.config.b.a {

    private List<CgformFieldDO> fields;

}
