package org.jeecg.cgform.utils;

import com.baomidou.mybatisplus.annotation.DbType;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.dynamic.db.DbTypeUtils;
import org.jeecg.modules.online.config.b.b;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class DbConfigUtil extends org.jeecg.modules.online.config.d.e{

    /**
     * 判断数据库表是否存在
     * @param tableName
     * @return
     */
    public static boolean tableIsExist(String tableName){
        return tableIsExist(tableName, null);
    }
    public static boolean tableIsExist(String tableName, b dbConfig){
        Connection var2 = null;
        ResultSet var3 = null;

        try {
            String[] var4 = new String[]{"TABLE"};
            if (dbConfig == null) {
                var2 = getConnection();
            } else {
                var2 = b(dbConfig);
            }

            DatabaseMetaData var5 = var2.getMetaData();
            DbType var6 = c(dbConfig);
            String var7 = DbTypeUtils.getDbTypeString(var6);
            String var8 = a(tableName, var7);
            String var9;
            if (dbConfig != null) {
                var9 = dbConfig.getUsername();
            } else {
                org.jeecg.modules.online.config.b.b var10 = SpringContextUtils.getBean(b.class);
                var9 = var10.getUsername();
            }

            if (DbTypeUtils.dbTypeIsOracle(var6) || DbType.DB2.equals(var6)) {
                var9 = var9 != null ? var9.toUpperCase() : null;
            }

            if (DbTypeUtils.dbTypeIsSqlServer(var6)) {
                var3 = var5.getTables(var2.getCatalog(), null, var8, var4);
            } else if (DbTypeUtils.dbTypeIsPostgre(var6)) {
                var3 = var5.getTables(var2.getCatalog(), "public", var8, var4);
            } else if (DbType.HSQL.equals(var6)) {
                var3 = var5.getTables(var2.getCatalog(), "PUBLIC", var8.toUpperCase(), var4);
            } else {
                var3 = var5.getTables(var2.getCatalog(), var9, var8, var4);
            }

            if (var3.next()) {
                return true;
            }
        } catch (SQLException var20) {
            throw new RuntimeException(var20.getMessage());
        } finally {
            try {
                if (var3 != null) {
                    var3.close();
                }

                if (var2 != null) {
                    var2.close();
                }
            } catch (SQLException var19) {
                log.error(var19.getMessage(), var19);
            }

        }

        return false;
    }
}
