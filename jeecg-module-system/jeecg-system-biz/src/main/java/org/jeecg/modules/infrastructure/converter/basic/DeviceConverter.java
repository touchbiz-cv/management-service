package org.jeecg.modules.infrastructure.converter.basic;

import org.jeecg.modules.alarmrecord.dto.DeviceDto;
import org.jeecg.modules.alarmrecord.dto.DeviceUpdateDto;
import org.jeecg.modules.alarmrecord.entity.Device;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeviceConverter {

    DeviceConverter INSTANCE = Mappers.getMapper(DeviceConverter.class);

    Device transformIn(DeviceDto in);
    Device transformUpdateIn(DeviceUpdateDto in);

}
