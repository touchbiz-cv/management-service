package org.jeecg.cgform.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.annotation.DbType;
import com.touchbiz.common.entity.exception.ParamException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.jeecg.cgform.entity.CgHeadConfigEntity;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.dynamic.db.DbTypeUtils;
import org.jeecg.modules.online.cgform.entity.OnlCgformField;
import org.jeecg.modules.online.config.d.e;
import org.jeecg.modules.online.config.d.h;
import org.jeecg.modules.online.config.exception.a;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.jeecg.common.constant.CommonConstant.CHAR_BIG_N;
import static org.jeecg.common.constant.CommonConstant.CHAR_BIG_Y;
import static org.jeecg.common.constant.DataBaseConstant.ID;
import static org.jeecg.common.constant.SymbolConstant.SINGLE_QUOTATION_MARK;
import static org.jeecg.common.util.CommonUtils.getDatabaseTypeEnum;

/**
 * @author jiangyan
 */
@Slf4j
public class DbGenerator extends org.jeecg.modules.online.config.d.d {

    private final DbTableHandleI dbTableHandleI;

    public DbGenerator() throws SQLException, a {
        this.dbTableHandleI = new DbTableHandleI();
    }

    public List<String> generateAlterSql(DruidDataSource ds, CgHeadConfigEntity config) {
        DbType var2 = getDatabaseTypeEnum();
        String var3 = DbTypeUtils.getDbTypeString(var2);
        String tableName = e.a(config.getTableName(), var3);
        String var5 = "alter table  " + tableName + " ";
        List<String> alterSql = new ArrayList<>();

        try {
            Map<String, org.jeecg.modules.online.config.d.a> originFields = this.map(ds, tableName);
            Map<String, org.jeecg.modules.online.config.d.a>  cgFields= this.map(config);
            Map<String,String> var9 = this.map(config.getColumns());
            Iterator<String> var10 = cgFields.keySet().iterator();

            while(true) {
                String var11;
                do {
                    if (!var10.hasNext()) {
                        var10 = originFields.keySet().iterator();

                        while(var10.hasNext()) {
                            var11 = var10.next();
                            if (!cgFields.containsKey(var11.toLowerCase()) && !var9.containsValue(var11.toLowerCase())) {
                                alterSql.add(var5 + this.dropColumnSql(var11));
                            }
                        }

                        if (DbType.DB2.equals(var2)) {
                            alterSql.add("CALL SYSPROC.ADMIN_CMD('reorg table " + tableName + "')");
                        }

                        return alterSql;
                    }

                    var11 = var10.next();
                } while(ID.equalsIgnoreCase(var11));

                org.jeecg.modules.online.config.d.a var12;
                if (!originFields.containsKey(var11)) {
                    var12 = cgFields.get(var11);
                    String var17 = var9.get(var11);
                    if (var9.containsKey(var11) && originFields.containsKey(var17)) {
                        var var14 = originFields.get(var17);
                        String var15;
                        if (DbType.HSQL.equals(var2)) {
                            this.handleUpdateMultiSql(var14, var12, tableName, alterSql);
                        } else {
                            var15 = dbTableHandleI.getReNameFieldName(var12);
                            if (DbTypeUtils.dbTypeIsSqlServer(var2)) {
                                alterSql.add(var15);
                            } else {
                                alterSql.add(var5 + var15);
                            }

                            if (DbType.DB2.equals(var2)) {
                                this.handleUpdateMultiSql(var14, var12, tableName, alterSql);
                            } else {
                                if (!var14.equals(var12)) {
                                    alterSql.add(var5 + this.getUpdateColumnSql(var12, var14));
                                    if (DbTypeUtils.dbTypeIsPostgre(var2)) {
                                        alterSql.add(var5 + this.b(var12, var14));
                                    }
                                }

                                if (!DbTypeUtils.dbTypeIsSqlServer(var2) && !var14.b(var12)) {
                                    alterSql.add(this.getCommentSql(var12));
                                }
                            }
                        }

                        var15 = this.map(var11, var12.getColumnId());
                        alterSql.add(var15);
                    } else {
                        alterSql.add(var5 + this.getAddColumnSql(var12));
                        if (!DbTypeUtils.dbTypeIsSqlServer(var2) && StringUtils.isNotEmpty(var12.getComment())) {
                            alterSql.add(this.getCommentSql(var12));
                        }
                    }
                } else {
                    var12 = originFields.get(var11);
                    org.jeecg.modules.online.config.d.a var13 = cgFields.get(var11);
                    if (!DbType.DB2.equals(var2) && !DbType.HSQL.equals(var2)) {
                        if (!var12.a(var13, var2)) {
                            alterSql.add(var5 + this.getUpdateColumnSql(var13, var12));
                        }

                        if (!DbTypeUtils.dbTypeIsSqlServer(var2) && !DbTypeUtils.dbTypeIsOracle(var2) && !var12.b(var13)) {
                            alterSql.add(this.getCommentSql(var13));
                        }
                    } else {
                        this.handleUpdateMultiSql(var12, var13, tableName, alterSql);
                    }
                }
            }
        } catch (SQLException var16) {
            throw new ParamException(var16.getMessage());
        }
    }

