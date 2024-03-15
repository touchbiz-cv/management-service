package org.jeecg.modules.monitor.converter;


import io.fabric8.kubernetes.api.model.Pod;
import org.jeecg.modules.monitor.vo.PodVo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface K8sConverter {

    K8sConverter INSTANCE = Mappers.getMapper(K8sConverter.class);

    PodVo transformOut(Pod in);
}