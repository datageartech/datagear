/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
		File jdbcConfigFile = new File("test/config/jdbc.properties");
		if (!jdbcConfigFile.exists())
			jdbcConfigFile = new File("../test/config/jdbc.properties");

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

	protected Connection getConnection() throws SQLException
	{
		return DriverManager.getConnection(JDBC_PROPERTIES.getProperty("jdbc.url"),
				JDBC_PROPERTIES.getProperty("jdbc.user"), JDBC_PROPERTIES.getProperty("jdbc.password"));
	}

	protected Connection getConnection(Properties properties) throws SQLException
	{
		properties.setProperty("user", JDBC_PROPERTIES.getProperty("jdbc.user"));
		properties.setProperty("password", JDBC_PROPERTIES.getProperty("jdbc.password"));

		return DriverManager.getConnection(JDBC_PROPERTIES.getProperty("jdbc.url"), properties);
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