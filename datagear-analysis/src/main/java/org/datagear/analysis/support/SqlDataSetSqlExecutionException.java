/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;

/**
 * {@linkplain SqlDataSet}执行SQL异常。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetSqlExecutionException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	private String sql;

	public SqlDataSetSqlExecutionException(String sql)
	{
		super();
		this.sql = sql;
	}

	public SqlDataSetSqlExecutionException(String sql, String message)
	{
		super(message);
		this.sql = sql;
	}

	public SqlDataSetSqlExecutionException(String sql, Throwable cause)
	{
		super(cause);
		this.sql = sql;
	}

	public SqlDataSetSqlExecutionException(String sql, String message, Throwable cause)
	{
		super(message, cause);
		this.sql = sql;
	}

	public String getSql()
	{
		return sql;
	}

	protected void setSql(String sql)
	{
		this.sql = sql;
	}
}