    private Map<String, org.jeecg.modules.online.config.d.a> map(DruidDataSource dataSource, String var2) throws SQLException {
        Map<String, org.jeecg.modules.online.config.d.a> var4 = new HashMap<>(5);

        var connection = dataSource.getConnection();
        DatabaseMetaData metaData = connection.getMetaData();
        String username = dataSource.getUsername();
        DbType dbType = CommonUtils.getDatabaseTypeEnum();
        if (DbTypeUtils.dbTypeIsOracle(dbType) || DbType.DB2.equals(dbType)) {
            username = username.toUpperCase();
        }

        ResultSet resultSet;
        if (DbTypeUtils.dbTypeIsSqlServer(dbType)) {
            resultSet = metaData.getColumns(connection.getCatalog(), null, var2, "%");
        } else if (DbTypeUtils.dbTypeIsPostgre(dbType)) {
            resultSet = metaData.getColumns(connection.getCatalog(), "public", var2, "%");
        } else if (DbType.HSQL.equals(dbType)) {
            resultSet = metaData.getColumns(connection.getCatalog(), "PUBLIC", var2.toUpperCase(), "%");
        } else {
            resultSet = metaData.getColumns(connection.getCatalog(), username, var2, "%");
        }

        while(resultSet.next()) {
            org.jeecg.modules.online.config.d.a var10 = new org.jeecg.modules.online.config.d.a();
            var10.setTableName(var2);
            String var11 = resultSet.getString("COLUMN_NAME").toLowerCase();
            var10.setColumnName(var11);
            String var12 = resultSet.getString("TYPE_NAME");
            int var13 = resultSet.getInt("DECIMAL_DIGITS");
            String var14 = this.dbTableHandleI.getMatchClassTypeByDataType(var12, var13);
            var10.setColunmType(var14);
            var10.setRealDbType(var12);
            int var15 = resultSet.getInt("COLUMN_SIZE");
            var10.setColumnSize(var15);
            var10.setDecimalDigits(var13);
            String var16 = resultSet.getInt("NULLABLE") == 1 ? CHAR_BIG_Y : CHAR_BIG_N;
            var10.setIsNullable(var16);
            String var17 = resultSet.getString("REMARKS");
            var10.setComment(var17);
            String var18 = resultSet.getString("COLUMN_DEF");
            String var19 = map(var18) == null ? "" : map(var18);
            var10.setFieldDefault(var19);
            var4.put(var11, var10);
        }

        return var4;
    }

    private static String map(String var0) {
        if (StringUtils.isNotEmpty(var0)) {
            try {
                Double.valueOf(var0);
            } catch (Exception var2) {
                if (!var0.startsWith(SINGLE_QUOTATION_MARK) || !var0.endsWith(SINGLE_QUOTATION_MARK)) {
                    var0 = SINGLE_QUOTATION_MARK + var0 + SINGLE_QUOTATION_MARK;
                }
            }
        }
        return var0;
    }

    private Map<String, org.jeecg.modules.online.config.d.a> map(org.jeecg.modules.online.config.b.a var1) {
        return var1.getColumns().stream().map(field->{
            org.jeecg.modules.online.config.d.a column = new org.jeecg.modules.online.config.d.a();
            column.setTableName(var1.getTableName().toLowerCase());
            column.setColumnId(field.getId());
            column.setColumnName(field.getDbFieldName().toLowerCase());
            column.setColumnSize(field.getDbLength());
            column.setColunmType(field.getDbType().toLowerCase());
            column.setIsNullable(field.getDbIsNull() == 1 ? CHAR_BIG_Y : CHAR_BIG_N);
            column.setComment(field.getDbFieldTxt());
            column.setDecimalDigits(field.getDbPointLength());
            column.setFieldDefault(map(field.getDbDefaultVal()));
            column.setPkType(var1.getJformPkType() == null ? "UUID" : var1.getJformPkType());
            column.setOldColumnName(field.getDbFieldNameOld() != null ? field.getDbFieldNameOld().toLowerCase() : null);
            return column;
        }).collect(Collectors.toMap(org.jeecg.modules.online.config.d.a::getColumnName, y->y));

    }

    private Map<String, String> map(List<OnlCgformField> list) {
        return list.stream().collect(Collectors.toMap(
                OnlCgformField::getDbFieldName,
                field -> field.getDbFieldNameOld() != null ? field.getDbFieldNameOld() : "",
                (existingValue, newValue) -> existingValue,
                HashMap::new
        ));
    }

    private String dropColumnSql(String var1) {
        return dbTableHandleI.getDropColumnSql(var1);
    }

    private String b(org.jeecg.modules.online.config.d.a var1, org.jeecg.modules.online.config.d.a var2) {
        return dbTableHandleI.getSpecialHandle(var1, var2);
    }

