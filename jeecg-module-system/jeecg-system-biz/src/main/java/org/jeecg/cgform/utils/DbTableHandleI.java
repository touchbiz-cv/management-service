package org.jeecg.cgform.utils;

import org.apache.commons.lang.StringUtils;
import org.jeecg.modules.online.config.d.a;

import java.sql.JDBCType;

import static org.jeecg.common.constant.CommonConstant.CHAR_BIG_Y;
import static org.jeecg.common.constant.DataBaseConstant.ID;
import static org.jeecg.common.constant.SymbolConstant.SINGLE_QUOTATION_MARK;

public class DbTableHandleI extends org.jeecg.modules.online.config.service.a.d{

    @Override
    public String getMatchClassTypeByDataType(String dataType, int digits) {
        String var3 = "";

        if (JDBCType.VARCHAR.getName().equalsIgnoreCase(dataType)) {
            var3 = "string";
        } else if (JDBCType.DOUBLE.getName().equalsIgnoreCase(dataType)) {
            var3 = JDBCType.DOUBLE.getName().toLowerCase();
        } else if ("int".equalsIgnoreCase(dataType)) {
            var3 = "int";
        } else if (JDBCType.DATE.getName().equalsIgnoreCase(dataType)) {
            var3 = JDBCType.DATE.getName().toLowerCase();
        } else if ("Datetime".equalsIgnoreCase(dataType)) {
            var3 = "datetime";
        } else if (JDBCType.DECIMAL.getName().equalsIgnoreCase(dataType)) {
            var3 = "bigdecimal";
        } else if ("text".equalsIgnoreCase(dataType)) {
            var3 = "text";

        } else if (JDBCType.BLOB.getName().equalsIgnoreCase(dataType)) {
            var3 = "blob";
        }
        else if (JDBCType.TIMESTAMP.getName().equalsIgnoreCase(dataType)) {
            var3 = "timestamp";
        }

        return var3;
    }

    private String a(a var1) {
        String var3 = "";
        if ("string".equalsIgnoreCase(var1.getColunmType())) {
            var3 = var1.getColumnName() + " varchar(" + var1.getColumnSize() + ") " + (CHAR_BIG_Y.equals(var1.getIsNullable()) ? "NULL" : "NOT NULL");
        } else if (JDBCType.DATE.getName().equalsIgnoreCase(var1.getColunmType())) {
            var3 = var1.getColumnName() + " date " + (CHAR_BIG_Y.equals(var1.getIsNullable()) ? "NULL" : "NOT NULL");
        } else if ("datetime".equalsIgnoreCase(var1.getColunmType())) {
            var3 = var1.getColumnName() + " datetime " + (CHAR_BIG_Y.equals(var1.getIsNullable()) ? "NULL" : "NOT NULL");
        } else if ("int".equalsIgnoreCase(var1.getColunmType())) {
            var3 = var1.getColumnName() + " int(" + var1.getColumnSize() + ") " + (CHAR_BIG_Y.equals(var1.getIsNullable()) ? "NULL" : "NOT NULL");
        }
        else if ("bigint".equalsIgnoreCase(var1.getColunmType())) {
            var3 = var1.getColumnName() + " bigint(" + var1.getColumnSize() + ") " + (CHAR_BIG_Y.equals(var1.getIsNullable()) ? "NULL" : "NOT NULL");
        }
        else if (JDBCType.DOUBLE.getName().equalsIgnoreCase(var1.getColunmType())) {
            var3 = var1.getColumnName() + " double(" + var1.getColumnSize() + "," + var1.getDecimalDigits() + ") " + (CHAR_BIG_Y.equals(var1.getIsNullable()) ? "NULL" : "NOT NULL");
        } else if ("bigdecimal".equalsIgnoreCase(var1.getColunmType())) {
            var3 = var1.getColumnName() + " decimal(" + var1.getColumnSize() + "," + var1.getDecimalDigits() + ") " + (CHAR_BIG_Y.equals(var1.getIsNullable()) ? "NULL" : "NOT NULL");
        } else if ("text".equalsIgnoreCase(var1.getColunmType())) {
            var3 = var1.getColumnName() + " text " + (CHAR_BIG_Y.equals(var1.getIsNullable()) ? "NULL" : "NOT NULL");
        } else if (JDBCType.BLOB.getName().equalsIgnoreCase(var1.getColunmType())) {
            var3 = var1.getColumnName() + " blob " + (CHAR_BIG_Y.equals(var1.getIsNullable()) ? "NULL" : "NOT NULL");
        }
        else if (JDBCType.TIMESTAMP.getName().equalsIgnoreCase(var1.getColunmType())) {
            var3 = var1.getColumnName() + " timestamp " + (CHAR_BIG_Y.equals(var1.getIsNullable()) ? "NULL" : "NOT NULL");
        }

        var3 = var3 + (StringUtils.isNotEmpty(var1.getComment()) ? " COMMENT '" + var1.getComment() + SINGLE_QUOTATION_MARK : " ");
        var3 = var3 + (StringUtils.isNotEmpty(var1.getFieldDefault()) ? " DEFAULT " + var1.getFieldDefault() : " ");
        String var4 = var1.getPkType();
        if (ID.equalsIgnoreCase(var1.getColumnName()) && ("SEQUENCE".equalsIgnoreCase(var4) || "NATIVE".equalsIgnoreCase(var4))) {
            var3 = var3 + " AUTO_INCREMENT ";
        }

        return var3;
    }

    @Override
    public String getAddColumnSql(a columnMeta) {
        return " ADD COLUMN " + this.a(columnMeta) + ";";
    }
    @Override
    public String getReNameFieldName(a columnMeta) {
        return "CHANGE COLUMN " + columnMeta.getOldColumnName() + " " + this.a(columnMeta) + " ;";
    }
    @Override
    public String getUpdateColumnSql(a cgformcolumnMeta, a datacolumnMeta) {
        return " MODIFY COLUMN " + this.a(cgformcolumnMeta) + ";";
    }


}
