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
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.meta.resolver.GenericDBMetaResolver;
import org.datagear.util.IOUtil;
import org.datagear.util.JdbcUtil;
import org.datagear.util.resource.ClasspathInputStreamResourceFactory;
import org.datagear.util.resource.ClasspathReaderResourceFactory;
import org.datagear.util.resource.ResourceFactory;
import org.datagear.util.test.DBTestSupport;

/**
 * 数据交换测试支持类。
 * 
 * @author datagear@163.com
 *
 */
public abstract class DataexchangeTestSupport extends DBTestSupport
{
	public static final String TABLE_NAME_DATA_IMPORT = "T_DATA_IMPORT";
	public static final String TABLE_NAME_DATA_EXPORT = "T_DATA_EXPORT";
	public static final String TABLE_NAME_UNSIGNED_NUMBER = "T_UNSIGNED_NUMBER";

	protected DBMetaResolver dbMetaResolver;

	public DataexchangeTestSupport()
	{
		super();
		this.dbMetaResolver = new GenericDBMetaResolver();
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
			@Override
			public Logger getParentLogger() throws SQLFeatureNotSupportedException
			{
				throw new SQLFeatureNotSupportedException();
			}
		};
	}

	protected ResourceFactory<Reader> getTestReaderResourceFactory(String resourceName) throws IOException
	{
		return ClasspathReaderResourceFactory.valueOf(getResourceClasspath(resourceName), IOUtil.CHARSET_UTF_8);
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
