/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

import java.sql.Connection;

import org.datagear.util.JdbcUtil;
import org.datagear.util.QueryResultSet;

/**
 * 表{@linkplain Query}。
 * 
 * @author datagear@163.com
 *
 */
public class TableQuery extends AbstractQuery
{
	private String table;

	public TableQuery()
	{
		super();
	}

	public TableQuery(String table)
	{
		super();
		this.table = table;
	}

	public String getTable()
	{
		return table;
	}

	public void setTable(String table)
	{
		this.table = table;
	}

	@Override
	public QueryResultSet execute(Connection cn) throws Throwable
	{
		String sql = "SELECT * FROM " + JdbcUtil.quote(this.table, cn);
		return executeQueryValidation(cn, sql);
	}
}
