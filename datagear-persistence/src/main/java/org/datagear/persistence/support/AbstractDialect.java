/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import org.datagear.persistence.Dialect;
import org.datagear.persistence.Order;
import org.datagear.persistence.SqlBuilder;

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
