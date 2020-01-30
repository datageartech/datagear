/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.util;

import java.io.File;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Scanner;

import org.datagear.util.IOUtil;
import org.datagear.util.JdbcUtil;
import org.datagear.util.SqlScriptParser;
import org.datagear.util.SqlScriptParser.SqlStatement;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Derby数据库SQL客户端。
 * 
 * @author datagear@163.com
 *
 */
public class DerbySqlClient
{
	public static void main(String[] args) throws Exception
	{
		ClassPathXmlApplicationContext applicationContext = null;
		Connection cn = null;

		try
		{
			applicationContext = new ClassPathXmlApplicationContext(
					"org/datagear/web/datagear-propertyConfigurer.xml, datagear-dataSource.xml");

			DriverManagerDataSource dataSource = applicationContext.getBean(DriverManagerDataSource.class);

			println("*****************************************");
			println("Derby SQL client");
			println("URL :" + dataSource.getUrl());
			println("*****************************************");
			println();
			println("Print SQL for executing single sql");
			println("Print @<sql-file-path> for executing sql scripts");
			println("Print [exist] for exist");

			Scanner scanner = new Scanner(System.in);
			cn = dataSource.getConnection();

			while (scanner.hasNextLine())
			{
				String input = scanner.nextLine().trim();

				if (input.isEmpty())
					;
				else if ("exit".equalsIgnoreCase(input))
				{
					println("Bye!");
					IOUtil.close(scanner);
					System.exit(0);
				}
				else if (input.startsWith("@"))
				{
					String filePath = input.substring(1);

					println("start execute sql scripts in file :" + filePath);

					Reader reader = null;

					try
					{
						reader = IOUtil.getReader(new File(filePath), "UTF-8");
						SqlScriptParser sqlScriptParser = new SqlScriptParser(reader);
						SqlStatement sql = null;

						while ((sql = sqlScriptParser.parseNext()) != null)
						{
							println(sql.getSql());
							executeSql(cn, sql.getSql());
						}
					}
					catch (Throwable t)
					{
						t.printStackTrace();
					}
					finally
					{
						IOUtil.close(reader);
					}

					println("finish execute sql scripts in file :" + filePath);
				}
				else
				{
					executeSql(cn, input);
				}
			}
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
			applicationContext.close();
		}
	}

	protected static void executeSql(Connection cn, String sql)
	{
		Statement st = null;

		try
		{
			st = cn.createStatement();

			boolean query = st.execute(sql);

			println("-----------------------------------------");
			if (!query)
			{
				println(st.getUpdateCount() + " records affected.");
			}
			else
			{
				ResultSet rs = st.getResultSet();

				ResultSetMetaData metaData = rs.getMetaData();
				int columnCount = metaData.getColumnCount();

				for (int i = 1; i <= columnCount; i++)
				{
					String columnLabel = metaData.getColumnLabel(i);

					if (i > 1)
						print(", ");

					print(columnLabel);
				}

				println();

				while (rs.next())
				{
					for (int i = 1; i <= columnCount; i++)
					{
						Object cv = rs.getObject(i);

						if (i > 1)
							print(", ");

						print(cv);
					}

					println();
				}
			}
			println("-----------------------------------------");
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		finally
		{
			JdbcUtil.closeStatement(st);
		}
	}

	protected static void print(Object o)
	{
		String str = "NULL";

		if (o == null)
			;
		else if (o instanceof String)
			str = (String) o;
		else
			str = o.toString();

		System.out.print(str);
	}

	protected static void println(Object o)
	{
		String str = "NULL";

		if (o == null)
			;
		else if (o instanceof String)
			str = (String) o;
		else
			str = o.toString();

		System.out.println(str);
	}

	protected static void println()
	{
		System.out.println();
	}
}
