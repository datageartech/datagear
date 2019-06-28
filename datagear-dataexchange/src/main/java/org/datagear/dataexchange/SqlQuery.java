/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 * SQL {@linkplain Query}。
 * 
 * @author datagear@163.com
 *
 */
public class SqlQuery extends AbstractQuery
{
	private String sql;

	public SqlQuery()
	{
		super();
	}

	public SqlQuery(String sql)
	{
		super();
		this.sql = sql;
	}

	public String getSql()
	{
		return sql;
	}

	public void setSql(String sql)
	{
		this.sql = sql;
	}

	@Override
	public ResultSet execute(Connection cn) throws Throwable
	{
		return executeQuery(cn, this.sql);
	}
}
