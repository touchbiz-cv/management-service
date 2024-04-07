package org.jeecg.cgform.database.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;

/**
 * @author jiangyan
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper =true)
@TableName("onl_cgform_head")
public class CgformHeadDO extends OnlCgformHead {

    @ApiModelProperty(value = "表类型,1是物理表2是视图")
    private Integer isView;

    @ApiModelProperty(value = "关联的页面编辑器id，如果有数据则可以跳转打开对应的页面编辑器")
    private String pageEditorId;
}
