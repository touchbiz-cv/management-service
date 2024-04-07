package org.jeecg.cgform.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static org.jeecg.common.constant.DataBaseConstant.ID;

/**
 * @author jiangyan
 */
@Slf4j
@Service
public class DbServiceImpl {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DbServiceImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public String executeInsert(String tableName, Map<String, Object> sqlParams) {
        // 使用 NamedParameterJdbcTemplate 执行 SQL
        SqlParameterSource[] parameters = SqlParameterSourceUtils.createBatch(sqlParams);

        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName(tableName);
        if(!sqlParams.containsKey(ID)){
            jdbcInsert.usingGeneratedKeyColumns(ID);
            Number generatedId = jdbcInsert.executeAndReturnKey(parameters[0]);
            // 获取插入后的自增 ID
            return String.valueOf(generatedId.longValue());
        }
        jdbcInsert.execute(parameters[0]);
        return String.valueOf(sqlParams.get(ID));
    }

    public void executeUpdate(String tableName, Map<String, Object> sqlParams) {
        String updateSql = buildUpdateSql(tableName, sqlParams.keySet());

        SqlParameterSource parameters = new MapSqlParameterSource(sqlParams);

        int rowsUpdated = namedParameterJdbcTemplate.update(updateSql, parameters);

        log.info("Rows updated: " + rowsUpdated);
    }

    private String buildUpdateSql(String tableName, Set<String> columnNames) {
        // 构建 UPDATE 语句
        StringBuilder updateSql = new StringBuilder("UPDATE ")
                .append(tableName)
                .append(" SET ");

        for (String columnName : columnNames) {
            updateSql.append(columnName).append("=:").append(columnName).append(", ");
        }

        // 移除最后一个逗号和空格
        updateSql.delete(updateSql.length() - 2, updateSql.length());

        // 这里假设有一个名为 "id" 的列作为主键，你可以根据实际情况修改
        updateSql.append(" WHERE id=:id");

        return updateSql.toString();
    }


}
