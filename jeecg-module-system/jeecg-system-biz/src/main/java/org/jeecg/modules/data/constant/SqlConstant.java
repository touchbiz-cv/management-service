package org.jeecg.modules.data.constant;

import java.util.List;

/**
 * @Description: sql语法常量
 * @author: pangqn
 * @date: 2022年11月07日
 */
public class SqlConstant {

    public static final String ON = "on";

    public static final String EQUAL = "=";

    public static final String REGEXP = "REGEXP";

    public static final String IN = "in";

    public static final String SPOT = ".";

    public static final String SELECT = "select";

    public static final String INSERT = "insert";

    public static final String INSERT_INTO = "insert into ";

    public static final String UPDATE = "update";

    public static final String DELETE = "delete";

    public static final String SET = "set";

    public static final String WHERE = "where";

    public static final String FROM = "from";

    public static final String AND = "and";

    public static final String OR = "or";

    public static final String VALUE = "value";

    public static final String AS = "as";
    public static final String QUOTATION = "`";

    public static final String VARCHAR_QUOTATION = "'";

    public static final String VARCHAR_QUOTATION_2 = "\"";

    public static final String PARAM_LEFT = "{";
    public static final String PARAM_RIGHT = "}";

    public static final Long SOURCE_MATSER_ID = -1L;

    public static final List<String> NOT_IN_SELECT = List.of("delete ", "update ", "insert ", "truncate ", "drop ", "alter ");

}