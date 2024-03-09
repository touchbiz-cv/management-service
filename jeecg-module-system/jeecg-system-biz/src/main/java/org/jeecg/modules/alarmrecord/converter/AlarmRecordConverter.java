package org.jeecg.modules.alarmrecord.converter;

import org.jeecg.modules.alarmrecord.dto.AlarmRecordDto;
import org.jeecg.modules.alarmrecord.entity.AlarmRecord;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlarmRecordConverter {

    AlarmRecordConverter INSTANCE = Mappers.getMapper(AlarmRecordConverter.class);

    AlarmRecordDto transformOut(AlarmRecord in);
}