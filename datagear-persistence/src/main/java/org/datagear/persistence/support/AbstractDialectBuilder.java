/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.sql.SQLException;

import org.datagear.persistence.DialectBuilder;
import org.datagear.persistence.DialectException;

/**
 * 抽象{@linkplain DialectBuilder}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDialectBuilder extends PersistenceSupport implements DialectBuilder
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
