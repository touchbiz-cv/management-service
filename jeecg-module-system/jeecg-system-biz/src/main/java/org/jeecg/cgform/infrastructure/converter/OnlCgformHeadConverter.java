package org.jeecg.cgform.infrastructure.converter;

import org.jeecg.modules.online.cgform.entity.OnlCgformHead;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import static org.jeecg.common.constant.CommonConstant.CHAR_BIG_N;
import static org.jeecg.common.constant.DataBaseConstant.ID;

/**
 * @author jiangyan
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OnlCgformHeadConverter {

    OnlCgformHeadConverter INSTANCE = Mappers.getMapper(OnlCgformHeadConverter.class);

    @Mapping(constant =  "1", target = "copyType")
    @Mapping(constant = "1", target = "tableVersion")
    @Mapping(constant = "1", target = "tableType")
    @Mapping(constant = CHAR_BIG_N, target = "isDbSynch")
    @Mapping(source = "tableVersion", target = "copyVersion")
    @Mapping(source = ID, target = "physicId")
    @Mapping(ignore = true, target = "hascopy")
    @Mapping(ignore = true, target = "taskId")
    @Mapping(ignore = true, target = "lowAppId")
    @Mapping(ignore = true, target = "selectFieldString")
    @Mapping(ignore = true, target = "tabOrderNum")
    @Mapping(ignore = true, target = "subTableStr")

    OnlCgformHead copy(OnlCgformHead in);


}