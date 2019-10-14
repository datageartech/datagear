/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.sql.ResultSet;
import java.util.List;

import org.datagear.model.Model;

/**
 * 数据库方言。
 * 
 * @author datagear@163.com
 *
 */
public interface Dialect
{
	/**
	 * 为给定名字添加标识符引用符号。
	 * 
	 * @param name
	 * @return
	 */
	String quote(String name);

	/**
	 * 获取标识符引用符号。
	 * <p>
	 * 数据库中，对于包含某些特殊字符（比如：'.'）的标识符，通常需要在两侧添加引用符号，否则会出现语法错误。
	 * </p>
	 * 
	 * @return
	 */
	String getIdentifierQuote();

	/**
	 * 是否支持分页查询SQL。
	 * <p>
	 * 某些数据库可能不支持SQL级的分页查询，只能通过{@linkplain ResultSet}实现分页查询。
	 * </p>
	 * 
	 * @return
	 */
	boolean supportsPagingSql();

	/**
	 * 构建分页查询SQL。
	 * <p>
	 * 如果由于参数不合法等原因无法构建分页查询SQL，则返回{@code null}。
	 * </p>
	 * 
	 * @param queryView
	 *            查询视图SQL
	 * @param condition
	 *            查询条件SQL，允许为{@code null}
	 * @param orders
	 *            排序集，允许为{@code null}或者空数组
	 * @param startRow
	 *            起始行号，以{@code 1}开始计数
	 * @param count
	 *            查询记录数
	 * @return
	 */
	SqlBuilder toPagingSql(SqlBuilder queryView, SqlBuilder condition, Order[] orders, long startRow, int count);

	/**
	 * 构建关键字SQL查询条件。
	 * <p>
	 * 返回{@code null}、或者空{@linkplain SqlBuilder}表示无关键字SQL查询条件。
	 * </p>
	 * 
	 * @param model
	 * @param query
	 *            此次查询
	 * @param queryColumnPropertyPaths
	 *            此次查询的结果集{@linkplain ColumnPropertyPath}列表
	 * @return
	 */
	SqlBuilder toKeywordQueryCondition(Model model, Query query,
			List<? extends ColumnPropertyPath> queryColumnPropertyPaths);

	/**
	 * 给定SQL类型的列是否是可排序的。
	 * 
	 * @param sqlType
	 * @return
	 */
	boolean isSortable(int sqlType);
}
