package org.jeecg.cgform.utils;

import com.touchbiz.common.entity.exception.ParamException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.cgform.entity.CgTableEntity;
import org.jeecgframework.codegenerate.generate.pojo.ColumnVo;
import org.jeecgframework.codegenerate.generate.util.f;

import java.math.BigDecimal;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

import static org.jeecg.common.constant.SymbolConstant.COMMA;

/**
 *
 * @author jiangyan
 */
@Slf4j
public class DbReadTableUtil {

    /**
     * @param connection
     * @param tableName
     * @return
     * @throws Exception
     */
    public static List<ColumnVo> readOriginalTableColumn(Connection connection, String tableName) throws Exception {
        ResultSet resultSet;
        String sqlStr = null;
        List<ColumnVo> var3 = new ArrayList<>();

        int var5;
        Statement statement = null;
        try {
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String var4 = connection.getCatalog();
            DatabaseMetaData metaData = connection.getMetaData();

            // 获取连接 URL
            String url = metaData.getURL();
            log.info(" connect databaseName : " + var4);
            if (org.jeecgframework.codegenerate.database.a.a(url)) {
                sqlStr = MessageFormat.format("select column_name,data_type,column_comment,numeric_precision,numeric_scale,character_maximum_length,is_nullable nullable from information_schema.columns where table_name = {0} and table_schema = {1} order by ORDINAL_POSITION", org.jeecgframework.codegenerate.generate.util.f.c(tableName), org.jeecgframework.codegenerate.generate.util.f.c(var4));
            }

            if (org.jeecgframework.codegenerate.database.a.b(url)) {
                sqlStr = MessageFormat.format(" select colstable.column_name column_name, colstable.data_type data_type, commentstable.comments column_comment, colstable.Data_Precision column_precision, colstable.Data_Scale column_scale,colstable.Char_Length,colstable.nullable from user_tab_cols colstable  inner join user_col_comments commentstable  on colstable.column_name = commentstable.column_name  where colstable.table_name = commentstable.table_name  and colstable.table_name = {0}", org.jeecgframework.codegenerate.generate.util.f.c(tableName.toUpperCase()));
            }

            if (org.jeecgframework.codegenerate.database.a.d(url)) {
                sqlStr = MessageFormat.format("select icm.column_name as field,icm.udt_name as type,fieldtxt.descript as comment, icm.numeric_precision_radix as column_precision ,icm.numeric_scale as column_scale ,icm.character_maximum_length as Char_Length,icm.is_nullable as attnotnull  from information_schema.columns icm, (SELECT A.attnum,( SELECT description FROM pg_catalog.pg_description WHERE objoid = A.attrelid AND objsubid = A.attnum ) AS descript,A.attname \tFROM pg_catalog.pg_attribute A WHERE A.attrelid = ( SELECT oid FROM pg_class WHERE relname = {0} ) AND A.attnum > 0 AND NOT A.attisdropped  ORDER BY\tA.attnum ) fieldtxt where icm.table_name={1} and fieldtxt.attname = icm.column_name", org.jeecgframework.codegenerate.generate.util.f.c(tableName), org.jeecgframework.codegenerate.generate.util.f.c(tableName));
            }

            if (org.jeecgframework.codegenerate.database.a.c(url)) {
                sqlStr = MessageFormat.format("select distinct cast(a.name as varchar(50)) column_name,  cast(b.name as varchar(50)) data_type,  cast(e.value as NVARCHAR(200)) comment,  cast(ColumnProperty(a.object_id,a.Name,'''Precision''') as int) num_precision,  cast(ColumnProperty(a.object_id,a.Name,'''Scale''') as int) num_scale,  a.max_length,  (case when a.is_nullable=1 then '''y''' else '''n''' end) nullable,column_id   from sys.columns a left join sys.types b on a.user_type_id=b.user_type_id left join (select top 1 * from sys.objects where type = '''U''' and name ={0}  order by name) c on a.object_id=c.object_id left join sys.extended_properties e on e.major_id=c.object_id and e.minor_id=a.column_id and e.class=1 where c.name={0} order by a.column_id", org.jeecgframework.codegenerate.generate.util.f.c(tableName));
            }

            resultSet = statement.executeQuery(sqlStr);
            resultSet.last();
            var5 = resultSet.getRow();
            if (var5 <= 0) {
                throw new ParamException("该表不存在或者表中没有字段");
            }

            ColumnVo var7 = new ColumnVo();
            var7.setFieldName(org.jeecgframework.codegenerate.generate.util.f.d(resultSet.getString(1).toLowerCase()));

            var7.setFieldDbName(resultSet.getString(1).toUpperCase());
            var7.setPrecision(org.jeecgframework.codegenerate.generate.util.f.b(resultSet.getString(4)));
            var7.setScale(org.jeecgframework.codegenerate.generate.util.f.b(resultSet.getString(5)));
            var7.setCharmaxLength(org.jeecgframework.codegenerate.generate.util.f.b(resultSet.getString(6)));
            var7.setNullable(org.jeecgframework.codegenerate.generate.util.f.a(resultSet.getString(7)));
            var7.setFieldType(getFieldType(resultSet.getString(2).toLowerCase(), var7.getPrecision(), var7.getScale()));
            var7.setFieldDbType(getFieldDbType(resultSet.getString(2).toLowerCase()));
            org.jeecgframework.codegenerate.generate.util.f.a(var7);
            var7.setFiledComment(StringUtils.isBlank(resultSet.getString(3)) ? var7.getFieldName() : resultSet.getString(3));
            log.debug("columnt.getFieldName() -------------" + var7.getFieldName());
            var3.add(var7);

            while (true) {
                if (!resultSet.previous()) {
                    log.debug("读取表成功");
                    break;
                }

                ColumnVo var8 = new ColumnVo();
                var8.setFieldName(org.jeecgframework.codegenerate.generate.util.f.d(resultSet.getString(1).toLowerCase()));
                var8.setFieldDbName(resultSet.getString(1).toUpperCase());
                var8.setPrecision(org.jeecgframework.codegenerate.generate.util.f.b(resultSet.getString(4)));
                var8.setScale(org.jeecgframework.codegenerate.generate.util.f.b(resultSet.getString(5)));
                var8.setCharmaxLength(org.jeecgframework.codegenerate.generate.util.f.b(resultSet.getString(6)));
                var8.setNullable(org.jeecgframework.codegenerate.generate.util.f.a(resultSet.getString(7)));
                var8.setFieldType(getFieldType(resultSet.getString(2).toLowerCase(), var8.getPrecision(), var8.getScale()));
                var8.setFieldDbType(getFieldDbType(resultSet.getString(2).toLowerCase()));
                org.jeecgframework.codegenerate.generate.util.f.a(var8);
                var8.setFiledComment(StringUtils.isBlank(resultSet.getString(3)) ? var8.getFieldName() : resultSet.getString(3));
                var3.add(var8);
            }
        } finally {
            if (statement != null) {
                statement.close();
                System.gc();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return var3.stream()
                .sorted(Comparator.comparingInt(var3::indexOf))
                .toList();
    }


    public static String getFieldType(String fieldType, String length, String point) {
        if (fieldType.contains("char")) {
            fieldType = String.class.getName();
        }
        else if (fieldType.contains("bigint")) {
            fieldType = Long.class.getName();
        }

        else if (fieldType.contains("int")) {
            fieldType = Integer.class.getName();
        } else if (fieldType.contains("float")) {
            fieldType = Float.class.getName();
        } else if (fieldType.contains("double")) {
            fieldType = Double.class.getName();
        } else if (fieldType.contains("number")) {
            if (StringUtils.isNotBlank(point) && Integer.parseInt(point) > 0) {
                fieldType = BigDecimal.class.getName();
            } else if (StringUtils.isNotBlank(length) && Integer.parseInt(length) > 10) {
                fieldType = Long.class.getName();
            } else {
                fieldType = Integer.class.getName();
            }
        } else if (fieldType.contains("decimal")) {
            fieldType = BigDecimal.class.getName();
        } else if (fieldType.contains("date")) {
            fieldType = Date.class.getName();
        }
        else if (fieldType.contains("timestamp")) {
            fieldType = Timestamp.class.getName();
        }
        else if (fieldType.contains("time")) {
            fieldType = Date.class.getName();
        } else if (fieldType.contains("blob")) {
            fieldType = "byte[]";
        } else if (fieldType.contains("clob")) {
            fieldType = Clob.class.getName();
        } else if (fieldType.contains("numeric")) {
            fieldType = BigDecimal.class.getName();
        } else {
            fieldType = Object.class.getName();
        }

        return fieldType;
    }

    public static String getFieldDbType(String var0) {
        String[] var1 = var0.split("_");
        int var2 = 0;
        StringBuilder var0Builder = new StringBuilder(var0);
        for(int index = var1.length; var2 < index; ++var2) {
            if (var2 > 0) {
                String var4 = var1[var2].toLowerCase();
                var4 = var4.substring(0, 1).toUpperCase() + var4.substring(1);
                var0Builder.append(var4);
            } else {
                var0Builder.append(var1[var2].toLowerCase());
            }
        }
        return var0Builder.toString();
    }

    public static List<String> readAllTableNames(Connection connection) throws SQLException {
        String sqlStr = null;
        ArrayList<String> list = new ArrayList<>(0);
        Statement statement = null;
        try {
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String catalog = connection.getCatalog();
            log.info(" connect databaseName : " + catalog);
            DatabaseMetaData metaData = connection.getMetaData();

            // 获取连接 URL
            String url = metaData.getURL();
            //是否为mysql，mariadb等开源数据库
            if (org.jeecgframework.codegenerate.database.a.a(url)) {
                sqlStr = MessageFormat.format("select table_name  from information_schema.tables where table_schema = {0}", org.jeecgframework.codegenerate.generate.util.f.c(catalog));
            }

            //是否为oracle
            else if (org.jeecgframework.codegenerate.database.a.b(url)) {
                sqlStr = " select distinct colstable.table_name as  table_name from user_tab_cols colstable order by colstable.table_name";
            }

            //是否为pg
            else if (org.jeecgframework.codegenerate.database.a.d(url)) {
                if (!connection.getSchema().contains(COMMA)) {
                    sqlStr = MessageFormat.format("select tablename from pg_tables where schemaname in( {0} )", org.jeecgframework.codegenerate.generate.util.f.c(connection.getCatalog()));
                } else {
                    String[] var5 = connection.getSchema().split(COMMA);
                    var stringBuilder = Arrays.stream(var5).map(var9 -> f.c(var9) + ",").collect(Collectors.joining());

                    sqlStr = MessageFormat.format("select tablename from pg_tables where schemaname in( {0} )", stringBuilder.substring(0, stringBuilder.length() - 1));
                }
            }

            //是否为sqlServer
            else if (org.jeecgframework.codegenerate.database.a.c(url)) {
                sqlStr = "select distinct c.name as  table_name from sys.objects c where c.type = 'U' ";
            }

            log.debug("sql:{}", sqlStr);
            ResultSet resultSet = statement.executeQuery(sqlStr);

            while(resultSet.next()) {
                String var20 = resultSet.getString(1);
                list.add(var20);
            }
        } catch (Exception err) {
            log.error("", err);
        } finally {
            if (statement != null) {
                statement.close();
                System.gc();
            }
            if (connection != null) {
                connection.close();
            }
        }

        return list;
    }

    public static List<CgTableEntity> readAllTableNamesV2(Connection connection) throws SQLException {
        String sqlStr = null;
        List<CgTableEntity> tableList = new ArrayList<>(5);
        Statement statement = null;
        try {
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String catalog = connection.getCatalog();
            log.info(" connect databaseName : " + catalog);
            DatabaseMetaData metaData = connection.getMetaData();

            // 获取连接 URL
            String url = metaData.getURL();
            //是否为mysql，mariadb等开源数据库
            if (org.jeecgframework.codegenerate.database.a.a(url)) {
                sqlStr = MessageFormat.format("select table_name,table_type from information_schema.tables where table_schema = {0}", org.jeecgframework.codegenerate.generate.util.f.c(catalog));
            }

            //是否为oracle
            else if (org.jeecgframework.codegenerate.database.a.b(url)) {
                sqlStr = " select distinct colstable.table_name as  table_name from user_tab_cols colstable order by colstable.table_name";
            }

            //是否为pg
            else if (org.jeecgframework.codegenerate.database.a.d(url)) {
                if (!connection.getSchema().contains(COMMA)) {
                    sqlStr = MessageFormat.format("select tablename from pg_tables where schemaname in( {0} )", org.jeecgframework.codegenerate.generate.util.f.c(connection.getCatalog()));
                } else {
                    String[] var5 = connection.getSchema().split(COMMA);
                    var stringBuilder = Arrays.stream(var5).map(var9 -> f.c(var9) + ",").collect(Collectors.joining());

                    sqlStr = MessageFormat.format("select tablename from pg_tables where schemaname in( {0} )", stringBuilder.substring(0, stringBuilder.length() - 1));
                }
            }

            //是否为sqlServer
            else if (org.jeecgframework.codegenerate.database.a.c(url)) {
                sqlStr = "select distinct c.name as  table_name from sys.objects c where c.type = 'U' ";
            }

            log.debug("sql:{}", sqlStr);
            ResultSet resultSet = statement.executeQuery(sqlStr);

            while(resultSet.next()) {
                CgTableEntity entity = new CgTableEntity();
                entity.setTableName(resultSet.getString(1));
                var tableType = resultSet.getString(2);
                if(tableType.contains("VIEW")){
                    entity.setTableType("VIEW");
                    entity.setTableTypeDesc("视图");
                }
                else{
                    entity.setTableType("TABLE");
                    entity.setTableTypeDesc("物理表");
                }

                tableList.add(entity);
            }
        } catch (Exception err) {
            log.error("", err);
        } finally {
            if (statement != null) {
                statement.close();
                System.gc();
            }
        }

        return tableList;
    }

    @SneakyThrows
    public static boolean isView(Connection connection, String tableName){
        String sqlStr = null;
        Statement statement = null;
        try {
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String catalog = connection.getCatalog();
            log.info(" connect databaseName : " + catalog);
            DatabaseMetaData metaData = connection.getMetaData();

            // 获取连接 URL
            String url = metaData.getURL();
            //是否为mysql，mariadb等开源数据库
            if (org.jeecgframework.codegenerate.database.a.a(url)) {
                sqlStr = MessageFormat.format("select table_type from information_schema.tables where table_schema = {0} and table_name={1}", org.jeecgframework.codegenerate.generate.util.f.c(catalog), org.jeecgframework.codegenerate.generate.util.f.c(tableName));
            }

            //是否为oracle
            else if (org.jeecgframework.codegenerate.database.a.b(url)) {
                sqlStr = " select distinct colstable.table_name as  table_name from user_tab_cols colstable order by colstable.table_name";
            }

            //是否为pg
            else if (org.jeecgframework.codegenerate.database.a.d(url)) {
                if (!connection.getSchema().contains(COMMA)) {
                    sqlStr = MessageFormat.format("select tablename from pg_tables where schemaname in( {0} )", org.jeecgframework.codegenerate.generate.util.f.c(connection.getCatalog()));
                } else {
                    String[] var5 = connection.getSchema().split(COMMA);
                    var stringBuilder = Arrays.stream(var5).map(var9 -> f.c(var9) + ",").collect(Collectors.joining());

                    sqlStr = MessageFormat.format("select tablename from pg_tables where schemaname in( {0} )", stringBuilder.substring(0, stringBuilder.length() - 1));
                }
            }

            //是否为sqlServer
            else if (org.jeecgframework.codegenerate.database.a.c(url)) {
                sqlStr = "select distinct c.name as  table_name from sys.objects c where c.type = 'U' ";
            }

            log.debug("sql:{}", sqlStr);
            ResultSet resultSet = statement.executeQuery(sqlStr);

            if(resultSet.next()) {
                var tableType = resultSet.getString(1);
                return tableType.contains("VIEW");

            }
        } catch (Exception err) {
            log.error("", err);
        } finally {
            if (statement != null) {
                statement.close();
                System.gc();
            }
        }
        return false;
    }
}
