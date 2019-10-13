/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Types;

import org.datagear.persistence.Dialect;
import org.datagear.persistence.Order;
import org.datagear.persistence.SqlBuilder;
import org.datagear.util.JDBCCompatiblity;

/**
 * 抽象{@linkplain Dialect}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDialect implements Dialect
{
	/** 标识引用符 */
	private String identifierQuote;

	public AbstractDialect()
	{
		super();
	}

	public AbstractDialect(String identifierQuote)
	{
		super();
		this.identifierQuote = identifierQuote;
	}

	@Override
	public String getIdentifierQuote()
	{
		return identifierQuote;
	}

	public void setIdentifierQuote(String identifierQuote)
	{
		this.identifierQuote = identifierQuote;
	}

	@Override
	public String quote(String name)
	{
		return this.identifierQuote + name + this.identifierQuote;
	}

	@Override
	@JDBCCompatiblity("某些驱动程序对有些类型不支持排序（比如Oracle对于BLOB类型）")
	public boolean isSortable(int sqlType)
	{
		if (Types.BIGINT == sqlType || Types.BIT == sqlType || Types.BOOLEAN == sqlType || Types.CHAR == sqlType
				|| Types.DATE == sqlType || Types.DECIMAL == sqlType || Types.DOUBLE == sqlType
				|| Types.FLOAT == sqlType || Types.INTEGER == sqlType || Types.NCHAR == sqlType
				|| Types.NUMERIC == sqlType || Types.NVARCHAR == sqlType || Types.REAL == sqlType
				|| Types.SMALLINT == sqlType || Types.TIME == sqlType || Types.TIMESTAMP == sqlType
				|| Types.TINYINT == sqlType || Types.VARCHAR == sqlType)
			return true;

		return false;
	}

	/**
	 * 转换为排序SQL。
	 * 
	 * @param orders
	 * @return
	 * @see Order#toOrderSql(Order...)
	 */
	protected SqlBuilder toOrderSql(Order... orders)
	{
		return Order.toOrderSql(orders);
	}

	/**
	 * SQL是否为空。
	 * 
	 * @param sql
	 * @return
	 */
	protected boolean isEmptySql(SqlBuilder sql)
	{
		return sql == null || sql.isEmpty();
	}
}
