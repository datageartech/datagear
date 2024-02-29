/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.util.test;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.datagear.util.FileUtil;

/**
 * 数据库测试支持类。
 * 
 * @author datagear@163.com
 *
 */
public abstract class DBTestSupport
{
	private static final Properties JDBC_PROPERTIES = new Properties();

	static
	{
		File jdbcConfigFile = FileUtil.getFile("test/config/jdbc.properties");
		if (!jdbcConfigFile.exists())
			jdbcConfigFile = FileUtil.getFile("../test/config/jdbc.properties");

		try
		{
			Reader reader = new FileReader(jdbcConfigFile);
			JDBC_PROPERTIES.load(reader);
			reader.close();
		}
		catch (Exception e)
		{
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			else
				throw new RuntimeException(e);
		}
	}

	protected Properties getProperties()
	{
		return JDBC_PROPERTIES;
	}

	protected String getUrl()
	{
		return JDBC_PROPERTIES.getProperty("jdbc.url");
	}

	protected String getUser()
	{
		return JDBC_PROPERTIES.getProperty("jdbc.user");
	}

	protected String getPassword()
	{
		return JDBC_PROPERTIES.getProperty("jdbc.password");
	}

	protected Connection getConnection() throws SQLException
	{
		return DriverManager.getConnection(JDBC_PROPERTIES.getProperty("jdbc.url"),
				JDBC_PROPERTIES.getProperty("jdbc.user"), JDBC_PROPERTIES.getProperty("jdbc.password"));
	}

	protected DataSource getDataSource() throws SQLException
	{
		return new DataSource()
		{
			@SuppressWarnings("unchecked")
			@Override
			public <T> T unwrap(Class<T> iface) throws SQLException
			{
				return (T) this;
			}

			@Override
			public boolean isWrapperFor(Class<?> iface) throws SQLException
			{
				return true;
			}

			@Override
			public void setLoginTimeout(int seconds) throws SQLException
			{
			}

			@Override
			public void setLogWriter(PrintWriter out) throws SQLException
			{
			}

			@Override
			public Logger getParentLogger() throws SQLFeatureNotSupportedException
			{
				return null;
			}

			@Override
			public int getLoginTimeout() throws SQLException
			{
				return 0;
			}

			@Override
			public PrintWriter getLogWriter() throws SQLException
			{
				return null;
			}

			@Override
			public Connection getConnection(String username, String password) throws SQLException
			{
				return getConnection();
			}

			@Override
			public synchronized Connection getConnection() throws SQLException
			{
				return DBTestSupport.this.getConnection();
			}
		};
	}

	protected void println()
	{
		System.out.println();
	}

	protected void println(Object o)
	{
		System.out.println((o == null ? "null" : o.toString()));
	}

	protected void print(Object o)
	{
		System.out.print((o == null ? "null" : o.toString()));
	}
}