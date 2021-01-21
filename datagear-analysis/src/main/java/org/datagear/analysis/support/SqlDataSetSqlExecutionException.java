/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
