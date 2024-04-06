package org.jeecg.modules.alarmrecord.enums;

import lombok.Getter;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.util.ObjectUtils;

/**
 * @description 状态 状态字段，0为未启用状态，1为启用状态
 * @date 2024-04-06
 */
@Getter
public enum StatusEnum {


    OFF(0, "未启用"),

    ON(1, "启用");

    StatusEnum(Integer value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    public static StatusEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            throw new JeecgBootException("规则状态 不能为空");
        }
        for (StatusEnum ruleStatusEnum : StatusEnum.values()) {
            if (ruleStatusEnum.getValue().equals(value)) {
                return ruleStatusEnum;
            }
        }
        throw new JeecgBootException(String.format("规则状态 错误：%s", value));
    }

    private final Integer value;
    private final String msg;

}
