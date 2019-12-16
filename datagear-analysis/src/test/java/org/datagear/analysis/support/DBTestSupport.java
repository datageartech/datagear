/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.analysis.support;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

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

	protected Connection getConnection(Properties properties) throws Exception
	{
		properties.setProperty("user", JDBC_PROPERTIES.getProperty("jdbc.user"));
		properties.setProperty("password", JDBC_PROPERTIES.getProperty("jdbc.password"));

		return DriverManager.getConnection(JDBC_PROPERTIES.getProperty("jdbc.url"), properties);
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