    private void handleUpdateMultiSql(org.jeecg.modules.online.config.d.a var1, org.jeecg.modules.online.config.d.a var2, String var3, List<String> var4) {
        dbTableHandleI.handleUpdateMultiSql(var1, var2, var3, var4);
    }

    private String getAddColumnSql(org.jeecg.modules.online.config.d.a var1) {
        return dbTableHandleI.getAddColumnSql(var1);
    }

    private String getCommentSql(org.jeecg.modules.online.config.d.a var1) {
        return dbTableHandleI.getCommentSql(var1);
    }

    private String map(String var1, String var2) {
        return "update onl_cgform_field set DB_FIELD_NAME_OLD = '" + var1 + "' where ID ='" + var2 + SINGLE_QUOTATION_MARK;
    }

    private String getUpdateColumnSql(org.jeecg.modules.online.config.d.a var1, org.jeecg.modules.online.config.d.a var2) {
        return dbTableHandleI.getUpdateColumnSql(var1, var2);
    }

    @SneakyThrows
    public static void generateDdlSql(DruidDataSource dataSource, org.jeecg.modules.online.config.b.a var0) throws HibernateException {
        DbType dbType = getDatabaseTypeEnum();
        if (DbTypeUtils.dbTypeIsOracle(dbType)) {
            var0.getColumns().forEach(column->{
                if ("int".equals(column.getDbType())) {
                    column.setDbType("double");
                    column.setDbPointLength(0);
                }
            });
        }

        String tableTemplate = h.a("org/jeecg/cgform/tableTemplate.ftl", map(var0, dbType));
        log.info("template:{}", tableTemplate);


        HashMap<String,Object> stringObjectHashMap = new HashMap<>(5);
        stringObjectHashMap.put("hibernate.connection.driver_class", dataSource.getDriverClassName());
        stringObjectHashMap.put("hibernate.connection.url", dataSource.getUrl());
        stringObjectHashMap.put("hibernate.connection.username", dataSource.getUsername());
        String password = dataSource.getPassword();
        if (password != null) {
            stringObjectHashMap.put("hibernate.connection.password", password);
        }
        stringObjectHashMap.put("hibernate.show_sql", true);
        stringObjectHashMap.put("hibernate.format_sql", true);
        stringObjectHashMap.put("hibernate.temp.use_jdbc_metadata_defaults", false);
        stringObjectHashMap.put("hibernate.dialect", DbTypeUtils.getDbDialect(dbType));
        stringObjectHashMap.put("hibernate.hbm2ddl.auto", "create");
        stringObjectHashMap.put("hibernate.connection.autocommit", false);
        stringObjectHashMap.put("hibernate.current_session_context_class", "thread");
        var  standardServiceRegistry = new StandardServiceRegistryBuilder().applySettings(stringObjectHashMap).build();


        MetadataSources metadataSources = new MetadataSources(standardServiceRegistry);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(tableTemplate.getBytes(StandardCharsets.UTF_8));
        metadataSources.addInputStream(inputStream);
        Metadata metadata = metadataSources.buildMetadata();
        SchemaExport schemaExport = new SchemaExport();
        schemaExport.create(EnumSet.of(TargetType.DATABASE), metadata);

        inputStream.close();
        var exceptions = schemaExport.getExceptions();

        for (var o : exceptions) {
            Exception var11 = (Exception) o;
            if ("java.sql.SQLSyntaxErrorException".equals(var11.getCause().getClass().getName())) {
                SQLSyntaxErrorException var12 = (SQLSyntaxErrorException) var11.getCause();
                if ("42000".equals(var12.getSQLState())) {
                    if (1064 != var12.getErrorCode() && 903 != var12.getErrorCode()) {
                        continue;
                    }

                    log.error(var12.getMessage());
                    throw new ParamException("请确认表名是否为关键字。");
                }
            } else {
                if ("com.microsoft.sqlserver.jdbc.SQLServerException".equals(var11.getCause().getClass().getName())) {
                    if (var11.getCause().toString().contains("Incorrect syntax near the keyword")) {
                        log.error("", var11);
                        throw new ParamException(var11.getCause().getMessage());
                    }

                    log.error(var11.getMessage());
                    continue;
                }

                if (DbType.DM.equals(dbType) || DbType.DB2.equals(dbType)) {
                    String message = var11.getMessage();
                    if (message != null && message.contains("Error executing DDL \"drop table")) {
                        log.error(message);
                        continue;
                    }
                }
            }

            throw new ParamException(var11.getMessage());
        }

    }

    private static Map<String, Object> map(org.jeecg.modules.online.config.b.a var0, DbType var1) {
        String var2 = DbTypeUtils.getDbTypeString(var1);
        HashMap<String,Object> var3 = new HashMap<>(5);
        for (OnlCgformField var5 : var0.getColumns()) {
            var5.setDbDefaultVal(map(var5.getDbDefaultVal()));
        }
        var3.put("entity", var0);
        var3.put("dataType", var2);
        var3.put("db", var1.getDb());
        return var3;
    }


}
