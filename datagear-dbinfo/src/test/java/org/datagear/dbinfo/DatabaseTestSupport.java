/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbinfo;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 单元测试支持类。
 * 
 * @author datagear@163.com
 *
 */
public class DatabaseTestSupport
{
	public DatabaseTestSupport()
	{
		super();
	}

	protected Connection getMysqlConnection() throws Exception
	{
		String url = "jdbc:mysql://127.0.0.1:3306/datagear?useUnicode=true&amp;characterEncoding=UTF-8";

		return DriverManager.getConnection(url, "root", "");
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
