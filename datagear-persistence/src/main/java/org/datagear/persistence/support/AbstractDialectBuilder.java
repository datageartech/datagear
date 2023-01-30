/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
