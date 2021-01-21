/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

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
	public ResultSet execute(Connection cn) throws Throwable
	{
		DatabaseMetaData metaData = cn.getMetaData();

		String quote = metaData.getIdentifierQuoteString();

		String sql = "SELECT * FROM " + quote + this.table + quote;

		return executeQuery(cn, sql);
	}
}
