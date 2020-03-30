/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.sql.ResultSet;

import org.datagear.meta.Table;
import org.datagear.util.Sql;

/**
 * 数据库方言。
 * 
 * @author datagear@163.com
 *
 */
public interface Dialect
{
	/** 默认作为关键字查询的列数。 */
	int DEFAULT_KEYWORD_QUERY_COLUMN_COUNT = 3;

	/**
	 * 为给定名字添加标识符引用符号。
	 * 
	 * @param name
	 * @return
	 */
	String quote(String name);

	/**
	 * 给定名字是否已添加标识符引用符号。
	 * 
	 * @param name
	 * @return
	 */
	boolean isQuoted(String name);

	/**
	 * 移除给定名字的标识符引用符号。
	 * <p>
	 * 如果没有，则返回原名字。
	 * </p>
	 * 
	 * @param name
	 * @return
	 */
	String unquote(String name);

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
	 * 如果由于参数不合法等原因无法构建分页查询SQL，返回{@code null}。
	 * </p>
	 * 
	 * @param query
	 *            查询SQL
	 * @param orders
	 *            排序集，允许为{@code null}或者空数组
	 * @param startRow
	 *            起始行号，以{@code 1}开始计数
	 * @param count
	 *            查询记录数
	 * @return
	 */
	Sql toPagingQuerySql(Sql query, Order[] orders, long startRow, int count);

	/**
	 * 获取作为关键字查询的列数。
	 * 
	 * @return
	 */
	int getKeywordQueryColumnCount();

	/**
	 * 构建关键字SQL查询条件。
	 * <p>
	 * 返回{@code null}、或者空{@linkplain Sql}表示无关键字SQL查询条件。
	 * </p>
	 * 
	 * @param table
	 * @param query
	 *            此次查询
	 * @param parameterized
	 *            是否参数化条件，如果为{@code false}，将返回原生的SQL语句（不包含{@code "?"}参数化标识）
	 * @return
	 */
	Sql toKeywordQueryCondition(Table table, Query query, boolean parameterized);

	/**
	 * 构建排序SQL，格式为：{@code "... ORDER BY ..."}。
	 * 
	 * @param query
	 * @param orders
	 *            允许为{@code null}
	 * @return
	 */
	Sql toOrderSql(Sql query, Order[] orders);
}
