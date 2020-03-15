/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 抽象{@linkplain DialectBuilder}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDialectBuilder implements DialectBuilder
{
	public AbstractDialectBuilder()
	{
		super();
	}

	/**
	 * 获取标识符引用符。
	 * 
	 * @param cn
	 * @return
	 */
	protected String getIdentifierQuote(Connection cn)
	{
		try
		{
			return cn.getMetaData().getIdentifierQuoteString();
		}
		catch (SQLException e)
		{
			throw new DialectException(e);
		}
	}
}
