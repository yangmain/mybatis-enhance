/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 恒宇少年
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.gitee.hengboy.mybatis.enhance.common.helper.sql;

import com.gitee.hengboy.mybatis.enhance.common.OrmConfigConstants;
import com.gitee.hengboy.mybatis.enhance.common.enums.PlaceholderEnum;
import com.gitee.hengboy.mybatis.enhance.common.struct.ColumnStruct;
import com.gitee.hengboy.mybatis.enhance.exception.OrmCoreFrameworkException;
import com.gitee.hengboy.mybatis.enhance.named.OrPart;
import com.gitee.hengboy.mybatis.enhance.named.helper.NamedMethodHelper;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 提供Mybatis Xml形式构建Mapper内sql的工具类
 *
 * @author：于起宇 <br/>
 * ===============================
 * Created with IDEA.
 * Date：2018/4/14
 * Time：下午4:15
 * 简书：http://www.jianshu.com/u/092df3f77bca
 * ================================
 */
public class MapperXmlMySqlHelper {

    /**
     * 处理MyBatis的xml形式sql必须使用<script>开头以及</script>结尾
     *
     * @param expression 表达式内容
     * @return
     * @throws OrmCoreFrameworkException
     */
    public static String script(String expression, String... expressions) throws OrmCoreFrameworkException {
        if (StringUtils.isEmpty(expression)) {
            throw new OrmCoreFrameworkException("生成xml形式的sql时请至少传递一个[expression].");
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("<script>");
        buffer.append(expression);

        // 追加数组内的表达式
        if (!ObjectUtils.isEmpty(expressions)) {
            for (String content : expressions) {
                buffer.append(content);
            }
        }
        buffer.append("</script>");
        return buffer.toString();
    }

    /**
     * 获取命名规则查询的where的条件内容
     *
     * @param methodName     方法名称
     * @param columnMappings 列映射集合
     * @return
     * @throws OrmCoreFrameworkException
     */
    public static String named(String methodName, Map<String, ColumnStruct> columnMappings) throws OrmCoreFrameworkException {
        StringBuffer buffer = new StringBuffer();
        List<OrPart> orParts = NamedMethodHelper.getNamedWhereSql(methodName, columnMappings);
        if (!ObjectUtils.isEmpty(orParts)) {
            // 遍历所有OrPart
            for (int j = 0; j < orParts.size(); j++) {
                OrPart orPart = orParts.get(j);
                // 获取OrPart内的查询条件子项
                List<String> predicates = orPart.getPredicates();
                // 遍历每一个查询条件子项
                for (int i = 0; i < predicates.size(); i++) {
                    buffer.append(predicates.get(i));
                    // 每个查询条件采用and方式拼接
                    if (i < predicates.size() - 1) {
                        buffer.append(PlaceholderEnum.AND.getValue());
                    }
                }
                // 每一个OrPart采用or方式拼接
                if (j < orParts.size() - 1) {
                    buffer.append(PlaceholderEnum.OR.getValue());
                }
            }
        }
        return buffer.toString();
    }

    /**
     * 生成update xx
     *
     * @param tableName 表名
     * @return
     * @throws OrmCoreFrameworkException
     */
    public static String update(String tableName) throws OrmCoreFrameworkException {
        if (StringUtils.isEmpty(tableName)) {
            throw new OrmCoreFrameworkException("生成update表达式时，请传递[tableName].");
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(PlaceholderEnum.UPDATE.getValue());
        buffer.append(tableName);
        return buffer.toString();
    }

    /**
     * 生成delete from标签的xml字符串
     *
     * @param tableName 表名
     * @return
     * @throws OrmCoreFrameworkException
     */
    public static String delete(String tableName) throws OrmCoreFrameworkException {
        if (StringUtils.isEmpty(tableName)) {
            throw new OrmCoreFrameworkException("生成deleteFrom表达式时，请传递[tableName].");
        }
        return PlaceholderEnum.DELETE_FROM.getValue() + tableName;
    }

    /**
     * 生成select from标签的xml字符串
     *
     * @param tableName 表名
     * @param columns   列名数组
     * @return
     * @throws OrmCoreFrameworkException
     */
    public static String select(String tableName, String[] columns) throws OrmCoreFrameworkException {
        if (StringUtils.isEmpty(tableName)) {
            throw new OrmCoreFrameworkException("生成selectFrom表达式时，请传递[tableName].");
        }
        if (ObjectUtils.isEmpty(columns)) {
            throw new OrmCoreFrameworkException("生成selectFrom表达式时，请传递[columns].");
        }

        return new SQL().SELECT(columns).FROM(tableName).toString();
    }

    /**
     * 生成set标签xml字符串
     * <set>
     * xxxx = #{xxx}
     * </set>
     *
     * @param expressions 表达式数组集合
     * @return
     * @throws OrmCoreFrameworkException
     */
    public static String set(String... expressions) throws OrmCoreFrameworkException {
        if (ObjectUtils.isEmpty(expressions)) {
            throw new OrmCoreFrameworkException("生成update表达式时，请传递[sets].");
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("<set>");
        for (String set : expressions) {
            buffer.append(set);
            buffer.append(PlaceholderEnum.SPLIT.getValue());
        }
        buffer.append("</set>");
        return buffer.toString();
    }

    /**
     * 生成where标签xml字符串
     * 传递多个表达式
     * <where>
     * <if test="xxx!=null">
     * xxxxxxx
     * </if>
     * </where>
     *
     * @param expression  表达式
     * @param expressions 表达式可变数组参数
     * @return
     * @throws OrmCoreFrameworkException
     */
    public static String where(String expression, String... expressions) throws OrmCoreFrameworkException {
        if (StringUtils.isEmpty(expression) && ObjectUtils.isEmpty(expressions)) {
            throw new OrmCoreFrameworkException("where表达式生成时请至少传递一个[expression]表达式.");
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("<where>");
        buffer.append(expression);

        // 如果存在表达式数组
        if (!ObjectUtils.isEmpty(expressions)) {
            // 遍历追加表达式
            for (String express : expressions) {
                buffer.append(express);
            }
        }
        buffer.append("</where>");
        return buffer.toString();
    }

    /**
     * if表达式
     * <if test="xxx!=null">
     * contents
     * </if>
     *
     * @param paramName   参数名称
     * @param expression  表达式
     * @param expressions 表达式数据
     * @return
     * @throws OrmCoreFrameworkException
     */
    public static String ifNotNull(String paramName, String expression, String... expressions) throws OrmCoreFrameworkException {
        if (StringUtils.isEmpty(expression) && ObjectUtils.isEmpty(expressions)) {
            throw new OrmCoreFrameworkException("if表达式生成时请至少传递一个[content].");
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("<if test='");
        buffer.append(paramName);
        buffer.append("!=null'>");
        buffer.append(expression);
        // 如果存在数据表达式时，循环追加到if内
        if (!ObjectUtils.isEmpty(expressions)) {
            for (String content : expressions) {
                buffer.append(content);
            }
        }
        buffer.append("</if>");
        return buffer.toString();
    }

    /**
     * 生成in查询条件的xml字符串
     *
     * @param columnName 列名
     * @param collection 集合名词
     * @param item       每一个项的名称
     * @return
     * @throws OrmCoreFrameworkException
     */
    public static String in(String columnName, String collection, String item) throws OrmCoreFrameworkException {
        if (StringUtils.isEmpty(columnName)) {
            throw new OrmCoreFrameworkException("in表达式生成时需要传递[columnName]参数.");
        }

        StringBuffer buffer = new StringBuffer();
        // 追加列名
        buffer.append(columnName);
        // in 条件
        buffer.append(PlaceholderEnum.IN.getValue());
        // foreach追加元素内容
        buffer.append(foreach(collection, item, "(", ")", ","));

        // 如果collection非空时再进行处理foreach
        return ifNotNull(collection, buffer.toString());
    }

    /**
     * 生成foreach标签xml字符串
     * <foreach collection="" item="" open="" close="" separator="">
     *
     * </foreach>
     *
     * @param collection 遍历集合的名称
     * @param item       每一个项的名称
     * @param open       开始字符串
     * @param close      结束字符串
     * @param separator  每一个项的分隔符
     * @return
     */
    public static String foreach(String collection, String item, String open, String close, String separator) throws OrmCoreFrameworkException {
        if (StringUtils.isEmpty(collection) || StringUtils.isEmpty(item) || StringUtils.isEmpty(separator)) {
            throw new OrmCoreFrameworkException("请检查[collection]、[item]、[separator]等参数是否传递.");
        }
        StringBuffer sqlBuffer = new StringBuffer("<foreach ");
        sqlBuffer.append("collection = '");
        sqlBuffer.append(collection);
        sqlBuffer.append("' item = '");
        sqlBuffer.append(item);
        // 存在open时进行追加
        if (!StringUtils.isEmpty(open)) {
            sqlBuffer.append("' open = '");
            sqlBuffer.append(open);
        }
        // 存在close时进行追加
        if (!StringUtils.isEmpty(close)) {
            sqlBuffer.append("' close = '");
            sqlBuffer.append(close);
        }
        sqlBuffer.append("' separator = '");
        sqlBuffer.append(separator);
        sqlBuffer.append("'>");
        sqlBuffer.append(OrmConfigConstants.PARAMETER_PREFIX);
        sqlBuffer.append(item);
        sqlBuffer.append(OrmConfigConstants.PARAMETER_SUFFIX);
        sqlBuffer.append("</foreach>");
        return sqlBuffer.toString();
    }

    /**
     * 生成分页查询SQL
     * 如：limit 20,10 或 limit 10
     *
     * @return
     * @throws OrmCoreFrameworkException
     */
    public static String pageable() throws OrmCoreFrameworkException {
        StringBuffer buffer = new StringBuffer();
        buffer.append(PlaceholderEnum.LIMIT.getValue());
        // 追加offset
        buffer.append(ifNotNull(OrmConfigConstants.PAGEABLE_PARAMETER_OFFSET_CONTENT, OrmConfigConstants.PAGEABLE_PARAMETER_OFFSET + PlaceholderEnum.SPLIT.getValue()));
        // 追加限制条数
        buffer.append(OrmConfigConstants.PAGEABLE_PARAMETER_LIMIT);

        return ifNotNull(OrmConfigConstants.PAGEABLE_PARAMETER_LIMIT_CONTENT, buffer.toString());
    }

    /**
     * 生成分页对象内的排序SQL
     * 如：order by ${pageable.sort.sorter}
     *
     * @return
     * @throws OrmCoreFrameworkException
     */
    public static String sort() throws OrmCoreFrameworkException {
        StringBuffer buffer = new StringBuffer();
        buffer.append(PlaceholderEnum.ORDER_BY.getValue());
        buffer.append(OrmConfigConstants.PAGEABLE_ORDER_BY);
        return ifNotNull(OrmConfigConstants.SORT_NAME, ifNotNull(OrmConfigConstants.SORT_COLUMN_NAME, buffer.toString()));
    }

    /**
     * 生成count统计的SQL
     *
     * @param tableName  表名
     * @param columnName 统计列的名称
     * @return
     * @throws OrmCoreFrameworkException
     */
    public static String count(String tableName, String columnName) throws OrmCoreFrameworkException {
        if (StringUtils.isEmpty(tableName) || StringUtils.isEmpty(columnName)) {
            throw new OrmCoreFrameworkException("请检查[tableName]、[columnName]等参数是否传递.");
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(PlaceholderEnum.SELECT.getValue());
        buffer.append(PlaceholderEnum.COUNT.getValue());
        buffer.append(PlaceholderEnum.SPLIT_PREFIX.getValue().trim());
        buffer.append(columnName);
        buffer.append(PlaceholderEnum.SPLIT_SUFFIX.getValue().trim());
        buffer.append(PlaceholderEnum.FROM.getValue());
        buffer.append(tableName);
        return buffer.toString();
    }
}
