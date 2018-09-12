/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.util;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.Statement;

import org.datagear.connection.AbstractDriverChecker;
import org.datagear.connection.ConnectionOption;
import org.datagear.connection.DriverChecker;
import org.datagear.connection.JdbcUtil;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.dbinfo.TableInfo;

/**
 * 会执行测试SQL的{@linkplain DriverChecker}。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDriverChecker extends AbstractDriverChecker
{
	private DatabaseInfoResolver databaseInfoResolver;

	public SqlDriverChecker()
	{
		super();
	}

	public SqlDriverChecker(DatabaseInfoResolver databaseInfoResolver)
	{
		super();
		this.databaseInfoResolver = databaseInfoResolver;
	}

	public DatabaseInfoResolver getDatabaseInfoResolver()
	{
		return databaseInfoResolver;
	}

	public void setDatabaseInfoResolver(DatabaseInfoResolver databaseInfoResolver)
	{
		this.databaseInfoResolver = databaseInfoResolver;
	}

	@Override
	protected boolean checkConnection(Driver driver, ConnectionOption connectionOption) throws Throwable
	{
		Connection cn = null;

		try
		{
			cn = getConnection(driver, connectionOption);

			TableInfo tableInfo = this.databaseInfoResolver.getRandomTableInfo(cn);

			// 如果不包含任何表，则可认为校验通过
			if (tableInfo == null)
				return true;

			executeSelectCountSqlTest(cn, tableInfo.getName());

			return true;
		}
		finally
		{
			closeConnection(cn);
		}
	}

	/**
	 * 执行“SELECT COUNT(*)”测试。
	 * 
	 * @param cn
	 * @param table
	 * @throws Throwable
	 */
	protected void executeSelectCountSqlTest(Connection cn, String table) throws Throwable
	{
		Statement st = null;

		try
		{
			st = cn.createStatement();
			st.executeQuery("SELECT COUNT(*) FROM " + table);
		}
		finally
		{
			JdbcUtil.closeStatement(st);
		}
	}
}
