/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.datagear.connection.JdbcUtil;
import org.datagear.dataexchange.support.CsvDataImporterTest;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.dbinfo.DevotedDatabaseInfoResolver;
import org.datagear.dbinfo.GenericDatabaseInfoResolver;
import org.datagear.dbinfo.WildcardDevotedDatabaseInfoResolver;

/**
 * 数据交换测试支持类。
 * 
 * @author datagear@163.com
 *
 */
public abstract class DataexchangeTestSupport extends DBTestSupport
{
	protected DatabaseInfoResolver databaseInfoResolver;

	public DataexchangeTestSupport()
	{
		super();

		List<DevotedDatabaseInfoResolver> devotedDatabaseInfoResolver = new ArrayList<DevotedDatabaseInfoResolver>();
		devotedDatabaseInfoResolver.add(new WildcardDevotedDatabaseInfoResolver());
		this.databaseInfoResolver = new GenericDatabaseInfoResolver(devotedDatabaseInfoResolver);
	}

	protected void clearTable(Connection cn, String table) throws SQLException
	{
		Statement st = null;

		try
		{
			st = cn.createStatement();
			st.executeUpdate("delete from " + table);
		}
		finally
		{
			JdbcUtil.closeStatement(st);
		}
	}

	protected int getCount(Connection cn, String table) throws SQLException
	{
		Statement st = null;
		ResultSet rs = null;

		try
		{
			st = cn.createStatement();
			rs = st.executeQuery("select count(*) from " + table);

			int count = 0;

			if (rs.next())
				count = rs.getInt(1);

			return count;
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(st);
		}
	}

	protected InputStream getTestResourceInputStream(String resourceName) throws IOException
	{
		return CsvDataImporterTest.class.getClassLoader()
				.getResourceAsStream("org/datagear/dataexchange/support/" + resourceName);
	}

	protected SimpleDataSource buildSimpleDataSource(Connection cn)
	{
		return new SimpleDataSource(cn);
	}

	protected static class SimpleDataSource implements DataSource
	{
		private Connection connection;

		public SimpleDataSource(Connection connection)
		{
			super();
			this.connection = connection;
		}

		public void setConnection(Connection connection)
		{
			this.connection = connection;
		}

		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException
		{
			throw new UnsupportedExportException();
		}

		@Override
		public boolean isWrapperFor(Class<?> iface) throws SQLException
		{
			throw new UnsupportedExportException();
		}

		@Override
		public PrintWriter getLogWriter() throws SQLException
		{
			throw new UnsupportedExportException();
		}

		@Override
		public void setLogWriter(PrintWriter out) throws SQLException
		{
			throw new UnsupportedExportException();
		}

		@Override
		public void setLoginTimeout(int seconds) throws SQLException
		{
			throw new UnsupportedExportException();
		}

		@Override
		public int getLoginTimeout() throws SQLException
		{
			throw new UnsupportedExportException();
		}

		@Override
		public Connection getConnection() throws SQLException
		{
			return this.connection;
		}

		@Override
		public Connection getConnection(String username, String password) throws SQLException
		{
			throw new UnsupportedExportException();
		}

	}
}
