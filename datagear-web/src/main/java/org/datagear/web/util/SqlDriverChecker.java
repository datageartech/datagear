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

package org.datagear.web.util;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.Statement;

import org.datagear.connection.AbstractDriverChecker;
import org.datagear.connection.ConnectionOption;
import org.datagear.connection.DriverChecker;
import org.datagear.meta.SimpleTable;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.util.JdbcUtil;

/**
 * 会执行测试SQL的{@linkplain DriverChecker}。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDriverChecker extends AbstractDriverChecker
{
	private DBMetaResolver dbMetaResolver;

	public SqlDriverChecker()
	{
		super();
	}

	public SqlDriverChecker(DBMetaResolver dbMetaResolver)
	{
		super();
		this.dbMetaResolver = dbMetaResolver;
	}

	public DBMetaResolver getDbMetaResolver()
	{
		return dbMetaResolver;
	}

	public void setDbMetaResolver(DBMetaResolver dbMetaResolver)
	{
		this.dbMetaResolver = dbMetaResolver;
	}

	@Override
	protected boolean checkConnection(Driver driver, ConnectionOption connectionOption) throws Throwable
	{
		Connection cn = null;

		try
		{
			cn = getConnection(driver, connectionOption);

			SimpleTable simpleTable = this.dbMetaResolver.getRandomDataTable(cn);

			// 如果不包含任何表，则可认为校验通过
			if (simpleTable == null)
				return true;

			executeSelectCountSqlTest(cn, simpleTable.getName());

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
