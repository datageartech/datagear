/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.dbinfo.DevotedDatabaseInfoResolver;
import org.datagear.dbinfo.GenericDatabaseInfoResolver;
import org.datagear.dbinfo.WildcardDevotedDatabaseInfoResolver;
import org.datagear.util.JdbcUtil;

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

	protected DataSource buildTestDataSource()
	{
		return new DataSource()
		{
			@Override
			public <T> T unwrap(Class<T> iface) throws SQLException
			{
				throw new UnsupportedExchangeException();
			}

			@Override
			public boolean isWrapperFor(Class<?> iface) throws SQLException
			{
				throw new UnsupportedExchangeException();
			}

			@Override
			public void setLoginTimeout(int seconds) throws SQLException
			{
				throw new UnsupportedExchangeException();
			}

			@Override
			public void setLogWriter(PrintWriter out) throws SQLException
			{
				throw new UnsupportedExchangeException();
			}

			@Override
			public int getLoginTimeout() throws SQLException
			{
				throw new UnsupportedExchangeException();
			}

			@Override
			public PrintWriter getLogWriter() throws SQLException
			{
				throw new UnsupportedExchangeException();
			}

			@Override
			public Connection getConnection(String username, String password) throws SQLException
			{
				throw new UnsupportedExchangeException();
			}

			@Override
			public Connection getConnection() throws SQLException
			{
				return DataexchangeTestSupport.this.getConnection();
			}

			// @Override
			public Logger getParentLogger() throws SQLFeatureNotSupportedException
			{
				throw new SQLFeatureNotSupportedException();
			}
		};
	}

	protected ResourceFactory<Reader> getTestReaderResourceFactory(String resourceName) throws IOException
	{
		return ClasspathReaderResourceFactory.valueOf(getResourceClasspath(resourceName), "UTF-8");
	}

	protected ResourceFactory<InputStream> getTestInputStreamResourceFactory(String resourceName) throws IOException
	{
		return ClasspathInputStreamResourceFactory.valueOf(getResourceClasspath(resourceName));
	}

	protected String getResourceClasspath(String resourceName)
	{
		return "org/datagear/dataexchange/" + resourceName;
	}

	protected class MockValueDataImportListener implements ValueDataImportListener
	{
		@Override
		public void onStart()
		{
			println("onStart");
		}

		@Override
		public void onException(DataExchangeException e)
		{
			println("onException : " + e.getMessage());
		}

		@Override
		public void onSuccess()
		{
			println("onSuccess");
		}

		@Override
		public void onFinish()
		{
			println("onFinish");
		}

		@Override
		public void onSuccess(DataIndex dataIndex)
		{
			println("onSuccess : " + dataIndex);
		}

		@Override
		public void onIgnore(DataIndex dataIndex, DataExchangeException e)
		{
			println("onIgnore : " + dataIndex);
		}

		@Override
		public void onSetNullColumnValue(DataIndex dataIndex, String columnName, Object columnValue,
				DataExchangeException e)
		{
			println("onSetNullColumnValue : " + dataIndex + ", " + columnName);
		}
	}
}
