package org.jeecg.cgform.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.cgform.database.domain.CgformHeadDO;

import java.util.List;

/**
 * @author jiangyan
 */
public interface CgformHeadMapper extends BaseMapper<CgformHeadDO> {

    @Select({"select physic_id from onl_cgform_head GROUP BY physic_id"})
    List<String> queryCopyPhysicId();

    @Select("select table_name from onl_cgform_head where is_base='Y'")
    List<String> getBaseTable();

    @Update("update onl_cgform_head set is_base = #{isBase} where table_name = #{tableName}")
    void updateIsBase(@Param(value = "isBase") String isBase, @Param(value = "tableName") String tableName);

}
