
package org.jeecg.cgform.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jiangyan
 */

@Getter
@AllArgsConstructor
public enum TableTypeEnum {

    /**
     *
     */
    SINGLE(1, "单表"),
    /**
     *
     */
    MASTER(2, "主表"),

    SLAVE(3, "子表");

    /**
     * 状态值
     */
    private final Integer type;

    /**
     * 状态名称
     */
    private final String name;


}
