package org.jeecg.cgform.database.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jeecg.modules.online.cgform.entity.OnlCgformField;

/**
 * @author jiangyan
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper =true)
@TableName("onl_cgform_field")
public class CgformFieldDO extends OnlCgformField {

    @ApiModelProperty(value = "填充规则")
    private String fillRule;
}
